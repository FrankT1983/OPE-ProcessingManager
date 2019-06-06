package de.c3e.ProcessManager.Utils;

import de.c3e.ProcessManager.DataTypes.WorkbookToCsv;
import de.c3e.ProcessManager.GlobalSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.lang.invoke.MethodHandles;

/**
 * Little Helper class to read a text file.
 */
public class TextFileUtil
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    /**
     * Opens a file and returns its content as a string.
     * @param f The file.
     * @return The contents, as a string.
     */
    public static String FileToString(java.io.File f)
    {
        if((f != null) && f.isFile())
        {
            try
            {
                String         line;
                StringBuilder  stringBuilder = new StringBuilder();
                String         ls = System.getProperty("line.separator");

                try (BufferedReader reader = new BufferedReader(new java.io.FileReader(f)))
                {
                    while ((line = reader.readLine()) != null)
                    {
                        stringBuilder.append(line);
                        stringBuilder.append(ls);
                    }
                }

                return stringBuilder.toString();
            }
            catch (Exception e)
            {
                return e.toString();
            }
        }

        return "";
    }

    /**
     * Opens a file from a given path and returns its content as a string.
     * @param path The path to a file.
     * @return The contents, as a string.
     */
    public static String FileToString(String path)
    {
        String fullPath = GlobalSettings.WorkFolder + path;
        return FileToString(new File(fullPath));
    }


    public static void WriteArrayAsCsv(String[][] strings, BufferedWriter writer)
    {
        for (String[] line : strings)
        {
            StringBuilder buffer = new StringBuilder();
            for (String column : line)
            {
                buffer.append(column).append(WorkbookToCsv.DEFAULT_SEPARATOR);
            }

            try
            {
                String s = buffer.toString().trim();
                writer.write(s.substring(0,s.length()-1));
                writer.newLine();
            } catch (Exception e)
            {
                logger.error("Could not write csv: " + e.toString());
            }
        }
    }
}
