/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.facades;

import java.io.Closeable;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface Source extends Closeable
{
  /**
   * Returns the full root directory of the game installation, i.e. the directory of WorldOfGoo(.exe) for Windows/Linux,
   * and the .app file for Mac OS X/iOS.
   *
   * @return the real root directory of this source.
   */
  SourceFile getRealRoot();

//  SourceFile getRoot();

  /**
   * Returns the game directory (containing properties and res). For Windows/Linux/iOS, this is the same as the real root,
   * but for Mac OS X, it's the subdirectory Contents/Resources/game.
   *
   * @return the game directory of this source.
   */
  SourceFile getGameRoot();
}
