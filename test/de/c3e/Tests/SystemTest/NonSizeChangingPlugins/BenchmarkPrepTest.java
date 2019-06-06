package de.c3e.Tests.SystemTest.NonSizeChangingPlugins;

import de.c3e.ProcessManager.Main;
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

public class BenchmarkPrepTest
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void BenchmarkPrepLinearDelay()
    {
        int sizeX=1000;
        int sizeY=1000;
        int sizeC=3;
        int sizeZ=1;
        int sizeT=1;
        int planeSize = sizeX * sizeY;
        byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ * sizeT];
        int current = 0;

        // => needs the complete image
        int max = 0;
        for (long t = 0; t <sizeT ; t++)
        {
            for (long z = 0; z < sizeZ; z++)
            {
                for (long c = 0; c < sizeC; c++)
                {
                    for (long i = 0; i < sizeX * sizeY; i++)
                    {

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
        extraParams.add( new String[]{"Delay","0.002","in"});
        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "LinearDelayBlock" , extraParams );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
}
