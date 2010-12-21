/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.model;

import java.io.IOException;

import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.projects.ProjectManager;
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
    SourceFile sourceDir = ProjectManager.simpleInit().getSource().getRoot().getChild("res/levels/" + levelName);

    SourceFile sceneFile = sourceDir.getChild(levelName + ".scene.bin");
    Document sceneDoc = GameFormat.decodeXmlBinFile(sceneFile);
    scene = new Scene(sceneDoc);

    SourceFile resourcesFile = sourceDir.getChild(levelName + ".resrc.bin");
    Document resourcesDoc = GameFormat.decodeXmlBinFile(resourcesFile);
    resources = new Resources(resourcesDoc);

    SourceFile levelFile = sourceDir.getChild(levelName + ".level.bin");
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
    Level l = new Level("EconomicDivide");

    System.out.println("l = " + l);
  }
}
