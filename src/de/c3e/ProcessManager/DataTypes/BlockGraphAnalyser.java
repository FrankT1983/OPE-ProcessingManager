package de.c3e.ProcessManager.DataTypes;

import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsOutputLoopBack;
import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.BlockRepository.BlockRepository;
import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.Utils.DebugHelper;
import de.c3e.ProcessManager.Utils.LogUtilities;
import de.c3e.ProcessManager.WorkerManager.IPartialResult;
import de.c3e.ProcessManager.WorkerManager.ISupportsMultiPass;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;
import de.c3e.ProcessingManager.Types.SplitTypes;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to analyse things in the graph.
 */
public class BlockGraphAnalyser
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static List<GraphBlock> GetWorkReadyBlocks(BlockGraph graph)
    {
        List<GraphBlock> readyBlocks = new ArrayList<>();
        for (GraphBlock block : graph.AllBlocks)
        {
            if (block.getStatus() == BlockStatus.Ready)
            {
                if (AllInputsReady(block))
                {
                    readyBlocks.add(block);
                }
            }

            if (block.getStatus() == BlockStatus.PartiallyFinished)
            {
                if (AllInputsReady(block))
                {
                    readyBlocks.add(block);
                }
            }
        }

        return readyBlocks;
    }

    private static boolean AllInputsReady(GraphBlock block)
    {
        for (BlockIO input : block.Inputs)
        {
            if (!input.isValid())
            {
                return false;
            }

            Object value = input.getValue();
            if (value instanceof IPartialResult)
            {
                if (!((IPartialResult)value).isComplete())
                {
                    return false;
                }
            }
        }
        return true;
    }

    public static void PushResultOverLinks(BlockGraph graph)
    {
        for (BlockLink link : graph.Links)
        {
            if (link.Status == BlockLink.LinkStatus.finishedTransfer)
            {   continue;   }

            if (link.OriginBlock.getStatus() != BlockStatus.Finished)
            {
                // todo: maybe latter build something to start working on partial results
                continue;
            }

            if (link.OriginPort.isValid())
            {
                Object value = link.OriginPort.getValue();

                if (value instanceof IPartialResult)
                {
                    if (!((IPartialResult) value).isComplete())
                    {
                        continue;
                    }
                }

                link.DestinationPort.SetValue(value);
                link.Status = BlockLink.LinkStatus.finishedTransfer;
            }
        }
    }

    private static BlockIO FindInputForLoopingOutput(BlockIO originPort, List<BlockIO> inputs, String type)
    {
        IWorkBlock block = blockRepository.getWorkBlockFromType(type);
        if (block instanceof ISupportsOutputLoopBack)
        {
            String loopBackName = ((ISupportsOutputLoopBack) block).getNameOfLoopBackPortForOutput(originPort.Name);
            if (loopBackName != null)
            {
                for(BlockIO input : inputs )
                {
                    if (input.NameOrIdEquals(loopBackName))
                    {
                        return input;
                    }
                }
            }
        }
        return null;
    }

    public static boolean AllDone(BlockGraph graph)
    {
        for (GraphBlock block :  graph.AllBlocks)
        {
            if (block.getStatus() == BlockStatus.Finished || block.getStatus() == BlockStatus.FinishedAndEvicted)
            {
                continue;
            }

            return false;
        }
        return true;
    }

    public static List<GraphBlock> GetUnSplittableWork(List<GraphBlock> workers)
    {
        List<GraphBlock> result = new ArrayList<>();

        for (GraphBlock block : workers)
        {
            if (GetSpiltType(block.Type) == SplitType.cantSplit)
            {
                result.add(block);
            }
        }
        return result;
    }

    private static BlockRepository blockRepository = new BlockRepository();

    private static SplitType GetSpiltType(String type)
    {
        switch (type)
        {
            /*
            case "de.c3e.BlockTemplates.Examples.SetChannelToAverage" : return SplitType.independentChannels;
            case  "AddXBlock":
            case  "de.c3e.BlockTemplates.Examples.AddXBlock":
            case  "DelayedAddXYBlock" : return SplitType.independentPoints;
            case  "AddXYBlock" : return SplitType.independentPoints;
*/
            default:
                try
                {
                    IWorkBlock block = blockRepository.getWorkBlockFromType(type);
                    if (block instanceof ISupportsSplitting)
                    {
                        SplitType typ= ((ISupportsSplitting)block).getSplitType();
                        if (typ.type == SplitTypes.useDependencies)
                        {
                            return GlobalSettings.EnableDependencyDistribution ? typ : SplitType.cantSplit;
                        }
                        return typ;
                    }
                }
                catch (Exception e)
                {
                    DebugHelper.PrintException(e,logger);
                    logger.warn("Could not determine split type " + type);
                }

                return SplitType.cantSplit;
        }
    }

    /**
     * Do a plausibility check on the graph.
     * @param graph The graph to check
     */
    public static void CheckGraph(BlockGraph graph)
    {
        for (BlockLink link : graph.Links)
        {
            if (link.OriginBlock == null)
            {
                logger.error("Origin Block null of link : " + link.toDebutString() );
                continue;
            }

            if (link.OriginPort == null)
            {
                logger.error("Origin Port null of link : " + link.toDebutString() );
                continue;
            }

            if (link.DestinationBlock == null)
            {
                logger.error("Destination Block null of link : " + link.toDebutString() );
                continue;
            }


            if (link.DestinationPort == null)
            {
                logger.error("Destination Port null of link : " + link.toDebutString() );
            }
        }
    }

    public static void ClearPorts(Iterable<BlockIO> ports)
    {
        for(BlockIO io:ports)
        {
            io.Invalidate();
        }
    }

    public static void SetupForReRun(GraphBlock blockInGraph)
    {        
        for(BlockIO out: blockInGraph.Outputs)
        {
            if (out.isValid() && out.getValue() instanceof ISupportsMultiPass)
            {
                ISupportsMultiPass value = (ISupportsMultiPass)out.getValue();
                BlockIO loopBackInput = FindInputForLoopingOutput(out, blockInGraph.Inputs, blockInGraph.Type);
                if (loopBackInput != null)
                {
                    loopBackInput.SetValue(value);
                } else
                {
                    logger.error("Loopback port not found");
                    DebugHelper.BreakIntoDebug();
                }
            }

            out.Invalidate();
        }

        blockInGraph.uniqueScheduleId = -1;
        blockInGraph.workPartsScheduled = 0;
        blockInGraph.Itteration++;
        blockInGraph.setStatus(BlockStatus.Ready);
    }

    /***
     * After a block has been finished, and all blocks that depend on him are also finished all data belonging to it can
     * be efficted from memory ... this function will find this finised blocks no one dependes on.
     * @param graph
     * @return
     */
    public static List<GraphBlock> GetEvictableBlocks(BlockGraph graph)
    {
        List<GraphBlock> result = new ArrayList<>();
        for (GraphBlock block : graph.AllBlocks)
        {
            if (block.getStatus()!= BlockStatus.Finished)
            {   continue;   }

            List<GraphBlock> nextLayer = GetBlocksDependingOn(graph,block);
            boolean allDone = true;
            for (GraphBlock nextBlock : nextLayer)
            {
                if (nextBlock.getStatus() != BlockStatus.Finished)
                {
                    allDone = false;
                    break;
                }
            }

            if (allDone){ result.add(block);}
        }
        return result;
    }

    private static List<GraphBlock> GetBlocksDependingOn(BlockGraph graph, GraphBlock source)
    {
        List<GraphBlock> result = new ArrayList<>();
        for (BlockLink link : graph.Links)
        {
            if (link.OriginBlock == source)
            {
                result.add(link.DestinationBlock);
            }
        }
        return result;
    }
}


