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
 * Created by Frank on 17.01.2017.
 */
public class ConstantChannelTest
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
        TestTools.StandardUnitTestPreperatins();
    }


    @Test
    public void ExtractChannel_FromJson()
    {
        int sizeX=5;
        int sizeY=5;
        int sizeC=3;
        byte[] ch1 = new byte[sizeX * sizeY];
        byte[] ch2 = new byte[sizeX * sizeY];
        byte[] ch3 = new byte[sizeX * sizeY];
        byte[][] img = new byte[3][];
        img[0] = ch1;
        img[1] = ch2;
        img[2] = ch3;
        for (byte c = 0; c < sizeC; c++)
        {
            for (byte i = 0; i < sizeX * sizeY; i++)
            {
                img[c][i] = (byte)(i + c);
            }
        }

        final int channelToExtract = 2;
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

        String graphParameterJson =  ExtractChannelWorkflowAndParameters(inputFile,outputFile );

        MultipleInstancesTest.RunFromFilesJson(5,graphParameterJson);

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);


        double[] resultData = TestTools.DataFromFile(outputFile,0);
        assertNotNull(resultData);
        assertEquals(sizeX*sizeY, resultData.length);
        for (byte i = 0; i < sizeX * sizeY; i++)
        {
            assertEquals((byte)(i + channelToExtract) , (byte)resultData[i]);
        }

        // can't really test more, than if something went through the whole pipeline.
        // todo: make reference image => test with that
    }


    private String ExtractChannelWorkflowAndParameters(File inputFile, File outputFile)
    {
        // do replacements for json
        String inputFilePath =  JSONObject.escape(inputFile.getAbsolutePath());
        String outFilePath =  JSONObject.escape(outputFile.getAbsolutePath());

        return "{  \n" +
                "  \"intermediates\":true,\n" +
                "  \"blocks\":[  \n" +
                "    {  \n" +
                "      \"Inputs\":[  \n" +
                "\n" +
                "      ],\n" +
                "      \"positionX\":393,\n" +
                "      \"positionY\":548,\n" +
                "      \"blockName\":\"Constant\",\n" +
                "      \"Outputs\":[  \n" +
                "        \"Value\"\n" +
                "      ],\n" +
                "      \"blockId\":\"Constant\",\n" +
                "      \"inputList\":[  \n" +
                "        {  \n" +
                "          \"id\":\"Name\",\n" +
                "          \"value\":\"Channel To Extract\"\n" +
                "        },\n" +
                "        {  \n" +
                "          \"id\":\"Value\",\n" +
                "          \"value\":\"2\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"blockType\":\"Constant\",\n" +
                "      \"elementId\":\"0\"\n" +
                "    },\n" +
                "    {  \n" +
                "      \"Inputs\":[  \n" +
                "        \"ImageId\"\n" +
                "      ],\n" +
                "      \"positionX\":435,\n" +
                "      \"positionY\":185,\n" +
                "      \"blockName\":\"OmeroImage\",\n" +
                "      \"Outputs\":[  \n" +
                "        \"LoadedImage\"\n" +
                "      ],\n" +
                "      \"blockId\":\"OmeroImage\",\n" +
                "      \"inputList\":[  \n" +
                "\n" +
                "      ],\n" +
                "      \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\",\n" +
                "      \"elementId\":\"1\"\n" +
                "    },\n" +
                "    {  \n" +
                "      \"Inputs\":[  \n" +
                "        \"Image to save\",\n" +
                "        \"DataSet Id\"\n" +
                "      ],\n" +
                "      \"positionX\":1130,\n" +
                "      \"positionY\":408,\n" +
                "      \"blockName\":\"OmeroImageSaveToDataSet\",\n" +
                "      \"Outputs\":[  \n" +
                "\n" +
                "      ],\n" +
                "      \"blockId\":\"OmeroImageSaveToDataSet\",\n" +
                "      \"inputList\":[  \n" +
                "\n" +
                "      ],\n" +
                "      \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\",\n" +
                "      \"elementId\":\"2\"\n" +
                "    },\n" +
                "    {  \n" +
                "      \"Inputs\":[  \n" +
                "        \"Sequence\",\n" +
                "        \"Channel\"\n" +
                "      ],\n" +
                "      \"positionX\":720,\n" +
                "      \"positionY\":340,\n" +
                "      \"blockName\":\"ExtractChannel\",\n" +
                "      \"Outputs\":[  \n" +
                "        \"Extracted\"\n" +
                "      ],\n" +
                "      \"blockId\":\"ExtractChannel\",\n" +
                "      \"inputList\":[  \n" +
                "\n" +
                "      ],\n" +
                "      \"blockType\":\"plugins.tprovoost.sequenceblocks.extract.ExtractChannel\",\n" +
                "      \"elementId\":\"3\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"name\":\"Fix Channel Extract\",\n" +
                "  \"parameters\":[  \n" +
                "    [  \n" +
                "      \"2\",\n" +
                "      \"Value\",\n" +
                "      \""+ outFilePath +"\",\n" +
                "      \"out\"\n" +
                "    ],\n" +
                "    [  \n" +
                "      \"0\",\n" +
                "      \"Value\",\n" +
                "      \"2\",\n" +
                "      \"in\"\n" +
                "    ],\n" +
                "    [  \n" +
                "      \"0\",\n" +
                "      \"Name\",\n" +
                "      \"Channel To Extract\",\n" +
                "      \"in\"\n" +
                "    ],\n" +
                "    [  \n" +
                "      \"1\",\n" +
                "      \"Value\",\n" +
                "      \""+inputFilePath+"\",\n" +
                "      \"out\"\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"intermediateDataSet\":751,\n" +
                "  \"links\":[  \n" +
                "    {  \n" +
                "      \"sourcePort\":\"LoadedImage\",\n" +
                "      \"targetBlock\":\"3\",\n" +
                "      \"targetPort\":\"Sequence\",\n" +
                "      \"sourceBlock\":\"1\",\n" +
                "      \"anchors\":[  \n" +
                "        [  \n" +
                "          1,\n" +
                "          0.5,\n" +
                "          1,\n" +
                "          0,\n" +
                "          0,\n" +
                "          0\n" +
                "        ],\n" +
                "        [  \n" +
                "          0,\n" +
                "          0.3333333333333333,\n" +
                "          -1,\n" +
                "          0,\n" +
                "          0,\n" +
                "          0\n" +
                "        ]\n" +
                "      ]\n" +
                "    },\n" +
                "    {  \n" +
                "      \"sourcePort\":\"Value\",\n" +
                "      \"targetBlock\":\"3\",\n" +
                "      \"targetPort\":\"Channel\",\n" +
                "      \"sourceBlock\":\"0\",\n" +
                "      \"anchors\":[  \n" +
                "        [  \n" +
                "          1,\n" +
                "          0.5,\n" +
                "          1,\n" +
                "          0,\n" +
                "          0,\n" +
                "          0\n" +
                "        ],\n" +
                "        [  \n" +
                "          0,\n" +
                "          0.6666666666666666,\n" +
                "          -1,\n" +
                "          0,\n" +
                "          0,\n" +
                "          0\n" +
                "        ]\n" +
                "      ]\n" +
                "    },\n" +
                "    {  \n" +
                "      \"sourcePort\":\"Extracted\",\n" +
                "      \"targetBlock\":\"2\",\n" +
                "      \"targetPort\":\"Image to save\",\n" +
                "      \"sourceBlock\":\"3\",\n" +
                "      \"anchors\":[  \n" +
                "        [  \n" +
                "          1,\n" +
                "          0.5,\n" +
                "          1,\n" +
                "          0,\n" +
                "          0,\n" +
                "          0\n" +
                "        ],\n" +
                "        [  \n" +
                "          0,\n" +
                "          0.3333333333333333,\n" +
                "          -1,\n" +
                "          0,\n" +
                "          0,\n" +
                "          0\n" +
                "        ]\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"runId\":\"4659570e-f8a3-410c-8108-aaf313787724\"\n" +
                "}";
    }
}

