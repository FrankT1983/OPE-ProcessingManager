package de.c3e.ProcessManager.BlockRepository;

import com.google.common.base.Joiner;
import com.jgoodies.common.base.Strings;
import de.c3e.BlockTemplates.Examples.*;
import de.c3e.BlockTemplates.Templates.*;
import de.c3e.BlockTemplates.Templates.Basics.ExternalToolBlockBase;
import de.c3e.BlockTemplates.Templates.Basics.ParallelCalculatorBase;
import de.c3e.BlockTemplates.Templates.Basics.ParallelProjectionBase;
import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.Utils.ClassInJar;
import de.c3e.ProcessManager.Utils.LogUtilities;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import icy.util.ClassUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Repository for loading plugins based on templates
 */
class PluginBlockRepository
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private final static List<ClassInJar> knownClasses = new ArrayList<>();
    private final static List<Class> knownLoadedClasses = new ArrayList<>();

    static private String pluginPath = "Libs/pluginBlocks/";
    static private String pluginPath2 = "PluginDeploy/";

    static List<String> getPluginPaths()
    {
        return  Arrays.asList( GlobalSettings.WorkFolder + pluginPath , GlobalSettings.WorkFolder + pluginPath2 );
/*
        if (StringUtils.isNotBlank(GlobalSettings.WorkFolder))
        {
            return GlobalSettings.WorkFolder + pluginPath;
        }

        return pluginPath;*/
    }

    static IWorkBlock TryGetPluginBlock(String type)
    {
        if (type.equals(AddXBlock.class.getSimpleName()) || type.equals(AddXBlock.class.getName()))
        {
            return PointCalculatorImpl.CreateFromPlugin(new AddXBlock());
        }
        else
        {
            switch (type)
            {
                case "AddXYBlock":
                {   return PointCalculatorImpl.CreateFromPlugin(new AddXYBlock());  }

                case "de.c3e.BlockTemplates.Examples.SetChannelToAverage":
                {   return ChannelCalculatorImpl.CreateFromPlugin(new SetChannelToAverage());   }

                case "HistogramLinearization":
                {   return NonSizeChangingPluginImpl.CreateFromPlugin(new HistogramLinearization());   }

                case "CyanConversion":
                {   return NonSizeChangingPluginImpl.CreateFromPlugin(new CyanConversion());   }


                case "DelayedAddXYBlock":
                {   return PointCalculatorImpl.CreateFromPlugin(new DelayedAddXYBlock());   }

                case "de.c3e.BlockTemplates.Examples.CopyExternalToolBlock":
                {   return ExternalToolBlockImpl.CreateFromPlugin(new CopyExternalToolBlock()); }

                case "de.c3e.BlockTemplates.Examples.ParallelExternalCopyBlock":
                {   return ExternalToolBlockImpl.CreateFromPlugin(new ParallelExternalCopyBlock()); }

                case  "DeconvolutionTool" :
                case  "de.c3e.BlockTemplates.Examples.ParallelPlaneDeconvolutionTool" :
                {   return ExternalToolBlockImpl.CreateFromPlugin(new ParallelPlaneDeconvolutionTool());    }

                case "PyStanTool":
                {   return ExternalToolBlockImpl.CreateFromPlugin(new PyStanTool());    }

                case "SeabornDiagonalPlot":
                {   return ExternalToolBlockImpl.CreateFromPlugin(new SeabornDiagonalPlotTool());    }

                case "CellProfilerTool":
                case "de.c3e.BlockTemplates.Examples.CellProfilerTool":
                case "de.c3e.ProcessManager.BlockRepository.CellProfilerTool":
                {   return ExternalToolBlockImpl.CreateFromPlugin(new CellProfilerTool());    }

                case "PlotCellCount":
                {   return ExternalToolBlockImpl.CreateFromPlugin(new PlotCellCount()); }

                case "TimePointSelection":
                {   return ProjectionPluginImpl.CreateFromPlugin(new TimePointSelectionPlane());    }

                case "TimePointSelectionOld":
                {return ProjectionPluginImpl.CreateFromPlugin(new TimePointSelection());    }
                
                default:
                    // look up loaded classes
                    {
                        ClassLoader myCL = Thread.currentThread().getContextClassLoader();
                        try
                        {
                            Class loadedClass = myCL.loadClass(type);
                            synchronized (knownLoadedClasses)
                            {
                                knownLoadedClasses.add(loadedClass);
                                IWorkBlock pointCalc = getWorkBlockFromClass(loadedClass);
                                if (pointCalc != null) return pointCalc;
                            }
                        }
                        catch (Exception e)
                        {
                            // not found
                        }
                    }


                    synchronized (knownClasses)
                    {
                        if (knownClasses.size() == 0)
                        {
                            ReloadPluginList();
                        }

                        for (ClassInJar c : knownClasses)
                        {
                            if (c.Class.getSimpleName().equals(type) || c.Class.getName().equals(type))
                            {
                                IWorkBlock pointCalc = getWorkBlockFromClass(c.Class);
                                if (pointCalc != null) return pointCalc;
                            }
                        }

                        logger.info("Not found in Plugin Repository " +  type);
                    }
            }
        }

        return  null;
    }

    private static IWorkBlock getWorkBlockFromClass(Class cl)
    {
        if(PointCalculator.class.isAssignableFrom(cl))
        {
            try
            {
                // instantiation of plugins from 1st version
                PointCalculatorImpl pointCalc = PointCalculatorImpl.CreateFromPlugin((PointCalculator) cl.getConstructor().newInstance());
                return pointCalc;
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if( ParallelCalculatorBase.class.isAssignableFrom(cl))
        {
            try
            {
                // instantiation of plugins from 2nd version => with one class for all
                // non size changing stuff
                NonSizeChangingPluginImpl pointCalc = NonSizeChangingPluginImpl.CreateFromPlugin((ParallelCalculatorBase) cl.getConstructor().newInstance());
                return pointCalc;
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if( ParallelProjectionBase.class.isAssignableFrom(cl))
        {
            try
            {
                // instantiation of plugins from 2nd version => with one class for all
                // non size changing stuff
                ProjectionPluginImpl pointCalc = ProjectionPluginImpl.CreateFromPlugin((ParallelProjectionBase) cl.getConstructor().newInstance());
                return pointCalc;
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        if( ExternalToolBlockBase.class.isAssignableFrom(cl))
        {
            try
            {
                // instantiation of plugins from 2nd version => with one class for all
                // non size changing stuff
                ExternalToolBlockImpl pointCalc = ExternalToolBlockImpl.CreateFromPlugin((ExternalToolBlockBase) cl.getConstructor().newInstance());

                if (Strings.isNotBlank(GlobalSettings.WorkFolder))
                {   pointCalc.setDeploymentFolder(GlobalSettings.WorkFolder + "/Tools/");}
                return pointCalc;
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static void ReloadPluginList()
    {
        List<String> paths = getPluginPaths();
        List<ClassInJar> allClasses = new ArrayList<>();
        for (String path : paths)
        {
            allClasses.addAll(FindClassesInPath(path));
        }
        List<ClassInJar> pluginClasses =  new ArrayList<>();

        StringBuilder found = new StringBuilder();
        for (ClassInJar c : allClasses)
        {
            if (IWorkBlock.class.isAssignableFrom(c.Class))
            {
                pluginClasses.add(c);
                found.append(c.ClassName).append("\n");
                continue;
            }


            if (PointCalculator.class.isAssignableFrom(c.Class))
            {
                pluginClasses.add(c);
                found.append(c.ClassName).append("\n");
                continue;
            }

            if (ParallelCalculatorBase.class.isAssignableFrom(c.Class))
            {
                pluginClasses.add(c);
                found.append(c.ClassName).append("\n");
                continue;
            }


            if (ExternalToolBlockBase.class.isAssignableFrom(c.Class))
            {
                pluginClasses.add(c);
                found.append(c.ClassName).append("\n");
                continue;
            }
        }

        String pathsString = Joiner.on(" + ").join(paths);
        logger.info("Reloaded Plugin List from paths: " + pathsString + "  found:" + allClasses.size() + " classes \n" + found);

        knownClasses.clear();
        knownClasses.addAll(pluginClasses);
    }

    private static List<ClassInJar> FindClassesInPath(String path)
    {
        List<ClassInJar> result = new ArrayList<>();

        final File dir = new File(path);

        if (dir.isDirectory())
        {
            for (File file : dir.listFiles())
            {
                result.addAll(FindClassesInPath(file.getPath()));
            }
        }
        else
        {
            if (FilenameUtils.getExtension(dir.getName()).equalsIgnoreCase("jar"))
            {
                logger.info("Found jar file : " +  dir.getAbsolutePath());
                try
                {
                    URL workPluginUrl = dir.toURI().toURL();
                    URL[] classUrls = {workPluginUrl};
                    URLClassLoader ucl = new URLClassLoader(classUrls , PluginBlockRepository.class.getClassLoader());

                    Set<String> classes = ClassUtil.findClassNamesInJAR(dir.getAbsolutePath());
                    for (String className : classes)
                    {
                        Class loadedClass = ucl.loadClass(className);
                        ClassInJar newEntry = new ClassInJar();
                        newEntry.ClassName = className;
                        newEntry.JarFile = dir;
                        newEntry.Class = loadedClass;
                        result.add(newEntry);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }
}
