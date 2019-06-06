package de.c3e.Tests.SystemTest;

import de.c3e.ProcessManager.Main;
import de.c3e.Tests.MultipleInstancesTest;
import de.c3e.Tests.TestTools;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class ApplicationExampleTest
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }

    public void ApplicationExampleRunMultiple()
    {
        for(int i=0;i<100;i++)
        {
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++ " + i + "   +++++++++++++++++++++++++++++++++++++++++++++++++");
            ApplicationExample();
        }
    }

    @Test
    public void ApplicationExample()
    {
        //File inputFile = new File("C:\\PHD\\UnitTest\\CellProfiler\\4TColor.tif");
        //File inputFile = new File("C:\\PHD\\UnitTest\\10Timepoints.tif");
        File inputFile = new File("C:\\PHD\\UnitTest\\20TStackCell3ConvedSmall.tif");
        //File inputFile = new File("C:\\PHD\\UnitTest\\10TimepointsConved.tif");
        File outputFileGraph= null;
        File outputFileBeatified= null;
        try
        {
            outputFileGraph = File.createTempFile("UnitTest", ".png");
            System.out.println(outputFileGraph.getAbsolutePath());
            outputFileGraph.deleteOnExit();

            outputFileBeatified = File.createTempFile("UnitTest", ".png");
            System.out.println(outputFileGraph.getAbsolutePath());
            outputFileBeatified.deleteOnExit();
        }
        catch (Exception e)
        {   assertTrue(false);}


        String graphParameterJson = ConstructWorkflowJason(inputFile,outputFileGraph,outputFileBeatified);

        //AverageExecutionTimeBalancer.MinPortion = 0.5;
        MultipleInstancesTest.RunFromFilesJson(10,graphParameterJson);

        assertTrue(outputFileGraph.exists());
        assertTrue(outputFileBeatified.exists());

    }

    private String ConstructWorkflowJason(File inputFile, File outputFileGraph, File outputFileBeatified)
    {
        // do replacements for json
        String inputFilePath = JSONObject.escape(inputFile.getAbsolutePath());
        String outputFileGraphPath = JSONObject.escape(outputFileGraph.getAbsolutePath());
        String outputFileBeatifiedPath = JSONObject.escape(outputFileBeatified.getAbsolutePath());


        return "{  \n" +
                "   \"intermediates\":true,\n" +
                "   \"blocks\":[  \n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"ImageId\"\n" +
                "         ],\n" +
                "         \"positionX\":395,\n" +
                "         \"positionY\":439,\n" +
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
                "            \"Input\"\n" +
                "         ],\n" +
                "         \"positionX\":727,\n" +
                "         \"positionY\":439,\n" +
                "         \"GitFilePath\":\"ParallelPlaneDeconvolutionTool.java\",\n" +
                "         \"blockName\":\"Deconvolution\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"Deconvolution\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"GitRepo\":\"https://git.inf-ra.uni-jena.de/xo46rud/OpePlugins.git\",\n" +
                "         \"blockType\":\"DeconvolutionTool\",\n" +
                "         \"elementId\":\"1\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\",\n" +
                "            \"Value\"\n" +
                "         ],\n" +
                "         \"positionX\":1036,\n" +
                "         \"positionY\":686,\n" +
                "         \"blockName\":\"Add Value\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"AddXBlock\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"de.c3e.BlockTemplates.Examples.AddXBlock\",\n" +
                "         \"elementId\":\"2\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\"\n" +
                "         ],\n" +
                "         \"positionX\":1608,\n" +
                "         \"positionY\":685,\n" +
                "         \"blockName\":\"CyanConversion\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"CyanConversion\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"CyanConversion\",\n" +
                "         \"elementId\":\"4\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\",\n" +
                "            \"TimeIndex\"\n" +
                "         ],\n" +
                "         \"positionX\":712,\n" +
                "         \"positionY\":667,\n" +
                "         \"blockName\":\"TimePointSelection\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"TimePointSelection\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"TimePointSelection\",\n" +
                "         \"elementId\":\"5\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\"\n" +
                "         ],\n" +
                "         \"positionX\":1381,\n" +
                "         \"positionY\":438,\n" +
                "         \"blockName\":\"Plot CellCount\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"PlotCellCount\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"PlotCellCount\",\n" +
                "         \"elementId\":\"6\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\"\n" +
                "         ],\n" +
                "         \"positionX\":1090,\n" +
                "         \"positionY\":439,\n" +
                "         \"blockName\":\"CellProfiler\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"CellProfiler\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"CellProfilerTool\",\n" +
                "         \"elementId\":\"7\"\n" +
                "      },\n" +
                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Image to save\",\n" +
                "            \"DataSet Id\"\n" +
                "         ],\n" +
                "         \"positionX\":1776,\n" +
                "         \"positionY\":456,\n" +
                "         \"blockName\":\"Store to Dataset\",\n" +
                "         \"Outputs\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockId\":\"OmeroImageSaveToDataSet\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\",\n" +
                "         \"elementId\":\"8\"\n" +
                "      },\n" +

                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Input\"\n" +
                "         ],\n" +
                "         \"positionX\":1271,\n" +
                "         \"positionY\":686,\n" +
                "         \"blockName\":\"HistogramLinearization\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\"HistogramLinearization\",\n" +
                "         \"inputList\":[  \n" +
                "         ],\n" +
                "         \"blockType\":\"HistogramLinearization\",\n" +
                "         \"elementId\":\"9\"\n" +
                "      },\n" +

                "      {  \n" +
                "         \"Inputs\":[  \n" +
                "            \"Image to save\",\n" +
                "            \"DataSet Id\"\n" +
                "         ],\n" +
                "         \"positionX\":1895,\n" +
                "         \"positionY\":705,\n" +
                "         \"blockName\":\"Store to Dataset\",\n" +
                "         \"Outputs\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockId\":\"OmeroImageSaveToDataSet\",\n" +
                "         \"inputList\":[  \n" +
                "\n" +
                "         ],\n" +
                "         \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\",\n" +
                "         \"elementId\":\"10\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"name\":\"ExampleWorkflow\",\n" +
                "   \"parameters\":[  \n" +
                "      [  \n" +
                "         \"5\",\n" +
                "         \"TimeIndex\",\n" +
                "         \"2\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"2\",\n" +
                "         \"Value\",\n" +
                "         \"-20\",\n" +
                "         \"in\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"8\",\n" +
                "         \"Value\",\n" +
                "         \"" + outputFileGraphPath+ "\",\n" +
                "         \"out\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"10\",\n" +
                "         \"Value\",\n" +
                "         \"" + outputFileGraphPath+ "\",\n" +
                "         \"out\"\n" +
                "      ],\n" +
                "      [  \n" +
                "         \"0\",\n" +
                "         \"Value\",\n" +
                "         \""+inputFilePath+"\",\n" +
                "         \"out\"\n" +
                "      ]\n" +
                "   ],\n" +
                "   \"versions\":[  \n" +
                "      [  \n" +
                "         \"1\",\n" +
                "         \"a48ede8dbb81f8f8ca3934f228c3665ca1bd8d1a\"\n" +
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
                "         \"targetBlock\":\"7\",\n" +
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
                "         \"targetBlock\":\"6\",\n" +
                "         \"targetPort\":\"Input\",\n" +
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
                "         \"sourcePort\":\"Output\",\n" +
                "         \"targetBlock\":\"8\",\n" +
                "         \"targetPort\":\"Image to save\",\n" +
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
                "         \"sourcePort\":\"Output\",\n" +
                "         \"targetBlock\":\"5\",\n" +
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
                "         \"sourcePort\":\"Output\",\n" +
                "         \"targetBlock\":\"9\",\n" +
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
                "         \"targetBlock\":\"4\",\n" +
                "         \"targetPort\":\"Input\",\n" +
                "         \"sourceBlock\":\"9\",\n" +
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
                "         \"targetBlock\":\"10\",\n" +
                "         \"targetPort\":\"Image to save\",\n" +
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
                "               0.3333333333333333,\n" +
                "               -1,\n" +
                "               0,\n" +
                "               0,\n" +
                "               0\n" +
                "            ]\n" +
                "         ]\n" +
                "      }\n" +
                "   ],\n" +
                "   \"runId\":\"1b4befc3-aee5-43bc-9475-cd2591b69cb8\"\n" +
                "}";
    }


}
