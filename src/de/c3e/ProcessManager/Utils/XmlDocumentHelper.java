package de.c3e.ProcessManager.Utils;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

/**
 * Created by Frank on 01.11.2016.
 */
public class XmlDocumentHelper
{

    public static String XmlDocToString(Document doc)
    {
        try
        {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }

    public static Document XmlDocumentFromString(String inputXmlString)
    {
        try
        {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            ByteArrayInputStream input =  new ByteArrayInputStream(inputXmlString.getBytes("UTF-8"));
            return builder.parse(input);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
