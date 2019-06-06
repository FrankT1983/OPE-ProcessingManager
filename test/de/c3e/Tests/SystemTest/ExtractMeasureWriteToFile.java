package de.c3e.Tests.SystemTest;

import de.c3e.ProcessManager.Main;
import de.c3e.Tests.MultipleInstancesTest;
import de.c3e.Tests.TestTools;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Extract a channel calculate a roi, measure it and write result to file
 */
public class ExtractMeasureWriteToFile
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void ThresholdAndMeasureRoi_FromJson()
    {
        final int channelToExtract = 2;

        File inputFile = new File("C:\\PHD\\UnitTest\\cells.tif");
        File outputFile= null;
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
    }


    private String ExtractChannelWorkflowAndParameters(File inputFile, File outputFile, int channel)
    {
        // do replacements for json
        String inputFilePath =  JSONObject.escape(inputFile.getAbsolutePath());
        String outFilePath =  JSONObject.escape(outputFile.getAbsolutePath());

        return "{  \n" +
                "   \"parameters\":[  \n" +

                "[  \n" +
                "         \"6\",\n" +
                "         \"measures\",\n" +
                "         \"all\",\n" +
                "         \"in\"\n" +
                "      ],\n" +

                "      [  \n" +
                "         \"4\",\n" +
                "         \"Treat as percentiles\",\n" +
                "         \"false\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"5\",\n" +
                "         \"destinationFile\",\n" +
                "         \""+outFilePath+"\",\n" +
                "         \"in\"\n" +
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
                "         \"positionX\":280,\n" +
                "         \"positionY\":250,\n" +
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
                "         \"positionX\":788,\n" +
                "         \"positionY\":268,\n" +
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
                "         \"positionX\":1238,\n" +
                "         \"positionY\":291,\n" +
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
                "         \"positionX\":356,\n" +
                "         \"positionY\":509,\n" +
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
                "         \"positionX\":820,\n" +
                "         \"positionY\":630,\n" +
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
                "            \"data\",\n" +
                "            \"destinationFile\"\n" +
                "         ],\n" +
                "         \"positionX\":886,\n" +
                "         \"positionY\":968,\n" +
                "         \"blockName\":\"DataToCsvFileBlock\",\n" +
                "         \"Outputs\":[ ],\n" +
                "         \"blockId\":\"DataToCsvFileBlock\",\n" +
                "         \"blockType\":\"DataToCsvFileBlock\",\n" +
                "         \"elementId\":\"5\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"measures\",\n" +
                "            \"Regions of interest\",\n" +
                "            \"Sequence\"\n" +
                "         ],\n" +
                "         \"positionX\":1240,\n" +
                "         \"positionY\":675,\n" +
                "         \"blockName\":\"ROIMeasures\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Workbook\"\n" +
                "         ],\n" +
                "         \"blockId\":\"ROIMeasures\",\n" +
                "         \"blockType\":\"plugins.adufour.roi.ROIMeasures\",\n" +
                "         \"elementId\":\"6\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"name\":\"For test\",\n" +
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
                "         \"targetBlock\":\"4\",\n" +
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
                "               0.2,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"Workbook\",\n" +
                "         \"targetBlock\":\"5\",\n" +
                "         \"targetPort\":\"data\",\n" +
                "         \"sourceBlock\":\"6\",\n" +
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
                "         \"targetBlock\":\"6\",\n" +
                "         \"targetPort\":\"Sequence\",\n" +
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
                "               0.75,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"ROI\",\n" +
                "         \"targetBlock\":\"6\",\n" +
                "         \"targetPort\":\"Regions of interest\",\n" +
                "         \"sourceBlock\":\"4\",\n" +
                "         \"anchors\":[  \n" +
                "            [  \n" +
                "               1,\n" +
                "               0.6666666666666666,\n" +
                "               1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ],\n" +
                "            [  \n" +
                "               0,\n" +
                "               0.5,\n" +
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
