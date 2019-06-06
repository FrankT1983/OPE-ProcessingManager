package de.c3e.ProcessManager.BlockRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker block for the input of a long parameter.
 */
public class LongParameterWorkBlock extends LoggingWorkBlockBase
{
    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        // has no inputs
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
        // does nothing, is created with ready outputs.
        return new HashMap<>();
    }

    @Override
    public boolean IsFinished()
    {
        return true;
    }
}
