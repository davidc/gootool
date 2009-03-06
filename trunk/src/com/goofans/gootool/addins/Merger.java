package com.goofans.gootool.addins;

import com.goofans.gootool.wog.WorldOfGoo;
import com.goofans.gootool.io.FinalNewlineRemovingReader;
import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.io.UnicodeReader;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
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

  public Merger(File encryptedFile, Reader transform) throws IOException, TransformerException
  {
    this(new UnicodeReader(new ByteArrayInputStream(GameFormat.decodeBinFile(encryptedFile)), GameFormat.DEFAULT_CHARSET), transform);
//    System.out.println("encryptedFile = " + encryptedFile);
//    System.out.println(">>"+Utilities.readReaderIntoString(new FinalNewlineRemovingReader(new StringReader(BinFormat.decodeFile(encryptedFile))))+"<<");
  }

  public Merger(Reader input, Reader transform) throws TransformerException
  {
//    System.out.println("input = " + input);
    this.input = input;

    Source transformSource = new StreamSource(new FinalNewlineRemovingReader(transform));
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

    Source src = new StreamSource(new FinalNewlineRemovingReader(input));
    StringWriter writer = new StringWriter();
    Result res = new StreamResult(writer);
    transformer.transform(src, res);

//    System.out.println("writer.toString() = " + writer.toString());
    result = writer.toString();
    return result;
  }

  public void writeEncoded(File out) throws IOException
  {
    if (result == null) throw new RuntimeException("Not yet merged!");
    GameFormat.encodeBinFile(out, result.getBytes(GameFormat.DEFAULT_CHARSET));
  }


  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardcodedFileSeparator", "HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
  public static void main(String[] args) throws IOException, TransformerException
  {
    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    worldOfGoo.init();

//    FileReader transformReader = new FileReader(new File("addins/src\\net.davidc.test.merger\\merge\\res\\levels\\GoingUp\\GoingUp.level"));
//    File in = new File(WorldOfGoo.getWogDir(), "res\\levels\\GoingUp\\GoingUp.level.bin.2dboy");
//    File out = new File(WorldOfGoo.getWogDir(), "res\\levels\\GoingUp\\GoingUp.level.bin");

//    FileReader transformReader = new FileReader(new File("addins/src\\net.davidc.test.merger\\merge\\res\\levels\\EconomicDivide\\EconomicDivide.level"));
//    File in = new File(WorldOfGoo.getWogDir(), "res\\levels\\EconomicDivide\\EconomicDivide.level.bin.2dboy");
//    File out = new File(WorldOfGoo.getWogDir(), "res\\levels\\EconomicDivide\\EconomicDivide.level.bin");

    FileReader transformReader = new FileReader(new File("resources/watermark.xsl"));
    File in = worldOfGoo.getGameFile("properties/text.xml.bin");
    File out = worldOfGoo.getGameFile("properties/newtext.xml.bin");

    Merger merger = new Merger(in, transformReader);

    merger.setTransformParameter("watermark", "goofans.com");

    String merged = merger.merge();
    System.out.println("merged = " + merged);

    merger.writeEncoded(out);
  }
}
