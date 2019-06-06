package de.c3e.Tests.SystemTest;

import de.c3e.ProcessManager.Main;
import de.c3e.Tests.MultipleInstancesTest;
import de.c3e.Tests.TestTools;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class CellProfilerJson
{

    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void CellProfilerFromJson()
    {
        File inputFile = new File("C:\\PHD\\UnitTest\\CellProfiler\\input2\\2StacksRGB.tif");
        File outputFile= null;
        try
        {
            outputFile = File.createTempFile("UnitTest", ".txt");
            outputFile.deleteOnExit();
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  getFile(inputFile,outputFile);

        MultipleInstancesTest.RunFromFilesJson(2,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

    }

    private String getFile(File inputFile, File outputFile)
    {
        // do replacements for json
        String inputFilePath = JSONObject.escape(inputFile.getAbsolutePath());
        String outFilePath = JSONObject.escape(outputFile.getAbsolutePath());

        return "{  \n" +
                "   \"intermediates\":true,\n" +
                "   \"blocks\":[  \n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"ImageId\"\n" +
                "         ],\n" +
                "         \"positionX\":569,\n" +
                "         \"positionY\":390,\n" +
                "         \"blockName\":\"Load Image\",\n" +
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
                "            \"ImageId\",\n" +
                "            \"Data\"\n" +
                "         ],\n" +
                "         \"positionX\":1201,\n" +
                "         \"positionY\":659,\n" +
                "         \"blockName\":\"AnnotateImageWithData\",\n" +
                "         \"Outputs\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockId\":\"AnnotateImageWithData\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"AnnotateImageWithData\",\n" +
                "         \"elementId\":\"2\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\"\n" +
                "         ],\n" +
                "         \"positionX\":888,\n" +
                "         \"positionY\":496,\n" +
                "         \"blockName\":\"CellProfilerTool\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"CellProfilerTool\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"CellProfilerTool\",\n" +
                "         \"elementId\":\"3\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"name\":\"CellprofilerTestV2\",\n" +
                "   \"parameters\":[  \n" +
                "      [  \n" +
                "         \"2\",\n" +
                "         \"Value\",\n" +
                "         \""+outFilePath+"\",\n" +
                "         \"out\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"0\",\n" +
                "         \"Value\",\n" +
                "         \""+ inputFilePath +"\",\n" +
                "         \"out\"\n" +
                "      ]\n" +
                "   ],\n" +
                "   \"versions\":[  \n" +
                "\n" +
                "   ],\n" +
                "   \"intermediateDataSet\":751,\n" +
                "   \"links\":[  \n" +
                "      {  \n" +
                "         \"sourcePort\":\"LoadedImage\",\n" +
                "         \"targetBlock\":\"3\",\n" +
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
                "         \"targetBlock\":\"2\",\n" +
                "         \"targetPort\":\"Data\",\n" +
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
                "               0.6666666666666666,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      }\n" +
                "   ],\n" +
                "   \"runId\":\"6265ecfe-9cc1-47cb-b3b2-6449fc1c428d\"\n" +
                "}";
    }
}
