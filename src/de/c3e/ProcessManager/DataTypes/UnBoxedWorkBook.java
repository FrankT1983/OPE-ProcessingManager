package de.c3e.ProcessManager.DataTypes;


import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Class for bringing a workbook in a serializable form.
 */
public class UnBoxedWorkBook implements Serializable
{

    private byte[] data;
    public UnBoxedWorkBook(Workbook value)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try
        {
            try
            {
                value.write(bos);
            } finally
            {
                bos.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        this.data = bos.toByteArray();
    }

    public Workbook ReBox()
    {
        try
        {
            return new HSSFWorkbook(new ByteArrayInputStream(this.data));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
