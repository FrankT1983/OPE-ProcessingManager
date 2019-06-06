package de.c3e.ProcessManager.WorkerManager;


import de.c3e.ProcessManager.Utils.DebugHelper;
import de.c3e.ProcessManager.WorkerManager.Messages.StartWorkMessage;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frank on 26.07.2016.
 */
public class RemoteWorkerMasterSide implements IWorker
{
    SendReceiveInfrastructure sendReceiveInfrastructure;
    private boolean isReady ;
    private boolean someoneGotTheResults = true;
    private final int workerId;
    private String workType;

    private Map<String, Object> receivedResults = new HashMap();
    private boolean isFinished = false;
    private PartialWorkRequest usedSplit;
    private long scheduledBlockId;


    public RemoteWorkerMasterSide(int workerId,SendReceiveInfrastructure infrastruct)
    {
        this.sendReceiveInfrastructure = infrastruct;
        this.workerId = workerId;
    }

    /**
     * Get the id of the worker that this master side representation ... represents.
     * @return
     */
    public int getRemoteId()
    {
        return workerId;
    }

    @Override
    public boolean isBusy()
    {
        return !this.isReady || !this.someoneGotTheResults;
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
        return true;
    }

    @Override
    public void SetWork(IWorkBlock worker)
    {

    }

    @Override
    public void SetWorkType(String workType)
    {
        this.workType = workType;
    }

    @Override
    public String GetWorkType()
    {
        return this.workType;
    }

    @Override
    public boolean ScheduleBlock(Map<String, Object> inputs, long schedulingId , String blockId)
    {
        return this.ScheduleBlock(inputs, schedulingId, blockId,  null);
    }

    @Override
    public boolean ScheduleBlock(Map<String, Object> inputs, long schedulingId , String blockId , PartialWorkRequest howToSplit)
    {
        // start a new work => get rid of old results
        this.receivedResults.clear();
        this.isFinished = false;
        this.isReady = false;
        this.someoneGotTheResults = false;
        this.usedSplit = howToSplit;
        this.scheduledBlockId = schedulingId;
        StartWorkMessage message = new StartWorkMessage(inputs, schedulingId,blockId ,howToSplit, this.workType);
        this.sendReceiveInfrastructure.SendMessage(message, workerId);
        return true;
    }

    @Override
    public SplitType getSplitType()
    {
        // should no be called here. Only the actual worker can really say what split type he supports.
        DebugHelper.BreakIntoDebug();
        return null;
    }

    @Override
    public Map<String, Object> GetAndRegisterResults()
    {
        this.someoneGotTheResults = true;
        return this.receivedResults;
    }

    @Override
    public void Finalize()
    {
        // nothing to do
    }

    public void SetReadyState(boolean ready)
    {
        this.isReady = ready;
        this.isFinished = ready;
    }

    public void AddResult(String objectId, Object object)
    {
        String uid;
        Object reloaded = object;
        if (object instanceof IRemoteObject)
        {
            reloaded = this.sendReceiveInfrastructure.AddRemoteResult((IRemoteObject)object);
        }

        this.receivedResults.put(objectId,reloaded);

    }
}
