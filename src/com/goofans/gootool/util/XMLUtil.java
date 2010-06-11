/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import com.goofans.gootool.io.UnicodeReader;
import com.goofans.gootool.io.GameFormat;

/**
 * XML manipulation utilities.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class XMLUtil
{
  private static final Logger log = Logger.getLogger(XMLUtil.class.getName());

  private XMLUtil()
  {
  }

  /**
   * Create a document from the contents of a file.
   *
   * @param file The file to read.
   * @return the Document read.
   * @throws IOException if the file cannot be read, or contains malformed XML.
   */
  public static Document loadDocumentFromFile(File file) throws IOException
  {
    try {
      return loadDocumentInternal(new FileInputStream(file));
    }
    catch (SAXException e) {
      log.log(Level.SEVERE, "Unable to parse " + file.getName(), e);
      throw new IOException("Unable to parse " + file.getName());
    }
  }

  /**
   * Create a document from the contents of a stream.
   *
   * @param is The stream to read.
   * @return the Document read.
   * @throws IOException if the stream cannot be read, or contains malformed XML.
   */
  public static Document loadDocumentFromInputStream(InputStream is) throws IOException
  {
    try {
      return loadDocumentInternal(is);
    }
    catch (SAXException e) {
      log.log(Level.SEVERE, "Unable to parse document", e);
      throw new IOException("Unable to parse document");
    }
  }

  private static Document loadDocumentInternal(InputStream is) throws IOException, SAXException
  {
    DocumentBuilder builder;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      log.log(Level.SEVERE, "Unable to create an XML document builder", e);
      throw new IOException("Unable to create an XML document builder: " + e.getLocalizedMessage());
    }

    /* Swallow any BOM at the start of file */

    UnicodeReader r = new UnicodeReader(is, GameFormat.DEFAULT_CHARSET);

    return builder.parse(new InputSource(r));
  }

  /**
   * Writes a document to a file.
   *
   * @param d    The document to write.
   * @param file The file to write to.
   * @throws TransformerException if an identity transformer instance doesn't exist or cannot be created, or if it fails to transform the XML.
   */
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

  /**
   * Writes a document to a string.
   *
   * @param d The document to write.
   * @return A String containing the document as XML.
   * @throws TransformerException if an identity transformer instance doesn't exist or cannot be created, or if it fails to transform the XML.
   */
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

  /**
   * Creates a new, blank document for use.
   *
   * @return A blank document.
   * @throws ParserConfigurationException if a suitable document builder cannot be found.
   */
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

  public static Float getAttributeFloat(Node node, String attributeName, Float defaultValue)
  {
    NamedNodeMap attributes = node.getAttributes();
    if (attributes == null) return defaultValue;

    Node attribute = attributes.getNamedItem(attributeName);
    if (attribute == null) return defaultValue;

    try {
      Float f = Float.valueOf(attribute.getNodeValue().trim());
      if (f.isNaN() || f.isInfinite()) return defaultValue;
      return f;
    }
    catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  public static float getAttributeFloatRequired(Node node, String attributeName) throws IOException
  {
    Float f = getAttributeFloat(node, attributeName, null);
    if (f == null) throw new IOException("Mandatory attribute " + attributeName + " not specified on " + node.getNodeName());
    return f;
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

  public static String getElementString(Element el, String tagName)
  {
    Element foundEl = getElement(el, tagName);
    if (foundEl == null) return "";
    return foundEl.getTextContent().trim();
  }

  public static String getElementStringRequired(Element el, String tagName) throws IOException
  {
    return getElementRequired(el, tagName).getTextContent().trim();
  }

  public static double getElementDouble(Element el, String tagName, double defaultValue) throws IOException
  {
    NodeList list = el.getElementsByTagName(tagName);
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

  public static int getElementInteger(Element el, String tagName, int defaultValue) throws IOException
  {
    NodeList list = el.getElementsByTagName(tagName);
    if (list.getLength() == 0) return defaultValue;

    try {
      return Integer.valueOf(list.item(0).getTextContent().trim());
    }
    catch (NumberFormatException e) {
      throw new IOException("Invalid " + tagName + " integer value: " + list.item(0));
    }
  }

  public static int getElementIntegerRequired(Element el, String tagName) throws IOException
  {
    NodeList list = el.getElementsByTagName(tagName);
    if (list.getLength() == 0) throw new IOException("element " + tagName + " not found");

    try {
      return Integer.valueOf(list.item(0).getTextContent().trim());
    }
    catch (NumberFormatException e) {
      throw new IOException("Invalid " + tagName + " integer value: " + list.item(0));
    }
  }

  public static Element findElementByAttributeValue(Element root, String elementName, String attributeName, String attributeValue, boolean caseSensitive)
  {
    NodeList elements = root.getElementsByTagName(elementName);
    for (int i = 0; i < elements.getLength(); ++i) {
      Element element = (Element) elements.item(i);
      Attr attribute = element.getAttributeNode(attributeName);
      if (attribute != null &&
              ((caseSensitive && attribute.getValue().equals(attributeValue)) ||
                      (!caseSensitive && attribute.getValue().equalsIgnoreCase(attributeValue)))) {
        return element;
      }
    }

    return null;
  }
}
