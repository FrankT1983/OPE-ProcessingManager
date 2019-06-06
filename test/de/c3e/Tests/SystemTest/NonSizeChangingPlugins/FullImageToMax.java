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

/**
 * Created by Frank on 27.03.2017.
 */
public class FullImageToMax
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void FullImageToMax_Test()
    {
        int sizeX=5;
        int sizeY=5;
        int sizeC=3;
        int sizeZ=3;
        int sizeT=4;
        int planeSize = sizeX * sizeY;
        byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ * sizeT];
        int current = 0;

        // => needs the complete image
        int max = 0;
        for (byte t = 0; t <sizeT ; t++)
        {
            for (byte z = 0; z < sizeZ; z++)
            {
                for (byte c = 0; c < sizeC; c++)
                {
                    for (byte i = 0; i < sizeX * sizeY; i++)
                    {
                        byte curr = (byte) (i + c * 5 + z * 10 + t*3);
                        inputData[current] = (byte) curr;
                        max = Math.max(max, curr);
                        current++;
                    }
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
            TestTools.WriteImageToFile(inputFile,inputData,sizeX,sizeY,sizeC, sizeZ, sizeT);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.FullyHyperCubeMax" );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        for (byte t = 0; t <sizeT ; t++)
        {
            for (byte z = 0; z < sizeZ; z++)
            {
                for (byte c = 0; c < sizeC; c++)
                {
                    double[] resultData = TestTools.DataFromFile(outputFile, c, z, t);
                    assertNotNull(resultData);
                    assertEquals(planeSize, resultData.length);
                    int i = 0;
                    for (int y = 0; y < sizeY; y++)
                    {
                        for (int x = 0; x < sizeX; x++)
                        {
                            // max of the cube => independent of position
                            assertEquals(max, resultData[i], 0.0001);
                            i++;
                        }
                    }
                }
            }
        }
    }
}
