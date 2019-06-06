package de.c3e.ProcessManager.BlockRepository;


import de.c3e.BlockTemplates.Templates.Basics.ExternalToolBlockBase;
import de.c3e.BlockTemplates.Templates.Helpers.ExternalProgramHelpers;
import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessingManager.Types.SourceControlInfo;
import de.c3e.ProcessingManager.Types.ToolInputOutputConfiguration;

import java.io.File;
import java.util.List;

public class SeabornDiagonalPlotTool extends ExternalToolBlockBase
{
    private final static String ScriptName = "stanResultToDiagPlot.py";

//    static final SourceControlInfo info =
//            new SourceControlInfo("GIT",
//                    "https://git.inf-ra.uni-jena.de/xo46rud/OpeToolBinaries/raw/master/DeconvolutionLab2/",
//                    "DeconvolutionLab2/" + JarName,
//                    "f4d64df8b3a8af8fc40e6b59b2888114fcc51f3d");

    static final SourceControlInfo info = null;
    static final ToolInputOutputConfiguration toolConfig = new ToolInputOutputConfiguration().OutputsImage().InputIsTable().ReInterpretDataTypes();

    SeabornDiagonalPlotTool()
    {
        super(info,toolConfig);
    }

    @Override
    public void RunCalculation(List<File> pathToInputFile, List<File> pathToOutput)
    {
        if (pathToInputFile.size() != 1 || pathToOutput.size() != 1)
        {return;}

        String command;
        if (System.getProperty("os.name").startsWith("Windows"))
        {
            command= "cmd.exe /c copy \"C:\\PHD\\UnitTest\\stanOutput.csv\" " +   pathToOutput.get(0).getAbsolutePath();
            ExternalProgramHelpers.CallProgram(command,this.workFolder);
            return;
        }
        else
        {
            command = "python ";
            command+= GlobalSettings.WorkFolder + "Tools/";
            command+= ScriptName;
            command+= " " + pathToInputFile.get(0).getAbsolutePath();
            command+= " " + pathToOutput.get(0).getAbsolutePath();
            ExternalProgramHelpers.CallProgram(command,this.workFolder);
        }
    }
}
