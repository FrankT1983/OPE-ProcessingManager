package de.c3e.Tests;

import de.c3e.ProcessManager.BlockRepository.LoadImageWorkBlock;
import de.c3e.ProcessManager.BlockRepository.OutputWorkBlock;
import de.c3e.ProcessManager.BlockRepository.StringParameterWorkBlock;
import de.c3e.ProcessManager.DataTypes.BlockGraph;
import de.c3e.ProcessManager.DataTypes.BlockLink;
import de.c3e.ProcessManager.DataTypes.GraphReader;
import de.c3e.ProcessManager.DataTypes.GraphTransformations;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for testing the addoption of plain icy protocolls to the needs of the processing manager.
 */
public class GraphModifierTest
{
    @Test
    public void TestOmeroInputBlockTransformation()
    {
        String originGraph = "<protocol VERSION=\"4\">\n" +
                "<blocks>\n" +
                "<block ID=\"746669491\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" collapsed=\"false\" definedName=\"Omero image input\" height=\"86\" keepsResults=\"true\" width=\"211\" xLocation=\"27\" yLocation=\"147\">\n" +
                "<variables>\n" +
                "<input>\n" +
                "<variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"Parameter 1\" visible=\"true\"/>\n" +
                "</input>\n" +
                "<output>\n" +
                "<variable ID=\"LoadedImage\" name=\"Loaded image\" runtime=\"false\" visible=\"true\"/>\n" +
                "</output>\n" +
                "</variables>\n" +
                "</block>\n" +
                "<block ID=\"115261383\" blockType=\"plugins.adufour.blocks.tools.Display\" className=\"plugins.adufour.blocks.tools.Display\" collapsed=\"false\" definedName=\"Display\" height=\"84\" keepsResults=\"true\" width=\"151\" xLocation=\"302\" yLocation=\"160\">\n" +
                "<variables>\n" +
                "<input>\n" +
                "<variable ID=\"object\" name=\"object\" runtime=\"false\" typeName=\"icy.sequence.Sequence\" visible=\"true\"/>\n" +
                "</input>\n" +
                "<output/>\n" +
                "</variables>\n" +
                "</block>\n" +
                "</blocks>\n" +
                "<links>\n" +
                "<link dstBlockID=\"115261383\" dstVarID=\"object\" srcBlockID=\"746669491\" srcVarID=\"LoadedImage\"/>\n" +
                "</links>\n" +
                "</protocol>";

        BlockGraph origin = GraphReader.ParseFromString(originGraph);

        assertEquals(2, origin.AllBlocks.size());
        assertEquals(1, origin.Links.size());

        GraphTransformations.ModifyGraphForUseInManager(origin);

        assertEquals(3, origin.AllBlocks.size());
        assertEquals(2, origin.Links.size());

        assertTrue(TestTools.HasType(origin.AllBlocks, StringParameterWorkBlock.typeName));
        assertTrue(TestTools.HasType(origin.AllBlocks, LoadImageWorkBlock.typeName));
        assertTrue(TestTools.HasType(origin.AllBlocks, "plugins.adufour.blocks.tools.Display"));

        for (BlockLink link:origin.Links)
        {
            switch (link.OriginBlock.Type)
            {
                case StringParameterWorkBlock.typeName:
                    assertEquals(link.DestinationBlock.Type, LoadImageWorkBlock.typeName);
                    break;
                case LoadImageWorkBlock.typeName:
                    assertEquals(link.DestinationBlock.Type, "plugins.adufour.blocks.tools.Display");
                    break;
                default:
                    assertTrue(false);
                    break;
            }
        }
    }

