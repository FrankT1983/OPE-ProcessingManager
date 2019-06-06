package de.c3e.Tests.SystemTest.NonSizeChangingPlugins;

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

public class UnknownBlock
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void UnknownBlock_Test()
    {
        int sizeX=10;
        int sizeY=10;
        int sizeC=1;
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
                        inputData[current] = (byte) (i);
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

        BenchmarkHelper.SetDelayFactor(0.00);
        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "BlockIDoNotKnow");
        MultipleInstancesTest.RunFromFilesJsonDumpResults(5,graphParameterJson,false,"TestResults/" , new ConstantSizeBalancer(1000));
    }
}
