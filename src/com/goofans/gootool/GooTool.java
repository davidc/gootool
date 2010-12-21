/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.Version;

/**
 * Responsible for launching the application, creating the view and mainController, and linking them together.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GooTool
{
  private static final Logger log = Logger.getLogger(GooTool.class.getName());

  private static ImageIcon icon;
  private static GooToolResourceBundle resourceBundle;
  private static MainController mainController;

  private static ExecutorService threadPoolExecutor;
  private static ScheduledExecutorService scheduledExecutor;

  private GooTool()
  {
  }

  public static void main(String[] args)
  {
    log.info("Launching gootool " + Version.RELEASE_FULL);
    log.info("Java version " + System.getProperty("java.version") + " from " + System.getProperty("java.vendor") + " in " + System.getProperty("java.home"));
    log.info("OS " + System.getProperty("os.name") + " version " + System.getProperty("os.version") + " on " + System.getProperty("os.arch"));

    try {
      initQueuedTasks();

      if (!PlatformSupport.preStartup(new ArrayList<String>(Arrays.asList(args)))) {
        return;
      }

      GUIUtil.switchToSystemLookAndFeel();

      initIcon();
      initTextProvider();
      initExecutors();

      mainController = new MainController();

      PlatformSupport.startup(mainController);

      ProgressIndicatingTask startupTask = new StartupTask(mainController);

      GUIUtil.runTask((JFrame) null, resourceBundle.formatString("launcher.title", Version.RELEASE_FRIENDLY), startupTask);
      // In preparation for new splash screen:
//      final ProgressDialog progressDialog = new ProgressDialog(null, resourceBundle.getText("launcher.title", Version.RELEASE_FRIENDLY));
//      startupTask.addListener(progressDialog);
//      GUIUtil.runTask(startupTask, progressDialog);
    }
    catch (Throwable t) {
      log.log(Level.SEVERE, "Uncaught exception", t);
      JOptionPane.showMessageDialog(null, "Uncaught exception (" + t.getClass().getName() + "):\n" + t.getLocalizedMessage(), "GooTool Exception", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
  }

  /**
   * This is public so it can be used by the main() function of test cases.
   */
  public static void initExecutors()
  {
    if (threadPoolExecutor != null) {
      throw new RuntimeException("Executors are already initialised");
    }
    threadPoolExecutor = Executors.newCachedThreadPool();
    scheduledExecutor = Executors.newScheduledThreadPool(1);
  }

  private static List<Runnable> queuedTasks;
  private static boolean startupIsComplete = false;

  public static void initQueuedTasks()
  {
    queuedTasks = new LinkedList<Runnable>();
  }

  /**
   * Queue a task to be run after the startup is complete (or now, if we've already started up).
   * The task is run on the GUI event dispatch thread.
   *
   * @param task the Runnable to be executed after startup
   */
  public static synchronized void queueTask(Runnable task)
  {
    if (startupIsComplete) {
      log.finest("Running task immediately: " + task);
      task.run();
    }
    else {
      log.finest("Queueing task for later: " + task);
      queuedTasks.add(task);
    }
  }

  public static synchronized void startupIsComplete()
  {
    startupIsComplete = true;
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        /* Run all the queued tasks sequentially */
        for (Runnable task : queuedTasks) {
          log.finest("Running queued task: " + task);
          task.run();
        }
        queuedTasks.clear();
      }
    });
  }

  private static void initIcon()
  {
    icon = new ImageIcon(GooTool.class.getResource("/48x48.png"));
    log.fine("icon = " + icon);
  }

  public static synchronized ImageIcon getMainIcon()
  {
    if (icon == null) {
      initIcon();
    }
    return icon;
  }

  public static Image getMainIconImage()
  {
    return getMainIcon().getImage();
  }

  private static synchronized void initTextProvider()
  {
    if (resourceBundle == null) {
      resourceBundle = new GooToolResourceBundle("text");
    }
  }

  public static GooToolResourceBundle getTextProvider()
  {
    if (resourceBundle == null) initTextProvider();
    return resourceBundle;
  }

  public static MainController getController()
  {
    return mainController;
  }

  public static void executeTaskInThreadPool(Runnable task)
  {
    log.log(Level.FINEST, "Executing in thread pool task " + task);
    threadPoolExecutor.execute(task);
  }

  public static void scheduleTask(Runnable task, long delay)
  {
    log.log(Level.FINEST, "Scheduling task " + task + " in " + delay + " msec");
    scheduledExecutor.schedule(task, delay, TimeUnit.MILLISECONDS);
  }

  public static void scheduleTaskWithFixedDelay(Runnable task, long initialDelayMsec, long delayMsec)
  {
    log.log(Level.FINEST, "Scheduling task " + task + " with init delay " + initialDelayMsec + " and recurring delay " + delayMsec);
    scheduledExecutor.scheduleWithFixedDelay(task, initialDelayMsec, delayMsec, TimeUnit.MILLISECONDS);
  }

  public static Preferences getPreferences()
  {
    return Preferences.userNodeForPackage(GooTool.class);
  }
}
