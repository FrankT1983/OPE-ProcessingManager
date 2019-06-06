package de.c3e.Tests.SystemTest;

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
 * Extract a Channel from an image
 */
public class ExtractChannelFromJsonFile2
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }


    @Test
    public void ExtractChannel2_FromJson()
    {

        final int channelToExtract = 2;
        File inputFile = new File("C:\\PHD\\UnitTest\\cells.tif");
        File outputFile= null;

        File reverenceFile = new File("C:\\PHD\\UnitTest\\Channel2.png");
        try
        {
            outputFile = File.createTempFile("UnitTest", ".txt");
            outputFile.deleteOnExit();
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  ExtractChannelWorkflowAndParameters(inputFile,outputFile, channelToExtract );

        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);


        double[] resultData = TestTools.DataFromFile(outputFile,0);

        double[] reverenceData = TestTools.DataFromFile(reverenceFile,0);
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


    private String ExtractChannelWorkflowAndParameters(File inputFile, File outputFile, int channel)
    {
        // do replacements for json
        String inputFilePath =  JSONObject.escape(inputFile.getAbsolutePath());
        String outFilePath =  JSONObject.escape(outputFile.getAbsolutePath());

        return "{  \n" +
                "   \"parameters\":[  \n" +
                "      [  \n" +
                "         \"2\",\n" +
                "         \"Value\",\n" +
                "         \""+outFilePath+"\",\n" +
                "         \"out\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"1\",\n" +
                "         \"Channel\",\n" +
                "         \""+channel+"\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"0\",\n" +
                "         \"Value\",\n" +
                "         \""+inputFilePath+"\",\n" +
                "         \"out\"\n" +
                "      ]\n" +
                "   ],\n" +
                "   \"blocks\":[  \n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"ImageId\"\n" +
                "         ],\n" +
                "         \"positionX\":592,\n" +
                "         \"positionY\":361,\n" +
                "         \"blockName\":\"OmeroImage\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"LoadedImage\"\n" +
                "         ],\n" +
                "         \"blockId\":\"0\",\n" +
                "         \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\",\n" +
                "         \"elementId\":\"0\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Sequence\",\n" +
                "            \"Channel\"\n" +
                "         ],\n" +
                "         \"positionX\":811,\n" +
                "         \"positionY\":590,\n" +
                "         \"blockName\":\"ExtractChannel\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Extracted\"\n" +
                "         ],\n" +
                "         \"blockId\":\"ExtractChannel\",\n" +
                "         \"blockType\":\"plugins.tprovoost.sequenceblocks.extract.ExtractChannel\",\n" +
                "         \"elementId\":\"1\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Image to save\",\n" +
                "            \"DataSet Id\"\n" +
                "         ],\n" +
                "         \"positionX\":1062,\n" +
                "         \"positionY\":368,\n" +
                "         \"blockName\":\"OmeroImageSaveToDataSet\",\n" +
                "         \"Outputs\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockId\":\"OmeroImageSaveToDataSet\",\n" +
                "         \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\",\n" +
                "         \"elementId\":\"2\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"name\":\"ExtractChannel\",\n" +
                "   \"links\":[  \n" +
                "      {  \n" +
                "         \"sourcePort\":\"LoadedImage\",\n" +
                "         \"targetBlock\":\"1\",\n" +
                "         \"targetPort\":\"Sequence\",\n" +
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
                "         \"sourcePort\":\"Extracted\",\n" +
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
                "   ]\n" +
                "}";
    }
}
