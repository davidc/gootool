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

  public Level(String levelName) throws IOException
  {
    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();

    String levelPrefix = "res/levels/" + levelName + "/" + levelName;

    File sceneFile = worldOfGoo.getGameFile(levelPrefix + ".scene.bin");
    Document sceneDoc = GameFormat.decodeXmlBinFile(sceneFile);
    scene = new Scene(sceneDoc);

    File resourcesFile = worldOfGoo.getGameFile(levelPrefix + ".resrc.bin");
    Document resourcesDoc = GameFormat.decodeXmlBinFile(resourcesFile);
    resources = new Resources(resourcesDoc);

    File levelFile = worldOfGoo.getGameFile(levelPrefix + ".level.bin");
    Document levelDoc = GameFormat.decodeXmlBinFile(levelFile);
    levelContents = new LevelContents(levelDoc);
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
    Level l = new Level("EconomicDivide");

    System.out.println("l = " + l);
  }
}
