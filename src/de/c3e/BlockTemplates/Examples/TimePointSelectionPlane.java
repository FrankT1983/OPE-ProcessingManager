package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.Annotations.InputParameter;
import de.c3e.BlockTemplates.Templates.Helpers.TemplateHelper;
import de.c3e.BlockTemplates.Templates.IndexedPlaneProjector;

@InputParameter(Name = "TimeIndex" , Description = "The time point to select" , Typ = int.class)
public class TimePointSelectionPlane<T> extends IndexedPlaneProjector<T>
{
    public TimePointSelectionPlane()
    {
        super(  ImageDimension.X,ImageDimension.Y,ImageDimension.T);
    }

    @Override
    public T[][] Aggregate(T[][] data1, T[][] data2, int posTData1, int posTData2)
    {
        int desiredT = this.getInput("TimeIndex");
        T[][] result = TemplateHelper.CreateNewWithSameSize(data1);


        T[][] source = data1;
        if (posTData2== desiredT)
        {
            source= data2;
        }

        for (int y = 0; y < data1.length; y++)
        {
            System.arraycopy(source[y],0,result[y],0,source[y].length);
        }
        return result;
    }

    public byte[][] Aggregate(byte[][] data1, byte[][] data2, int posTData1, int posTData2)
    {
        int desiredT = this.getInput("TimeIndex");
        byte[][] result = TemplateHelper.CreateNewWithSameSize(data1);


        byte[][] source = data1;
        if (posTData2== desiredT)
        {
            source= data2;
        }

        for (int y = 0; y < data1.length; y++)
        {
            System.arraycopy(source[y],0,result[y],0,source[y].length);
        }
        return result;
    }

    public float[][] Aggregate(float[][] data1, float[][] data2, int posTData1, int posTData2)
    {
        int desiredT = this.getInput("TimeIndex");
        float[][] result = TemplateHelper.CreateNewWithSameSize(data1);


        float[][] source = data1;
        if (posTData2== desiredT)
        {
            source= data2;
        }

        for (int y = 0; y < data1.length; y++)
        {
            System.arraycopy(source[y],0,result[y],0,source[y].length);
        }
        return result;
    }
}

