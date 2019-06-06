package de.c3e.BlockTemplates.Templates;

import de.c3e.BlockTemplates.Templates.Annotations.InputParameter;
import de.c3e.BlockTemplates.Templates.Interfaces.IHasInputParameters;
import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.DataTypes.BlockIO;
import de.c3e.ProcessManager.DataTypes.GraphBlock;
import de.c3e.ProcessManager.Utils.*;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

import static de.c3e.BlockTemplates.Templates.ProjectionPluginImpl.PluginFunctionName;

/**
 * Base class, for a block, that calculates a new image on a pixel wise basis;
 *
 * this ins an old class and is replaced by NonSizeChangingPluginImpl
 */
@Deprecated
public class PointCalculatorImpl extends BlockTemplatesBase implements IWorkBlock, IHasInputParameters, ISupportsSplitting
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static PointCalculatorImpl CreateFromPlugin(PointCalculator input)
    {
        PointCalculatorImpl imp = new PointCalculatorImpl();
        input.SetImplementation(imp);
        imp.SetImplementation(input);
        return imp;
    }

    public static final String ImageInput = "Input";
    public static final String ImageOutput = "Output";

    private AbstractImageObject inputObject;
    private AbstractImageObject outputObject;
    private boolean isFinished = false;

    private PointCalculator implementation;

    @Override
    public final void SetInputs(Map<String, Object> inputs)
    {
        if (inputs.containsKey(ImageInput))
        {
            this.inputObject  = (AbstractImageObject)inputs.get(ImageInput);
        }

        setInputsForAttributes(this,this.GetInputsFromAttributes(this.implementation), inputs);
    }

    @Override
    public final boolean RunWork()
    {
        AbstractImageObject inputImage = this.inputObject;

        logger.info("start PointCalculator");

        if (inputImage != null)
        {
            List<ImageSubBlock> resultImages = new ArrayList<>();
            for (ImageSubBlock block : inputImage.getLocalPartsSubBlocks())
            {
                ImageSubBlock result = ImageSubBlock.ofSameType(block);

                result.dimensions = block.dimensions;

                int pixelCount = block.dimensions.getPixelSize();
                Object resultArray = Array.newInstance(block.type,pixelCount);
                result.data = resultArray;

                List<Method> methodList = ReflectionHelper.getMethodWithName("Calculate",this.implementation);
                Method nativeFunction = ReflectionHelper.GetMethodForElementType(methodList, block.data.getClass());

                if (nativeFunction == null)
                {
                    for (int i=0;i<pixelCount;i++)
                    {
                        Object d = Array.get(block.data,i);
                        double conv = (double)TypeConversionHelper.CastToCorrectType(double.class,block.type,d);
                        double res = this.implementation.Calculate(conv);
                        Object backConv = TypeConversionHelper.CastToCorrectType(block.type,double.class,res);

                        Array.set(resultArray,i,backConv);
                    }
                }
                else
                {
                    try
                    {
                        for (int i=0;i<pixelCount;i++)
                        {
                            Object d = Array.get(block.data,i);

                                Object res = nativeFunction.invoke(this.implementation, d);
                                Array.set(resultArray,i,res);
                        }
                    }
                    catch (Exception ex)
                    {
                        DebugHelper.PrintException(ex,logger);
                        DebugHelper.BreakIntoDebug();
                    }
                }

                resultImages.add(result);
            }
            this.outputObject = AbstractImageObject.fromSubBlocksAndTemplate(resultImages,this.inputObject);
        }
        else
        {
            logger.info("Input null, nothing to do");
        }


        this.isFinished = true;

        logger.info("finished PointCalculator");
        return true;
    }

    @Override
    public final Map<String, Object> GetResults()
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

    public static void InitGraphBlock(GraphBlock addBlock, Class blockClass)
    {
        {
            BlockIO imageInput = new BlockIO();
            imageInput.Id = ImageInput;
            imageInput.Name = ImageInput;
            addBlock.Inputs.add(imageInput);
        }

        for (InputParameter input : GetInputAnnotations(blockClass))
        {
            BlockIO generated = new BlockIO();
            generated.Id = input.Name();
            generated.Name = input.Name();
            addBlock.Inputs.add(generated);
        }

        {
            BlockIO outImage = new BlockIO();
            outImage.Id = ImageOutput;
            outImage.Name = ImageOutput;
            addBlock.Outputs.add(outImage);
        }
    }

    public void SetImplementation(PointCalculator implementation)
    {
        this.implementation = implementation;
    }

    @Override
    public SplitType getSplitType()
    {
        return this.implementation.getSplitType();
    }

    @Override
    public <T> T getInput(String s)
    {
        return null;
    }
}
