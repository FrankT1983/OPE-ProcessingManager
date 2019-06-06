package de.c3e.BlockTemplates.Templates;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.Basics.ParallelCalculatorBase;
import de.c3e.BlockTemplates.Templates.Interfaces.IHasInputParameters;
import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.Utils.*;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

/**
 * The implementation class for non size changing plugins.
 * This will handle getting the inputs, passing it to the plugin and the overall
 * integration with the rest of the processing.
 */
public class NonSizeChangingPluginImpl extends BlockTemplatesBase implements IWorkBlock, IHasInputParameters, ISupportsSplitting
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private ParallelCalculatorBase implementation;

    public static final String ImageInput = "Input";
    @SuppressWarnings("WeakerAccess")
    public static final String ImageOutput = "Output";

    private AbstractImageObject inputObject;
    private AbstractImageObject outputObject;
    private boolean isFinished = false;

    public static NonSizeChangingPluginImpl CreateFromPlugin(ParallelCalculatorBase input)
    {
        NonSizeChangingPluginImpl imp = new NonSizeChangingPluginImpl();
        input.SetImplementation(imp);
        imp.SetImplementation(input);
        return imp;
    }

    private void SetImplementation(ParallelCalculatorBase implementation)
    {
        this.implementation = implementation;
    }

    @Override
    public SplitType getSplitType()
    {
        return this.implementation.getSplitType();
    }

    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        if (inputs.containsKey(ImageInput))
        {
            this.inputObject  = (AbstractImageObject)inputs.get(ImageInput);
        }

        setInputsForAttributes(this,this.GetInputsFromAttributes(this.implementation), inputs);
    }

    @Override
    public boolean RunWork()
    {
        AbstractImageObject inputImage = this.inputObject;
        List<ImageSubBlock> resultImages = new ArrayList<>();

        if (inputImage == null)
        {
            return false;
        }

        if (this.implementation != null)
        {
            ParallelCalculatorBase impl = this.implementation;
            ImageDimension[] dependencies = impl.getDependency();
            for (ImageSubBlock block : inputImage.getLocalSlices(dependencies))
            {
                ImageSubBlock result = ImageSubBlock.ofSameType(block);
                result.dimensions = block.dimensions;


                impl.setMinValue(block.getMinValue());
                impl.setMaxValue(block.getMaxValue());

                Object resizedToDimensions;
                {
                    Object castToCorrectType1D;
                    {
                        double[] data = TypeConversionHelper.ToDoubleArray(block.data);
                        // convert first, so only one conversion routine is needed.
                        castToCorrectType1D = TypeConversionHelper.CastToCorrectType1D(impl.typeOfT, double.class, data);
                    }

                    int[] sizes = SubSetFinder.SizesOfDimension(block.dimensions, dependencies);
                    resizedToDimensions = TypeConversionHelper.Reshape(castToCorrectType1D, sizes);
                }

                Object outStuff;
                try
                {
                    // todo:
                    // this is awkward, but since i don't know how deeply nested the array is and what I'm really calling
                    // reflection will have to do.
                    // maybe build Object Calculate(Object) in base class, that calls correct implementation?

                    // edge case: Point calculator does not work on arrays, but rather directly on the Type argument
                    Method pointCalc = null;
                    try
                    {
                        pointCalc = impl.getClass().getMethod("Calculate", impl.typeOfT);
                    }
                    catch (Exception e)
                    {
                        // do nothing
                    }

                    if (pointCalc != null)
                    {
                        // this is somewhat unnecessary => reshape a single point ... but it keeps the amount of special code
                        // low
                        // todo: find an elegant way to enable plugin in like special cases for performance.
                        outStuff = pointCalc.invoke(impl, Array.get(resizedToDimensions,0));
                    }
                    else
                    {
                        outStuff = impl.getClass().getMethod("Calculate", resizedToDimensions.getClass()).invoke(impl, resizedToDimensions);
                    }
                }
                catch (Exception e)
                {
                    DebugHelper.BreakIntoDebug();
                    logger.error("Exception in point calculator: \n" + e.toString());
                    return  false;
                }

                Object flattenedResult = TypeConversionHelper.Flatten(outStuff);
                result.data = TypeConversionHelper.CastToCorrectType1D(block.type, impl.typeOfT, flattenedResult);
                resultImages.add(result);
            }
        }


        this.outputObject = AbstractImageObject.fromSubBlocksAndTemplate(resultImages,this.inputObject);
        this.isFinished = true;
        return true;
    }


    public <T> T getInput(String id)
    {
        if (!this.Inputs.containsKey(id))
        {
            logger.error("Tried to access parameter that did not exist " + id);
        }

        Object data = this.Inputs.get(id);
        //noinspection unchecked
        return (T)TypeConversionHelper.CastToCorrectType(this.implementation.typeOfT,data.getClass(),data);
    }

    @Override
    public Map<String, Object> GetResults()
    {
        Map<String, Object> result = new HashMap<>();
        result.put(ImageOutput,this.outputObject);
        return result;
    }

    @Override
    public final boolean IsFinished()
    {
        return this.isFinished;
    }
}
