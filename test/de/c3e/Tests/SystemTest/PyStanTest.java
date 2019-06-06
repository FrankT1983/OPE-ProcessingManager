package de.c3e.Tests.SystemTest;

import de.c3e.ProcessManager.Main;
import de.c3e.Tests.MultipleInstancesTest;
import de.c3e.Tests.TestTools;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class PyStanTest
{

    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    @Test
    public void TestWithPyStan()
    {
        File inputFile = new File("C:\\PHD\\UnitTest\\StanInputData.json");
        File inputFile2 = new File("C:\\PHD\\UnitTest\\pystanExampleModel.stan");
        File outputFile= null;
        try
        {
            outputFile = File.createTempFile("UnitTest", ".txt");
            outputFile.deleteOnExit();
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  constructWorkflowJson(inputFile, inputFile2 ,outputFile);

        MultipleInstancesTest.RunFromFilesJson(2,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }

    private String constructWorkflowJson(File inputFile, File modelFile,  File outputFile)
    {
        // do replacements for json
        String inputFilePath = JSONObject.escape(inputFile.getAbsolutePath());
        String modelFilePath = JSONObject.escape(inputFile.getAbsolutePath());
        String outFilePath = JSONObject.escape(outputFile.getAbsolutePath());

        return "{  \n" +
                "   \"intermediates\":true,\n" +
                "   \"blocks\":[  \n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"ImageId\"\n" +
                "         ],         \n" +
                "         \"blockName\":\"Load File\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"LoadedFile\"\n" +
                "         ],\n" +
                "         \"blockId\":\"OmeroFile\",\n" +
                "         \"inputList\":[],\n" +
                "         \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroTextFileInputBlock\",\n" +
                "         \"elementId\":\"0\"\n" +
                "      },\t  \n" +
                "\t  {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"FileId\"\n" +
                "         ],         \n" +
                "         \"blockName\":\"Load File\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"LoadedFile\"\n" +
                "         ],\n" +
                "         \"blockId\":\"OmeroFile\",\n" +
                "         \"inputList\":[],\n" +
                "         \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroTextFileInputBlock\",\n" +
                "         \"elementId\":\"10\"\n" +
                "      },\n" +
                "\t  \n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"ImageId\",\n" +
                "            \"Data\"\n" +
                "         ],\n" +
                "       \n" +
                "         \"blockName\":\"AnnotateImageWithData\",\n" +
                "         \"Outputs\":[],\n" +
                "         \"blockId\":\"AnnotateImageWithData\",\n" +
                "         \"inputList\":[],\n" +
                "         \"blockType\":\"AnnotateImageWithData\",\n" +
                "         \"elementId\":\"2\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"ModelCode\",\n" +
                "            \"Input\"\n" +
                "         ],        \n" +
                "         \"blockName\":\"PyStanTool\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"PyStanTool\",\n" +
                "         \"inputList\":[],\n" +
                "         \"blockType\":\"PyStanTool\",\n" +
                "         \"elementId\":\"3\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"name\":\"PyStanUnitTest\",\n" +
                "   \"parameters\":[  \n" +
                "      [  \n" +
                "         \"2\",\n" +
                "         \"Value\",\n" +
                "         \""+outFilePath+"\",\n" +
                "         \"out\"\n" +
                "      ],\n" +
                "\t  [  \n" +
                "         \"10\",\n" +
                "         \"Value\",\n" +
                "         \""+modelFilePath+"\",\n" +
                "         \"out\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"0\",\n" +
                "         \"Value\",\n" +
                "         \""+inputFilePath+"\",\n" +
                "         \"out\"\n" +
                "      ]\n" +
                "   ],\n" +
                "   \"versions\":[],\n" +
                "   \"intermediateDataSet\":751,\n" +
                "   \"links\":[  \n" +
                "      {  \n" +
                "         \"sourcePort\":\"LoadedImage\",\n" +
                "         \"sourceBlock\":\"0\",\n" +
                "         \"targetBlock\":\"3\",\n" +
                "         \"targetPort\":\"Input\",         \n" +
                "      },\n" +
                "      {  \n" +
                "         \"sourcePort\":\"Output\",\n" +
                "         \"targetBlock\":\"2\",\n" +
                "         \"targetPort\":\"Data\",\n" +
                "         \"sourceBlock\":\"3\" \n" +
                "      }\n" +
                "\t  ,\n" +
                "      {  \n" +
                "         \"sourcePort\":\"LoadedFile\",\n" +
                "         \"sourceBlock\":\"10\",\n" +
                "         \"targetPort\":\"ModelCode\",\n" +
                "         \"targetBlock\":\"3\",\n" +
                "      }\n" +
                "   ],\n" +
                "   \"runId\":\"just-for-unit-tests\"\n" +
                "}";
    }
}
