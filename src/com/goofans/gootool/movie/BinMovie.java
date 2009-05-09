package com.goofans.gootool.movie;

import net.infotrek.util.XMLStringBuffer;

import java.io.*;
import java.util.Map;
import java.util.LinkedHashMap;

import com.goofans.gootool.leveledit.model.Resources;
import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.wog.WorldOfGoo;
import com.goofans.gootool.util.Utilities;
import org.w3c.dom.Document;

/**
 * TODO check number of actors is sensible, and other validation on the file when loading.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BinMovie
{
  //  private static final int BINMOVIE_LENGTH = 20;
  private static final int BINACTOR_LENGTH = 32;
//  private static final int BINIMAGEANIMATION_LENGTH = 52;

  private float length;
  private BinActor[] actors;
  private BinImageAnimation[] anims;

  private BinImageAnimation soundAnim;

  public BinMovie(File file) throws IOException
  {
    FileInputStream is = new FileInputStream(file);

    int fileLength = (int) file.length();
    byte[] contents = new byte[fileLength];
    if (is.read(contents) != fileLength) {
      throw new IOException("short read on movie " + file.getName());
    }
    is.close();


    length = BinaryFormat.getFloat(contents, 0);
    int numActors = BinaryFormat.getInt(contents, 4);
    int actorsOffset = BinaryFormat.getInt(contents, 8);
    int animsOffset = BinaryFormat.getInt(contents, 12);
    int stringsOffset = BinaryFormat.getInt(contents, 16);

    actors = new BinActor[numActors];
    anims = new BinImageAnimation[numActors];

    for (int actorNum = 0; actorNum < numActors; ++actorNum) {
//      System.out.println("\n== ACTOR " + actorNum + " ==\n");
      int binActorOffset = actorsOffset + (actorNum * BINACTOR_LENGTH);
      int actorType = BinaryFormat.getInt(contents, binActorOffset + 0);

      int imageStrIndex = BinaryFormat.getInt(contents, binActorOffset + 4);
      String imageStr = BinaryFormat.getString(contents, stringsOffset + imageStrIndex);

      int labelStrIndex = BinaryFormat.getInt(contents, binActorOffset + 8);
      String labelStr = BinaryFormat.getString(contents, stringsOffset + labelStrIndex);

      int fontStrIndex = BinaryFormat.getInt(contents, binActorOffset + 12);
      String fontStr = BinaryFormat.getString(contents, stringsOffset + fontStrIndex);

      float labelMaxWidth = BinaryFormat.getFloat(contents, binActorOffset + 16);
      float labelWrapWidth = BinaryFormat.getFloat(contents, binActorOffset + 20);
      int labelJustification = BinaryFormat.getInt(contents, binActorOffset + 24);
      float depth = BinaryFormat.getFloat(contents, binActorOffset + 28);

      BinActor actor = new BinActor(actorType, imageStr, labelStr, fontStr, labelMaxWidth, labelWrapWidth, labelJustification, depth);
//      System.out.println("actor = " + actor);

      int binImageAnimOffset = BinaryFormat.getInt(contents, animsOffset + (actorNum * 4));
//      System.out.println("binImageAnimOffset = " + binImageAnimOffset);
      BinImageAnimation anim = new BinImageAnimation(contents, binImageAnimOffset);
//      System.out.println("anim = " + anim);

      actors[actorNum] = actor;
      anims[actorNum] = anim;

      if (anim.hasSound) {
        if (soundAnim != null) {
          throw new AssertionError("got a second sound actor!");
        }
        soundAnim = anim.extractSoundAnim();
      }
    }
  }

  /**
   * Produces a well-formed XML document for this BinImageAnimation.
   *
   * @return XML document string
   */
  public String toXMLDocument()
  {
    StringBuffer sb = new StringBuffer();
    // TODO xml prolog
    XMLStringBuffer xml = new XMLStringBuffer(sb, "");
    toXML(xml);
    return xml.toXML();
  }

  /**
   * Produces a &lt;movie&gt; element, not a well-formed document.
   *
   * @param xml XMLStringBuffer to write into
   */
  public void toXML(XMLStringBuffer xml)
  {
    Map<String, String> attributes = new LinkedHashMap<String, String>();
    attributes.put("length", String.valueOf(length));
    xml.push("movie", attributes);

    for (int i = 0; i < actors.length; i++) {
      BinActor actor = actors[i];
      BinImageAnimation anim = anims[i];
//      xml.push("element");// todo rename
      anim.validateContiguousFrames();
      actor.toXML(xml, anim);
//      xml.pop("element");
    }

    if (soundAnim != null) {
      xml.push("sounds");
      soundAnim.toXMLSounds(xml);
      xml.pop("sounds");
    }

    xml.pop("movie");
  }

  public static void main(String[] args) throws IOException
  {

    final WorldOfGoo wog = WorldOfGoo.getTheInstance();
    wog.init();

    File f = wog.getGameFile("res\\movie");
    for (File file : f.listFiles()) {
      String movie = file.getName();
      if (!movie.equals("_generic")) {
        System.out.println("\n\n>>>>>>>> " + file.getName());

        Document doc = GameFormat.decodeXmlBinFile(wog.getGameFile("res\\movie\\" + movie + "\\" + movie + ".resrc.bin"));
//        Resources r = new Resources(doc);
        BinMovie m = new BinMovie(wog.getGameFile("res\\movie\\" + movie + "\\" + movie + ".movie.binltl"));//, r);
        System.out.println(m.toXMLDocument());
//        Utilities.writeFile(new File("movie", movie + ".movie.xml"), m.toXMLDocument().getBytes());

      }
    }

//    String movie = "Chapter5End";
//    Document doc = GameFormat.decodeXmlBinFile(wog.getGameFile("res\\movie\\" + movie + "\\" + movie + ".resrc.bin"));
//    Resources r = new Resources(doc);
//    BinMovie m = new BinMovie(wog.getGameFile("res\\movie\\" + movie + "\\" + movie + ".movie.binltl"), r);
  }
}
