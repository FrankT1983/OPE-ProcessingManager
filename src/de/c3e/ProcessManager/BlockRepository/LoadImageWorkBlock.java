package de.c3e.ProcessManager.BlockRepository;

import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.Utils.DebugHelper;
import de.c3e.ProcessManager.Utils.LogUtilities;

import icy.imagej.ImageJUtil;
import icy.sequence.Sequence;
import loci.common.DebugTools;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.ImporterOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ij.ImagePlus;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

/**
 * Prototype of a work block to load an image.
 * This will mean
 * - opening an image with bio formats
 * - reading pixel data
 * - potentially reading meta data
 */
public class LoadImageWorkBlock extends LoggingWorkBlockBase
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static final String typeName = "loadImage";
    public static final String outputName = "LoadedImage";
    public static final String inputName = "ImagePath";
    public static final String inputImage = "Image";        // this can be used, if a image should be passed to this block => e.g. unit tests.

    /// Inputs: ImageId
    /// Outputs : Image

    private String filePath;

    private Object loadedObject = null;

    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        if (inputs.containsKey(inputName))
        {
            this.filePath = (String)inputs.get(inputName);
        }

        if (inputs.containsKey(inputImage))
        {
            this.loadedObject = inputs.get(inputImage);
        }
    }

    @Override
    public boolean RunWork()
    {
        super.RunWork();
        if (this.loadedObject != null)
        {
            logger.info("Did already had something loaded");
            return true;
        }

        this.loadedObject = LoadFromPath(this.filePath);
        boolean returnValue =this.loadedObject != null;
        logger.error ("Return value: " + returnValue);
        return returnValue;
    }

    private static Object LoadFromPath(String path)
    {
        if (path == null)
        {
            logger.error("File path empty");
            return null;
        }

        File fileToOpen = new File(path);
        if (!fileToOpen.exists() || !fileToOpen.isFile() )
        {
            fileToOpen = new File(GlobalSettings.WorkFolder + path);
            if (!fileToOpen.exists() || !fileToOpen.isFile() )
            {
                logger.error(LoadImageWorkBlock.class.getSimpleName() + ": File not found: " + path + " (absolute Path: " +fileToOpen.getAbsolutePath() + ")");
                return null;
            }
        }
        path = fileToOpen.getAbsolutePath();
        logger.info( LoadImageWorkBlock.class.getSimpleName() + ": Opening file " + path);

        Object result = null;
        try
        {
            DebugTools.enableLogging("Error");
            final ImporterOptions options = new ImporterOptions();
            options.setId(path);
            final ImportProcess process = new ImportProcess(options);
            if (!process.execute()) throw new IllegalStateException("Process failed");
            final ImagePlusReader reader = new ImagePlusReader(process);
            result = reader.openImagePlus();
            logger.info( LoadImageWorkBlock.class.getClass().getSimpleName() + ": Finished opening file " + path);
        }
        catch (Exception e)
        {
            DebugHelper.PrintException(e,logger);
            e.printStackTrace();
        }
        return result;
    }

    public static AbstractImageObject LoadAbstractImageFromPath(String path)
    {
        Object temp = LoadFromPath(path);
        return ToAbstractImage(temp);
    }

    @Override
    /**
     * Return the loaded object as a result.
     * This considers way more class options than the actual load code will produce since this block can
     * also be used to inject a object via an input parameter.
     */
    public Map<String, Object> GetResults()
    {
        Map<String, Object> result = new HashMap<>();

        AbstractImageObject res = ToAbstractImage(this.loadedObject);
        if (res !=null && res.getLocalPartsSubBlocks()!= null && res.getLocalPartsSubBlocks().size() > 0)
        {
            logger.info("Opened Image file. Data Type was : " + res.getLocalPartsSubBlocks().get(0).type);
        }
        if (res != null)
        {
            this.loadedObject = res;
            result.put(outputName, this.loadedObject);
        }

        return result;
    }

    static private AbstractImageObject ToAbstractImage(Object in)
    {
        Object tmp= in;
        if (tmp != null)
        {
            if (tmp instanceof ImagePlus[])
            {
                tmp = ImageJUtil.convertToIcySequence(((ImagePlus[])tmp)[0], null);
            }

            if (tmp instanceof Sequence)
            {
                tmp = AbstractImageObject.fromSequence((Sequence)tmp);

            }

            if (tmp instanceof AbstractImageObject)
            {
                ((AbstractImageObject) tmp).CompactLocalBlocks(false);
                return (AbstractImageObject)tmp;
            }
        }
        return null;
    }

    @Override
    public boolean IsFinished()
    {
        return true;
    }
}