    @Test
    public void TestOutputBlockTransformation()
    {
        String protocol =
                "<protocol VERSION=\"4\">\n" +
                        "    <blocks>\n" +
                        "        <block ID=\"812133990\" blockType=\"plugins.tlecomte.histogram.HistogramEqualization\" className=\"plugins.tlecomte.histogram.HistogramEqualization\" collapsed=\"false\" definedName=\"Histogram Equalization\" height=\"132\" keepsResults=\"true\" width=\"236\" xLocation=\"307\" yLocation=\"243\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"Input\" name=\"Input\" runtime=\"false\" value=\"Active Sequence\" visible=\"true\" />\n" +
                        "                    <variable ID=\"Channel\" name=\"Channel\" runtime=\"false\" value=\"0\" visible=\"true\" />\n" +
                        "                    <variable ID=\"In-place\" name=\"In-place\" runtime=\"false\" value=\"false\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output>\n" +
                        "                    <variable ID=\"Equalized sequence\" name=\"Equalized sequence\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </output>\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "        <block ID=\"1370359593\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" collapsed=\"false\" definedName=\"Omero script parameter long\" height=\"111\" keepsResults=\"true\" width=\"293\" xLocation=\"250\" yLocation=\"374\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"Value\" name=\"TestValue(ignored in Omero)\" runtime=\"false\" value=\"0\" visible=\"true\" />\n" +
                        "                    <variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"Parameter 4\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output>\n" +
                        "                    <variable ID=\"Value\" name=\"Value\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </output>\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "        <block ID=\"1443738339\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" collapsed=\"false\" definedName=\"Omero image save to data set\" height=\"105\" keepsResults=\"true\" width=\"268\" xLocation=\"775\" yLocation=\"340\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"Image to save\" name=\"Image to save\" runtime=\"false\" typeName=\"icy.sequence.Sequence\" visible=\"true\" />\n" +
                        "                    <variable ID=\"DataSet Id\" name=\"DataSet Id\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output />\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "    </blocks>\n" +
                        "    <links>\n" +
                        "        <link dstBlockID=\"1443738339\" dstVarID=\"Image to save\" srcBlockID=\"812133990\" srcVarID=\"Equalized sequence\" />\n" +
                        "        <link dstBlockID=\"1443738339\" dstVarID=\"DataSet Id\" srcBlockID=\"1370359593\" srcVarID=\"Value\" />\n" +
                        "    </links>\n" +
                        "</protocol>";

        BlockGraph graph = GraphReader.ParseFromString(protocol);

        assertEquals(3, graph.AllBlocks.size());
        assertEquals(2, graph.Links.size());


        assertTrue(TestTools.HasType(graph.AllBlocks, "plugins.tlecomte.histogram.HistogramEqualization"));
        assertTrue(TestTools.HasType(graph.AllBlocks, "plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet"));
        assertTrue(TestTools.HasType(graph.AllBlocks, "plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong"));

        GraphTransformations.ModifyGraphForUseInManager(graph);

        assertEquals(4, graph.AllBlocks.size());
        assertEquals(2, graph.Links.size());

        assertTrue(TestTools.HasType(graph.AllBlocks,"plugins.tlecomte.histogram.HistogramEqualization"));
        assertTrue(TestTools.HasType(graph.AllBlocks,OutputWorkBlock.typeName));
        assertTrue(TestTools.HasType(graph.AllBlocks,"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong"));
        assertTrue(TestTools.HasType(graph.AllBlocks,StringParameterWorkBlock.typeName));
    }

