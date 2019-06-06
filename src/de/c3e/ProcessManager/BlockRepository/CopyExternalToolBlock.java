package de.c3e.ProcessManager.BlockRepository;


import de.c3e.BlockTemplates.Templates.Basics.ExternalToolBlockBase;
import de.c3e.BlockTemplates.Templates.Helpers.ExternalProgramHelpers;

import java.io.File;
import java.util.List;

/***
 * Do nothing by copying the data.
 */
public class CopyExternalToolBlock extends ExternalToolBlockBase
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
        }
        else
        {
            command = "cp";
        }
        ExternalProgramHelpers.CallProgram(command + " "  +pathToInput.getAbsolutePath() + " " + pathToOutput.getAbsolutePath(),this.workFolder);
    }
}





