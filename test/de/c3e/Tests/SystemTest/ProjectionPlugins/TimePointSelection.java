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
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for Max Intensity Projection
 *  => Projection Plugins
 */
public class TimePointSelection
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void TimePointSelection_Test()
    {
        int sizeX=5;
        int sizeY=5;
        int sizeC=1;
        int sizeZ=1;
        int sizeT=5;
        int planeSize = sizeX * sizeY;
        byte[] inputData = new byte[sizeX * sizeY * sizeC * sizeZ*sizeT];
        int current = 0;
        for (byte t = 0; t <sizeT ; t++)
        {
            for (byte z = 0; z < sizeZ; z++)
            {
                for (byte i = 0; i < sizeX * sizeY * sizeC; i++)
                {
                    inputData[current] = (byte) (t);
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
            TestTools.WriteImageToFile(inputFile,inputData,sizeX,sizeY,sizeC, sizeZ,sizeT);
        }
        catch (Exception e)
        {   assertTrue(false);}

        Integer desiredT=2;

        List<String[]> extraParams = new ArrayList<>();
        extraParams.add( new String[]{"TimeIndex",desiredT.toString(),"in"});
        String graphParameterJson =  WorkflowHelper.PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile, "TimePointSelectionOld" ,extraParams);
        MultipleInstancesTest.RunFromFilesJson(2,graphParameterJson, new ConstantSizeBalancer(planeSize*2));

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        for (byte t = 0; t <1 ; t++)
        {
            double[] resultData = TestTools.DataFromFile(outputFile, 0,0,t);
            assertNotNull(resultData);
            assertEquals(planeSize, resultData.length);

        }
    }
}
