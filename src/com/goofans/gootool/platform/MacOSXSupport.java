/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.platform;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.MainController;
import com.goofans.gootool.util.FileNameExtensionFilter;
import com.goofans.gootool.util.Utilities;

/**
 * Support routines for Mac OS X
 * <p/>
 * See http://developer.apple.com/documentation/Java/Reference/1.5.0/appledoc/api/index.html for the ApplicationListener API.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MacOSXSupport extends PlatformSupport implements ApplicationListener
{
  private static final Logger log = Logger.getLogger(MacOSXSupport.class.getName());

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String[] PROFILE_SEARCH_PATHS = {
          "%HOME%/Library/Application Support/WorldOfGoo/"
  };

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String TOOL_STORAGE_DIRECTORY = "%HOME%/Library/Application Support/GooTool/";

  private MainController mainController;

  @SuppressWarnings({"HardcodedFileSeparator"})
  public static final String[] SOURCE_SEARCH_PATHS = {
          "/Applications/World of Goo.app",
  };

  @SuppressWarnings({"HardcodedFileSeparator"})
  public static final String[] EXE_FILENAMES = {"Contents/MacOS/World of Goo",
          "Contents/MacOS/BigMacWrapper", "Contents/MacOS/BigMacWrapper_ppc", "Contents/MacOS/BigMacWrapper_x86"};

  MacOSXSupport()
  {
  }

  @Override
  protected boolean doPreStartup(List<String> args)
  {
    // Mac OS X handles single instance, and our ApplicationListener handles file opening,
    // so we always permit startup.
    return true;
  }

  @Override
  protected void doStartup(MainController mainController)
  {
    this.mainController = mainController;

    Application app = new Application();
    app.addAboutMenuItem();
    app.addApplicationListener(this);
    log.fine("Mac: listener added");
  }

  public void handleAbout(ApplicationEvent event)
  {
    log.fine("Mac: handleAbout");
    mainController.openAboutDialog();
    event.setHandled(true);
  }

  public void handleOpenApplication(ApplicationEvent event)
  {
  }

  public void handleReOpenApplication(ApplicationEvent event)
  {
  }

  public void handleOpenFile(ApplicationEvent event)
  {
    log.fine("Mac: handleOpenFile");
    final File addinFile = new File(event.getFilename());
    GooTool.queueTask(new Runnable()
    {
      public void run()
      {
        mainController.installAddin(addinFile);
      }
    });
    event.setHandled(true);
  }

  public void handlePreferences(ApplicationEvent event)
  {
  }

  public void handlePrintFile(ApplicationEvent event)
  {
  }

  public void handleQuit(ApplicationEvent event)
  {
    log.fine("Mac: handleQuit");
    mainController.maybeExit();
//    event.setHandled(true);  this causes us to always quit even if they cancel
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

    chooser.setDialogTitle("Choose where to save your World of Goo");
    chooser.setFileFilter(new FileNameExtensionFilter("Application", "app"));

    if (defaultFile != null)
      chooser.setSelectedFile(defaultFile);
    else
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
    log.log(Level.FINE, "Launching application at " + targetDir); //NON-NLS

    ProcessBuilder pb = new ProcessBuilder("open", targetDir.getAbsolutePath()); //NON-NLS
    pb.directory(targetDir);
    pb.start();
  }
}
