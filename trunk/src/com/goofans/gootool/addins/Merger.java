/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.addins;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

import com.goofans.gootool.facades.Source;
import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.io.FinalNewlineRemovingReader;
import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.io.UnicodeReader;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectManager;

/**
 * Handles a single XSL transformation.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Merger
{
  private Reader input;
  private String result;
  private Transformer transformer;

//  static {
//    System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
//  }
//
//  public Merger(File encryptedFile, Reader transform) throws IOException, TransformerException
//  {
//    this(GameFormat.decodeBinFile(encryptedFile), transform);
////    System.out.println("encryptedFile = " + encryptedFile);
////    System.out.println(">>"+Utilities.readReaderIntoString(new FinalNewlineRemovingReader(new StringReader(BinFormat.decodeFile(encryptedFile))))+"<<");
//  }

  public Merger(byte[] input, Reader transform) throws IOException, TransformerException
  {
    this(new UnicodeReader(new ByteArrayInputStream(input), GameFormat.DEFAULT_CHARSET), transform);
  }

  public Merger(Reader input, Reader transform) throws TransformerException
  {
//    System.out.println("input = " + input);
    this.input = input;

    StreamSource transformSource = new StreamSource(new FinalNewlineRemovingReader(transform));
    try {
      transformer = TransformerFactory.newInstance().newTransformer(transformSource);
    }
    catch (TransformerFactoryConfigurationError e) {
      throw new TransformerException(e.getMessage(), e);
    }
  }

  public void setTransformParameter(String name, Object value)
  {
    transformer.setParameter(name, value);
  }

  public String merge() throws TransformerException
  {
//    try {
//      System.out.println(">>"+Utilities.readReaderIntoString(new FinalNewlineRemovingReader(input))+"<<");
//    }
//    catch (IOException e) {
//      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//    }

    StreamSource src = new StreamSource(new FinalNewlineRemovingReader(input));
    StringWriter writer = new StringWriter();
    Result res = new StreamResult(writer);
    transformer.transform(src, res);

//    System.out.println("writer.toString() = " + writer.toString());
    result = writer.toString();
    return result;
  }

  public byte[] getResult() throws UnsupportedEncodingException
  {
    return result.getBytes(GameFormat.DEFAULT_CHARSET);
  }


  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardcodedFileSeparator", "HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
  public static void main(String[] args) throws IOException, TransformerException
  {
//    FileReader transformReader = new FileReader(new File("addins/src\\net.davidc.test.merger\\merge\\res\\levels\\GoingUp\\GoingUp.level"));
//    File in = new File(WorldOfGoo.getWogDir(), "res\\levels\\GoingUp\\GoingUp.level.bin.2dboy");
//    File out = new File(WorldOfGoo.getWogDir(), "res\\levels\\GoingUp\\GoingUp.level.bin");

//    FileReader transformReader = new FileReader(new File("addins/src\\net.davidc.test.merger\\merge\\res\\levels\\EconomicDivide\\EconomicDivide.level"));
//    File in = new File(WorldOfGoo.getWogDir(), "res\\levels\\EconomicDivide\\EconomicDivide.level.bin.2dboy");
//    File out = new File(WorldOfGoo.getWogDir(), "res\\levels\\EconomicDivide\\EconomicDivide.level.bin");

    FileReader transformReader = new FileReader(new File("resources/watermark.xsl"));
    Project project = ProjectManager.simpleInit();
    Source source = project.getSource();
    try {
      SourceFile in = source.getGameRoot().getChild(project.getGameXmlFilename("properties/text.xml"));
      File out = new File("newtext.xml.bin");

      Merger merger = new Merger(GameFormat.AES_BIN_CODEC.decodeFile(in), transformReader);

      merger.setTransformParameter("watermark", "goofans.com");

      String merged = merger.merge();
      System.out.println("merged = " + merged);

      GameFormat.AES_BIN_CODEC.encodeFile(out, merger.getResult());
    }
    finally {
      source.close();
    }
  }
}
