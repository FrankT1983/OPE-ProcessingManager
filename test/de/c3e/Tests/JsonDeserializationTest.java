package de.c3e.Tests;

/**
 * unit tests for checking json deserialization
 */

import de.c3e.ProcessManager.DataTypes.BlockGraph;
import de.c3e.ProcessManager.DataTypes.GraphReader;
import de.c3e.ProcessManager.DataTypes.ScriptInputParameters;
import de.c3e.ProcessManager.Utils.ParameterReader;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;
public class JsonDeserializationTest
{

    @Test
    public void TestGraph()
    {
        String input = "{\"parameters\": [[\"2\", \"DataSet Id\", \"601\"], [\"1\", \"Channel\", \"0\"], [\"0\", \"ImageId\", \"351\"]], \"blocks\": [{\"Inputs\": [\"ImageId\"], \"positionX\": 334, \"positionY\": 152, \"blockName\": \"OmeroImage\", \"Outputs\": [\"LoadedImage\"], \"blockId\": \"0\", \"blockType\": \"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\", \"elementId\": \"0\"}, {\"Inputs\": [\"Input\", \"Channel\"], \"positionX\": 380, \"positionY\": 453, \"blockName\": \"HistogramEqualization\", \"Outputs\": [\"Equalized sequence\"], \"blockId\": \"HistogramEqualization\", \"blockType\": \"plugins.tlecomte.histogram.HistogramEqualization\", \"elementId\": \"1\"}, {\"Inputs\": [\"Image to save\", \"DataSet Id\"], \"positionX\": 926, \"positionY\": 291, \"blockName\": \"OmeroImageSaveToDataSet\", \"Outputs\": [], \"blockId\": \"OmeroImageSaveToDataSet\", \"blockType\": \"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\", \"elementId\": \"2\"}], \"links\": [{\"sourcePort\": \"LoadedImage\", \"targetBlock\": \"1\", \"targetPort\": \"Input\", \"sourceBlock\": \"0\", \"anchors\": [[1, 0.5, 1, 0, 0, 0], [0, 0.3333333333333333, -1, 0, 0, 0]]}, {\"sourcePort\": \"Equalized sequence\", \"targetBlock\": \"2\", \"targetPort\": \"Image to save\", \"sourceBlock\": \"1\", \"anchors\": [[1, 0.5, 1, 0, 0, 0], [0, 0.3333333333333333, -1, 0, 0, 0]]}]}";

        BlockGraph graph = GraphReader.GraphFromJson(input);

        assertNotNull(graph);
        assertNotNull(graph.AllBlocks);
        assertEquals(3,graph.AllBlocks.size());

        assertTrue(TestTools.HasType(graph.AllBlocks, "plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock"));
        assertTrue(TestTools.HasType(graph.AllBlocks, "plugins.tlecomte.histogram.HistogramEqualization"));
        assertTrue(TestTools.HasType(graph.AllBlocks, "plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet"));

        assertNotNull(graph.Links);
        assertEquals(2,graph.Links.size());


        assertTrue(TestTools.HasLink(graph,"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock" , "LoadedImage", "plugins.tlecomte.histogram.HistogramEqualization" ,"Input" ));
        assertTrue(TestTools.HasLink(graph,"plugins.tlecomte.histogram.HistogramEqualization" , "Equalized sequence", "plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet" ,"Image to save" ));
    }


