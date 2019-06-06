package de.c3e.Tests;

import de.c3e.ProcessManager.Utils.DebugHelper;
import de.c3e.ProcessManager.Utils.TypeConversionHelper;
import icy.type.collection.array.DynamicArray;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by Frank on 12.07.2016.
 */
public class TypConversionTest
{
    @Test
    public void TestDoubleConversion()
    {
        Double desired = new Double(2.0);

        Double d;
        d = TypeConversionHelper.TryGetDouble( 2.0);
        assertEquals(desired,d);

        d = TypeConversionHelper.TryGetDouble(2);
        assertEquals(desired,d);

        d = TypeConversionHelper.TryGetDouble("2");
        assertEquals(desired,d);
    }


    @Test
    public void TestCasting()
    {
        Byte res = (Byte)TypeConversionHelper.CastToCorrectType(Byte.class,String.class,"12");
        Assert.assertNotNull(res);
        Assert.assertEquals(new Byte((byte)12),res);
    }


    @Test
    public void TestGenericSize()
    {
        Object foo = Array.newInstance(byte.class,1,2,3);
        int size = TypeConversionHelper.GetCompleteSizeOfNonJagged(foo);
        Assert.assertEquals(1*2*3,size);

        foo = Array.newInstance(Integer.class,8,3,4);
        size = TypeConversionHelper.GetCompleteSizeOfNonJagged(foo);
        Assert.assertEquals(8*3*4,size);
    }
}
