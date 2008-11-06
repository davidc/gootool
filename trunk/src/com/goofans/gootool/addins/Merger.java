package com.goofans.gootool.addins;

import com.goofans.gootool.wog.WorldOfGoo;
import com.goofans.gootool.io.BinFormat;

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

  public Merger(File encryptedFile, Reader transform) throws IOException, TransformerException
  {
    this(new StringReader(BinFormat.decodeFile(encryptedFile)), transform);
  }

  public Merger(Reader input, Reader transform) throws TransformerException
  {
    this.input = input;

    Source transformSource = new StreamSource(transform);
    transformer = TransformerFactory.newInstance().newTransformer(transformSource);
  }

  public void setTransformParameter(String name, Object value)
  {
    transformer.setParameter(name, value);
  }

  public String merge() throws TransformerException
  {
    Source src = new StreamSource(input);
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
    BinFormat.encodeFile(out, result);
  }


  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException, TransformerException
  {
    WorldOfGoo.init();

//    FileReader transformReader = new FileReader(new File("addins/src\\net.davidc.test.merger\\merge\\res\\levels\\GoingUp\\GoingUp.level"));
//    File in = new File(WorldOfGoo.getWogDir(), "res\\levels\\GoingUp\\GoingUp.level.bin.2dboy");
//    File out = new File(WorldOfGoo.getWogDir(), "res\\levels\\GoingUp\\GoingUp.level.bin");

//    FileReader transformReader = new FileReader(new File("addins/src\\net.davidc.test.merger\\merge\\res\\levels\\EconomicDivide\\EconomicDivide.level"));
//    File in = new File(WorldOfGoo.getWogDir(), "res\\levels\\EconomicDivide\\EconomicDivide.level.bin.2dboy");
//    File out = new File(WorldOfGoo.getWogDir(), "res\\levels\\EconomicDivide\\EconomicDivide.level.bin");

    FileReader transformReader = new FileReader(new File("resources/watermark.xsl"));
    File in = new File(WorldOfGoo.getWogDir(), "properties/text.xml.bin");
    File out = new File(WorldOfGoo.getWogDir(), "properties/newtext.xml.bin");

    Merger merger = new Merger(in, transformReader);

    merger.setTransformParameter("watermark", "goofans.com");

    String merged = merger.merge();
    System.out.println("merged = " + merged);

    merger.writeEncoded(out);
  }

}
