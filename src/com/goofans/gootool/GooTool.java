package com.goofans.gootool;

import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.util.Version;
import com.goofans.gootool.util.WogExeFileFilter;
import com.goofans.gootool.view.MainFrame;
import com.goofans.gootool.wog.WorldOfGoo;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.*;

/**
 * Responsible for launching the application, creating the view and controller, and linking them together.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GooTool
{
  private static final Logger log = Logger.getLogger(GooTool.class.getName());

  private static GooTool theInstance;

  private MainFrame mainFrame;

  private Controller controller;
  private final ImageIcon icon;

  private GooTool()
  {
    theInstance = this;
    controller = new Controller();

    // Locate WoG
    WorldOfGoo.init();

    if (!WorldOfGoo.isWogFound()) {
      JOptionPane.showMessageDialog(null, "GooTool couldn't automatically find World of Goo. Please locate WorldOfGoo.exe on the next screen", "World of Goo not found", JOptionPane.WARNING_MESSAGE);

      while (!WorldOfGoo.isWogFound()) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new WogExeFileFilter());

        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
          log.info("User refused to locate WorldOfGoo.exe, exiting");
          // TODO temp for lang
//          System.exit(2);
          break;
        }

        File selectedFile = chooser.getSelectedFile();
        try {
          WorldOfGoo.init(selectedFile.getParentFile());
        }
        catch (FileNotFoundException e) {
          log.info("WoG not found at " + selectedFile + " (" + selectedFile.getParentFile() + ")");

          JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "File not found", JOptionPane.ERROR_MESSAGE);
        }
      }
    }


    icon = new ImageIcon(getClass().getResource("/48x48.png"));
    log.fine("icon = " + icon);

    Configuration c;
    try {
      c = WorldOfGoo.readConfiguration();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Error reading configuration", e);
      //TODO temp for lang
      JOptionPane.showMessageDialog(null, "Error reading current WoG configuration. Loading defaults.", "GooTool Error", JOptionPane.ERROR_MESSAGE);
      c = new Configuration();

      // Restore this:
//      JOptionPane.showMessageDialog(null, "Error reading current WoG configuration. Bailing out.", "GooTool Error", JOptionPane.ERROR_MESSAGE);
//      System.exit(2);
    }
    controller.setInitialConfiguration(c);

    mainFrame = new MainFrame(controller);
    controller.setMainFrame(mainFrame);

    mainFrame.pack();
    mainFrame.setVisible(true);
  }

  public static void main(String[] args)
  {
//    try {
//      initLogger();
//    }
//    catch (IOException e) {
//      JOptionPane.showMessageDialog(null, "ERROR: Couldn't open our logfile: " + e.getLocalizedMessage(), "Error opening gootool", JOptionPane.ERROR_MESSAGE);
//      e.printStackTrace();
//      System.exit(1);
//    }

    String systemLaf = UIManager.getSystemLookAndFeelClassName();
    try {
      UIManager.setLookAndFeel(systemLaf);
      log.log(Level.FINER, "Changed look and feel to " + systemLaf);
    }
    catch (Exception e) {
      log.log(Level.WARNING, "unable to change to look and feel to " + systemLaf, e);
    }

    log.info("Launching gootool " + Version.RELEASE_FULL);

    try {
      new GooTool();
    }
    catch (Throwable t) {
      log.log(Level.SEVERE, "Uncaught exception", t);
      JOptionPane.showMessageDialog(null, "Uncaught exception (" + t.getClass().getName() + ") " + t.getLocalizedMessage(), "GooTool Exception", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
  }

  public static GooTool getTheInstance()
  {
    return theInstance;
  }

  public ImageIcon getMainIcon()
  {
    return icon;
  }

  public Image getMainIconImage()
  {
    return icon.getImage();
  }
}
