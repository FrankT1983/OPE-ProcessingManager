package de.c3e.ProcessManager.Utils;

import com.google.common.base.Joiner;
import de.c3e.BlockTemplates.ImageDimension;
import de.c3e.ProcessManager.DataTypes.ResultGraphPosition;

import de.c3e.ProcessManager.WorkerManager.ICommunicationInfrastructure;
import de.c3e.ProcessManager.WorkerManager.IPartialResult;
import de.c3e.ProcessManager.WorkerManager.IRemoteObject;
import de.c3e.ProcessManager.WorkerManager.ISupportsMultiPass;
import de.c3e.ProcessingManager.Types.SplitType;
import icy.sequence.Sequence;
import org.slf4j.Logger;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Wraps all things the image needs into an abstraction.
 */
public class AbstractImageObject implements INeedsComInfrastructe, IPartialResult, ISupportsMultiPass, IRemoteObject, Serializable , IIsPartOfActiveCommunication, ISupportsEviction
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    private ResultGraphPosition graphPosition;

    private final List<EvictedSubBlock> evictedParts = Collections.synchronizedList(new ArrayList<EvictedSubBlock>());
    private final List<ImageSubBlock> localParts = Collections.synchronizedList(new ArrayList<ImageSubBlock>());
    private final List<RemoteImageSubBlock> remoteParts = Collections.synchronizedList(new ArrayList<RemoteImageSubBlock>());

    private ImageSubSet FullSize = new ImageSubSet();

    public  int getFullSizeX() {return this.FullSize.SizeX;}
    public  int getFullSizeY() {return this.FullSize.SizeY;}
    public  int getFullSizeZ() {return this.FullSize.SizeZ;}
    public  int getFullSizeT() {return this.FullSize.SizeT;}
    public  int getFullSizeC() {return this.FullSize.SizeC;}

    private ICommunicationInfrastructure comInfrastructure;

    // todo: refactor this out of the image, no reason why it should track this.
    public final Map<Long, List<ImageSubSet>> alreadyRequestedPixels = new HashMap<>();

    @Override
    public synchronized void AddPart(Object value)
    {
        synchronized (this.localParts){ synchronized (this.remoteParts)
        {
            if (value instanceof ImageSubBlock)
            {
                for (ImageSubBlock p : this.localParts)
                {
                    if (p.dimensions.equals(((ImageSubBlock) value).dimensions))
                    {
                        return;
                    }
                }

                this.localParts.add((ImageSubBlock) value);
                this.RemoveRemoteBlocksThatExistLocally();
                return;
            }

            if (value instanceof AbstractImageObject)
            {
                AbstractImageObject img = (AbstractImageObject) value;
                if (this.graphPosition.equals(img.graphPosition))
                {
                    this.localParts.addAll(img.getLocalPartsSubBlocks());
                    this.remoteParts.addAll(img.getRemotePartsSubBlocks());

                    this.RemoveRemoteBlocksThatExistLocally();
                }
            }
        }}
    }

    @Override
    public Object GetLocalParts(int myRank)
    {
        return this.localParts;
    }

    public List<ImageSubSet> getFullBoundsL()
    {
        List<ImageSubSet> result = new ArrayList<>();
        result.add(this.getFullBounds());
        return result;
    }

    public List<ImageSubSet> getFullBoundsAndMarkRequested(long uniqueScheduleId)
    {
        List<ImageSubSet> result = new ArrayList<>();
        result.add(this.getFullBounds());
        synchronized (this.alreadyRequestedPixels)
        {
            this.alreadyRequestedPixels.put(uniqueScheduleId, new ArrayList<>(result));
        }
        return result;
    }

    public ImageSubSet getFullBounds()
    {
        ImageSubSet result = new ImageSubSet();
        result.SizeX = this.FullSize.SizeX;
        result.SizeY = this.FullSize.SizeY;
        result.SizeZ = this.FullSize.SizeZ;
        result.SizeC = this.FullSize.SizeC;
        result.SizeT = this.FullSize.SizeT;
        return result;
    }

    public List<ImageSubSet> getLocalBoundsOfNode(int nodeId)
    {
        if (this.getWorkerId() == nodeId)
        {   return getLocalBounds();    }

        synchronized (this.remoteParts)
        {
            List<ImageSubSet> result = new ArrayList<>();
            for (RemoteImageSubBlock b : this.remoteParts)
            {
                if (b.getWorkerId() == nodeId)
                {
                    result.add(b.getDimensions());
                }
            }
            return result;
        }
    }

    public List<ImageSubSet> getLocalBounds()
    {
        synchronized (this.localParts)
        {
            List<ImageSubSet> result = new ArrayList<>();

            for (ImageSubBlock b : this.localParts)
            {
                result.add(b.dimensions);
            }
            return result;
        }
    }

    public List<ImageSubSet> getAllBounds()
    {
        synchronized (this.localParts) { synchronized (this.remoteParts)
        {
            List<ImageSubSet> result = new ArrayList<>();

            result.addAll(this.getLocalBounds());

            for (RemoteImageSubBlock b : this.remoteParts)
            {
                result.add(b.getDimensions());
            }
            return result;
        }}
    }

    public boolean HasUnrequestedPixels()
    {
        long sum = 0;
        synchronized (this.alreadyRequestedPixels)
        {
            for (Map.Entry<Long, List<ImageSubSet>> e : this.alreadyRequestedPixels.entrySet())
            {
                sum += SubSetFinder.NumberOfPixels(e.getValue());
            }
        }

        return sum < this.getFullSizePixelCount();
    }

    public void ReinterpretLocalBlocksToType(AbstractImageObject inputObject)
    {
        synchronized (this.localParts)
        {
            double minValue = inputObject.getLowesMinValue();
            double maxValue = inputObject.getBiggestMaxValue();

            for (ImageSubBlock b : this.localParts)
            {
                b.setMinValue(minValue);
                b.setMaxValue(maxValue);
            }
        }
    }

    public double getLowesMinValue()
    {
        synchronized (this.localParts)
        {
            if (this.localParts.isEmpty())
            {
                return 0;
            }
            double res = this.localParts.get(0).getMinValue();

            for (ImageSubBlock b : this.localParts)
            {
                res = Math.min(res,b.getMinValue());
            }

            return res;
        }
    }

    public double getBiggestMaxValue()
    {
        synchronized (this.localParts)
        {
            if (this.localParts.isEmpty())
            {
                return 0;
            }
            double res = this.localParts.get(0).getMaxValue();

            for (ImageSubBlock b : this.localParts)
            {
                res = Math.max(res,b.getMaxValue());
            }

            return res;
        }
    }

    public int AllSubblocksHaveSameSize(ImageDimension d)
    {
        int res = -1;

        synchronized (this.localParts)
        {
            synchronized (this.remoteParts)
            {
                if (!this.localParts.isEmpty())
                {
                    res = this.localParts.get(0).dimensions.getSize(d);
                }

                for (ImageSubBlock b : this.localParts)
                {
                    if (b.dimensions.getSize(d) != res)
                    {
                        ;
                        return -1;
                    }
                }


                if (res == -1 && !this.remoteParts.isEmpty())
                {
                    res = this.remoteParts.get(0).getDimensions().getSize(d);
                }

                for (RemoteImageSubBlock b : this.remoteParts)
                {
                    if (b.getDimensions().getSize(d) != res)
                    {
                        return -1;
                    }
                }
            }
            return res;
        }
    }

    public void setFullSize(int newSize, ImageDimension d)
    {
        this.FullSize.setSize(newSize,d);
    }

    @Override
    public void Evict()
    {
        synchronized (this.evictedParts)
        {
            synchronized (this.localParts)
            {
                List<ImageSubBlock> tmp = new ArrayList<>(this.localParts);
                for (ImageSubBlock b : tmp)
                {
                    this.evictedParts.add(EvictedSubBlock.Evict(b));
                    this.localParts.remove(b);
                }
                this.localParts.clear();
            }
        }
    }

    @Override
    public void DeEvict()
    {
        synchronized (this.evictedParts)
        {
            synchronized (this.localParts)
            {
                List<EvictedSubBlock> tmp = new ArrayList<>(this.evictedParts);
                for (EvictedSubBlock b : tmp)
                {
                    this.localParts.add(b.DeEvict());
                    this.evictedParts.remove(b);
                }
                this.evictedParts.clear();
            }
        }
    }

    @Override
    public boolean isEvicted()
    {
        synchronized (this.evictedParts)
        {
            return !this.evictedParts.isEmpty();
        }
    }

    private void RemoveRemoteBlocksThatExistLocally()
    {
        synchronized (this.localParts) { synchronized (this.remoteParts)
        {
            for (ImageSubBlock b : this.localParts)
            {
                RemoteImageSubBlock toRemove = null;
                for (RemoteImageSubBlock remote : this.remoteParts)
                {
                    if (remote.getDimensions().equals(b.dimensions))
                    {
                        toRemove = remote;
                        break;
                    }
                }

                if (toRemove != null)
                {
                    this.remoteParts.remove(toRemove);
                }
            }
        }}
    }

    @Override
    public void setCommunicationInfrastructure(ICommunicationInfrastructure infrastructure)
    {
        this.comInfrastructure = infrastructure;
    }

    public static AbstractImageObject fromSequence(Sequence sequence)
    {
        if (sequence == null)
        {   return null;    }

        AbstractImageObject result = new AbstractImageObject();
        result.InitFromSequence(sequence);
        return result;
    }

    public static AbstractImageObject fromSubBlock(ImageSubBlock block)
    {
        AbstractImageObject result = new AbstractImageObject();
        result.InitFromSubBlock(block);
        return result;
    }

    public static AbstractImageObject fromSubBlocks(List<ImageSubBlock> blocks)
    {
        AbstractImageObject result = new AbstractImageObject();
        result.localParts.addAll(blocks);
        result.UpdateBoundsToMaximumFromParts();
        return result;
    }

    public static AbstractImageObject fromSubBlocksAndTemplate(List<ImageSubBlock> blocks, AbstractImageObject temp)
    {
        AbstractImageObject result = new AbstractImageObject();
        result.InitFromSubBlocksAndTemplate(blocks, temp);
        return result;
    }

    public static AbstractImageObject fromSubBlocksAndFullBounds(List<ImageSubBlock> blocks, ImageSubSet temp)
    {
        AbstractImageObject result = new AbstractImageObject();
        result.InitFromSubBlocksAndFullBounds(blocks, temp);
        return result;
    }

    public void PullAllRemoteParts()
    {
        // local temp to prevent java.util.ConcurrentModificationException
        List<RemoteImageSubBlock> localTemp = new ArrayList<>(this.remoteParts);
        for (RemoteImageSubBlock b :localTemp)
        {
            List<ImageSubSet> intersections = SubSetFinder.Intersection(b.getDimensions(), this.getBoundsOfLocalParts());
            if (intersections.size() == 1)
            {
                if(intersections.get(0).equals(b.getDimensions()))
                {
                    // already have that data
                    this.remoteParts.remove(b);
                    continue;
                }
            }
            this.comInfrastructure.RequestPartOfPartialObject(new RemoteImagePartRequest(b));
        }

        while(this.remoteParts.size()>0)
        {
            try
            {
                // sleep while waiting for the results.
                Thread.sleep(100);
            }
            catch (Exception e)
            {   e.printStackTrace();}
        }

        this.CompactLocalBlocks(false);
    }

    public void CompactLocalBlocks(boolean complexMode)
    {
        synchronized (this.localParts)
        {
            List<ImageSubBlock> tmp = new ArrayList<>(this.localParts);
            List<ImageSubBlock> compactedBlocks = complexMode ?  ImageSubBlockUtils.CompactSubBlocksComplex(tmp) : ImageSubBlockUtils.CompactSubBlocks(tmp);
            if (compactedBlocks != null)
            {
                this.localParts.clear();
                this.localParts.addAll(compactedBlocks);
            }
        }
    }

    /***
     * Collect all remote parts needed to fill a desired Subset from the comInfrastructure.
     * @param desiredSubsets The subsets needed as local parts.
     */
    private void CollectNeededRemoteParts( List<ImageSubSet> desiredSubsets)
    {
        List<RemoteImagePartRequest> neededRemoteBlocks = new ArrayList<>();
        List<RemoteImagePartRequest> alreadyRequested = new ArrayList<>();

        long colletionStart = System.currentTimeMillis();
        do
        {
            neededRemoteBlocks.clear();
            synchronized (this.localParts){synchronized (this.remoteParts){
                // this could actually start requesting more blocks, but that should not happen
                for (ImageSubSet subSet : desiredSubsets)
                {
                    neededRemoteBlocks.addAll(ImageSubBlockUtils.CalculateNeededRemoteBlocks(subSet, this.localParts, this.remoteParts));
                }
            }   }

            if (neededRemoteBlocks.size() == 0)
            {break;}

            for (RemoteImagePartRequest b :neededRemoteBlocks)
            {
                boolean skip = false;
                for (RemoteImagePartRequest a : alreadyRequested)
                {
                    if (a.Block == b.Block && a.SubSet.equals(b.SubSet))
                    {
                        skip = true;
                    }
                }

                if (skip)
                {
                    continue;
                }

                alreadyRequested.add(b);
                this.comInfrastructure.RequestPartOfPartialObject(b);
            }

            try
            {
                // sleep while waiting for the results.
                Thread.sleep(100);
            }
            catch (Exception e)
            {   e.printStackTrace();}

            if (System.currentTimeMillis() - colletionStart  > 20 * 1000)
            {
                logger.info("Waiting for parts for more than " + 20 + " seconds: missing : " + Joiner.on(" ").join(neededRemoteBlocks));
                colletionStart += 20 * 1000;
            }
        }  while(neededRemoteBlocks.size()>0);
    }

    /**
     * Try to gather a sub image of this distributed image, the master will decide who much data the image
     *
     * @return The image, with the requested sub image as it's local parts.
     */
    public AbstractImageObject getSubImageFromMaster(List<ImageSubSet> allocatedSubsets )
    {
        if (allocatedSubsets == null || allocatedSubsets.size() == 0)
        {
            // nothing to do
            return null;
        }

        this.CollectNeededRemoteParts(allocatedSubsets);


        // construct a sub image, that only contains the part allocation => in case we have more local stuff then needed
        // => prevent double calculations
        List<ImageSubBlock> intersection = ImageSubBlockUtils.ConstructSubBlocksFromParts(allocatedSubsets,this.localParts);
        AbstractImageObject result = fromSubBlocksAndTemplate(intersection,this);
        //result.completeness = block.dimensions.getPixelSize() / (this.getFullSizePixelCount());
        result.CompactLocalBlocks(false);
        return result;
    }

    public synchronized  List<ImageSubSet> GetSubset(long numberPixels, SplitType splitType, long requesterId)
    {
        return GetSubset(numberPixels,splitType,requesterId,-1);
    }

    public synchronized  List<ImageSubSet> GetSubset(long numberPixels, SplitType splitType, long requesterId, int forNode)
    {
        synchronized (this.alreadyRequestedPixels)
        {
            // figure out which sub plane to extract
            Collection<ImageSubSet> alreadyRequested = this.alreadyRequestedPixels.containsKey(requesterId) ? new ArrayList<>(this.alreadyRequestedPixels.get(requesterId)) : new ArrayList<ImageSubSet>();
            List<ImageSubSet> desiredSubsets = this.CreateSubSet(numberPixels, alreadyRequested, splitType, forNode);

            if (desiredSubsets == null)
            {
                // noting left to do?
                return null;
            }

            if (!this.alreadyRequestedPixels.containsKey(requesterId))
            {
                this.alreadyRequestedPixels.put(requesterId,new ArrayList<ImageSubSet>());
            }

            List<ImageSubSet> already = this.alreadyRequestedPixels.get(requesterId);
            if (SubSetFinder.HasIntersection(already,desiredSubsets))
            {
                // debug helper
                DebugHelper.BreakIntoDebug();
            }

            already.addAll(desiredSubsets);
            already = SubSetFinder.CompactSubSets(already);

            this.alreadyRequestedPixels.put(requesterId, already);

            return desiredSubsets;
        }
    }

    public boolean WasCompletelyRequested(Long by)
    {
        synchronized (this.alreadyRequestedPixels)
        {
            List<ImageSubSet> s = this.alreadyRequestedPixels.get(by);
            List<ImageSubSet> allBounds = this.getAllBounds();
            logger.info("Compare a " +s.toString() + "§§§§" + allBounds.toString());

            return SubSetFinder.AreEqual(s,allBounds);
        }
    }

    public double[] getDataXYAsDoubleArray(int startX, int width, int starty, int height, int c, int z, int t)
    {
        ImageSubBlock block = this.getDataXYAsSubsetBlock(startX,width,starty,height,c,z,t);
        return TypeConversionHelper.ToDoubleArray(block.data);
    }

    public short[] getDataXYAsShortArray(int startX, int sizeX, int startY, int sizeY, int c, int z, int t)
    {
        ImageSubBlock block = this.getDataXYAsSubsetBlock(startX, sizeX, startY, sizeY, c, z, t);
        return TypeConversionHelper.ToShortArray(block.data);
    }

    public byte[] getDataXYAsByteArray(int startX, int sizeX, int startY, int sizeY, int c, int z, int t)
    {
        ImageSubBlock block = this.getDataXYAsSubsetBlock(startX,sizeX,startY,sizeY,c,z,t);
        return TypeConversionHelper.ToByteArray(block.data);
    }

    private ImageSubBlock getDataXYAsSubsetBlock(int startX, int sizeX, int startY, int sizeY, int c, int z, int t)
    {
        ImageSubSet desiredSubset = new ImageSubSet(sizeX,sizeY);
        desiredSubset.StartX= startX;
        desiredSubset.StartY= startY;
        desiredSubset.StartC = c;
        desiredSubset.StartZ = z;
        desiredSubset.StartT = t;

        desiredSubset.SizeC = 1;
        desiredSubset.SizeZ = 1;
        desiredSubset.SizeT = 1;

        return ImageSubBlockUtils.ConstructSubBlockFromParts(desiredSubset, this.localParts);
    }

    public double[] getDataXYAsDoubleArray(int c, int z, int t)
    {
        ImageSubSet desiredSubset = new ImageSubSet(this.FullSize.SizeX,this.FullSize.SizeY);
        desiredSubset.StartC = c;
        desiredSubset.StartZ = z;
        desiredSubset.StartT = t;

        desiredSubset.SizeC = 1;
        desiredSubset.SizeZ = 1;
        desiredSubset.SizeT = 1;

        ImageSubBlock block = ImageSubBlockUtils.ConstructSubBlockFromParts(desiredSubset, this.localParts);

        if (block.type.getName().equals(Double.class.getName()))
        {
            return (double[]) block.data;
        }
        else
        {
            return TypeConversionHelper.ToDoubleArray(block.data);
        }
    }

    /**
     * Create a version, where the local parts are only known with there ids, and which worker they are on.
     * @return
     */
    public AbstractImageObject CreateEmptyVersion(int workerID)
    {
        AbstractImageObject image = new AbstractImageObject();
        image.setObjectGraphPosition(this.getObjectGraphPosition());
        image.FullSize.SizeX = this.FullSize.SizeX;
        image.FullSize.SizeY = this.FullSize.SizeY;
        image.FullSize.SizeZ = this.FullSize.SizeZ;
        image.FullSize.SizeT = this.FullSize.SizeT;
        image.FullSize.SizeC = this.FullSize.SizeC;

        for (ImageSubBlock part: this.localParts)
        {
            RemoteImageSubBlock remotePart = new RemoteImageSubBlock(part.dimensions, workerID, this.getObjectGraphPosition());

            image.remoteParts.add(remotePart);
        }
        return image;
    }

    @Override
    public Object CreateShallowClone()
    {
        AbstractImageObject image = new AbstractImageObject();
        synchronized (this.localParts)
        {
            synchronized (this.remoteParts)
            {
                image.setObjectGraphPosition(this.getObjectGraphPosition());
                image.FullSize.SizeX = this.FullSize.SizeX;
                image.FullSize.SizeY = this.FullSize.SizeY;
                image.FullSize.SizeZ = this.FullSize.SizeZ;
                image.FullSize.SizeT = this.FullSize.SizeT;
                image.FullSize.SizeC = this.FullSize.SizeC;
                image.getLocalPartsSubBlocks().addAll(this.getLocalPartsSubBlocks());
                image.getRemotePartsSubBlocks().addAll(this.getRemotePartsSubBlocks());
            }
        }





        return image;
    }

    private double getOverallCompleteness()
    {
        // todo: this could technicaly not be correct. It would be better to merge the boundaries and check if
        return (this.getLocalBlockSize() + this.getRemoteBlockSize()) / (double)this.getFullSizePixelCount();
    }

    @Override
    public boolean isComplete()
    {
        double completness = getOverallCompleteness();


        if (completness < 1.0)
        {
            return false;
        }


        List<ImageSubSet> allBounds =  this.getAllBounds();
        List<ImageSubSet>  comp = SubSetFinder.CompactSubSetsComplex(allBounds);

        if (comp.size() == 1)
        {
            if (comp.get(0).equals(this.FullSize))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            if (completness == 1.0)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }


    @Override
    public ResultGraphPosition getObjectGraphPosition()
    {
        return this.graphPosition;
    }

    @Override
    public void setObjectGraphPosition(ResultGraphPosition uid)
    {
        this.graphPosition = uid;
    }

    @Override
    public int getWorkerId()
    {
        return 0;
    }

    @Override
    public Object CreateEmptyRepresentation(int workerID)
    {
        return this.CreateEmptyVersion(workerID);
    }

    public long getLocalBlockSize()
    {
        long result = 0;
        for (ImageSubBlock b : this. localParts)
        {
            result += b.dimensions.getPixelSize();
        }
        return result;
    }

    private long getRemoteBlockSize()
    {
        long result = 0;
        synchronized (this.remoteParts)
        {
            for (RemoteImageSubBlock b : this.remoteParts)
            {
                result += b.getDimensions().getPixelSize();
            }
        }
        return result;
    }

    public List<ImageSubBlock> getLocalSlices(ImageDimension[] dimensions)
    {
        synchronized (this.localParts)
        {
            List<ImageSubSet> localBounds = this.getBoundsOfLocalParts();
            List<ImageSubSet> sliceBounds = new ArrayList<>();
            for (ImageSubSet subSet : localBounds)
            {
                sliceBounds.addAll(SubSetFinder.SliceAlongDimensions(subSet,dimensions));
            }
            List<ImageSubBlock> result = new ArrayList<>();
            for (ImageSubSet slice : sliceBounds)
            {
                ImageSubBlock sliceData = ImageSubBlockUtils.ConstructSubBlockFromParts(slice,this.localParts);
                result.add(sliceData);
            }

            return result;
        }
    }



    public List<ImageSubBlock> getLocalPartsSubBlocks()
    {
        return this.localParts;
    }

    public List<RemoteImageSubBlock> getRemotePartsSubBlocks()
    {
        return this.remoteParts;
    }

    private void InitFromSubBlocksAndTemplate(List<ImageSubBlock> blocks, AbstractImageObject temp)
    {
        this.FullSize.SizeX = temp.FullSize.SizeX;
        this.FullSize.SizeY = temp.FullSize.SizeY;
        this.FullSize.SizeZ = temp.FullSize.SizeZ;
        this.FullSize.SizeT = temp.FullSize.SizeT;
        this.FullSize.SizeC = temp.FullSize.SizeC;

        this.localParts.clear();
        CheckBlocks(blocks);
        List<ImageSubBlock> compactedBlocks = ImageSubBlockUtils.CompactSubBlocks(blocks);
        CheckBlocks(compactedBlocks);
        this.localParts.addAll(compactedBlocks);
    }

    private void InitFromSubBlocksAndFullBounds(List<ImageSubBlock> blocks, ImageSubSet temp)
    {
        this.FullSize.SizeX = temp.SizeX;
        this.FullSize.SizeY = temp.SizeY;
        this.FullSize.SizeZ = temp.SizeZ;
        this.FullSize.SizeT = temp.SizeT;
        this.FullSize.SizeC = temp.SizeC;

        this.localParts.clear();
        CheckBlocks(blocks);
        List<ImageSubBlock> compactedBlocks = ImageSubBlockUtils.CompactSubBlocks(blocks);
        CheckBlocks(compactedBlocks);
        this.localParts.addAll(compactedBlocks);
    }

    private void CheckBlocks(List<ImageSubBlock> blocks)
    {
        for(ImageSubBlock b : blocks)
        {
            CheckBlock(b);
        }
    }

    private void InitFromSequence(Sequence sequence)
    {
        this.FullSize.SizeX = sequence.getSizeX();
        this.FullSize.SizeY = sequence.getSizeY();
        this.FullSize.SizeZ = sequence.getSizeZ();
        this.FullSize.SizeT = sequence.getSizeT();
        this.FullSize.SizeC = sequence.getSizeC();

        this.localParts.clear();
        this.localParts.addAll(ImageSubBlockUtils.fromSequence(sequence));
    }

    private void InitFromSubBlock(ImageSubBlock block)
    {
        this.FullSize.SizeX = block.dimensions.SizeX;
        this.FullSize.SizeY = block.dimensions.SizeY;
        this.FullSize.SizeZ = block.dimensions.SizeZ;
        this.FullSize.SizeT = block.dimensions.SizeT;
        this.FullSize.SizeC = block.dimensions.SizeC;

        this.localParts.clear();
        CheckBlock(block);
        this.localParts.add(block);
    }

    private void CheckBlock(ImageSubBlock block)
    {
        if (block.data instanceof Array)
        {
            if (Array.getLength(block.data) != block.dimensions.getPixelSize())
            {
                DebugHelper.BreakIntoDebug();
            }
        }
    }

    private List<ImageSubSet> CreateSubSet(long numberPixels, Collection<ImageSubSet> alreadyRequested, SplitType splitType, int forNode)
    {
        //this.UpdateBoundsToMaximumFromParts();
        List<ImageSubSet> availableBounds = forNode >= 0 ? this.getLocalBoundsOfNode(forNode) : this.getAllBounds();
        //return SubSetFinder.CalculateSubSet(numberPixels, alreadyRequested, splitType, this.FullSize);
        if (availableBounds.size() == 0)
        {   return new ArrayList<>(); }

        return SubSetFinder.CalculateSubSetFromList(numberPixels, alreadyRequested, splitType, availableBounds);
    }

    private void UpdateBoundsToMaximumFromParts()
    {
        synchronized (this.localParts)
        {
            synchronized (this.remoteParts)
            {
                for (ImageDimension d : ImageDimension.AllValues)
                {
                    this.FullSize.setSize(1,d);
                }

                for (ImageSubBlock l : this.localParts)
                {
                    for (ImageDimension d : ImageDimension.AllValues)
                    {
                        this.FullSize.setSize(Math.max(this.FullSize.getSize(d), l.dimensions.getSize(d) + l.dimensions.getStart(d)),d);
                    }
                }

                for (RemoteImageSubBlock l : this.remoteParts)
                {
                    for (ImageDimension d : ImageDimension.AllValues)
                    {
                        this.FullSize.setSize(Math.max(this.FullSize.getSize(d), l.getDimensions().getSize(d) + l.getDimensions().getStart(d)),d);
                    }
                }
            }
        }
    }

    public List<ImageSubSet> getBoundsOfLocalParts()
    {
        List<ImageSubSet> result = new ArrayList<>();
        for (ImageSubBlock block : this.localParts)
        {
            result.add(block.dimensions);
        }

        return result;
    }

    public  long getFullSizePixelCount()
    {
        return this.FullSize.getPixelSize();
    }

    public  long getLocalSizePixelCount()
    {
        synchronized (this.localParts)
        {
            long result = 0;
            for(ImageSubBlock block : this.localParts)
            {
                result += block.dimensions.getPixelSize();
            }

            return result;
        }
    }

    public  long getLocalSizePixelCount(int onWorker)
    {
        long result = 0;

        synchronized (this.remoteParts)
        {

            for(RemoteImageSubBlock block : this.remoteParts)
            {
                if (block.getWorkerId()== onWorker)
                {
                    result += block.getDimensions().getPixelSize();
                }
            }
        }

        return result;
    }

    @Override
    public boolean NeedsMorePasses()
    {
        // todo: not the safest way, but should work for now
        return getOverallCompleteness()> 1.0;
    }


}

