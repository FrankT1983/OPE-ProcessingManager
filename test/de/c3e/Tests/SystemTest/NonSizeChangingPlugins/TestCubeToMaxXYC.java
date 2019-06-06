package de.c3e.Tests.SystemTest.NonSizeChangingPlugins;

import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.Main;
import de.c3e.Tests.MultipleInstancesTest;
import de.c3e.Tests.TestTools;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Frank on 23.03.2017.
 */
public class TestCubeToMaxXYC
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void CubeMaxXYC_Test()
    {
        int sizeX=5;
        int sizeY=5;
        int sizeC=3;
        int sizeZ=3;
        int planeSize = sizeX * sizeY;
        byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ];
        int current = 0;

        // => needs x,y and all channels =>  maximum of each z plane will be created
        int[] max = new int[sizeZ];
        for (byte z = 0; z <sizeZ ; z++)
        {
            for(byte c = 0; c <sizeC ; c++)
            {
                for (byte i = 0; i < sizeX * sizeY; i++)
                {
                    byte curr =  (byte) (i + c*5+ z * 10);
                    inputData[current] = (byte) curr;
                    max[z]=Math.max(max[z], curr);
                    current++;
                }
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

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.CubeMaxXYC" );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        for (byte z = 0; z <sizeZ ; z++)
        {
            for (byte c=0;c<sizeC;c++)
            {
                double[] resultData = TestTools.DataFromFile(outputFile, c, z, 0);
                assertNotNull(resultData);
                assertEquals(planeSize, resultData.length);
                int i = 0;
                for (int y = 0; y < sizeY; y++)
                {
                    for (int x = 0; x < sizeX; x++)
                    {
                        // max of the cube => independent of position
                        assertEquals(max[z], resultData[i], 0.0001);
                        i++;
                    }
                }
            }
        }
    }

    @Test
    public void CubeMaxXYZ_Test()
    {
        int sizeX=5;
        int sizeY=5;
        int sizeC=3;
        int sizeZ=3;
        int planeSize = sizeX * sizeY;
        byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ];
        int current = 0;

        // => needs x,y and complete stack =>  maximum of each channel will be created
        int[] max = new int[sizeC];
        for (byte z = 0; z <sizeZ ; z++)
        {
            for(byte c = 0; c <sizeC ; c++)
            {
                for (byte i = 0; i < sizeX * sizeY; i++)
                {
                    byte curr =  (byte) (i + c*5+ z * 10);
                    inputData[current] = curr;
                    max[c]=Math.max(max[c], curr);
                    current++;
                }
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

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.CubeMaxXYZ" );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        for (byte z = 0; z <sizeZ ; z++)
        {
            for (byte c=0;c<sizeC;c++)
            {
                double[] resultData = TestTools.DataFromFile(outputFile, c, z, 0);
                assertNotNull(resultData);
                assertEquals(planeSize, resultData.length);
                int i = 0;
                for (int y = 0; y < sizeY; y++)
                {
                    for (int x = 0; x < sizeX; x++)
                    {
                        // max of the cube => independent of position
                        assertEquals(max[c], resultData[i], 0.0001);
                        i++;
                    }
                }
            }
        }
    }
}
