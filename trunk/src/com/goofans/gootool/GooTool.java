package com.goofans.gootool;

import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.Version;
import com.goofans.gootool.util.GUIUtil;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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

  private GooTool()
  {
  }

  public static void main(String[] args)
  {
    GUIUtil.switchToSystemLookAndFeel();

    log.info("Launching gootool " + Version.RELEASE_FULL);

    try {
      initIcon();

      Controller controller = new Controller();

      ProgressIndicatingTask startupTask = new StartupTask(controller);

      GUIUtil.runTask(null, "Launching GooTool", startupTask);
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
}
