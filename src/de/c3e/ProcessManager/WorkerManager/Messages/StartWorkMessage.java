package de.c3e.ProcessManager.WorkerManager.Messages;

import de.c3e.ProcessManager.WorkerManager.PartialWorkRequest;

import java.util.Map;

/**
 * Created by Frank on 26.07.2016.
 */
public class StartWorkMessage extends MessageBase
{
    private final Map<String, Object> inputs;
    private final PartialWorkRequest howToSplit;
    private final String workType;
    private final long schedulingId;
    private final String blockId;

    public StartWorkMessage(Map<String, Object> inputs, long schedulingId,String blockId , PartialWorkRequest howToSplit, String workType)
    {
        this.inputs = inputs;
        this.howToSplit = howToSplit;
        this.workType = workType;
        this.schedulingId = schedulingId;
        this.blockId = blockId;
    }

    public Map<String, Object> getInputs()
    {
        return inputs;
    }

    public PartialWorkRequest getHowToSplit()
    {
        return howToSplit;
    }

    public String getWorkType()
    {
        return workType;
    }

    public long getSchedulingId()
    {
        return schedulingId;
    }

    public String getBlockID()
    {
        return this.blockId;
    }

    @Override
    public String toString()
    {
        return "StartWorkMessage : " + this.workType + "BlockId " + this.blockId + " SchedulingID " + this.schedulingId + " HowToSplit " + this.howToSplit + " inputs " + inputs;
    }
}
