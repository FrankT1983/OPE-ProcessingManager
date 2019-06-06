package de.c3e.ProcessManager.DataTypes;

import de.c3e.ProcessManager.BlockRepository.*;
import de.c3e.ProcessManager.Utils.LogUtilities;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * Class helping with transforming and modifying Block graphs.
 */
public class GraphTransformations
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static List<String> FillGraph(BlockGraph graph, List<ScriptInputParameters> inputs)
    {
        DumpGraph(graph);

        List<String> notFound = new ArrayList<>();
        for (ScriptInputParameters param : inputs)
        {
            boolean found = false;

            for (GraphBlock block: graph.AllBlocks)
            {
                if (!block.Id.equals(param.BlockId))
                {   continue;   }

                List<BlockIO> list = param.Direction.equals("in")? block.Inputs : block.Outputs ;
                for (BlockIO input: list)
                {
                    if (!input.Name.equals(param.PortName))
                    {   continue;   }

                    input.SetValue(param.Value);
                    found = true;
                    break;
                }

                if (found)
                {   break;  }
                notFound.add("FillGraphWithInputs : did not find Input to fill : '" + param.BlockId + "' '" + param.PortName + "'");
            }

        }
        return notFound;
    }

    private static void DumpGraph(BlockGraph graph)
    {
        if (graph == null)
        {
            logger.error("No Graph");
            return;
        }

        if (graph.AllBlocks != null)
        {
            if (graph.AllBlocks.size() > 0 )
            {
                for (GraphBlock b : graph.AllBlocks)
                {
                    logger.debug("Block: " + b.Id + " " + b.Type);
                }
            }
            else
            {
                logger.error("empty block list in Graph");
            }
        }
        else
        {
            logger.error("No blocks in Graph");
        }
    }

    /**
     * Perform modifications to make a graph ready to be used by the manager.
     * This means
     *  - replacing OmeroImageInputBlock with a combination of StringParameterWorkBlock and LoadImageWorkBlock
     * @param graph The graph to be modified.
     */
    public static void ModifyGraphForUseInManager(BlockGraph graph)
    {
        ReplaceOmeroImageInputBlocks(graph);
        ReplaceOmeroImageOutputBlocks(graph);
        ReplaceAnnotateImageBlocks(graph);
    }

    private static void ReplaceAnnotateImageBlocks(BlockGraph originGraph)
    {
        do
        {
            GraphBlock omeroBlock = null;
            for (GraphBlock block : originGraph.AllBlocks)
            {
                if (block.Type.equals("AnnotateImageWithData"))
                {
                    omeroBlock = block;
                    break;
                }
            }

            if (omeroBlock == null)
            {
                break;
            }

            originGraph.AllBlocks.remove(omeroBlock);
            GraphBlock newOutBlock = GraphRepository.CreateStandardDataToFileBlock();
            originGraph.AllBlocks.add(newOutBlock);

            GraphBlock fileDestinationBlock = GraphRepository.CreateStandardStringBlock(omeroBlock.Id);
            originGraph.AllBlocks.add(fileDestinationBlock);

            // replace original Links
            {
                List<BlockLink> removeList = new ArrayList<>();
                List<BlockLink> addList = new ArrayList<>();
                for (BlockLink link : originGraph.Links)
                {
                    if (link.DestinationBlock == omeroBlock)
                    {
                        BlockLink replacementLink;
                        if (link.DestinationPort.Name.equals("Data"))
                        {
                            replacementLink = new BlockLink(link.OriginPort, link.OriginBlock,
                                    newOutBlock.InputByName(DataToCsvFileBlock.dataName), newOutBlock);
                            addList.add(replacementLink);
                            removeList.add(link);
                        }
                        else if(link.DestinationPort.Name.equals("ImageId"))
                        {
                            // Storing the result in the correct dataset is handled outside of the Processing Manager
                            removeList.add(link);
                        }
                    }
                }
                originGraph.Links.removeAll(removeList);
                originGraph.Links.addAll(addList);
            }

            // link both blocks
            {
                BlockLink idToImageLoad = new BlockLink(fileDestinationBlock.OutputByName(StringParameterWorkBlock.outputName), fileDestinationBlock,
                        newOutBlock.InputByName(DataToCsvFileBlock.fileName), newOutBlock);

                originGraph.Links.add(idToImageLoad);
            }
        }while (true);
    }

    private static void ReplaceOmeroImageOutputBlocks(BlockGraph originGraph)
    {
        do
        {
            GraphBlock omeroBlock = null;
            for (GraphBlock block : originGraph.AllBlocks)
            {
                if (block.Type.equals("plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet"))
                {
                    omeroBlock = block;
                    break;
                }
            }

            if (omeroBlock == null)
            {
                break;
            }

            originGraph.AllBlocks.remove(omeroBlock);
            GraphBlock newOutBlock = GraphRepository.CreateStandardOutBlock();
            originGraph.AllBlocks.add(newOutBlock);

            GraphBlock fileDestinationBlock = GraphRepository.CreateStandardStringBlock(omeroBlock.Id);
            originGraph.AllBlocks.add(fileDestinationBlock);

            // replace original Links
            {
                List<BlockLink> removeList = new ArrayList<>();
                List<BlockLink> addList = new ArrayList<>();
                for (BlockLink link : originGraph.Links)
                {
                    if (link.DestinationBlock == omeroBlock)
                    {
                        BlockLink replacementLink;
                        if (link.DestinationPort.Name.equals("Image to save"))
                        {
                            replacementLink = new BlockLink(link.OriginPort, link.OriginBlock,
                                    newOutBlock.InputByName(OutputWorkBlock.ImageInput), newOutBlock);
                            addList.add(replacementLink);
                            removeList.add(link);
                        }
                        else if(link.DestinationPort.Name.equals("DataSet Id"))
                        {
                            // Storing the result in the correct dataset is handled outside of the Processing Manager
                            removeList.add(link);
                        }
                    }
                }
                originGraph.Links.removeAll(removeList);
                originGraph.Links.addAll(addList);
            }

            // link both blocks
            {
                BlockLink idToImageLoad = new BlockLink(fileDestinationBlock.OutputByName(StringParameterWorkBlock.outputName), fileDestinationBlock,
                        newOutBlock.InputByName(OutputWorkBlock.PathInput), newOutBlock);

                originGraph.Links.add(idToImageLoad);
            }


        }while (true);
    }

    private static void ReplaceOmeroImageInputBlocks(BlockGraph originGraph)
    {
        ReplaceInputBlocks(originGraph,"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock",
                LoadImageWorkBlock.typeName,LoadImageWorkBlock.inputName,LoadImageWorkBlock.inputImage,LoadImageWorkBlock.outputName);
        ReplaceInputBlocks(originGraph,"plugins.Frank.de.c3e.ProcessManager.OmeroTextFileInputBlock",
                LoadTextFileWorkBlock.typeName,LoadTextFileWorkBlock.inputName,LoadTextFileWorkBlock.inputImage,LoadTextFileWorkBlock.outputName);
        ReplaceInputBlocks(originGraph,"OmeroTextFileInputBlock",
                LoadTextFileWorkBlock.typeName,LoadTextFileWorkBlock.inputName,LoadTextFileWorkBlock.inputImage,LoadTextFileWorkBlock.outputName);
    }

    private static void ReplaceInputBlocks(BlockGraph originGraph, String inputBlockType,String loadBlockName, String loadInputName, String loadInputFile,  String loadOutputName)
    {
        do
        {
            GraphBlock omeroBlock = null;
            for (GraphBlock block : originGraph.AllBlocks)
            {
                if (block.Type.equals(inputBlockType))
                {
                    omeroBlock = block;
                    break;
                }
            }

            if (omeroBlock == null) {   break;  }

            logger.info("Replace Block " + omeroBlock.toString());

            originGraph.AllBlocks.remove(omeroBlock);

            GraphBlock inputImageId = GraphRepository.CreateStandardStringBlock(omeroBlock.Id);
            GraphBlock loadImage = GraphRepository.CreateStandardImageLoadBlock(AddOriginalBlockPrefix(omeroBlock.Id),loadBlockName,loadInputName, loadInputFile,loadOutputName);

            // construct string parameter block to get the image name from the parameter file
            originGraph.AllBlocks.add(inputImageId);

            // construct loading block to load exactly that image
            originGraph.AllBlocks.add(loadImage);

            // replace original Links
            {
                List<BlockLink> removeList = new ArrayList<>();
                List<BlockLink> addList = new ArrayList<>();
                for (BlockLink link : originGraph.Links)
                {
                    if (link.OriginBlock == omeroBlock)
                    {
                        BlockLink replacementLink = new BlockLink(loadImage.Outputs.get(0), loadImage,
                                link.DestinationPort, link.DestinationBlock);
                        removeList.add(link);
                        addList.add(replacementLink);
                    }
                }
                originGraph.Links.removeAll(removeList);
                originGraph.Links.addAll(addList);
            }

            // link both blocks
            {
                BlockLink idToImageLoad = new BlockLink(inputImageId.Outputs.get(0), inputImageId, loadImage.Inputs.get(0), loadImage);
                originGraph.Links.add(idToImageLoad);
            }

        }while (true);
    }

    private static final String OriginalPrefix = "xxOriginalxx";

    /**
     * When reading a graph, the load blocks will be split into two. A StringParameterBlock and the
     * actual load block. Since only on Id is available for these two blocks, on will get a prefix.
     * @param input The Id to modify.
     * @return The modified id.
     */
    private static String AddOriginalBlockPrefix(String input)
    {
        return OriginalPrefix + input;
    }

    /**
     * When reading a graph, the load blocks will be split into two. A StringParameterBlock and the
     * actual load block. Since only on Id is available for these two blocks, on will get a prefix.
     * This function will reverse the process.
     * @param input The Id to modify.
     * @return The modified id.
     */
    public static String RemoveOriginalBlockPrefix(String input)
    {
        return input.replaceFirst("^"+OriginalPrefix, "");
    }
}
