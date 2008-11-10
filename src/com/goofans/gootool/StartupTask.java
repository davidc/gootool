package com.goofans.gootool;

import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.view.MainFrame;
import com.goofans.gootool.wog.WorldOfGoo;

import javax.swing.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.InvocationTargetException;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class StartupTask extends ProgressIndicatingTask
{
  private static final Logger log = Logger.getLogger(Controller.class.getName());
  private Controller controller;

  public StartupTask(Controller controller)
  {
    this.controller = controller;
  }

  public void run() throws Exception
  {
    beginStep("Locating World of Goo", false);
    initWog();

    beginStep("Locating profile", false);
    initProfile();

    beginStep("Loading configuration", false);
    Configuration c = initModel();

    beginStep("Initialising GUI", false);
    initControllerAndView(c);
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
            JOptionPane.showMessageDialog(null, "GooTool couldn't automatically find World of Goo. Please locate WorldOfGoo.exe on the next screen", "World of Goo not found", JOptionPane.WARNING_MESSAGE);

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
      JOptionPane.showMessageDialog(null, "Error reading current WoG configuration: " + e.getLocalizedMessage(), "GooTool Error", JOptionPane.ERROR_MESSAGE);
      System.exit(2);
      return null;
    }
    return c;
  }



  private void initControllerAndView(Configuration c)
  {
    controller.setInitialConfiguration(c);

    MainFrame mainFrame = new MainFrame(controller);
    controller.setMainFrame(mainFrame);

    mainFrame.pack();
    mainFrame.setVisible(true);
  }

}
