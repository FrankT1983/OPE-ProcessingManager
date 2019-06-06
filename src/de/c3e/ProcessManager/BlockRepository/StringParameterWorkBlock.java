package de.c3e.ProcessManager.BlockRepository;

import java.util.HashMap;
import java.util.Map;

/**
 *  Worker block for the input of a long parameter.
 */
public class StringParameterWorkBlock extends LoggingWorkBlockBase
{
    public static final String typeName = "StringParameter";
    public static final String outputName = "Value";


    @Override
    public void SetInputs(Map<String, Object> map)
    {

    }

    @Override
    public boolean RunWork()
    {
        // does nothing, is created with ready outputs.
        super.RunWork();
        return true;
    }

    @Override
    public Map<String, Object> GetResults()
    {
        // does nothing, is created with ready outputs.
        return new HashMap<>();
    }

    @Override
    public boolean IsFinished()
    {        return true;    }
}
