package de.c3e.ProcessManager.LoadBalancing;

import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.BlockRepository.BlockRepository;
import de.c3e.ProcessManager.DataTypes.BlockIO;
import de.c3e.ProcessManager.DataTypes.GraphBlock;
import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.Utils.DebugHelper;
import de.c3e.ProcessManager.Utils.ExecutionTimeMeasurements;
import de.c3e.ProcessManager.WorkerManager.CalculationAllocation;
import de.c3e.ProcessManager.WorkerManager.IWorker;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Simple balancer, that will always use predict the same number of pixels.
 */
public class ConstantSizeBalancer implements IBalancer
{
    private final long constantPixels;

    public ConstantSizeBalancer(long pixels)
    {
        this.constantPixels = pixels;
    }

    @Override
    public void RegisterExecutionStatistics(Map<Integer, ExecutionTimeMeasurements> remoteExecutionStatistics)
    {
    }

    @Override
    public long EstimatePixelCountForTime(String workType, double timeInSeconds)
    {
        return constantPixels;
    }

    @Override
    public void SaveStatistics(String filePath)
    {
        // this balancer does not require any statistics to work.
    }

    @Override
    public void LoadStatistics(String filePath)
    {
        // this balancer dos not require any statics for work.
    }

    private BlockRepository repository = new BlockRepository();

    @Override
    public Queue<CalculationAllocation> ScheduleBlockOnWorkers(GraphBlock block, Queue<IWorker> workers, long desiredBlockTime)
    {
        IWorkBlock executor = repository.getWorkBlockFromType(block.Type);
        SplitType splitType = SplitType.cantSplit;
        if (executor instanceof ISupportsSplitting)
        {
            splitType = ((ISupportsSplitting)executor).getSplitType();
        }

        AbstractImageObject v = null;
        for(BlockIO input :block.Inputs)
        {
            Object value = input.getValue();
            if (input.isValid() && value instanceof AbstractImageObject)
            {
                if (v != null)
                {   DebugHelper.BreakIntoDebug();// don't support multi image calculations yet
                }
                v = (AbstractImageObject)value;
            }
        }

        Queue<CalculationAllocation> result = new LinkedList<>();
        if (v == null)
        {
            CalculationAllocation aloc = new CalculationAllocation(block);
            aloc.worker = workers.poll();
            aloc.OriginId = block.uniqueScheduleId;
            result.add(aloc);
            return result;
        }

        if (splitType == SplitType.cantSplit)
        {
            CalculationAllocation aloc = new CalculationAllocation(block);
            aloc.worker = workers.poll();
            aloc.OriginId = block.uniqueScheduleId;
            aloc.imageAreas = v.getFullBoundsL();
            aloc.splitType = splitType.type;
            result.add(aloc);
            return result;
        }

        while (!workers.isEmpty())
        {
            IWorker w = workers.poll();
            CalculationAllocation aloc = new CalculationAllocation(block);
            aloc.worker = w;
            aloc.imageAreas = v.GetSubset(this.constantPixels,splitType,block.uniqueScheduleId);
            if (aloc.imageAreas == null)
            {   continue; }
            aloc.OriginId = block.uniqueScheduleId;
            result.add(aloc);
        }
        return result;
    }

    @Override
    public long RestrictDesiredPixelsByImageSize(long desiredPixelCount, long fullSizePixelCount)
    {
        return Math.min(desiredPixelCount,fullSizePixelCount);
    }

    @Override
    public String toString()
    {
        return "ConstantSizeBalancer : " + this.constantPixels + " pixels";
    }
}
