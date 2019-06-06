package de.c3e.ProcessManager.Utils;

public class RemoteImagePartRequest extends RemoteObjectPartRequest
{
    public RemoteImageSubBlock Block;
    public ImageSubSet SubSet;

    RemoteImagePartRequest(RemoteImageSubBlock block, ImageSubSet subSet)
    {
        super(block);
        this.Block = block;
        this.SubSet = subSet;
    }

    public RemoteImagePartRequest(RemoteImageSubBlock block)
    {
        super(block);
        this.Block = block;
        this.SubSet = this.Block.getDimensions();
    }
}



