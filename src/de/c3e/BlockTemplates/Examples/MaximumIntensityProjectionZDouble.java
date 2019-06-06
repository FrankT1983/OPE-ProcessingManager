package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.PointProjectorDouble;

public class MaximumIntensityProjectionZDouble extends PointProjectorDouble
{
    public MaximumIntensityProjectionZDouble()
    {
        super(ImageDimension.Z);
    }

    @Override
    public double Aggregate(double data1, double data2)
    {
        return Math.max(data1,data2);
    }
}


