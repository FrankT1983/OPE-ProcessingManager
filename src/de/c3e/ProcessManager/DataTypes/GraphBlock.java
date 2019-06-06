package de.c3e.ProcessManager.DataTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A block inside the graph. Representing one operation to be performed.
 */
public class GraphBlock
{
    public String Id = "";
    public String Type = "";

    public List<BlockIO> Inputs = new ArrayList<>();
    public List<BlockIO> Outputs = new ArrayList<>();


    // todo: refactor this into separate class => this does not belong here
    private BlockStatus Status = BlockStatus.Ready;
    public int workPartsScheduled = 0;

    /**
     * The Unique scheduling id is used to identify blocks inside the execution.
     * This is particularly needed for Blocks that can loop (such as projections) as
     * they will be executed multiple time requiring multiple scheduling ids to differentiate the results.
     */
    public long uniqueScheduleId = -1;

    public long Itteration = 0;

    public synchronized void AddResults(Map<String, Object> results)
    {
        for (BlockIO output : this.Outputs)
        {
            // have to check name or id, because icy uses names in bocks, but id's for links
            if (results.containsKey(output.Id))
            {
                output.SetValue(results.get(output.Id));
            }else if( results.containsKey(output.Name) )
            {
                output.SetValue(results.get(output.Name));
            }
        }
    }

    public BlockIO InputByName(String inputName)
    {
        for(BlockIO input : this.Inputs)
        {
            if (input.Name != null && input.Name.equals(inputName) )
            {
                return input;
            }

            if (input.Id != null && input.Id.equals(inputName) )
            {
                return input;
            }
        }

        return null;
    }

    public BlockIO OutputByName(String outputName)
    {
        for(BlockIO output : this.Outputs)
        {
            if (output.Name != null && output.Name.equals(outputName) )
            {
                return output;
            }

            if (output.Id != null && output.Id.equals(outputName) )
            {
                return output;
            }
        }

        return null;
    }

    public BlockStatus getStatus()
    {
        return Status;
    }

    public void setStatus(BlockStatus status)
    {
        this.Status = status;
    }

    public String GenerateBlockID()
    {
        return this.Id + " " + this.Type + (this.Itteration>0?" (Iteration "+ this.Itteration+")" :"");
    }

    @Override
    public String toString()
    {
        return this.Type + " : " + this.Status;
    }
}

