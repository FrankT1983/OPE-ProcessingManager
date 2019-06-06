package de.c3e.ProcessManager.Utils;


import de.c3e.ProcessManager.DataTypes.UnBoxedIcyRoi;
import de.c3e.ProcessManager.DataTypes.UnBoxedWorkBook;
import icy.roi.ROI;
import icy.sequence.Sequence;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plugins.adufour.vars.lang.*;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Helper for boxing and unboxing of icy types.
 */
public class IcyHelpers
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static Object UnboxIcyVar(Object input)
    {
        if(input instanceof VarSequence)
        {
            Sequence seq = ((VarSequence) input).getValue();
            return AbstractImageObject.fromSequence(seq);

        } else if (input instanceof Var<?>)
        {
            Object value =  ((Var<?>)input).getValue();
            if (value instanceof ROI[])
            {
                return new UnBoxedIcyRoi(( ROI[])value);
            }

            if (value instanceof Workbook)
            {
                return new UnBoxedWorkBook((Workbook)value);
            }


            return value;

        }

        return null;
    }

    public static boolean BoxAndSetToIcyVar(Var<?> var, Object input)
    {
        if ((var instanceof VarInteger))
        {
            Integer value = null;
            if (input instanceof Integer)
            {
                value = (Integer) input;
            }
            else if (input instanceof String)
            {
                try
                {
                    value = Integer.parseInt((String)input);
                }
                catch (Exception e)
                {
                    return false;
                }
            }
            ((VarInteger)var).setValue(value);
            return true;
        }
        else if ( var instanceof VarSequence)
        {
            Sequence seq = ImageConversion.TryConvertImageToIcySequence(input);
            ((VarSequence)var).setValue(seq);
            return true;
        }
        else if (var instanceof VarBoolean)
        {
            if(input instanceof String)
            {
                ((VarBoolean)var).setValue( ((String) input).equalsIgnoreCase("true"));
            }
            else
            {
                ((VarBoolean)var).setValue( (boolean) input);
            }
            return true;
        }
        else if (var instanceof VarDouble)
        {
            if(input instanceof String)
            {
                try
                {
                    double value = Double.parseDouble((String)input);
                    ((VarDouble)var).setValue(value);

                }
                catch (Exception e)
                {
                    return false;
                }
            }
            else
            {
                ((VarDouble)var).setValue((double) input);
            }
            return true;
        }
        else  if (var instanceof VarDoubleArrayNative)
        {
            if (input instanceof double[])
            {
                ((VarDoubleArrayNative)var).setValue((double[])input);
                return true;
            }
            else if (input instanceof Double )
            {
                double[] array = new double[1];
                array[0] = (Double)input;
                ((VarDoubleArrayNative)var).setValue(array);
                return true;
            }
            else if (input instanceof Integer)
            {
                double[] array = new double[1];
                array[0] = ((Integer) input).doubleValue();
                ((VarDoubleArrayNative)var).setValue(array);
                return true;
            }
        }
        else
        {
            // try reelection to get the method
            try
            {
                Method setMethod = var.getClass().getMethod("setValue", input.getClass());
                setMethod.invoke(var,input);
                return true;
            }
            catch (Exception e)
            {
                logger.error("Could not Box value : " + e.toString() );
            }
        }
        return false;
    }
}
