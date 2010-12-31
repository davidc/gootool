/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.GooToolResourceBundle;
import com.goofans.gootool.ios.IosConnection;
import com.goofans.gootool.projects.IosProject;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.GUIUtil;

/**
 * Properties dialog for local projects.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class IosProjectPropertiesDialog extends JDialog implements ActionListener, ProjectPropertiesDialog, DocumentListener
{
  private static final Logger log = Logger.getLogger(IosProjectPropertiesDialog.class.getName());

  public static final String CMD_OK = "IosProject>OK";
  public static final String CMD_CANCEL = "IosProject>Cancel";
  public static final String CMD_TEST_CONNECTION = "IosProject>TestConnection";

  private static final GooToolResourceBundle resourceBundle = GooTool.getTextProvider();
  public static final String REGEX_VALID_IP = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$";

  private JPanel contentPane;

  private JTextField projectNameText;
  private JButton okButton;
  private JButton cancelButton;
  private JButton testConnectionButton;
  private JTextField hostText;
  private JPasswordField passwordText;

  private final JFrame mainWindow;
  private boolean okButtonPressed = false;

  public IosProjectPropertiesDialog(JFrame mainWindow, IosProject project)
  {
    super(mainWindow, true);

    if (project == null) {
      setTitle(resourceBundle.getString("projectProps.ios.title.add"));
    }
    else {
      setTitle(resourceBundle.getString("projectProps.ios.title.properties"));
    }

    this.mainWindow = mainWindow;

    setContentPane(contentPane);
    getRootPane().setDefaultButton(okButton);

    projectNameText.getDocument().addDocumentListener(this);
    hostText.getDocument().addDocumentListener(this);
    passwordText.getDocument().addDocumentListener(this);

    testConnectionButton.addActionListener(this);
    testConnectionButton.setActionCommand(CMD_TEST_CONNECTION);

    okButton.addActionListener(this);
    okButton.setActionCommand(CMD_OK);

    cancelButton.addActionListener(this);
    cancelButton.setActionCommand(CMD_CANCEL);

    GUIUtil.setCloseOnEscape(this);

    if (project != null) {
      projectNameText.setText(project.getName());

      hostText.setText(project.getHost());
      passwordText.setText(project.getPassword());
    }
    else {
      projectNameText.setText(resourceBundle.getString("project.name.new.ios"));

      // TODO autodetect ipad on network if possible?
    }

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    GUIUtil.setFocusOnOpen(this, projectNameText);

    getRootPane().setDefaultButton(okButton);

    updateButtonStates();

    pack();
    setLocationRelativeTo(mainWindow);
  }

  public void actionPerformed(ActionEvent event)
  {
    String cmd = event.getActionCommand();

    log.log(Level.FINEST, "IosProjectPropertiesDialog action: " + cmd);
    if (cmd.equals(CMD_TEST_CONNECTION)) {
      testConnection();
    }
    else if (cmd.equals(CMD_OK)) {
      onOK();
    }
    else if (cmd.equals(CMD_CANCEL)) {
      dispose();
    }
  }

  // TODO this needs to go on a progressindicating task
  private void testConnection()
  {
    IosConnection con = new IosConnection(hostText.getText(), new String(passwordText.getPassword()));

    boolean success;
    try {
      success = con.testConnection();
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(mainWindow, resourceBundle.formatString("projectProps.ios.device.testConnection.exception", e.getMessage()),
              resourceBundle.getString("projectProps.ios.device.testConnection.result"), JOptionPane.WARNING_MESSAGE);
      return;
    }
    finally {
      con.close();
    }

    if (success) {
      JOptionPane.showMessageDialog(mainWindow, resourceBundle.getString("projectProps.ios.device.testConnection.success"),
              resourceBundle.getString("projectProps.ios.device.testConnection.result"), JOptionPane.INFORMATION_MESSAGE);
    }
    else {
      JOptionPane.showMessageDialog(mainWindow, resourceBundle.getString("projectProps.ios.device.testConnection.failure"),
              resourceBundle.getString("projectProps.ios.device.testConnection.result"), JOptionPane.WARNING_MESSAGE);

    }
  }

  private boolean arePropertiesValid()
  {
    if (projectNameText.getText().length() == 0) return false;

    String host = hostText.getText();
    if (host.length() == 0) return false;

    Pattern p = Pattern.compile(REGEX_VALID_IP);
    if (!p.matcher(host).matches()) return false;

//    if (passwordText.getPassword().length() == 0) return false;

    return true;
  }

  private void onOK()
  {
    if (!arePropertiesValid()) return;

    // Final checks

    // TODO make sure this host's not in use by any other projects

    // Make sure source directory isn't the same as the target directory and that neither are parents of the other

    // TODO If we've not done a test connection, do it now.

    // OK save.
    okButtonPressed = true;

    dispose();// TODO we don't want to really dispose here, do we? since it will kill our textfields! seems to work though!
  }

  public boolean isOkButtonPressed()
  {
    return okButtonPressed;
  }

  public void saveToProject(Project project)
  {
    IosProject iosProject = (IosProject) project;
    iosProject.setName(projectNameText.getText());
    iosProject.setHost(hostText.getText());
    iosProject.setPassword(new String(passwordText.getPassword()));
  }

  private void updateButtonStates()
  {
    okButton.setEnabled(arePropertiesValid());
    testConnectionButton.setEnabled(arePropertiesValid());
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
    DebugUtil.setAllLogging();

    GUIUtil.switchToSystemLookAndFeel();

    IosProjectPropertiesDialog dialog = new IosProjectPropertiesDialog(null, null);
    dialog.setVisible(true);
  }
}
