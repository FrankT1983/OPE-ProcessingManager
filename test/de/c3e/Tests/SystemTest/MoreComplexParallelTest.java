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
 * Created by Frank on 10.03.2017.
 */
public class MoreComplexParallelTest
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
    }


    // Not to self: this test takes nearly 50 seconds
    @Test
    public void AddSomeStuffAndSetToAverage()
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

        String graphParameterJson =  ExtractChannelWorkflowAndParameters(inputFile,outputFile );

        MultipleInstancesTest.RunFromFilesJsonDumpResults(5,graphParameterJson,false,"TestResults/");

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);


        for (int c = 0;c<1;c++)
        {
            double[] resultData = TestTools.DataFromFile(outputFile, c);
            assertNotNull(resultData);
        }
    }


    private String ExtractChannelWorkflowAndParameters(File inputFile, File outputFile)
    {
        // do replacements for json
        String inputFilePath =  JSONObject.escape(inputFile.getAbsolutePath());
        String outFilePath =  JSONObject.escape(outputFile.getAbsolutePath());

        return "{  \n" +
                "   \"intermediates\":false,\n" +
                "   \"blocks\":[  \n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"ImageId\"\n" +
                "         ],\n" +
                "         \"positionX\":340,\n" +
                "         \"positionY\":202,\n" +
                "         \"blockName\":\"OmeroImage\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"LoadedImage\"\n" +
                "         ],\n" +
                "         \"blockId\":\"OmeroImage\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\",\n" +
                "         \"elementId\":\"0\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\",\n" +
                "            \"Value\"\n" +
                "         ],\n" +
                "         \"positionX\":528,\n" +
                "         \"positionY\":441,\n" +
                "         \"blockName\":\"AddXBlock\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"AddXBlock\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"de.c3e.BlockTemplates.Examples.AddXBlock\",\n" +
                "         \"elementId\":\"1\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\"\n" +
                "         ],\n" +
                "         \"positionX\":886,\n" +
                "         \"positionY\":442,\n" +
                "         \"blockName\":\"SetChannelToAverage\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"SetChannelToAverage\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"de.c3e.BlockTemplates.Examples.SetChannelToAverage\",\n" +
                "         \"elementId\":\"2\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\",\n" +
                "            \"Value\"\n" +
                "         ],\n" +
                "         \"positionX\":480,\n" +
                "         \"positionY\":669,\n" +
                "         \"blockName\":\"AddXBlock\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"AddXBlock\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"de.c3e.BlockTemplates.Examples.AddXBlock\",\n" +
                "         \"elementId\":\"3\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\"\n" +
                "         ],\n" +
                "         \"positionX\":732,\n" +
                "         \"positionY\":611,\n" +
                "         \"blockName\":\"SetChannelToAverage\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"SetChannelToAverage\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"de.c3e.BlockTemplates.Examples.SetChannelToAverage\",\n" +
                "         \"elementId\":\"4\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\"\n" +
                "         ],\n" +
                "         \"positionX\":756,\n" +
                "         \"positionY\":830,\n" +
                "         \"blockName\":\"SetChannelToAverage\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"SetChannelToAverage\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"de.c3e.BlockTemplates.Examples.SetChannelToAverage\",\n" +
                "         \"elementId\":\"5\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Image to save\",\n" +
                "            \"DataSet Id\"\n" +
                "         ],\n" +
                "         \"positionX\":1217,\n" +
                "         \"positionY\":736,\n" +
                "         \"blockName\":\"OmeroImageSaveToDataSet\",\n" +
                "         \"Outputs\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockId\":\"OmeroImageSaveToDataSet\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\",\n" +
                "         \"elementId\":\"6\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"positionX\":118,\n" +
                "         \"positionY\":852,\n" +
                "         \"blockName\":\"Constant\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Value\"\n" +
                "         ],\n" +
                "         \"blockId\":\"Constant\",\n" +
                "         \"inputList\":[  \n" +
                "            {  \n" +
                "               \"id\":\"Name\",\n" +
                "               \"value\":\"To Add\"\n" +
                "            },\n" +
                "            {  \n" +
                "               \"id\":\"Value\",\n" +
                "               \"value\":\"25\"\n" +
                "            }\n" +
                "         ],\n" +
                "         \"blockType\":\"Constant\",\n" +
                "         \"elementId\":\"7\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"name\":\"ParallelTest\",\n" +
                "   \"parameters\":[  \n" +
                "      [  \n" +
                "         \"7\",\n" +
                "         \"Value\",\n" +
                "         \"25\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"7\",\n" +
                "         \"Name\",\n" +
                "         \"To Add\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"6\",\n" +
                "         \"Value\",\n" +
                "      \""+ outFilePath +"\",\n" +
                "         \"out\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"0\",\n" +
                "         \"Value\",\n" +
                "      \""+inputFilePath+"\",\n" +
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
                "               0.5,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"Output\",\n" +
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
                "         \"targetBlock\":\"4\",\n" +
                "         \"targetPort\":\"Input\",\n" +
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
                "               0.5,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"Output\",\n" +
                "         \"targetBlock\":\"5\",\n" +
                "         \"targetPort\":\"Input\",\n" +
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
                "         \"sourcePort\":\"Output\",\n" +
                "         \"targetBlock\":\"6\",\n" +
                "         \"targetPort\":\"Image to save\",\n" +
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
                "         \"sourcePort\":\"Value\",\n" +
                "         \"targetBlock\":\"3\",\n" +
                "         \"targetPort\":\"Value\",\n" +
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
                "               0.6666666666666666,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"Value\",\n" +
                "         \"targetBlock\":\"1\",\n" +
                "         \"targetPort\":\"Value\",\n" +
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
                "               0.6666666666666666,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      }\n" +
                "   ],\n" +
                "   \"runId\":\"9b5627bc-b8d7-419b-81ab-42f9006229ac\"\n" +
                "}";
    }
}
