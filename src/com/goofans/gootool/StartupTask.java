/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.view.MainFrame;
import com.goofans.gootool.wog.WorldOfGoo;
import com.goofans.gootool.siteapi.VersionCheck;
import com.goofans.gootool.siteapi.RatingUpdateTask;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sequentially performs GooTool's initialisation tasks before revealing the main UI.
 * <p/>
 * This isn't run on the event dispatch thread so any UI interaction must use invokeAndWait().
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class StartupTask extends ProgressIndicatingTask
{
  private static final Logger log = Logger.getLogger(StartupTask.class.getName());
  private final Controller controller;
  private final GooToolResourceBundle resourceBundle;

  public StartupTask(Controller controller)
  {
    this.controller = controller;
    resourceBundle = GooTool.getTextProvider();
  }

  @Override
  public void run() throws Exception
  {
    beginStep(resourceBundle.getString("launcher.locategoo"), false);
    initWog();

    beginStep(resourceBundle.getString("launcher.loadaddins"), false);
    initAddins();

    beginStep(resourceBundle.getString("launcher.profile"), false);
    initProfile();

    beginStep(resourceBundle.getString("launcher.loadconfig"), false);
    Configuration c = initModel();

    beginStep(resourceBundle.getString("launcher.initgui"), false);
    MainFrame mainFrame = initControllerAndView(c);

    // Schedule a check for new version in 2 seconds
    GooTool.scheduleTask(new VersionCheck(mainFrame, false), 2000);

    // Maybe launch a new thread to download billboards
    BillboardUpdater.maybeUpdateBillboards();

    // Launch thread to retrieve user ratings. Initially in 0.5 seconds, thereafter every 15 minutes.
    GooTool.scheduleTaskWithFixedDelay(new RatingUpdateTask(), 500, 15 * 60 * 1000);

    GooTool.startupIsComplete();
  }

  private void initWog()
  {
    // Locate WoG
    final WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    worldOfGoo.init();

    if (!worldOfGoo.isWogFound()) {

      // Do it in the UI thread
      try {
        SwingUtilities.invokeAndWait(new Runnable()
        {
          public void run()
          {
            String message = resourceBundle.getString("launcher.locategoo.notfound.message." + PlatformSupport.getPlatform().toString().toLowerCase());
            log.finer("dialog opening");
            JOptionPane.showMessageDialog(null, message, resourceBundle.getString("launcher.locategoo.notfound.title"), JOptionPane.WARNING_MESSAGE);

            log.finer("dialog closed");
            while (!worldOfGoo.isWogFound()) {
              int result = controller.askToLocateWog();
              if (result == -2) {
                log.info("User refused to locate WorldOfGoo.exe, exiting");
                System.exit(2);
              }
            }
          }
        });
      }
      catch (Exception e) {
        throw new RuntimeException("Unexpected exception locating World of Goo", e);
      }
    }

    warnIfDemo(worldOfGoo);
  }

  private void warnIfDemo(WorldOfGoo worldOfGoo)
  {
    try {
      if (worldOfGoo.getGameFile("res/levels/island3/island3.level.bin").exists()) {
        return;
      }
    }
    catch (IOException e) {
      // do nothing
    }
    showMessageDialog(resourceBundle.getString("launcher.demo.title"), resourceBundle.getString("launcher.demo.message"), JOptionPane.WARNING_MESSAGE);
  }

  private void initAddins()
  {
    WorldOfGoo wog = WorldOfGoo.getTheInstance();

    File addinsDir = null;
    try {
      addinsDir = wog.getAddinsDir();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to create addins directory " + addinsDir, e);
      showMessageDialog(resourceBundle.getString("launcher.initaddins.cantcreate.title"),
              resourceBundle.formatString("launcher.loadaddins.cantcreate.message", addinsDir, e.getLocalizedMessage()),
              JOptionPane.ERROR_MESSAGE);
    }

    // Possibly migrate from old addins directory

    File oldAddinsDir = wog.getOldAddinsDir();

    if (oldAddinsDir != null) {
      showMessageDialog(resourceBundle.getString("launcher.migrateaddins.title"),
              resourceBundle.formatString("launcher.migrateaddins.message", oldAddinsDir, addinsDir),
              JOptionPane.INFORMATION_MESSAGE);

      for (File file : oldAddinsDir.listFiles()) {
        File destFile = new File(addinsDir, file.getName());
        try {
          Utilities.deleteFileIfExists(destFile);
          Utilities.moveFile(file, destFile);
        }
        catch (IOException e) {
          log.log(Level.WARNING, "Unable to move " + file + " to " + destFile, e);
          showMessageDialog(resourceBundle.getString("launcher.migrateaddins.failure.title"),
                  resourceBundle.formatString("launcher.migrateaddins.failure.message", file, destFile),
                  JOptionPane.ERROR_MESSAGE);
        }
      }

      oldAddinsDir.delete();
    }

    wog.updateInstalledAddins();
  }

  private void showMessageDialog(final String title, final String message, final int messageType)
  {
    try {
      SwingUtilities.invokeAndWait(new Runnable()
      {
        public void run()
        {
          JOptionPane.showMessageDialog(null, message, title, messageType);
        }
      });
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to display message", e);
    }
  }

  private void initProfile()
  {
    ProfileFactory.init();
  }

  private Configuration initModel()
  {
    Configuration c;
    try {
      c = WorldOfGoo.getTheInstance().readConfiguration();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Error reading configuration", e);
      showMessageDialog(resourceBundle.getString("launcher.loadconfig.error.title"),
              resourceBundle.formatString("launcher.loadconfig.error.message", e.getLocalizedMessage()),
              JOptionPane.ERROR_MESSAGE);
      System.exit(2);
      return null;
    }
    return c;
  }

  private MainFrame initControllerAndView(Configuration c)
  {
    controller.setInitialConfiguration(c);

    MainFrame mainFrame = new MainFrame(controller);
    controller.setMainFrame(mainFrame);

    mainFrame.setVisible(true);

    return mainFrame;
  }
}
