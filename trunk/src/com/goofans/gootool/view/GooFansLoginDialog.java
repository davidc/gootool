/*
 * Copyright (c) 2008, 2009, 2010, 2011, 2012 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.GooToolResourceBundle;
import com.goofans.gootool.MainController;
import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.siteapi.LoginTestRequest;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.ProgressIndicatingTask;

public class GooFansLoginDialog extends JDialog implements DocumentListener, ActionListener
{
  private static final Logger log = Logger.getLogger(GooFansLoginDialog.class.getName());

  private static final GooToolResourceBundle resourceBundle = GooTool.getTextProvider();

  public static final String CMD_LOGIN = "GooFansLoginDialog>Login";
  public static final String CMD_CANCEL = "GooFansLoginDialog>Cancel";

  private JPanel contentPane;
  private JButton loginButton;
  private JButton cancelButton;
  private JTextField goofansUsername;
  private JPasswordField goofansPassword;

  public GooFansLoginDialog(MainController mainController)
  {
    setTitle(resourceBundle.getString("gooFansLogin.title"));
    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(loginButton);

    goofansUsername.setText(ToolPreferences.getGooFansUsername());
    goofansPassword.setText(ToolPreferences.getGooFansPassword());

    goofansUsername.getDocument().addDocumentListener(this);
    goofansPassword.getDocument().addDocumentListener(this);

    loginButton.setActionCommand(CMD_LOGIN);
    loginButton.addActionListener(this);
    cancelButton.setActionCommand(CMD_CANCEL);
    cancelButton.addActionListener(this);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    GUIUtil.setDisposeOnEscape(this);

    updateButtonStates();

    pack();
    setLocationRelativeTo(mainController.getMainWindow());
  }

  private void onLogin()
  {
    if (!arePropertiesValid()) return;

    final String username = goofansUsername.getText();
    final String password = new String(goofansPassword.getPassword());

    ProgressIndicatingTask task = new ProgressIndicatingTask()
    {
      @Override
      public void run() throws Exception
      {
        beginStep(resourceBundle.getString("gooFansLogin.testing.message"), false);
        LoginTestRequest request = new LoginTestRequest(username, password);
        request.loginTest();
      }
    };

    try {
      GUIUtil.runTask(this, resourceBundle.getString("gooFansLogin.testing.title"), task);

      ToolPreferences.setGooFansUsername(username);
      ToolPreferences.setGooFansPassword(password);
      ToolPreferences.setGooFansLoginOk(true);

      GUIUtil.showInformationDialog(this, resourceBundle.getString("gooFansLogin.success.title"), resourceBundle.getString("gooFansLogin.success.message"));

      dispose();
    }
    catch (Exception e) {
      log.log(Level.WARNING, "Login test API call failed", e);
      GUIUtil.showErrorDialog(this, resourceBundle.getString("gooFansLogin.error.title"), resourceBundle.formatString("gooFansLogin.error.message", e.getLocalizedMessage()));
    }
  }

  private boolean arePropertiesValid()
  {
    return !goofansUsername.getText().isEmpty() && goofansPassword.getPassword().length > 0;
  }

  private void updateButtonStates()
  {
    loginButton.setEnabled(arePropertiesValid());
  }

  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals(CMD_LOGIN)) {
      onLogin();
    }
    else if (e.getActionCommand().equals(CMD_CANCEL))
    {
      dispose();
    }
  }

  public void insertUpdate(DocumentEvent e)
  {
    updateButtonStates();
  }

  public void removeUpdate(DocumentEvent e)
  {
    updateButtonStates();
  }

  public void changedUpdate(DocumentEvent e)
  {
    updateButtonStates();
  }

  public static void main(String[] args)
  {
    GooTool.initExecutors();

    GooFansLoginDialog dialog = new GooFansLoginDialog(null);
    dialog.setVisible(true);
    System.exit(0);
  }
}
