/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.movie;

import com.goofans.gootool.facades.Source;
import com.goofans.gootool.projects.Project;
import net.infotrek.util.XMLStringBuffer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

  private final float length;
  private final BinActor[] actors;
  private final BinImageAnimation[] anims;

  private BinImageAnimation soundAnim;

  public BinMovie(SourceFile file) throws IOException
  {
    this(Utilities.readStreamIntoBytes(file.read()));
  }

  public BinMovie(byte[] contents)
  {
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

  public BinMovie(Document doc) throws IOException
  {
    Element movieEl = doc.getDocumentElement();
    if (!"movie".equals(movieEl.getTagName())) throw new IOException("Document element is not movie"); //NON-NLS

    length = XMLUtil.getAttributeFloatRequired(movieEl, "length"); //NON-NLS

    NodeList actorEls = movieEl.getElementsByTagName("actor"); //NON-NLS

    List<BinActor> actorsList = new ArrayList<BinActor>(actorEls.getLength());
    List<BinImageAnimation> animsList = new ArrayList<BinImageAnimation>(actorEls.getLength());

    for (int i = 0; i < actorEls.getLength(); ++i) {
      Element actorEl = (Element) actorEls.item(i);

      BinActor actor = new BinActor(actorEl);
      Element animEl = XMLUtil.getElement(actorEl, "animation");
      if (animEl == null) animEl = XMLUtil.getElement(actorEl, "complex-animation");
      if (animEl == null) throw new IOException("Actor " + i + " has no animation");
      BinImageAnimation anim = new BinImageAnimation(animEl);

      actorsList.add(actor);
      animsList.add(anim);
    }

    actors = actorsList.toArray(new BinActor[actorsList.size()]);
    anims = animsList.toArray(new BinImageAnimation[actorsList.size()]);

    // TODO sounds

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
    xml.addComment("This XML format is subject to change. Do not program against this format yet!"); //NON-NLS
    xml.addComment("See: http://goofans.com/forum/world-of-goo/modding/407"); //NON-NLS
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
    attributes.put("length", String.valueOf(length)); //NON-NLS
    xml.push("movie", attributes); //NON-NLS

    for (int i = 0; i < actors.length; i++) {
      BinActor actor = actors[i];
      BinImageAnimation anim = anims[i];
//      xml.push("element");// todo rename
      anim.validateContiguousFrames();
      actor.toXML(xml, anim);
//      xml.pop("element");
    }

    if (soundAnim != null) {
      xml.push("sounds"); //NON-NLS
      soundAnim.toXMLSounds(xml);
      xml.pop("sounds"); //NON-NLS
    }

    xml.pop("movie"); //NON-NLS
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardcodedLineSeparator", "HardCodedStringLiteral", "HardcodedFileSeparator", "StringConcatenation"})
  public static void main(String[] args) throws IOException
  {
    Project project = ProjectManager.simpleInit();
    SourceFile sourceGameRoot = project.getSource().getGameRoot();
    SourceFile f = sourceGameRoot.getChild("res\\movie");
    for (SourceFile file : f.list()) {
      String movie = file.getName();
      if (!"_generic".equals(movie)) {
        System.out.println("\n\n>>>>>>>> " + file.getName());

//        Document doc = GameFormat.decodeXmlBinFile(wog.getGameFile("res\\movie\\" + movie + "\\" + movie + ".resrc.bin"));
//        Resources r = new Resources(doc);
        BinMovie m = new BinMovie(sourceGameRoot.getChild(project.getGameAnimMovieFilename("res/movie/" + movie + "/" + movie + ".movie")));//, r);
//        System.out.println(m.toXMLDocument());
        Utilities.writeFile(new File("movie", movie + ".movie.xml"), m.toXMLDocument().getBytes());
      }
    }

//    String movie = "Chapter5End";
//    Document doc = GameFormat.decodeXmlBinFile(wog.getGameFile("res\\movie\\" + movie + "\\" + movie + ".resrc.bin"));
//    Resources r = new Resources(doc);
//    BinMovie m = new BinMovie(wog.getGameFile("res\\movie\\" + movie + "\\" + movie + ".movie.binltl"), r);
  }
}
