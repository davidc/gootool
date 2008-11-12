package com.goofans.gootool.util;

import javax.swing.*;
import java.awt.event.*;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GUIUtil
{
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
}
