/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.GooToolResourceBundle;
import com.goofans.gootool.util.GUIUtil;
import net.infotrek.util.DesktopUtil;
import com.goofans.gootool.util.Version;
import com.goofans.gootool.util.VersionSpec;

/**
 * Popup to inform user of a new version of GooTool.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class NewVersionDialog extends JDialog
{
  private JPanel contentPane;
  private JButton yesButton;
  private JButton noButton;
  private JLabel icon;
  private JCheckBox ignoreThisVersionCheckBox;
  private JLabel message;
  private JLabel curVersionLabel;
  private JLabel latestVersionLabel;

  private static final GooToolResourceBundle resourceBundle = GooTool.getTextProvider();

  public NewVersionDialog(final Frame parentWindow, final VersionSpec latestVersion, String messageText, final String downloadUrl)
  {
    super(parentWindow, resourceBundle.getString("newVersion.title"));
    setContentPane(contentPane);
    getRootPane().setDefaultButton(yesButton);

    setModal(true);
    setResizable(false);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    GUIUtil.setCloseOnEscape(this);
    GUIUtil.setFocusOnOpen(this, yesButton);

    noButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (ignoreThisVersionCheckBox.isSelected()) {
          ToolPreferences.setIgnoreUpdate(latestVersion);
        }
        setVisible(false);
      }
    });

    yesButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        DesktopUtil.browseAndWarn(downloadUrl, parentWindow);
        setVisible(false);
      }
    });

    curVersionLabel.setText(resourceBundle.formatString("newVersion.curVersion", Version.RELEASE_MAJOR, Version.RELEASE_MINOR, Version.RELEASE_MICRO));
    latestVersionLabel.setText(resourceBundle.formatString("newVersion.latestVersion", latestVersion));
    message.setText("<html>" + messageText + "</html>"); //NON-NLS

    pack();
    setLocationRelativeTo(parentWindow);
  }

  private void createUIComponents()
  {
    icon = new JLabel(GooTool.getMainIcon());
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public static void main(String[] args)
  {
    GUIUtil.switchToSystemLookAndFeel();

    NewVersionDialog dialog = new NewVersionDialog(null, new VersionSpec(5, 6, 7), "Please upgrade!", "http://goofans.com/blah");
    dialog.setVisible(true);
    System.exit(0);
  }
}
