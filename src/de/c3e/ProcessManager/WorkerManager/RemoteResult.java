package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;

/**
 * A link to a remote object. Can be only used for unsplittable types.
 */
public class RemoteResult implements IRemoteObject, java.io.Serializable
{
    public final int WorkerId ;
    public ResultGraphPosition ObjectUid;

    public RemoteResult(int workerId, ResultGraphPosition uid)
    {
        this.WorkerId = workerId;
        this.ObjectUid = uid;
    }

    @Override
    public ResultGraphPosition getObjectGraphPosition()
    {
        return this.ObjectUid;
    }

    @Override
    public void setObjectGraphPosition(ResultGraphPosition uid)
    {
        this.ObjectUid = uid;
    }

    @Override
    public int getWorkerId()
    {
        return this.WorkerId;
    }

    @Override
    public Object CreateEmptyRepresentation(int workerID)
    {
        return new RemoteResult(workerID, this.ObjectUid);
    }
}
