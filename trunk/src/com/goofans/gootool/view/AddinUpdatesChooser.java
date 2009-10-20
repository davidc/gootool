package com.goofans.gootool.view;

import javax.swing.*;
import java.awt.event.*;

public class AddinUpdatesChooser extends JDialog
{
  private JPanel contentPane;
  private JButton installUpdatesButton;
  private JButton cancelButton;
  private JTable updatesTable;

  private static final String[] COLUMN_NAMES;
  private static final Class[] COLUMN_CLASSES = new Class[]{Boolean.class, String.class, String.class, String.class};

  static {
    // TODO load from resources
    COLUMN_NAMES = new String[]{"", "Addin", "Your Version", "Latest Version"};
  }

  public AddinUpdatesChooser()
  {
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(installUpdatesButton);

    installUpdatesButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onOK();
      }
    });

    cancelButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onCancel();
      }
    });

// call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        onCancel();
      }
    });

// call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        onCancel();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    pack();
  }

  private void onOK()
  {
// add your code here
    dispose();
  }

  private void onCancel()
  {
// add your code here if necessary
    dispose();
  }

  public static void main(String[] args)
  {
    AddinUpdatesChooser dialog = new AddinUpdatesChooser();
    dialog.setVisible(true);
    System.exit(0);
  }
}
