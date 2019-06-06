package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.Helpers.TemplateHelper;
import de.c3e.BlockTemplates.Templates.HyperCube4DCalculator;

/**
 * Example for HyperCube4DCalculator
 */
@SuppressWarnings("unused")
public class HyperCubeMaxXYCZ extends HyperCube4DCalculator<Double>
{
    public HyperCubeMaxXYCZ()
    {
        super(ImageDimension.X, ImageDimension.Y, ImageDimension.C, ImageDimension.Z);
    }

    @Override
    public Double[][][][] Calculate(Double[][][][] data)
    {
        Double maxIntensity = Double.MIN_VALUE;
        for (Double[][][] colorPlane : data)
        {
            for (Double[][] plane : colorPlane)
            {
                for (Double[] line : plane)
                {
                    for (Double point : line)
                    {
                        maxIntensity = Math.max(maxIntensity, point);
                    }
                }
            }
        }

        Double[][][][] result = TemplateHelper.CreateNewWithSameSize(data);
        for (int h=0;h<data.length;h++)
        {
            for (int i = 0; i < data[0].length; i++)
            {
                for (int j = 0; j < data[0][0].length; j++)
                {
                    for (int k = 0; k < data[0][0][0].length; k++)
                    {
                        result[h][i][j][k] = maxIntensity;
                    }
                }
            }
        }
        return result;
    }
}


