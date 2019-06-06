package de.c3e.ProcessManager.Utils;

import de.c3e.BlockTemplates.ImageDimension;
import org.apache.commons.collections4.iterators.IteratorIterable;

import java.awt.*;
import java.util.Collection;
import java.util.Enumeration;

/**
 * The dimensions of a sub image.
 */
public class ImageSubSet implements java.io.Serializable
{

    // not this is not easily expendable, if a new dimension is added
    public int StartX  ;
    public int SizeX ;
    public int StartY;
    public int SizeY;
    public int StartC;
    public int SizeC;
    public int StartT;
    public int SizeT;
    public int StartZ;
    public int SizeZ;

    public ImageSubSet(ImageSubSet that)
    {
        this.StartX = that.StartX ;
        this.SizeX  = that.SizeX  ;
        this.StartY = that.StartY ;
        this.SizeY  = that.SizeY  ;
        this.StartC = that.StartC ;
        this.SizeC  = that.SizeC  ;
        this.StartT = that.StartT ;
        this.SizeT  = that.SizeT  ;
        this.StartZ = that.StartZ ;
        this.SizeZ  = that.SizeZ;
    }

    @Override
    public boolean equals( Object o )
    {
        if (!( o instanceof ImageSubSet ))
        {   return false;   }

        if ( o == this )
        {   return true;    }


        ImageSubSet that = (ImageSubSet) o;

        return
        this.StartX == that.StartX   &&
        this.SizeX  == that.SizeX    &&
        this.StartY == that.StartY   &&
        this.SizeY  == that.SizeY    &&
        this.StartC == that.StartC   &&
        this.SizeC  == that.SizeC    &&
        this.StartT == that.StartT   &&
        this.SizeT  == that.SizeT    &&
        this.StartZ == that.StartZ   &&
        this.SizeZ  == that.SizeZ;
    }

    public ImageSubSet()
    {
        this.StartX = 0  ;
        this.SizeX = 0 ;
        this.StartY = 0;
        this.SizeY = 0;
        this.StartC = 0;
        this.SizeC = 1;
        this.StartT = 0;
        this.SizeT = 1;
        this.StartZ = 0;
        this.SizeZ = 1;
    }

    public ImageSubSet(int width, int height)
    {
        this.StartX = 0  ;
        this.SizeX = width ;
        this.StartY = 0;
        this.SizeY = height;
        this.StartC = 0;
        this.SizeC = 1;
        this.StartT = 0;
        this.SizeT = 1;
        this.StartZ = 0;
        this.SizeZ = 1;
    }

    public ImageSubSet(int width, int height, int fullSizeC, int fullSizeZ, int fullSizeT)
    {
        this.StartX = 0  ;
        this.SizeX = width ;
        this.StartY = 0;
        this.SizeY = height;
        this.StartC = 0;
        this.SizeC = fullSizeC;
        this.StartT = 0;
        this.SizeT = fullSizeT;
        this.StartZ = 0;
        this.SizeZ = fullSizeZ;
    }

    public int getPixelSize()
    {
        return this.SizeX * this.SizeY  * this.SizeZ * this.SizeT * this.SizeC ;
    }

    public int getEndX(){return this.StartX + this.SizeX;}
    public int getEndY(){return this.StartY + this.SizeY;}
    public int getEndC()
    {
        return this.StartC + this.SizeC;
    }
    public int getEndT()
    {
        return this.StartT + this.SizeT;
    }
    public int getEndZ()
    {
        return this.StartZ + this.SizeZ;
    }

    public void setStarts(int toValue, Iterable<ImageDimension> dimensions)
    {
        for(ImageDimension d: dimensions)
        {
            switch (d)
            {
                case X:this.StartX = toValue;break;
                case Y:this.StartY = toValue;break;
                case C:this.StartC = toValue;break;
                case Z:this.StartZ = toValue;break;
                case T:this.StartT = toValue;break ;
                default:
                    // Missing a dimension
                    DebugHelper.BreakIntoDebug();
            }
        }
    }

