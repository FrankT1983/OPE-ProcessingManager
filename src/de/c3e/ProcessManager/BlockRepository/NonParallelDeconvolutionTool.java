package de.c3e.ProcessManager.BlockRepository;

import de.c3e.BlockTemplates.Templates.Basics.ExternalToolBlockBase;
import de.c3e.BlockTemplates.Templates.Helpers.ExternalProgramHelpers;
import de.c3e.ProcessingManager.Types.SourceControlInfo;
import de.c3e.ProcessingManager.Types.ToolInputOutputConfiguration;

import java.io.File;
import java.util.List;

@SuppressWarnings("unused")
public class NonParallelDeconvolutionTool extends ExternalToolBlockBase
{
    private final static String JarName = "DeconvolutionLab2custom.jar";

    static final SourceControlInfo info =
            new SourceControlInfo("GIT",
                    "https://git.inf-ra.uni-jena.de/xo46rud/OpeToolBinaries/raw/master/DeconvolutionLab2/",
                    "DeconvolutionLab2/" + JarName,
                    "f4d64df8b3a8af8fc40e6b59b2888114fcc51f3d");

    static final ToolInputOutputConfiguration toolConfig = new ToolInputOutputConfiguration().OutputsImage().ReInterpretDataTypes();

    NonParallelDeconvolutionTool()
    {
        super(info,toolConfig);
    }

    @Override
    public void RunCalculation(List<File> pathToInputFile, List<File> pathToOutput)
    {
        if (pathToInputFile.size() != 1 || pathToOutput.size() != 1)
        {return;}

        String outputName = pathToOutput.get(0).getName();
        if (outputName.toLowerCase().endsWith(".tif"))
        {
            // Deconvolution lab will ad it's own extension
            outputName = outputName.substring(0,outputName.length()-4);
        }

        String command = "";
        command+= "Run -image file "+ pathToInputFile.get(0).getAbsolutePath();
        command+= " -psf synthetic Double-Helix 3.0 30.0 10.0 size 30 30 10 intensity 255.0";
        command+= " -algorithm RIF 0.1000";
        command+= " -out mip " + outputName ;
        ExternalProgramHelpers.CallJar(JarName, command,this. deploymentFolder, this.workFolder);
    }
}



