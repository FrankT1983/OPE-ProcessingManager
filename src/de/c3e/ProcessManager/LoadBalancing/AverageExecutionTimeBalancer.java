package de.c3e.ProcessManager.LoadBalancing;

import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.BlockRepository.BlockRepository;
import de.c3e.ProcessManager.DataTypes.BlockIO;
import de.c3e.ProcessManager.DataTypes.GraphBlock;
import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.Utils.*;

import de.c3e.ProcessManager.WorkerManager.CalculationAllocation;
import de.c3e.ProcessManager.WorkerManager.IWorker;
import de.c3e.ProcessManager.WorkerManager.RemoteWorkerMasterSide;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Load balancing class that uses previous run statistics of a given block, to estimate its execution time.
 *
 * This version will calculate the average time the execution needed to compute its final result.
 */
public class AverageExecutionTimeBalancer implements IBalancer
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    protected Map<Integer, ExecutionTimeMeasurements> statistics ;

    @Override
    public long EstimatePixelCountForTime(String type, double timeInSeconds)
    {
        return this.EstimatePixelCountForTimeLogging(type,timeInSeconds,true);
    }

    public long EstimatePixelCountForTimeLogging(String type, double timeInSeconds, boolean log)
    {
        if (this.statistics == null)
        {
            return 1000;
        }

        // clone the keys, so I don't have to lock an object that does not belong to me
        HashSet<Integer> keys = new HashSet<>(this.statistics.keySet());
        List<ExecutionTimeMeasurementEntry> measurements = new ArrayList<>();
        for (Integer k : keys)
        {
            measurements.addAll(this.statistics.get(k).EntriesOfBlockType(type));
        }

        IApproximator approximator = new LinearApproximator();
        long pixelCount = 1000;
        IntSet foo = new IntSet();
        boolean wasRandom = false;
        if (measurements.size() > 0)
        {
            for(ExecutionTimeMeasurementEntry e : measurements)
            {
                approximator.AddDataPoints(e.getTimeNeeded().toUnit(TimeUnit.SECONDS), e.getInputPixelCount());
                foo.add((int)e.getInputPixelCount());
            }

            if (foo.size() < 10)
            {
                double random = (Math.random() * 10000) + 1000;
                pixelCount = (long)random;
                wasRandom =true;

            }
            else
            {
                pixelCount = (long)approximator.Approximate(timeInSeconds);
            }
        }

        if (log)
        {
            Log("\tEstimated for " + timeInSeconds + " seconds with type \t" + type + "\t" + pixelCount + "\tpixels \t was done randomly:" + wasRandom);
        }
        return Math.max(1 , pixelCount) ;
    }

    private static final String JsonEntryString = "Entries";
    @Override
    public void SaveStatistics(String filePath)
    {
        logger.info("Save statistics for Average Execution Time Balancer from: " + filePath );
        List<ExecutionTimeMeasurementEntry> allEntrise = new ArrayList<>();
        for (ExecutionTimeMeasurements m: this.statistics.values())
        {
            allEntrise.addAll(m.get());
        }

        List<Map<String,Object>> es = new ArrayList<>();
        for (ExecutionTimeMeasurementEntry e : allEntrise)
        {
            es.add(e.toJsonSerializableForm());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("Entries", es);

        JSONObject mapJson = new JSONObject(result);
        try(BufferedWriter w = new BufferedWriter(new FileWriter(filePath)))
        {
            mapJson.writeJSONString(w);
        }
        catch(IOException e)
        {
            logger.error("Could not open Balancer data file " + filePath + " " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void LoadStatistics(String filePath)
    {
        logger.info("Load statistics for Average Execution Time Balancer from: " + filePath );
        File f = new File(filePath);
        if(!f.exists() || f.isDirectory()) {
            return;
        }


        JSONObject data ;
        try
        {
            JSONParser jsonParser = new JSONParser();

            Object object = jsonParser.parse(new FileReader(filePath));
            data = (JSONObject) object;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return;
        }

        ExecutionTimeMeasurements loadedEntries = new ExecutionTimeMeasurements();
        loadedEntries.setWorkerId(-1);
        Object e =  data.get(JsonEntryString);
        if (e instanceof JSONArray)
        {
            JSONArray foo = (JSONArray) e;
            for (Object o : foo)
            {
                if (o instanceof JSONObject)
                {
                    ExecutionTimeMeasurementEntry measurementEntry = ExecutionTimeMeasurementEntry.FromJson((JSONObject) o);
                    loadedEntries.add(measurementEntry);
                }
            }
        }

        this.statistics.put(-1,loadedEntries);
    }
    public static double MinPortion = 0.02;

    public long RestrictDesiredPixelsByImageSize(long desiredPixelCount, long fullSizePixelCount)
    {
        // don't hand out pieces smaller then 5% of the image => the overhead will be to great
        long res = desiredPixelCount;
        if (fullSizePixelCount * MinPortion > desiredPixelCount)

        {
            logger.info("Up desired pixels from " + res + " to " + ((int)(MinPortion*100)) +"% of image size to restrict overhead.");
            res = (long)(fullSizePixelCount * MinPortion);
        }


        // don't try to hand out more pixels, than there are in the image
        res = Math.min(res,fullSizePixelCount);

        // limit to 10 megabyte of double data
        //res = Math.min(res, 1024*1024 / 4 *10);

        return res;
    }

    public void RegisterExecutionStatistics(Map<Integer, ExecutionTimeMeasurements> statistics)
    {
        this.statistics = statistics;
    }

    protected static void Log(String toLog)
    {
        logger.debug(toLog);

        Writer output;
        try {

            output = new BufferedWriter(new FileWriter(GlobalSettings.WorkFolder + "Balancer.log",true));  //clears file every time
            output.append(toLog).append("\n");
            output.close();
        }catch (IOException e) {
            DebugHelper.BreakIntoDebug();
        }
    }


    private BlockRepository repository = new BlockRepository();

    @Override
    public Queue<CalculationAllocation> ScheduleBlockOnWorkers(GraphBlock block, Queue<IWorker> workers, long desiredBlockTime)
    {
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

        if (splitType == SplitType.cantSplit)
        {
            CalculationAllocation aloc = new CalculationAllocation(block);
            aloc.worker = workers.poll();
            aloc.OriginId = block.uniqueScheduleId;
            aloc.imageAreas = v.getFullBoundsAndMarkRequested(block.uniqueScheduleId);
            aloc.splitType = splitType.type;
            result.add(aloc);
            return result;
        }

        long calcsize = this.EstimatePixelCountForTime(block.Type,desiredBlockTime);
        calcsize = this.RestrictDesiredPixelsByImageSize(calcsize,v.getFullSizePixelCount());
        while (!workers.isEmpty())
        {
            IWorker w = workers.poll();
            CalculationAllocation aloc = new CalculationAllocation(block);
            aloc.worker = w;
            aloc.imageAreas = v.GetSubset(calcsize,splitType,block.uniqueScheduleId);
            aloc.splitType = splitType.type;

            if (aloc.imageAreas == null || aloc.imageAreas.size()==0)
            {
                break;
            }

            aloc.OriginId = block.uniqueScheduleId;
            result.add(aloc);
        }
        return result;
    }
}

