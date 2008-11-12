package com.goofans.gootool.view;

import com.goofans.gootool.util.Version;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.GooTool;

import javax.swing.*;
import java.text.DateFormat;

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

  public AboutDialog(JFrame mainFrame)
  {
    super(mainFrame, "About gootool", true);

    setLocationByPlatform(true);
    setResizable(false);

    setIconImage(GooTool.getMainIconImage());

    setContentPane(rootPanel);

    GUIUtil.setPackOnOpen(this);
    
    GUIUtil.setDefaultClosingOkButton(okButton, this);
    GUIUtil.setCloseOnEscape(this);

//    infoPane.addHyperlinkListener(new HyperlinkLaunchingListener());

    DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    versionField.setText(Version.RELEASE_FULL + " (" + df.format(Version.RELEASE_DATE) + ")");
    df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    buildField.setText(df.format(Version.BUILD_DATE) + " by " + Version.BUILD_USER + " on Java " + Version.BUILD_JAVA);

    pack();
  }
}
