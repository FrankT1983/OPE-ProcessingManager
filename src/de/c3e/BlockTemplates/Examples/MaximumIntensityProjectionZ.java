package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.PointProjector;

/**
 * Reduce a stack to a single slice, containing the maximum intensity for that point/channel
 */
@SuppressWarnings("unused") // class will be found through reflection => can suppress this warning
public class MaximumIntensityProjectionZ extends PointProjector<Double>
{
    public MaximumIntensityProjectionZ()
    {
        super(ImageDimension.Z);
    }

    @Override
    public Double Aggregate(Double data1, Double data2)
    {
        return Math.max(data1,data2);
    }
}



