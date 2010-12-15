/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import javax.swing.*;
import java.awt.*;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.util.ProgressListener;

/**
 * Shows a dialog with a progress bar that can be updated to show the step name and (optionally)
 * the step progress percentage. If no percentage is specified, the dialog is given an indeterminate
 * progress bar.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProgressDialog extends JDialog implements ProgressListener
{
  private JLabel task;
  private JProgressBar progressBar;
  private JPanel rootPanel;
  private JLabel iconLabel;


  public ProgressDialog(JFrame owner, String title)
  {
    super(owner, title, true);
    init(owner);
  }

  public ProgressDialog(JDialog owner, String title)
  {
    super(owner, title, true);
    init(owner);
  }

  private void init(Component owner)
  {
    setResizable(false);

    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    // TODO 1.6
//    setIconImage(GooTool.getMainIconImage());
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

    iconLabel.setIcon(GooTool.getMainIcon());

    setSize(300, 100);
    setLocationRelativeTo(owner);
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
