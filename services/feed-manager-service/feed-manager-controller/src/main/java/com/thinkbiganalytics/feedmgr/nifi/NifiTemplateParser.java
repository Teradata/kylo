package com.thinkbiganalytics.feedmgr.nifi;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by sr186054 on 5/7/16.
 */
public class NifiTemplateParser {



    public static String getTemplateName(String nifiTemplate) throws ParserConfigurationException, XPathExpressionException, IOException, SAXException {

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();

        InputSource source = new InputSource(new StringReader(nifiTemplate));
        String name = (String) xpath.evaluate("/template/name", source,XPathConstants.STRING);
        return name;
    }
}
