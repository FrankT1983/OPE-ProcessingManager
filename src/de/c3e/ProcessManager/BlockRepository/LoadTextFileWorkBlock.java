package de.c3e.ProcessManager.BlockRepository;

import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.Utils.DebugHelper;
import de.c3e.ProcessManager.Utils.LogUtilities;
import org.slf4j.Logger;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class LoadTextFileWorkBlock extends LoggingWorkBlockBase
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static final String typeName = "loadText";
    public static final String outputName = "LoadedText";
    public static final String inputName = "FilePath";
    public static final String inputImage = "File";        // this can be used, if a image should be passed to this block => e.g. unit tests.

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
        return this.loadedObject != null;
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

        String result;
        try {
            result = new String(Files.readAllBytes(fileToOpen.toPath()));
        }
        catch (Exception ex)
        {
            DebugHelper.PrintException(ex,logger);
            ex.printStackTrace();
            result = null;
        }


        return result;
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
        result.put(outputName, this.loadedObject);

        return result;
    }


    @Override
    public boolean IsFinished()
    {
        return true;
    }
}
