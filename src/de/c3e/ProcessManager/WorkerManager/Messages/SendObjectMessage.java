package de.c3e.ProcessManager.WorkerManager.Messages;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;

/**
 * Created by Frank on 26.07.2016.
 */
public class SendObjectMessage extends MessageBase
{
    private final ResultGraphPosition objectId;
    private final Object object;

    public SendObjectMessage(ResultGraphPosition requestedObjectPosition, Object value)
    {
        this.objectId = requestedObjectPosition;
        this.object = value;
    }

    public ResultGraphPosition getObjectId()
    {
        return objectId;
    }

    public Object getObject()
    {
        return object;
    }

    @Override
    public String toString()
    {
        return "SendObjectMessage : " + this.objectId + " " + this.object;
    }
}
