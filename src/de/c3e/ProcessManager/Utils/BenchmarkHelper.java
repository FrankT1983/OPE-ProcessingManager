package de.c3e.ProcessManager.Utils;

import de.c3e.ProcessManager.WorkerManager.Messages.SendObjectPartMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;

public class BenchmarkHelper
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private static double delayFactor = 0;


    public static void SetDelayFactor(double newFactor)
    {
        delayFactor = newFactor;
        logger.info("Change DelayFactor to " + delayFactor);
    }

    /**
     *
     * Network is to fast to measure its impact on the balancer.
     * This will slowdown transfer for image data
     * @param object
     */
    public static void DelayDataTransfer(Serializable object, String Message)
    {
        if (!(object instanceof SendObjectPartMessage))
        {   return; }

        if (Double.compare(delayFactor,0.0) == 0)
        {
            logger.info("Don't delay");
            return;
        }

        if (object instanceof SendObjectPartMessage)
        {
            SendObjectPartMessage m = (SendObjectPartMessage)(object);
            if (m.getObjectPart() instanceof ImageSubBlock)
            {
                long pixels = ((ImageSubBlock) m.getObjectPart()).dimensions.getPixelSize();

                try
                {
                    // sleep while waiting for the results.
                    long delay = (long)(pixels*delayFactor);
                    logger.info("~Delay " + pixels + " pixels for "  +delay + " Message "  + Message);
                    Thread.sleep(delay);
                }
                catch (Exception e)
                {   e.printStackTrace();}
            }
            else
            {
                DebugHelper.BreakIntoDebug();
            }
        }
    }

}
