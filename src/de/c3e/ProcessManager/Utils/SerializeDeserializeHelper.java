package de.c3e.ProcessManager.Utils;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import java.io.*;
import java.lang.invoke.MethodHandles;

/**
 * Helper class to simply serialisation and deserialization
 */
public class SerializeDeserializeHelper
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static void ObjectToFile(Serializable object, String path)
    {
        try
        {
        FileUtils.writeByteArrayToFile(new File(path), ObjectToBytes(object));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public static void ObjectToFile(Serializable object, File targetFile) throws Exception
    {
        FileOutputStream fout = new FileOutputStream(targetFile);
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(object);
        oos.close();
        fout.close();
    }

    public static Object ObjectFromFile(File targetFile) throws Exception
    {
        FileInputStream fout = new FileInputStream(targetFile);
        ObjectInputStream oos = new ObjectInputStream(fout);
        Object res = oos.readObject();
        oos.close();
        fout.close();
        return res;
    }

    // code from http://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
    public static byte[] ObjectToBytes(Serializable object)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(object);
            return bos.toByteArray();
        }
        catch (Exception e)
        {
            logger.error("Error Serializing object: " + object);
            e.printStackTrace();
            DebugHelper.PrintException(e,logger);
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
            } catch (IOException ex)
            {
                // ignore close exception
            }
            try
            {
                bos.close();
            } catch (IOException ex)
            {
                // ignore close exception
            }
        }
        return null;
    }

    public static Serializable BytesToObject(byte[] ser)
    {
        ByteArrayInputStream bis = new ByteArrayInputStream(ser);
        ObjectInput in = null;
        try
        {
            in = new ObjectInputStream(bis);
            return (Serializable) in.readObject();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }

    public static Object ObjectFromFile(String absolutePath)
    {
        try
        {
            byte[] bytes = FileUtils.readFileToByteArray(new File(absolutePath));
            return BytesToObject(bytes);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;

    }
}
