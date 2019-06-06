package de.c3e.Tests;


import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.ProcessManager.Utils.*;
import de.c3e.ProcessingManager.Types.SplitType;
import de.c3e.ProcessingManager.Types.SplitTypes;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for the subset finding, merging logic.
 */
public class SubSetFinderTest
{
    @Test
    public void SinglePlainStartTest()
    {
        ImageSubSet subSet = SubSetFinder.CalculateSubSet(20, new ArrayList<ImageSubSet>(), new SplitType(SplitTypes.independentPoints), new ImageSubSet(10,10,1,1,1)).get(0);

        assertNotNull(subSet);

        // should give the first 2 lines
        assertEquals(0, subSet.StartX );
        assertEquals(10, subSet.SizeX );

        assertEquals(0, subSet.StartY );
        assertEquals(2, subSet.SizeY);

        assertEquals(0, subSet.StartC );
        assertEquals(1, subSet.SizeC );

        assertEquals(0, subSet.StartT );
        assertEquals(1, subSet.SizeT );

        assertEquals(0, subSet.StartZ );
        assertEquals(1, subSet.SizeZ );
    }

    @Test
    public void SinglePlainMiddleTest()
    {
        ImageSubSet alreadyRequested = new ImageSubSet(10,3);
        ImageSubSet subSet = SubSetFinder.CalculateSubSet(20, Collections.singleton(alreadyRequested)
                ,new SplitType(SplitTypes.independentPoints)
                ,new ImageSubSet(10,10,1,1,1)).get(0);

        assertNotNull(subSet);

        // should give the first 2 lines
        assertEquals(0, subSet.StartX );
        assertEquals(10, subSet.SizeX );

        assertEquals(3, subSet.StartY );
        assertEquals(2, subSet.SizeY);

        assertEquals(0, subSet.StartC );
        assertEquals(1, subSet.SizeC );

        assertEquals(0, subSet.StartT );
        assertEquals(1, subSet.SizeT );

        assertEquals(0, subSet.StartZ );
        assertEquals(1, subSet.SizeZ );

        assertEquals(20, subSet.getPixelSize());
    }

    @Test
    public void ChannelTest1()
    {
        ImageSubSet alreadyRequested = new ImageSubSet(10,10);
        alreadyRequested.setSize(2,ImageDimension.C);
        ImageSubSet subSet = SubSetFinder.CalculateSubSet(100,Collections.singleton(alreadyRequested), new SplitType(SplitTypes.independentChannels),new ImageSubSet(10,10,4,1,1)).get(0);

        assertNotNull(subSet);

        // should give me a complete plain, to channels in
        assertEquals(0, subSet.StartX );
        assertEquals(10, subSet.SizeX );

        assertEquals(0, subSet.StartY );
        assertEquals(10, subSet.SizeY);

        assertEquals(2, subSet.StartC );
        assertEquals(1, subSet.SizeC );

        assertEquals(0, subSet.StartT );
        assertEquals(1, subSet.SizeT );

        assertEquals(0, subSet.StartZ );
        assertEquals(1, subSet.SizeZ );
    }

    @Test
    public void SinglePlainEndTest()
    {
        ImageSubSet alreadyRequested = new ImageSubSet(10,9);
        ImageSubSet subSet = SubSetFinder.CalculateSubSet(20,Collections.singleton(alreadyRequested), new SplitType(SplitTypes.independentPoints),new ImageSubSet(10,10,1,1,1)).get(0);

        assertNotNull(subSet);

        // should give the first 2 lines
        assertEquals(0, subSet.StartX );
        assertEquals(10, subSet.SizeX );

        assertEquals(9, subSet.StartY );
        assertEquals(1, subSet.SizeY);

        assertEquals(0, subSet.StartC);
        assertEquals(1, subSet.SizeC );

        assertEquals(0, subSet.StartT );
        assertEquals(1, subSet.SizeT );

        assertEquals(0, subSet.StartZ );
        assertEquals(1, subSet.SizeZ );

        // should give less than requested
        assertEquals(10, subSet.getPixelSize());
    }

    @Test
    public void SinglePlainOvershootTest()
    {
        ImageSubSet alreadyRequested = new ImageSubSet(10,10);
        List<ImageSubSet> subSet = SubSetFinder.CalculateSubSet(20,Collections.singleton(alreadyRequested), new SplitType(SplitTypes.independentPoints),new ImageSubSet(10,10,1,1,1));

        assertTrue(subSet == null);
    }

