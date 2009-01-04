package com.goofans.gootool.leveledit.model;

import java.io.File;
import java.io.StringReader;
import java.io.IOException;

import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.wog.WorldOfGoo;
import org.w3c.dom.Document;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Level
{
  private Scene scene;
  private Resources resources;
  private LevelContents levelContents;

  public Level(File wogDir, String prefix) throws IOException
  {
    File dir = new File(wogDir, "res/levels/" + prefix); // TODO use getGameFile

    File sceneFile = new File(dir, prefix + ".scene.bin");
    String xml = GameFormat.decodeBinFile(sceneFile);
    Document doc = XMLUtil.loadDocumentFromReader(new StringReader(xml));
    scene = new Scene(doc);

    File resourcesFile = new File(dir, prefix + ".resrc.bin");
    xml = GameFormat.decodeBinFile(resourcesFile);
    doc = XMLUtil.loadDocumentFromReader(new StringReader(xml));
    resources = new Resources(doc, wogDir);

    File levelFile = new File(dir, prefix + ".level.bin");
    xml = GameFormat.decodeBinFile(levelFile);
    doc = XMLUtil.loadDocumentFromReader(new StringReader(xml));
    levelContents = new LevelContents(doc);
  }

  public Scene getScene()
  {
    return scene;
  }

  public Resources getResources()
  {
    return resources;
  }

  public LevelContents getLevelContents()
  {
    return levelContents;
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    worldOfGoo.init();
    Level l = new Level(worldOfGoo.getWogDir(), "EconomicDivide");

    System.out.println("l = " + l);
  }
}
