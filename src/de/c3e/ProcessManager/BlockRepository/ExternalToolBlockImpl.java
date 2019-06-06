package de.c3e.ProcessManager.BlockRepository;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.Basics.ExternalToolBlockBase;
import de.c3e.BlockTemplates.Templates.BlockTemplatesBase;
import de.c3e.BlockTemplates.Templates.Interfaces.IHasInputParameters;
import de.c3e.BlockTemplates.Templates.Interfaces.IRequiresMerging;
import de.c3e.BlockTemplates.Templates.Interfaces.ISupportsSplitting;
import de.c3e.ProcessManager.Utils.*;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import de.c3e.ProcessingManager.Types.SplitType;
import de.c3e.ProcessingManager.Types.SplitTypes;
import de.c3e.ProcessingManager.Types.ToolInputOutputConfiguration;
import de.c3e.ProcessingManager.Types.ToolInputOutputFileType;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ExternalToolBlockImpl extends BlockTemplatesBase implements IWorkBlock, ISupportsSplitting
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private ExternalToolBlockBase implementation;
    private boolean isFinished;

    static ExternalToolBlockImpl CreateFromPlugin(ExternalToolBlockBase input)
    {
        ExternalToolBlockImpl imp = new ExternalToolBlockImpl();
        imp.SetImplementation(input);
        return imp;
    }

    @Override
    public SplitType getSplitType()
    {
        if (this.implementation == null)
        {
            DebugHelper.BreakIntoDebug();
            return SplitType.cantSplit;
        }

        return this.implementation.getSplitType();
    }

    private void SetImplementation(ExternalToolBlockBase implementation)
    {
        this.implementation = implementation;
    }

    private Object inputObject;
    private Object outputObject;

    private static final String ImageInput = "Input";
    private static final String ImageOutput = "Output";

    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        if (inputs.containsKey(ImageInput))
        {
            this.inputObject = inputs.get(ImageInput);
        }

        setInputsForAttributes(this,this.GetInputsFromAttributes(this.implementation), inputs);
    }

    @Override
    public boolean RunWork()
    {
        if( implementation.getInputOutputConfig() != null)
        {
            if ((implementation.getInputOutputConfig().getInputFileType() == ToolInputOutputFileType.CSV) ||
                    (implementation.getInputOutputConfig().getInputFileType() == ToolInputOutputFileType.Json)) {
                List<String> inputStrings = new ArrayList<>();
                inputStrings.add((String) this.inputObject);
                for (Object o : this.Inputs.values()) {
                    inputStrings.add(o.toString());
                }
                //todo: handle this with a dictionary


                this.outputObject = this.RunExternallyForTextFiles(inputStrings);
                return this.outputObject != null;

            }
        }

        AbstractImageObject inputImage = null;
        logger.info("Input : " + this.inputObject.toString());
        if (this.inputObject instanceof  AbstractImageObject)
        {
            logger.info("Input is Image");
            inputImage = (AbstractImageObject)this.inputObject;
        }
        // boolean runOk = true;
        if (inputImage == null)
        {
            if (this.inputObject instanceof AbstractTableObject)
            {
                logger.info("Input is Table");
                this.outputObject = RunExternally(this.inputObject);

                if (this.outputObject == null)  { return  false;}
            }
            else
            {
                DebugHelper.BreakIntoDebug();
                return false;
            }
        }else if (inputImage.isComplete())
        {
            this.outputObject = RunExternally(inputImage);
        }
        else
        {
            List<ImageSubBlock> partialResults = new ArrayList<>();
            List<AbstractTableObject> partialCsvResults = new ArrayList<>();

            ToolInputOutputFileType outputType = ToolInputOutputFileType.Image;
            {
                ToolInputOutputConfiguration conf = this.implementation.getInputOutputConfig();
                if (conf != null)   {   outputType = conf.getOutputFileType();  }
            }

            /// todo: implement force split parameter
            List<ImageSubSet> sliceBounds = new ArrayList<>();
            for (ImageSubBlock i : inputImage.getLocalPartsSubBlocks())
            {
                if (this.implementation.getSplitType().type == SplitTypes.useDependencies)
                {
                    sliceBounds.addAll(SubSetFinder.SliceAlongDimensions(i.dimensions, this.implementation.getSplitType().dependencies));
                }
                else
                {
                    sliceBounds.add(i.dimensions);
                }
            }

            for(ImageSubSet s: sliceBounds)
            //for(ImageSubBlock b: inputImage.getLocalPartsSubBlocks())
            {
                ImageSubBlock b = ImageSubBlockUtils.ConstructSubBlockFromParts(s,inputImage.getLocalPartsSubBlocks());
                if (b== null)
                {
                    DebugHelper.BreakIntoDebug();

                    List<ImageSubSet> sliceBounds2 = new ArrayList<>();
                    for (ImageSubBlock i : inputImage.getLocalPartsSubBlocks())
                    {
                        if (this.implementation.getSplitType().type == SplitTypes.useDependencies)
                        {
                            sliceBounds2.addAll(SubSetFinder.SliceAlongDimensions(i.dimensions, this.implementation.getSplitType().dependencies));
                        }
                        else
                        {
                            sliceBounds2.add(i.dimensions);
                        }
                    }
                    continue;
                }

                ImageSubSet original = new ImageSubSet(b.dimensions);
                b.dimensions.setStart(0, ImageDimension.AllValues);

                AbstractImageObject tmp = AbstractImageObject.fromSubBlock(b);
                Object tmpRes = RunExternally(tmp);

                if (tmpRes == null)
                {
                    DebugHelper.BreakIntoDebug();
                    continue;
                }

                b.dimensions = original;
                // todo: check to do this proper, but deconvolution changes size by 1 ... accommodate for that
                if (this.implementation.getSplitType().type == SplitTypes.useDependencies)
                {
                    if (tmpRes instanceof AbstractImageObject)
                    {
                        for (ImageDimension d :this.implementation.getSplitType().dependencies )
                        {                        b.dimensions.setSize(((AbstractImageObject)tmpRes).getFullBounds().getSize(d),d);}
                    }
                }

                if (tmpRes instanceof AbstractImageObject)
                {
                    if (outputType != ToolInputOutputFileType.Image)
                    {   DebugHelper.BreakIntoDebug();   }

                    AbstractImageObject tmpImg = (AbstractImageObject) tmpRes;
                    if (!SubSetFinder.SameSize(tmpImg.getFullBounds(), original))
                    {
                        DebugHelper.BreakIntoDebug();
                    }

                    if (!tmpImg.isComplete())
                    {
                        DebugHelper.BreakIntoDebug();
                    }

                    if (tmpImg.getLocalPartsSubBlocks().size() != 1)
                    {
                        DebugHelper.BreakIntoDebug();
                    }

                    ImageSubBlock res = tmpImg.getLocalPartsSubBlocks().get(0);
                    res.dimensions = new ImageSubSet(original);
                    partialResults.add(res);
                } else if ( tmpRes instanceof AbstractTableObject)
                {
                    AbstractTableObject tmpTable =(AbstractTableObject)tmpRes;
                    logger.info("Result is table with " + tmpTable.getRowCount() + " rows. Requires Merging: " + (this.implementation instanceof IRequiresMerging) + " " + s.toString());
                    if (outputType != ToolInputOutputFileType.CSV)
                    {   DebugHelper.BreakIntoDebug();   }

                    if (this.implementation instanceof IRequiresMerging)
                    {
                        MergePostProcessing postProcessing = MergeModifierImplementationFactory.getImplementation(((IRequiresMerging) this.implementation).getMergePostProcessing());
                        postProcessing.DoModifications(tmpRes,b.dimensions, ImageDimension.others(Arrays.asList(this.implementation.getSplitType().dependencies)));
                    }

                    partialCsvResults.add(tmpTable);
                }
            }

            switch (outputType)
            {
                case Image:
                    {
                        AbstractImageObject mergedResult = AbstractImageObject.fromSubBlocksAndTemplate(partialResults, inputImage);
                        if (this.implementation.getSplitType().type == SplitTypes.useDependencies)
                        {
                            for (ImageDimension d :this.implementation.getSplitType().dependencies )
                            {
                                int size = mergedResult.AllSubblocksHaveSameSize(d);

                                if (size == -1)
                                {
                                    // todo: this could be a potential bug: first check all , than update
                                    break;
                                }

                                if (size != mergedResult.getFullBounds().getSize(d))
                                {
                                    mergedResult.setFullSize(size,d);

                                }
                            }
                        }
                        mergedResult.getLocalBounds();
                        this.outputObject = mergedResult;
                    }
                    break;

                case CSV : this.outputObject = AbstractTableObject.fromSubTables(partialCsvResults); break;
            }
        }

        this.isFinished = true;
        return this.outputObject != null;
    }

    private static File CreateTempDir(String path)
    {
        File tmp= new File(path);
        try
        {
            if (tmp.mkdirs())
            {
                tmp.deleteOnExit();
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            DebugHelper.BreakIntoDebug();
        }
        return tmp;
    }

    private Object RunExternally(Object inputObject)
    {
        Object result = null;
        try
        {
            Path temp = Files.createTempDirectory("OpeTool") ;
            File inputDir = CreateTempDir(temp + "/input/");
            File outputDir = CreateTempDir(temp + "/output/");

            temp.toFile().deleteOnExit();
            inputDir.deleteOnExit();
            outputDir.deleteOnExit();

            boolean useOutputFile = true;
            boolean useInputFile = true;

            ToolInputOutputConfiguration conf = this.implementation.getInputOutputConfig();
            if (conf != null)
            {
                useOutputFile = !conf.isOutputIntoFolder();
                useInputFile = !conf.isInputFromFolder();
            }

            File inputFile = new File(temp + "/input/input.tiff");
            File inputPath = useInputFile ? inputFile : new File(temp + "/input/");
            File output = new File( temp + "/" + (useOutputFile? "output.tif" : "output/"));
            this.setWorkFolder(temp.toAbsolutePath().toString());

            //// Write input data to file for external tool
            try
            {
                if (inputObject instanceof AbstractImageObject)
                {
                    OutputWorkBlock.WriteImageToFile((AbstractImageObject)inputObject, inputFile.getAbsolutePath());
                } else
                if (inputObject instanceof  AbstractTableObject)
                {
                    inputFile = new File(temp + "/input/input.csv");
                    ((AbstractTableObject) inputObject).StoreInCsv(inputFile.getAbsolutePath(),true,",");
                }

                inputFile.deleteOnExit();
            }
            catch (Exception ex)
            {
                logger.error("Could not write data. " + ex);
                ex.printStackTrace();
                DebugHelper.BreakIntoDebug();
            }

            try
            {
                List<File> inputsArray = new ArrayList<>() ;
                inputsArray.add(inputPath);

                List<File> outputsArray = new ArrayList<>() ;
                outputsArray.add(output);

                this.implementation.RunCalculation(inputsArray, outputsArray);
            }
            catch (Exception ex)
            {
                logger.error("Could execute external tool. " + ex);
                ex.printStackTrace();
                DebugHelper.BreakIntoDebug();
            }

            try
            {
                result = null;
                if (conf != null)
                {
                    if (!useOutputFile)
                    {
                        output = new File(output.getAbsolutePath() + "/" + conf.getOutputFileName());
                        output.deleteOnExit();
                    }

                    if (conf.getOutputFileType() != null)
                    {
                        result = LoadResultFile(conf,output);
                    }
                }

                if (result == null)
                {
                    result = LoadImageWorkBlock.LoadAbstractImageFromPath(output.getAbsolutePath());
                }
            }
            catch (Exception ex)
            {
                logger.error("Could not import result of external tool. " + ex);
                DebugHelper.PrintException(ex,logger);
                DebugHelper.BreakIntoDebug();
                return null;
            }

            if (!System.getProperty("os.name").startsWith("Windows"))
            {
                // somehow does not work under windows in unit tests
                FileUtils.deleteDirectory(temp.toFile());
            }

        }
        catch (FileSystemException ex)
        {
            logger.error("Could not import result of external tool. File system Exception" + ex);
            DebugHelper.PrintException(ex,logger);
            DebugHelper.PrintSysInfo(logger);
            DebugHelper.BreakIntoDebug();
            return null;

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            DebugHelper.PrintException(ex,logger);
            //DebugHelper.BreakIntoDebug();
        }
        return result;
    }

    private Object LoadResultFile(ToolInputOutputConfiguration conf, File output)
    {
        Object result = null;
        switch (conf.getOutputFileType())
        {
            case CSV:
                result = new AbstractTableObject(output.getAbsolutePath());
                logger.info("Read external Result: " + ((AbstractTableObject) result).getRowCount() + " lines.");
                return  result;

            case Json:
                try {
                    result = new String(Files.readAllBytes(output.toPath()));
                    logger.info("Read external Result: " + ((String) result).length() + " characters.");
                }catch (Exception ex)
                {
                    DebugHelper.PrintException(ex,logger);
                    return null;
                }
                return  result;

            case Image:
                result = LoadImageWorkBlock.LoadAbstractImageFromPath(output.getAbsolutePath());

                if (result == null)
                {
                    logger.error("Could not read result file " + output.getAbsolutePath());
                    return null;
                }

                if (conf.isReinterpretDataTypes() && (inputObject instanceof AbstractImageObject))
                {
                    ((AbstractImageObject) result).ReinterpretLocalBlocksToType((AbstractImageObject) inputObject);
                }
                break;
        }
        return  result;
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

    public void setDeploymentFolder(String path)
    {
        this.implementation.setDeploymentFolder(path);
    }

    private void setWorkFolder(String path)
    {
        this.implementation.setWorkFolder(path);
    }

    protected Object RunExternallyForTextFiles(List<String> inputs)
    {
        List<File> inputsAsTextFiles = new ArrayList<>();

        try {
            Path temp = Files.createTempDirectory("OpeTool");
            File inputDir = CreateTempDir(temp + "/input/");
            File outputDir = CreateTempDir(temp + "/output/");
            File output;
            switch (this.implementation.getInputOutputConfig().getOutputFileType())
            {
                case CSV:
                case Json:
                    output = new File(temp + "/" + "output.txt" );
                    break;
                default:
                    DebugHelper.BreakIntoDebug();
                    return false;
            }

            this.setWorkFolder(temp.toAbsolutePath().toString());
            temp.toFile().deleteOnExit();
            inputDir.deleteOnExit();
            outputDir.deleteOnExit();

            int i = 0;
            for (String s : inputs) {
                File inputFile = new File(temp + "/input/input" + i++ + ".txt");
                inputFile.deleteOnExit();
                FileUtils.writeStringToFile(inputFile, s);
                inputsAsTextFiles.add(inputFile);
            }

            List<File> outputFiles = new ArrayList<>();
            outputFiles.add(output);
            this.implementation.RunCalculation(inputsAsTextFiles, outputFiles);

            return  LoadResultFile(this.implementation.getInputOutputConfig(),output);
        }
        catch (Exception ex)
        {
            DebugHelper.PrintException(ex,logger);
            ex.printStackTrace();
            return null;
        }
    }
}
