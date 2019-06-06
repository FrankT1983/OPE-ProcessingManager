package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessManager.Utils.ImageSubSet;
import de.c3e.ProcessingManager.Types.SplitType;
import de.c3e.ProcessingManager.Types.SplitTypes;

import java.io.Serializable;
import java.util.List;

/**
 * Pack all parameters for requesting a worker to do partial work.
 * --> this way the worker it self can decide what
 */
public class PartialWorkRequest implements Serializable
{
    public final long OriginId;
    public final List<ImageSubSet> ImageAllocation  ;
    public SplitTypes splitType = null;


    public PartialWorkRequest(long originId, List<ImageSubSet> allocation)
    {
        this.OriginId = originId;
        this.ImageAllocation = allocation;
    }

    public PartialWorkRequest(long originId, List<ImageSubSet> allocation, SplitTypes splitType)
    {
        this.OriginId = originId;
        this.ImageAllocation = allocation;
        this.splitType = splitType;
    }

    public PartialWorkRequest(long originId)
    {
        this.OriginId = originId;
        this.ImageAllocation = null;
    }
}


