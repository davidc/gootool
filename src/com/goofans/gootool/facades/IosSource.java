/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.facades;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.zip.ZipFile;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class IosSource implements Source
{
  private final SourceFile realRoot;
  private final SourceFile gameRoot;
  private final ZipFile zipFile;

  public IosSource(File zipFile) throws IOException
  {
    if (!zipFile.exists()) throw new FileNotFoundException("File " + zipFile + " does not exist");

    this.zipFile = new ZipFile(zipFile, ZipFile.OPEN_READ);

    realRoot = new IosSourceFile(this, this.zipFile, "");
    gameRoot = realRoot;
  }

  public SourceFile getRealRoot()
  {
    return realRoot;
  }

  public SourceFile getGameRoot()
  {
    return gameRoot;
  }

  public void close() throws IOException
  {
    zipFile.close();
  }

  public static void main(String[] args) throws IOException
  {
    IosSource source = new IosSource(new File("source - with manual dirs.zip"));

    SourceFile root = source.getRealRoot();

    System.out.println("root = " + root);
    System.out.println("root.isDirectory() = " + root.isDirectory());

    SourceFile properties = root.getChild("properties");
    System.out.println("properties = " + properties);
    System.out.println("properties.getName() = " + properties.getName());
    System.out.println("properties.getFullName() = " + properties.getFullName());
    System.out.println("properties.isFile() = " + properties.isFile());
    System.out.println("properties.getParentDirectory() = " + properties.getParentDirectory());

    SourceFile text = properties.getChild("text.xml");
    System.out.println("text = " + text);
    System.out.println("text.getName() = " + text.getName());
    System.out.println("text.getFullName() = " + text.getFullName());

    System.out.println("text.getParentDirectory() = " + text.getParentDirectory());

    text = root.getChild("properties/text.xml");
    System.out.println("text = " + text);
    System.out.println("text.getName() = " + text.getName());
    System.out.println("text.getFullName() = " + text.getFullName());
    System.out.println("text.getSize() = " + text.getSize());
    System.out.println("text.lastModified() = " + new Date(text.lastModified()));
    System.out.println("text.isFile() = " + text.isFile());

    System.out.println("root.list() = " + root.list());

    System.out.println("root.getChild(\"res/balls\").list() = " + root.getChild("res/balls").list());
    System.out.println("root.getChild(\"res/images\").list() = " + root.getChild("res/images").list());


    System.out.println("root.getChild(\"res/images/backtoisland.png\").getName() = " + root.getChild("res/images/backtoisland.png").getName());

    source.close();
  }
}
