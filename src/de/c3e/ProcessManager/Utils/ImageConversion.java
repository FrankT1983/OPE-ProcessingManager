package de.c3e.ProcessManager.Utils;



import icy.image.IcyBufferedImage;
import icy.imagej.ImageJUtil;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import ij.ImagePlus;

import java.util.List;


/**
 * Helper class to over ways to convert images between different needed formats.
 */
public class ImageConversion
{
    static Sequence TryConvertImageToIcySequence(Object data)
    {
        if (data instanceof Sequence)
        {   return (Sequence)data;    }

        Sequence convertedImage = new Sequence();

        if (data instanceof ImagePlus[])
        {
            for (ImagePlus img : (ImagePlus[])data)
            {
                return ImageJUtil.convertToIcySequence(img,null);
            }
        }
        if (data instanceof AbstractImageObject)
        {
            return ToSequence(((AbstractImageObject)data));
        }

        return convertedImage;
    }

    /**
     * Convert an abstract image object into a Icy sequence object
     * @param abstractImageObject The abstract image object.
     * @return The sequence object.
     */
    public static Sequence ToSequence(AbstractImageObject abstractImageObject)
    {
        List<ImageSubSet> comp =SubSetFinder.CompactSubSets(abstractImageObject.getBoundsOfLocalParts());
        if (comp.size() != 1)
        {   DebugHelper.BreakIntoDebug();   }
        ImageSubSet bounds = comp.get(0);

        Class dataType = abstractImageObject.getLocalPartsSubBlocks().get(0).type;
        boolean signed = abstractImageObject.getLocalPartsSubBlocks().get(0).isSigned;

        Sequence image = new Sequence();
        for(int t=bounds.StartT;t<bounds.SizeT;t++)
        {
            for(int z=bounds.StartZ;z<bounds.SizeZ;z++)
            {

                if (dataType.getName().equals(byte.class.getName()))
                {

                    IcyBufferedImage icy_img;
                    if (signed)
                    {
                        icy_img=new IcyBufferedImage(bounds.SizeX, bounds.SizeY, bounds.SizeC, DataType.BYTE);
                    }
                    else
                    {
                        icy_img=new IcyBufferedImage(bounds.SizeX, bounds.SizeY, bounds.SizeC, DataType.UBYTE);
                    }

                    image.setImage(t, z, icy_img);

                    for (int c = bounds.StartC; c < bounds.SizeC; c++)
                    {
                        byte[] data = abstractImageObject.getDataXYAsByteArray(bounds.StartX, bounds.SizeX, bounds.StartY, bounds.SizeY, c, z, t);
                        Array1DUtil.byteArrayToArray(data, image.getDataXY(t, z, c),signed);
                    }
                    continue;
                }

                if (dataType.getName().equals(short.class.getName()))
                {

                    IcyBufferedImage icy_img;
                    if (signed)
                    {
                        icy_img=new IcyBufferedImage(bounds.SizeX, bounds.SizeY, bounds.SizeC, DataType.SHORT);
                    }
                    else
                    {
                        icy_img=new IcyBufferedImage(bounds.SizeX, bounds.SizeY, bounds.SizeC, DataType.USHORT);
                    }

                    image.setImage(t, z, icy_img);

                    for (int c = bounds.StartC; c < bounds.SizeC; c++)
                    {
                        short[] data = abstractImageObject.getDataXYAsShortArray(bounds.StartX, bounds.SizeX, bounds.StartY, bounds.SizeY, c, z, t);
                        Array1DUtil.shortArrayToArray(data, image.getDataXY(t, z, c),signed);
                    }
                    continue;
                }

                //if (dataType.getName().equals(Double.class.getName()))
                // fallback and double
                {
                    if (!dataType.getName().equals(Double.class.getName()))
                    {
                        // fallback convertion is somthing I actually don't want to do
                        DebugHelper.BreakIntoDebug();
                    }

                    IcyBufferedImage icy_img = new IcyBufferedImage(bounds.SizeX, bounds.SizeY, bounds.SizeC, DataType.DOUBLE);
                    image.setImage(t, z, icy_img);

                    for (int c = bounds.StartC; c < bounds.SizeC; c++)
                    {
                        double[] data = abstractImageObject.getDataXYAsDoubleArray(bounds.StartX, bounds.SizeX, bounds.StartY, bounds.SizeY, c, z, t);
                        Array1DUtil.doubleArrayToArray(data, image.getDataXY(t, z, c));
                    }
                }
            }
        }
        return image;
    }

}
