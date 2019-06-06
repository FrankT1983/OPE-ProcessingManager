package de.c3e.ProcessManager.WorkerManager;

import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.BlockRepository.BlockRepository;

import de.c3e.ProcessManager.Utils.DebugHelper;
import de.c3e.ProcessManager.Utils.LogUtilities;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Worker for executing work on a separate thread.
 */
class ThreadWorker implements IWorker
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private boolean isFinished = false;

    private Thread workThread;

    private IWorkBlock blockToRun;

    private BlockRepository repository = new BlockRepository();
    private String workType;

    private String threadName;

    private long DurationOfLastCalculation =0;      // in [ms]
    private long StartOfLastCalculation=0;          // in [ms]
    private boolean LastCaluclationWasSuccessful = true;
    private Map<String, Object> usedInputs;

    // Object that will be notified if the run status changes => to avoid busy waiting
    public final Lock  lock = new ReentrantLock();
    private final Condition finished = lock.newCondition();


    @Override
    public boolean isBusy()
    {
        synchronized (lock)
        {
            return this.workThread != null;
        }
    }

    @Override
    public boolean isFinished()
    {
        return this.isFinished;
    }

    @Override
    public int getId()
    {
        return 0;
    }

    @Override
    public boolean isRemoteWorker()
    {
        return false;
    }

    @Override
    public void SetWork(IWorkBlock worker)
    {
        synchronized (lock)
        {
            this.blockToRun = worker;
        }
    }

    @Override
    public void SetWorkType(String workType)
    {
        synchronized (lock)
        {
            this.blockToRun = this.repository.getWorkBlockFromType(workType);
            this.workType = workType;
        }
    }

    @Override
    public String GetWorkType()
    {
        return this.workType;
    }

    public boolean ValidBlockToRun()
    {
        return this.blockToRun != null;
    }

    @Override
    public boolean ScheduleBlock(final Map<String, Object> inputs, long schedulingId, String blockId)
    {
        if (this.isBusy())
        {
            logger.error("Scheduling a job, while another is running.");
        }

        this.isFinished =false;
        synchronized (lock)
        {
            this.workThread = new Thread()
            {
                public void run()
                {
                    try
                    {
                        // im using currentTimeMillis, which is not as precises as nanoTime but
                        // from a fixed point => thus hopefully this makes the values easier to compare
                        // between worker nodes
                        blockToRun.SetInputs(inputs);
                        usedInputs = inputs;
                        StartOfLastCalculation = System.currentTimeMillis();
                        LastCaluclationWasSuccessful = blockToRun.RunWork();
                        long current=System.currentTimeMillis();
                        DurationOfLastCalculation = current - StartOfLastCalculation;
                        WorkFinished();
                    }
                    catch (Throwable e)
                    {
                        DebugHelper.PrintException(e,logger);
                        logger.error("Exception when executing work: \n" + e.toString());
                        LastCaluclationWasSuccessful = false;
                        e.printStackTrace();
                        WorkFinished();
                    }
                }
            };

            if (StringUtils.isNotBlank(this.threadName))
            {
                this.workThread.setName(this.threadName);
            }
            this.workThread.start();
        }
        return true;
    }

    @Override
    public boolean ScheduleBlock(Map<String, Object> inputs, long schedulingId, String blockId, PartialWorkRequest howToSplit)
    {
        // thread worker does not support splitting.
        // since it will work locally it has no speed advantage to split.
        return this.ScheduleBlock(inputs,schedulingId, blockId);
    }



    @Override
    public SplitType getSplitType()
    {
        if (this.blockToRun instanceof ISupportsSplitting)
        {
            return ((ISupportsSplitting) this.blockToRun).getSplitType();
        }
        return SplitType.cantSplit;
    }

    @Override
    public Map<String, Object> GetAndRegisterResults()
    {
        return this.blockToRun.GetResults();
    }

    @Override
    public void Finalize()
    {
        if (this.workThread != null)
        {
            this.workThread.interrupt();
        }
    }

    public Map<String, Object> getUsedInputs()
    {
        return usedInputs;
    }

    public boolean LastCalculationWasSuccessful()
    {
        return LastCaluclationWasSuccessful;
    }

    public Condition getSignallingObject()
    {
        return finished;
    }

    void setThreadName(String threadName)
    {
        this.threadName = threadName;
    }

    long getStartOfLastCalculation()
    {
        return  StartOfLastCalculation;
    }

    long getDurationOfLastCalculation()
    {
        return DurationOfLastCalculation;
    }

    private void WorkFinished()
    {
        logger.info("ThreadWorker Finished Work on " + this.workType);
        lock.lock();
        try
        {
            synchronized (lock)
            {
                this.workThread = null;
                this.isFinished = true;
                this.finished.signalAll();
            }
        }
        finally
        {
            lock.unlock();
        }
    }
}
