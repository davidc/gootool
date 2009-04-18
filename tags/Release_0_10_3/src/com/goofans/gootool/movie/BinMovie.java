package com.goofans.gootool.movie;

import java.io.*;

import com.goofans.gootool.leveledit.model.Resources;
import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.wog.WorldOfGoo;
import org.w3c.dom.Document;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BinMovie
{
  private static final int BINMOVIE_LENGTH = 20;
  private static final int BINACTOR_LENGTH = 32;
  private static final int BINIMAGEANIMATION_LENGTH = 52;

  public BinMovie(File file, Resources resources) throws IOException
  {
    FileInputStream is = new FileInputStream(file);

    int fileLength = (int) file.length();
    byte[] contents = new byte[fileLength];
    if (is.read(contents) != fileLength) {
      throw new IOException("short read on movie " + file.getName());
    }
    is.close();


    float length = BinaryFormat.getFloat(contents, 0);
    System.out.println("length = " + length);
    int numActors = BinaryFormat.getInt(contents, 4);
    System.out.println("numActors = " + numActors);
    int actorsOffset = BinaryFormat.getInt(contents, 8);
    System.out.println("actorsOffset = " + actorsOffset);
    int animsOffset = BinaryFormat.getInt(contents, 12);
    System.out.println("animsOffset = " + animsOffset);
    int stringsOffset = BinaryFormat.getInt(contents, 16);
    System.out.println("stringsOffset = " + stringsOffset);

    for (int actorNum = 0; actorNum < numActors; ++actorNum) {
      System.out.println("\n== ACTOR " + actorNum + " ==\n");
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
      System.out.println("actor = " + actor);

      int binImageAnimOffset = BinaryFormat.getInt(contents, animsOffset + (actorNum * 4));
      System.out.println("binImageAnimOffset = " + binImageAnimOffset);
      BinImageAnimation anim = new BinImageAnimation(contents, binImageAnimOffset);
//      System.out.println("anim = " + anim);
    }
  }


  public static void main(String[] args) throws IOException
  {
    String movie = "Chapter5End";

    final WorldOfGoo wog = WorldOfGoo.getTheInstance();
    wog.init();
    Document doc = GameFormat.decodeXmlBinFile(wog.getGameFile("res\\movie\\" + movie + "\\" + movie + ".resrc.bin"));

    Resources r = new Resources(doc);
    BinMovie m = new BinMovie(wog.getGameFile("res\\movie\\" + movie + "\\" + movie + ".movie.binltl"), r);
  }
}
