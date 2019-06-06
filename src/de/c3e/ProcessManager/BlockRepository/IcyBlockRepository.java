package de.c3e.ProcessManager.BlockRepository;

import de.c3e.ProcessManager.DataTypes.BlockIO;
import de.c3e.ProcessManager.DataTypes.GraphBlock;
import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.Utils.LogUtilities;
import de.c3e.ProcessingManager.Interfaces.IWorkBlock;
import icy.plugin.PluginDescriptor;
import icy.plugin.classloader.JarClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import icy.plugin.PluginLoader;
import plugins.adufour.blocks.lang.Block;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Repository responsible for finding icy plugins to do work.
 */
public class IcyBlockRepository
{
    private final static String CLASS_SUFFIX = ".class";

    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    static IWorkBlock TryGetIcyBlock(String type)
    {
        icy.preferences.ApplicationPreferences.load();
        icy.preferences.ApplicationPreferences.setOs("unix");
        icy.preferences.GeneralPreferences.load();

        try
        {
            IWorkBlock result = TryGetFromJarFile(type);
            if (result != null)
            {
                logger.info("found in jar file");
                return  result;
            }

            result = TryLoadingWithPluginLoader(type);
            if (result == null)
            {
                logger.error("Could not load using plugin loader");
            }
            return result;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logger.error("Exception when trying to get block\n " +e.toString());
            return null;

        }
    }

    private static IWorkBlock TryLoadingWithPluginLoader(String type) throws NoSuchMethodException, InstantiationException, IllegalAccessException, java.lang.reflect.InvocationTargetException
    {
        for (final PluginDescriptor pluginDescriptor : PluginLoader.getPlugins())
        {
            if (pluginDescriptor.getClassName().equals(type))
            {
                Constructor construct =  pluginDescriptor.getPluginClass().getConstructor();
                Object myObject = construct.newInstance();

                if (myObject instanceof Block)
                {
                    return new IcyBlockWrapper(myObject);
                }
            }
        }
        return null;
    }

    private static IWorkBlock TryGetFromJarFile(String type)
    {
        try
        {
            String icypath = GlobalSettings.IcyFolder + "Libs/Icy/";
            String path = type.replace(".", "/");
            path = icypath + path + ".jar";

            File jarFile = new File(path);
            if (!jarFile.exists()|| !jarFile.isFile() )
            {return null;}

            JarFile file = new JarFile(path);
            Enumeration allEntries = file.entries();
            List<String> classes = new ArrayList<>();
            while (allEntries.hasMoreElements())
            {
                JarEntry entry = (JarEntry) allEntries.nextElement();
                String name = entry.getName();
                if (name.endsWith(CLASS_SUFFIX))
                {
                    int end = name.indexOf(CLASS_SUFFIX);
                    name = name.substring(0, end);
                    name = name.replace("/", ".");
                    if (name.equalsIgnoreCase(type))
                    {
                        classes.add(name);
                    }
                }
            }

            if (classes.size() < 1)
            {
                logger.error("no class found with name");
                return null;
            }

            // list of dependencies => some are already in the dependencies of the project => cleanup later
            URL blocksJar = new URL("file:///" + icypath + "plugins/adufour/blocks/Blocks.jar");
            URL EzPluginJar = new URL("file:///" + icypath + "plugins/adufour/ezplug/EzPlug.jar");
            URL icqJar = new URL("file:///" + icypath + "icy.jar");
            URL resourceJar = new URL("file:///" + icypath + "resources.jar");

            CheckIfExists(blocksJar);
            CheckIfExists(EzPluginJar);
            CheckIfExists(icqJar);
            CheckIfExists(resourceJar);

            URL workPluginUrl = new URL("file:///" + path);
            URL[] classUrls = {icqJar, resourceJar, EzPluginJar, blocksJar, workPluginUrl};



            URLClassLoader ucl = new URLClassLoader(classUrls, IcyBlockRepository.class.getClassLoader());

            Class loadedClass = ucl.loadClass(classes.get(0));
            Constructor construct = loadedClass.getConstructor();
            Object myObject = construct.newInstance();

            return new IcyBlockWrapper(myObject);
        }
        catch (Exception e)
        {
            logger.error("no class found with name");
            return null;
        }
    }

    private static void CheckIfExists(URL blocksJar)
    {
        File temp = new File(blocksJar.getFile());
        if (!temp.exists())
        {
            logger.warn("Could not find file : " + blocksJar.getFile());
        }
    }

    public static GraphBlock ConstructThreasholdBlock()
    {
        GraphBlock loadImage = new GraphBlock();
        loadImage.Type = "plugins.adufour.thresholder.Thresholder";

        {
            BlockIO input1 = new BlockIO();
            input1.Name = "Input";
            loadImage.Inputs.add(input1);
        }

        {
            BlockIO input2 = new BlockIO();
            input2.Name = "channel";
            input2.SetValue(0);  // optional parameter, start it with valid.
            loadImage.Inputs.add(input2);
        }

        {
            BlockIO input3 = new BlockIO();
            input3.Name = "Manual thresholds";
            loadImage.Inputs.add(input3);
        }

        {
            BlockIO input4 = new BlockIO();
            input4.Name = "Treat as percentiles";
            input4.SetValue(false);  // optional parameter, start it with valid.
            loadImage.Inputs.add(input4);
        }

        {
            BlockIO outValue = new BlockIO();
            outValue.Name = "Binary output";
            outValue.Id = "output";
            loadImage.Outputs.add(outValue);
        }

        return loadImage;
    }
}
