package de.c3e.ProcessManager.DataTypes;

import de.c3e.ProcessManager.Utils.DebugHelper;
import de.c3e.ProcessManager.Utils.LogUtilities;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.lang.invoke.MethodHandles;
import java.util.*;

import org.json.*;


/**
 * Reader class to load a working graph form a file
 */
public class GraphReader
{
    private static Logger logger = LogUtilities.ConstructLogger(MethodHandles.lookup().lookupClass().getSimpleName());

    public static BlockGraph ParseFromString(String descritption)
    {
        BlockGraph result = new BlockGraph();

        Document doc = XmlDocumentFromString(descritption);
        {
            NodeList blocks = doc.getElementsByTagName("block");
            for (int b = 0; b < blocks.getLength(); b++)
            {
                Node e = blocks.item(b);
                if (e.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }
                result.AllBlocks.add(BlockFromElement((Element) e));
            }
        }

        {
            NodeList links = doc.getElementsByTagName("link");
            for (int l = 0; l < links.getLength(); l++)
            {
                Node e = links.item(l);
                if (e.getNodeType() != Node.ELEMENT_NODE)
                {   continue;   }

                BlockLink link = LinkFromElement((Element)e, result.AllBlocks);
                if (link != null)
                {
                    result.Links.add(link);
                }
                else
                {
                    DebugHelper.BreakIntoDebug();
                }
            }
        }
        return result;
    }

    private static BlockLink LinkFromElement(Element e,List<GraphBlock> blocks)
    {
        String originBlockId =  e.getAttribute("srcBlockID");
        String originPortId =  e.getAttribute("srcVarID");
        String destinationBlockId =  e.getAttribute("dstBlockID");
        String destinationPortId =  e.getAttribute("dstVarID");
        String type =  e.getAttribute("srcVarType");

        GraphBlock originBlock = null;
        BlockIO originPort = null;

        GraphBlock destinationBlock = null;
        BlockIO destinationPort = null;

        for (GraphBlock block:blocks)
        {
            if (destinationBlockId.equals(block.Id))
            {
                for (BlockIO input : block.Inputs)
                {
                    if (input.NameOrIdEquals(destinationPortId))
                    {
                        destinationBlock = block;
                        destinationPort = input;
                        break;
                    }
                }
            }

            if (originBlockId.equals(block.Id))
            {
                for (BlockIO output : block.Outputs)
                {
                    if (output.NameOrIdEquals(originPortId))
                    {
                        originBlock = block;
                        originPort = output;
                        break;
                    }
                }

            }
        }

        if (originBlock == null)
        {
            logger.error("Could not find source Block " + originBlockId);
        }

        if (destinationBlock == null)
        {
            logger.error("Could not find source Block " + destinationBlockId);
        }

        if ((originBlock == null) || (destinationBlock==null))
        {return null;}

        return new BlockLink(originPort,originBlock,destinationPort,destinationBlock);
    }

    private static GraphBlock BlockFromElement(Element e)
    {
        GraphBlock block = new GraphBlock();
        block.Id = e.getAttribute("ID");
        block.Type = e.getAttribute("blockType");
        block.Inputs.addAll(GetPortsFromNodes(e, "input"));
        block.Outputs.addAll(GetPortsFromNodes(e, "output"));

        return block;
    }

    private static List<BlockIO> GetPortsFromNodes(Element e, String portType)
    {
        List<BlockIO> ports = new ArrayList<>();
        NodeList inputs = e.getElementsByTagName(portType);
        for (int i = 0; i < inputs.getLength(); i++)
        {
            Node input = inputs.item(i);
            if (input.getNodeType() != Node.ELEMENT_NODE)
            {   continue;   }

            NodeList variables = ((Element)input).getElementsByTagName("variable");
            for (int v = 0; v < variables.getLength(); v++)
            {
                Node var = variables.item(v);
                if (e.getNodeType() != Node.ELEMENT_NODE)
                {
                    continue;
                }

                Element variable = (Element) var;

                BlockIO port = new BlockIO();
                port.Id = variable.getAttribute("ID");
                port.Name = variable.getAttribute("name");

                String value = variable.getAttribute("value");
                if(StringUtils.isNotBlank(value))
                {
                    port.SetValue(value);
                }

                ports.add(port);
            }
        }
        return ports;
    }


