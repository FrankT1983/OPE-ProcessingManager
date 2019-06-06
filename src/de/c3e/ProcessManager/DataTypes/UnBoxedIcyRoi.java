package de.c3e.ProcessManager.DataTypes;

import de.c3e.ProcessManager.Utils.XmlDocumentHelper;
import icy.roi.ROI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to store a not serializable Roi array in its (serializable) xml form
 * to be passed around between workers.
 */
public class UnBoxedIcyRoi implements Serializable
{
    private static final String ClassAttributeKey = "Class";
    private static final String RootKey = "root";
    private static final String StorageKey = "Storage";
    private final String xmlString ;

    public UnBoxedIcyRoi(ROI[] rois)
    {
        String tmp="";
        try
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element root = doc.createElement(RootKey);
            doc.appendChild(root);
            for (ROI roi : rois)
            {
                Element n = doc.createElement(StorageKey);
                n.setAttribute(ClassAttributeKey, roi.getClassName());
                roi.saveToXML(n);
                root.appendChild(n);
            }

            tmp = XmlDocumentHelper.XmlDocToString(doc);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        this.xmlString = tmp;
    }

    public ROI[] ReBox()
    {
        Document doc = XmlDocumentHelper.XmlDocumentFromString(this.xmlString);
        List<ROI> result = new ArrayList<>();
        try
        {
            if (doc != null)
            {
                NodeList childes = doc.getElementsByTagName(StorageKey);
                for (int i = 0; i < childes.getLength(); i++)
                {
                    Node c = childes.item(i);
                    if (!(c instanceof Element))
                    {
                        continue;
                    }

                    Element e = (Element) c;
                    String objectType = e.getAttribute(ClassAttributeKey);
                    try
                    {
                        Class cls = Class.forName(objectType);
                        ROI roi = (ROI) cls.newInstance();
                        roi.loadFromXML(e);
                        result.add(roi);
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }

                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        ROI[] res = new ROI[result.size()];
        res = result.toArray(res);
        return res;
    }
}
