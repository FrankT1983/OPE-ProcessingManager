package de.c3e.BlockTemplates.Examples;

import de.c3e.BlockTemplates.Templates.ChannelCalculator;

/**
 * Simple test implementation to test Channel Calculator
 */
public class SetChannelToAverage extends ChannelCalculator
{
    @Override
    public double[][] Calculate(double[][] data)
    {
        double sum = 0;
        int sizeY = data.length;
        int sizeX = data[0].length;
        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                sum += data[y][x];
            }
        }

        double average = sum / ((double)(sizeX*sizeY));

        for (int y = 0; y < sizeY; y++)
        {
            for (int x = 0; x < sizeX; x++)
            {
                data[y][x] = average;
            }
        }


        return data;
    }
}