    @Test
    public void TestParameters()
    {
        String input = "{\"parameters\": [[\"2\", \"DataSet Id\", \"601\" , \"out\"], [\"1\", \"Channel\", \"0\", \"out\"], [\"0\", \"ImageId\", \"351\", \"out\"]], \"blocks\": [{\"Inputs\": [\"ImageId\"], \"positionX\": 334, \"positionY\": 152, \"blockName\": \"OmeroImage\", \"Outputs\": [\"LoadedImage\"], \"blockId\": \"0\", \"blockType\": \"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\", \"elementId\": \"0\"}, {\"Inputs\": [\"Input\", \"Channel\"], \"positionX\": 380, \"positionY\": 453, \"blockName\": \"HistogramEqualization\", \"Outputs\": [\"Equalized sequence\"], \"blockId\": \"HistogramEqualization\", \"blockType\": \"plugins.tlecomte.histogram.HistogramEqualization\", \"elementId\": \"1\"}, {\"Inputs\": [\"Image to save\", \"DataSet Id\"], \"positionX\": 926, \"positionY\": 291, \"blockName\": \"OmeroImageSaveToDataSet\", \"Outputs\": [], \"blockId\": \"OmeroImageSaveToDataSet\", \"blockType\": \"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\", \"elementId\": \"2\"}], \"links\": [{\"sourcePort\": \"LoadedImage\", \"targetBlock\": \"1\", \"targetPort\": \"Input\", \"sourceBlock\": \"0\", \"anchors\": [[1, 0.5, 1, 0, 0, 0], [0, 0.3333333333333333, -1, 0, 0, 0]]}, {\"sourcePort\": \"Equalized sequence\", \"targetBlock\": \"2\", \"targetPort\": \"Image to save\", \"sourceBlock\": \"1\", \"anchors\": [[1, 0.5, 1, 0, 0, 0], [0, 0.3333333333333333, -1, 0, 0, 0]]}]}";

        List<ScriptInputParameters> results = ParameterReader.ParametersFromJsonString(input);

        assertNotNull(results);
        assertEquals(3,results.size());

        boolean [] found = new boolean[3];
        for (int i =0; i< 3;i++)
        {
            ScriptInputParameters param = results.get(i);
            switch (param.BlockId)
            {
                case "0":
                    assertEquals("ImageId", param.PortName);
                    assertEquals("351", param.Value);
                    found[0] = true;
                    break;
                case "1":
                    assertEquals("Channel", param.PortName);
                    assertEquals("0", param.Value);
                    found[1] = true;
                    break;
                case "2":
                    assertEquals("DataSet Id", param.PortName);
                    assertEquals("601", param.Value);
                    found[2] = true;
                    break;
            }
        }

        assertTrue(found[0] && found[1] && found[2]);
    }

