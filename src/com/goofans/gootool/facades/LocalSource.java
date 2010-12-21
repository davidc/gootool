/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.facades;

import java.io.File;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LocalSource implements Source
{
  private final File rootDirectory;

  public LocalSource(File rootDirectory)
  {
    this.rootDirectory = rootDirectory;
  }

  public SourceFile getRoot()
  {
    return new LocalSourceFile(this, rootDirectory);
  }

  @Override
  public String toString()
  {
    return "LocalSource{" +
            "rootDirectory=" + rootDirectory +
            '}';
  }
}
