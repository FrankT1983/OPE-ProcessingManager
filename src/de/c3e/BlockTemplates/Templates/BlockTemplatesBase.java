package de.c3e.BlockTemplates.Templates;


import de.c3e.BlockTemplates.Templates.Annotations.InputParameter;
import de.c3e.BlockTemplates.Templates.Annotations.InputParameterDeclarations;
import de.c3e.ProcessManager.Utils.LogUtilities;
import de.c3e.ProcessManager.Utils.TypeConversionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * Base class for all templated blocks.
 */
public abstract class BlockTemplatesBase
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    protected final Map<String, Object> Inputs = new HashMap<>();
    private final Map<String, Double> ConvertedDoubleInputs = new HashMap<>();

    public Double getDoubleInput(String id)
    {
        Double dataInCorrectType = this.ConvertedDoubleInputs.get(id);
        if (dataInCorrectType != null)
        {
            return dataInCorrectType;
        }

        if (!this.Inputs.containsKey(id))
        {
            logger.error("Tried to access parameter that did not exist " + id);
        }

        Object data = this.Inputs.get(id);
        Double converted = TypeConversionHelper.TryGetDouble(data);
        this.ConvertedDoubleInputs.put(id, converted);
        return converted;
    }


    public void SetInputValue(String input, Object o)
    {
        this.Inputs.put(input,o);
    }


    protected static List<InputParameter> GetInputAnnotations(Class blockclass)
    {
        List<InputParameter> result = new ArrayList<>();
        if (blockclass.isAnnotationPresent(InputParameterDeclarations.class))
        {
            InputParameterDeclarations annotation = (InputParameterDeclarations) blockclass.getAnnotation(InputParameterDeclarations.class);
            result.addAll(Arrays.asList(annotation.value()));
        }

        if (blockclass.isAnnotationPresent(InputParameter.class))
        {
            result.add((InputParameter)blockclass.getAnnotation(InputParameter.class));
        }

        return result;
    }

    protected List<InputParameter> GetInputsFromAttributes(Object implementation)
    {
        return GetInputAnnotations(implementation.getClass());
    }

    static public void setInputsForAttributes(BlockTemplatesBase block, List<InputParameter> inputParameters, Map<String, Object> inputs)
    {
        for (InputParameter input : inputParameters)
        {
            for (Map.Entry<String, Object> e : inputs.entrySet())
            {
                if (input.Name().equals(e.getKey()))
                {
                    Object data = e.getValue();
                    block.SetInputValue(input.Name(),TypeConversionHelper.CastToCorrectType(input.Typ(),data.getClass(),data));
                    break;
                }
            }
        }
    }
}
