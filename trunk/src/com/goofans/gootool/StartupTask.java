package com.goofans.gootool;

import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.view.MainFrame;
import com.goofans.gootool.wog.WorldOfGoo;
import com.goofans.gootool.versioncheck.VersionCheck;

import javax.swing.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.MalformedURLException;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class StartupTask extends ProgressIndicatingTask
{
  private static final Logger log = Logger.getLogger(Controller.class.getName());
  private Controller controller;
  private TextProvider textProvider;

  public StartupTask(Controller controller)
  {
    this.controller = controller;
    textProvider = GooTool.getTextProvider();
  }

  public void run() throws Exception
  {
    beginStep(textProvider.getText("launcher.locatewog"), false);
    initWog();

    beginStep(textProvider.getText("launcher.profile"), false);
    initProfile();

    beginStep(textProvider.getText("launcher.loadconfig"), false);
    Configuration c = initModel();

    beginStep(textProvider.getText("launcher.initgui"), false);
    MainFrame mainFrame = initControllerAndView(c);

    // Launch a new thread to check for new version
    try {
      new Thread(new VersionCheck(mainFrame, false)).start();
    }
    catch (MalformedURLException e) {
      log.log(Level.WARNING, "Unable to check version on startup", e);
    }
  }

  private void initWog()
  {
    // Locate WoG
    WorldOfGoo.init();

    if (!WorldOfGoo.isWogFound()) {

      // Do it in the UI thread
      try {
        SwingUtilities.invokeAndWait(new Runnable()
        {
          public void run()
          {
            JOptionPane.showMessageDialog(null, textProvider.getText("launcher.locatewog.notfound.message"), textProvider.getText("launcher.locatewog.notfound.title"), JOptionPane.WARNING_MESSAGE);

            while (!WorldOfGoo.isWogFound()) {
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
        throw new RuntimeException("Unexpected exception locating WoG", e);
      }
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
      c = WorldOfGoo.readConfiguration();
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
