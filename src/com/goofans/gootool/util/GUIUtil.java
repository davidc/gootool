package com.goofans.gootool.util;

import javax.swing.*;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.view.ProgressDialog;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GUIUtil
{
  private static final Logger log = Logger.getLogger(GUIUtil.class.getName());

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

  public static void setPackOnOpen(final JDialog dialog)
  {
    dialog.addWindowListener(new WindowAdapter()
    {
      public void windowOpened(WindowEvent e)
      {
        dialog.pack();
      }
    });
  }

  public static void setFocusOnOpen(final JDialog dialog, final JComponent component)
  {
    dialog.addWindowListener(new WindowAdapter()
    {
      public void windowOpened(WindowEvent e)
      {
        component.requestFocusInWindow();
      }
    });
  }

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

  public static void runTask(JFrame parent, String windowTitle, final ProgressIndicatingTask task) throws Exception
  {
    final ProgressDialog progressDialog = new ProgressDialog(parent, windowTitle);
    task.addListener(progressDialog);
    runTask(task, progressDialog);
  }

  public static void runTask(final ProgressIndicatingTask task, final JDialog progressDialog) throws Exception
  {
    final Exception[] result = new Exception[]{null};

    Thread thread = new Thread()
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
    };

    thread.start();

    progressDialog.setVisible(true); // blocks

    /* Now it has exited */

    if (result[0] != null) {
      throw result[0];
    }
  }
}
