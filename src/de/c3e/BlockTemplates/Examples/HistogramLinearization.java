package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.CubeCalculator;
import de.c3e.BlockTemplates.Templates.Helpers.TemplateHelper;
import de.c3e.BlockTemplates.Templates.PlaneCalculator;

import java.util.HashSet;
import java.util.Set;

/// A plugin equalize the histogram of an image plane.
@SuppressWarnings("unused")
public class HistogramLinearization extends PlaneCalculator<Integer>
{
    public HistogramLinearization()
    {
        // setup which plane is required
        super(ImageDimension.Y,ImageDimension.X);
    }

    @Override
    public Integer[][] Calculate(Integer[][] data)
    {
        Set<Integer> foundIntensities = new HashSet<>();

        Integer maxIntensity = (int)this.getMaxValue();
        Integer minIntensity = (int)this.getMinValue();
        for (Integer[] line : data)
        {
            for (Integer point : line)
            {
                maxIntensity = Math.max(maxIntensity,point);
                minIntensity = Math.min(minIntensity,point);
            }
        }
        double dataRange = (double)maxIntensity- (double) minIntensity;
        //double intRange = (double)Integer.MAX_VALUE - (double)Integer.MIN_VALUE;
        double intRange = 256.0;

        // rescale Image
        Integer[][] result = TemplateHelper.CreateNewWithSameSize(data);
        for (int i=0;i<data.length;i++)
        {
            for (int j=0;j<data[0].length;j++)
            {
                int current = data [i][j];
                result[i][j] = (int) (((current - minIntensity)  / dataRange) * intRange);
            }
        }

        return result;
    }
}



