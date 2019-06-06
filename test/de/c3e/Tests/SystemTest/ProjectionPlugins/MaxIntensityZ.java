package de.c3e.Tests.SystemTest.ProjectionPlugins;

import de.c3e.ProcessManager.GlobalSettings;
import de.c3e.ProcessManager.LoadBalancing.ConstantSizeBalancer;
import de.c3e.ProcessManager.Main;
import de.c3e.Tests.MultipleInstancesTest;
import de.c3e.Tests.SystemTest.NonSizeChangingPlugins.WorkflowHelper;
import de.c3e.Tests.TestTools;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for Max Intensity Projection
 *  => Projection Plugins
 */
public class MaxIntensityZ
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void MaxIntensityProjectionZ_Test()
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

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.MaximumIntensityProjectionZ" );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson, new ConstantSizeBalancer(planeSize*2));

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        for (byte z = 0; z <1 ; z++)
        {
            double[] resultData = TestTools.DataFromFile(outputFile, 0,z,0);
            assertNotNull(resultData);
            assertEquals(planeSize, resultData.length);
            int i = 0;
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    // projection in z => max z
                    assertEquals( sizeX * y + x  + 20, resultData[i], 0.0001);
                    i++;
                }
            }
        }
    }

    @Test    public void MaxIntensityProjectionZ_FromFile_Test()
    {
        File inputFile = new File("C:\\PHD\\UnitTest\\DeconvolutionMerged.tif");
        File outputFile= null;
        try
        {
            outputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile.deleteOnExit();
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.MaximumIntensityProjectionZDouble" );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());        assertTrue(outputFile.length() > 0);


        double[] resultData = TestTools.DataFromFile(outputFile, 0,0,0);
        assertNotNull(resultData);
        assertEquals(672*712, resultData.length);
    }

    @Test
    public void MaxIntensityProjectionZ_FromFile_CorrectType_Test()
    {
        File inputFile = new File("C:\\PHD\\UnitTest\\DeconvolutionMerged.tif");
        File outputFile= null;
        try
        {
            outputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile.deleteOnExit();
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.MaximumIntensityProjectionZShort" );

        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());        assertTrue(outputFile.length() > 0);

        short[] resultData = TestTools.DataFromFileShort(outputFile, 0);
        assertNotNull(resultData);
        assertEquals(672*712, resultData.length);

        File reverenceFile = new File("C:\\PHD\\UnitTest\\DeconvolutionMergedMaxIntensity.tif");
        short[] reverenceData = TestTools.DataFromFileShort(reverenceFile,0);
        assertNotNull(resultData);
        assertNotNull(reverenceData);
        assertEquals(reverenceData.length,resultData.length);

        double maxDiv = 0;
        for (int i=0; i< reverenceData.length; i++)
        {
            double delta = Math.abs(resultData[i] - reverenceData[i]);
            assertTrue(delta < 2);
            maxDiv = Math.max(maxDiv,delta);
        }

        System.out.println("Maximum difference between result and reverence : " + maxDiv);
    }

    @Test
    public void MaxIntensityPlaneProjectionZ_Test()
    {
        int sizeX=5;
        int sizeY=5;
        int sizeC=1;
        int sizeZ=3;
        int planeSize = sizeX * sizeY;
        byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ];
        int current = 0;
        int max = 0;
        for (byte z = 0; z <sizeZ ; z++)
        {
            for (byte i = 0; i < sizeX * sizeY * sizeC; i++)
            {
                inputData[current] = (byte)(i + z * 10);
                max = Math.max(max, inputData[current]);
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

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.MaximumIntensityPlaneProjectionZ" );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        for (byte z = 0; z <1 ; z++)
        {
            double[] resultData = TestTools.DataFromFile(outputFile, 0,z,0);
            assertNotNull(resultData);
            assertEquals(planeSize, resultData.length);
            int i = 0;
            for (int y = 0; y < sizeY; y++)
            {
                for (int x = 0; x < sizeX; x++)
                {
                    // projection in z => max z
                    assertEquals( max, resultData[i], 0.0001);
                    i++;
                }
            }
        }
    }

    @Test
    public void MaxIntensityPlaneProjectionZMultipleC_WithMultiPassTest()
    {
        MaxIntensityPlaneProjectionZMultipleC_Test(true);
    }

    @Test
    public void MaxIntensityPlaneProjectionZMultipleC_WithoutMultiPassTest()
    {
        MaxIntensityPlaneProjectionZMultipleC_Test(false);
    }

    private void MaxIntensityPlaneProjectionZMultipleC_Test(boolean useMultiPass)
    {
        int sizeX=5;
        int sizeY=5;
        int sizeC=2;
        int sizeZ=3;
        int planeSize = sizeX * sizeY;
        byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ];
        int current = 0;
        for (byte z = 0; z <sizeZ ; z++)
        {
            for (byte c = 0 ; c <sizeC ; c++)
            {
                for (byte i = 0; i < sizeX * sizeY; i++)
                {
                    inputData[current] = (byte) (i + z * 13 + c*7);
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

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.MaximumIntensityPlaneProjectionZ" );

        GlobalSettings.OverridePixelEstimate = sizeX*sizeY+1;
        GlobalSettings.EnableMultiPassProjectionCalculations = useMultiPass;
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);
        GlobalSettings.OverridePixelEstimate = -1;
        GlobalSettings.EnableMultiPassProjectionCalculations = true;

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        for (byte z = 0; z <1 ; z++)
        {
            for (byte c = 0; c <sizeC ; c++)
            {
                double[] resultData = TestTools.DataFromFile(outputFile, c, z, 0);
                assertNotNull(resultData);
                assertEquals(planeSize, resultData.length);
                int i = 0;
                for (int y = 0; y < sizeY; y++)
                {
                    for (int x = 0; x < sizeX; x++)
                    {
                        // projection in z => max z
                        assertEquals((sizeX*sizeY-1) + (sizeZ-1) * 13 + c*7, resultData[i], 0.0001);
                        i++;
                    }
                }
            }
        }
    }
}
