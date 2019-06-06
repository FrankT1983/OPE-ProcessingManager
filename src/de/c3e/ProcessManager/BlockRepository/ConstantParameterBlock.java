package de.c3e.ProcessManager.BlockRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for introducing constants into workflows
 */
public class ConstantParameterBlock extends LoggingWorkBlockBase
{
    final String input = "Value";
    final String output = "Value";

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
