/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import com.goofans.gootool.addins.AddinsStore;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.siteapi.RatingUpdateTask;
import com.goofans.gootool.siteapi.VersionCheck;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.Version;
import com.goofans.gootool.util.VersionSpec;
import com.goofans.gootool.view.MainWindow;

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
  private final MainController mainController;
  private final GooToolResourceBundle resourceBundle;

  private static final String PREF_LASTVERSION = "gootool_version";

  // Time delay (in msec) after startup to perform various tasks

  private static final int RATINGUPDATE_INITIAL_DELAY = 500; // Initial ratings update (0.5 seconds)
  private static final int RATINGUPDATE_RECURRING_DELAY = 15 * 60 * 1000; // Recurring ratings update (15 minutes)

  private static final int VERSIONCHECK_DELAY = 2000; // Version check delay (2 seconds)

  public StartupTask(MainController mainController)
  {
    this.mainController = mainController;
    resourceBundle = GooTool.getTextProvider();
  }

  @Override
  public void run() throws Exception
  {
    // Init the addins dir first, in case upgradeVersion needs it
    beginStep(resourceBundle.getString("launcher.initAddinsDir"), false);
    initAddinsDir();

    upgradeVersion();

//    beginStep(resourceBundle.getString("launcher.locategoo"), false);
//    initWog();

    beginStep(resourceBundle.getString("launcher.initAddins"), false);
    initAddins();

//  TODO
//    beginStep(resourceBundle.getString("launcher.profile"), false);
//    initProfile();

//    beginStep(resourceBundle.getString("launcher.loadconfig"), false);
//    LocalProjectConfiguration c = initModel();

    beginStep(resourceBundle.getString("launcher.initGui"), false);
    MainWindow mainWindow = initControllerAndView();

    // TODO uncomment and test
//    List<Project> projects = ProjectManager.getProjects();
//    if (projects.isEmpty()) {
//      mainController.newProject();
//      if (projects.isEmpty()) {
//        showMessageDialog(resourceBundle.getString("launcher.noproject.title"), resourceBundle.getString("launcher.noproject.message"), JOptionPane.ERROR_MESSAGE);
//        System.exit(0);
//      }
//    }

    // Startup complete, final tasks:

    // Schedule a check for new version in 2 seconds
    GooTool.scheduleTask(new VersionCheck(mainWindow, false), VERSIONCHECK_DELAY);

    // Maybe launch a new thread to download billboards
    BillboardUpdater.maybeUpdateBillboards();

    // Launch thread to retrieve user ratings. Initially in 0.5 seconds, thereafter every 15 minutes.
    GooTool.scheduleTaskWithFixedDelay(new RatingUpdateTask(), RATINGUPDATE_INITIAL_DELAY, RATINGUPDATE_RECURRING_DELAY);

    GooTool.getPreferences().put(PREF_LASTVERSION, Version.RELEASE.toString());

    GooTool.startupIsComplete();
  }

  private void upgradeVersion()
  {
    String versionStr = GooTool.getPreferences().get(PREF_LASTVERSION, null);
    if (versionStr == null) return;

    VersionSpec lastVersion = new VersionSpec(versionStr);

    if (lastVersion.compareTo(Version.RELEASE) >= 0) {
      return;
    }

    beginStep(resourceBundle.getString("launcher.upgrade"), false);

    if (lastVersion.compareTo(new VersionSpec(1, 0, 3)) < 0) {
      // Migrate addins from old <customdir>/addins
      // At this point, we haven't upgraded to 1.1.0, so custom_dir is still on the GooTool preferences node


      migrateAddins1_0_3();


    }

    if (lastVersion.compareTo(new VersionSpec(1, 1, 0)) < 0) {
      // Migrate old single-project preferences to new project 0

      int r = JOptionPane.showConfirmDialog(parentComponent, resourceBundle.getString("launcher.upgrade.1.1.0.message"), resourceBundle.getString("launcher.upgrade.1.1.0.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

      if (r != JOptionPane.OK_OPTION) {
        log.log(Level.SEVERE, "User refused to migrate to 1.1.0");
        showMessageDialog(resourceBundle.getString("launcher.upgrade.1.1.0.title"), resourceBundle.getString("launcher.upgrade.1.1.0.cancelled"), JOptionPane.WARNING_MESSAGE);
        System.exit(1);
      }

      ProjectManager.migratePreferences1_1_0();
    }
  }

  private void migrateAddins1_0_3()
  {
    Preferences oldPrefs = GooTool.getPreferences();
    String oldCustomDir = oldPrefs.get("custom_dir", null);
    if (oldCustomDir == null || oldCustomDir.length() == 0) return;

    File oldAddinsDir = null;
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
      case LINUX:
        oldAddinsDir = new File(oldCustomDir, "addins");
        break;
      case MACOSX:
        oldAddinsDir = new File(oldCustomDir, "Contents/Resources/addins");
        break;
    }

    if (oldAddinsDir != null && oldAddinsDir.exists()) {
      File addinsDir = AddinsStore.getAddinsDir();

      showMessageDialog(resourceBundle.getString("launcher.upgrade.1.0.3.migrateAddins.title"),
              resourceBundle.formatString("launcher.upgrade.1.0.3.migrateAddins.message", oldAddinsDir, addinsDir),
              JOptionPane.INFORMATION_MESSAGE);

      for (File file : oldAddinsDir.listFiles()) {
        File destFile = new File(addinsDir, file.getName());
        try {
          Utilities.deleteFileIfExists(destFile);
          Utilities.moveFile(file, destFile);
        }
        catch (IOException e) {
          log.log(Level.WARNING, "Unable to move " + file + " to " + destFile, e);
          showMessageDialog(resourceBundle.getString("launcher.upgrade.1.0.3.migrateAddins.failure.title"),
                  resourceBundle.formatString("launcher.upgrade.1.0.3.migrateAddins.failure.message", file, destFile),
                  JOptionPane.ERROR_MESSAGE);
        }
      }

      if (!oldAddinsDir.delete()) {
        log.log(Level.WARNING, "Unable to delete old addins directory " + oldAddinsDir);
      }
    }
  }

  /* private void initWog()
  {
    // Locate WoG
    final WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    worldOfGoo.init();

    if (!worldOfGoo.isWogFound()) {
      String message = resourceBundle.getString("launcher.locategoo.notfound.message." + PlatformSupport.getPlatform().toString().toLowerCase());
      log.finer("dialog opening");
      showMessageDialog(resourceBundle.getString("launcher.locategoo.notfound.title"), message, JOptionPane.WARNING_MESSAGE);

      log.finer("dialog closed");
      while (!worldOfGoo.isWogFound()) {
        int result = mainController.askToLocateWog();
        if (result == -2) {
          log.info("User refused to locate WorldOfGoo.exe, exiting");
          System.exit(2);
        }
      }
    }

    warnIfDemo(worldOfGoo);
  }*/

  /**
   * Initialise the addins directory. Called early in the startup sequence in case upgrade tasks need it.
   */
  private void initAddinsDir()
  {
    try {
      AddinsStore.initAddinsDir();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to create addins directory", e);
      showMessageDialog(resourceBundle.getString("launcher.initAddins.cantCreate.title"),
              resourceBundle.formatString("launcher.initAddins.cantCreate.message", e.getLocalizedMessage()),
              JOptionPane.ERROR_MESSAGE);
    }
  }

  private void initAddins()
  {
    AddinsStore.updateAvailableAddins();
  }

  private void showMessageDialog(final String title, final String message, final int messageType)
  {
    try {
      SwingUtilities.invokeAndWait(new Runnable()
      {
        public void run()
        {
          JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
        }
      });
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to display message", e);
    }
  }
/*
  private void initProfile()
  {
    ProfileFactory.init();
  }

  private LocalProjectConfiguration initModel()
  {
    LocalProjectConfiguration c;
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
  }*/

  private MainWindow initControllerAndView()
  {
//    ProjectManager.getProjects().get(0).getConfiguration();
//    mainController.setInitialConfiguration(c); //TODO

    MainWindow mainWindow = new MainWindow(mainController);
    mainController.setMainFrame(mainWindow);

//    projectPanel.setVisible(true);
    mainWindow.setVisible(true);

    return mainWindow;
  }

  @Override
  public String toString()
  {
    return "StartupTask";
  }
}
