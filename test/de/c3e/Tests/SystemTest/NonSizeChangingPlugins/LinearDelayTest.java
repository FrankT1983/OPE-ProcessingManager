package de.c3e.Tests.SystemTest.NonSizeChangingPlugins;

import de.c3e.ProcessManager.LoadBalancing.AverageExecutionTimeBalancer;
import de.c3e.ProcessManager.LoadBalancing.AverageWithTransferTimeBalancer;
import de.c3e.ProcessManager.LoadBalancing.ConstantSizeBalancer;
import de.c3e.ProcessManager.Main;
import de.c3e.ProcessManager.Utils.BenchmarkHelper;
import de.c3e.Tests.MultipleInstancesTest;
import de.c3e.Tests.TestTools;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Frank on 19.06.2017.
 */
public class LinearDelayTest
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
            int sizeX=100;
            int sizeY=100;
            int sizeC=10;
            int sizeZ=1;
            int sizeT=1;
            int planeSize = sizeX * sizeY;
            byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ * sizeT];
            int current = 0;

            // => needs the complete image
            for (int t = 0; t <sizeT ; t++)
            {
                for (int z = 0; z < sizeZ; z++)
                {
                    for (int c = 0; c < sizeC; c++)
                    {
                        for (int i = 0; i < sizeX * sizeY; i++)
                        {
                            inputData[current] = (byte) (i + z * 13 + c * 7 + t*5);
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

            List<String[]> extraParams = new ArrayList<>();
            extraParams.add( new String[]{"Delay","0.000","in"});
            BenchmarkHelper.SetDelayFactor(0.0);
            String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.ProcessManager.BlockRepository.Benchmarking.LinearDelayBlock", extraParams );
            MultipleInstancesTest.RunFromFilesJsonDumpResults(5,graphParameterJson,false,"TestResults/" , new ConstantSizeBalancer(1000));
            //MultipleInstancesTest.RunFromFilesJsonDumpResults(5,graphParameterJson,false,"TestResults/" , new AverageExecutionTimeBalancer());
            BenchmarkHelper.SetDelayFactor(0.0);


            assertTrue(outputFile.exists());
            assertTrue(outputFile.length() > 0);

            int i = 0;
            for (byte t = 0; t <sizeT ; t++)
            {
                for (byte z = 0; z < sizeZ; z++)
                {
                    for (byte c = 0; c < sizeC; c++)
                    {
                        double[] resultData = TestTools.DataFromFile(outputFile, c, z, t);
                        assertNotNull(resultData);
                        assertEquals(planeSize, resultData.length);
                        int j = 0;

                        for (int y = 0; y < sizeY; y++)
                        {
                            for (int x = 0; x < sizeX; x++)
                            {
                                // max of the cube => independent of position
                                assertEquals(inputData[i], resultData[j], 0.0001);
                                i++;
                                j++;
                            }
                        }
                    }
                }
            }
        }

    @Test
    public void FullImageToMax_Average_Test()
    {
        int sizeX=100;
        int sizeY=100;
        int sizeC=10;
        int sizeZ=1;
        int sizeT=1;
        int planeSize = sizeX * sizeY;
        byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ * sizeT];
        int current = 0;

        // => needs the complete image
        for (int t = 0; t <sizeT ; t++)
        {
            for (int z = 0; z < sizeZ; z++)
            {
                for (int c = 0; c < sizeC; c++)
                {
                    for (int i = 0; i < sizeX * sizeY; i++)
                    {
                        inputData[current] = (byte) (i + z * 13 + c * 7 + t*5);
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

        List<String[]> extraParams = new ArrayList<>();
        extraParams.add( new String[]{"Delay","0.000","in"});
        BenchmarkHelper.SetDelayFactor(0.0);
        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.ProcessManager.BlockRepository.Benchmarking.LinearDelayBlock", extraParams );
        MultipleInstancesTest.RunFromFilesJsonDumpResults(5,graphParameterJson,false,"TestResults/" ,new AverageExecutionTimeBalancer());
        BenchmarkHelper.SetDelayFactor(0.0);


        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        int i = 0;
        for (byte t = 0; t <sizeT ; t++)
        {
            for (byte z = 0; z < sizeZ; z++)
            {
                for (byte c = 0; c < sizeC; c++)
                {
                    double[] resultData = TestTools.DataFromFile(outputFile, c, z, t);
                    assertNotNull(resultData);
                    assertEquals(planeSize, resultData.length);
                    int j = 0;

                    for (int y = 0; y < sizeY; y++)
                    {
                        for (int x = 0; x < sizeX; x++)
                        {
                            // max of the cube => independent of position
                            assertEquals(inputData[i], resultData[j], 0.0001);
                            i++;
                            j++;
                        }
                    }
                }
            }
        }
    }

    @Test
    public void FullImageToMax_AverageTransfer_Test()
    {
        int sizeX=100;
        int sizeY=100;
        int sizeC=10;
        int sizeZ=1;
        int sizeT=1;
        int planeSize = sizeX * sizeY;
        byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ * sizeT];
        int current = 0;

        // => needs the complete image
        for (int t = 0; t <sizeT ; t++)
        {
            for (int z = 0; z < sizeZ; z++)
            {
                for (int c = 0; c < sizeC; c++)
                {
                    for (int i = 0; i < sizeX * sizeY; i++)
                    {
                        inputData[current] = (byte) (i + z * 13 + c * 7 + t*5);
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

        List<String[]> extraParams = new ArrayList<>();
        extraParams.add( new String[]{"Delay","0.000","in"});
        BenchmarkHelper.SetDelayFactor(0.0);
        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.ProcessManager.BlockRepository.Benchmarking.LinearDelayBlock", extraParams );
        MultipleInstancesTest.RunFromFilesJsonDumpResults(5,graphParameterJson,false,"TestResults/" ,new AverageWithTransferTimeBalancer());
        BenchmarkHelper.SetDelayFactor(0.0);


        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        int i = 0;
        for (byte t = 0; t <sizeT ; t++)
        {
            for (byte z = 0; z < sizeZ; z++)
            {
                for (byte c = 0; c < sizeC; c++)
                {
                    double[] resultData = TestTools.DataFromFile(outputFile, c, z, t);
                    assertNotNull(resultData);
                    assertEquals(planeSize, resultData.length);
                    int j = 0;

                    for (int y = 0; y < sizeY; y++)
                    {
                        for (int x = 0; x < sizeX; x++)
                        {
                            // max of the cube => independent of position
                            assertEquals(inputData[i], resultData[j], 0.0001);
                            i++;
                            j++;
                        }
                    }
                }
            }
        }
    }
}
