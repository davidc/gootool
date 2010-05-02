/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
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
import com.goofans.gootool.util.Utilities;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
@SuppressWarnings({"HardcodedFileSeparator"})
public class WorldOfGooWindows extends WorldOfGoo
{
  private static final Logger log = Logger.getLogger(WorldOfGooWindows.class.getName());

  private static final String[] SEARCH_PATHS = {
          "%ProgramFiles%\\WorldOfGoo", "%ProgramFiles%\\World Of Goo",
          "%SystemDrive%\\Program Files\\WorldOfGoo", "%SystemDrive%\\Program Files\\World Of Goo",
          "%SystemDrive%\\Games\\WorldOfGoo", "%SystemDrive%\\Games\\World Of Goo",
          "%HOME%/.PlayOnLinux/wineprefix/WorldOfGoo/drive_c/Program Files/WorldOfGoo", // PlayOnLinux
          "%HOME%/.wine/drive_c/Program Files/WorldOfGoo", // wine
          "%ProgramFiles\\Steam\\steamapps\\common\\world of goo" // steam
  };
  public static final String EXE_FILENAME = "WorldOfGoo.exe";


  private boolean wogFound;
  private File wogDir;
  private File addinsDir;
  private File customDir;
  static final String USER_CONFIG_FILE = "properties\\config.txt";

  private static final String ADDIN_DIR = "addins";

  WorldOfGooWindows()
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
    File f = new File(searchPath, EXE_FILENAME);

    log.finest("looking for World of Goo at " + f);
    if (f.exists()) {
      foundWog(searchPath);
      return true;
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
   * @param path Path to exe, including exe itself
   * @throws java.io.FileNotFoundException if WorldOfGoo.exe wasn't found at this path
   */
  @Override
  public void init(File path) throws FileNotFoundException
  {
    if (!locateWogAtPath(path.getParentFile())) {
      throw new FileNotFoundException("WorldOfGoo.exe was not found at " + path);
    }
    log.info("Found World of Goo through user selection at: " + wogDir);
  }

  @Override
  public void launch() throws IOException
  {
    File exe = new File(getCustomDir(), EXE_FILENAME);
    log.log(Level.FINE, "Launching " + exe + " in " + customDir);

    // TODO why does this take forever when launching under IDEA?
    ProcessBuilder pb = new ProcessBuilder(exe.getAbsolutePath());
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

    addinsDir = new File(customDir, ADDIN_DIR);
    Utilities.mkdirsOrException(addinsDir);

    updateInstalledAddins();
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
    return !new File(getCustomDir(), EXE_FILENAME).exists();
  }

  @Override
  public File getGameFile(String pathname) throws IOException
  {
    return new File(getWogDir(), pathname);
  }

  @Override
  public File getCustomGameFile(String pathname) throws IOException
  {
    return new File(getCustomDir(), pathname);
  }

  @Override
  protected File getAddinInstalledFile(String addinId) throws IOException
  {
    return new File(getAddinInstalledDir(), addinId + GOOMOD_EXTENSION_WITH_DOT);
  }

  @Override
  public File chooseCustomDir(Component mainFrame)
  {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Choose a directory to save into");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    if (chooser.showSaveDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
      return null;
    }

    return chooser.getSelectedFile();
  }

  @Override
  protected File getAddinInstalledDir() throws IOException
  {
    if (addinsDir == null) {
      throw new IOException("Addins directory isn't selected yet");
    }
    return addinsDir;
  }
}
