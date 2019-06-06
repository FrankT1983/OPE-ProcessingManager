package de.c3e.BlockTemplates.Templates;

import com.google.common.base.Joiner;
import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.Basics.ParallelProjectionBase;
import de.c3e.BlockTemplates.Templates.Interfaces.IHasInputParameters;
import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsOutputLoopBack;
import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.Utils.*;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;
import de.c3e.ProcessingManager.Types.SplitTypes;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Implementation for al basic handling of projection plugins.
 */
public class ProjectionPluginImpl extends BlockTemplatesBase implements IWorkBlock, IHasInputParameters, ISupportsSplitting , ISupportsOutputLoopBack
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private ParallelProjectionBase implementation;

    public static final String ImageInput = "Input";
    public static final String ImageOutput = "Output";
    public static final String PluginFunctionName = "Aggregate";

    private AbstractImageObject inputObject;
    private AbstractImageObject outputObject;
    private boolean isFinished = false;

    public static ProjectionPluginImpl CreateFromPlugin(ParallelProjectionBase input)
    {
        ProjectionPluginImpl imp = new ProjectionPluginImpl();
        input.SetImplementation(imp);
        imp.SetImplementation(input);
        return imp;
    }

    private void SetImplementation(ParallelProjectionBase implementation)
    {
        this.implementation = implementation;
    }

    @Override
    public SplitType getSplitType()
    {
        if (GlobalSettings.EnableMultiPassProjectionCalculations)
        {    return this.implementation.getSplitType();}
        else
        // legacy code / unit test baseline during implementation of MultiPassProjectionCalculations
        {
            List<ImageDimension> foo = new ArrayList<>(Arrays.asList(this.implementation.getDependency()));
            foo.add(this.implementation.getProjectionDirection());
            ImageDimension[] conv = new ImageDimension[foo.size()];
            for (int i =0;i< foo.size();i++)
            {
                conv[i]= foo.get(i);
            }

            return new SplitType(SplitTypes.useDependencies, conv);
        }
    }

    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        if (inputs.containsKey(ImageInput))
        {
            this.inputObject = (AbstractImageObject) inputs.get(ImageInput);
        }

        setInputsForAttributes(this,this.GetInputsFromAttributes(this.implementation), inputs);
    }

    public <T> T getInput(String id)
    {
        if (!this.Inputs.containsKey(id))
        {
            logger.error("Tried to access parameter that did not exist " + id);
        }

        return (T)this.Inputs.get(id);

    }

    @Override
    public Map<String, Object> GetResults()
    {
        Map<String, Object> result = new HashMap<>();
        result.put(ImageOutput, this.outputObject);
        return result;
    }

    @Override
    public final boolean IsFinished()
    {
        return this.isFinished;
    }


    @Override
    public boolean RunWork()
    {
        AbstractImageObject inputImage = this.inputObject;
        List<ImageSubBlock> resultImages = new ArrayList<>();

        if (this.implementation != null)
        {
            ParallelProjectionBase impl = this.implementation;
            ImageDimension[] dependencies = impl.getDependency();
            ImageDimension projectionDir = impl.getProjectionDirection();


            if (dependencies == null || dependencies.length == 0)
            {
                return RunWorkPointCalculation();
            }


            List<ImageSubBlock> compacted = ImageSubBlockUtils.CompactSubBlocks(inputImage.getLocalPartsSubBlocks());
            List<ImageSubBlock> calculationChunks = new ArrayList<>();

            for (ImageSubBlock block : compacted)
            {
                // cut up everything into the chunks that each calculation needs
                calculationChunks.addAll(block.getCutLocalSlicesKeepSizeInDimensions(dependencies));
            }

            // sort by projection dimension
            List<List<ImageSubBlock>> sortedByProjectionDimension = ImageSubBlockUtils.SortByDimension(calculationChunks, Arrays.asList(projectionDir));

            for (List<ImageSubBlock> mergeParts : sortedByProjectionDimension)
            {
                // project till only one slice remains
                while (mergeParts.size() > 1)
                {
                    ImageSubBlock block1 = mergeParts.get(0);
                    mergeParts.remove(0);

                    ImageSubBlock block2 = mergeParts.get(0);
                    mergeParts.remove(0);

                    ImageSubBlock result = ImageSubBlock.ofSameType(block1);
                    result.dimensions = new ImageSubSet(block1.dimensions);

                    Object resizedToDimensions1;
                    Object resizedToDimensions2;

                    List<Method> methodList = ReflectionHelper.getMethodWithName(PluginFunctionName,impl);
                    boolean hasNativeFunction = ReflectionHelper.GetMethodForElementType(methodList, block1.data.getClass(), block2.data.getClass()) != null;

                    if (!hasNativeFunction)
                    {   logger.info(this.implementation.getClass().getName() + " has no native function for type " + ReflectionHelper.getBaseElementType(block1.data.getClass()));}

                    {

                        Object castToCorrectType1D_1 = ConvertToAppropriate1DType(impl,block1, hasNativeFunction);
                        int[] sizes = SubSetFinder.SizesOfDimension(block1.dimensions, dependencies);
                        resizedToDimensions1 = TypeConversionHelper.Reshape(castToCorrectType1D_1, sizes);
                    }


                    {
                        Object castToCorrectType1D_2 = ConvertToAppropriate1DType(impl,block1, hasNativeFunction);

                        int[] sizes = SubSetFinder.SizesOfDimension(block2.dimensions, dependencies);
                        resizedToDimensions2 = TypeConversionHelper.Reshape(castToCorrectType1D_2, sizes);
                    }

                    // collapse expected sizes in projection
                    result.dimensions.setStart(Math.min(block1.dimensions.getStart(projectionDir), block2.dimensions.getStart(projectionDir)), projectionDir);
                    result.dimensions.setSize(1, projectionDir);

                    Object outStuff;
                    try
                    {
                        Method otherCalculators = ReflectionHelper.GetMethodForType(methodList,resizedToDimensions1.getClass(), resizedToDimensions2.getClass());
                        if (otherCalculators == null && impl.typeOfT == null)
                        {
                            // try finding the generic fallback
                            otherCalculators = ReflectionHelper.GetMethodForElementType(methodList,Object.class, Object.class);
                        }
                        if (otherCalculators == null)
                        {
                            DebugHelper.BreakIntoDebug();
                            logger.error("Could not find calculation function for " + this.getClass().getName());
                            return false;
                        }

                        if (otherCalculators.getParameterTypes().length == 2)
                        {
                            outStuff = otherCalculators.invoke(impl, resizedToDimensions1, resizedToDimensions2);
                        }
                        else
                        {
                            outStuff = otherCalculators.invoke(impl, resizedToDimensions1, resizedToDimensions2 ,1 ,2);
                        }
                    } catch (Exception e)
                    {
                        DebugHelper.BreakIntoDebug();
                        DebugHelper.PrintException(e,logger);
                        logger.error("Exception running plugin: \n" + e.toString());
                        return false;
                    }

                    Object flattenedResult = TypeConversionHelper.Flatten(outStuff);
                    if (block1.type.equals( ReflectionHelper.getBaseElementType(flattenedResult.getClass())))
                    {
                        result.data = flattenedResult;
                    }
                    else
                    {
                        result.data = TypeConversionHelper.CastToCorrectType1D(block1.type, flattenedResult);
                    }
                    mergeParts.add(result);
                }
                resultImages.add(mergeParts.get(0));
            }

            ImageSubSet resultSize = this.inputObject.getFullBounds();
            resultSize.setSize(1, projectionDir);

            logger.warn("Debug: Projected " + Joiner.on(" ||  ").join(compacted) + " to " + Joiner.on(" || ").join(resultImages));

            this.outputObject = AbstractImageObject.fromSubBlocksAndFullBounds(resultImages, resultSize);
        }

        this.isFinished = true;
        return true;
    }

    private static Object ConvertToAppropriate1DType(ParallelProjectionBase impl, ImageSubBlock block, boolean hasNativeFunction)
    {
        if (!hasNativeFunction)
        {
            if (impl.typeOfT != null )
            {
                return TypeConversionHelper.CastToCorrectType1D(impl.typeOfT, block.data);
            }
            else
            {
                // has a generic function -> go to boxed type
                Class wrapperType = ClassUtils.primitiveToWrapper( block.data.getClass().getComponentType());
                return  TypeConversionHelper.CastToCorrectType1D(wrapperType, block.data);
            }
        }
        else
        {
            return block.data;
        }
    }

    /**
     * Edge case: This calculation actually requires only singe points.
     * This will usually run out of memory, since it creates slices for each and every single point
     * => make it a special case for now
     * @return true if successful.
     */
    private boolean RunWorkPointCalculation()
    {
        AbstractImageObject inputImage = this.inputObject;
        List<ImageSubBlock> resultImages = new ArrayList<>();

        if (this.inputObject == null)
        {
            logger.error("No Input object");
            return false;
        }

        if (this.implementation != null)
        {
            ParallelProjectionBase impl = this.implementation;
            ImageDimension projectionDir = impl.getProjectionDirection();

            List<ImageSubBlock> compacted = ImageSubBlockUtils.CompactSubBlocks(inputImage.getLocalPartsSubBlocks());
            if (compacted == null)
            {  return  false;}

            List<ImageSubBlock> compacted2 = new ArrayList<>();
            while (compacted.size() >0)
            {
                ImageSubBlock  c = compacted.get(0);
                compacted.remove(0);
                compacted2.add(RunWorkPointCalculation( c , impl));
            }

            compacted = compacted2;
            if (compacted.size() == 1 && compacted.get(0).dimensions.getSize(projectionDir) == 1)
            {
                // pre-compaction took care of everything
                ImageSubSet resultSize = this.inputObject.getFullBounds();
                resultSize.setSize(1,projectionDir);

                this.outputObject = AbstractImageObject.fromSubBlocksAndFullBounds(compacted, resultSize);
                this.isFinished = true;
                return true;
            }

            List<ImageSubBlock> calculationChunks = new ArrayList<>();
            Collection<ImageDimension> o = ImageDimension.others(Collections.singletonList(projectionDir));
            for (ImageSubBlock block : compacted)
            {
                // cut up everything into the chunks that each calculation needs
                calculationChunks.addAll(block.getCutLocalSlicesKeepSizeInDimensions(o));
            }

            // sort by projection dimension
            List<List<ImageSubBlock>> sortedByProjectionDimension = ImageSubBlockUtils.SortByDimension(calculationChunks, Collections.singletonList(projectionDir));
            for (List<ImageSubBlock> mergeParts : sortedByProjectionDimension)
            {
                while (mergeParts.size() > 1)
                {
                    ImageSubBlock block1 = mergeParts.get(0);
                    ImageSubBlock block2 = null;
                    mergeParts.remove(0);


                    ImageSubBlock result = ImageSubBlock.ofSameType(block1);
                    result.dimensions = block1.dimensions;

                    Object resizedToDimensions1;
                    Object resizedToDimensions2;
                    {
                        // scope this, to save memory
                        double[] data1 = TypeConversionHelper.ToDoubleArray(block1.data);
                        resizedToDimensions1 = TypeConversionHelper.CastToCorrectType1D(impl.typeOfT, double.class, data1);
                    }

                    {
                        block2 = mergeParts.get(0);
                        mergeParts.remove(0);
                        double[] data2 = TypeConversionHelper.ToDoubleArray(block2.data);
                        resizedToDimensions2 = TypeConversionHelper.CastToCorrectType1D(impl.typeOfT, double.class, data2);

                        result.dimensions.setStart(Math.min(block1.dimensions.getStart(projectionDir), block2.dimensions.getStart(projectionDir)), projectionDir);
                        result.dimensions.setSize(1, projectionDir);
                    }

                    int size = Array.getLength(resizedToDimensions1);
                    if (size != Array.getLength(resizedToDimensions2))
                    {
                        DebugHelper.BreakIntoDebug();
                    }

                    Object outStuff = Array.newInstance(impl.typeOfT,size);
                    try
                    {

                        // edge case: Point calculator does not work on arrays, but rather directly on the Type argument
                        Method pointCalc = null;
                        try
                        {
                            pointCalc = impl.getClass().getMethod(PluginFunctionName, impl.typeOfT, impl.typeOfT);
                        } catch (Exception e)
                        {   /* do nothing */ }

                        if (pointCalc != null)
                        {
                            for (int i = 0; i < size;i++)
                            {
                                Object res = pointCalc.invoke(impl, Array.get(resizedToDimensions1, i), Array.get(resizedToDimensions2, i));
                                Array.set(outStuff,i,res);
                            }
                        }else
                        {
                            try
                            {
                                pointCalc = impl.getClass().getMethod(PluginFunctionName, impl.typeOfT, impl.typeOfT, int.class ,int.class);
                            } catch (Exception e)
                            {   /* do nothing */ }

                            if (pointCalc != null)
                            {
                                int index1 = block1.dimensions.getStart(projectionDir);
                                int index2 = block2.dimensions.getStart(projectionDir);
                                for (int i = 0; i < size;i++)
                                {
                                    Object res = pointCalc.invoke(impl, Array.get(resizedToDimensions1, i), Array.get(resizedToDimensions2, i),
                                            index1,index2);
                                    Array.set(outStuff,i,res);
                                }
                            }else
                            {
                                throw new UnsupportedOperationException();
                            }
                        }
                    } catch (Exception e)
                    {
                        DebugHelper.BreakIntoDebug();
                        logger.error("Exception in point calculator: \n" + e.toString());
                        return false;
                    }

                    Object flattenedResult = TypeConversionHelper.Flatten(outStuff);
                    result.data = TypeConversionHelper.CastToCorrectType1D(block1.type, impl.typeOfT, flattenedResult);
                    mergeParts.add(result);
                }
                resultImages.add(mergeParts.get(0));
            }

            ImageSubSet resultSize = this.inputObject.getFullBounds();
            resultSize.setSize(1, projectionDir);

            this.outputObject = AbstractImageObject.fromSubBlocksAndFullBounds(resultImages, resultSize);
        }
        this.isFinished = true;
        return true;
    }

    private ImageSubBlock RunWorkPointCalculation(ImageSubBlock block, ParallelProjectionBase impl)
    {
        ImageDimension projectionDir = impl.getProjectionDirection();
        ImageSubBlock result = ImageSubBlock.ofSameType(block);
        result.dimensions = new ImageSubSet(block.dimensions);

        if ( block.dimensions.getSize(projectionDir)>1 && block.type.equals(short.class) && impl instanceof PointProjectorDouble)
        {
            return ProjectionPluginOptimizations.RunPointProjectingOnDoubleImplFromShortArray(block, (PointProjectorDouble) impl, projectionDir, result);
        }else if (block.dimensions.getSize(projectionDir)>1 && block.type.equals(short.class) && impl instanceof PointProjectorShort)
        {
            return ProjectionPluginOptimizations.RunPointProjectingOnShortImplFromShortArray(block, (PointProjectorShort) impl, projectionDir, result);
        }

        // DebugHelper.BreakIntoDebug();
        return  block;
    }

    @Override
    public String getNameOfLoopBackPortForOutput(String output)
    {
        if (output.equals(ImageOutput))
        {
            return ImageInput;
        }
        return null;
    }
}