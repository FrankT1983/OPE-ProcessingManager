package de.c3e.Tests.SystemTest.NonSizeChangingPlugins;

import de.c3e.ProcessManager.Main;
import de.c3e.Tests.MultipleInstancesTest;
import de.c3e.Tests.TestTools;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class TestPlaneToMaxXZ
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void PlaneToMaxXZ_Test()
    {
        int sizeX=5;
        int sizeY=5;
        int sizeC=1;
        int sizeZ=3;
        int planeSize = sizeX * sizeY;
        byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ];
        int current = 0;
        for (byte z = 0; z <sizeZ ; z++)
        {
            for (byte i = 0; i < sizeX * sizeY * sizeC; i++)
            {
                inputData[current] = (byte)(i + z * 10);
                current++;
            }
        }

        File inputFile = null;
        File outputFile= null;
        try
        {
            inputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile = File.createTempFile("UnitTest", ".tiff");
            inputFile.deleteOnExit();
            outputFile.deleteOnExit();
            TestTools.WriteImageToFile(inputFile,inputData,sizeX,sizeY,sizeC, sizeZ);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.PlaneToMaxXZ" );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        for (byte z = 0; z <sizeZ ; z++)
        {
            double[] resultData = TestTools.DataFromFile(outputFile, 0,z,0);
            assertNotNull(resultData);
            assertEquals(planeSize, resultData.length);
            int i = 0;
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    // x-z plane => result varies only in y
                    assertEquals( sizeX * y + (sizeX -1) + (sizeZ-1) * 10, resultData[i], 0.0001);
                    i++;
                }
            }
        }
    }

    @Test
    public void PlaneToMaxXY_Test()
    {
        int sizeX=5;
        int sizeY=5;
        int sizeC=1;
        int sizeZ=3;
        int planeSize = sizeX * sizeY;
        byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ];
        int current = 0;
        for (byte z = 0; z <sizeZ ; z++)
        {
            for (byte i = 0; i < sizeX * sizeY * sizeC; i++)
            {
                inputData[current] = (byte)(i + z * 10);
                current++;
            }
        }

        File inputFile = null;
        File outputFile= null;
        try
        {
            inputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile = File.createTempFile("UnitTest", ".tiff");
            inputFile.deleteOnExit();
            outputFile.deleteOnExit();
            TestTools.WriteImageToFile(inputFile,inputData,sizeX,sizeY,sizeC, sizeZ);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.PlaneToMaxXY" );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        for (byte z = 0; z <sizeZ ; z++)
        {
            double[] resultData = TestTools.DataFromFile(outputFile, 0,z,0);
            assertNotNull(resultData);
            assertEquals(planeSize, resultData.length);
            for (int i = 0; i < planeSize; i++)
            {
                assertEquals((planeSize - 1)+ z * 10, resultData[i], 0.0001);
            }
        }
    }
}

