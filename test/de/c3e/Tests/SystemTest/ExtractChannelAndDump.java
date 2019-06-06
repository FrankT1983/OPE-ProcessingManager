package de.c3e.Tests.SystemTest;

import de.c3e.ProcessManager.Main;
import de.c3e.Tests.MultipleInstancesTest;
import de.c3e.Tests.TestTools;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test a channel extract and dump intermediates
 */
public class ExtractChannelAndDump
{
    @Before
    public void SetupTest()
    {
        Main.InitIcy();
    }

    @Test
    public void ExtractChannelAndDump_from_Json()
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

        final int channelToExtract = 1;
        File inputFile = null;
        File outputFile= null;
        Path intermediateDir = null;
        try
        {
            inputFile = File.createTempFile("UnitTest", ".tiff");
            outputFile = File.createTempFile("UnitTest", ".tiff");
            inputFile.deleteOnExit();
            outputFile.deleteOnExit();
            intermediateDir = Files.createTempDirectory("UniTest");
            intermediateDir.toFile().deleteOnExit();

            TestTools.WriteMultiChannelToFile(inputFile,ch1,ch2,ch3,sizeX,sizeY);
        }
        catch (Exception e)
        {   assertTrue(false);}

        String graphParameterJson =  this.TestJson(inputFile,outputFile, channelToExtract );

        MultipleInstancesTest.RunFromFilesJsonDumpResults(5,graphParameterJson,true,intermediateDir.toString());

        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);


        double[] resultData = TestTools.DataFromFile(outputFile,0);
        assertNotNull(resultData);
        assertEquals(sizeX*sizeY, resultData.length);
        for (byte i = 0; i < sizeX * sizeY; i++)
        {
            assertEquals((byte)(i + channelToExtract) , (byte)resultData[i]);
        }

        File[] directoryListing = intermediateDir.toFile().listFiles();
        assertNotNull(directoryListing);
        for (File f : directoryListing)
        {   f.deleteOnExit();   }
        assertEquals("small objects, input image, split image, 2x Statistics", 5, directoryListing.length);
    }


    private String TestJson(File inputFile, File outputFile, int channel)
    {
        // do replacements for json
        String inputFilePath =  JSONObject.escape(inputFile.getAbsolutePath());
        String outFilePath =  JSONObject.escape(outputFile.getAbsolutePath());

        return "{  \n" +
                "  \"intermediates\":true,\n" +
                "  \"blocks\":[  \n" +
                "    {  \n" +
                "      \"Inputs\":[  \n" +
                "        \"ImageId\"\n" +
                "      ],\n" +
                "      \"positionX\":476,\n" +
                "      \"positionY\":256,\n" +
                "      \"blockName\":\"OmeroImage\",\n" +
                "      \"Outputs\":[  \n" +
                "        \"LoadedImage\"\n" +
                "      ],\n" +
                "      \"blockId\":\"0\",\n" +
                "      \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\",\n" +
                "      \"elementId\":\"0\"\n" +
                "    },\n" +
                "    {  \n" +
                "      \"Inputs\":[  \n" +
                "        \"Sequence\",\n" +
                "        \"Channel\"\n" +
                "      ],\n" +
                "      \"positionX\":763,\n" +
                "      \"positionY\":427,\n" +
                "      \"blockName\":\"ExtractChannel\",\n" +
                "      \"Outputs\":[  \n" +
                "        \"Extracted\"\n" +
                "      ],\n" +
                "      \"blockId\":\"ExtractChannel\",\n" +
                "      \"blockType\":\"plugins.tprovoost.sequenceblocks.extract.ExtractChannel\",\n" +
                "      \"elementId\":\"1\"\n" +
                "    },\n" +
                "    {  \n" +
                "      \"Inputs\":[  \n" +
                "        \"Image to save\",\n" +
                "        \"DataSet Id\"\n" +
                "      ],\n" +
                "      \"positionX\":1052,\n" +
                "      \"positionY\":630,\n" +
                "      \"blockName\":\"OmeroImageSaveToDataSet\",\n" +
                "      \"Outputs\":[  \n" +
                "\n" +
                "      ],\n" +
                "      \"blockId\":\"OmeroImageSaveToDataSet\",\n" +
                "      \"blockType\":\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\",\n" +
                "      \"elementId\":\"2\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"name\":\"ExtractChannel\",\n" +
                "  \"parameters\":[  \n" +
                "    [  \n" +
                "      \"2\",\n" +
                "      \"Value\",\n" +
                "      \""+outFilePath+"\",\n" +
                "      \"out\"\n" +
                "    ],\n" +
                "    [  \n" +
                "      \"1\",\n" +
                "      \"Channel\",\n" +
                "      \""+channel+"\",\n" +
                "      \"in\"\n" +
                "    ],\n" +
                "    [  \n" +
                "      \"0\",\n" +
                "      \"Value\",\n" +
                "      \""+inputFilePath+"\",\n" +
                "      \"out\"\n" +
                "    ]\n" +
                "  ],\n" +
                "  \"links\":[  \n" +
                "    {  \n" +
                "      \"sourcePort\":\"LoadedImage\",\n" +
                "      \"targetBlock\":\"1\",\n" +
                "      \"targetPort\":\"Sequence\",\n" +
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
                "          0.3333333333333333,\n" +
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
                "    }\n" +
                "  ],\n" +
                "  \"runId\":\"53a87218-9872-4f42-90a5-1dee1f7805a8\"\n" +
                "}";
    }
}
