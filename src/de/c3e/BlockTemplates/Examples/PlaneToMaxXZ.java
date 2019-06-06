package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.Helpers.TemplateHelper;
import de.c3e.BlockTemplates.Templates.PlaneCalculator;



/**
 * Example to set an plan to its maximum value => this is the x-z version
 */
@SuppressWarnings("unused")
public class PlaneToMaxXZ extends PlaneCalculator<Integer>
{
    public PlaneToMaxXZ()
    {
        // setup which plane is required
        super(ImageDimension.Z,ImageDimension.X);
    }

    @Override
    public Integer[][] Calculate(Integer[][] data)
    {
        Integer maxIntensity = Integer.MIN_VALUE;
        for (Integer[] line : data)
        {
            for (Integer point : line)
            {
                maxIntensity = Math.max(maxIntensity,point);
            }
        }

        Integer[][] result = TemplateHelper.CreateNewWithSameSize(data);
        for (int i=0;i<data.length;i++)
        {
            for (int j=0;j<data[0].length;j++)
            {
                result[i][j] = maxIntensity;
            }
        }

        return result;
    }
}



