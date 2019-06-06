package de.c3e.ProcessManager.BlockRepository;

import de.c3e.ProcessManager.BlockRepository.Benchmarking.LinearDelayBlock;
import de.c3e.ProcessManager.Utils.LogUtilities;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

/**
 * Figure out which block to run from a given typeName.
 */
public class BlockRepository
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public IWorkBlock getWorkBlockFromType(String type)
    {
        switch (type)
        {
            case "plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong":
            case "LongParameter":
                return LogAndReturn(new LongParameterWorkBlock());

            case LoadImageWorkBlock.typeName:
                return LogAndReturn(new LoadImageWorkBlock());

            case LoadTextFileWorkBlock.typeName:
                return LogAndReturn(new LoadTextFileWorkBlock());

            case "invert":
                return LogAndReturn(new InvertWorkBlock());

            case OutputWorkBlock.typeName:
                return LogAndReturn(new OutputWorkBlock());

            case CollectResultsWorkBlock.typeName:
                return LogAndReturn(new CollectResultsWorkBlock());

            case StringParameterWorkBlock.typeName:
                return LogAndReturn(new StringParameterWorkBlock());

            case DataToCsvFileBlock.TypeName:
                return LogAndReturn(new DataToCsvFileBlock());

            case "plugins.adufour.roi.ROIMeasures":
                return LogAndReturn(new RoiMeasuresIcyModifyed());

            case "SameParameter":
                return LogAndReturn(new SameParameter());

            case "Constant":
                return LogAndReturn(new ConstantParameterBlock());

            case "LinearDelayBlock":
            case "de.c3e.ProcessManager.BlockRepository.Benchmarking.LinearDelayBlock":
                return  LogAndReturn( new LinearDelayBlock());

            default:
                IWorkBlock plugin = PluginBlockRepository.TryGetPluginBlock(type);
                if (plugin != null)
                {
                    return plugin;
                }

                logger.info("Try Icy Repo for " + type);
                IWorkBlock icyWorker = IcyBlockRepository.TryGetIcyBlock(type);
                if (icyWorker != null)
                {
                    return icyWorker;
                }
                logger.error("Could not find block typeName in Repo: " + type);
        }
        return null;
    }

    private IWorkBlock LogAndReturn(IWorkBlock block)
    {
        logger.info("Found block in BlockRepository " + block.getClass().getName() );
        return block;
    }
}
