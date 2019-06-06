package de.c3e.ProcessManager.WorkerManager;

import de.c3e.ProcessManager.DataTypes.GraphBlock;
import de.c3e.ProcessManager.Utils.ImageSubSet;
import de.c3e.ProcessingManager.Types.SplitType;
import de.c3e.ProcessingManager.Types.SplitTypes;

import java.io.Serializable;
import java.util.List;

public class CalculationAllocation implements Serializable
{
    public CalculationAllocation(GraphBlock b)
    {
        this.block = b;
    }

    public long OriginId;

    public IWorker worker;

    public List<ImageSubSet> imageAreas;

    public GraphBlock block;

    public SplitTypes splitType;
}
