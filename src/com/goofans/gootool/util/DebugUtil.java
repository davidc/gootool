package com.goofans.gootool.util;

import javax.swing.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.image.BufferedImage;
import java.awt.*;

/**
 * Utilities for use only in test cases (psvm etc)
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class DebugUtil
{
  public static void setAllLogging()
  {
//    Logger.getLogger("").setLevel(Level.ALL);
    Logger.getLogger("com.goofans").setLevel(Level.ALL);
    Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
  }

  public static void showImageWindow(BufferedImage image)
  {
    JDialog dlg = new JDialog((Frame) null, "Image Debug", true);
    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    JLabel imgLabel = new JLabel(new ImageIcon(image));
    Dimension d = new Dimension(image.getWidth(null), image.getHeight(null));
    imgLabel.setPreferredSize(d);
    dlg.getContentPane().add(imgLabel);
    dlg.pack();
    GUIUtil.setCloseOnEscape(dlg);
    dlg.setVisible(true);
  }

  public static void showPanelWindow(JPanel panel)
  {
    JDialog dlg = new JDialog((Frame) null, "Panel Debug", true);
    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dlg.getContentPane().add(panel);
    dlg.pack();
    dlg.setVisible(true);
  }
}
