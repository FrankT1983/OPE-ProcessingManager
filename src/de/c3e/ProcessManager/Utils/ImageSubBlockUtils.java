package de.c3e.ProcessManager.Utils;

import com.google.common.collect.Sets;
import de.c3e.BlockTemplates.ImageDimension;
import icy.sequence.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Helper to convert Icy Images to my own type.
 * And some more functions for working with subblocks.
 */
public class ImageSubBlockUtils
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static List<ImageSubBlock> fromSequence(Sequence icy)
    {
        List<ImageSubBlock> result = new ArrayList<>();

        for (int t = 0; t< icy.getSizeT() ; t ++)
        {
            for (int z = 0; z < icy.getSizeZ() ; z ++)
            {
                for (int c = 0; c< icy.getSizeC() ; c ++)
                {
                    ImageSubBlock block = new ImageSubBlock();
                    switch (icy.getDataType_())
                    {
                        case INT:
                        {
                            block.data = icy.getDataCopyXYAsInt(t, z, c);
                            block.type = int.class;
                        }   break;

                        case DOUBLE:
                        {
                            block.data = icy.getDataCopyXYAsDouble(t, z, c);
                            block.type = double.class;
                        }   break;

                        case FLOAT:
                        {
                            block.data = icy.getDataCopyXYAsFloat(t, z, c);
                            block.type = float.class;

                        }   break;


                        case SHORT:
                        case USHORT:
                        {
                            block.data = icy.getDataCopyXYAsShort(t, z, c);
                            block.type = short.class;
                        }break;



                        case UBYTE:
                        case BYTE:
                        {
                            block.data = icy.getDataCopyXYAsByte(t, z, c);
                            block.type = byte.class;
                        }break;

                        default:
                            DebugHelper.BreakIntoDebug();
                    }

                    block.dimensions = ImageSubBlockUtils.Plane(t,z,c,icy);
                    block.isSigned = icy.getDataType_().isSigned();
                    block.setMinValue(icy.getDataType_().getMinValue());
                    block.setMaxValue(icy.getDataType_().getMaxValue());
                    result.add(block);
                }
                icy.removeImage(t,z);
            }
        }

        return result;
    }

    private static ImageSubSet Plane(int t, int z, int c, Sequence icy)
    {
        ImageSubSet res = new ImageSubSet();
        res.StartX = 0;
        res.SizeX = icy.getSizeX();

        res.StartY = 0;
        res.SizeY = icy.getSizeY();

        res.StartC =c ;
        res.SizeC = 1;

        res.StartT = t;
        res.SizeT = 1;

        res.StartZ = z;
        res.SizeZ = 1;

        return res;
    }

    public static List<ImageSubBlock> ConstructSubBlocksFromParts(List<ImageSubSet> allocatedSubsets, List<ImageSubBlock> localParts)
    {
        List<ImageSubBlock> neededSubBLocks = new ArrayList<>();
        synchronized (localParts)
        {
            for (ImageSubBlock p : localParts)
            {
                for (ImageSubSet set : allocatedSubsets)
                {
                    ImageSubSet intersection = SubSetFinder.Intersection(p.dimensions, set);
                    if (intersection != null)
                    {
                        if (intersection.equals(p.dimensions))
                        {
                            neededSubBLocks.add(p);
                        } else
                        {
                            ImageSubBlock intersectionData = GetSubareaFromPart(intersection, p);
                            neededSubBLocks.add(intersectionData);

                        }
                    }
                }
            }
        }
        List<ImageSubBlock> compacted = CompactSubBlocks(neededSubBLocks);
        if (compacted == null)
        {
            DebugHelper.BreakIntoDebug();
        }

        return compacted;
    }

    public static ImageSubBlock ConstructSubBlockFromParts(ImageSubSet desiredSubset, List<ImageSubBlock> parts)
    {
        if (parts.size() == 1)
        {
            ImageSubBlock singlePart = parts.get(0);
            if (singlePart.dimensions.equals(desiredSubset))
            {
                return singlePart;
            }

            ImageSubSet intersection = SubSetFinder.Intersection(singlePart.dimensions, desiredSubset);
            if (intersection == null)
            {   return null;    }
        }

        boolean changedSomething = false;
        List<ImageSubBlock> neededSubBLocks = new ArrayList<>();
        for (ImageSubBlock p :parts)
        {
            ImageSubSet intersection = SubSetFinder.Intersection(p.dimensions, desiredSubset);
            if (intersection != null)
            {
                if (intersection.equals(p.dimensions))
                {
                    neededSubBLocks.add(p);
                }
                else
                {
                    ImageSubBlock intersectionData = GetSubareaFromPart(intersection, p);
                    neededSubBLocks.add(intersectionData);
                    changedSomething = true;
                }
            }
        }

        List<ImageSubBlock> compacted = CompactSubBlocks(neededSubBLocks);
        if (compacted == null)
        {
            DebugHelper.BreakIntoDebug();
        }

        if ((!changedSomething) && compacted.size() == parts.size())
        {
            // in this case the recursion will run till it hits a stack overflow
            DebugHelper.BreakIntoDebug();
        }

        if (compacted.size() == 1)
        {
            ImageSubBlock only = compacted.get(0);
            if (!desiredSubset.equals(only.dimensions))
            {
                DebugHelper.BreakIntoDebug();
                if (SubSetFinder.Intersection(only.dimensions, desiredSubset).equals(desiredSubset))
                {
                    // constructed the subblock, but created to big of a block
                    ImageSubBlock res = GetSubareaFromPart(desiredSubset, only);
                    return res;
                }


            }
            return  compacted.get(0);
        }

        return ConstructSubBlockFromParts(desiredSubset, compacted);
    }

    /**
     * This is for compacting sub blocks that can not be merged nicely by the normal
     * adjacent operation. As this is can be rather costly: use wisely.
     * @param input The list to compact
     * @return A compacted list;
     */
    public static List<ImageSubBlock> CompactSubBlocksComplex(List<ImageSubBlock> input)
        {
        List<ImageSubBlock> workList = CompactSubBlocks(input);
        if (workList.size()<1)  {   return workList;}


        boolean mergedSomething;
        do
        {
            mergedSomething = false;

            for (int j = input.size(); j>1 ;j--)
            {
                List<List<ImageSubBlock>> allTuples = CreateTuples(workList,j);

                for (List<ImageSubBlock> t : allTuples)
                {
                    List<ImageSubSet> subsets = ImageSubBlockUtils.GetSubSets(t);
                    ImageSubSet boundingBox = SubSetFinder.BoundingBox(subsets);

                    if (SubSetFinder.FillsBoundingBox(boundingBox, subsets))
                    {
                        ImageSubBlock  merged = ConstructFrom(boundingBox,t);
                        workList.removeAll(t);
                        workList.add(merged);
                        mergedSomething = true;
                        break;
                    }
                }

                if (mergedSomething)
                {
                    break;
                }
            }
        } while (mergedSomething);
        return workList;
    }



    private static List<ImageSubSet> GetSubSets(List<ImageSubBlock> list)
    {
        List<ImageSubSet> results = new ArrayList<>();
        for(ImageSubBlock b : list)
        {
            results.add(b.dimensions);
        }
        return results;
    }

    private static List<List<ImageSubBlock>> CreateTuples (List<ImageSubBlock> input,int tupleSize)
    {
        Set<ImageSubBlock> asSet = new HashSet<>(input);
        List<List<ImageSubBlock>> result = new ArrayList<>();

        for (Set<ImageSubBlock> s:  Sets.powerSet(asSet))
        {
            if (s.size() == tupleSize)
            {
                result.add( new ArrayList<>(s));
            }
        }
        return result;
    }

    /**
     * Merge sub blocks in list.
     * @param input A list of sub-blocks.
     * @return The same space, but in merged sub-blocks.
     */
    public static List<ImageSubBlock> CompactSubBlocks(List<ImageSubBlock> input)
    {
        if (input.size() == 0)
            return new ArrayList<>();

        if (input.size() == 1)
            return new ArrayList<>(input);

        List<ImageSubBlock> workList = new ArrayList<>(input);
        // merge inside the plain
        boolean mergedSomething = true;
        while (mergedSomething && workList.size()>1)
        {

            ImageSubBlock first = null;
            ImageSubBlock second = null;
            boolean foundWork = false;
            int toDrop = 0;
            for (int i =0 ; i< workList.size() && !foundWork && toDrop == 0; i++)
            {
                first = workList.get(i);
                for (int j=i+1 ; j< workList.size() && !foundWork && toDrop == 0; j++)
                {
                    second = workList.get(j);
                    foundWork = SubSetFinder.AreAdjacent(first.dimensions,second.dimensions) != null ;

                    ImageSubSet foo = SubSetFinder.Intersection(first.dimensions, second.dimensions);
                    if (foo!=null)
                    {
                        if (foo.equals(first.dimensions)) {toDrop = 1;}
                        if (foo.equals(second.dimensions)) {toDrop = 2;}
                    }
                }
            }

            if (toDrop>0)
            {
                ImageSubBlock keep = second;
                ImageSubBlock remove = first;

                if (toDrop == 1)
                {
                     keep = second;
                     remove = first;
                }
                if (toDrop == 2)
                {
                     keep = first;
                     remove = second;
                }

                workList.remove(remove);
                logger.error("Had to drop " + remove +  " since " + keep + "contains it completely" );
                continue;
            }

            mergedSomething = foundWork;
            if (foundWork)
            {
                ImageSubBlock merged = Merge(first,second);
                workList.remove(first);
                workList.remove(second);
                workList.add(merged);
            }
        }

        return workList;
    }

    private static ImageSubBlock MergeLineWise(ImageSubBlock first, ImageSubBlock second)
    {
        int resultDataSize = first.dimensions.getPixelSize() + second.dimensions.getPixelSize();
        Object resultData =   Array.newInstance(first.type,resultDataSize);
        ImageSubSet mergedBounds = SubSetFinder.MergeSubSets(Arrays.asList(first.dimensions,second.dimensions));

        int dataOffset = 0;
        for (int t= mergedBounds.StartT;t < mergedBounds.getEndT() ; t++)
        {
            for (int z= mergedBounds.StartZ;z < mergedBounds.getEndZ() ; z++)
            {
                for (int c = mergedBounds.StartC;c < mergedBounds.getEndC() ; c++)
                {
                    for (int y = mergedBounds.StartY;y < mergedBounds.getEndY() ; y++)
                    {
                        int x = mergedBounds.StartX;
                        {
                            int toCopy = mergedBounds.SizeX;
                            if (SubSetFinder.ContainsPoint(first.dimensions,x,y,c,z,t))
                            {
                                int offset = GetOffset(first, x, y, c, z, t);
                                System.arraycopy(first.data, offset, resultData, dataOffset, toCopy);
                            }
                            else if (SubSetFinder.ContainsPoint(second.dimensions,x,y,c,z,t))
                            {
                                int offset = GetOffset(second, x, y, c, z, t);
                                System.arraycopy(second.data, offset, resultData, dataOffset, toCopy);
                            }
                            else
                            {
                                // should never come here
                                DebugHelper.BreakIntoDebug();
                                throw  new NotImplementedException();
                            }
                            dataOffset+=toCopy;
                        }
                    }
                }
            }
        }

        ImageSubBlock result = ImageSubBlock.ofSameType(first);
        result.data = resultData;
        result.dimensions = mergedBounds;
        return result;
    }

    private static ImageSubBlock MergePlaneWise(ImageSubBlock first, ImageSubBlock second)
    {
        if (first.data == null || second.data== null)
        {
            logger.error("Sub block data is empty, cant merge");
            return  null;
        }

        int resultDataSize = first.dimensions.getPixelSize() + second.dimensions.getPixelSize();
        Object resultData =   Array.newInstance(first.type,resultDataSize);
        ImageSubSet mergedBounds = SubSetFinder.MergeSubSets(Arrays.asList(first.dimensions,second.dimensions));

        int dataOffset = 0;
        for (int t= mergedBounds.StartT;t < mergedBounds.getEndT() ; t++)
        {
            for (int z= mergedBounds.StartZ;z < mergedBounds.getEndZ() ; z++)
            {
                for (int c = mergedBounds.StartC;c < mergedBounds.getEndC() ; c++)
                {
                    int y = mergedBounds.StartY;
                    {
                        int x = mergedBounds.StartX;
                        {
                            int toCopy = mergedBounds.SizeX *mergedBounds.SizeY;

                            if (SubSetFinder.ContainsPoint(first.dimensions,x,y,c,z,t))
                            {
                                int offset = GetOffset(first, x, y, c, z, t);
                                System.arraycopy(first.data, offset, resultData, dataOffset, toCopy);
                            }
                            else if (SubSetFinder.ContainsPoint(second.dimensions,x,y,c,z,t))
                            {
                                int offset = GetOffset(second, x, y, c, z, t);
                                System.arraycopy(second.data, offset, resultData, dataOffset, toCopy);
                            }
                            else
                            {
                                // should never come here
                                DebugHelper.BreakIntoDebug();
                                throw  new NotImplementedException();
                            }
                            dataOffset+=toCopy;
                        }
                    }
                }
            }
        }

        ImageSubBlock result = ImageSubBlock.ofSameType(first);
        result.data = resultData;
        result.dimensions = mergedBounds;
        return result;
    }


    private static ImageSubBlock Merge(ImageSubBlock first, ImageSubBlock second)
    {
        if (first.dimensions.StartX == second.dimensions.StartX && first.dimensions.SizeX == second.dimensions.SizeX)
        {
            if (first.dimensions.StartY == second.dimensions.StartY && first.dimensions.SizeY == second.dimensions.SizeY)
            {
                return MergePlaneWise(first, second);
            }
            else
            {
                return MergeLineWise(first, second);
            }
        }

        ImageSubSet mergedBounds = SubSetFinder.MergeSubSets(Arrays.asList(first.dimensions,second.dimensions));
        List<ImageSubBlock> toMerge = new ArrayList<>();
        toMerge.add(first);
        toMerge.add(second);
        return ConstructFrom(mergedBounds,toMerge);
    }

    private static ImageSubBlock ConstructFrom(ImageSubSet boundingBox, List<ImageSubBlock> subsets)
    {
        ImageSubBlock first = subsets.get(0);
        Object resultData =  Array.newInstance(first.type,boundingBox.getPixelSize());
        int dataOffset = 0;

        for (int t= boundingBox.StartT;t < boundingBox.getEndT() ; t++)
        {
            for (int z= boundingBox.StartZ;z < boundingBox.getEndZ() ; z++)
            {
                for (int c = boundingBox.StartC;c < boundingBox.getEndC() ; c++)
                {
                    for (int y = boundingBox.StartY;y < boundingBox.getEndY() ; y++)
                    {
                        for (int x = boundingBox.StartX;x < boundingBox.getEndX() ; x++)
                        {
                            boolean done = false;
                            for (ImageSubBlock s : subsets)
                            {
                                if (SubSetFinder.ContainsPoint(s.dimensions,x,y,c,z,t))
                                {
                                    int pointsFromThisBlock = s.dimensions.getEndX() - x;
                                    int dataOffsetSource= GetOffset(s,x,y,c,z,t);

                                    try
                                    {
                                        System.arraycopy(s.data, dataOffsetSource, resultData, dataOffset, pointsFromThisBlock);
                                    }
                                    catch (Exception ex)
                                    {
                                        DebugHelper.BreakIntoDebug();
                                    }
                                    dataOffset+= pointsFromThisBlock;
                                    x+=pointsFromThisBlock-1;
                                    done = true;
                                    break;
                                }
                            }

                            if (!done)
                            {
                                // should never come here
                                DebugHelper.BreakIntoDebug();
                                throw  new NotImplementedException();
                            }

                        }
                    }

                }
            }
        }

        ImageSubBlock result = ImageSubBlock.ofSameType(first);
        result.data = resultData;
        result.dimensions = boundingBox;
        return result;
    }

    private static Object GetDataAsArray(ImageSubBlock source,int posX, int posY, int posC, int posZ, int posT, int size)
    {
        int dataOffset= GetOffset(source,posX,posY,posC,posZ,posT);

        Object resultData =   Array.newInstance(source.type,size);
        System.arraycopy(source.data,dataOffset,resultData,0,size);
        return resultData;
    }

    public static int GetOffset(ImageSubBlock source,int posX, int posY, int posC, int posZ, int posT)
    {
        ImageSubSet bounds = source.dimensions;
        int lineSize = bounds.SizeX;
        int planeSize = lineSize * bounds.SizeY;
        int colorPlaneSize = planeSize * bounds.SizeC;
        int stackSize = colorPlaneSize * bounds.SizeZ;
        // int fullSize = colorPlaneSize * bounds.SizeT;

        return  stackSize       *(posT - bounds.StartT)+
                colorPlaneSize  *(posZ - bounds.StartZ)+
                planeSize       *(posC - bounds.StartC)+
                lineSize        *(posY - bounds.StartY)+
                (posX - bounds.StartX);
    }

    /**
     * Calcualte which remote block are needed to fully fill a desired subset.
     * @param desiredSubset The desired subset.
     * @param localParts Parts that are already available.
     * @param remoteParts Parts that are available remotely.
     * @return The list of remote parts needed.
     */
    public static List<RemoteImagePartRequest> CalculateNeededRemoteBlocks(final ImageSubSet desiredSubset, final List<ImageSubBlock> localParts, final List<RemoteImageSubBlock> remoteParts)
    {
        List<RemoteImagePartRequest> result = new ArrayList<>();

        // check if covered by local parts
        {
            List<ImageSubSet> localDimensions = new ArrayList<>();
            for (ImageSubBlock l : localParts)
            {
                localDimensions.add(l.dimensions);
            }
            List<ImageSubSet> localDimensionsCompacted = SubSetFinder.CompactSubSets(localDimensions);
            List<ImageSubSet> localInterSectingBounds = new ArrayList<>();
            for (ImageSubSet l : localDimensionsCompacted)
            {
                // satisfy this from a single local block
                ImageSubSet localIntersect = SubSetFinder.Intersection(l, desiredSubset);
                if (localIntersect != null && localIntersect.equals(desiredSubset))
                {
                    return new ArrayList<>();
                }
                if (localIntersect != null)
                {
                    localInterSectingBounds.add(localIntersect);
                }
            }


            // satisfy this from multiple local blocks
            localInterSectingBounds = SubSetFinder.CompactSubSets(localInterSectingBounds);
            if (localInterSectingBounds.size() == 1  && localInterSectingBounds.get(0).equals(desiredSubset))
            {
                return new ArrayList<>();
            }
        }

        for (RemoteImageSubBlock p : remoteParts)
        {
            boolean alreadyHaveThisToo = false;
            for (ImageSubBlock l : localParts)
            {
                if (l.dimensions.equals(p.getDimensions()))
                {
                    alreadyHaveThisToo = true;
                    break;
                }

            }
            if (alreadyHaveThisToo)
            {   continue;   }

            // fully covered by locals
            ImageSubSet intersect = SubSetFinder.Intersection(p.getDimensions(), desiredSubset);
            if (intersect != null)
            {
                boolean alreadyInList = false;
                for (RemoteImagePartRequest already : result)
                {
                    if (already.Block.getDimensions().equals(p.getDimensions()) && already.SubSet.equals(intersect))
                    {
                        alreadyInList=true;
                        break;
                    }
                }


                if (!alreadyInList)
                {
                    result.add(new RemoteImagePartRequest(p, intersect));
                }
            }
        }

        return result;
    }

    private static ImageSubBlock GetSubareaFromPartLineWiseOptimization(ImageSubSet desiredSubSet, ImageSubBlock p)
    {
        ImageSubSet bounds = desiredSubSet;
        int resultDataSize = desiredSubSet.getPixelSize();
        Object resultData =   Array.newInstance(p.type,resultDataSize);
        int copied = 0;
        // todo : this could be way more efficient
        for (int t= bounds.StartT;t < bounds.getEndT() ; t++)
        {
            for (int z= bounds.StartZ;z < bounds.getEndZ() ; z++)
            {
                for (int c = bounds.StartC;c < bounds.getEndC() ; c++)
                {
                    for (int y = bounds.StartY;y < bounds.getEndY() ; y++)
                    {
                        Object data = GetDataAsArray(p, desiredSubSet.StartX, y, c, z, t, desiredSubSet.SizeX);
                        System.arraycopy(data, 0, resultData, copied, desiredSubSet.SizeX);
                        copied+= desiredSubSet.SizeX;
                    }
                }
            }
        }
        ImageSubBlock result = ImageSubBlock.ofSameType(p);
        result.data = resultData;
        result.dimensions = desiredSubSet;
        return result;
    }

    private static ImageSubBlock GetSubareaFromPart(ImageSubSet desiredSubSet, ImageSubBlock p)
    {
        if (p.dimensions.StartX == desiredSubSet.StartX && p.dimensions.SizeX >= desiredSubSet.SizeX)
        {
            return GetSubareaFromPartLineWiseOptimization(desiredSubSet,p);
        }

        ImageSubSet bounds = desiredSubSet;
        int resultDataSize = desiredSubSet.getPixelSize();
        Object resultData =   Array.newInstance(p.type,resultDataSize);

        int copied = 0;
        // todo : this could be way more efficient
        for (int t= bounds.StartT;t < bounds.getEndT() ; t++)
        {
            for (int z= bounds.StartZ;z < bounds.getEndZ() ; z++)
            {
                for (int c = bounds.StartC;c < bounds.getEndC() ; c++)
                {
                    for (int y = bounds.StartY;y < bounds.getEndY() ; y++)
                    {
                        for (int x = bounds.StartX;x < bounds.getEndX() ; x++)
                        {
                            try
                            {
                                if (SubSetFinder.ContainsPoint(p.dimensions, x, y, c, z, t))
                                {
                                    Object data = GetDataAsArray(p, x, y, c, z, t, 1);
                                    System.arraycopy(data, 0, resultData, copied, 1);
                                    copied++;
                                } else
                                {
                                    // should not happen
                                    DebugHelper.BreakIntoDebug();
                                }
                            } catch (Exception e)
                            {
                                DebugHelper.BreakIntoDebug();
                                throw e;
                            }
                        }
                    }
                }
            }
        }
        ImageSubBlock result = ImageSubBlock.ofSameType(p);
        result.data = resultData;
        result.dimensions = desiredSubSet;
        return result;
    }

    public static List<List<ImageSubBlock>> SortByDimension(List<ImageSubBlock> toSort, Collection<ImageDimension> sortDimensions)
    {
        List<ImageSubBlock> use = new ArrayList<>(toSort);
        List<List<ImageSubBlock>> result = new ArrayList<>();
        while (use.size()>0)
        {
            ImageSubBlock next = use.get(0);
            use.remove(0);

            List<ImageSubBlock> belongTogether = new ArrayList<>();
            belongTogether.add(next);
            List<ImageSubBlock> toRemove = new ArrayList<>();
            for(ImageSubBlock other: use)
            {
                if (SubSetFinder.InSameProjectionDirection(other.dimensions,next.dimensions,sortDimensions))
                {
                    belongTogether.add(other);
                    toRemove.add(other);
                }
            }
            use.removeAll(toRemove);
            result.add(belongTogether);
        }
        return result;
    }


}

