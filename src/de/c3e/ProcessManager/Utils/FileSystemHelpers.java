package de.c3e.ProcessManager.Utils;

import java.io.File;

/**
 * Helper function for interacting with the file system.
 */
public class FileSystemHelpers
{
    public static void CreateFolder(String dumpFolder)
    {
        File theDir = new File(dumpFolder);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            try{
                theDir.mkdir();
            }
            catch(SecurityException se){
                //handle it
            }
        }
    }

    public static String CheckAndCorrectDirectoryPathEnd(String dumpFolder)
    {
        if (dumpFolder.length()>0)
        {
            if (!((dumpFolder.endsWith("/") || dumpFolder.endsWith("\\"))))
            {
                dumpFolder = dumpFolder + "/";
            }
        }
        return dumpFolder;
    }
}
