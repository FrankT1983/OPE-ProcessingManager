package de.c3e.ProcessManager.BlockRepository;


import java.util.HashMap;
import java.util.Map;

/**
 * Invert input data.
 */
public class InvertWorkBlock extends LoggingWorkBlockBase
{
    public static  final String Input1Name= "Input";

    // Inputs: Input
    // Outputs: invertedImage

    private Object inputObject;
    protected Object invertObject;

    @Override
    public void SetInputs(Map<String, Object> inputs)
    {
        if (inputs.containsKey(Input1Name))
        {
            this.inputObject  = inputs.get(Input1Name);
        }
    }

    @Override
    public boolean RunWork()
    {
        super.RunWork();

        if (this.invertObject instanceof int[])
        {
            int[] array =(int[])this.inputObject;
            this.invertObject = new int[array.length];

            for (int i=0;i<array.length;i++)
            {
                ((int[])this.invertObject)[i] = (255-array[i] ) % 256;
            }
        }

        return true;
    }


    @Override
    public Map<String, Object> GetResults()
    {
        Map<String, Object> result = new HashMap<>();
        result.put("invertedImage",this.invertObject);
        return result;
    }

    @Override
    public boolean IsFinished()
    {
        return true;
    }
}
