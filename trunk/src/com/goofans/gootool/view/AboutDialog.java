/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import net.infotrek.util.TextUtil;

import javax.swing.*;
import java.text.DateFormat;
import java.util.Map;
import java.util.Properties;

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

  private static final GooToolResourceBundle resourceBundle = GooTool.getTextProvider();

  public AboutDialog(JFrame mainFrame)
  {
    super(mainFrame, resourceBundle.getString("about.title"), true);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    setResizable(false);

    setContentPane(rootPanel);

//    GUIUtil.setPackOnOpen(this);

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

    pack();
    setLocationRelativeTo(mainFrame);
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args)
  {
    Properties p = System.getProperties();
    for (Map.Entry<Object, Object> property : p.entrySet()) {
      System.out.println(property.getKey() + " = " + property.getValue());
    }

    GUIUtil.switchToSystemLookAndFeel();
    new AboutDialog(null).setVisible(true);
  }
}
