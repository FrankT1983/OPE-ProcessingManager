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
 * Created by Frank on 17.03.2017.
 */
public class TestCustomThreshold
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void CustomThreshold_Test()
    {
        int sizeX=5;
        int sizeY=5;
        byte[] inputData = new byte[sizeX * sizeY];
        for (byte i = 0; i < inputData.length; i++)
        {
            inputData[i] = i;
        }

        File inputFile = null;
        File outputFile= null;
        try
        {
            inputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile = File.createTempFile("UnitTest", ".tiff");
            inputFile.deleteOnExit();
            outputFile.deleteOnExit();
            TestTools.WriteImageToFile(inputFile,inputData,sizeX,sizeY);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  ExtractChannelWorkflowAndParameters(inputFile,outputFile, 11 );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        double[] resultData = TestTools.DataFromFile(outputFile,0);
        assertNotNull(resultData);
        assertEquals(inputData.length, resultData.length);
        for (int i =0;i< inputData.length; i++)
        {
            assertEquals(inputData[i]  >= 11 ? 1.0 : 0.0 , resultData[i] , 0.0001);
        }
    }

    //@Test
    // this will run pretty long, so use in only for extensive tests
    public void CustomThresholdWithRealFile_Test()
    {
        File inputFile = new File("C:\\PHD\\UnitTest\\M001S06.lsm");
        File outputFile= null;
        try
        {
            outputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile.deleteOnExit();

        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  ExtractChannelWorkflowAndParameters(inputFile,outputFile, 11 );
        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        double[] resultData = TestTools.DataFromFile(outputFile,0);
        assertNotNull(resultData);
    }

    private String ExtractChannelWorkflowAndParameters(File inputFile, File outputFile, int threshold)
    {
        // do replacements for json
        String inputFilePath =  JSONObject.escape(inputFile.getAbsolutePath());
        String outFilePath =  JSONObject.escape(outputFile.getAbsolutePath());

        return  "{  \n" +
                "   \"intermediates\":true,\n" +
                "   \"blocks\":[  \n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"ImageId\"\n" +
                "         ],\n" +
                "         \"positionX\":370,\n" +
                "         \"positionY\":280,\n" +
                "         \"blockName\":\"OmeroImage\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"LoadedImage\"\n" +
                "         ],\n" +
                "         \"blockId\":\"OmeroImage\",\n" +
                "         \"inputList\":[  \n" +
                "         ],\n" +
                "         \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\",\n" +
                "         \"elementId\":\"0\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\",\n" +
                "            \"Threshold\"\n" +
                "         ],\n" +
                "         \"positionX\":745,\n" +
                "         \"positionY\":299,\n" +
                "         \"blockName\":\"Threshold\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"Thresholder\",\n" +
                "         \"inputList\":[  \n" +
                "         ],\n" +
                "         \"blockType\":\"de.c3e.Blocks.Threshold\",\n" +
                "         \"elementId\":\"1\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Image to save\",\n" +
                "            \"DataSet Id\"\n" +
                "         ],\n" +
                "         \"positionX\":790,\n" +
                "         \"positionY\":567,\n" +
                "         \"blockName\":\"OmeroImageSaveToDataSet\",\n" +
                "         \"Outputs\":[  \n" +
                "         ],\n" +
                "         \"blockId\":\"OmeroImageSaveToDataSet\",\n" +
                "         \"inputList\":[  \n" +
                "         ],\n" +
                "         \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\",\n" +
                "         \"elementId\":\"2\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"name\":\"CustomThreshold\",\n" +
                "   \"parameters\":[  \n" +
                "      [  \n" +
                "         \"1\",\n" +
                "         \"Threshold\",\n" +
                "         \""+ String.valueOf(threshold)+"\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"2\",\n" +
                "         \"Value\",\n" +
                "         \""+outFilePath+"\",\n" +
                "         \"out\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"0\",\n" +
                "         \"Value\",\n" +
                "         \""+inputFilePath+"\",\n" +
                "         \"out\"\n" +
                "      ]\n" +
                "   ],\n" +
                "   \"intermediateDataSet\":751,\n" +
                "   \"links\":[  \n" +
                "      {  \n" +
                "         \"sourcePort\":\"LoadedImage\",\n" +
                "         \"targetBlock\":\"1\",\n" +
                "         \"targetPort\":\"Input\",\n" +
                "         \"sourceBlock\":\"0\",\n" +
                "         \"anchors\":[  \n" +
                "            [  \n" +
                "               1,\n" +
                "               0.5,\n" +
                "               1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ],\n" +
                "            [  \n" +
                "               0,\n" +
                "               0.3333333333333333,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"Output\",\n" +
                "         \"targetBlock\":\"2\",\n" +
                "         \"targetPort\":\"Image to save\",\n" +
                "         \"sourceBlock\":\"1\",\n" +
                "         \"anchors\":[  \n" +
                "            [  \n" +
                "               1,\n" +
                "               0.5,\n" +
                "               1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ],\n" +
                "            [  \n" +
                "               0,\n" +
                "               0.3333333333333333,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      }\n" +
                "   ],\n" +
                "   \"runId\":\"94beaa6e-a655-4844-8dbf-c3c440a2d324\"\n" +
                "}";
    }
}
