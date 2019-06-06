package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.CubeCalculator;
import de.c3e.BlockTemplates.Templates.Helpers.TemplateHelper;

@SuppressWarnings("unused")
public class CubeMaxXYC extends CubeCalculator<Integer>
{
    public CubeMaxXYC()
    {
        super(ImageDimension.X, ImageDimension.Y, ImageDimension.C);
    }

    @Override
    public Integer[][][] Calculate(Integer[][][] data)
    {
        Integer maxIntensity = Integer.MIN_VALUE;
        for (Integer[][] plane : data)
        {
            for (Integer[] line : plane)
            {
                for (Integer point : line)
                {
                    maxIntensity = Math.max(maxIntensity, point);
                }
            }
        }

        Integer[][][] result = TemplateHelper.CreateNewWithSameSize(data);
        for (int i=0;i<data.length;i++)
        {
            for (int j=0;j<data[0].length;j++)
            {
                for (int k=0;k<data[0].length;k++)
                {
                    result[i][j][k] = maxIntensity;
                }
            }
        }

        return result;
    }
}