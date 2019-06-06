package de.c3e.ProcessManager.Utils;

public class RemoteObjectPartRequest
{
    public RemoteObjectBlock Block;

    /***
     * Constructor to request full remote object;
     * @param block The part to request.
     */
    public RemoteObjectPartRequest(RemoteObjectBlock block)
    {
        this.Block = block;
    }
}
