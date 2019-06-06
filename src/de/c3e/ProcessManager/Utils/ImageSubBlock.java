package de.c3e.ProcessManager.Utils;

import de.c3e.BlockTemplates.ImageDimension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Image Data in parts
 * This has to be a homogeneous area, but not necessarily a x-y plane.
 */
public class ImageSubBlock implements java.io.Serializable
{
    public ImageSubSet dimensions;
    /**
     * The data representing the image.
     * The dimension order is:
     * T-Z-C-Y-X
     */
    public Object data;
    public Class type;
    public boolean isSigned;

    @Override public String toString()
    {
        return this.dimensions.toString();
    }

    public static ImageSubBlock ofSameType(ImageSubBlock other)
    {
        ImageSubBlock result = new ImageSubBlock();
        result.maxValue = other.getMaxValue();
        result.minValue = other.getMinValue();
        result.type = other.type;
        result.isSigned = other.isSigned;
        return result;
    }

    public List<ImageSubBlock> getCutLocalSlicesKeepSizeInDimensions(ImageDimension[] dimensions)
    {
        return getCutLocalSlicesKeepSizeInDimensions(Arrays.asList(dimensions));
    }

    public List<ImageSubBlock> getCutLocalSlicesKeepSizeInDimensions(Collection<ImageDimension> dimensions)
    {
        ImageSubSet localBounds = this.dimensions;
        List<ImageSubSet> sliceBounds = SubSetFinder.SliceAlongDimensions(localBounds,dimensions);
        List<ImageSubBlock> result = new ArrayList<>();
        for (ImageSubSet slice : sliceBounds)
        {
            List<ImageSubBlock> sources = new ArrayList<>();
            sources.add(this);
            ImageSubBlock sliceData = ImageSubBlockUtils.ConstructSubBlockFromParts(slice,sources);
            result.add(sliceData);
        }

        return result;
    }

    private double maxValue;
    private double minValue;
    public double getMinValue()
    {
        return this.minValue;
    }
    public  double getMaxValue()
    {
        return this.maxValue;
    }

    public void setMinValue(double newMin)
    {
        this.minValue = newMin;
    }
    public void setMaxValue(double newMax)
    {
        this.maxValue = newMax;
    }
}



