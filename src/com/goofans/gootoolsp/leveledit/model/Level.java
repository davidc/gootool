/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.model;

import java.io.File;
import java.io.IOException;

import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.wog.WorldOfGoo;
import org.w3c.dom.Document;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Level
{
  private final Scene scene;
  private final Resources resources;
  private final LevelContents levelContents;

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

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  public static void main(String[] args) throws IOException
  {
    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    worldOfGoo.init();
    Level l = new Level("EconomicDivide");

    System.out.println("l = " + l);
  }
}
