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
   * and the .app file for Mac OS X/iOS. This is the complete directory that needs to be copied on install.
   *
   * @return the real root directory of this source.
   */
  SourceFile getRealRoot();

//  SourceFile getRoot();

  /**
   * Returns the game directory (containing properties and res). For Windows/Linux/iOS prior to 1.50, this is the same as the real root,
   * but for Mac OS X, it's the subdirectory Contents/Resources/game. This is the root of the game resources.
   *
   * @return the game directory of this source.
   */
  SourceFile getGameRoot();

  /**
   * Gets the disk filename for an XML file (with encryption suffix if applicable).
   *
   * @param baseName the name with the first suffix .scene/.level/etc, but without any .bin at the end
   * @return the disk filename
   */
  String getGameXmlFilename(String baseName);

  /**
   * Gets the disk filename for a PNG image (with encryption suffix if applicable).
   *
   * @param baseName the name without any .png at the end
   * @return the disk filename
   */
  String getGamePngFilename(String baseName);


  /**
   * Checks whether the game is the demo version (i.e. is missing chapter 3)
   *
   * @return true if the game is only the demo
   */
  boolean isDemoVersion();
}
