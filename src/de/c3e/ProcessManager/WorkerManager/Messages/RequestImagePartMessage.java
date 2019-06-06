package de.c3e.ProcessManager.WorkerManager.Messages;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;
import de.c3e.ProcessManager.Utils.ImageSubSet;

public class RequestImagePartMessage extends RequestObjectPartMessage
{

    private final String partIdentification;
    private final ImageSubSet subSet;


    public RequestImagePartMessage(ResultGraphPosition fullObjectId, String partIdentification, ImageSubSet subSet)
    {
        super(fullObjectId);
        this.partIdentification =  partIdentification;
        this.subSet = subSet;
    }

    public String getPartIdentification()
    {
        return partIdentification;
    }

    public ImageSubSet getSubSet()
    {
        return subSet;
    }
}
