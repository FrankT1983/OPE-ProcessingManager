package de.c3e.ProcessManager.BlockRepository;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.Helpers.ExternalProgramHelpers;
import de.c3e.BlockTemplates.Templates.PlaneExternalTool;
import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessingManager.Types.SourceControlInfo;
import de.c3e.ProcessingManager.Types.ToolInputOutputConfiguration;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class ParallelPlaneDeconvolutionTool extends PlaneExternalTool
{
    private final static String JarName = "DeconvolutionLab2custom.jar";

    static final SourceControlInfo info =
            new SourceControlInfo("GIT",
                    "https://git.inf-ra.uni-jena.de/xo46rud/OpeToolBinaries/raw/master/DeconvolutionLab2/",
                    "DeconvolutionLab2/" + JarName,
                    "f4d64df8b3a8af8fc40e6b59b2888114fcc51f3d");

    static final ToolInputOutputConfiguration config = new ToolInputOutputConfiguration().OutputsImage().ReInterpretDataTypes();

    public ParallelPlaneDeconvolutionTool()
    {
        super(info, config, ImageDimension.Y,ImageDimension.X);
    }

    @Override
    public void RunCalculation(List<File> pathToInputs, List<File> pathToOutputs)
    {
        File pathToInput = pathToInputs.get(0);
        File pathToOutput = pathToOutputs.get(0);

        String outputName = pathToOutput.getName();
        if (outputName.toLowerCase().endsWith(".tif"))
        {
            // Deconvolution lab will ad it's own extension
            outputName = outputName.substring(0,outputName.length()-4);
        }

        String command = "";
        command+= "Run -image file "+ pathToInput.getAbsolutePath();
        command+= " -display no -monitor console ";
        if (System.getProperty("os.name").startsWith("Windows"))
        {
            command += " -psf synthetic Double-Helix 3.0 30.0 10.0 size 30 30 10 intensity 255.0";

            // for debug only:
            command= "cmd.exe /c copy ";
            ExternalProgramHelpers.CallProgram(command + " "  +pathToInput.getAbsolutePath() + " " + pathToOutput.getAbsolutePath(),this.workFolder);
            return;
        }else
        {
            command += " -psf file /home/xo46rud/ope/omeroEnv/TestDataConv/PsfForApplication.tif";
        }
        command+= " -algorithm RIF 0.1000";
        command+= " -out mip " + outputName ;
        ExternalProgramHelpers.CallJar(GlobalSettings.WorkFolder + "Tools/" + JarName, command , this.deploymentFolder ,this.workFolder);
    }
}


