package de.c3e.ProcessManager.WorkerManager.Messages;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;
import de.c3e.ProcessManager.Utils.ImageSubBlock;

/**
 * Created by Frank on 27.07.2016.
 */
public class SendObjectPartMessage extends MessageBase
{
    private final Object objectPart;
    private final ResultGraphPosition fullObjectId;

    public SendObjectPartMessage(Object objectPart, ResultGraphPosition fullObjectId)
    {
        this.objectPart = objectPart;
        this.fullObjectId = fullObjectId;
    }

    public ResultGraphPosition getFullObjectId()
    {
        return fullObjectId;
    }

    public Object getObjectPart()
    {
        return objectPart;
    }

    @Override
    public  String toString()
    {
        return "SendObjectPartMessage  [" + fullObjectId  + "] " + objectPart.toString();
    }
}
