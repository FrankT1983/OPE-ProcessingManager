package de.c3e.Tests;


import de.c3e.ProcessManager.Utils.ImageSubBlock;
import de.c3e.ProcessManager.Utils.ImageSubBlockUtils;
import de.c3e.ProcessManager.Utils.ImageSubSet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the Icy Helper
 */
public class SubBlockHelpers
{

    @Test
    public void SimpleSubBlock()
    {
        List<ImageSubBlock> input = new ArrayList<>();
        {
            ImageSubBlock block1 = new ImageSubBlock();

            block1.dimensions = new ImageSubSet(10, 10);
            byte[] data = new byte[100];
            block1.data = data;
            block1.type = byte.class;
            input.add(block1);

            for (byte i = 0; i < 100; i++)
            {
                data[i] = i;
            }
        }

        ImageSubSet firstToLines = new ImageSubSet(10,10);
        firstToLines.SizeY =2;

        ImageSubBlock block = ImageSubBlockUtils.ConstructSubBlockFromParts(firstToLines,input);

        assertTrue(block.data instanceof byte[]);
        assertEquals(20 , ((byte[])block.data).length);
        assertNotNull(block.dimensions);
        assertNotNull(block.type);
        byte[] data = ((byte[])block.data);
        for (byte i = 0; i < 20; i++)
        {
            assertEquals(i,data[i]);
        }
    }

    @Test
    public void MiddelFromOneBlock()
    {
        List<ImageSubBlock> input = new ArrayList<>();
        {
            ImageSubBlock block1 = new ImageSubBlock();

            block1.dimensions = new ImageSubSet(10, 10);
            byte[] data = new byte[100];
            block1.data = data;
            block1.type = byte.class;
            input.add(block1);

            for (byte i = 0; i < 100; i++)
            {
                data[i] = i;
            }
        }

        ImageSubSet middleTwoLines = new ImageSubSet(10,10);
        middleTwoLines.StartY = 3;
        middleTwoLines.SizeY =2;

        ImageSubBlock block = ImageSubBlockUtils.ConstructSubBlockFromParts(middleTwoLines,input);

        assertTrue(block.data instanceof byte[]);
        assertEquals(20 , ((byte[])block.data).length);
        byte[] data = ((byte[])block.data);
        for (byte i = 0; i < 20; i++)
        {
            assertEquals(i + 30,data[i]);
        }
    }

    @Test
    public void EndFromMultipleSubBlocks()
    {
        List<ImageSubBlock> input = new ArrayList<>();
        byte i = 0;
        {
            ImageSubBlock block1 = new ImageSubBlock();

            block1.dimensions = new ImageSubSet(5, 2);
            byte[] data = new byte[10];
            block1.data = data;
            block1.type = byte.class;
            input.add(block1);
            for (int j=0; j < 10; j++)
            {
                data[j] = i++;
            }
        }

        {
            ImageSubBlock block2 = new ImageSubBlock();

            block2.dimensions = new ImageSubSet(5, 2);
            block2.dimensions.StartY = 2;
            byte[] data = new byte[10];
            block2.data = data;
            block2.type = byte.class;
            input.add(block2);
            for (int j=0; j < 10; j++)
            {
                data[j] = i++;
            }
        }

        {
            ImageSubBlock block3 = new ImageSubBlock();

            block3.dimensions = new ImageSubSet(5, 1);
            block3.dimensions.StartY = 4;
            byte[] data = new byte[5];
            block3.data = data;
            block3.type = byte.class;
            input.add(block3);
            for (int j=0; j < 5; j++)
            {
                data[j] = i++;
            }
        }

        {
            ImageSubSet firstToLines = new ImageSubSet(5, 2);
            firstToLines.SizeY = 2;

            ImageSubBlock block = ImageSubBlockUtils.ConstructSubBlockFromParts(firstToLines, input);

            assertTrue(block.data instanceof byte[]);
            assertEquals(10, ((byte[]) block.data).length);
            assertNotNull(block.dimensions);
            assertNotNull(block.type);
            byte[] data = ((byte[]) block.data);
            for (i = 0; i < 10; i++)
            {
                assertEquals(i, data[i]);
            }
        }

        {
            ImageSubSet firstToLines = new ImageSubSet(5, 1);
            firstToLines.StartY = 4;

            ImageSubBlock block = ImageSubBlockUtils.ConstructSubBlockFromParts(firstToLines, input);

            assertTrue(block.data instanceof byte[]);
            assertEquals(5, ((byte[]) block.data).length);
            assertNotNull(block.dimensions);
            assertNotNull(block.type);
            byte[] data = ((byte[]) block.data);
            for (i = 0; i < data.length; i++)
            {
                assertEquals(i+20 , data[i]);
            }
        }

        {
            ImageSubSet all = new ImageSubSet(5, 5);
            ImageSubBlock block = ImageSubBlockUtils.ConstructSubBlockFromParts(all, input);

            assertTrue(block.data instanceof byte[]);
            assertEquals(25, ((byte[]) block.data).length);
            assertNotNull(block.dimensions);
            assertNotNull(block.type);
            byte[] data = ((byte[]) block.data);
            for (i = 0; i < data.length; i++)
            {
                assertEquals(i , data[i]);
            }
        }
    }

    @Test
    public void XZSliceFromZStack()
    {
        List<ImageSubBlock> input = new ArrayList<>();
        int sizeX = 5;
        int sizeY = 5;

        byte i = 0;
        for (int z = 0; z < 3 ; z ++)
        {
            ImageSubBlock block = new ImageSubBlock();

            block.dimensions = new ImageSubSet(sizeX, sizeY);
            block.dimensions.StartZ = z;
            byte[] data = new byte[block.dimensions.SizeX*block.dimensions.SizeY];
            block.data = data;
            block.type = byte.class;
            input.add(block);
            for (int j=0; j < data.length; j++)
            {
                data[j] = i++;
            }
        }

        {
            ImageSubSet firstSlice = new ImageSubSet(1, sizeY);
            firstSlice.SizeZ = 3;
            firstSlice.StartY = 0;

            ImageSubBlock block = ImageSubBlockUtils.ConstructSubBlockFromParts(firstSlice, input);

            assertTrue(block.data instanceof byte[]);
            assertEquals(15, ((byte[]) block.data).length);
            assertNotNull(block.dimensions);
            assertNotNull(block.type);
            byte[] data = ((byte[]) block.data);
            i=0;
            for (int j=0; j < 5; j++)
            {
                assertEquals(j*sizeX , data[i]);
                i++;
            }
            for (int j=0; j < 5; j++)
            {
                assertEquals(j*sizeX + sizeY * sizeX, data[i]);
                i++;
            }
            for (int j=0; j < 5; j++)
            {
                assertEquals(j*sizeX + 2* sizeY*sizeX , data[i]);
                i++;
            }
        }
    }

}
