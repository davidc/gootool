/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.platform;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.MainController;
import com.goofans.gootool.util.Utilities;

/**
 * Support routines for Windows.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class WindowsSupport extends PlatformSupport
{
  private static final Logger log = Logger.getLogger(WindowsSupport.class.getName());

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String[] PROFILE_SEARCH_PATHS = {
          // NEW locations (under profile)
          "%LOCALAPPDATA%\\2DBoy\\WorldOfGoo", // generic, appdata
          "%USERPROFILE%\\AppData\\Local\\2DBoy\\WorldOfGoo", // vista
          "%USERPROFILE%\\Local Settings\\Application Data\\2DBoy\\WorldOfGoo", // xp

          // OLD locations (under All Users)
          "%ProgramData%\\2DBoy\\WorldOfGoo", // generic all users, vista (c:\programdata...)
          "%ALLUSERSPROFILE%\\Application Data\\2DBoy\\WorldOfGoo", // generic all users, xp but not internationalised C:\Documents and Settings\All Users\Application Data\2DBoy\WorldOfGoo
          "C:\\ProgramData\\2DBoy\\WorldOfGoo", // fixed, vista
          "C:\\Documents and Settings\\All Users\\Application Data\\2DBoy\\WorldOfGoo", // fixed, xp

          "%HOME%/.PlayOnLinux/wineprefix/WorldOfGoo/drive_c/windows/profiles/%USERNAME%/Application Data/2DBoy/WorldOfGoo", // PlayOnLinux, new format
          "%HOME%/.PlayOnLinux/wineprefix/WorldOfGoo/drive_c/windows/profiles/All Users/Application Data/2DBoy/WorldOfGoo", // PlayOnLinux, old format

          "%HOME%/.wine/drive_c/windows/profiles/%USERNAME%/Application Data/2DBoy/WorldOfGoo", //wine, new format
          "%HOME%/.wine/drive_c/windows/profiles/All Users/Application Data/2DBoy/WorldOfGoo", //wine, old format
  };

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String TOOL_STORAGE_DIRECTORY = "%APPDATA%\\GooTool\\";

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String[] SOURCE_SEARCH_PATHS = {
          "%ProgramFiles%\\WorldOfGoo",
          "%ProgramFiles(x86)%\\WorldOfGoo", // 32-bit dir on 64-bit windows
          "%ProgramFiles%\\World Of Goo",
          "%ProgramFiles(x86)%\\World Of Goo", // 32-bit dir on 64-bit windows
          "%SystemDrive%\\Program Files\\WorldOfGoo",
          "%SystemDrive%\\Program Files\\World Of Goo",
          "%SystemDrive%\\Games\\WorldOfGoo",
          "%SystemDrive%\\Games\\WorldOfGoo1.30",
          "%SystemDrive%\\Games\\World Of Goo",
          "%HOME%/.PlayOnLinux/wineprefix/WorldOfGoo/drive_c/Program Files/WorldOfGoo", // PlayOnLinux
          "%HOME%/.wine/drive_c/Program Files/WorldOfGoo", // wine
          "%ProgramFiles%\\Steam\\steamapps\\common\\world of goo", // steam
          "%ProgramFiles(x86)%\\Steam\\steamapps\\common\\world of goo" // steam 32-bit on 64-bit windows
  };

  public static final String EXE_FILENAME = "WorldOfGoo.exe"; // todo private


  WindowsSupport()
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

    return new File(dir, EXE_FILENAME).exists();
  }

  @Override
  protected void doLaunch(File targetDir) throws IOException
  {
    File exe = new File(targetDir, EXE_FILENAME);
    log.log(Level.FINE, "Launching " + exe + " in " + targetDir);

    ProcessBuilder pb = new ProcessBuilder(exe.getAbsolutePath());
    pb.directory(targetDir);
    pb.redirectErrorStream(true);
    final Process process = pb.start();

    // Close the process's stdin
    process.getOutputStream().close();
    // Launch a thread to consume its stdout and redirected stderr
    GooTool.executeTaskInThreadPool(new Runnable()
    {
      public void run()
      {
        InputStream is = process.getInputStream();
        try {
          while (is.read() != -1) {
            // do nothing
          }
        }
        catch (IOException e) {
          // do nothing
        }
        try {
          process.waitFor();
        }
        catch (InterruptedException e) {
          // do nothing
        }
      }
    });
  }
}
