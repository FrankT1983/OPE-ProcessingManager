package de.c3e.Tests;

import de.c3e.ProcessManager.DataTypes.BlockGraph;
import de.c3e.ProcessManager.Main;
import de.c3e.ProcessManager.Utils.AbstractImageObject;
import de.c3e.ProcessManager.Utils.SerializeDeserializeHelper;
import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the implementation with transport layer in combination with icy blocks
 */
public class MultipleWithIcy
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void RunTestForNumberOfClients()
    {
        int sizeX = 5;
        int sizeY = 5;
        int X = 1;
        int Y = 2;

        Sequence image = new Sequence();
        IcyBufferedImage icy_img = new IcyBufferedImage(sizeX, sizeY, 1, DataType.BYTE);
        byte[] inputData = new byte[sizeX * sizeY];
        for (byte i = 0; i < inputData.length; i++)
        {
            inputData[i] = i;
        }
        Array1DUtil.byteArrayToSafeArray(inputData, icy_img.getDataXY(0), icy_img.isSignedDataType(), icy_img.isSignedDataType());
        image.setImage(0, 0, icy_img);

        File tempfile = null;
        try
        {
            tempfile = File.createTempFile("UnitTestResult", "foo");
            tempfile.deleteOnExit();
        } catch (Exception e)
        {
            assertTrue(false);
            e.printStackTrace();
        }

        int threashhold = 10;

        // run test
        final BlockGraph graph = BlockTest.ConstructAddXWithThresholdResultsListGraph(X, Y, AbstractImageObject.fromSequence(image), "DelayedAddXYBlock", tempfile.getAbsolutePath(),threashhold);
        MultipleInstancesTest.RunGraphOnClients(4,graph);

        assertNotNull(graph);
        // this only works, because the worker is a local thread
        Object result = SerializeDeserializeHelper.ObjectFromFile(tempfile.getAbsolutePath());
        assertNotNull(result);
        assertTrue(result instanceof AbstractImageObject);

        AbstractImageObject resultImg = (AbstractImageObject)result;

        double[] resultData = resultImg.getDataXYAsDoubleArray(0,0,0);
        assertEquals(inputData.length, resultData.length);
        for (int i =0;i< inputData.length; i++)
        {
            assertEquals(inputData[i] + X + Y >= threashhold ? 1.0 : 0.0 , resultData[i] , 0.0001);
        }

    }

}

