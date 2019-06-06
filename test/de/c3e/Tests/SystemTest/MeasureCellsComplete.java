package de.c3e.Tests.SystemTest;

import de.c3e.ProcessManager.Main;
import de.c3e.Tests.MultipleInstancesTest;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Test the complete workflow of extracting channel => finding cells and measuring them
 */
public class MeasureCellsComplete
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
    }


    @Test
    public void MeasureCellsComplete_FromJson()
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

        MultipleInstancesTest.RunFromFilesJsonDumpResults(5,graphParameterJson, true, "c:\\tmp\\inter\\");

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
                "      [  \n" +
                "         \"8\",\n" +
                "         \"parameter\",\n" +
                "         \"3\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"2\",\n" +
                "         \"Classes\",\n" +
                "         \"2\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"6\",\n" +
                "         \"destinationFile\",\n" +
                "         \""+outFilePath+"\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"7\",\n" +
                "         \"parameter\",\n" +
                "         \"0\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"4\",\n" +
                "         \"extract mode\",\n" +
                "         \"all\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"3\",\n" +
                "         \"Treat as percentiles\",\n" +
                "         \"false\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"0\",\n" +
                "         \"Value\",\n" +
                "         \""+inputFilePath+"\",\n" +
                "         \"out\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"5\",\n" +
                "         \"measures\",\n" +
                "         \"all\",\n" +
                "         \"in\"\n" +
                "      ]\n" +
                "   ],\n" +
                "   \"blocks\":[  \n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"ImageId\"\n" +
                "         ],\n" +
                "         \"positionX\":485,\n" +
                "         \"positionY\":276,\n" +
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
                "            \"input\",\n" +
                "            \"filterX\",\n" +
                "            \"filterY\",\n" +
                "            \"filterZ\"\n" +
                "         ],\n" +
                "         \"positionX\":841,\n" +
                "         \"positionY\":370,\n" +
                "         \"blockName\":\"GaussianFilter\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"GaussianFilter\",\n" +
                "         \"blockType\":\"plugins.adufour.filtering.GaussianFilter\",\n" +
                "         \"elementId\":\"1\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\",\n" +
                "            \"Channel\",\n" +
                "            \"Classes\"\n" +
                "         ],\n" +
                "         \"positionX\":546,\n" +
                "         \"positionY\":752,\n" +
                "         \"blockName\":\"KMeansThresholdBlock\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"thresholds\"\n" +
                "         ],\n" +
                "         \"blockId\":\"KMeansThresholdBlock\",\n" +
                "         \"blockType\":\"plugins.adufour.thresholder.KMeansThresholdBlock\",\n" +
                "         \"elementId\":\"2\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\",\n" +
                "            \"channel\",\n" +
                "            \"Manual thresholds\",\n" +
                "            \"Treat as percentiles\"\n" +
                "         ],\n" +
                "         \"positionX\":1086,\n" +
                "         \"positionY\":719,\n" +
                "         \"blockName\":\"Thresholder\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"output\",\n" +
                "            \"ROI\"\n" +
                "         ],\n" +
                "         \"blockId\":\"Thresholder\",\n" +
                "         \"blockType\":\"plugins.adufour.thresholder.Thresholder\",\n" +
                "         \"elementId\":\"3\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"input sequence\",\n" +
                "            \"extract mode\",\n" +
                "            \"value\"\n" +
                "         ],\n" +
                "         \"positionX\":1378,\n" +
                "         \"positionY\":988,\n" +
                "         \"blockName\":\"LabelExtractor\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"ROI\"\n" +
                "         ],\n" +
                "         \"blockId\":\"LabelExtractor\",\n" +
                "         \"blockType\":\"plugins.adufour.roi.LabelExtractor\",\n" +
                "         \"elementId\":\"4\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"measures\",\n" +
                "            \"Regions of interest\",\n" +
                "            \"Sequence\"\n" +
                "         ],\n" +
                "         \"positionX\":1834,\n" +
                "         \"positionY\":674,\n" +
                "         \"blockName\":\"ROIMeasures\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Workbook\"\n" +
                "         ],\n" +
                "         \"blockId\":\"ROIMeasures\",\n" +
                "         \"blockType\":\"plugins.adufour.roi.ROIMeasures\",\n" +
                "         \"elementId\":\"5\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"data\",\n" +
                "            \"destinationFile\"\n" +
                "         ],\n" +
                "         \"positionX\":2239,\n" +
                "         \"positionY\":712,\n" +
                "         \"blockName\":\"DataToCsvFileBlock\",\n" +
                "         \"Outputs\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockId\":\"DataToCsvFileBlock\",\n" +
                "         \"blockType\":\"DataToCsvFileBlock\",\n" +
                "         \"elementId\":\"6\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"parameter\"\n" +
                "         ],\n" +
                "         \"positionX\":351,\n" +
                "         \"positionY\":1040,\n" +
                "         \"blockName\":\"SameParameter\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"parameter\"\n" +
                "         ],\n" +
                "         \"blockId\":\"LabelExtractor\",\n" +
                "         \"blockType\":\"SameParameter\",\n" +
                "         \"elementId\":\"7\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"parameter\"\n" +
                "         ],\n" +
                "         \"positionX\":421,\n" +
                "         \"positionY\":399,\n" +
                "         \"blockName\":\"SameParameter\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"parameter\"\n" +
                "         ],\n" +
                "         \"blockId\":\"LabelExtractor\",\n" +
                "         \"blockType\":\"SameParameter\",\n" +
                "         \"elementId\":\"8\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"name\":\"MeasureRoiToCsv\",\n" +
                "   \"links\":[  \n" +
                "      {  \n" +
                "         \"sourcePort\":\"LoadedImage\",\n" +
                "         \"targetBlock\":\"1\",\n" +
                "         \"targetPort\":\"input\",\n" +
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
                "         \"targetBlock\":\"2\",\n" +
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
                "         \"targetBlock\":\"3\",\n" +
                "         \"targetPort\":\"Manual thresholds\",\n" +
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
                "         \"targetBlock\":\"3\",\n" +
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
                "         \"sourcePort\":\"output\",\n" +
                "         \"targetBlock\":\"4\",\n" +
                "         \"targetPort\":\"input sequence\",\n" +
                "         \"sourceBlock\":\"3\",\n" +
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
                "               0.25,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"ROI\",\n" +
                "         \"targetBlock\":\"5\",\n" +
                "         \"targetPort\":\"Regions of interest\",\n" +
                "         \"sourceBlock\":\"4\",\n" +
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
                "               0.5,\n" +
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
                "         \"sourcePort\":\"Workbook\",\n" +
                "         \"targetBlock\":\"6\",\n" +
                "         \"targetPort\":\"data\",\n" +
                "         \"sourceBlock\":\"5\",\n" +
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
                "         \"sourcePort\":\"parameter\",\n" +
                "         \"targetBlock\":\"2\",\n" +
                "         \"targetPort\":\"Channel\",\n" +
                "         \"sourceBlock\":\"7\",\n" +
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
                "               0.5,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"parameter\",\n" +
                "         \"targetBlock\":\"3\",\n" +
                "         \"targetPort\":\"channel\",\n" +
                "         \"sourceBlock\":\"7\",\n" +
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
                "               0.4,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"parameter\",\n" +
                "         \"targetBlock\":\"4\",\n" +
                "         \"targetPort\":\"value\",\n" +
                "         \"sourceBlock\":\"7\",\n" +
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
                "         \"sourcePort\":\"parameter\",\n" +
                "         \"targetBlock\":\"1\",\n" +
                "         \"targetPort\":\"filterZ\",\n" +
                "         \"sourceBlock\":\"7\",\n" +
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
                "               0.8,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"parameter\",\n" +
                "         \"targetBlock\":\"1\",\n" +
                "         \"targetPort\":\"filterX\",\n" +
                "         \"sourceBlock\":\"8\",\n" +
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
                "               0.4,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"parameter\",\n" +
                "         \"targetBlock\":\"1\",\n" +
                "         \"targetPort\":\"filterY\",\n" +
                "         \"sourceBlock\":\"8\",\n" +
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
                "      }\n" +
                "   ]\n" +
                "}";
    }
}
