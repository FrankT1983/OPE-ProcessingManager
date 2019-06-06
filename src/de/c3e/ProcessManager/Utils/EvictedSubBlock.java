package de.c3e.ProcessManager.Utils;

import org.slf4j.Logger;

import java.io.File;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;

public class EvictedSubBlock implements java.io.Serializable
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public ImageSubSet dimensions;
    /**
     * The data representing the image.
     * The dimension order is:
     * T-Z-C-Y-X
     */
    public Object data;
    public Class type;
    public boolean isSigned;
    private File dataFile  ;

    public EvictedSubBlock(ImageSubSet dimensions, Class type, boolean isSigned)
    {
        this.dimensions = new ImageSubSet(dimensions);
        this.type = type;
        this.isSigned = isSigned;
    }

    @Override public String toString()
    {
        return "Evicted SubBlock :" + this.dimensions.toString();
    }

    public static EvictedSubBlock Evict(ImageSubBlock b)
    {
        EvictedSubBlock result = new EvictedSubBlock(b.dimensions,b.type,b.isSigned);
        result.SetData(b.data);
        return result;
    }

    public ImageSubBlock DeEvict()
    {
        ImageSubBlock res = new ImageSubBlock();
        res.dimensions = new ImageSubSet(this.dimensions);
        res.isSigned = this.isSigned;
        res.type = this.type;
        res.data = this.DataFromFile();
        return res;
    }

    private Object DataFromFile()
    {
        try
        {
            return SerializeDeserializeHelper.ObjectFromFile(this.dataFile);
        }
        catch (Throwable ex)
        {
            DebugHelper.PrintException(ex,logger);
            DebugHelper.BreakIntoDebug();
        }
        return null;
    }

    private void SetData(Object data)
    {
        if (data instanceof Serializable)
        {
            try
            {
                this.dataFile = File.createTempFile("EvictedData", ".dat");
                this.dataFile.deleteOnExit();

                SerializeDeserializeHelper.ObjectToFile((Serializable)data,this.dataFile);
            }
            catch (Throwable ex)
            {
                DebugHelper.PrintException(ex,logger);
                DebugHelper.BreakIntoDebug();
            }
        }
        else
        {
            DebugHelper.BreakIntoDebug();
        }
    }

    @Override protected void finalize() throws Throwable
    {
        try
        {
            if (this.dataFile != null)
            {
                this.dataFile.delete();
                this.dataFile = null;
            }
        } finally
        {
            super.finalize();
        }
    }}