    private static Document XmlDocumentFromString(String inputXmlString)
    {
        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            ByteArrayInputStream input =  new ByteArrayInputStream(inputXmlString.getBytes("UTF-8"));
            return builder.parse(input);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static BlockGraph GraphFromJson(String jsonString)
    {
        logger.debug("Json: \n " + jsonString);

        BlockGraph result = new BlockGraph();

        JSONObject obj;
        try{
            obj = new JSONObject(jsonString);
        }
        catch (Exception e)
        {
            logger.error("Could not parse graph from json: \n" + jsonString + " \n" + e.toString());
            throw e;
        }

        JSONArray array = obj.getJSONArray("blocks");
        Map<String,GraphBlock> blockMap = new HashMap<>();
        if (array != null)
        {
            for(int b=0;b<array.length();b++)
            {
                JSONObject blockObject =array.getJSONObject(b);

                String elementId = blockObject.getString("elementId");
                String blockType =blockObject.getString("blockType");



                JSONArray inputArray ;
                try
                {
                    inputArray = blockObject.getJSONArray("Inputs");
                }
                catch (Exception e)
                {
                    logger.error("could not find block \"Inputs\": " +blockType + " id: "  + elementId );
                    throw e;
                }

                JSONArray outputArray;
                try
                {
                    outputArray = blockObject.getJSONArray("Outputs");
                }
                catch (Exception e)
                {
                    logger.error("could not find block \"Outputs\": " +blockType + " id: "  + elementId );
                    throw e;
                }


                GraphBlock block = new GraphBlock();
                block.Id = elementId;
                block.Type = blockType;
                block.Inputs.addAll(GetPortsFromJsonArray(inputArray));
                block.Outputs.addAll(GetPortsFromJsonArray(outputArray));

                if (blockObject.has("inputList"))
                {
                    JSONArray formInputArray = blockObject.getJSONArray("inputList");
                    block.Inputs.addAll(GetPortsFromFormListJsonArray(formInputArray));
                }

                blockMap.put(elementId,block);

                result.AllBlocks.add(block);
            }
        }

        array = obj.getJSONArray("links");
        if (array != null)
        {
            for (int l = 0; l < array.length(); l++)
            {
                JSONObject linkObject =array.getJSONObject(l);

                String sourceId = linkObject.getString("sourceBlock");
                String sourcePort = linkObject.getString("sourcePort");
                String targetId = linkObject.getString("targetBlock");
                String targetPort = linkObject.getString("targetPort");

                GraphBlock sourceBlock = blockMap.get(sourceId);
                BlockIO sPort = null;
                for (BlockIO io :sourceBlock.Outputs)
                {
                    if (io.Id.equals(sourcePort) || io.Name.equals(sourcePort))
                    {
                        sPort = io;
                        break;
                    }
                }

                GraphBlock targetBlock = blockMap.get(targetId);
                BlockIO desPort = null;
                for (BlockIO io :targetBlock.Inputs)
                {
                    if (io.Id.equals(targetPort) || io.Name.equals(targetPort))
                    {
                        desPort = io;
                        break;
                    }
                }

                BlockLink link = new BlockLink(sPort,sourceBlock,desPort,targetBlock);
                result.Links.add(link);
            }
        }

        return result;
    }

    private static List<BlockIO> GetPortsFromJsonArray(JSONArray array)
    {
        List<BlockIO> result = new ArrayList<>();

        for(int e=0;e<array.length();e++)
        {
            String entry = array.getString(e);

            BlockIO io = new BlockIO();
            io.Name = entry;
            io.Id = entry;

            result.add(io);
        }

        return result;
    }

    private static List<BlockIO> GetPortsFromFormListJsonArray(JSONArray array)
    {
        List<BlockIO> result = new ArrayList<>();

        for(int e=0;e<array.length();e++)
        {
            JSONObject entry = array.getJSONObject(e);

            BlockIO io = new BlockIO();
            io.Name = entry.getString("id");
            io.Id = entry.getString("id");
            io.SetValue(entry.getString("value"));

            result.add(io);
        }

        return result;
    }
}
