package com.goofans.gootool.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.*;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

/**
 * @author David Croft (david.croft@infotrek.net)
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

    // Write the DOM document to the file
    Transformer xformer = TransformerFactory.newInstance().newTransformer();
    xformer.transform(source, result);

    return writer.toString();
  }
}