    @Override
    public String toString()
    {
        return  "X:" +this.StartX + " - " + this.getEndX()+ "|" +
                "Y:" +this.StartY + " - " + this.getEndY()+ "|" +
                "Z:" +this.StartZ + " - " + this.getEndZ()+ "|" +
                "C:" +this.StartC + " - " + this.getEndC()+ "|" +
                "T:" +this.StartT + " - " + this.getEndT();

    }

    public void setSizes(int toValue, ImageDimension[] dimensions)
    {
        for(ImageDimension d: dimensions)
        {
            switch (d)
            {
                case X:this.SizeX = toValue;break;
                case Y:this.SizeY = toValue;break;
                case C:this.SizeC = toValue;break;
                case Z:this.SizeZ = toValue;break;
                case T:this.SizeT = toValue;break ;
                default:
                    // Missing a dimension
                    DebugHelper.BreakIntoDebug();
            }
        }
    }

    public void setSizes(int toValue, Iterable<ImageDimension> dimensions)
    {
        for(ImageDimension d: dimensions)
        {
            switch (d)
            {
                case X:this.SizeX = toValue;break;
                case Y:this.SizeY = toValue;break;
                case C:this.SizeC = toValue;break;
                case Z:this.SizeZ = toValue;break;
                case T:this.SizeT = toValue;break ;
                default:
                    // Missing a dimension
                    DebugHelper.BreakIntoDebug();
            }
        }
    }
    public int getStart(ImageDimension dimension)
    {

        switch (dimension)
        {
            case X:return this.StartX ;
            case Y:return this.StartY ;
            case C:return this.StartC ;
            case Z:return this.StartZ ;
            case T:return this.StartT ;
            default:
                // Missing a dimension
                DebugHelper.BreakIntoDebug();
        }
        return  -1;
    }

    public int getSize(ImageDimension dimension)
    {
        switch (dimension)
        {
            case X:return this.SizeX ;
            case Y:return this.SizeY ;
            case C:return this.SizeC ;
            case Z:return this.SizeZ ;
            case T:return this.SizeT ;
            default:
                // Missing a dimension
                DebugHelper.BreakIntoDebug();
        }
        return  -1;
    }

    public void setSize(int newSize, ImageDimension dimension)
    {
        switch (dimension)
        {
            case X:this.SizeX = newSize;break;
            case Y:this.SizeY = newSize;break;
            case C:this.SizeC = newSize;break;
            case Z:this.SizeZ = newSize;break;
            case T:this.SizeT = newSize;break;
            default:
                // Missing a dimension
                DebugHelper.BreakIntoDebug();
        }
    }

    public void setStart(int newStart,ImageDimension dimension)
    {
        switch (dimension)
        {
            case X:this.StartX = newStart;break;
            case Y:this.StartY = newStart;break;
            case C:this.StartC = newStart;break;
            case Z:this.StartZ = newStart;break;
            case T:this.StartT = newStart;break;
            default:
                // Missing a dimension
                DebugHelper.BreakIntoDebug();
        }
    }

    public void setStart(int newStart,Iterable<ImageDimension> dimensions)
    {
        for(ImageDimension d : dimensions)
        {
            this.setStart(newStart,d);
        }
    }

    public void setEnd(int newEnd,ImageDimension dimension)
    {
        switch (dimension)
        {
            case X:this.SizeX = newEnd - this.StartX;break;
            case Y:this.SizeY = newEnd - this.StartY;break;
            case C:this.SizeC = newEnd - this.StartC;break;
            case Z:this.SizeZ = newEnd - this.StartZ;break;
            case T:this.SizeT = newEnd - this.StartT;break;

            default:
                // Missing a dimension
                DebugHelper.BreakIntoDebug();
        }
    }

    public int getEnd(ImageDimension d)
    {
        return this.getStart(d) + this.getSize(d);
    }

    public int getSizeT()
    {
        return this.SizeT;
    }

    public int getSizeZ()
    {
        return this.SizeZ;
    }

    public int getSizeC()
    {
        return this.SizeC;
    }

    public int getSizeY()
    {
        return this.SizeY;
    }

    public int getSizeX()
    {
        return this.SizeX;
    }
}
