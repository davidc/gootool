/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import net.infotrek.util.TextUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.DateFormat;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.GooToolResourceBundle;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.Version;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AboutDialog extends JDialog
{
  private static final GooToolResourceBundle resourceBundle = GooTool.getTextProvider();

  private JPanel rootPanel;
  private JLabel infoPane;
  private JLabel versionField;
  private JButton okButton;
  private JLabel buildField;
  private JLabel javaVersion;
  private JLabel javaVendor;
  private JLabel javaHome;
  private JLabel vmType;
  private JLabel vmMemory;
  private JLabel copyrightLabel;

  public AboutDialog(JFrame mainFrame)
  {
    super(mainFrame, resourceBundle.getString("about.title"), true);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    setResizable(false);

    setContentPane(rootPanel);

    GUIUtil.setDefaultClosingOkButton(okButton, this);
    GUIUtil.setCloseOnEscape(this);

//    infoPane.addHyperlinkListener(new HyperlinkLaunchingListener());

    DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    versionField.setText(resourceBundle.formatString("about.version.value", Version.RELEASE_FULL, df.format(Version.RELEASE_DATE)));
    df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    buildField.setText(resourceBundle.formatString("about.build.value", df.format(Version.BUILD_DATE), Version.BUILD_USER, Version.BUILD_JAVA));

    javaVersion.setText(System.getProperty("java.version"));
    javaVendor.setText(System.getProperty("java.vendor"));
    javaHome.setText(System.getProperty("java.home"));
    vmType.setText(System.getProperty("java.vm.name"));

    long totalMem = Runtime.getRuntime().totalMemory();
    long usedMem = totalMem - Runtime.getRuntime().freeMemory();

    vmMemory.setText(resourceBundle.formatString("about.vmMemory.value", TextUtil.binaryNumToString(usedMem), TextUtil.binaryNumToString(totalMem)));

    String text = copyrightLabel.getText();

    pack();
    setLocationRelativeTo(mainFrame);

    final BufferedImage eggImage;

    try {
      eggImage = ImageIO.read(getClass().getResourceAsStream("/mc.jpg"));

      int underlinedIndex = text.indexOf("David"); //NON-NLS
      FontMetrics fm = copyrightLabel.getFontMetrics(copyrightLabel.getFont());
      final Rectangle hitbox = new Rectangle(fm.stringWidth(text.substring(0, underlinedIndex)), 0,
              fm.charWidth(text.charAt(underlinedIndex)), fm.getHeight());

      copyrightLabel.addMouseMotionListener(new MouseMotionAdapter()
      {
        @Override
        public void mouseMoved(MouseEvent e)
        {
          if (hitbox.contains(e.getX(), e.getY())) getGlassPane().setVisible(true);
          else getGlassPane().setVisible(false);
        }
      });
      copyrightLabel.addMouseListener(new MouseInputAdapter()
      {
        @Override
        public void mouseExited(MouseEvent e)
        {
          getGlassPane().setVisible(false);
        }
      });

      setGlassPane(new JComponent()
      {
        @Override
        protected void paintComponent(Graphics g)
        {
          int centreX = getWidth() / 2;
          int centreY = getHeight() / 2;

          int x = centreX - (eggImage.getWidth() / 2);
          int y = centreY - (eggImage.getHeight() / 2);
          g.drawImage(eggImage, x, y, null);
        }
      });
    }
    catch (IOException e) {
      // Silent fail
    }
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args)
  {
    GUIUtil.switchToSystemLookAndFeel();
    new AboutDialog(null).setVisible(true);
  }
}
