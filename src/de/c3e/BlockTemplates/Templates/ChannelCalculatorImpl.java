package de.c3e.BlockTemplates.Templates;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.Interfaces.IHasInputParameters;
import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.Utils.ImageSubBlock;
import de.c3e.ProcessManager.Utils.LogUtilities;
import de.c3e.ProcessManager.Utils.TypeConversionHelper;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * Transform an image base on the
 */
public class ChannelCalculatorImpl extends BlockTemplatesBase implements IWorkBlock, IHasInputParameters, ISupportsSplitting
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static  final String ImageInput = "Input";
    public static  final String ImageOutput = "Output";

    private ChannelCalculator implementation;

    private AbstractImageObject inputObject;
    private AbstractImageObject outputObject;
    private boolean isFinished = false;

    public static ChannelCalculatorImpl CreateFromPlugin(ChannelCalculator input)
    {
        ChannelCalculatorImpl imp = new ChannelCalculatorImpl();
        input.SetImplementation(imp);
        imp.SetImplementation(input);
        return imp;
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

        logger.info("start ChannelCalculator");

        List<ImageSubBlock> resultImages = new ArrayList<>();
        for (ImageSubBlock block : inputImage.getLocalSlices(Dependencies()))
        {
            ImageSubBlock result = ImageSubBlock.ofSameType(block);
            result.dimensions = block.dimensions;

            int dataSize = block.dimensions.getPixelSize();
            double[][] data2d = new double[block.dimensions.SizeY][];
            {
                double[] data = TypeConversionHelper.ToDoubleArray(block.data);
                for (int y = 0; y < block.dimensions.SizeY; y++)
                {
                    data2d[y] = Arrays.copyOfRange(data, y * block.dimensions.SizeX, (y + 1) * block.dimensions.SizeX);
                }
            }

            double[][] outDouble2d = this.implementation.Calculate(data2d);
            double[] outDouble = new double[dataSize];

            for (int y = 0; y < block.dimensions.SizeY;y++)
            {
                for (int x = 0; x < block.dimensions.SizeX;x++)
                {
                    outDouble[y*block.dimensions.SizeX+x] = outDouble2d[y][x];
                }
            }

            if (block.type.getName().equals(byte.class.getName()))
            {
                byte[] output = TypeConversionHelper.ToByteArray(outDouble);
                result.data = output;
            } else if (block.type.getName().equals(double.class.getName()))
            {
                double[] output = TypeConversionHelper.ToDoubleArray(outDouble);
                result.data = output;
            }

            resultImages.add(result);
        }

        this.outputObject = AbstractImageObject.fromSubBlocksAndTemplate(resultImages,this.inputObject);
        this.isFinished = true;

        logger.info("finished ChannelCalculator");
        return true;
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

    public void SetImplementation(ChannelCalculator implementation)
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

    public static ImageDimension[] Dependencies()
    {
        return new ImageDimension[]{ImageDimension.X, ImageDimension.Y};
    }
}