    @Test
    public void DirectlyToSaveTest()
    {
        String protocol =
                "<protocol VERSION=\"4\">\n" +
                        "    <blocks>\n" +
                        "        <block ID=\"639127609\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" collapsed=\"false\" definedName=\"Omero image input\" height=\"86\" keepsResults=\"true\" width=\"211\" xLocation=\"217\" yLocation=\"346\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"Image\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output>\n" +
                        "                    <variable ID=\"LoadedImage\" name=\"Loaded image\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </output>\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "        <block ID=\"162479485\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" collapsed=\"false\" definedName=\"Omero script parameter long\" height=\"111\" keepsResults=\"true\" width=\"293\" xLocation=\"250\" yLocation=\"553\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"Value\" name=\"TestValue(ignored in Omero)\" runtime=\"false\" value=\"0\" visible=\"true\" />\n" +
                        "                    <variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"DataSetId\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output>\n" +
                        "                    <variable ID=\"Value\" name=\"Value\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </output>\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "        <block ID=\"1443738339\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" collapsed=\"false\" definedName=\"Omero image save to data set\" height=\"105\" keepsResults=\"true\" width=\"268\" xLocation=\"801\" yLocation=\"382\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"Image to save\" name=\"Image to save\" runtime=\"false\" typeName=\"icy.sequence.Sequence\" visible=\"true\" />\n" +
                        "                    <variable ID=\"DataSet Id\" name=\"DataSet Id\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output />\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "    </blocks>\n" +
                        "    <links>\n" +
                        "        <link dstBlockID=\"1443738339\" dstVarID=\"Image to save\" srcBlockID=\"639127609\" srcVarID=\"LoadedImage\" />\n" +
                        "        <link dstBlockID=\"1443738339\" dstVarID=\"DataSet Id\" srcBlockID=\"162479485\" srcVarID=\"Value\" />\n" +
                        "    </links>\n" +
                        "</protocol>";

        BlockGraph graph = GraphReader.ParseFromString(protocol);

        assertEquals(3, graph.AllBlocks.size());
        assertEquals(2, graph.Links.size());

        assertTrue(TestTools.HasType(graph.AllBlocks, "plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong"));
        assertTrue(TestTools.HasType(graph.AllBlocks, "plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet"));
        assertTrue(TestTools.HasType(graph.AllBlocks, "plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock"));

        GraphTransformations.ModifyGraphForUseInManager(graph);

        assertEquals(5, graph.AllBlocks.size());
        assertEquals(3, graph.Links.size());

        assertEquals(2,TestTools.Count(graph.AllBlocks, StringParameterWorkBlock.typeName));
        assertTrue(TestTools.HasType(graph.AllBlocks, LoadImageWorkBlock.typeName));
        assertTrue(TestTools.HasType(graph.AllBlocks, "plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong"));
        assertTrue(TestTools.HasType(graph.AllBlocks,OutputWorkBlock.typeName));

        assertTrue(TestTools.HasLink(graph, StringParameterWorkBlock.typeName, StringParameterWorkBlock.outputName
                                            , LoadImageWorkBlock.typeName ,LoadImageWorkBlock.inputName));

        assertTrue(TestTools.HasLink(graph, StringParameterWorkBlock.typeName, StringParameterWorkBlock.outputName
                , OutputWorkBlock.typeName,OutputWorkBlock.PathInput));

        assertTrue(TestTools.HasLink(graph, LoadImageWorkBlock.typeName, LoadImageWorkBlock.outputName
                , OutputWorkBlock.typeName,OutputWorkBlock.ImageInput));
    }

