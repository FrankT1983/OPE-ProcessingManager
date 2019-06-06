package de.c3e.ProcessManager.Utils;

import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Frank on 12.07.2016.
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public class TypeConversionHelper
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static Double TryGetDouble(Object input)
    {
        if (input instanceof Double )
        {
            return (Double)input;
        }

        return new Double(input.toString());
    }

    public static double[] ToDoubleArray(Object data)
    {
        if (data instanceof byte[])
        {
            return ByteToDoubleArray((byte[])data);
        }

        if (data instanceof short[])
        {
            return ShortToDoubleArray((short[])data);
        }

        if (data instanceof float[])
        {
            return FloatToDoubleArray((float[])data);
        }

        if (data instanceof double[])
        {
            return (double[])data;
        }

        DebugHelper.BreakIntoDebug();

        return null;
    }



    public static byte[] ToByteArray(Object data)
    {
        if (data instanceof byte[])
        {
            return (byte[])data;
        }

        if (data instanceof float[])
        {
            return FloatToByteArray((float[])data);
        }

        if (data instanceof double[])
        {
            return DoubleToByteArray((double[])data);
        }

        DebugHelper.BreakIntoDebug();
        return null;
    }

    public static short[] ToShortArray(Object data)
    {
        if (data instanceof short[])
        {
            return (short[])data;
        }

        if (data instanceof float[])
        {
            return FloatToShortArray((float[])data);
        }

        if (data instanceof double[])
        {
            return DoubleToShortArray((double[])data);
        }

        DebugHelper.BreakIntoDebug();
        return null;
    }

    private static short[] DoubleToShortArray(double[] data)
    {
        short[] result = new short[data.length];
        for( int i = 0 ; i <data.length;i++ )
        {
            result[i] = (short)data[i];
        }

        return result;
    }

    private static short[] FloatToShortArray(float[] data)
    {
        short[] result = new short[data.length];
        for( int i = 0 ; i <data.length;i++ )
        {
            result[i] = (short)data[i];
        }

        return result;
    }

    private static byte[] DoubleToByteArray(double[] data)
    {
        byte[] result = new byte[data.length];
        for( int i = 0 ; i <data.length;i++ )
        {
            result[i] = (byte)data[i];
        }

        return result;
    }

    private static byte[] FloatToByteArray(float[] data)
    {
        byte[] result = new byte[data.length];
        for( int i = 0 ; i <data.length;i++ )
        {
            result[i] = (byte)data[i];
        }

        return result;
    }


    private static double[] FloatToDoubleArray(float[] data)
    {
        double[] result = new double[data.length];
        for( int i = 0 ; i <data.length;i++ )
        {
            result[i] = (double)data[i];
        }

        return result;
    }

    private static double[] ByteToDoubleArray(byte[] data)
    {
        try
        {
            double[] result = new double[data.length];
            for (int i = 0; i < data.length; i++)
            {
                result[i] = (double) data[i];
            }

            return result;
        }catch (Exception e)
        {
            DebugHelper.BreakIntoDebug();
            return null;
        }
    }

    private static double[] ShortToDoubleArray(short[] data)
    {
        try
        {
            double[] result = new double[data.length];
            for (int i = 0; i < data.length; i++)
            {
                result[i] = data[i];
            }

            return result;
        }catch (Exception e)
        {
            DebugHelper.BreakIntoDebug();
            return null;
        }
    }

    public static Object CastToCorrectType1D(Class targetClass,  Object dataPoint)
    {
        return CastToCorrectType1D(targetClass, ReflectionHelper.getBaseElementType(dataPoint), dataPoint);
    }


    public static Object CastToCorrectType1D(Class targetClass, Class sourceClass, Object dataPoint)
    {
        int sourceLength = Array.getLength(dataPoint);
        Object result = Array.newInstance(targetClass,sourceLength);

        // check some common cases, where I can just copy
        if (sourceClass.equals(targetClass))
        {
            System.arraycopy(dataPoint,0,result,0,sourceLength);
            return result;
        }

        logger.info("Fallback to slow point wise conversion from " + sourceClass + " to " + targetClass);

        for (int i =0; i < sourceLength;i++)
        {
            Array.set(result,i, CastToCorrectType(targetClass,sourceClass,Array.get(dataPoint,i)) );
        }

        return result;
    }

    public static Object CastToCorrectType(Class targetClass, Class sourceClass, Object dataPoint)
    {
        if (sourceClass.equals(targetClass))
        {return dataPoint;}

        if ((double.class.equals(sourceClass)) || (Double.class.equals(sourceClass)))
        {
            if ((double.class.equals(targetClass)) || (Double.class.equals(targetClass)))
            {
                return dataPoint;
            }
            double source = (double) dataPoint;
            if ((Byte.class.equals(targetClass)) || (byte.class.equals(targetClass)))
            {
                return (byte)source;
            }

            if ((Short.class.equals(targetClass)) || (short.class.equals(targetClass)))
            {
                return (short)source;
            }

            if ((Float.class.equals(targetClass)) || (float.class.equals(targetClass)))
            {
                return (float)source;
            }

            if (Integer.class.equals(targetClass)|| (int.class.equals(targetClass)))
            {
                return (int) source;
            }
        }else
        if (String.class.equals(sourceClass))
        {
            String dp = (String)dataPoint;
            if ((Byte.class.equals(targetClass)) ||  (byte.class.equals(targetClass)))
            {
                return new Byte(dp);
            }
            if ((Integer.class.equals(targetClass)) ||  (int.class.equals(targetClass)))
            {
                return  new Integer(dp);
            }
            if ((Double.class.equals(targetClass)) ||  (double.class.equals(targetClass)))
            {
                return new Double(dp);
            }
        }else
        if ((Byte.class.equals(sourceClass)) || (byte.class.equals(sourceClass)))
        {
            Byte dp = (byte)dataPoint;
            if (double.class.equals(targetClass) || Double.class.equals(targetClass))
            {
                return dp.doubleValue();
            }
            if (byte.class.equals(targetClass) || Byte.class.equals(targetClass))
            {
                return dp;
            }
            if ((Integer.class.equals(targetClass)) ||  (int.class.equals(targetClass)))
            {
                return  new Integer(dp);
            }
            if ((Float.class.equals(targetClass)) || (float.class.equals(targetClass)))
            {
                return (float)dp;
            }
        }else
        if (Integer.class.equals(sourceClass))
        {
            Integer dp = (Integer)dataPoint;
            if ((Double.class.equals(targetClass)) ||  (double.class.equals(targetClass)))
            {
                return dp.doubleValue();
            }
            if (byte.class.equals(targetClass) || Byte.class.equals(targetClass))
            {
                return dp.byteValue();
            }
            if ((float.class.equals(targetClass)) || (Float.class.equals(targetClass)))
            {
                return dp.floatValue();
            }
            if ((Short.class.equals(targetClass)) || (short.class.equals(targetClass)))
            {
                return  dp.shortValue();
            }
            if ((Integer.class.equals(targetClass)) ||  (int.class.equals(targetClass)))
            {
                return dp;
            }
        }else
        if ((float.class.equals(sourceClass)) || (Float.class.equals(sourceClass)))
        {
            if ((float.class.equals(targetClass)) || (Float.class.equals(targetClass)))
            {
                return dataPoint;
            }
            Float dp = (float)dataPoint;

            if ((double.class.equals(targetClass)) || (Double.class.equals(targetClass)))
            {
                return (double)dp;
            }

            if (byte.class.equals(targetClass) || Byte.class.equals(targetClass))
            {
                return dp.byteValue();
            }
            if ((Short.class.equals(targetClass)) || (short.class.equals(targetClass)))
            {
                return  (short)((double)dp);
            }
        }


        // todo: reflection fallback ?

        DebugHelper.BreakIntoDebug();   // missing a case => could do it via strings
        return  null;
    }

    public static double[][] Reshape(double[] data, int size1, int size2)
    {
        double[][] data2d = new double[size1][];
        for (int y = 0; y < size1;y++)
        {
            data2d[y] = Arrays.copyOfRange(data,y * size2, (y+1) * size2);
        }

        return data2d;
    }

    public static Object Reshape(Object linearData, int[] sizes)
    {
        Class type = linearData.getClass().getComponentType();
        Object result = Array.newInstance(type,sizes);
        List<Object> subArrays = ToListOfSubArrays(result);

        int current = 0;
        for (int i=0;i<subArrays.size();i++)
        {
            Object current1DSubArray = subArrays.get(i);
            int toCopy = Array.getLength(current1DSubArray);
            System.arraycopy(linearData,current,current1DSubArray,0,toCopy);
            current += toCopy;
        }

        return result;
    }

    /*** Convert a multidimensional array into a list of its final 1d elements     */
    private static List<Object> ToListOfSubArrays(Object data)
    {
        List<Object> result =  new ArrayList<>();
        if (data.getClass().isArray())
        {
            int l = Array.getLength(data);
            if (l== 0)
            {   return result;  }

            Object firstElement = Array.get(data,0);
            if ((firstElement != null) && (firstElement.getClass().isArray()))
            {
                // nesting goes deeper
                for(int i=0;i<l;i++)
                {
                    result.addAll(ToListOfSubArrays( Array.get(data,i)))  ;
                }
            }
            else
            {
                // this is the final layer
                result.add(data);
            }
            return result;
        }
        // should never happen
        DebugHelper.BreakIntoDebug();
        return  null;
    }

    static public int GetCompleteSizeOfNonJagged(Object input)
    {
        if (input == null)
        {   return 1;   }   // for example Integer arrays from Array.newInstance are fillde with nulls

        if (input.getClass().isArray())
        {
            int result=1;
            int l = Array.getLength(input);
            if (l>0)
            {
                result = GetCompleteSizeOfNonJagged(Array.get(input,0)) * l;
            }
            return result;
        }
        else
        {
            return 1;
        }
    }

    // todo: code generation to build all these functions for native types
    private static double[] Flatten(double[][] data)
    {
        int elements = getOverallElementCount(data);
        double[] result = new double[elements];

        int i=0;
        for (int j=0;j<data.length;j++)
        {
            System.arraycopy(data[j], 0, result, i, data[j].length);
            i+= data[j].length;
        }
        return result;
    }

    private static float[] Flatten(float[][] data)
    {
        int elements = getOverallElementCount(data);
        float[] result = new float[elements];

        int i=0;
        for (int j=0;j<data.length;j++)
        {
            System.arraycopy(data[j], 0, result, i, data[j].length);
            i+= data[j].length;
        }
        return result;
    }

    private static byte[] Flatten(byte[][] data)
    {
        int elements = getOverallElementCount(data);
        byte[] result = new byte[elements];

        int i=0;
        for (int j=0;j<data.length;j++)
        {
            System.arraycopy(data[j], 0, result, i, data[j].length);
            i+= data[j].length;
        }
        return result;
    }

    private static int getOverallElementCount(byte[][] data)
    {
        int res = 0;
        for (int j=0;j<data.length;j++)
        {
            res += data[j].length;
        }
        return res;
    }

    private static int getOverallElementCount(float[][] data)
    {
        int res = 0;
        for (int j=0;j<data.length;j++)
        {
            res += data[j].length;
        }
        return res;
    }


    private static int getOverallElementCount(double[][] data)
    {
        int res = 0;
        for (int j=0;j<data.length;j++)
        {
            res += data[j].length;
        }
        return res;
    }



    public static Object Flatten(Object data)
    {
        if (data instanceof byte[][]){  return Flatten((byte[][])data);}
        else if (data instanceof double[][]){   return Flatten((double[][])data);}
        else if (data instanceof float[][]){    return Flatten((float[][])data);}
        else
        {
            return genericFlatten(data).toArray();
        }
    }

    private static List<Object> genericFlatten(Object data)
    {
        List<Object> result = new ArrayList<>();
        if (data.getClass().isArray())
        {
            int l = Array.getLength(data);
            if (l== 0)
            {
                return result;
            }

            Object firstElement = Array.get(data,0);
            if ((firstElement != null) && (firstElement.getClass().isArray()))
            {
                // nesting goes deeper
                for(int i=0;i<l;i++)
                {
                    result.addAll(genericFlatten(Array.get(data,i)))  ;
                }
            }
            else
            {
                // this is the final layer
                for(int i=0;i<l;i++)
                {
                    result.add(Array.get(data,i));
                }
            }
        }
        else
        {
            result.add( data);
        }
        return result;
    }

    public static Object CopyArray(Object data)
    {
        if (data instanceof double[]){   return Arrays.copyOf((double[])data,((double[])data).length);}
        else if (data instanceof byte[]){   return  Arrays.copyOf((byte[])data,((byte[])data).length);}
        else if (data instanceof short[]){   return  Arrays.copyOf((short[])data,((short[])data).length);}

        DebugHelper.BreakIntoDebug();
        return null;
    }
}

