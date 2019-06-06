package de.c3e.Tests.SystemTest;

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
 * Test created from a workflow that ran into an endless loop
 */
public class ExtractChannelAndThresholdFromJsonFile
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }


    @Test
    public void ExtractChannelAndThreshold_FromJson()
    {
        final int channelToExtract = 1;
        final int sizeX=10;
        final int sizeY=10;
        final int sizeC=3;
        byte[][] img = new byte[3][];
        byte[] ch1 = new byte[sizeX * sizeY];img[0] = ch1;
        byte[] ch2 = new byte[sizeX * sizeY];img[1] = ch2;
        byte[] ch3 = new byte[sizeX * sizeY]; img[2] = ch3;

        for (byte c = 0; c < sizeC; c++)
        {
            for (byte i = 0; i < sizeX * sizeY; i++)
            {
                img[c][i] = (byte)0;
            }
        }

        // draw circle in channel to extract
        for (byte x = 0; x < sizeX; x++)
        {
            for (byte y = 0; y <  sizeY; y++)
            {
                int dx = sizeX  /2 - x;
                int dy = sizeX  /2 - x;

                if (dx*dx + dy*dy < 9 )
                {
                    img[channelToExtract][y*sizeX + x] = (byte)120;
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

            TestTools.WriteMultiChannelToFile(inputFile,ch1,ch2,ch3,sizeX,sizeY);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  ExtractChannelWorkflowAndParameters(inputFile,outputFile, channelToExtract );

        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);


        double[] resultData = TestTools.DataFromFile(outputFile,0);
        assertNotNull(resultData);
        assertEquals(sizeX*sizeY, resultData.length);


        // can't really test more, than if something went through the whole pipeline.
        // todo: make reference image => test with that
    }


    private String ExtractChannelWorkflowAndParameters(File inputFile, File outputFile, int channel)
    {
        // do replacements for json
        String inputFilePath =  JSONObject.escape(inputFile.getAbsolutePath());
        String outFilePath =  JSONObject.escape(outputFile.getAbsolutePath());

        return "{  \n" +
                "   \"parameters\":[  \n" +
                "      [  \n" +
                "         \"4\",\n" +
                "         \"Treat as percentiles\",\n" +
                "         \"false\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"5\",\n" +
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
                "         \"2\",\n" +
                "         \"filterZ\",\n" +
                "         \"0\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"3\",\n" +
                "         \"Classes\",\n" +
                "         \"2\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"2\",\n" +
                "         \"filterX\",\n" +
                "         \"3\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"2\",\n" +
                "         \"filterY\",\n" +
                "         \"3\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"4\",\n" +
                "         \"channel\",\n" +
                "         \"0\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"0\",\n" +
                "         \"Value\",\n" +
                "         \""+inputFilePath+"\",\n" +
                "         \"out\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"3\",\n" +
                "         \"Channel\",\n" +
                "         \"0\",\n" +
                "         \"in\"\n" +
                "      ]\n" +
                "   ],\n" +
                "   \"blocks\":[  \n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"ImageId\"\n" +
                "         ],\n" +
                "         \"positionX\":522,\n" +
                "         \"positionY\":266,\n" +
                "         \"blockName\":\"OmeroImage\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"LoadedImage\"\n" +
                "         ],\n" +
                "         \"blockId\":\"OmeroImage\",\n" +
                "         \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\",\n" +
                "         \"elementId\":\"0\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Sequence\",\n" +
                "            \"Channel\"\n" +
                "         ],\n" +
                "         \"positionX\":1016,\n" +
                "         \"positionY\":308,\n" +
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
                "            \"input\",\n" +
                "            \"filterX\",\n" +
                "            \"filterY\",\n" +
                "            \"filterZ\"\n" +
                "         ],\n" +
                "         \"positionX\":616,\n" +
                "         \"positionY\":577,\n" +
                "         \"blockName\":\"GaussianFilter\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"GaussianFilter\",\n" +
                "         \"blockType\":\"plugins.adufour.filtering.GaussianFilter\",\n" +
                "         \"elementId\":\"2\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\",\n" +
                "            \"Channel\",\n" +
                "            \"Classes\"\n" +
                "         ],\n" +
                "         \"positionX\":599,\n" +
                "         \"positionY\":769,\n" +
                "         \"blockName\":\"KMeansThresholdBlock\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"thresholds\"\n" +
                "         ],\n" +
                "         \"blockId\":\"KMeansThresholdBlock\",\n" +
                "         \"blockType\":\"plugins.adufour.thresholder.KMeansThresholdBlock\",\n" +
                "         \"elementId\":\"3\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\",\n" +
                "            \"channel\",\n" +
                "            \"Manual thresholds\",\n" +
                "            \"Treat as percentiles\"\n" +
                "         ],\n" +
                "         \"positionX\":1166,\n" +
                "         \"positionY\":669,\n" +
                "         \"blockName\":\"Thresholder\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"output\",\n" +
                "            \"ROI\"\n" +
                "         ],\n" +
                "         \"blockId\":\"Thresholder\",\n" +
                "         \"blockType\":\"plugins.adufour.thresholder.Thresholder\",\n" +
                "         \"elementId\":\"4\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Image to save\",\n" +
                "            \"DataSet Id\"\n" +
                "         ],\n" +
                "         \"positionX\":1447,\n" +
                "         \"positionY\":492,\n" +
                "         \"blockName\":\"OmeroImageSaveToDataSet\",\n" +
                "         \"Outputs\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockId\":\"OmeroImageSaveToDataSet\",\n" +
                "         \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\",\n" +
                "         \"elementId\":\"5\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"name\":\"ExtractAndThreshold\",\n" +
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
                "         \"targetPort\":\"input\",\n" +
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
                "               0.2,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"output\",\n" +
                "         \"targetBlock\":\"3\",\n" +
                "         \"targetPort\":\"Input\",\n" +
                "         \"sourceBlock\":\"2\",\n" +
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
                "               0.25,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"Extracted\",\n" +
                "         \"targetBlock\":\"4\",\n" +
                "         \"targetPort\":\"Input\",\n" +
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
                "               0.2,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"thresholds\",\n" +
                "         \"targetBlock\":\"4\",\n" +
                "         \"targetPort\":\"Manual thresholds\",\n" +
                "         \"sourceBlock\":\"3\",\n" +
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
                "               0.6000000000000001,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"output\",\n" +
                "         \"targetBlock\":\"5\",\n" +
                "         \"targetPort\":\"Image to save\",\n" +
                "         \"sourceBlock\":\"4\",\n" +
                "         \"anchors\":[  \n" +
                "            [  \n" +
                "               1,\n" +
                "               0.3333333333333333,\n" +
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