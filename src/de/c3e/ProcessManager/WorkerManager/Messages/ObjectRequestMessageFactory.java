package de.c3e.ProcessManager.WorkerManager.Messages;

import de.c3e.ProcessManager.Utils.DebugHelper;
import de.c3e.ProcessManager.Utils.RemoteImagePartRequest;
import de.c3e.ProcessManager.Utils.RemoteObjectPartRequest;

public class ObjectRequestMessageFactory
{

    public static RequestObjectPartMessage ConstructRequestMessage(RemoteObjectPartRequest request)
    {
        if (request instanceof RemoteImagePartRequest)
        {
            RemoteImagePartRequest remotePart = (RemoteImagePartRequest)request;
            return new RequestImagePartMessage(remotePart.Block.getFullObjectId(),
                    remotePart.Block.getPartIdentification(),
                    remotePart.SubSet);
        }

        if (request != null)
        {
            return  new RequestObjectPartMessage(request.Block.getFullObjectId());
        }

        DebugHelper.BreakIntoDebug();
        return null;
    }
}
