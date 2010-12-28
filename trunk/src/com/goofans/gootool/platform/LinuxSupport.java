/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.platform;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.MainController;
import com.goofans.gootool.util.Utilities;

/**
 * Support routines for Linux.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LinuxSupport extends PlatformSupport
{
  private static final Logger log = Logger.getLogger(LinuxSupport.class.getName());

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String[] PROFILE_SEARCH_PATHS = {
          "%HOME%/.WorldOfGoo"
  };

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String TOOL_STORAGE_DIRECTORY = "%HOME%/.gootool/";

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String LASTRUN_FILE = "%HOME%/.WorldOfGoo/LastRun.txt";

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String[] SOURCE_SEARCH_PATHS = {
          "/opt/WorldOfGoo",
          "%HOME%/WorldOfGoo",
          "%HOME%/games/WorldOfGoo",
          "/usr/games/WorldOfGoo",
          "/usr/local/WorldOfGoo",
          "/usr/local/games/WorldOfGoo"
  };

  public static final String[] EXE_FILENAMES = {"WorldOfGoo.bin", "WorldOfGoo.bin32", "WorldOfGoo.bin64"};
  public static final String SCRIPT_FILENAME = "WorldOfGoo";

  LinuxSupport()
  {
  }

  @Override
  protected boolean doPreStartup(List<String> args)
  {
    return SingleInstance.getInstance().singleInstance(args);
  }

  @Override
  protected void doStartup(MainController mainController)
  {
  }

  @Override
  public String[] doGetProfileSearchPaths()
  {
    return PROFILE_SEARCH_PATHS;
  }

  @Override
  public File doGetToolStorageDirectory()
  {
    return new File(Utilities.expandEnvVars(TOOL_STORAGE_DIRECTORY));
  }


  @Override
  protected File doChooseLocalTargetDir(Component mainFrame, File defaultFile)
  {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Choose a directory to save into");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    chooser.setSelectedFile(defaultFile);

    if (chooser.showSaveDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
      return null;
    }

    return chooser.getSelectedFile();
  }

  /**
   * Attempts to locate WoG in various default locations.
   */
  @Override
  protected File doDetectWorldOfGooSource()
  {
    // Look in the "LastRun.txt" file

    File lastDirFile = new File(Utilities.expandEnvVars(LASTRUN_FILE));
    if (lastDirFile.exists()) {
      try {
        BufferedReader r = new BufferedReader(new FileReader(lastDirFile));
        try {
          String line;
          while ((line = r.readLine()) != null) {
            String[] bits = line.split("=", 2);
            log.finest("lastrun: " + bits[0] + " -> " + bits[1]);
            if ("gamedir".equalsIgnoreCase(bits[0])) { //NON-NLS
              File dir = new File(bits[1]);
              if (isWorldOfGooInDir(dir)) {
                log.info("Found World of Goo through lastrun pointer at " + dir);
                return dir;
              }
            }
          }
        }
        finally {
          r.close();
        }
      }
      catch (IOException e) {
        log.log(Level.WARNING, "Can't read lastrun file " + lastDirFile, e);
      }
    }

    for (String searchPath : SOURCE_SEARCH_PATHS) {
      String newSearchPath = Utilities.expandEnvVars(searchPath);

      if (newSearchPath != null) {
        File dir = new File(newSearchPath);
        if (isWorldOfGooInDir(dir)) {
          log.info("Found World of Goo through default search of \"" + searchPath + "\" at: " + dir);
          return dir;
        }
      }
    }

    return null;
  }

  private boolean isWorldOfGooInDir(File dir)
  {
    log.finest("looking for World of Goo at " + dir);

    for (String exeFilename : EXE_FILENAMES) {
      if (new File(dir, exeFilename).exists()) {
        return true;
      }
    }

    return false;
  }

  @Override
  protected void doLaunch(File targetDir) throws IOException
  {
    File exe = new File(targetDir, SCRIPT_FILENAME);
    log.log(Level.FINE, "Launching " + exe + " in " + targetDir);

    ProcessBuilder pb = new ProcessBuilder(exe.getAbsolutePath());
    pb.directory(targetDir);
    pb.start();
  }
}
