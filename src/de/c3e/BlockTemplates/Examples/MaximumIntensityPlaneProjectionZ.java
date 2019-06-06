package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.Helpers.TemplateHelper;
import de.c3e.BlockTemplates.Templates.IndexedPlaneProjector;
import de.c3e.BlockTemplates.Templates.PlaneProjector;


@SuppressWarnings("unused") // class will be found through reflection => can suppress this warning
public class MaximumIntensityPlaneProjectionZ extends PlaneProjector<Double>
{
    public MaximumIntensityPlaneProjectionZ()
    {
        super(  ImageDimension.X,ImageDimension.Y,ImageDimension.Z);
    }

    @Override
    public Double[][] Aggregate(Double[][] data1, Double[][] data2)
    {
        double max=0;
        Double[][] result = TemplateHelper.CreateNewWithSameSize(data1);
        for (int y=0; y< data1.length; y++)
        {
            for (int x=0; x< data1[0].length; x++)
            {
                max = Math.max(data1[y][x],max);
                max = Math.max(data2[y][x],max);
            }
        }

        for (int y=0; y< data1.length; y++)
        {
            for (int x = 0; x < data1[0].length; x++)
            {
                result[y][x] = max;
            }
        }
        return result;
    }
}


