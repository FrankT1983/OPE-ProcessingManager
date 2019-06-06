package de.c3e.Tests;

/**
 * Unit test for testing the deserialization of icy protocols.
 */
import de.c3e.ProcessManager.DataTypes.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class GraphReaderTest
{
    @Test
    public void SingleBlock()
    {
        String inputString =
                "<protocol VERSION=\"4\">\n" +
                "<blocks>\n" +
                    "<block ID=\"115261383\" blockType=\"plugins.adufour.blocks.tools.Display\" className=\"plugins.adufour.blocks.tools.Display\" collapsed=\"false\" definedName=\"Display\" height=\"54\" keepsResults=\"true\" width=\"144\" xLocation=\"223\" yLocation=\"141\">\n" +
                    "<variables>\n" +
                        "<input>\n" +
                            "<variable ID=\"object\" name=\"object\" runtime=\"false\" typeName=\"java.lang.Object\" value=\"\" visible=\"true\"/>\n" +
                        "</input>\n" +
                        "<output>\n" +
                            "<variable ID=\"test\" name=\"testName\" runtime=\"false\" typeName=\"java.lang.Object\" value=\"\" visible=\"true\"/>\n" +
                        "</output>\n" +
                        "</variables>\n" +
                    "</block>\n" +
                "</blocks>\n" +
                "<links/>\n" +
                "</protocol>\n";
        BlockGraph result = GraphReader.ParseFromString(inputString);

        assertNotNull(result);
        assertNotNull(result.Links);
        assertNotNull(result.AllBlocks);
        assertEquals(1,result.AllBlocks.size());
        assertEquals(0,result.Links.size());

        GraphBlock display = result.AllBlocks.get(0);
        assertEquals("115261383", display.Id);
        assertEquals("plugins.adufour.blocks.tools.Display", display.Type);
        assertNotNull(display.Inputs);
        assertNotNull(display.Outputs);
        assertEquals(1,display.Inputs.size());
        assertEquals(1,display.Outputs.size());

        {
            BlockIO input = display.Inputs.get(0);
            assertNotNull(input);
            assertEquals("object", input.Name);
        }

        {
            BlockIO output = display.Outputs.get(0);
            assertNotNull(output);
            assertEquals("testName", output.Name);
            assertEquals("test", output.Id);
        }
    }

    @Test
    public void DualPortSingleBlock()
    {
        String inputString =
                "<protocol VERSION=\"4\">\n" +
                        "<blocks>\n" +
                        "<block ID=\"115261383\" blockType=\"plugins.adufour.blocks.tools.Display\" className=\"plugins.adufour.blocks.tools.Display\" collapsed=\"false\" definedName=\"Display\" height=\"54\" keepsResults=\"true\" width=\"144\" xLocation=\"223\" yLocation=\"141\">\n" +
                        "<variables>\n" +
                        "<input>\n" +
                        "<variable ID=\"object\" name=\"object\" runtime=\"false\" typeName=\"java.lang.Object\" value=\"\" visible=\"true\"/>\n" +
                        "<variable ID=\"object2\" name=\"object2\" runtime=\"false\" typeName=\"java.lang.Object\" value=\"\" visible=\"true\"/>\n" +
                        "</input>\n" +
                        "<output>\n" +
                        "<variable ID=\"test\" name=\"test\" runtime=\"false\" typeName=\"java.lang.Object\" value=\"\" visible=\"true\"/>\n" +
                        "<variable ID=\"test2\" name=\"test\" runtime=\"false\" typeName=\"java.lang.Object\" value=\"\" visible=\"true\"/>\n" +
                        "</output>\n" +
                        "</variables>\n" +
                        "</block>\n" +
                        "</blocks>\n" +
                        "<links/>\n" +
                        "</protocol>\n";
        BlockGraph result = GraphReader.ParseFromString(inputString);

        assertNotNull(result);
        assertNotNull(result.Links);
        assertNotNull(result.AllBlocks);
        assertEquals(1,result.AllBlocks.size());
        assertEquals(0,result.Links.size());

        GraphBlock display = result.AllBlocks.get(0);
        assertEquals("115261383", display.Id);
        assertEquals("plugins.adufour.blocks.tools.Display", display.Type);
        assertNotNull(display.Inputs);
        assertNotNull(display.Outputs);
        assertEquals(2,display.Inputs.size());
        assertEquals(2,display.Outputs.size());

        {
            BlockIO input = display.Inputs.get(0);
            assertNotNull(input);
            assertEquals("object", input.Name);
        }

        {
            BlockIO input = display.Inputs.get(1);
            assertNotNull(input);
            assertEquals("object2", input.Name);
        }

        {
            BlockIO output = display.Outputs.get(0);
            assertNotNull(output);
            assertEquals("test", output.Name);
        }

        {
            BlockIO output = display.Outputs.get(1);
            assertNotNull(output);
            assertEquals("test2", output.Id);
        }
    }


    @Test
    public void TwoBlocks()
    {
        String inputString =
                "<protocol VERSION=\"4\">\n" +
                "<blocks>\n" +
                        "<block ID=\"115261383\" blockType=\"plugins.adufour.blocks.tools.Display\" className=\"plugins.adufour.blocks.tools.Display\" collapsed=\"false\" definedName=\"Display\" height=\"54\" keepsResults=\"true\" width=\"144\" xLocation=\"102\" yLocation=\"74\">\n" +
                        "<variables>\n" +
                        "<input>\n" +
                        "<variable ID=\"object\" name=\"object\" runtime=\"false\" typeName=\"java.lang.Object\" value=\"\" visible=\"true\"/>\n" +
                        "</input>\n" +
                        "<output/>\n" +
                        "</variables>\n" +
                        "</block>\n" +
                        "<block ID=\"1805175715\" blockType=\"plugins.adufour.blocks.tools.Display\" className=\"plugins.adufour.blocks.tools.Display\" collapsed=\"false\" definedName=\"Display\" height=\"54\" keepsResults=\"true\" width=\"144\" xLocation=\"135\" yLocation=\"192\">\n" +
                        "<variables>\n" +
                        "<input>\n" +
                        "<variable ID=\"object\" name=\"object\" runtime=\"false\" typeName=\"java.lang.Object\" value=\"\" visible=\"true\"/>\n" +
                        "</input>\n" +
                        "<output/>\n" +
                        "</variables>\n" +
                        "</block>\n" +
                        "</blocks>\n" +
                        "<links/>\n" +
                        "</protocol>\n";

        BlockGraph result = GraphReader.ParseFromString(inputString);

        assertNotNull(result);
        assertNotNull(result.Links);
        assertNotNull(result.AllBlocks);
        assertEquals(2,result.AllBlocks.size());
        assertEquals(0,result.Links.size());

        boolean found1=false;
        boolean found2=false;
        for (GraphBlock block: result.AllBlocks)
        {
            assertEquals("plugins.adufour.blocks.tools.Display",block.Type);
            found1 |= block.Id.equals("115261383");
            found2 |= block.Id.equals("1805175715");
        }
        assertTrue(found1);
        assertTrue(found2);

    }

    @Test
    public void LinkBlocks()
    {
        String inputString =
                "<protocol VERSION=\"4\">\n" +
                        "<blocks>\n" +
                        "<block ID=\"115261383\" blockType=\"plugins.adufour.blocks.tools.Display\" className=\"plugins.adufour.blocks.tools.Display\" collapsed=\"false\" definedName=\"Display\" height=\"54\" keepsResults=\"true\" width=\"144\" xLocation=\"102\" yLocation=\"74\">\n" +
                        "<variables>\n" +
                        "<input>\n" +
                        "<variable ID=\"object\" name=\"object\" runtime=\"false\" typeName=\"java.lang.Object\" value=\"\" visible=\"true\"/>\n" +
                        "</input>\n" +
                        "<output>\n" +
                        "<variable ID=\"test\" name=\"object\" runtime=\"false\" typeName=\"java.lang.Object\" value=\"\" visible=\"true\"/>\n" +
                        "</output>\n" +
                        "</variables>\n" +
                        "</block>\n" +
                        "<block ID=\"1805175715\" blockType=\"plugins.adufour.blocks.tools.Display\" className=\"plugins.adufour.blocks.tools.Display\" collapsed=\"false\" definedName=\"Display\" height=\"59\" keepsResults=\"true\" width=\"144\" xLocation=\"135\" yLocation=\"192\">\n" +
                        "<variables>\n" +
                        "<input>\n" +
                        "<variable ID=\"object\" name=\"object\" runtime=\"false\" typeName=\"java.lang.Object\" visible=\"true\"/>\n" +
                        "</input>\n" +
                        "<output/>\n" +
                        "</variables>\n" +
                        "</block>\n" +
                        "</blocks>\n" +
                        "<links>\n" +
                        "<link dstBlockID=\"1805175715\" dstVarID=\"object\" srcBlockID=\"115261383\" srcVarID=\"test\" srcVarType=\"java.lang.Object\"/>\n" +
                        "</links>\n" +
                        "</protocol>\n";


        BlockGraph result = GraphReader.ParseFromString(inputString);

        assertNotNull(result);
        assertNotNull(result.Links);
        assertNotNull(result.AllBlocks);
        assertEquals(2,result.AllBlocks.size());
        assertEquals(1,result.Links.size());

        BlockLink link = result.Links.get(0);
        assertEquals("115261383",link.OriginBlock.Id);
        assertEquals("1805175715",link.DestinationBlock.Id);
        assertEquals("test",link.OriginPort.Id);
        assertEquals("object",link.DestinationPort.Name);
    }

    @Test
    public void UseCaseTest1()
    {
        String inputString =
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
                        "    </blocks>\n" +
                        "    <links>\n" +
                        "        <link dstBlockID=\"2087420046\" dstVarID=\"Input\" srcBlockID=\"1339674000\" srcVarID=\"LoadedImage\" />\n" +
                        "        <link dstBlockID=\"740544130\" dstVarID=\"Image to save\" srcBlockID=\"2087420046\" srcVarID=\"Equalized sequence\" />\n" +
                        "        <link dstBlockID=\"2087420046\" dstVarID=\"Channel\" srcBlockID=\"1647119090\" srcVarID=\"Value\" />\n" +
                        "        <link dstBlockID=\"740544130\" dstVarID=\"DataSet Id\" srcBlockID=\"1409786826\" srcVarID=\"Value\" />\n" +
                        "    </links>\n" +
                        "</protocol>";

        BlockGraph result = GraphReader.ParseFromString(inputString);

        assertNotNull(result);
        assertNotNull(result.Links);
        assertNotNull(result.AllBlocks);
        assertEquals(5,result.AllBlocks.size());

        assertTrue(TestTools.HasId(result.AllBlocks, "1339674000"));
        assertTrue(TestTools.HasId(result.AllBlocks, "1647119090"));
        assertTrue(TestTools.HasId(result.AllBlocks, "2087420046"));
        assertTrue(TestTools.HasId(result.AllBlocks, "1409786826"));
        assertTrue(TestTools.HasId(result.AllBlocks, "740544130"));


        assertEquals(4,result.Links.size());
    }
}
