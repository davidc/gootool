package com.goofans.gootool;

import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.view.MainFrame;
import com.goofans.gootool.wog.WorldOfGoo;
import com.goofans.gootool.siteapi.VersionCheck;
import com.goofans.gootool.siteapi.RatingUpdateTask;

import javax.swing.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.MalformedURLException;

/**
 * Sequentially performs GooTool's initialisation tasks before revealing the main UI.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class StartupTask extends ProgressIndicatingTask
{
  private static final Logger log = Logger.getLogger(StartupTask.class.getName());
  private Controller controller;
  private TextProvider textProvider;

  public StartupTask(Controller controller)
  {
    this.controller = controller;
    textProvider = GooTool.getTextProvider();
  }

  public void run() throws Exception
  {
    beginStep(textProvider.getText("launcher.locategoo"), false);
    initWog();

    beginStep(textProvider.getText("launcher.profile"), false);
    initProfile();

    beginStep(textProvider.getText("launcher.loadconfig"), false);
    Configuration c = initModel();

    beginStep(textProvider.getText("launcher.initgui"), false);
    MainFrame mainFrame = initControllerAndView(c);

    // Launch a new thread to check for new version
    try {
      GooTool.executeTaskInThreadPool(new VersionCheck(mainFrame, false));
    }
    catch (MalformedURLException e) {
      log.log(Level.WARNING, "Unable to check version on startup", e);
    }

    // Maybe launch a new thread to download billboards
    BillboardUpdater.maybeUpdateBillboards();

    // Launch thread to retrieve user ratings. Initially in 0.5 seconds, thereafter every 15 minutes.
    GooTool.scheduleTaskWithFixedDelay(new RatingUpdateTask(), 500, 15*60*1000);

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
            String message = textProvider.getText("launcher.locategoo.notfound.message." + PlatformSupport.getPlatform().toString().toLowerCase());
            log.finer("dialog opening");
            JOptionPane.showMessageDialog(null, message, textProvider.getText("launcher.locategoo.notfound.title"), JOptionPane.WARNING_MESSAGE);

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
    JOptionPane.showMessageDialog(null, textProvider.getText("launcher.demo.message"), textProvider.getText("launcher.demo.title"), JOptionPane.WARNING_MESSAGE);
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
      JOptionPane.showMessageDialog(null, textProvider.getText("launcher.loadconfig.error.message", e.getLocalizedMessage()), textProvider.getText("launcher.loadconfig.error.title"), JOptionPane.ERROR_MESSAGE);
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

    mainFrame.pack();
    mainFrame.setVisible(true);

    return mainFrame;
  }
}
