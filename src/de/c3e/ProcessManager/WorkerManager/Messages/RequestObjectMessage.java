package de.c3e.ProcessManager.WorkerManager.Messages;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;

/**
 * Message for requesting an object with a given position in the graph
 */
public class RequestObjectMessage extends MessageBase
{
    private final ResultGraphPosition requestedObjectId;

    public RequestObjectMessage(ResultGraphPosition objectUid)
    {
        this.requestedObjectId = objectUid;
    }

    public ResultGraphPosition getRequestedObjectId()
    {
        return requestedObjectId;
    }
}
