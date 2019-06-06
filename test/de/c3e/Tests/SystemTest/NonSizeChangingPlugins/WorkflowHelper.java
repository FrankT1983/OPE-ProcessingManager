package de.c3e.Tests.SystemTest.NonSizeChangingPlugins;

import org.json.simple.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Frank on 27.03.2017.
 */
public class WorkflowHelper
{
    static public String PassThroughOneBlockWorkflowAndParameters(File inputFile, File outputFile, String blockName)
    {
        return PassThroughOneBlockWorkflowAndParameters(inputFile,outputFile,blockName,new ArrayList<String[]>());
    }

    static public String PassThroughOneBlockWorkflowAndParameters(File inputFile, File outputFile, String blockName, List<String[]> extraParams)
    {
        // do replacements for json
        String inputFilePath =  JSONObject.escape(inputFile.getAbsolutePath());
        String outFilePath =  JSONObject.escape(outputFile.getAbsolutePath());

        String result =  "{  \n" +
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
                "            \"Input\",\n" ;

        for (String[] par: extraParams)
        {
            if (par.length != 3)
            {continue;}

            result += "         \""+ par[0]+"\",\n";
        }


        result +=                 "         ],\n" +
                "         \"positionX\":745,\n" +
                "         \"positionY\":299,\n" +
                "         \"blockName\":\""+blockName+"\",\n" +
                "         \"Outputs\":[  \n" +
                "            \"Output\"\n" +
                "         ],\n" +
                "         \"blockId\":\""+blockName+"\",\n" +
                "         \"inputList\":[  \n" +
                "         ],\n" +
                "         \"blockType\":\""+blockName+"\",\n" +
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
                "   \"name\":\"UnitTestWorkflow\",\n" +
                "   \"parameters\":[  \n" +
                "      [  \n" +
                "         \"2\",\n" +
                "         \"Value\",\n" +
                "         \""+outFilePath+"\",\n" +
                "         \"out\"\n" +
                "      ],\n";

        for (String[] par: extraParams)
        {
            if (par.length != 3)
            {continue;}

            result +=" [  \n" +
                    "         \"1\",\n" +
                    "         \""+ par[0]+"\",\n" +
                    "         \""+ par[1] +"\",\n" +
                    "         \""+ par[2]+"\"\n" +
                    "      ],\n" ;
        }

        result +=                "      [  \n" +
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

        return result;
    }

}