    @Test
    public void TestDumpIntermediates()
    {
        String input = " {\"intermediates\": true, \"blocks\": [{\"Inputs\": [\"ImageId\"], \"positionX\": 476, \"positionY\": 256, \"blockName\": \"OmeroImage\", \"Outputs\": [\"LoadedImage\"], \"blockId\": \"0\", \"blockType\": \"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\", \"elementId\": \"0\"}, {\"Inputs\": [\"Sequence\", \"Channel\"], \"positionX\": 763, \"positionY\": 427, \"blockName\": \"ExtractChannel\", \"Outputs\": [\"Extracted\"], \"blockId\": \"ExtractChannel\", \"blockType\": \"plugins.tprovoost.sequenceblocks.extract.ExtractChannel\", \"elementId\": \"1\"}, {\"Inputs\": [\"Image to save\", \"DataSet Id\"], \"positionX\": 1052, \"positionY\": 630, \"blockName\": \"OmeroImageSaveToDataSet\", \"Outputs\": [], \"blockId\": \"OmeroImageSaveToDataSet\", \"blockType\": \"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\", \"elementId\": \"2\"}], \"name\": \"ExtractChannel\", \"parameters\": [[\"2\", \"Value\", \"0d3905b1-bbad-4dda-b796-c0a44736a195.tiff\", \"out\"], [\"1\", \"Channel\", \"0\", \"in\"], [\"0\", \"Value\", \"Convalaria.jpg\", \"out\"]], \"links\": [{\"sourcePort\": \"LoadedImage\", \"targetBlock\": \"1\", \"targetPort\": \"Sequence\", \"sourceBlock\": \"0\", \"anchors\": [[1, 0.5, 1, 0, 0, 0], [0, 0.3333333333333333, -1, 0, 0, 0]]}, {\"sourcePort\": \"Extracted\", \"targetBlock\": \"2\", \"targetPort\": \"Image to save\", \"sourceBlock\": \"1\", \"anchors\": [[1, 0.5, 1, 0, 0, 0], [0, 0.3333333333333333, -1, 0, 0, 0]]}], \"runId\": \"7812cbac-d2ce-491b-91db-d2148f31653a\"}\n";
        boolean dump = ParameterReader.DumpIntermediatesParameterFromJsonString(input);

        assertTrue(dump);

        input = " {\"intermediates\": false, \"blocks\": [{\"Inputs\": [\"ImageId\"], \"positionX\": 476, \"positionY\": 256, \"blockName\": \"OmeroImage\", \"Outputs\": [\"LoadedImage\"], \"blockId\": \"0\", \"blockType\": \"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\", \"elementId\": \"0\"}, {\"Inputs\": [\"Sequence\", \"Channel\"], \"positionX\": 763, \"positionY\": 427, \"blockName\": \"ExtractChannel\", \"Outputs\": [\"Extracted\"], \"blockId\": \"ExtractChannel\", \"blockType\": \"plugins.tprovoost.sequenceblocks.extract.ExtractChannel\", \"elementId\": \"1\"}, {\"Inputs\": [\"Image to save\", \"DataSet Id\"], \"positionX\": 1052, \"positionY\": 630, \"blockName\": \"OmeroImageSaveToDataSet\", \"Outputs\": [], \"blockId\": \"OmeroImageSaveToDataSet\", \"blockType\": \"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\", \"elementId\": \"2\"}], \"name\": \"ExtractChannel\", \"parameters\": [[\"2\", \"Value\", \"0d3905b1-bbad-4dda-b796-c0a44736a195.tiff\", \"out\"], [\"1\", \"Channel\", \"0\", \"in\"], [\"0\", \"Value\", \"Convalaria.jpg\", \"out\"]], \"links\": [{\"sourcePort\": \"LoadedImage\", \"targetBlock\": \"1\", \"targetPort\": \"Sequence\", \"sourceBlock\": \"0\", \"anchors\": [[1, 0.5, 1, 0, 0, 0], [0, 0.3333333333333333, -1, 0, 0, 0]]}, {\"sourcePort\": \"Extracted\", \"targetBlock\": \"2\", \"targetPort\": \"Image to save\", \"sourceBlock\": \"1\", \"anchors\": [[1, 0.5, 1, 0, 0, 0], [0, 0.3333333333333333, -1, 0, 0, 0]]}], \"runId\": \"7812cbac-d2ce-491b-91db-d2148f31653a\"}\n";
        dump = ParameterReader.DumpIntermediatesParameterFromJsonString(input);

        assertFalse(dump);

        input = " { \"blocks\": [{\"Inputs\": [\"ImageId\"], \"positionX\": 476, \"positionY\": 256, \"blockName\": \"OmeroImage\", \"Outputs\": [\"LoadedImage\"], \"blockId\": \"0\", \"blockType\": \"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\", \"elementId\": \"0\"}, {\"Inputs\": [\"Sequence\", \"Channel\"], \"positionX\": 763, \"positionY\": 427, \"blockName\": \"ExtractChannel\", \"Outputs\": [\"Extracted\"], \"blockId\": \"ExtractChannel\", \"blockType\": \"plugins.tprovoost.sequenceblocks.extract.ExtractChannel\", \"elementId\": \"1\"}, {\"Inputs\": [\"Image to save\", \"DataSet Id\"], \"positionX\": 1052, \"positionY\": 630, \"blockName\": \"OmeroImageSaveToDataSet\", \"Outputs\": [], \"blockId\": \"OmeroImageSaveToDataSet\", \"blockType\": \"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\", \"elementId\": \"2\"}], \"name\": \"ExtractChannel\", \"parameters\": [[\"2\", \"Value\", \"0d3905b1-bbad-4dda-b796-c0a44736a195.tiff\", \"out\"], [\"1\", \"Channel\", \"0\", \"in\"], [\"0\", \"Value\", \"Convalaria.jpg\", \"out\"]], \"links\": [{\"sourcePort\": \"LoadedImage\", \"targetBlock\": \"1\", \"targetPort\": \"Sequence\", \"sourceBlock\": \"0\", \"anchors\": [[1, 0.5, 1, 0, 0, 0], [0, 0.3333333333333333, -1, 0, 0, 0]]}, {\"sourcePort\": \"Extracted\", \"targetBlock\": \"2\", \"targetPort\": \"Image to save\", \"sourceBlock\": \"1\", \"anchors\": [[1, 0.5, 1, 0, 0, 0], [0, 0.3333333333333333, -1, 0, 0, 0]]}], \"runId\": \"7812cbac-d2ce-491b-91db-d2148f31653a\"}\n";
        dump = ParameterReader.DumpIntermediatesParameterFromJsonString(input);

        assertFalse(dump);
    }
}
