package de.c3e.Tests.SystemTest;

import de.c3e.ProcessManager.LoadBalancing.AverageExecutionTimeBalancer;
import de.c3e.ProcessManager.LoadBalancing.ConstantSizeBalancer;
import de.c3e.ProcessManager.Main;
import de.c3e.ProcessManager.Utils.AbstractTableObject;
import de.c3e.Tests.MultipleInstancesTest;
import de.c3e.Tests.SystemTest.NonSizeChangingPlugins.WorkflowHelper;
import de.c3e.Tests.TestTools;
import icy.util.Random;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExternalToolTest
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void CopyTool()
    {
        final int sizeX=10;
        final int sizeY=10;
        final int sizeC=3;
        byte[][] img = new byte[3][];
        byte[] ch1 = new byte[sizeX * sizeY];img[0] = ch1;
        byte[] ch2 = new byte[sizeX * sizeY];img[1] = ch2;
        byte[] ch3 = new byte[sizeX * sizeY]; img[2] = ch3;

        for (int c = 0; c < sizeC; c++)
        {
            for (int i = 0; i < sizeX * sizeY; i++)
            {
                img[c][i] = (byte) Random.nextInt(200);
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

            TestTools.WriteMultiChannelToFile(inputFile,ch1,ch2,ch3,sizeX,sizeY);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.CopyExternalToolBlock" );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        double[] resultCh1 = TestTools.DataFromFile(outputFile,0);
        double[] resultCh2 = TestTools.DataFromFile(outputFile,1);
        double[] resultCh3 = TestTools.DataFromFile(outputFile,2);
        assertNotNull(resultCh1);
        assertNotNull(resultCh2);
        assertNotNull(resultCh3);

        for (int i = 0; i < sizeX * sizeY; i++)
        {
            Assert.assertEquals((byte)resultCh1[i],ch1[i]);
            Assert.assertEquals((byte)resultCh2[i],ch2[i]);
            Assert.assertEquals((byte)resultCh3[i],ch3[i]);
        }

        inputFile.delete();
        outputFile.delete();
    }

    @Test
    public void ParallelCopyTool()
    {
        final int sizeX=10;
        final int sizeY=10;
        final int sizeC=3;
        byte[][] img = new byte[3][];
        byte[] ch1 = new byte[sizeX * sizeY];img[0] = ch1;
        byte[] ch2 = new byte[sizeX * sizeY];img[1] = ch2;
        byte[] ch3 = new byte[sizeX * sizeY]; img[2] = ch3;

        for (int c = 0; c < sizeC; c++)
        {
            for (int i = 0; i < sizeX * sizeY; i++)
            {
                img[c][i] = (byte) Random.nextInt(200);
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

            TestTools.WriteMultiChannelToFile(inputFile,ch1,ch2,ch3,sizeX,sizeY);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.ParallelExternalCopyBlock" );
        MultipleInstancesTest.RunFromFilesJsonDumpResults(5,graphParameterJson,false,"TestResults/" , new ConstantSizeBalancer(100));

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        double[] resultCh1 = TestTools.DataFromFile(outputFile,0);
        double[] resultCh2 = TestTools.DataFromFile(outputFile,1);
        double[] resultCh3 = TestTools.DataFromFile(outputFile,2);
        assertNotNull(resultCh1);
        assertNotNull(resultCh2);
        assertNotNull(resultCh3);

        for (int i = 0; i < sizeX * sizeY; i++)
        {
            Assert.assertEquals((byte)resultCh1[i],ch1[i]);
            Assert.assertEquals((byte)resultCh2[i],ch2[i]);
            Assert.assertEquals((byte)resultCh3[i],ch3[i]);
        }

        inputFile.delete();
        outputFile.delete();
    }



    @Test
    public void DeconvolutionTool()
    {
        final int sizeX=10;
        final int sizeY=10;
        final int sizeC=3;
        byte[][] img = new byte[3][];
        byte[] ch1 = new byte[sizeX * sizeY];img[0] = ch1;
        byte[] ch2 = new byte[sizeX * sizeY];img[1] = ch2;
        byte[] ch3 = new byte[sizeX * sizeY]; img[2] = ch3;

        for (int c = 0; c < sizeC; c++)
        {
            for (int i = 0; i < sizeX * sizeY; i++)
            {
                img[c][i] = (byte) Random.nextInt(200);
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

            TestTools.WriteMultiChannelToFile(inputFile,ch1,ch2,ch3,sizeX,sizeY);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "de.c3e.BlockTemplates.Examples.ParallelPlaneDeconvolutionTool" );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        double[] resultCh1 = TestTools.DataFromFile(outputFile,0);
        double[] resultCh2 = TestTools.DataFromFile(outputFile,1);
        double[] resultCh3 = TestTools.DataFromFile(outputFile,2);
        assertNotNull(resultCh1);
        assertNotNull(resultCh2);
        assertNotNull(resultCh3);

        for (int i = 0; i < sizeX * sizeY; i++)
        {
            Assert.assertEquals((byte)resultCh1[i],ch1[i]);
            Assert.assertEquals((byte)resultCh2[i],ch2[i]);
            Assert.assertEquals((byte)resultCh3[i],ch3[i]);
        }

        inputFile.delete();
        outputFile.delete();
    }

    @Test
    public void CellProfilerTest()
    {
        //File inputFile = new File("C:\\PHD\\UnitTest\\CellProfiler\\ExampleComposite.png");
        //File inputFile = new File("C:\\PHD\\UnitTest\\CellProfiler\\2StacksRGB.tif");
        //AverageExecutionTimeBalancer.MinPortion = 0.5;
        File inputFile = new File("C:\\PHD\\UnitTest\\2StacksRGB.tif");
        File outputFile= null;
        try
        {
            outputFile = File.createTempFile("UnitTest", ".csv");
            outputFile.deleteOnExit();
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "CellProfilerTool" );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);
        //AverageExecutionTimeBalancer.MinPortion = 0.02;

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
        try
        {
            List<String> lines = Files.readAllLines(outputFile.toPath(), StandardCharsets.UTF_8);
            assertTrue(lines.size() > 580);

            AbstractTableObject table = new AbstractTableObject(outputFile.getAbsolutePath());
            assertTrue(table.getHeader().contains("Z"));
            assertTrue(table.getHeader().contains("T"));

            List<String> ts= table.getColumn(table.getColumnCount()-1);
            Set<String> setT = new HashSet<>(ts);

            //Assert.assertEquals(10,setT.size() );
        }
        catch (Exception ex)
        {
            assertTrue(false);
        }

        outputFile.delete();
    }
}