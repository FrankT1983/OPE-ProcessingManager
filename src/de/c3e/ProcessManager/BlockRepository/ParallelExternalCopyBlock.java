package de.c3e.ProcessManager.BlockRepository;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.Helpers.ExternalProgramHelpers;
import de.c3e.BlockTemplates.Templates.PlaneExternalTool;

import java.io.File;
import java.util.List;

public class ParallelExternalCopyBlock extends PlaneExternalTool
{
    ParallelExternalCopyBlock()
    {
        super(ImageDimension.Y,ImageDimension.X);
    }

    @Override
    public void RunCalculation(List<File> pathToInputs, List<File> pathToOutputs)
    {
        File pathToInput = pathToInputs.get(0);
        File pathToOutput = pathToOutputs.get(0);
        String command;
        if (System.getProperty("os.name").startsWith("Windows"))
        {
            command= "cmd.exe /c copy " ;
        }
        else
        {
            command = "cp";
        }
        ExternalProgramHelpers.CallProgram(command + " "  + pathToInput.getAbsolutePath() + " " + pathToOutput.getAbsolutePath(),this.workFolder);
    }
}
