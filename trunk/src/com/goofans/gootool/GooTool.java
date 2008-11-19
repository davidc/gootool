package com.goofans.gootool;

import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.Version;
import com.goofans.gootool.util.GUIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ResourceBundle;

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
  public static TextProvider textProvider;

  private GooTool()
  {
  }

  public static void main(String[] args)
  {
    GUIUtil.switchToSystemLookAndFeel();

    log.info("Launching gootool " + Version.RELEASE_FULL);
    log.info("Java version " + System.getProperty("java.version") + " from " + System.getProperty("java.vendor") + " in " + System.getProperty("java.home"));

    try {
      initIcon();
      initTextProvider();

      Controller controller = new Controller();

      ProgressIndicatingTask startupTask = new StartupTask(controller);

      GUIUtil.runTask(null, textProvider.getText("launcher.title", Version.RELEASE_FULL), startupTask);
    }
    catch (Throwable t) {
      log.log(Level.SEVERE, "Uncaught exception", t);
      JOptionPane.showMessageDialog(null, "Uncaught exception (" + t.getClass().getName() + ") " + t.getLocalizedMessage(), "GooTool Exception", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
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

  private static void initTextProvider()
  {
    textProvider = new TextProvider("text");
  }

  public static TextProvider getTextProvider()
  {
    return textProvider;
  }
}