    @Test
    public void MergeSubsets()
    {
        List<ImageSubSet> input = new ArrayList<>();
        input.add(new ImageSubSet(10,10));

        ImageSubSet result = SubSetFinder.MergeSubSets(input);

        assertNotNull(result);
        assertEquals(10, result.SizeX);
        assertEquals(10, result.SizeY);
    }

    @Test
    public void Merge2Subsets()
    {
        List<ImageSubSet> input = new ArrayList<>();
        input.add(new ImageSubSet(5,3));

        ImageSubSet secondHalf = new ImageSubSet();
        secondHalf.SizeX = 5;
        secondHalf.SizeY = 2;
        secondHalf.StartY = 3;
        input.add(secondHalf);

        ImageSubSet result = SubSetFinder.MergeSubSets(input);

        assertNotNull(result);
        assertEquals(5, result.SizeX);
        assertEquals(5, result.SizeY);
    }

    @Test
    public void Merge2inverseOrderSubsets()
    {
        List<ImageSubSet> input = new ArrayList<>();

        ImageSubSet secondHalf = new ImageSubSet();
        secondHalf.SizeX = 5;
        secondHalf.SizeY = 2;
        secondHalf.StartY = 3;
        input.add(secondHalf);

        input.add(new ImageSubSet(5,3));

        ImageSubSet result = SubSetFinder.MergeSubSets(input);

        assertNotNull(result);
        assertEquals(5, result.SizeX);
        assertEquals(5, result.SizeY);
    }

    @Test
    public void Merge2Channels()
    {
        List<ImageSubSet> input = new ArrayList<>();

        ImageSubSet secondHalf = new ImageSubSet(5,5);
        secondHalf.StartC = 1;
        input.add(secondHalf);


        input.add(new ImageSubSet(5,5));

        ImageSubSet result = SubSetFinder.MergeSubSets(input);

        assertNotNull(result);
        assertEquals(5, result.SizeX);
        assertEquals(5, result.SizeY);
        assertEquals(2, result.SizeC);
    }

    @Test
    public void Merge2TimePoints()
    {
        List<ImageSubSet> input = new ArrayList<>();

        ImageSubSet secondHalf = new ImageSubSet(5,5);
        secondHalf.StartT =1;
        input.add(secondHalf);


        input.add(new ImageSubSet(5,5));

        ImageSubSet result = SubSetFinder.MergeSubSets(input);

        assertNotNull(result);
        assertEquals(5, result.SizeX);
        assertEquals(5, result.SizeY);
        assertEquals(2, result.SizeT);
    }

    @Test
    public void SinglePlainStartTest2()
    {
        ImageSubSet subSet = SubSetFinder.CalculateSubSet(20, new ArrayList<ImageSubSet>(), new SplitType(SplitTypes.independentPoints),new ImageSubSet(1024,786,1,1,1)).get(0);

        assertNotNull(subSet);

        // should give the first line
        assertEquals(0, subSet.StartX );
        assertEquals(1024, subSet.SizeX );

        assertEquals(0, subSet.StartY );
        assertEquals(1, subSet.SizeY);

        assertEquals(0, subSet.StartC );
        assertEquals(1, subSet.SizeC );

        assertEquals(0, subSet.StartT );
        assertEquals(1, subSet.SizeT );

        assertEquals(0, subSet.StartZ );
        assertEquals(1, subSet.SizeZ );
    }

    @Test
    public void RequestAfterEnd()
    {
        ImageSubSet alreadyRequested = new ImageSubSet(10,10);
        alreadyRequested.setSize(3,ImageDimension.C);
        List<ImageSubSet> subSet = SubSetFinder.CalculateSubSet(10,Collections.singleton(alreadyRequested), new SplitType(SplitTypes.independentPoints),new ImageSubSet(10,10,3,1,1));

        assertNull(subSet);
    }

    @Test
    public void RequestAfterEndChannelWise()
    {
        ImageSubSet alreadyRequested = new ImageSubSet(10,10);
        alreadyRequested.setSize(3,ImageDimension.C);
        List<ImageSubSet> subSet = SubSetFinder.CalculateSubSet(10,Collections.singleton(alreadyRequested), new SplitType(SplitTypes.independentChannels),new ImageSubSet(10,10,3,1,1));

        assertNull(subSet);
    }

