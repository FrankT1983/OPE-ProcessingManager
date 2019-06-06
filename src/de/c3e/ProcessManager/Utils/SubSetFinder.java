package de.c3e.ProcessManager.Utils;

import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.BlockTemplates.Templates.ChannelCalculatorImpl;
import de.c3e.ProcessingManager.Types.SplitType;
import de.c3e.ProcessingManager.Types.SplitTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * Helper for working with image sub sets
 */
public class SubSetFinder
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private static ImageSubSet CalculateSubSetDependencies(long numberPixels, long alreadyRequested, ImageDimension[] dep, Iterable<ImageSubSet> source)
    {
        if (alreadyRequested>= NumberOfPixels(source))
        {   return null;   }

        List<ImageSubSet> sliceBounds = new ArrayList<>();
        for (ImageSubSet i:source)
        {
            sliceBounds.addAll(SubSetFinder.SliceAlongDimensions(i, dep));
        }

        if ( NumberOfPixels(source) != NumberOfPixels(sliceBounds) )
        {
            DebugHelper.BreakIntoDebug();
        }

        if (sliceBounds.size()>0)
        {
            int slicePixelSize = sliceBounds.get(0).getPixelSize();
            if ((alreadyRequested % slicePixelSize) != 0)
            {
                // some handed out an incomplete slice, should not happen
                DebugHelper.BreakIntoDebug();
                logger.error("some handed out an incomplete slice, should not happen : " + (alreadyRequested % slicePixelSize) );
                return null;
            }

            int i = 0;
            int added = 0;
            List<ImageSubSet> nextSlices = new ArrayList<>();
            // find the next few slices that have not yet been requested and are needed to satisfy the
            // desired number of Pixels
            for (ImageSubSet s :sliceBounds)
            {
                if (i>=alreadyRequested)
                {
                    nextSlices.add(s);
                    added += slicePixelSize;
                    if (added>=numberPixels )
                    {
                        break;
                    }
                }

                i+= slicePixelSize;
            }

            if (nextSlices.isEmpty())
            {
                logger.error("Did not find next slice. Already requested:" + alreadyRequested + " Source size: " + NumberOfPixels(source) + " Num Pixel Slices" + NumberOfPixels(sliceBounds)  + " Possible slices " + sliceBounds.size() + " " + Joiner.on(" | ").join(sliceBounds) + " deps: " + Joiner.on(" | ").join(dep) + " sources " +  Joiner.on(" | ").join(source));

                return null;
            }

            // check if I can't merge these bounds
            ImageSubSet merged = SubSetFinder.MergeSubSets(nextSlices);
            if (merged != null)
            {
                return merged;
            }

            // todo: Check partial merge?
            return nextSlices.get(0);
        }

        // should never come here
        DebugHelper.BreakIntoDebug();
        return null;
    }

    private static List<ImageSubSet> CalculateSubSetDependenciesWithMin2SlicesInOneProjectionDirection(long numberPixels, Iterable<ImageSubSet> alreadyRequested, ImageDimension[] dep, ImageDimension project, Iterable<ImageSubSet> source)
    {
        final int minSlices = 2;
        if (NumberOfPixels(alreadyRequested)>= NumberOfPixels(source))
        {
            return null;
        }

        List<ImageSubSet> sliceBounds = new ArrayList<>();
        for (ImageSubSet i:source)
        {
            if (dep.length == 0 && SubSetFinder.NumberOfPixels(source)> 1000)
            {
                // special case: independent points : this will almost certainly run out of memory
                // since it will generate a list with bounds for each point -> just slice along something
                /*if (project.length > 0)
                {
                    sliceBounds.addAll(SubSetFinder.SliceAlongDimensions(i, project));
                }
                else*/
                {
                    // todo: Find good axis
                    ImageDimension[] foo = new ImageDimension[]{ImageDimension.X};
                    sliceBounds.addAll(SubSetFinder.SliceAlongDimensions(i, foo));
                }

            }
            else
            {
                sliceBounds.addAll(SubSetFinder.SliceAlongDimensions(i, dep));
            }
        }

        if (sliceBounds.size()>0)
        {
            int added = 0;
            List<ImageSubSet> nextSlices = new ArrayList<>();
            // find the next few slices that have not yet been requested and are needed to satisfy the
            // desired number of Pixels
            ImageSubSet first = null;
            // todo: Reorder along projection dir ================================================================
            for (ImageSubSet s :sliceBounds)
            {
                List<ImageSubSet> inter = Intersection(s,alreadyRequested);
                if (inter.size() == 1 && inter.get(0).equals(s))
                {
                    // was already completely requested
                    continue;
                }

                ImageSubSet notYetRequested;
                if (inter.size() == 0)
                {
                    // not yet requested at all => request all
                    notYetRequested = new ImageSubSet(s);
                }
                else
                {
                    // only partly requested => calculate missing part
                    notYetRequested = SubSetFinder.Difference(s, alreadyRequested, Collections.singletonList(project));
                    if (notYetRequested == null || notYetRequested.getPixelSize() == 0)
                    {
                        // was already completely requested
                        continue;
                    }
                }

                boolean add = false;
                if (first == null)
                {
                    first = notYetRequested;
                    add = true;
                }
                else
                {
                    // todo: re-order will get rid of this crazy logic
                    if (DifferOnlyInDimension(project, first,notYetRequested))
                    {
                        add = true;
                    }
                }

                if (add)
                {
                    nextSlices.add(notYetRequested);
                    added += notYetRequested.getPixelSize();
                    if (added >= numberPixels && nextSlices.size() >= minSlices)
                    {
                        break;
                    }
                }
            }
            // check if I can't merge these bounds
            return SubSetFinder.CompactSubSets(nextSlices);
        }

        // should never come here
        DebugHelper.BreakIntoDebug();
        return null;
    }

    public static ImageSubSet Difference(ImageSubSet s, Iterable<ImageSubSet> diffList , Collection<ImageDimension> dimensionsToDiffer)
    {
        ImageSubSet residue = new ImageSubSet(s);
        for (ImageSubSet set: diffList)
        {
            if (DifferOnlyInDimension(dimensionsToDiffer,s,set))
            {
                residue = Difference(residue, set, dimensionsToDiffer);
            }
        }
        return residue;
    }

    public static List<ImageSubSet> CalculateSubSet(long numberPixels, Iterable<ImageSubSet> alreadyRequested, SplitType splitType, ImageSubSet source)
    {
        logger.info( numberPixels +" " + alreadyRequested +" " +  splitType.type +" " +  source.SizeX+" " +  source.SizeY +" " +  source.SizeZ +" " +  source.SizeT +" " +  source.SizeC);
        if (splitType.type == SplitTypes.independentChannels)
        {
            // remap legacy split type
            splitType.type = SplitTypes.useDependencies;
            splitType.dependencies = ChannelCalculatorImpl.Dependencies();
        }

        // hack: if I try to point split decently sized images the current code will create a list with the dimensions
        //       of each Pixel => resulting in an out of heap memory exception => use my legacy code to handle this.
        if (splitType.type == SplitTypes.useDependencies && splitType.dependencies.length== 0)
        {
            splitType.type = SplitTypes.independentPoints;
        }

        List<ImageSubSet> result= null;

        long pixels = NumberOfPixels(alreadyRequested);
        if (splitType.type == SplitTypes.useDependencies )
        {
            ImageSubSet tmp = CalculateSubSetDependencies(numberPixels, pixels, splitType.dependencies, Collections.singletonList(source));
            if (tmp != null)
            {
                result = Collections.singletonList(tmp);
            }
        }else if (splitType.type == SplitTypes.useDependenciesAndProject )
        {
            result = CalculateSubSetDependenciesWithMin2SlicesInOneProjectionDirection(numberPixels,alreadyRequested,splitType.dependencies,splitType.projectionDirection,Collections.singletonList(source));
        }
        else
        {
            ImageSubSet tmp =getImageSubSetIndependentPoints(numberPixels, pixels, splitType, source);
            if (tmp != null)
            {
                List<ImageSubSet> res= new ArrayList<>();
                res.add((ImageSubSet)tmp);

                result = res;
            }
        }

        logger.debug("Requested: " + numberPixels + " Provided Slice with" + SubSetFinder.NumberOfPixels(result)+  " \t" + SubSetFinder.ListToString(result));
        return result;
    }

    private static ImageSubSet getImageSubSetIndependentPoints(long numberPixels, long alreadyRequested, SplitType splitType,  ImageSubSet source)
    {
        int t ;
        int c = 0;
        int z = 0;
        int numberPixelsTransfer = 0;
        int pixelsPerPlane = source.SizeX* source.SizeY;
        boolean found = false;
        int passedPlanes = 0;

        for (t=0; t < source.SizeT; t++)
        {
            for (z=0; z < source.SizeZ; z++)
            {
                for (c=0; c < source.SizeC; c++)
                {
                    numberPixelsTransfer += pixelsPerPlane;
                    found = alreadyRequested < numberPixelsTransfer;
                    if (found)
                    {
                        break;
                    }

                    passedPlanes++;
                }
                if (found)
                {
                    break;
                }
            }

            if (found)
            {
                break;
            }
        }

        if (!found)
        {
            // outside of area
            return null;
        }

        switch (splitType.type)
        {
            case independentPoints:
            {
                // don't work across image planes

                long pixelsTillStartOfThisPlane = pixelsPerPlane * passedPlanes;
                if (numberPixels < source.SizeX )
                {
                    // work only with full lines
                    numberPixels = source.SizeX;
                }

                long offsetInImage = alreadyRequested - pixelsTillStartOfThisPlane;
                int startY = (int) offsetInImage / source.SizeX;
                // I did get an overflow here, if the requested pixel count is very high => fast operations
                int sizeY = (int) Math.min(numberPixels / (long)source.SizeX , (long)source.SizeY);

                if (startY + sizeY > source.SizeY)
                {
                    sizeY = source.SizeY - startY;
                }

                ImageSubSet result = new ImageSubSet();
                result.StartX = 0;
                result.SizeX = source.SizeX;

                result.StartY = startY;
                result.SizeY = sizeY;

                result.SizeC = 1;
                result.StartC = c;

                // if I already give the whole plane, consider giving more than one channel
                if (startY == 0 && sizeY == source.SizeY)
                {
                    while(result.getPixelSize() < numberPixels)
                    {
                        result.SizeC++;
                        if (result.StartC + result.SizeC > source.SizeC)
                        {
                            result.SizeC = source.SizeC - result.StartC ;
                            break;
                        }
                    }
                }

                result.SizeZ = 1;
                result.StartZ = z;
                // if I already give the whole multi color plane, consider giving more than one z slice
                if (startY == 0 && sizeY == source.SizeY && result.StartC == 0 && result.SizeC == source.SizeC )
                {
                    while(result.getPixelSize() < numberPixels)
                    {
                        result.SizeZ++;
                        if (result.StartZ + result.SizeZ > source.SizeZ)
                        {
                            result.SizeZ = source.SizeZ - result.StartZ ;
                            break;
                        }
                    }
                }

                result.SizeT = 1;
                result.StartT = t;
                // if I already give the whole stack, consider giving more than on time point
                if (    result.StartY == 0 && result.SizeY == source.SizeY &&
                        result.StartC == 0 && result.SizeC == source.SizeC &&
                        result.StartZ == 0 && result.SizeZ == source.SizeZ )
                {
                    while(result.getPixelSize() < numberPixels)
                    {
                        result.SizeT++;
                        if (result.StartT + result.SizeT > source.SizeT)
                        {
                            result.SizeT = source.SizeT - result.StartT ;
                            break;
                        }
                    }
                }
                return result;
            }
        }
        return null;
    }


    /**
     * Merge a number of given subsets bounds to on big on
     * @param input The list of bounds to merge.
     * @return The merged bound.
     */
    public static ImageSubSet MergeSubSets(final Collection<ImageSubSet> input)
    {
        if (input.size() == 0)
            return null;

        List<ImageSubSet> workList = new ArrayList<>(input);

        // merge inside the plain
        while (true)
        {
            if (workList.size() == 1)
            {
                return workList.get(0);
            }

            ImageSubSet first = null;
            ImageSubSet second = null;
            ImageDimension dim = null;
            for (int i =0 ; i< workList.size() && dim == null; i++)
            {
                first = workList.get(i);
                for (int j=1 ; j< workList.size() && dim == null ; j++)
                {
                    second = workList.get(j);
                    dim = AreAdjacent(first,second);
                }
            }

            if (dim == null)
            {
                // did not find any more things
                break;
            }

            ImageSubSet merged = MergeDimension(first,second,dim);
            if (merged == null)
            {   DebugHelper.BreakIntoDebug();break; }

            workList.remove(first);
            workList.remove(second);
            workList.add(merged);

        }

        // could not merge everything
        return null;
    }

    private static ImageSubSet MergeDimension(ImageSubSet first, ImageSubSet second, ImageDimension d)
    {
        ImageSubSet result = new ImageSubSet(first);
        if ((result.getEnd(d) == second.getStart(d)) )
        {
            result.setSize(result.getSize(d) + second.getSize(d),d);
            return result;
        }

        if ((result.getStart(d) == second.getEnd(d)))
        {
            result.setStart(second.getStart(d),d);
            result.setSize(result.getSize(d) + second.getSize(d),d);
            return  result;
        }

        return null;
    }

    private static boolean DifferOnlyInDimension(ImageDimension dim, ImageSubSet first, ImageSubSet second)
    {
        return DifferOnlyInDimension(Collections.singleton(dim),first,second);
    }

    private static boolean DifferOnlyInDimension(Collection<ImageDimension> dim, ImageSubSet first, ImageSubSet second)
    {
        for(ImageDimension dCheck :ImageDimension.AllValues)
        {
            if ((first.getSize(dCheck) == second.getSize(dCheck)) && (first.getStart(dCheck) == second.getStart(dCheck)))
            {continue;}

            if (!dim.contains(dCheck))
            {return false;}
        }
        return true;
    }


    static ImageDimension AreAdjacent(ImageSubSet first, ImageSubSet second)
    {
        // x adjacent
        {
            if ((first.getEndX() == second.StartX) || (second.getEndX() == first.StartX))
            {
                // everything else is the same
                if (
                                (first.StartY == second.StartY)  &&
                                (first.SizeY == second. SizeY)  &&
                                (first.StartC == second.StartC)  &&
                                (first. SizeC == second. SizeC)  &&
                                (first.StartT == second.StartT)  &&
                                (first. SizeT == second. SizeT)  &&
                                (first.StartZ == second.StartZ)  &&
                                (first. SizeZ == second. SizeZ)
                        )
                {
                    return ImageDimension.X;
                }
                else
                {
                    return null;
                }
            }
        }


        // y adjacent
        {
            if (    (first.getEndY() == second.StartY)   ||
                    ((first.StartY == second.getEndY()))    )
            {
                // everything else is the same
                if (
                        (first.StartX == second.StartX)  &&
                        (first. SizeX == second. SizeX)  &&
                        (first.StartC == second.StartC)  &&
                        (first. SizeC == second. SizeC)  &&
                        (first.StartT == second.StartT)  &&
                        (first. SizeT == second. SizeT)  &&
                        (first.StartZ == second.StartZ)  &&
                        (first. SizeZ == second. SizeZ)
                        )
                {
                    return ImageDimension.Y;
                }
                else
                {
                    return null;
                }
            }
        }

        // c adjacent
        {
            if (    (first.getEndC() == second.StartC)   ||
                    (first.StartC == second.getEndC()))
            {
                // everything else is the same
                if (
                        (first.StartX == second.StartX)  &&
                        (first. SizeX == second. SizeX)  &&
                        (first.StartY == second.StartY)  &&
                        (first. SizeY == second. SizeY)  &&
                        (first.StartT == second.StartT)  &&
                        (first. SizeT == second. SizeT)  &&
                        (first.StartZ == second.StartZ)  &&
                        (first. SizeZ == second. SizeZ)
                        )
                {
                    return ImageDimension.C;
                }
                else
                {
                    return null;
                }
            }
        }

        // z adjacent
        {
            if (    (first.getEndZ() == second.StartZ)    ||
                    (first.StartZ == second.getEndZ())    )
            {
                // everything else is the same
                if (    (first.StartX == second.StartX)  &&
                        (first. SizeX == second. SizeX)  &&
                        (first.StartY == second.StartY)  &&
                        (first. SizeY == second. SizeY)  &&
                        (first.StartT == second.StartT)  &&
                        (first. SizeT == second. SizeT)  &&
                        (first.StartC == second.StartC)  &&
                        (first. SizeC == second. SizeC))
                {
                    return ImageDimension.Z;
                }
                else
                {
                    return null;
                }
            }
        }

        // t adjacent
        {
            if (    (first.getEndT() == second.StartT)    ||
                    (first.StartT == second.getEndT()) )
            {
                // everything else is the same
                if (    (first.StartX == second.StartX)  &&
                        (first. SizeX == second. SizeX)  &&
                        (first.StartY == second.StartY)  &&
                        (first. SizeY == second. SizeY)  &&
                        (first.StartZ == second.StartZ)  &&
                        (first. SizeZ == second. SizeZ)  &&
                        (first.StartC == second.StartC)  &&
                        (first. SizeC == second. SizeC))
                {
                    return ImageDimension.T;
                }
                else
                {
                    return null;
                }
            }
        }

        return null;
    }


    public static List<ImageSubSet> SliceAlongDimensions(ImageSubSet bounds, ImageDimension[] dimensions)
    {
        return SliceAlongDimensions(bounds, Arrays.asList(dimensions));
    }


    /**
     * Slicing can take long => cache results
     */
    private static LoadingCache<SliceCacheKey, List<ImageSubSet>> sliceCache =
        CacheBuilder.newBuilder().maximumSize(100).build(
                new CacheLoader<SliceCacheKey, List<ImageSubSet>>()
            {
                public List<ImageSubSet> load(SliceCacheKey key)
                {
                    return SliceAlongDimensionsCalculation(key.bounds, key.getDimensions());
                }
            }
        );



    static List<ImageSubSet> SliceAlongDimensions(ImageSubSet bounds, Collection<ImageDimension> dims)
    {
        try
        {
            return sliceCache.get(new SliceCacheKey(bounds, dims));
        }
        catch (Exception ex)
        {
            return SliceAlongDimensionsCalculation(bounds,dims);
        }
    }

    /** Get All slices in along some dimensions
     * This means the planes in the passed dimensions will be kept together
     * @param bounds The complete bounds
     * @param dims The dimensions, in which the planes should stay together
     * @return The resulting list of dimension.
     */
    public static List<ImageSubSet> SliceAlongDimensionsCalculation(ImageSubSet bounds, Collection<ImageDimension> dims)
    {
        List<ImageSubSet> result = new ArrayList<>();
        boolean containsX = dims.contains(ImageDimension.X);
        boolean containsY = dims.contains(ImageDimension.Y);
        boolean containsZ = dims.contains(ImageDimension.Z);
        boolean containsT = dims.contains(ImageDimension.T);
        boolean containsC = dims.contains(ImageDimension.C);

        // todo: I know, that this can be done with recursion, but that is harder to understand and debug later
        for (int t= bounds.StartT;t < bounds.getEndT() ; t++)
        {
            ImageSubSet subSet = new ImageSubSet();
            CopySizeIfContainedSliceOtherwise(subSet,bounds, containsT , ImageDimension.T , t);
            for (int z= bounds.StartZ;z < bounds.getEndZ() ; z++)
            {
                CopySizeIfContainedSliceOtherwise(subSet,bounds, containsZ , ImageDimension.Z , z);
                for (int c = bounds.StartC;c < bounds.getEndC() ; c++)
                {
                    CopySizeIfContainedSliceOtherwise(subSet,bounds, containsC , ImageDimension.C , c);
                    for (int y = bounds.StartY;y < bounds.getEndY() ; y++)
                    {
                        CopySizeIfContainedSliceOtherwise(subSet,bounds, containsY , ImageDimension.Y , y);
                        for (int x = bounds.StartX;x < bounds.getEndX() ; x++)
                        {
                            CopySizeIfContainedSliceOtherwise(subSet,bounds, containsX , ImageDimension.X , x);
                            result.add(new ImageSubSet(subSet));
                            if (dims.contains( ImageDimension.X))   {break;}
                        }
                        if (dims.contains( ImageDimension.Y))   {break;}
                    }
                    if (dims.contains( ImageDimension.C))   {break;}
                }
                if (dims.contains( ImageDimension.Z))   {break;}
            }
            if (dims.contains( ImageDimension.T))   {break;}
        }
        return result;
    }

    /**
     * Copy the size of a bound to a destination bound in a given dimension, if the dimension is part of a list.
     * If not, create a size 1 slice in the provided dimension.
     * @param toModify The bounds to be modified.
     * @param sourceBounds The bounds from which to copy the full range.
     * @param dimension The dimensions currently being discussed.
     * @param indexInDimension The index of the dimensions, in case a slice is created.
     */
    private static void CopySizeIfContainedSliceOtherwise(ImageSubSet toModify, ImageSubSet sourceBounds, boolean contains, ImageDimension dimension, int indexInDimension)
    {
        if (contains)
        {
            SetStartOfDimension(toModify,dimension,StartOfDimension(sourceBounds,dimension));
            SetSizeOfDimension(toModify,dimension,SizeOfDimension(sourceBounds,dimension));
        }
        else
        {
            SetStartOfDimension(toModify,dimension,indexInDimension);
            SetSizeOfDimension(toModify,dimension,1);
        }
    }


    private static int SizeOfDimension(ImageSubSet localBounds, ImageDimension dimension)
    {
        switch (dimension)
        {
            case X: return localBounds.SizeX;
            case Y: return localBounds.SizeY;
            case Z: return localBounds.SizeZ;
            case T: return localBounds.SizeT;
            case C: return localBounds.SizeC;
        }
        // should not happen
        throw  new NotImplementedException();
    }

    private static int StartOfDimension(ImageSubSet localBounds, ImageDimension dimension)
    {
        switch (dimension)
        {
            case X: return localBounds.StartX;
            case Y: return localBounds.StartY;
            case Z: return localBounds.StartZ;
            case T: return localBounds.StartT;
            case C: return localBounds.StartC;
        }
        // should not happen
        throw  new NotImplementedException();
    }

    private static int EndOfDimension(ImageSubSet localBounds, ImageDimension dimension)
    {
        switch (dimension)
        {
            case X: return localBounds.getEndX();
            case Y: return localBounds.getEndY();
            case Z: return localBounds.getEndZ();
            case T: return localBounds.getEndT();
            case C: return localBounds.getEndC();
        }
        // should not happen
        throw  new NotImplementedException();
    }

    private static void SetStartOfDimension(ImageSubSet toModify, ImageDimension dimension, int value)
    {
        switch (dimension)
        {
            case X: toModify.StartX=value;return;
            case Y: toModify.StartY=value;return;
            case Z: toModify.StartZ=value;return;
            case T: toModify.StartT=value;return;
            case C: toModify.StartC=value;return;
        }
        // should not happen
        throw  new NotImplementedException();
    }

    private static void SetSizeOfDimension(ImageSubSet toModify, ImageDimension dimension, int value)
    {
        switch (dimension)
        {
            case X: toModify.SizeX=value;return;
            case Y: toModify.SizeY=value;return;
            case Z: toModify.SizeZ=value;return;
            case T: toModify.SizeT=value;return;
            case C: toModify.SizeC=value;return;
        }
        // should not happen
        throw  new NotImplementedException();

    }

    static ImageSubSet Intersection(ImageSubSet s1, ImageSubSet s2)
    {
        ImageSubSet result = new ImageSubSet( s1);

        for(ImageDimension d :ImageDimension.AllValues)
        {
            if (!InterSectDimension(s1, s2, d, result))
            {
                return null;
            }
        }
        return result;
    }


    static List<ImageSubSet> Intersection(ImageSubSet s1, Iterable<ImageSubSet> s2)
    {
        List<ImageSubSet> intersections = new ArrayList<>();
        for (ImageSubSet part : s2)
        {
            ImageSubSet partInter = Intersection(s1,part);
            if (partInter != null)
            {
                intersections.add(partInter);
            }
        }

        return CompactSubSets(intersections);
    }

    static boolean HasIntersection(Iterable<ImageSubSet> s1, Iterable<ImageSubSet> s2)
    {
        List<ImageSubSet> intersections = new ArrayList<>();
        for (ImageSubSet part1 : s1)
        {
            for (ImageSubSet part2 : s2)
            {
                ImageSubSet partInter = Intersection(part1, part2);
                if (partInter != null)
                {
                    return true;
                }
            }
        }

        return  false;
    }

    // Subtrackt s2 from s1 along a given dimensions
    private static ImageSubSet Difference(ImageSubSet s1, ImageSubSet s2, Collection<ImageDimension> dimensionsToDiffer)
    {
        ImageSubSet result = new ImageSubSet(s1);
        if (Intersection(s1,s2) == null)
        {
            return result;
        }

        for(ImageDimension d :ImageDimension.AllValues)
        {
            if (!dimensionsToDiffer.contains(d))
            { continue;}
           DifferenceDimension(result, s2, d);
        }
        return result;
    }

    private static void DifferenceDimension(ImageSubSet s1, ImageSubSet s2, ImageDimension d)
    {


        int size = s1.getSize(d);
        if (s2.getEnd(d) > s1.getStart(d))
        {
            size = Math.max(0,size -  s2.getEnd(d) - s1.getStart(d));
            s1.setStart(s2.getEnd(d),d);
            s1.setSize(size,d);
        }
        if (s1.getEnd(d) < s2.getStart(d))
        {
            size = Math.max(0, size-  s1.getEnd(d) - s2.getStart(d));
            s1.setSize(size,d);
        }
    }

    private static boolean InterSectDimension(ImageSubSet s1, ImageSubSet s2, ImageDimension dim, ImageSubSet toModify)
    {
        int start = Math.max(StartOfDimension(s1,dim), StartOfDimension(s2,dim));
        int end = Math.min(EndOfDimension(s1,dim), EndOfDimension(s2,dim));
        if (start < end)
        {
            SetStartOfDimension(toModify,dim,start);
            SetSizeOfDimension(toModify,dim,end- start);
            return true;
        }
        return  false;
    }

    static boolean ContainsPoint(ImageSubSet d, int x, int y, int c, int z, int t)
    {
        return
                ((d.StartT<=t) && (d.StartT+d.SizeT>t)) &&
                ((d.StartC<=c) && (d.StartC+d.SizeC>c)) &&
                ((d.StartZ<=z) && (d.StartZ+d.SizeZ>z)) &&
                ((d.StartY<=y) && (d.StartY+d.SizeY>y)) &&
                ((d.StartX<=x) && (d.StartX+d.SizeX>x)) ;
    }

    public static int[] SizesOfDimension(ImageSubSet dimensions, ImageDimension[] dependencies)
    {
        if ((dimensions == null) || (dependencies == null ))
        {   return new int[0];}

        if (dependencies.length == 0)
        {
            // single point
            return new int[]{1};
        }

        int[] deaNumbers = ToOrdinal(dependencies);
        Arrays.sort(dependencies);

        int[] result = new int[dependencies.length];
        for (int i=0;i<result.length;i++)
        {
            // revert order, to ensure that later data generation is in X last
            result[i] = SizeOfDimension(dimensions,ImageDimension.values()[deaNumbers[result.length-i-1]]);
        }

        return result;
    }

    private static  int [] ToOrdinal(ImageDimension[] data)
    {
        int[] result = new int[data.length];
        for (int i=0;i<data.length;i++)
        {
            result[i] = data[i].ordinal();
        }
        return result;
    }

    static boolean InSameProjectionDirection(ImageSubSet d1, ImageSubSet d2, Collection<ImageDimension> sortDimensions)
    {
        for (ImageDimension d:ImageDimension.AllValues)
        {
            if (d1.getStart(d) != d2.getStart(d) || d1.getSize(d) != d2.getSize(d))
            {
                if (!sortDimensions.contains(d))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Calculate an image subset of a given pixel size from a list of sub blocks
     * @param numberPixels The number of pixels the subset should have (minimum)
     * @param alreadyRequested A list of subsets, that have already be requested and should not be part in of the result
     * @param splitType How the source subsets can be split into smaller pieces.
     * @param source The source subsets.
     * @return The result subsets.
     */
    static List<ImageSubSet> CalculateSubSetFromList(long numberPixels, Collection<ImageSubSet> alreadyRequested, SplitType splitType, List<ImageSubSet> source)
    {
        List<ImageSubSet> compacted = CompactSubSetsComplex(source);
        if (splitType.type == SplitTypes.independentChannels)
        {
            // remap legacy split type
            splitType.type = SplitTypes.useDependencies;
            splitType.dependencies = ChannelCalculatorImpl.Dependencies();
        }

        // hack: if I try to point split decently sized images the current code will create a list with the dimensions
        //       of each Pixel => resulting in an out of heap memory exception => use my legacy code to handle this.
        if (splitType.type == SplitTypes.useDependencies && splitType.dependencies.length== 0)
        {
            splitType.type = SplitTypes.independentPoints;
        }

        List<ImageSubSet> result = null;
        long pixelsAlreadyRequested = NumberOfPixels(alreadyRequested);
        if (splitType.type == SplitTypes.useDependencies )
        {
            ImageSubSet res = CalculateSubSetDependencies(numberPixels,pixelsAlreadyRequested,splitType.dependencies,compacted);
            if (res != null)
            {
                List<ImageSubSet> tmp= new ArrayList<>();
                tmp.add(res);
                result = tmp;
            }
            else {result = null ; }
        } else if (splitType.type == SplitTypes.useDependenciesAndProject )
        {
            if (splitType.dependencies == null || splitType.dependencies.length == 0)
            {
                // optimization for point wise
                if (alreadyRequested == null || alreadyRequested.size() == 0)
                {
                    result = CalculatePointProjectionSubset(numberPixels,splitType.projectionDirection, compacted);
                }
                else if (alreadyRequested.size() == 1)
                {
                    result = CalculatePointProjectionSubset(numberPixels,splitType.projectionDirection, alreadyRequested.iterator().next() , compacted);
                }

                if (result == null)
                {
                    result = CalculateSubSetDependenciesWithMin2SlicesInOneProjectionDirection(numberPixels,alreadyRequested,splitType.dependencies,splitType.projectionDirection,compacted);
                }

            }
            else
            {
                result = CalculateSubSetDependenciesWithMin2SlicesInOneProjectionDirection(numberPixels,alreadyRequested,splitType.dependencies,splitType.projectionDirection,compacted);
            }
        }
        else
        {
            for( int i=0; i< compacted.size();i++)
            {
                long alreadyReq = pixelsAlreadyRequested;
                for (int j=0;j<i;j++)
                {
                    alreadyReq -=compacted.get(j).getPixelSize();
                }

                ImageSubSet res = getImageSubSetIndependentPoints(numberPixels, alreadyReq, splitType, compacted.get(i));
                if (res != null)
                {
                    List<ImageSubSet> tmp = new ArrayList<>();
                    tmp.add(res);
                    result = tmp;
                    break;
                }
            }
        }

        logger.debug("Requested: " + numberPixels + " Provided Slice from List with " + SubSetFinder.NumberOfPixels(result)+  " \t" + SubSetFinder.ListToString(result));
        return result ;
    }

    private static List<ImageSubSet> CalculatePointProjectionSubset(long numberPixels, ImageDimension projectionDirection, ImageSubSet alreadyRequested, List<ImageSubSet> compacted)
    {
        long already = 0;
        List<ImageSubSet> result = new ArrayList<>();

        for (ImageSubSet s :compacted)
        {
            ImageSubSet inter = Intersection(s, alreadyRequested);
            if (inter != null && inter.equals(s))
            {
                // was already completely requested
                continue;
            }

            ImageSubSet notYetRequested = Difference(s, alreadyRequested);
            if (notYetRequested == null)
            {
                return null;
            }

            if (notYetRequested.getPixelSize() == 0)
            {
                // was already completely requested
                continue;
            }

            ImageSubSet toAdd = CalculatePointProjectionSubset(numberPixels - already,projectionDirection, notYetRequested);
            result.add(toAdd);
            already += toAdd.getPixelSize();
        }

        return result;
    }

    private static ImageSubSet CalculatePointProjectionSubset(long numberPixels, ImageDimension projectionDirection, ImageSubSet source)
    {
        ImageSubSet toAdd = new ImageSubSet(1,1);
        toAdd.setStart(source.getStart(projectionDirection), projectionDirection);
        toAdd.setSize(source.getSize(projectionDirection),projectionDirection);

        for (ImageDimension d: ImageDimension.AllValues)
        {
            if (projectionDirection != d)
            {
                ExpandTillSize(numberPixels, toAdd, source, d);
            }
        }

        return toAdd;
    }

    private static List<ImageSubSet> CalculatePointProjectionSubset(long numberPixels, ImageDimension projectionDirection, List<ImageSubSet> source)
    {
        long already = 0;
        List<ImageSubSet> result = new ArrayList<>();
        for (ImageSubSet foo : source)
        {
            if (numberPixels + foo.getPixelSize() < numberPixels)
            {
                result.add(foo);
                already += foo.getPixelSize();
                continue;
            }

            ImageSubSet toAdd = CalculatePointProjectionSubset(numberPixels - already,projectionDirection,foo);

            already += foo.getPixelSize();
            result.add(toAdd);
        }

        return result;
    }

    private static void ExpandTillSize(long desiredPixelCount, ImageSubSet toAdd, ImageSubSet foo,ImageDimension d)
    {
        toAdd.setStart(foo.getStart(d),d);
        while(toAdd.getPixelSize() < desiredPixelCount  && toAdd.getSize(d) < foo.getSize(d))
        {
            toAdd.setSize(toAdd.getSize(d)+1,d);
        }
    }

    public static long getPixelSize(List<ImageSubSet> input)
    {
        long sum = 0;
        for(ImageSubSet s:input)
        {
            sum += s.getPixelSize();
        }
        return sum;
    }

    static List<ImageSubSet> CompactSubSets(List<ImageSubSet> input)
    {
        if (input == null)
        {    return new ArrayList<>();}

        if (input.size() == 0)
        {    return new ArrayList<>();}

        if (input.size() == 1)
            return input;

        long inputSize = SubSetFinder.getPixelSize(input);
        List<ImageSubSet> workList = new ArrayList<>(input);
        // merge inside the plain
        boolean mergedSomething = true;
        while (mergedSomething)
        {

            ImageSubSet first = null;
            ImageSubSet second = null;
            boolean foundWork = false;
            for (int i =0 ; i< workList.size() && !foundWork; i++)
            {
                first = workList.get(i);
                for (int j=1 ; j< workList.size() && !foundWork ; j++)
                {
                    second = workList.get(j);
                    foundWork = SubSetFinder.AreAdjacent(first,second) != null ;
                }
            }

            mergedSomething = foundWork;
            if (foundWork)
            {
                workList.remove(first);
                workList.remove(second);
                ImageSubSet merged = MergeSubSets(Arrays.asList(first,second));
                workList.add(merged);
            }
        }

        long outputSize = SubSetFinder.getPixelSize(workList);
        if (outputSize != inputSize)
        {
            DebugHelper.BreakIntoDebug();
        }
        return workList;
    }

    private static List<List<ImageSubSet>> CreateTuples(List<ImageSubSet> input,int tupleSize)
    {
        Set<ImageSubSet> asSet = new HashSet<>(input);
        List<List<ImageSubSet>> result = new ArrayList<>();

        for (Set<ImageSubSet> s:  Sets.powerSet(asSet))
        {
            if (s.size() == tupleSize)
            {
                result.add( new ArrayList<>(s));
            }
        }
        return result;
    }


    public static List<ImageSubSet> CompactSubSetsComplex(List<ImageSubSet> input)
    {
        List<ImageSubSet> workList = CompactSubSets(input);
        if (workList.size()<1)  {   return workList;}


        long inputSize = SubSetFinder.getPixelSize(input);
        boolean mergedSomething;
        do
        {
            mergedSomething = false;

            for (int j = input.size(); j>1 ;j--)
            {
                List<List<ImageSubSet>> allTuples = CreateTuples(workList,j);

                for (List<ImageSubSet> t : allTuples)
                {
                    ImageSubSet boundingBox = SubSetFinder.BoundingBox(t);

                    if (SubSetFinder.FillsBoundingBox(boundingBox, t))
                    {
                        workList.removeAll(t);
                        workList.add(boundingBox);
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

        long outputSize = SubSetFinder.getPixelSize(workList);
        if (outputSize != inputSize)
        {
            DebugHelper.BreakIntoDebug();
        }
        return workList;
    }

    public static long NumberOfPixels(Iterable<ImageSubSet> list)
    {
        if (list == null)
        {
            return -1;
        }

        long sum = 0;
        try
        {
            for (ImageSubSet i : list)
            {
                sum += i.getPixelSize();
            }
        }catch (Exception ex)
        {
            DebugHelper.BreakIntoDebug();
        }

        return sum;
    }

    /**
     * Create the difference of two Subs Sets
     * @param full the area to be subtracted be subtracted from
     * @param part the area being subtracted
     * @return The result, null if it could not be done for some reason
     */
    public static ImageSubSet Difference(ImageSubSet full, ImageSubSet part)
    {
        ImageSubSet result = new ImageSubSet(full);

        List<ImageDimension> differencesAlong = new ArrayList<>();
        for (ImageDimension d: ImageDimension.AllValues)
        {
            if (full.getSize(d) != part.getSize(d) || full.getStart(d) != part.getStart(d))
            {
                differencesAlong.add(d);
            }
        }

        if (differencesAlong.size()!= 1)
        {
            DebugHelper.BreakIntoDebug();
        }


        ImageDimension d = differencesAlong.get(0);

        result.setSize( full.getSize(d) -  part.getSize(d)  ,d);
        result.setStart( part.getEnd(d), d);


        return result;
    }

    public static String ListToString(List<ImageSubSet> imageAllocation)
    {
        if (imageAllocation == null) return "";
        StringBuilder builder = new StringBuilder();
        for(ImageSubSet s : imageAllocation)
        {
            builder.append(s.toString());
            builder.append("\t");
        }
        return builder.toString().trim();
    }

    public static ImageSubSet BoundingBox(List<ImageSubSet> sets)
    {
        ImageSubSet result = new ImageSubSet();
        for(ImageDimension d : ImageDimension.AllValues)
        {
            for (ImageSubSet set : sets)
            {
                if (result.getStart(d) > set.getStart(d))
                {
                    result.setStart(set.getStart(d),d);
                }

                if (result.getEnd(d) < set.getEnd(d))
                {
                    result.setEnd(set.getEnd(d),d);
                }
            }
        }
        return result;
    }

    public static boolean FillsBoundingBox(ImageSubSet boundingBox, List<ImageSubSet> subsets)
    {
        for (int t = boundingBox.StartT; t <boundingBox.SizeT ; t++)
        {
            for (int c = boundingBox.StartC; c <boundingBox.SizeC ; c++)
            {
                for (int z = boundingBox.StartZ; z <boundingBox.SizeZ ; z++)
                {
                    for (int y = boundingBox.StartY; y <boundingBox.SizeY ; y++)
                    {
                        for (int x = boundingBox.StartX; x <boundingBox.SizeX ; x++)
                        {
                            boolean found = false;
                            for (ImageSubSet s : subsets)
                            {
                                if (SubSetFinder.ContainsPoint(s,x,y,c,z,t))
                                {
                                    found = true;
                                    if (x==0 && s.getEnd(ImageDimension.X) == boundingBox.getEnd(ImageDimension.X))
                                    {
                                        x = s.getEndX();
                                        y = s.getEndY();
                                    }
                                    else
                                    {
                                        x = s.getEndX();
                                    }

                                    break;
                                }
                            }
                            if (! found)
                            {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    public static boolean SameSize(ImageSubSet s1, ImageSubSet s2)
    {
        for (ImageDimension d: ImageDimension.AllValues)
        {
            if (s1.getSize(d)!= s2.getSize(d))
            {
                return false;
            }
        }
        return true;
    }

    /***
     * Check whether two sets of subsets describe the same area.
     * @param s1 Subset 1.
     * @param s2 Subset 2.
     * @return True, if the area is the same, False otherwise.
     */
    public static boolean AreEqual(List<ImageSubSet> s1, List<ImageSubSet> s2)
    {
        if (SubSetFinder.NumberOfPixels(s1) != SubSetFinder.NumberOfPixels(s2))
        {   return false;   }

        // find individual intersection
        List<ImageSubSet> compOrig = CompactSubSetsComplex(s1);
        List<ImageSubSet> allIntersections = new ArrayList<>();
        for(ImageSubSet s : compOrig)
        {
            List<ImageSubSet> foo = Intersection(s,s2);
            allIntersections.addAll(foo);
        }

        // merge intersections
        List<ImageSubSet> comp = CompactSubSetsComplex(allIntersections);

        // if equal, the sum of all intersection should equal the original list
        List<ImageSubSet> workList = new ArrayList<>(compOrig);
        for(ImageSubSet s : compOrig)
        {
            boolean found = false;
            for(ImageSubSet otherS : comp)
            {
                if (s.equals(otherS))
                {
                    found = true;
                    break;
                }
            }

            if (found)
            {
                workList.remove(s);
            }
        }

        return workList.size() == 0;
    }
}

class SliceCacheKey
{
    final ImageSubSet bounds;
    private Collection<ImageDimension> dimensions;

    SliceCacheKey(ImageSubSet bounds, Collection<ImageDimension> dims)
    {
        this.bounds=bounds;
        this.dimensions = dims;
    }

    public Collection<ImageDimension> getDimensions()
    {
        return dimensions;
    }

    ImageSubSet getBounds()
    {
        return bounds;
    }

    @Override
    public int hashCode()
    {
        return this.bounds.StartC * 3 + this.bounds.StartY *5  + this.bounds.StartZ * 7 + this.bounds.StartT * 11  + this.bounds.StartX * 13
                + this.bounds.StartY * 17;
    }
    @Override
    public boolean equals(Object other)
    {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof SliceCacheKey))return false;

        SliceCacheKey o =((SliceCacheKey) other);

        if (!this.bounds.equals(o.bounds))
        {   return false;}

        if (this.dimensions.size() != o.dimensions.size())
        {   return false;}

        for (ImageDimension d : dimensions)
        {
            if (!o.dimensions.contains(d))
            {return false;}
        }

        return true;
    }
}
