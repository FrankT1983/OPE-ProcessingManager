package de.c3e.BlockTemplates.Templates;

import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.ProcessManager.Utils.ImageSubBlock;

/**
 * helper class to store some of the optimized functions, that would otherwise clutter ProjectionPluginImpl
 */
class ProjectionPluginOptimizations
{
    static ImageSubBlock RunPointProjectingOnDoubleImplFromShortArray(ImageSubBlock block, PointProjectorDouble impl, ImageDimension projectionDir, ImageSubBlock result)
    {
        short[] outStuff = new short[result.dimensions.getPixelSize()];
        short[] data = (short[])block.data;
        result.dimensions.setSize(1, projectionDir);

        System.arraycopy(data,0,outStuff,0, block.dimensions.getSizeC()*block.dimensions.getSizeY()*block.dimensions.getSizeX());

        int i=0;
        int j=0;
        for (int t = 0; t < block.dimensions.getSizeT(); t++)
        {
            for (int z = 0; z < block.dimensions.getSizeZ(); z++)
            {
                for (int c = 0; c < block.dimensions.getSizeC(); c++)
                {
                    for (int y = 0; y < block.dimensions.getSizeY(); y++)
                    {
                        for (int x = 0; x < block.dimensions.getSizeX(); x++)
                        {
                            outStuff[j] =  (short)(impl.Aggregate((double)outStuff[j], (double)data[i]));
                            i++;
                            j++;
                        }
                    }
                }
                j = 0;
            }
        }
        result.data = outStuff;
        return result;
    }

    static ImageSubBlock RunPointProjectingOnShortImplFromShortArray(ImageSubBlock block, PointProjectorShort impl, ImageDimension projectionDir, ImageSubBlock result)
    {
        short[] outStuff = new short[result.dimensions.getPixelSize()];
        short[] data = (short[])block.data;
        result.dimensions.setSize(1, projectionDir);

        System.arraycopy(data,0,outStuff,0, block.dimensions.getSizeC()*block.dimensions.getSizeY()*block.dimensions.getSizeX());
        int i=0;
        int j=0;
        for (int t = 0; t < block.dimensions.getSizeT(); t++)
        {
            for (int z = 0; z < block.dimensions.getSizeZ(); z++)
            {
                for (int c = 0; c < block.dimensions.getSizeC(); c++)
                {
                    for (int y = 0; y < block.dimensions.getSizeY(); y++)
                    {
                        for (int x = 0; x < block.dimensions.getSizeX(); x++)
                        {
                            outStuff[j] = impl.Aggregate(outStuff[j], data[i]);
                            i++;
                            j++;
                        }
                    }
                }
                j = 0;
            }
        }
        result.data = outStuff;
        return result;
    }
}