    @Test
    public void Difference()
    {
        ImageSubSet full = new ImageSubSet(672,712);
        full.setSize(104,ImageDimension.Z);

        ImageSubSet part = new ImageSubSet(full);
        part.setSize(36, ImageDimension.Y);
        ImageSubSet remaining = SubSetFinder.Difference(full,part);

        assertNotNull(remaining);

        assertEquals(remaining.getSize(ImageDimension.X),672);
        assertEquals(remaining.getSize(ImageDimension.Y),712-36);
        assertEquals(remaining.getStart(ImageDimension.Y),36);
    }

    @Test
    public void CalculateNeededParts()
    {
        List<ImageSubBlock> local = new ArrayList<>();
        List<RemoteImageSubBlock> remote = new ArrayList<>();
        int sizeX=5;
        int sizeY=5;
        int sizeC=3;
        int sizeZ=3;
        int sizeT=4;

        // => needs complete z stack including all channel =>  maximum of each time point
        for (byte t = 0; t <sizeT ; t++)
        {
            for (byte z = 0; z < sizeZ; z++)
            {
                for (byte c = 0; c < sizeC; c++)
                {
                    RemoteImageSubBlock p = new RemoteImageSubBlock();
                    ImageSubSet currentDim = new ImageSubSet(sizeX,sizeY);
                    currentDim.StartC = c;
                    currentDim.StartZ = z;
                    currentDim.StartT = t;
                    p.setDimensions(currentDim);
                    remote.add(p);
                }
            }
        }

        ImageSubSet desired = new ImageSubSet(sizeX,sizeY,sizeC,sizeZ,sizeT);
        List<RemoteImagePartRequest> needed = ImageSubBlockUtils.CalculateNeededRemoteBlocks(desired,local,remote);

        assertNotNull(needed);
        assertEquals(remote.size(), needed.size());
    }

    @Test
    public void ValuesFromDebuggingTest()
    {
        List<ImageDimension> dims = new ArrayList<>();
        dims.add(ImageDimension.X);
        dims.add(ImageDimension.Y);
        dims.add(ImageDimension.C);
        ImageSubSet foo = new ImageSubSet(3008,20008,3,1,1);
        List<ImageSubSet> res = SubSetFinder.SliceAlongDimensionsCalculation(foo,dims);

        assertEquals(1,res.size());
    }

    @Test
    public void ValuesFromDebuggingTest2()
    {

        List<ImageSubSet> alreadyRequested = new ArrayList<>();
        alreadyRequested.add( new ImageSubSet(1504,104,1,1,1));
        alreadyRequested.add( new ImageSubSet(1504,104,2,1,1));
        alreadyRequested.add( new ImageSubSet(1504,104,3,1,1));

        alreadyRequested.get(0).StartT =8;
        alreadyRequested.get(1).StartC =1;
        alreadyRequested.get(2).StartT =2;

        ImageSubSet s = new ImageSubSet(1504,104,1,1,1);


        ImageSubSet notYetRequested = SubSetFinder.Difference(s,alreadyRequested, Collections.singletonList(ImageDimension.T));

        assertTrue(notYetRequested.equals(s));
    }


    @Test
    public void AreEqualTest1()
    {

        List<ImageSubSet> list1 = new ArrayList<>();
        list1.add( new ImageSubSet(11,13,1,1,5));
        list1.add( new ImageSubSet(11,13,1,1,5));
        list1.get(0).StartT =5;

        List<ImageSubSet> list2 = new ArrayList<>();
        list2.add( new ImageSubSet(11,13,1,1,3));
        list2.add( new ImageSubSet(11,13,1,1,7));
        list2.get(1).StartT =3;


        assertTrue(SubSetFinder.AreEqual(list1,list2));
    }


    @Test
    public void AreEqualTest2()
    {

        List<ImageSubSet> list1 = new ArrayList<>();
        list1.add( new ImageSubSet(11,13,1,1,5));
        list1.add( new ImageSubSet(11,13,1,1,5));
        list1.get(0).StartT =5;

        List<ImageSubSet> list2 = new ArrayList<>();
        list2.add( new ImageSubSet(11,13,1,1,3));
        list2.add( new ImageSubSet(11,13,1,1,6));
        list2.get(1).StartT =3;


        assertFalse(SubSetFinder.AreEqual(list1,list2));
    }
}
