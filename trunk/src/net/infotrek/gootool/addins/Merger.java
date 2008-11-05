package net.infotrek.gootool.addins;

import net.infotrek.gootool.wog.WorldOfGoo;
import net.infotrek.gootool.io.BinFormat;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * @author David Croft (david.croft@infotrek.net)
 * @version $Id$
 */
public class Merger
{
  private Reader input;
  private Reader transform;

  public Merger(Reader input, Reader transform)
  {
    this.input = input;
    this.transform = transform;
  }

  public Merger(File encryptedFile, Reader transform) throws IOException
  {
    String input = BinFormat.decodeFile(encryptedFile);

    this.input = new StringReader(input);
    this.transform = transform;
  }

  public String merge() throws IOException, TransformerException
  {
    Source transformSource = new StreamSource(transform);

    Transformer transformer = TransformerFactory.newInstance().newTransformer(transformSource);

    Source src = new StreamSource(input);
    StringWriter writer = new StringWriter();
    Result res = new StreamResult(writer);
    transformer.transform(src, res);

//    System.out.println("writer.toString() = " + writer.toString());
    return writer.toString();
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException, TransformerException
  {
    WorldOfGoo.init();

    FileReader transformReader = new FileReader(new File("addins/src\\net.davidc.test.merger\\merge\\res\\levels\\GoingUp\\GoingUp.level"));
    File in = new File(WorldOfGoo.getWogDir(), "res\\levels\\GoingUp\\GoingUp.level.bin.2dboy");
    File out = new File(WorldOfGoo.getWogDir(), "res\\levels\\GoingUp\\GoingUp.level.bin");

//    FileReader transformReader = new FileReader(new File("addins/src\\net.davidc.test.merger\\merge\\res\\levels\\EconomicDivide\\EconomicDivide.level"));
//    File in = new File(WorldOfGoo.getWogDir(), "res\\levels\\EconomicDivide\\EconomicDivide.level.bin.2dboy");
//    File out = new File(WorldOfGoo.getWogDir(), "res\\levels\\EconomicDivide\\EconomicDivide.level.bin");

    Merger merger = new Merger(in, transformReader);
    String merged = merger.merge();
    System.out.println("merged = " + merged);

    BinFormat.encodeFile(out, merged);
  }
}
