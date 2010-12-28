/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.util;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * A file filter for JFileChoosers that only allows selection of valid profile files (pers2.dat).
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProfileFileFilter extends FileFilter
{
  public ProfileFileFilter()
  {
  }

  /**
   * Tests the specified file, returning true if the file is
   * accepted, false otherwise. True is returned if the extension
   * matches one of the file name extensions of this {@code
   * FileFilter}, or the file is a directory.
   *
   * @param f the {@code File} to test
   * @return true if the file is to be accepted, false otherwise
   */
  @Override
  public boolean accept(File f)
  {
    if (f != null) {
      if (f.isDirectory()) {
        return true;
      }
      if ("pers2.dat".equalsIgnoreCase(f.getName())) { //NON-NLS
        return true;
      }
    }
    return false;
  }

  /**
   * The description of this filter. For example: "JPG and GIF Images."
   *
   * @return the description of this filter
   */
  @Override
  public String getDescription()
  {
    return "World of Goo Profile (pers2.dat)";
  }

  /**
   * Returns a string representation of the {@code FileNameExtensionFilter}.
   * This method is intended to be used for debugging purposes,
   * and the content and format of the returned string may vary
   * between implementations.
   *
   * @return a string representation of this {@code FileNameExtensionFilter}
   */
  @Override
  @SuppressWarnings({"StringConcatenation", "DuplicateStringLiteralInspection"})
  public String toString()
  {
    return super.toString() + "[description=" + getDescription() + "]";
  }
}