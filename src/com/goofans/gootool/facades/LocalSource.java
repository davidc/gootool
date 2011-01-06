/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.facades;

import java.io.File;
import java.io.IOException;

import com.goofans.gootool.platform.PlatformSupport;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LocalSource implements Source
{
  private final File rootDirectory;

  private final SourceFile realRoot;
  private final SourceFile gameRoot;

  public LocalSource(File rootDirectory)
  {
    this.rootDirectory = rootDirectory;

    realRoot = new LocalSourceFile(this, this.rootDirectory);

    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
      case LINUX:
        gameRoot = realRoot;
        break;
      case MACOSX:
        gameRoot = realRoot.getChild("Contents/Resources/game");
        break;
      default:
        throw new RuntimeException("Unknown platform");
    }
  }

  public SourceFile getRealRoot()
  {
    return realRoot;
  }

  public SourceFile getGameRoot()
  {
    return gameRoot;
  }

  @Override
  public String toString()
  {
    return "LocalSource{" +
            "rootDirectory=" + rootDirectory +
            '}';
  }

  public void close() throws IOException
  {
    // Nothing to do for a local source.
    // TODO have SourceFile throw an exception if we're closed
  }
}