    @Test
    public void UseCaseTEst()
    {
        String protocol =
                "<protocol VERSION=\"4\">\n" +
                        "    <blocks>\n" +
                        "        <block ID=\"1647119090\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" collapsed=\"false\" definedName=\"Omero script parameter long\" height=\"114\" keepsResults=\"true\" width=\"282\" xLocation=\"6\" yLocation=\"117\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"Value\" name=\"TestValue(ignored in Omero)\" runtime=\"false\" value=\"0\" visible=\"true\" />\n" +
                        "                    <variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"Channel Input\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output>\n" +
                        "                    <variable ID=\"Value\" name=\"Value\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </output>\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "        <block ID=\"1409786826\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroScriptParameterLong\" collapsed=\"false\" definedName=\"Omero script parameter long\" height=\"137\" keepsResults=\"true\" width=\"318\" xLocation=\"14\" yLocation=\"254\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"Value\" name=\"TestValue(ignored in Omero)\" runtime=\"false\" value=\"0\" visible=\"true\" />\n" +
                        "                    <variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"DataSetId\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output>\n" +
                        "                    <variable ID=\"Value\" name=\"Value\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </output>\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "        <block ID=\"1339674000\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" collapsed=\"false\" definedName=\"Omero image input\" height=\"90\" keepsResults=\"true\" width=\"234\" xLocation=\"52\" yLocation=\"22\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"InputImage\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output>\n" +
                        "                    <variable ID=\"LoadedImage\" name=\"Loaded image\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </output>\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "        <block ID=\"2087420046\" blockType=\"plugins.tlecomte.histogram.HistogramEqualization\" className=\"plugins.tlecomte.histogram.HistogramEqualization\" collapsed=\"false\" definedName=\"Histogram Equalization\" height=\"132\" keepsResults=\"true\" width=\"236\" xLocation=\"432\" yLocation=\"89\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"Input\" name=\"Input\" runtime=\"false\" visible=\"true\" />\n" +
                        "                    <variable ID=\"Channel\" name=\"Channel\" runtime=\"false\" visible=\"true\" />\n" +
                        "                    <variable ID=\"In-place\" name=\"In-place\" runtime=\"false\" value=\"false\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output>\n" +
                        "                    <variable ID=\"Equalized sequence\" name=\"Equalized sequence\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </output>\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "        <block ID=\"740544130\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" collapsed=\"false\" definedName=\"Omero image save to data set\" height=\"230\" keepsResults=\"true\" width=\"268\" xLocation=\"756\" yLocation=\"88\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"Image to save\" name=\"Image to save\" runtime=\"false\" typeName=\"icy.sequence.Sequence\" visible=\"true\" />\n" +
                        "                    <variable ID=\"DataSet Id\" name=\"DataSet Id\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output />\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "        <block ID=\"900725942\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageInputBlock\" collapsed=\"false\" definedName=\"Omero image input\" height=\"86\" keepsResults=\"true\" width=\"211\" xLocation=\"5\" yLocation=\"461\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"ParameterName\" name=\"ParameterName\" runtime=\"false\" value=\"Image2\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output>\n" +
                        "                    <variable ID=\"LoadedImage\" name=\"Loaded image\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </output>\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "        <block ID=\"454647905\" blockType=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" className=\"plugins.Frank.de.c3e.ProcessManager.OmeroImageSaveToDataSet\" collapsed=\"false\" definedName=\"Omero image save to data set\" height=\"86\" keepsResults=\"true\" width=\"268\" xLocation=\"837\" yLocation=\"536\">\n" +
                        "            <variables>\n" +
                        "                <input>\n" +
                        "                    <variable ID=\"Image to save\" name=\"Image to save\" runtime=\"false\" typeName=\"java.lang.String\" visible=\"true\" />\n" +
                        "                    <variable ID=\"DataSet Id\" name=\"DataSet Id\" runtime=\"false\" visible=\"true\" />\n" +
                        "                </input>\n" +
                        "                <output />\n" +
                        "            </variables>\n" +
                        "        </block>\n" +
                        "    </blocks>\n" +
                        "    <links>\n" +
                        "        <link dstBlockID=\"2087420046\" dstVarID=\"Channel\" srcBlockID=\"1647119090\" srcVarID=\"Value\" />\n" +
                        "        <link dstBlockID=\"740544130\" dstVarID=\"DataSet Id\" srcBlockID=\"1409786826\" srcVarID=\"Value\" />\n" +
                        "        <link dstBlockID=\"2087420046\" dstVarID=\"Input\" srcBlockID=\"1339674000\" srcVarID=\"LoadedImage\" />\n" +
                        "        <link dstBlockID=\"740544130\" dstVarID=\"Image to save\" srcBlockID=\"2087420046\" srcVarID=\"Equalized sequence\" />\n" +
                        "        <link dstBlockID=\"454647905\" dstVarID=\"DataSet Id\" srcBlockID=\"1409786826\" srcVarID=\"Value\" />\n" +
                        "        <link dstBlockID=\"454647905\" dstVarID=\"Image to save\" srcBlockID=\"900725942\" srcVarID=\"LoadedImage\" />\n" +
                        "    </links>\n" +
                        "</protocol>";


        BlockGraph graph = GraphReader.ParseFromString(protocol);

        assertEquals(7, graph.AllBlocks.size());
        assertEquals(6, graph.Links.size());

    }
}
