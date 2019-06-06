package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessManager.DataTypes.GraphBlock;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simple Work manager which runs all work on the main thread.
 * Will be used as baseline for test, since the leas can go wrong here.
 */
public class MainThreadWorkManager implements IWorkerManager, IWorker
{
    private boolean isFinished = false;
    private IWorkBlock workBlock;

    @Override
    public IWorker getFreeWorker()
    {
        return this;
    }

    @Override
    public List<IWorker> getFreeWorkers()
    {
        List<IWorker> result = new ArrayList<>();
        result.add(this);
        return result;
    }

    @Override
    public ICommunicationInfrastructure getInfrastructure()
    {
        return null;
    }

    @Override
    public void Finalize()
    {}

    @Override
    public boolean isBusy()
    {
        return false;
    }

    @Override
    public boolean isFinished()
    {
        return this.isFinished;
    }

    @Override
    public int getId()
    {
        return 2;
    }

    @Override
    public boolean isRemoteWorker()
    {
        return false;
    }

    @Override
    public void SetWork(IWorkBlock worker)
    {
        this.workBlock = worker;
    }

    @Override
    public void SetWorkType(String workType)
    {
    }

    @Override
    public String GetWorkType()
    {
        return this.workBlock.getClass().getName();
    }

    // todo: not all worker need the block id => make derived interface
    @Override
    public boolean ScheduleBlock(Map<String, Object> inputs, long schedulingId, String blockId)
    {
        this.workBlock.SetInputs(inputs);
        boolean success =this.workBlock.RunWork();
        this.isFinished = this.workBlock.IsFinished();
        return success;
    }

    @Override
    public boolean ScheduleBlock(Map<String, Object> inputs, long schedulingId, String blockId , PartialWorkRequest howToSplit)
    {
        // main thread worker does not support splitting
        return this.ScheduleBlock(inputs, schedulingId,blockId);
    }

    @Override
    public SplitType getSplitType()
    {
        return SplitType.cantSplit;
    }

    @Override
    public Map<String, Object> GetAndRegisterResults()
    {
        return null;
    }

    @Override
    public boolean HadError()
    {
        return false;
    }

    @Override
    public void SendEvictionNotice(GraphBlock b)
    {
    }
}
