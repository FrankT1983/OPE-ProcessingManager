package de.c3e.ProcessManager.BlockRepository;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.Basics.ExternalToolBlockBase;
import de.c3e.BlockTemplates.Templates.Helpers.ExternalProgramHelpers;
import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessingManager.Types.TableMergePostprocessing;
import de.c3e.ProcessingManager.Types.ToolInputOutputConfiguration;
import de.c3e.ProcessingManager.Types.ToolInputOutputFileType;

import java.io.File;
import java.util.List;

public class PlotCellCount extends ExternalToolBlockBase
{
    @Override
    public void RunCalculation(List<File> pathToInputs, List<File> pathToOutputs)
    {
        File pathToInput = pathToInputs.get(0);
        File pathToOutput = pathToOutputs.get(0);
        String command;
        if (System.getProperty("os.name").startsWith("Windows"))
        {
            command= "cmd.exe /c copy " ;
            String debugCommand = "cmd.exe /c copy C:\\PlayGround\\Git\\\\PythonPlayGround\\foo.png " + pathToOutput;
            ExternalProgramHelpers.CallProgram(debugCommand,this.workFolder);
            return;
        }
        else
        {
            command = GlobalSettings.WorkFolder + "Tools/CellCount.py";
            //command = this.deploymentFolder +  "CellCount.py";
        }
        ExternalProgramHelpers.CallProgram(command + " "  + pathToInput.getAbsolutePath() + " " + pathToOutput.getAbsolutePath(), this.workFolder);
    }
}
