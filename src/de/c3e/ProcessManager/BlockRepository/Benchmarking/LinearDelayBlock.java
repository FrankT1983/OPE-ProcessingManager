package de.c3e.ProcessManager.BlockRepository.Benchmarking;

import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.BlockRepository.LoggingWorkBlockBase;
import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessingManager.Types.SplitType;
import de.c3e.ProcessingManager.Types.SplitTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frank on 14.06.2017.
 */
public class LinearDelayBlock extends LoggingWorkBlockBase implements ISupportsSplitting
{
    public static final String ImageInput = "Input";
    public static final String DelayInput = "Delay";
    public static final String ImageOutput = "Output";

    private AbstractImageObject inputObject;
    private AbstractImageObject outputObject;
    private double delay;
    private boolean isFinished = false;

    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        if (inputs.containsKey(ImageInput))
        {
            this.inputObject  = (AbstractImageObject)inputs.get(ImageInput);
        }

        if (inputs.containsKey(DelayInput))
        {
            Object delay = inputs.get(DelayInput);
            if (delay instanceof Double )
            {
                this.delay = (double)delay;
            }
            else if ( delay instanceof  String)
            {
                this.delay = Double.parseDouble((String) delay);
            }
        }
    }

    @Override
    public Map<String, Object> GetResults()
    {
        Map<String, Object> result = new HashMap<>();
        result.put(ImageOutput,this.outputObject);
        return result;
    }

    @Override
    public boolean RunWork()
    {
        super.RunWork();
        AbstractImageObject inputImage = this.inputObject;
        long pixels = inputImage.getLocalSizePixelCount();
        long wait = (long)(this.delay * pixels);

        logger.info("Delay: " + String.valueOf(this.delay) +" " + pixels + " pixels : "+ wait + " millis");

        try
        {
            Thread.sleep(wait);
        } catch (InterruptedException e)
        {
            logger.error("Exception Waiting :" + e);
            e.printStackTrace();
        }
        logger.info("Finished waiting : "+ wait + " millis");

        this.outputObject = this.inputObject;
        this.isFinished = true;
        return true;
    }


    @Override
    public final boolean IsFinished()
    {
        return this.isFinished;
    }

    @Override
    public SplitType getSplitType()
    {
        return new SplitType(SplitTypes.independentPoints);
    }
}
