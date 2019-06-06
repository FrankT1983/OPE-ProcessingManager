
package de.c3e.Tests;

import de.c3e.ProcessManager.Utils.ImageSubBlock;
import de.c3e.ProcessManager.Utils.ImageSubBlockUtils;
import de.c3e.ProcessManager.Utils.ImageSubSet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CompactionTest
{
    @Test
    public void EmptyListTest()
    {
        List<ImageSubBlock> test = new ArrayList<>();
        List<ImageSubBlock> res=  ImageSubBlockUtils.CompactSubBlocks(test);
        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    public void EmptyListComplexTest()
    {
        List<ImageSubBlock> test = new ArrayList<>();
        List<ImageSubBlock> res=  ImageSubBlockUtils.CompactSubBlocksComplex(test);
        assertNotNull(res);
        assertEquals(0, res.size());
    }

    @Test
    public void OneDimensionDifferent()
    {
        List<ImageSubBlock> test = new ArrayList<>();

        {
            ImageSubBlock foo = new ImageSubBlock();
            foo.dimensions = new ImageSubSet(10, 5);
            foo.data = new byte[50];
            foo.type = byte.class;

            test.add(foo);
        }

        {
            ImageSubBlock bar = new ImageSubBlock();
            bar.dimensions = new ImageSubSet(10, 5);
            bar.dimensions.StartY = 5;
            bar.data = new byte[50];
            bar.type = byte.class;

            test.add(bar);
        }

        List<ImageSubBlock> res=  ImageSubBlockUtils.CompactSubBlocksComplex(test);
        assertNotNull(res);
        assertEquals(1, res.size());
    }

    private ImageSubBlock CreatePiece(int startX, int sizeX, int startY, int sizeY)
    {
        return CreatePiece(startX,sizeX,startY,sizeY,0,1);
    }

    private ImageSubBlock CreatePiece(int startX, int sizeX, int startY, int sizeY,int startC, int sizeC)
    {
        ImageSubBlock foo = new ImageSubBlock();
        foo.dimensions = new ImageSubSet(sizeX, sizeY,sizeC,1,1);
        foo.dimensions.StartX=startX;
        foo.dimensions.StartY=startY;
        foo.dimensions.StartY=startY;
        foo.dimensions.StartC=startC;
        foo.data = new byte[sizeX*sizeY*sizeC];
        foo.type = byte.class;
        return foo;
    }

    @Test
    public void FivePieceCompleteDimensionDifferent()
    {
        List<ImageSubBlock> test = new ArrayList<>();

        test.add(this.CreatePiece(0,2,0,1));
        test.add(this.CreatePiece(2,1,0,2));
        test.add(this.CreatePiece(0,1,1,2));
        test.add(this.CreatePiece(1,1,1,1));
        test.add(this.CreatePiece(1,2,2,1));

        /*
            112
            342
            355
        */


        List<ImageSubBlock> res=  ImageSubBlockUtils.CompactSubBlocksComplex(test);
        assertNotNull(res);
        assertEquals(1, res.size());
        ImageSubBlock b = res.get(0);
        assertEquals(3, b.dimensions.SizeX);
        assertEquals(3, b.dimensions.SizeY);
        assertEquals(0, b.dimensions.StartX);
        assertEquals(0, b.dimensions.StartY);
    }

    @Test
    public void ProvidedToMuch()
    {
        List<ImageSubBlock> test = new ArrayList<>();

        test.add(this.CreatePiece(0,1504,0,1004,0,2));
        test.add(this.CreatePiece(0,1504,0,1004,1,1));

        List<ImageSubBlock> res=  ImageSubBlockUtils.CompactSubBlocksComplex(test);
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(test.get(0).dimensions,res.get(0).dimensions);
    }
}
