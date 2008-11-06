package com.goofans.gootool.view;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.wog.ConfigurationProgressListener;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProgressDialog extends JDialog implements ConfigurationProgressListener
{
  private JLabel task;
  private JProgressBar progressBar;
  private JPanel rootPanel;
  private JLabel iconLabel;


  public ProgressDialog(JFrame mainFrame, String title)
  {
    super(mainFrame, title, true);

//    setLocationByPlatform(true);
    setLocationRelativeTo(mainFrame);

    setResizable(false);

    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    setIconImage(GooTool.getTheInstance().getMainIconImage());
    setContentPane(rootPanel);
//
//    addWindowListener(new WindowAdapter()
//    {
//      public void windowOpened(WindowEvent e)
//      {
//        pack();
//      }
//    });

    task.setText("");

    iconLabel.setIcon(GooTool.getTheInstance().getMainIcon());

    pack();
  }

  public void beginStep(final String taskDescription, final boolean progressAvailable)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        task.setText(taskDescription);
        progressBar.setIndeterminate(!progressAvailable);
        progressBar.setValue(0);
        pack();
      }
    });
  }

  public void progressStep(final float percent)
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        progressBar.setValue((int) percent);
      }
    });
  }
}
