package com.goofans.gootool.view;

import com.goofans.gootool.util.Version;
import com.goofans.gootool.GooTool;

import javax.swing.*;
import java.awt.event.*;
import java.text.DateFormat;

/**
 * TODO ESC to close dialog
 *
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

    okButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        setVisible(false);
      }
    });

    addWindowListener(new WindowAdapter()
    {
      public void windowOpened(WindowEvent e)
      {
        pack();
        okButton.requestFocusInWindow();
      }
    });

    //TODO this doesn't work.
    addKeyListener(new KeyAdapter()
    {
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          setVisible(false);
        }
      }
    });

//    infoPane.addHyperlinkListener(new HyperlinkLaunchingListener());

    // TODO svn revision
    DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    versionField.setText(Version.RELEASE_FULL + " (" + df.format(Version.RELEASE_DATE) + ")");
    df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
    buildField.setText(df.format(Version.BUILD_DATE) + " by " + Version.BUILD_USER + " using " + Version.BUILD_JAVA);

    pack();
  }

}
