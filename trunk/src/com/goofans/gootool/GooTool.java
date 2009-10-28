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
import java.text.MessageFormat;

import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.Version;

/**
 * Responsible for launching the application, creating the view and controller, and linking them together.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GooTool
{
  private static final Logger log = Logger.getLogger(GooTool.class.getName());

  private static ImageIcon icon;
  private static GooToolResourceBundle resourceBundle;
  private static Controller controller;

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

      controller = new Controller();

      PlatformSupport.startup(controller);

      ProgressIndicatingTask startupTask = new StartupTask(controller);

      GUIUtil.runTask((JFrame) null, MessageFormat.format(resourceBundle.getString("launcher.title"), Version.RELEASE_FRIENDLY), startupTask);
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

  public static Controller getController()
  {
    return controller;
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
}
