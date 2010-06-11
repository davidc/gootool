/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.util;

import javax.swing.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.view.ProgressDialog;

/**
 * GUI-related utility functions.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GUIUtil
{
  private static final Logger log = Logger.getLogger(GUIUtil.class.getName());

  private GUIUtil()
  {
  }

  /**
   * Adds a keyboard listener to close the dialog when ESCAPE is pressed.
   *
   * @param dialog The dialog to act upon.
   */
  public static void setCloseOnEscape(final JDialog dialog)
  {
    dialog.getRootPane().registerKeyboardAction(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        dialog.setVisible(false);
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
  }

  /**
   * Sets a dialog to pack() when it is opened.
   *
   * @param dialog The dialog to act upon.
   */
  public static void setPackOnOpen(final JDialog dialog)
  {
    dialog.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowOpened(WindowEvent e)
      {
        dialog.pack();
      }
    });
  }

  /**
   * Sets a dialog to request focus on a component when it is opened.
   *
   * @param dialog    The dialog to act upon.
   * @param component The component in the dialog to focus.
   */
  public static void setFocusOnOpen(final JDialog dialog, final JComponent component)
  {
    dialog.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowOpened(WindowEvent e)
      {
        component.requestFocusInWindow();
      }
    });
  }

  /**
   * Sets a button to be the default button of a dialog, and to close the window when clicked.
   *
   * @param okButton The button to act upon.
   * @param dialog   The dialog window containing this button.
   */
  public static void setDefaultClosingOkButton(JButton okButton, final JDialog dialog)
  {
    dialog.getRootPane().setDefaultButton(okButton);

    okButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        dialog.setVisible(false);
      }
    });
  }

  /**
   * Switch to the system's default Swing Look and Feel.
   */
  public static void switchToSystemLookAndFeel()
  {
    String systemLaf = UIManager.getSystemLookAndFeelClassName();
    try {
      UIManager.setLookAndFeel(systemLaf);
      log.log(Level.FINER, "Changed look and feel to " + systemLaf);
    }
    catch (Exception e) {
      log.log(Level.WARNING, "unable to change to look and feel to " + systemLaf, e);
    }
  }

  /**
   * Run a progess-indicating task, opening up a new dialog to display its progress.
   *
   * @param owner       The owner frame for the modal dialog.
   * @param windowTitle The title of the dialog.
   * @param task        The task to run.
   * @throws Exception any exception thrown by the task that ran.
   */
  public static void runTask(JFrame owner, String windowTitle, final ProgressIndicatingTask task) throws Exception
  {
    final ProgressDialog progressDialog = new ProgressDialog(owner, windowTitle);
    task.addListener(progressDialog);
    runTask(task, progressDialog);
  }

  /**
   * Run a progess-indicating task, opening up a new dialog to display its progress.
   *
   * @param owner       The owner dialog for the modal dialog.
   * @param windowTitle The title of the dialog.
   * @param task        The task to run.
   * @throws Exception any exception thrown by the task that ran.
   */
  public static void runTask(JDialog owner, String windowTitle, final ProgressIndicatingTask task) throws Exception
  {
    final ProgressDialog progressDialog = new ProgressDialog(owner, windowTitle);
    task.addListener(progressDialog);
    runTask(task, progressDialog);
  }

  /**
   * Runs a task on the next available thread-pool thread, updating the given progress dialog with its progress.
   *
   * @param task           The task to run.
   * @param progressDialog The progress dialog to update.
   * @throws Exception any exception thrown by the task that ran.
   */
  public static void runTask(final ProgressIndicatingTask task, final JDialog progressDialog) throws Exception
  {
    final Exception[] result = new Exception[]{null};

    GooTool.executeTaskInThreadPool(new Runnable()
    {
      public void run()
      {
        try {
          task.run();
          result[0] = null;
        }
        catch (Exception e) {
          log.log(Level.SEVERE, "runTask " + task.getClass().getName() + " threw exception", e);
          result[0] = e;
        }
        finally {
          SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              progressDialog.setVisible(false);
            }
          });
        }
      }
    });

    progressDialog.setVisible(true); // blocks until dialog is closed by our thread.

    /* Now it has exited */

    if (result[0] != null) {
      throw result[0];
    }
  }
}
