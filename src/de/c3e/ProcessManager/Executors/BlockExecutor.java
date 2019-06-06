package de.c3e.ProcessManager.Executors;

import com.google.common.base.Joiner;
import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.BlockRepository.BlockRepository;
import de.c3e.ProcessManager.BlockRepository.GraphBlockTrackingDecorator;
import de.c3e.ProcessManager.DataTypes.*;
import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.Utils.IIsPartOfActiveCommunication;
import de.c3e.ProcessManager.Utils.ImageSubSet;
import de.c3e.ProcessManager.Utils.LogUtilities;
import de.c3e.ProcessManager.WorkerManager.*;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to execute the work of a block
 */
public class BlockExecutor
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private BlockRepository repository = new BlockRepository();
    private final List<GraphBlockTrackingDecorator> runningBlocks = new ArrayList<>();

    public void CollectResultsFromBlocks()
    {
        synchronized (this.runningBlocks)
        {
            List<GraphBlockTrackingDecorator> finishedBlocks = new ArrayList<>();
            for (GraphBlockTrackingDecorator work : this.runningBlocks)
            {
                if (work.isFinished())
                {
                    finishedBlocks.add(work);
                }
            }

            this.runningBlocks.removeAll(finishedBlocks);
            for (GraphBlockTrackingDecorator finished : finishedBlocks)
            {
                GraphBlock blockInGraph = finished.getBlockInGraph();
                blockInGraph.AddResults(finished.CollectResults());
            }

            for (GraphBlockTrackingDecorator finished : finishedBlocks)
            {
                GraphBlock blockInGraph = finished.getBlockInGraph();

                List<GraphBlockTrackingDecorator> stillRunning = GetRunningBlocksOf(blockInGraph);

                UpdateBlockStatusFromOutPorts(blockInGraph, !stillRunning.isEmpty());
            }
        }
    }

    private  List<GraphBlockTrackingDecorator> GetRunningBlocksOf(GraphBlock blockInGraph)
    {
        List<GraphBlockTrackingDecorator> res = new ArrayList<>();
        synchronized (this.runningBlocks)
        {
             for (GraphBlockTrackingDecorator work : this.runningBlocks)
            {
                if (work.getBlockInGraph().equals(blockInGraph))
                {
                    res.add(work);
                }
            }
        }
        return res;
    }

    /**
     * The purpose of this functions is to prevent block that have been already started to
     * be assigned to additional workers, only for them to notice, that there are no more parts
     * available for calculation.
     * @param blocks The list of blocks to modify
     */
    public void removeExecutingBlocks(List<GraphBlock> blocks)
    {
        for (GraphBlockTrackingDecorator work : this.runningBlocks)
        {
            boolean stillWorkToDo = false;
            GraphBlock block = work.getBlockInGraph();
            for (BlockIO i : block.Inputs)
            {
                Object v = i.getValue();
                if ( v instanceof  AbstractImageObject )
                {
                    AbstractImageObject img = (AbstractImageObject) v;
                    if (img.HasUnrequestedPixels())
                    {
                        stillWorkToDo = true;
                        break;
                    }
                }
            }

            if (!stillWorkToDo)
            {blocks.remove(block);}
        }
    }

    public String GetRunningBlocks()
    {
        List<String> running = new ArrayList<>();
        synchronized (this.runningBlocks)
        {
            for(GraphBlockTrackingDecorator block : this.runningBlocks)
            {
                running.add(block.getBlockInGraph().Type + "(Node " + block.getWorkerIdentification() +")");
            }
        }
        return Joiner.on(" ").join(running);
    }


    private void UpdateBlockStatusFromOutPorts(GraphBlock blockInGraph, boolean hasStillRunningParts)
    {
        for (BlockIO out : blockInGraph.Outputs)
        {
            if (!out.isValid())
            {
                // this is a case, if a worker finished but had nothing to do : can
                // reschedule block
                blockInGraph.setStatus(BlockStatus.PartiallyFinished);
                return;
            }

            Object resultValue = out.getValue();
            if (resultValue instanceof IPartialResult)
            {
                if (!((IPartialResult)resultValue).isComplete())
                {
                    blockInGraph.setStatus(BlockStatus.PartiallyFinished);
                    return;
                }
            }
        }

        // check if all inputs where completely processed
        // this is only relevant for size changing or projection plugins
        // todo: make this the default case ... isComplete isn't a very good criteria to implement outside of images
        for (BlockIO in : blockInGraph.Inputs)
        {
            if (!in.isValid())
            {continue;}
            Object inValue = in.getValue();
            IWorkBlock blockToRun = this.repository.getWorkBlockFromType(blockInGraph.Type);
            if (blockToRun instanceof ISupportsSplitting)
            {
                if (inValue instanceof AbstractImageObject)
                {
                    AbstractImageObject v = (AbstractImageObject) inValue;
                    if (!v.WasCompletelyRequested(blockInGraph.uniqueScheduleId))
                    {
                        blockInGraph.setStatus(BlockStatus.PartiallyFinished);
                        return;
                    }
                    else
                    {
                        logger.info("Was completely requested: " + v .toString()  );
                        logger.info("Part Sets " + v.alreadyRequestedPixels.toString());
                        for(List<ImageSubSet> e : v.alreadyRequestedPixels.values())
                        {
                            logger.info("Part Sets " + e.toString());
                        }
                    }
                }
            }
        }

        for (GraphBlockTrackingDecorator b :this.runningBlocks)
        {
            if (b.getBlockInGraph().equals(blockInGraph))
            {
                blockInGraph.setStatus(BlockStatus.PartiallyFinished);
                return;
            }
        }

        if (hasStillRunningParts)
        {
            logger.info("Block nearly: " + blockInGraph.GenerateBlockID()  );
            blockInGraph.setStatus(BlockStatus.PartiallyFinished);
            return;
        }

        // at this point its clear, that the outputs are complete => check for reruns because of loops
        if(NeedsReRun(blockInGraph))
        {
            BlockGraphAnalyser.SetupForReRun(blockInGraph);
            blockInGraph.setStatus(BlockStatus.Ready);
            BlockGraphAnalyser.ClearPorts(blockInGraph.Outputs);
            return;
        }


        blockInGraph.setStatus(BlockStatus.Finished);
        logger.info("Finished block : " + blockInGraph.GenerateBlockID()  );
    }

    private boolean NeedsReRun(GraphBlock blockInGraph)
    {
        for (BlockIO out : blockInGraph.Outputs)
        {
            if (out.isValid() && out.getValue() instanceof ISupportsMultiPass)
            {
                ISupportsMultiPass value = (ISupportsMultiPass) out.getValue();
                if (value.NeedsMorePasses())
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean IsBusy()
    {
        return this.runningBlocks.size() > 0;
    }

    public boolean StartExecution(GraphBlock graphBlock, IWorker workerForBlock, PartialWorkRequest howToSplit)
    {
        graphBlock.setStatus(BlockStatus.Working);

        IWorkBlock blockToRun = null;
        if (!workerForBlock.isRemoteWorker())
        {
            // if the calculation is done by this program, might as well resolve the work block here.
            blockToRun = this.repository.getWorkBlockFromType(graphBlock.Type);
            workerForBlock.SetWork(blockToRun);
            assert blockToRun != null;
        }
        else
        {
            // let the remote worker figure out what to use
            workerForBlock.SetWorkType(graphBlock.Type);
        }

        GraphBlockTrackingDecorator decorated = new GraphBlockTrackingDecorator(blockToRun, graphBlock, workerForBlock);

        this.runningBlocks.add(decorated);

        Map<String,Object> inputs = CollectInputInformation(graphBlock);
        return workerForBlock.ScheduleBlock(inputs, graphBlock.uniqueScheduleId , graphBlock.GenerateBlockID(), howToSplit);
    }

    private static  Map<String,Object> CollectInputInformation(GraphBlock graphBlock)
    {
        Map<String,Object> inputValues = new HashMap<>();

        for (BlockIO input : graphBlock.Inputs)
        {
            Object value = input.getValue();
            if (value instanceof IIsPartOfActiveCommunication)
            {
                value = ((IIsPartOfActiveCommunication)value).CreateShallowClone();
            }
            inputValues.put(input.Name,value);
        }
        return inputValues;
    }
}
