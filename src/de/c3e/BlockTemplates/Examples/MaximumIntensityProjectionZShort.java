package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.PointProjectorShort;

public class MaximumIntensityProjectionZShort extends PointProjectorShort
{
    public MaximumIntensityProjectionZShort()
    {
        super(ImageDimension.Z);
    }

    @Override
    public short Aggregate(short data1, short data2)
    {
        if (data1 > data2)
            return data1;
        return data2;
    }
}
