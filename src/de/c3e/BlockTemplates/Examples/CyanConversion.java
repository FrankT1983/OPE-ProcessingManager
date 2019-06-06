package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.CubeCalculator;
import de.c3e.BlockTemplates.Templates.Helpers.TemplateHelper;

public class CyanConversion extends CubeCalculator<Byte>
{
    public CyanConversion()
    {
        super(ImageDimension.X,ImageDimension.Y,ImageDimension.C);
    }

    @Override
    public Byte[][][] Calculate(Byte[][][] data)
    {
        Byte[][][] result = TemplateHelper.CreateNewWithSameSize(data);
        int cSize =data.length;

        int cStart=0;
        if (cSize >= 3)
        {
            for (cStart = 0; cStart < data.length; cStart++)
            {
                for (int y = 0; y < data[0].length; y++)
                {
                    for (int x = 0; x < data[0][0].length; x++)
                    {
                        result[0][y][x] = data[0][y][x];
                        result[1][y][x] = data[1][y][x];
                        result[2][y][x] = data[0][y][x];
                    }
                }
            }
        }

        // copy rest of the channels
        for (int c = cStart; c < data.length; c++)
        {
            for (int y = 0; y < data[0].length; y++)
            {
                for (int x = 0; x < data[0][0].length; x++)
                {
                    result[c][y][x] = data[c][y][x];
                }
            }
        }

        return result;
    }
}
