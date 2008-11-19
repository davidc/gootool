package com.goofans.gootool.versioncheck;

import net.infotrek.util.EncodingUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.URLLauncher;
import com.goofans.gootool.util.Version;
import com.goofans.gootool.util.VersionSpec;

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

  public NewVersionDialog(final Window parentWindow, final VersionSpec latestVersion, String messageText)
  {
    super(parentWindow, "New version available!");
    setContentPane(contentPane);
    getRootPane().setDefaultButton(yesButton);

    setLocationByPlatform(true);
    setIconImage(GooTool.getMainIconImage());
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
        URLLauncher.launchAndWarn("http://goofans.com/gootool/download?from=" + EncodingUtil.urlEncode(Version.RELEASE_FULL), parentWindow);
        setVisible(false);
      }
    });

    curVersionLabel.setText("Your version: " + Version.RELEASE_FULL);
    latestVersionLabel.setText("Latest version: " + latestVersion);
    message.setText("<html>" + messageText + "</html>");

    pack();
  }

  public static void main(String[] args)
  {
    GUIUtil.switchToSystemLookAndFeel();

    NewVersionDialog dialog = new NewVersionDialog(null, new VersionSpec(5, 6, 7), "Please upgrade!");
    dialog.setVisible(true);
    System.exit(0);
  }

  private void createUIComponents()
  {
    icon = new JLabel(GooTool.getMainIcon());
  }
}
