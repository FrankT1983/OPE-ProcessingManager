package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;

/**
 * A object that is stored in a result buffer of a communication infrastructure.
 * The id can be used to identify the object across instances.
 */
public interface IRemoteObject
{
    ResultGraphPosition getObjectGraphPosition();
    void setObjectGraphPosition(ResultGraphPosition position);
    int getWorkerId();

    /***
     * Create an representation of this object, that can be send to other workers and contains references to
     * the respective local parts.
     */
    Object CreateEmptyRepresentation(int localWorkerId);
}
