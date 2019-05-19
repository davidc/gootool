/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.wog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.util.FileNameExtensionFilter;
import com.goofans.gootool.util.Utilities;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
@SuppressWarnings({"HardcodedFileSeparator"})
public class WorldOfGooMacOSX extends WorldOfGoo
{
  private static final Logger log = Logger.getLogger(WorldOfGooWindows.class.getName());

  private static final String[] SEARCH_PATHS = {
          "/Applications/World of Goo.app",
  };
  public static final String[] EXE_FILENAMES = {"Contents/MacOS/World of Goo",
          "Contents/MacOS/BigMacWrapper", "Contents/MacOS/BigMacWrapper_ppc", "Contents/MacOS/BigMacWrapper_x86"};

  private static final String OLD_ADDIN_DIR = "Contents/Resources/addins";

  private boolean wogFound;
  private File wogDir;
  private File customDir;

  WorldOfGooMacOSX()
  {
  }

  @Override
  public boolean isWogFound()
  {
    return wogFound;
  }

  @Override
  public boolean isCustomDirSet()
  {
    return customDir != null;
  }

  /**
   * Attempts to locate WoG in various default locations.
   */
  @Override
  public void init()
  {
    String userWogDir = ToolPreferences.getWogDir();

    if (userWogDir != null) {
      if (locateWogAtPath(new File(userWogDir))) {
        log.info("Found World of Goo at stored location of \"" + userWogDir + "\" at: " + wogDir);
        return;
      }
    }

    for (String searchPath : SEARCH_PATHS) {
      String newSearchPath = Utilities.expandEnvVars(searchPath);

      if (newSearchPath != null && locateWogAtPath(new File(newSearchPath))) {
        log.info("Found World of Goo through default search of \"" + searchPath + "\" at: " + wogDir);
        return;
      }
    }
  }

  private boolean locateWogAtPath(File searchPath)
  {
    log.finest("looking for World of Goo at " + searchPath);

    for (String exeFilename : EXE_FILENAMES) {
      if (new File(searchPath, exeFilename).exists()) {
        foundWog(searchPath);
        return true;
      }
    }
    return false;
  }

  /* We've found WoG at the given path. Read in some bits */

  private void foundWog(File searchPath)
  {
    wogFound = true;
    wogDir = searchPath;

    ToolPreferences.setWogDir(wogDir.getAbsolutePath());

    String customDirPref = ToolPreferences.getCustomDir();

    try {
      if (customDirPref != null) {
        setCustomDir(new File(customDirPref));
      }
    }
    catch (IOException e) {
      log.log(Level.WARNING, "Can't use saved custom directory " + customDirPref, e);
    }
  }

  /**
   * Attempts to locate WoG in the user-supplied location.
   *
   * @param path Path to exe, excluding exe itself
   * @throws java.io.FileNotFoundException if WorldOfGoo.exe wasn't found at this path
   */
  @Override
  public void init(File path) throws FileNotFoundException
  {
    if (!locateWogAtPath(path)) {
      throw new FileNotFoundException("World of Goo was not found at " + path);
    }
    log.info("Found World of Goo through user selection at: " + wogDir);
  }

  @Override
  public void launch() throws IOException
  {
    log.log(Level.FINE, "Launching in " + customDir);

    ProcessBuilder pb = new ProcessBuilder("open", getCustomDir().getAbsolutePath());
    pb.directory(customDir);
    pb.start();
  }

  @Override
  public File getWogDir() throws IOException
  {
    if (!wogFound) {
      throw new IOException("World of Goo isn't found yet");
    }
    return wogDir;
  }

  @Override
  public void setCustomDir(File customDir) throws IOException
  {
    if (customDir.exists() && !customDir.isDirectory()) throw new IOException(customDir + " isn't a directory");
    if (!customDir.exists() && !customDir.mkdir()) throw new IOException("Can't create " + customDir);

    Utilities.testDirectoryWriteable(customDir);

    this.customDir = customDir;

    ToolPreferences.setCustomDir(customDir.getAbsolutePath());
  }

  @Override
  public File getCustomDir() throws IOException
  {
    if (customDir == null) {
      throw new IOException("Custom dir isn't selected yet");
    }
    return customDir;
  }

  @Override
  public boolean isFirstCustomBuild() throws IOException
  {
    for (String exeFilename : EXE_FILENAMES) {
      if (new File(getCustomDir(), exeFilename).exists())
        return false;
    }
    return true;
  }

  @Override
  public File getGameFile(String pathname) throws IOException
  {
    return new File(getWogDir(), "Contents/Resources/game/" + pathname);
  }

  @Override
  public File getCustomGameFile(String pathname) throws IOException
  {
    return new File(getCustomDir(), "Contents/Resources/game/" + pathname);
  }

  @Override
  public File chooseCustomDir(Component mainFrame)
  {
    JFileChooser chooser = new JFileChooser();

    chooser.setDialogTitle("Choose where to save your World of Goo");
    chooser.setFileFilter(new FileNameExtensionFilter("Application", "app"));

    chooser.setSelectedFile(new File(System.getProperty("user.home") + "/Desktop", "My Custom World of Goo"));

    if (chooser.showSaveDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
      return null;
    }

    File selectedFile = chooser.getSelectedFile();

    if (!selectedFile.getName().endsWith(".app")) {
      selectedFile = new File(selectedFile.getAbsoluteFile() + ".app");
    }

    return selectedFile;
  }

  @Override
  public File getOldAddinsDir()
  {
    if (customDir == null) return null;

    File oldAddinsDir = new File(customDir, OLD_ADDIN_DIR);
    if (!oldAddinsDir.exists()) oldAddinsDir = null;
    return oldAddinsDir;
  }
}