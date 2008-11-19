package com.goofans.gootool.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class XMLUtil
{
  public static Document loadDocumentFromFile(File file) throws IOException
  {
    DocumentBuilder builder;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      throw new IOException("Unable to create an XML document builder: " + e.getLocalizedMessage(), e);
    }

    Document document;
    try {
      document = builder.parse(file);
    }
    catch (SAXException e) {
      throw new IOException("Unable to load " + file.getName(), e);
    }
    return document;
  }

  // Can't use this, at least one fie has weird encoding
//  public static Document loadDocumentFromInputStream(InputStream is) throws IOException
//  {
//    DocumentBuilder builder;
//    try {
//      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//    }
//    catch (ParserConfigurationException e) {
//      throw new IOException("Unable to create an XML document builder: " + e.getLocalizedMessage(), e);
//    }
//
//    Document document;
//    try {
//      document = builder.parse(is);
//    }
//    catch (SAXException e) {
//      throw new IOException("Unable to load properties/config.txt", e);
//    }
//    return document;
//  }

  public static Document loadDocumentFromReader(Reader r) throws IOException
  {
    DocumentBuilder builder;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      throw new IOException("Unable to create an XML document builder: " + e.getLocalizedMessage(), e);
    }

    Document document;
    try {
      document = builder.parse(new InputSource(r));
    }
    catch (SAXException e) {
      throw new IOException("Unable to load document", e);
    }
    return document;
  }

  public static void writeDocumentToFile(Document d, File file) throws TransformerException
  {
    // Prepare the DOM document for writing
    Source source = new DOMSource(d);

    // Prepare the output file
    Result result = new StreamResult(file);

    // Write the DOM document to the file
    Transformer xformer = TransformerFactory.newInstance().newTransformer();
    xformer.transform(source, result);
  }

  public static String writeDocumentToString(Document d) throws TransformerException
  {
    // Prepare the DOM document for writing
    Source source = new DOMSource(d);

    // Prepare the output file
    StringWriter writer = new StringWriter();
    Result result = new StreamResult(writer);

    // Write the DOM document to the string
    Transformer xformer = TransformerFactory.newInstance().newTransformer();
    xformer.transform(source, result);

    return writer.toString();
  }

  public static Document newDocument() throws ParserConfigurationException
  {
    return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
  }

  public static String getAttributeString(Node node, String attributeName, String defaultValue)
  {
    NamedNodeMap attributes = node.getAttributes();
    if (attributes == null) return defaultValue;

    Node attribute = attributes.getNamedItem(attributeName);
    if (attribute == null) return defaultValue;

    return attribute.getNodeValue().trim();
  }

  public static String getAttributeStringRequired(Node node, String attributeName) throws IOException
  {
    String s = getAttributeString(node, attributeName, null);
    if (s == null) throw new IOException("Mandatory attribute " + attributeName + " not specified on " + node.getNodeName());
    return s;
  }

  public static Double getAttributeDouble(Node node, String attributeName, Double defaultValue)
  {
    NamedNodeMap attributes = node.getAttributes();
    if (attributes == null) return defaultValue;

    Node attribute = attributes.getNamedItem(attributeName);
    if (attribute == null) return defaultValue;

    try {
      Double d = Double.valueOf(attribute.getNodeValue().trim());
      if (d.isNaN() || d.isInfinite()) return defaultValue;
      return d;
    }
    catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static double getAttributeDoubleRequired(Node node, String attributeName) throws IOException
  {
    Double d = getAttributeDouble(node, attributeName, null);
    if (d == null) throw new IOException("Mandatory attribute " + attributeName + " not specified on " + node.getNodeName());
    return d;
  }

  public static Integer getAttributeInteger(Node node, String attributeName, Integer defaultValue)
  {
    NamedNodeMap attributes = node.getAttributes();
    if (attributes == null) return defaultValue;

    Node attribute = attributes.getNamedItem(attributeName);
    if (attribute == null) return defaultValue;

    try {
      return Integer.valueOf(attribute.getNodeValue().trim());
    }
    catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static int getAttributeIntegerRequired(Node node, String attributeName) throws IOException
  {
    Integer integer = getAttributeInteger(node, attributeName, null);
    if (integer == null) throw new IOException("Mandatory attribute " + attributeName + " not specified on " + node.getNodeName());
    return integer;
  }

  public static Boolean getAttributeBoolean(Node node, String attributeName, Boolean defaultValue)
  {
    NamedNodeMap attributes = node.getAttributes();
    if (attributes == null) return defaultValue;

    Node attribute = attributes.getNamedItem(attributeName);
    if (attribute == null) return defaultValue;

    return Boolean.valueOf(attribute.getNodeValue().trim());
  }

  // TODO something better than IOException
  public static boolean getAttributeBooleanRequired(Node node, String attributeName) throws IOException
  {
    Boolean b = getAttributeBoolean(node, attributeName, null);
    if (b == null) throw new IOException("Mandatory attribute " + attributeName + " not specified on " + node.getNodeName());
    return b;
  }

  public static Element getElement(Element el, String tagName)
  {
    NodeList nodes = el.getElementsByTagName(tagName);
    if (nodes.getLength() > 0) return (Element) nodes.item(0);
    return null;
  }

  public static Element getElementRequired(Element el, String tagName) throws IOException
  {
    Element foundEl = getElement(el, tagName);
    if (foundEl == null) throw new IOException("element " + tagName + " not found");
    return foundEl;
  }

  public static String getElementString(Element el, String tagName) throws IOException
  {
    Element foundEl = getElement(el, tagName);
    if (foundEl == null) return "";
    return foundEl.getTextContent().trim();
  }

  public static String getElementStringRequired(Element el, String tagName) throws IOException
  {
    return getElementRequired(el, tagName).getTextContent().trim();
  }

  public static double getElementDouble(Element addTextEl, String tagName, double defaultValue) throws IOException
  {
    NodeList list = addTextEl.getElementsByTagName(tagName);
    if (list.getLength() == 0) return defaultValue;

    try {
      Double d = Double.valueOf(list.item(0).getTextContent().trim());
      if (d.isNaN() || d.isInfinite()) return defaultValue;
      return d;
    }
    catch (NumberFormatException e) {
      throw new IOException("Invalid " + tagName + " double value: " + list.item(0));
    }
  }
}
