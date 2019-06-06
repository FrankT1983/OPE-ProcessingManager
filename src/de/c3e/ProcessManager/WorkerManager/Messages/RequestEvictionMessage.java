package de.c3e.ProcessManager.WorkerManager.Messages;

public class RequestEvictionMessage extends MessageBase
{
    private final String blockIdToEvict;
    public RequestEvictionMessage(String blockId)
    {
        super();
        this.blockIdToEvict=blockId;
    }

    public String getBlockIdToEvict()
    {
        return blockIdToEvict;
    }
}
