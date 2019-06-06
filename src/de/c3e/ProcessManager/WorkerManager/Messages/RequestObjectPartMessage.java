package de.c3e.ProcessManager.WorkerManager.Messages;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;
import de.c3e.ProcessManager.Utils.ImageSubSet;
import de.c3e.ProcessManager.Utils.RemoteImagePartRequest;
import de.c3e.ProcessManager.Utils.RemoteObjectPartRequest;

/**
 * Created by Frank on 27.07.2016.
 */
public class RequestObjectPartMessage extends MessageBase
{
    private final ResultGraphPosition fullObjectId;


    public RequestObjectPartMessage(ResultGraphPosition fullObjectId)
    {
        this.fullObjectId = fullObjectId;
    }


    public ResultGraphPosition getFullObjectId()
    {
        return fullObjectId;
    }
}


