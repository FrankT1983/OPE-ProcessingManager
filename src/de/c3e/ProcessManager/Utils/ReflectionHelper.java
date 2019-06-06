package de.c3e.ProcessManager.Utils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionHelper
{
    public  static List<Method> getMethodWithName(String methodName, Object obj)
    {
        List<Method> result = new ArrayList<>();
        Method[] methods = obj.getClass().getMethods();
        for (Method m : methods)
        {
            if (m.getName().equals(methodName))
            {
                result.add(m);

            }
        }
        return result;
    }



    public static Method GetMethodForType(List<Method> methodList, Class<?> aClass, Class<?> aClass1)
    {
        for (Method m : methodList)
        {
            Class<?>[] types = m.getParameterTypes();

            if (types.length>1)
            {
                if (types[0].equals(aClass) && types[1].equals(aClass1))
                {
                    return m;
                }
            }
        }

        return null;
    }

    public static Class<?> getBaseElementType(Class<?> aClass)
    {
        if (aClass.isArray())
        {
            return getBaseElementType(aClass.getComponentType());
        }
        else
        {
            return aClass;
        }
    }

    public static Method GetMethodForElementType(List<Method> methodList, Class<?> aClass, Class<?> bClass)
    {
        Class<?> baseA = getBaseElementType(aClass);
        Class<?> baseB = getBaseElementType(bClass);

        for (Method m : methodList)
        {
            Class<?>[] types = m.getParameterTypes();

            if (types.length>1)
            {
                Class<?> methodBaseA = getBaseElementType(types[0]);
                Class<?> methodBaseB = getBaseElementType(types[1]);
                if (methodBaseA.equals(baseA) && methodBaseB.equals(baseB))
                {
                    return m;
                }
            }
        }

        return null;
    }

    public static Method GetMethodForElementType(List<Method> methodList, Class<?> aClass)
    {
        Class<?> baseA = getBaseElementType(aClass);

        for (Method m : methodList)
        {
            Class<?>[] types = m.getParameterTypes();

            if (types.length>0)
            {
                Class<?> methodBaseA = getBaseElementType(types[0]);
                if (methodBaseA.equals(baseA) )
                {
                    return m;
                }
            }
        }

        return null;
    }

    public static Class getBaseElementType(Object dataPoint)
    {
        if (dataPoint.getClass().isArray())
        {
            if (Array.getLength(dataPoint) > 0)
            {
                return getBaseElementType(Array.get(dataPoint,0));
            }
            else
            {
                return getBaseElementType(dataPoint.getClass());
            }

        }
        else
        {
            return dataPoint.getClass();
        }
    }
}
