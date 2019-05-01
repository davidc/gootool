/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.facades;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.DebugUtil;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LocalSource implements Source
{
  private static final Logger log = Logger.getLogger(LocalSource.class.getName());

  private final File rootDirectory;

  private SourceFile realRoot;
  private SourceFile gameRoot;

  private boolean unencrypted = false;

  public LocalSource(File rootDirectory)
  {
    if (rootDirectory == null || !rootDirectory.exists() || !rootDirectory.isDirectory()) {
      throw new RuntimeException("Root directory does not exist");
    }

    this.rootDirectory = rootDirectory;

    realRoot = new LocalSourceFile(this, this.rootDirectory);

    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
      case LINUX:
        // If we're version 1.5+ on Windows, the resources are now in a "game" subdirectory
        gameRoot = realRoot.getChild("game");
        if (gameRoot == null || !gameRoot.isDirectory()) {
          // pre 1.5
          gameRoot = realRoot;
        }
        break;
      case MACOSX:
        gameRoot = realRoot.getChild("Contents/Resources/game");
        break;
      default:
        throw new RuntimeException("Unknown platform");
    }

    // If it's version 1.5+ then the bin files are unencrypted
    SourceFile testFile = getGameRoot().getChild("res/levels/island1/island1.level");
    if (testFile != null && testFile.isFile()) {
      log.finer("World of Goo is 1.5 or later, files are not encrypted");
      unencrypted = true;
    }

    log.fine("Local source initialised, real root = " + realRoot + ", game root = " + gameRoot);
  }

  public SourceFile getRealRoot()
  {
    if (realRoot == null) {
      throw new NullPointerException("Attempt to getRealRoot of already-closed LocalSource");
    }
    return realRoot;
  }

  public SourceFile getGameRoot()
  {
    if (gameRoot == null) {
      throw new NullPointerException("Attempt to getRealRoot of already-closed LocalSource");
    }
    return gameRoot;
  }

  public String getGameXmlFilename(String baseName)
  {
    if (unencrypted) {
      return baseName;
    }
    else {
      return baseName + ".bin"; //NON-NLS
    }
  }


  public String getGamePngFilename(String baseName)
  {
    if (PlatformSupport.getPlatform() == PlatformSupport.Platform.MACOSX) {
      return baseName + ".png.binltl"; //NON-NLS
    }
    else {
      return baseName + ".png"; //NON-NLS
    }
  }

  public boolean isDemoVersion()
  {
    SourceFile flagFile = getGameRoot().getChild(getGameXmlFilename("res/levels/island3/island3.level")); // NON-NLS
    return flagFile == null || !flagFile.isFile();
  }

  @Override
  public String toString()
  {
    return "LocalSource{" +
            "rootDirectory=" + rootDirectory +
            '}';
  }

  boolean isClosed()
  {
    return realRoot == null;
  }

  public void close() throws IOException
  {
    if (realRoot == null) {
      throw new IOException("Closing an already-closed LocalSource");
    }
    realRoot = gameRoot = null;
    // Nothing really to do for a local source, but we mark closed anyway so we can catch bad coding more easily on local targets
  }

  public static void main(String[] args) throws IOException
  {
    DebugUtil.setAllLogging();
    LocalSource src = new LocalSource(new File("c:/games/worldofgoo"));
    SourceFile file;
    try {
      System.out.println("src.isDemoVersion() = " + src.isDemoVersion());
      file = src.getGameRoot().getChild(src.getGameXmlFilename("res/levels/island3/island3.level")); // NON-NLS
      System.out.println("file.read() = " + file.read());
    }
    finally {
      src.close();
    }

    System.out.println("This should exception:");
    System.out.println("file.read() = " + file.read());
  }
}
