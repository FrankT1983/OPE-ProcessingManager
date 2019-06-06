package de.c3e.ProcessManager.BlockRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * A block to use the same parameter for replicating a parameter inside the block graph.
 */
public class SameParameter extends LoggingWorkBlockBase
{
    final String input = "parameter";
    final String output = "parameter";

    private Object inputObject;

    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        if (inputs.containsKey(input))
        {
            this.inputObject = inputs.get(input);
        }
    }

    @Override
    public boolean RunWork()
    {
        // does nothing, is created with ready outputs.
        return super.RunWork();
    }

    @Override
    public Map<String, Object> GetResults()
    {
        Map<String, Object> result = new HashMap<>();
        result.put(output,this.inputObject);
        return result;
    }

    @Override
    public boolean IsFinished()
    {
        return true;
    }
}

