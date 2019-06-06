package de.c3e.ProcessManager.BlockRepository;

import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.Utils.AbstractTableObject;
import de.c3e.ProcessManager.Utils.ImageConversion;
import de.c3e.ProcessManager.Utils.LogUtilities;

import icy.imagej.ImageJUtil;
import icy.sequence.Sequence;

import ij.ImagePlus;
import ij.io.FileSaver;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

/**
 * Prototype block to output data.
 */
public class OutputWorkBlock extends LoggingWorkBlockBase
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static final String ImageInput = "Image";
    public static final String PathInput = "destinationFilePath";
    public static final String typeName = "output";

    // inputs : Image, DatasetId

    private Object outputObject;

    private String destinationPath;


    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        if (inputs.containsKey(ImageInput))
        {
            this.outputObject = inputs.get(ImageInput);
        }

        if (inputs.containsKey(PathInput))
        {
            this.destinationPath = GlobalSettings.WorkFolder + inputs.get(PathInput);
        }
    }

    @Override
    public boolean RunWork()
    {
        super.RunWork();

        if (this.outputObject instanceof AbstractImageObject)
        {
            this.outputObject = ImageConversion.ToSequence(((AbstractImageObject) this.outputObject));
        }

        if (this.outputObject instanceof Sequence)
        {
            WriteImageToFile((Sequence)this.outputObject, this.destinationPath);
            logger.info( this.getClass().getSimpleName() + ": finished file " + this.destinationPath);
            return true;
        }

        if (this.outputObject instanceof AbstractTableObject)
        {
            AbstractTableObject table = (AbstractTableObject)this.outputObject;
            table.StoreInCsv(this.destinationPath,true,",");
            logger.info( this.getClass().getSimpleName() + ": finished writing csv file " + this.destinationPath);
            return true;
        }

        logger.error("Noting to do");
        return true;
    }

    static public void WriteImageToFile(AbstractImageObject image, String path)
    {
        logger.info( OutputWorkBlock.class.getSimpleName() + ": Writing image to file " + path);
        Sequence asSequence = ImageConversion.ToSequence(image);
        WriteImageToFile(asSequence,path);
    }

    static private void WriteImageToFile(Sequence outputObject, String path)
    {
        ImagePlus ijImagePlus = ImageJUtil.convertToImageJImage(outputObject,null);
        FileSaver saver = new FileSaver(ijImagePlus);

        String ext = FilenameUtils.getExtension(path).toLowerCase();

        switch (ext)
        {
            case "png":
                saver.saveAsPng(path);
                break;

            case "jpg":
            case"jpeg":
                saver.saveAsJpeg();
                break;

            case "tiff":
            case "tif":
            default:
                saver.saveAsTiff(path);
                break;
        }
    }

    @Override
    public Map<String, Object> GetResults()
    {
        // does not generate outputs
        return new HashMap<>();
    }

    @Override
    public boolean IsFinished()
    {
        return true;
    }
}
