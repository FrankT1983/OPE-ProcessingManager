package de.c3e.ProcessManager.BlockRepository;

import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.Utils.SerializeDeserializeHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Frank on 28.07.2016.
 */
public class CollectResultsWorkBlock extends LoggingWorkBlockBase
{
    public static final String ResultFilePath = "ResultsDestination";
    public static final String ResultInput = "Results";
    public static final String typeName = "CollectResultsWorkBlock";

    String resultDest;
    Object resultToSave;

    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        if (inputs.containsKey(ResultFilePath))
        {
            this.resultDest = (String) inputs.get(ResultFilePath);
        }

        if (inputs.containsKey(ResultInput))
        {
            this.resultToSave = inputs.get(ResultInput);
        }
    }

    @Override
    public boolean RunWork()
    {
        super.RunWork();

        System.out.println("Pull all parts");

        if (this.resultToSave instanceof AbstractImageObject)
        {
            ((AbstractImageObject) this.resultToSave).PullAllRemoteParts();
            // remove from infrastructure. Or else this will try to serialize the infrastructure
            ((AbstractImageObject) this.resultToSave).setCommunicationInfrastructure(null);

        }
        SerializeDeserializeHelper.ObjectToFile((Serializable)this.resultToSave , this.resultDest);
        return true;
    }

    @Override
    public Map<String, Object> GetResults()
    {
        return new HashMap();
    }

    @Override
    public boolean IsFinished()
    {
        return true;
    }
}
