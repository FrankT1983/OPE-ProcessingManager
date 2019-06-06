package de.c3e.ProcessManager.BlockRepository;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.CubeExternalTool;
import de.c3e.BlockTemplates.Templates.Helpers.ExternalProgramHelpers;
import de.c3e.ProcessManager.Utils.LogUtilities;
import de.c3e.ProcessingManager.Types.TableMergePostprocessing;
import de.c3e.ProcessingManager.Types.ToolInputOutputConfiguration;
import de.c3e.ProcessingManager.Types.ToolInputOutputFileType;

import org.slf4j.Logger;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;

@SuppressWarnings({"unused"})
public class CellProfilerTool extends CubeExternalTool
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public CellProfilerTool()
    {
        super(null,
                ToolInputOutputConfiguration.UseFileFromOutputFolder("Cells.csv", ToolInputOutputFileType.CSV).UseInputFolder(),
                TableMergePostprocessing.CreatePositionColumn(),
                ImageDimension.Y,ImageDimension.X,ImageDimension.C);
    }

    @Override
    public void RunCalculation(List<File> pathToInputs, List<File> pathToOutputs)
    {
        File pathToInput = pathToInputs.get(0);
        File pathToOutput = pathToOutputs.get(0);
        //  .\CellProfiler.exe -c -r  -i C:\PHD\UnitTest\CellProfiler\input -o C:\PHD\UnitTest\CellProfiler\output -p ./ExampleHumanForComposit.cppipe
        String command;
        if (System.getProperty("os.name").startsWith("Windows"))
        {
            command= "C:\\PHD\\UnitTest\\CellProfiler\\Cellprofiler.exe" ;

                    String debugCommand = "cmd.exe /c copy C:\\PHD\\UnitTest\\CellProfiler\\output " + pathToOutput;
                    ExternalProgramHelpers.CallProgram(debugCommand,this.workFolder);
                    return;
            //command += " -c -r  -i " + pathToInput.getAbsolutePath() + " -o " + pathToOutput.getAbsolutePath()  + " -p C:\\PHD\\UnitTest\\CellProfiler\\ExampleHumanForComposit.cppipe";
        }
        else
        {
            command = "cellprofiler -c -r -i " + pathToInput.getAbsolutePath() + " -o " + pathToOutput.getAbsolutePath()  + " -p /home/xo46rud/deployment/Tools/ExampleHumanForComposit.cppipe";
        }

        ExternalProgramHelpers.CallProgram(command,this.workFolder);
    }
}
