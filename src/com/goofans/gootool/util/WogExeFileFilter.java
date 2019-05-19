/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.util;

import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.wog.WorldOfGooWindows;
import com.goofans.gootool.wog.WorldOfGooMacOSX;
import com.goofans.gootool.wog.WorldOfGooLinux;

import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * A file filter for JFileChoosers that only allows selection of valid World of Goo installations.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class WogExeFileFilter extends FileFilter
{

  public WogExeFileFilter()
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
      String fileName = f.getName();

      switch (PlatformSupport.getPlatform()) {
        case WINDOWS:
          if (f.isDirectory()) {
            return true;
          }
          if (fileName.equalsIgnoreCase(WorldOfGooWindows.EXE_FILENAME)) {
            return true;
          }
          break;
        case MACOSX:
          if (f.isDirectory()) {
            if (!f.getName().endsWith(".app")) return true;
            for (String exeFilename : WorldOfGooMacOSX.EXE_FILENAMES) {
              if (new File(f, exeFilename).exists()) return true;
            }
          }
          break;
        case LINUX:
          if (f.isDirectory()) {
            return true;
          }
          for (String exeFilename : WorldOfGooLinux.EXE_FILENAMES) {
            if (fileName.equalsIgnoreCase(exeFilename)) {
              return true;
            }
          }
          break;
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
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
        return WorldOfGooWindows.EXE_FILENAME;
      case MACOSX:
        return "World of Goo";
      case LINUX:
        return WorldOfGooLinux.EXE_FILENAMES[0];
    }
    return null;
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
