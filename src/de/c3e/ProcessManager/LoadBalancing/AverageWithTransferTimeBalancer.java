package de.c3e.ProcessManager.LoadBalancing;

import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.BlockRepository.BlockRepository;
import de.c3e.ProcessManager.DataTypes.BlockIO;
import de.c3e.ProcessManager.DataTypes.GraphBlock;
import de.c3e.ProcessManager.Utils.*;
import de.c3e.ProcessManager.WorkerManager.CalculationAllocation;
import de.c3e.ProcessManager.WorkerManager.IWorker;
import de.c3e.ProcessManager.WorkerManager.RemoteWorkerMasterSide;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AverageWithTransferTimeBalancer extends AverageExecutionTimeBalancer  implements IAdvancedBalancer
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    @Override
    public List<ImageSubSet> EstimateCalculationSubsetsForTime(String workType, int origin, double timeInSeconds, AbstractImageObject img, SplitType splitType, long requesterId)
    {
        long pixelForLocalData = EstimatePixelCountForTime(workType, timeInSeconds);

        long desiredPixelCount = this.RestrictDesiredPixelsByImageSize(pixelForLocalData, img.getFullSizePixelCount());
        List<ImageSubSet> localParts = img.GetSubset(desiredPixelCount,splitType,requesterId, origin);

        long pixelsFromLocal = SubSetFinder.NumberOfPixels(localParts);
        long stillNeeded = desiredPixelCount - pixelsFromLocal;
        if (stillNeeded <= 0)
        {
            // satisfy requested calculation purely from  local data.
            return localParts;
        }

        double timeFromLocal = EstimateTimeFromPixelCount(workType, pixelsFromLocal);
        double timeLeft =  timeInSeconds - timeFromLocal;

        long tryThisMany = 0;
        int increment = (int) Math.max(0.05*pixelForLocalData, img.getFullSizeX() );
        double transferTime;
        double calculationTime;
        for (; tryThisMany < stillNeeded ; tryThisMany+= increment )
        {
            transferTime = EstimateTransferDuration(tryThisMany);
            calculationTime = EstimateTimeFromPixelCount(workType, tryThisMany);
            double overallTime = transferTime + calculationTime;
            if ( overallTime > timeLeft)
            {   break;  }
        }
        desiredPixelCount = tryThisMany;

        logger.info("Estimate for " + timeInSeconds + " Seconds on Node " + origin + " : " + pixelForLocalData + " local pixels at " + timeFromLocal +
                " + " + tryThisMany + " remote pixels at "  + EstimateTransferDuration(tryThisMany) + " transfer time and " + EstimateTimeFromPixelCount(workType, tryThisMany) + " calculation time" );

        List<ImageSubSet> remoteParts = img.GetSubset(desiredPixelCount,splitType,requesterId);

        List<ImageSubSet> result = new ArrayList<>();
        if (localParts != null)
        {
            result.addAll(localParts);
        }

        if(remoteParts != null)
        {
            result.addAll(remoteParts);
        }
        return result;
    }

    private double EstimateTimeFromPixelCount(String workType, long pixels)
    {
        // todo: technicaly not correct ... assumes linear relationship
        // use 10 seconds as that is a time span the system tries to optimize for => statistics best in that area
        double pixelsIn10Second = EstimatePixelCountForTimeLogging(workType, 10,false);
        return (pixels *10)  / pixelsIn10Second;
    }

    private double EstimateTransferDuration(long pixels)
    {
        if (this.statistics == null)
        {
            return 0.0;
        }

        // clone the keys, so I don't have to lock an object that does not belong to me
        HashSet<Integer> keys = new HashSet<>(this.statistics.keySet());
        List<ExecutionTimeMeasurementEntry> measurements = new ArrayList<>();
        for (Integer k : keys)
        {
            measurements.addAll(this.statistics.get(k).EntriesOfMeasurementTyp(ExecutionTimeMeasurementEntry.MeasuredType.TransportImageDataSend));
        }

        if (measurements.size() == 0)
        {
            logger.warn("No transfer statistics => assume free transfer");
            return 0.0;
        }


        IApproximator approximator = new LinearApproximator();
        for(ExecutionTimeMeasurementEntry e : measurements)
        {
            approximator.AddDataPoints(e.getInputPixelCount(), e.getTimeNeeded().toUnit(TimeUnit.SECONDS));
        }

        double transferTime = approximator.Approximate(pixels);

        if (Double.isNaN(transferTime))
        {
            transferTime = 0.0;
        }

        //Log("\tEstimated for " + pixels +  " pixels transferred\t" + transferTime);
        // start with an estimate
        return transferTime ;
    }

    private BlockRepository repository = new BlockRepository();

    @Override
    public Queue<CalculationAllocation> ScheduleBlockOnWorkers(GraphBlock block, Queue<IWorker> workers, long desiredBlockTime)
    {
        if (workers == null || workers.size() < 1)
        {   return  new LinkedList<>(); }

        IWorkBlock executor = repository.getWorkBlockFromType(block.Type);
        SplitType splitType = SplitType.cantSplit;
        if (executor instanceof ISupportsSplitting)
        {
            splitType = ((ISupportsSplitting)executor).getSplitType();
        }

        AbstractImageObject v = null;
        for(BlockIO input :block.Inputs)
        {
            Object value = input.getValue();
            if (input.isValid() && value instanceof AbstractImageObject)
            {
                if (v != null)
                {   DebugHelper.BreakIntoDebug();// don't support multi image calculations yet
                }
                v = (AbstractImageObject)value;
            }
        }


        Queue<CalculationAllocation> result = new LinkedList<>();
        if (v == null)
        {
            CalculationAllocation aloc = new CalculationAllocation(block);
            aloc.worker = workers.poll();
            aloc.OriginId = block.uniqueScheduleId;
            result.add(aloc);
            return result;
        }

        List<IWorker> workersAsList = new ArrayList<>(workers);
        if (splitType == SplitType.cantSplit)
        {
            long maxPixels = 0;
            IWorker maxWorker = null;
            for (IWorker w : workersAsList)
            {
                int id = w.getId();
                if (w instanceof RemoteWorkerMasterSide)
                {id=((RemoteWorkerMasterSide)w).getRemoteId();}

                long localThere = v.getLocalSizePixelCount(id);
                if (localThere > maxPixels)
                {
                    maxPixels=localThere;
                    maxWorker = w;
                }
            }

            if (maxWorker == null)
            {   maxWorker = workers.poll(); }

            CalculationAllocation aloc = new CalculationAllocation(block);
            aloc.worker =maxWorker;
            aloc.OriginId = block.uniqueScheduleId;
            aloc.imageAreas = v.getFullBoundsL();
            result.add(aloc);
            return result;
        }



        HashMap<IWorker, String> local = new HashMap<>();
        HashMap<IWorker, String> remote = new HashMap<>();


        long calcsize = this.EstimatePixelCountForTimeLogging(block.Type,desiredBlockTime,false);
        calcsize = this.RestrictDesiredPixelsByImageSize(calcsize,v.getFullSizePixelCount());
        List<CalculationAllocation> tmpResult = new ArrayList<>();

        // first: schedule  desired time on all nodes, that have local data
        for(IWorker w : workersAsList)
        {
            int id = w.getId();
            if (w instanceof RemoteWorkerMasterSide)
            {id=((RemoteWorkerMasterSide)w).getRemoteId();}

            CalculationAllocation aloc = new CalculationAllocation(block);
            aloc.worker = w;
            aloc.imageAreas = v.GetSubset(calcsize,splitType,block.uniqueScheduleId , id);
            if (aloc.imageAreas == null)
            {   continue;   }

            long pixels = SubSetFinder.NumberOfPixels(aloc.imageAreas);
            double calculationTime = this.EstimateTimeFromPixelCount(block.Type,pixels);
            local.put(w,String.valueOf(pixels) + "(" + calculationTime + ")");


            aloc.OriginId = block.uniqueScheduleId;
            tmpResult.add(aloc);
        }

        // fill up blocks with remote work
       for(CalculationAllocation a : tmpResult)
        {
            long pixels = SubSetFinder.getPixelSize(a.imageAreas);
            double usedTime = this.EstimateTimeFromPixelCount(block.Type,pixels);
            double freeTime = desiredBlockTime - usedTime;

            long additional = this.EstimatePixelCountForTimeWithTransfer(freeTime, block.Type);
            List<ImageSubSet> add = v.GetSubset(additional,splitType,block.uniqueScheduleId);
            if (add != null)
            {
                a.imageAreas.addAll(add);
                long p = SubSetFinder.NumberOfPixels(add);
                double calculationTime = this.EstimateTimeFromPixelCount(block.Type,p);
                double transferTime = this.EstimateTransferDuration(p);
                remote.put(a.worker,String.valueOf(p) + " (" + calculationTime + " C:T " + transferTime + ")");
            }
        }

        for(CalculationAllocation a : tmpResult)
        {
            if ((a.imageAreas != null) && (a.imageAreas.size() > 0))
            {
                result.add(a);
            }
        }

        StringBuilder logString = new StringBuilder();
        logString.append("\tEstimated for \t");
        logString.append(desiredBlockTime);
        logString.append("\tseconds with type\t");
        logString.append( block.Type);
        for (IWorker w : workersAsList)
        {
            logString.append("\tWorker\t");
            int id = w.getId();
            if (w instanceof RemoteWorkerMasterSide)
            {id=((RemoteWorkerMasterSide)w).getRemoteId();}
            logString.append(id);
            logString.append("\tlocal\t");
            logString.append(local.get(w));
            logString.append("\tremote\t");
            logString.append(remote.get(w));
        }

        Log(logString.toString());
        return result;
    }

    private long EstimatePixelCountForTimeWithTransfer(double freeTime, String type)
    {
        long pixels = (long)(this.EstimatePixelCountForTimeLogging(type,freeTime,false)*0.5);
        long increment = (long)(pixels*0.5);

        final long divisionSteps = 5;


        double sumTime = 0.0;
        boolean allwaysSmaller = true;
        for (int i=0;i<divisionSteps;i++)
        {
            double calculationTime = this.EstimateTimeFromPixelCount(type,pixels);
            double transferTime = this.EstimateTransferDuration(pixels);
            sumTime = calculationTime + transferTime;
            if (sumTime > freeTime)
            {
                pixels -= increment;
                increment = (long)(increment*0.5);
            }
            else
            {
                pixels += increment;
                increment = (long)(increment*0.5);
                allwaysSmaller = false;
            }
        }

        // special case for deciding to do nothing => for local worker
        if (sumTime > freeTime && allwaysSmaller)
        {
            return 0;
        }

        return pixels;
    }

}
