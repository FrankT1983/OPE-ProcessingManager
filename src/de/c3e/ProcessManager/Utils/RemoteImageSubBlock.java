package de.c3e.ProcessManager.Utils;

import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;
import de.c3e.ProcessManager.WorkerManager.IRemoteObjectPart;

public class RemoteImageSubBlock extends RemoteObjectBlock implements IRemoteObjectPart,  java.io.Serializable
{
    private ImageSubSet dimensions;

    public  RemoteImageSubBlock()
    {}

    public  RemoteImageSubBlock(ImageSubSet dimensions, int workerID, ResultGraphPosition objectGraphPosition)
    {
        this.dimensions = dimensions;
        this.setWorkerId(workerID);
        this.setFullObjectPosition(objectGraphPosition);
    }

    public void setDimensions(ImageSubSet dim)
    {
        this.dimensions = dim;
    }

    public ImageSubSet getDimensions()
    {
        return dimensions;
    }

    @Override
    public  String toString()
    {
        return super.toString() + " " + dimensions.toString();
    }
}
