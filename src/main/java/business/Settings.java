package business;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;

public class Settings
{
    public static String SETTINGS_FILE_PATH = "C:\\Automation\\Settings\\appsettings.xml";

    public static String read(String xPathString)
    {
        String value = "";
        try
        {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;

            builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(new FileInputStream(SETTINGS_FILE_PATH));

            XPath xPath = XPathFactory.newInstance().newXPath();

            //read an xml node using xpath
            value = xPath.compile(xPathString).evaluate(document);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return value;
    }

    public static void write(String xPathString, String newValue)
    {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try
        {
            builder = builderFactory.newDocumentBuilder();
            Document document = builder.parse(new FileInputStream(SETTINGS_FILE_PATH));

            XPath xPath = XPathFactory.newInstance().newXPath();

            //read an xml node using xpath
            NodeList nodeList = (NodeList) xPath.compile(xPathString).evaluate(document, XPathConstants.NODESET);
            nodeList.item(0).setTextContent(newValue);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(SETTINGS_FILE_PATH));
            transformer.transform(source, result);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
