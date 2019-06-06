package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;

import de.c3e.ProcessManager.LoadBalancing.IBalancer;
import de.c3e.ProcessManager.TransportLayer.ITransportLayer;
import de.c3e.ProcessManager.Utils.*;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;
import de.c3e.ProcessingManager.Types.SplitTypes;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The worker side of a remote worker.
 */
public class RemoteWorker implements IWorker
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private final ICommunicationInfrastructure comInfrastructure;
    private ThreadWorker internalWorker = new ThreadWorker();
    private PartialWorkRequest usedSplit;
    private boolean waitForWorkerToFinish = false;
    private boolean requestFinalization ;
    private long currentSchedulingId;
    private String currentBlockId;
    private String currentBlockType;

    private final ExecutionTimeMeasurements executionTimeMeasurements = new ExecutionTimeMeasurements();

    public RemoteWorker(ITransportLayer accessPoint, IBalancer balancer)
    {
        this.comInfrastructure = new SendReceiveInfrastructure(accessPoint, balancer);
        ((SendReceiveInfrastructure)this.comInfrastructure).RegisterExecutionStatistics(this.executionTimeMeasurements);
    }

    @Override
    public boolean isBusy()
    {
        return false;
    }

    @Override
    public boolean isFinished()
    {
        return false;
    }

    @Override
    public int getId()
    {
        if (this.comInfrastructure != null)
        {
            return this.comInfrastructure.getMyRank();
        }
        return -1;
    }

    @Override
    public boolean isRemoteWorker()
    {
        return true;
    }

    @Override
    public void SetWork(IWorkBlock worker)
    {
        this.internalWorker.SetWork(worker);
    }

    @Override
    public void SetWorkType(String workType)
    {
        this.currentBlockType = workType;
        try
        {
            long startSetup = System.currentTimeMillis();
            this.internalWorker.SetWorkType(workType);
            this.RecordAndReportTimeMeasurement(new ExecutionTimeMeasurementEntry(
                    startSetup,
                    System.currentTimeMillis()-startSetup,
                    "BlockInitialization", ExecutionTimeMeasurementEntry.MeasuredType.Initialization));

            if (!this.internalWorker.ValidBlockToRun())
            {
                this.comInfrastructure.ReportErrorStatus(ErrorStatus.BlockNotFound);
                logger.error("Error setting work type (" + workType + ") block not valid.");
            }
        }
        catch (Throwable e)
        {
            logger.error("Exception setting work type (" + workType + ") " + e.toString() + "\n" + ExceptionUtils.getFullStackTrace(e));
        }
    }

    @Override
    public String GetWorkType()
    {
        return this.internalWorker.GetWorkType();
    }

    @Override
    public boolean ScheduleBlock(Map<String, Object> inputs, long schedulingId , String blockId)
    {
        return this.ScheduleBlock(inputs, schedulingId, blockId, null);
    }

    @Override
    public boolean ScheduleBlock(Map<String, Object> inputs, long schedulingId, String blockId, PartialWorkRequest howToSplit)
    {
        logger.info(" "  +this.getId() + " RemoteWorker: Start " + this.currentBlockType );
        try
        {
            long start = System.currentTimeMillis();
            this.usedSplit = howToSplit;
            logger.info(" "  +this.getId()+ " RemoteWorker: " + this.currentBlockType  + " Unbox Inputs");
            Map<String, Object> unBoxedInputs = this.UnboxInputs(inputs);
            this.currentSchedulingId = schedulingId;
            this.currentBlockId = blockId;

            logger.info(" "  +this.getId() + " RemoteWorker: " + this.currentBlockType  + " Convert Split Data");
            Map<String, Object> extractedInputs;
            try
            {
                extractedInputs = ConvertSpiltData(unBoxedInputs, howToSplit);
            } catch (Throwable e)
            {
                logger.error(this.getId() + " Error loading data " + this.currentBlockType + " " + e);
                DebugHelper.BreakIntoDebug();
                extractedInputs = null;
                this.comInfrastructure.ReportErrorStatus(ErrorStatus.Unknown);
            }

            this.RecordAndReportTimeMeasurement(ExecutionTimeMeasurementEntry.DataCollectionEntry(start, System.currentTimeMillis()));

            if (extractedInputs == null)
            {
                logger.info(getId() + " RemoteWorker: Schedule Block : " + this.currentBlockType  + "Had nothing to do or acquiring data failed ");
                this.comInfrastructure.ReportReadyState();
                return true;
            }

            if (!this.internalWorker.ValidBlockToRun())
            {
                logger.info("Work block invalid => do nothing.");
                // report ready. The master node should decide what to do with this worker.
                this.comInfrastructure.ReportReadyState();
                return true;
            }

            logger.info(" "  + this.getId() + " RemoteWorker: Schedule Block : " + this.currentBlockType  + " Schedule Internal");
            this.internalWorker.ScheduleBlock(extractedInputs, schedulingId, blockId);
            this.waitForWorkerToFinish = true;
        }catch (Throwable e)
        {
            logger.error("Error Scheduling block: " + e.toString());
            DebugHelper.PrintException(e,logger);
            DebugHelper.BreakIntoDebug();

        }
        logger.info(" "  + this.getId() + " RemoteWorker: Schedule Block : " + this.currentBlockType  + " finished");
        return  true;
    }

    @Override
    public SplitType getSplitType()
    {
        return this.internalWorker.getSplitType();
    }

    /**
     * Get the results that this worker produced.
     */
    @Override
    public Map<String, Object> GetAndRegisterResults()
    {
        Map<String, Object> packedResult = new HashMap<>();
        ThreadWorker w = this.internalWorker;
        Map<String, Object>  currentResults;
        try
        {
            currentResults = w.GetAndRegisterResults();
        }
        catch (Exception ex)
        {
            logger.error("Error retrieving results: \n" + ex.toString()  + " " + ExceptionUtils.getStackTrace(ex));
            this.comInfrastructure.ReportErrorStatus(ErrorStatus.Unknown);
            return packedResult;
        }catch (OutOfMemoryError ex) {
            logger.error("Ran out of memory Retrieving results: \n" + ex.toString()  + " " + ExceptionUtils.getStackTrace(ex));
            this.comInfrastructure.ReportErrorStatus(ErrorStatus.OutOfMemory);
            return new HashMap<>();
        }

        for (Map.Entry<String,Object> o : currentResults.entrySet())
        {
            ResultGraphPosition pos = new ResultGraphPosition(this.currentSchedulingId, this.currentBlockId, o.getKey());
            Object data = o.getValue();

            if (data == null)
            {
                // this can happen, if the worker had nothing to do, because the number of needed workers was
                // overestimated
                continue;
            }

            if (data instanceof IRemoteObject)
            {
                ((IRemoteObject) data).setObjectGraphPosition(pos);
            }

            if (this.usedSplit == null)
            {
                this.comInfrastructure.RegisterResult(data, pos);
            }
            else
            {
                this.comInfrastructure.RegisterPartialResult(data, pos, this.usedSplit, o.getKey());
            }

            if (data instanceof IRemoteObject)
            {
                packedResult.put(o.getKey(), ((IRemoteObject)data).CreateEmptyRepresentation(this.getId()));
            }
            else
            {
                RemoteResult result = new RemoteResult(this.getId(), pos);
                packedResult.put(o.getKey(), result);
            }
        }
        return packedResult;
    }


    /***************************************************************
     *
     *
     * *************************************************************
     */
    public void RunMainLoop()
    {
        logger.info(this.comInfrastructure.getMyRank() +": Starting up worker:  " +  this.comInfrastructure.getMyRank());
        this.comInfrastructure.ReportReadyState();
        this.comInfrastructure.RegisterLocalWorker(this);
        Thread.currentThread().setName("MainThread of Remote worker : " + this.comInfrastructure.getMyRank());
        this.internalWorker.setThreadName("WorkerThread for Remote worker : " + this.comInfrastructure.getMyRank());

        int reportInterval = 10;
        // comInfrastructure will run a loop, wait for messages and use this class to schedule work if needed
        while(!this.requestFinalization)
        {
            try
            {
                this.internalWorker.lock.lock();
                this.internalWorker.getSignallingObject().await(reportInterval, TimeUnit.SECONDS);
            }
            catch (Exception e)
            {   e.printStackTrace();}
            finally
            {
                this.internalWorker.lock.unlock();
            }

            if (!this.waitForWorkerToFinish)
            {
              continue;
            }

            if (this.internalWorker.isFinished())
            {
                long start = System.currentTimeMillis();
                Map<String, Object> registeredResults = null;
                try
                {
                    registeredResults=this.GetAndRegisterResults();
                }
                catch (Throwable ex)
                {
                    logger.error("Collection of result produced error.");
                    this.comInfrastructure.ReportErrorStatus(ErrorStatus.Unknown);
                }

                this.RecordAndReportTimeMeasurement(ExecutionTimeMeasurementEntry.DataCollectionEntry(start,System.currentTimeMillis()));

                if (this.internalWorker.LastCalculationWasSuccessful())
                {
                    this.RecordAndReportTimeMeasurement(ExecutionTimeMeasurementEntry.CalculationEntry(
                            this.internalWorker.getStartOfLastCalculation(),
                            this.internalWorker.getDurationOfLastCalculation(),
                            this.currentBlockType,
                            this.internalWorker.getUsedInputs()));
                }
                else
                {
                    logger.error("Last calculation was not successful.");
                    this.comInfrastructure.ReportErrorStatus(ErrorStatus.CalculationFailed);
                }

                // this will only work, since mpi results can not overtake each other
                // if not, the worker would have to wait for an accomplishment, that the results have been registered.
                this.comInfrastructure.AnnounceResults(registeredResults);
                this.internalWorker = new ThreadWorker();
                this.comInfrastructure.ReportReadyState();
            }
        }
    }

    public  void Finalize()
    {
        this.requestFinalization = true;
    }

    private Map<String, Object> UnboxInputs(Map<String, Object> inputs)
    {
        Map<String, Object> unPackedResult = new HashMap<>();
        for (Map.Entry<String,Object> o : inputs.entrySet())
        {
            String key = o.getKey();
            Object data = o.getValue();

            if (data instanceof IRemoteObject && (!(data instanceof RemoteResult)))
            {
                IRemoteObject remoteResult = (IRemoteObject)data;
                data = this.comInfrastructure.RegisterInput(remoteResult.getObjectGraphPosition(), data);
            }

            if (data instanceof INeedsComInfrastructe)
            {
                ((INeedsComInfrastructe)data).setCommunicationInfrastructure(this.comInfrastructure);
            }


            //if (data instanceof IRemoteObject && (!(data instanceof AbstractImageObject)) && (!(data instanceof AbstractTableObject)))
            if (data instanceof RemoteResult)
            {
                IRemoteObject remoteResult = (IRemoteObject)data;
                Future<Object> result = this.comInfrastructure.RequestObjectFromWorker(remoteResult.getWorkerId(), remoteResult.getObjectGraphPosition());
                try
                {
                    unPackedResult.put(key,result.get());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                unPackedResult.put(key,data);
            }
        }

        return unPackedResult;
    }

    /**
     * Convert split data into the data that is needed for running this worker.
     * For Images this means requesting the needed remote parts to complete the image
     */
    private Map<String, Object> ConvertSpiltData(Map<String, Object> unBoxedInputs, PartialWorkRequest howToSplit)
    {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String,Object> input : unBoxedInputs.entrySet())
        {
            if (input.getValue() instanceof IPartialResult)
            {
                if (howToSplit == null ||    howToSplit.splitType == null ||  howToSplit.splitType == SplitTypes.cantSplit)
                {
                    // no splitting => need full object
                    IPartialResult partial = (IPartialResult) input.getValue();
                    partial.PullAllRemoteParts();

                    if (partial instanceof AbstractImageObject) { ((AbstractImageObject)partial).CompactLocalBlocks(true); }
                }
            }

            if (input.getValue() instanceof AbstractImageObject)
            {
                if (howToSplit == null || howToSplit.splitType == SplitTypes.cantSplit)
                {
                    // no splitting => need full image
                    result.put(input.getKey(), input.getValue());
                }
                else  if (howToSplit.ImageAllocation != null)
                {
                    AbstractImageObject image = (AbstractImageObject) input.getValue();
                    AbstractImageObject imageObject;
                    try
                    {
                        imageObject = image.getSubImageFromMaster(howToSplit.ImageAllocation);
                    }
                    catch (Throwable ex)
                    {
                        DebugHelper.PrintException(ex,logger);
                        DebugHelper.BreakIntoDebug();
                        throw ex;
                    }

                    if (imageObject == null)
                    {
                        return null;
                    }
                    result.put(input.getKey(), imageObject);
                }
            }
            else
            {
                result.put(input.getKey(),input.getValue());
            }
        }
        return result;
    }

    private void RecordAndReportTimeMeasurement(ExecutionTimeMeasurementEntry toReport)
    {
        this.executionTimeMeasurements.add(toReport);
        this.comInfrastructure.ReportTimeMeasurement(toReport);
    }
}
