package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.Templates.Helpers.TemplateHelper;
import de.c3e.BlockTemplates.Templates.HyperCube5DCalculator;

@SuppressWarnings("unused")
public class FullyHyperCubeMax extends HyperCube5DCalculator<Double>
{
    @Override
    public Double[][][][][] Calculate(Double[][][][][] data)
    {
        Double maxIntensity = Double.MIN_VALUE;
        for (Double[][][][] colorStack : data)
        {
            for (Double[][][] colorPlane : colorStack)
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
        }

        Double[][][][][] result = TemplateHelper.CreateNewWithSameSize(data);
        for (int g=0;g<data.length;g++)
        {
            for (int h = 0; h < data[0].length; h++)
            {
                for (int i = 0; i < data[0][0].length; i++)
                {
                    for (int j = 0; j < data[0][0][0].length; j++)
                    {
                        for (int k = 0; k < data[0][0][0][0].length; k++)
                        {
                            result[g][h][i][j][k] = maxIntensity;
                        }
                    }
                }
            }
        }
        return result;
    }
}
