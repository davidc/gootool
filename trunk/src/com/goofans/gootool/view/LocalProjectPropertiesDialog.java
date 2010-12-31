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
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.GooToolResourceBundle;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.profile.ProfileData;
import com.goofans.gootool.projects.LocalProject;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.ProfileFileFilter;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.WogExeFileFilter;

/**
 * Properties dialog for local projects.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LocalProjectPropertiesDialog extends JDialog implements ActionListener, ProjectPropertiesDialog
{
  private static final Logger log = Logger.getLogger(LocalProjectPropertiesDialog.class.getName());

  public static final String CMD_CHANGE_SOURCE_DIR = "LocalProject>SourceDir";
  public static final String CMD_CHANGE_TARGET_DIR = "LocalProject>TargetDir";
  public static final String CMD_CHANGE_PROFILE_FILE = "LocalProject>ProfileFile";
  public static final String CMD_OK = "LocalProject>OK";
  public static final String CMD_CANCEL = "LocalProject>Cancel";

  private static final GooToolResourceBundle resourceBundle = GooTool.getTextProvider();

  private JPanel contentPane;

  private JTextField projectNameText;
  private JTextField sourceDirText;
  private JButton changeSourceDirButton;
  private JTextField targetDirText;
  private JButton changeTargetDirButton;
  private JTextField profileFileText;
  private JButton changeProfileFileButton;
  private JButton okButton;
  private JButton cancelButton;

  private final JFrame mainWindow;
  private boolean okButtonPressed = false;

  public LocalProjectPropertiesDialog(JFrame mainWindow, LocalProject project)
  {
    super(mainWindow, true);

    if (project == null) {
      setTitle(resourceBundle.getString("projectProps.local.title.add"));
    }
    else {
      setTitle(resourceBundle.getString("projectProps.local.title.properties"));
    }

    this.mainWindow = mainWindow;

    setContentPane(contentPane);
    getRootPane().setDefaultButton(okButton);

    projectNameText.getDocument().addDocumentListener(new DocumentAdapter()
    {
      @Override
      protected void documentUpdated()
      {
        updateButtonStates();
      }
    });

    changeSourceDirButton.addActionListener(this);
    changeSourceDirButton.setActionCommand(CMD_CHANGE_SOURCE_DIR);

    changeTargetDirButton.addActionListener(this);
    changeTargetDirButton.setActionCommand(CMD_CHANGE_TARGET_DIR);

    changeProfileFileButton.addActionListener(this);
    changeProfileFileButton.setActionCommand(CMD_CHANGE_PROFILE_FILE);

    okButton.addActionListener(this);
    okButton.setActionCommand(CMD_OK);

    cancelButton.addActionListener(this);
    cancelButton.setActionCommand(CMD_CANCEL);

    GUIUtil.setCloseOnEscape(this);

    if (project != null) {
      projectNameText.setText(project.getName());

      sourceDirText.setText(project.getSourceDir());
      targetDirText.setText(project.getTargetDir());
      profileFileText.setText(project.getProfileFile());
    }
    else {
      projectNameText.setText(resourceBundle.getString("project.name.new.local"));

      File sourceDir = PlatformSupport.detectWorldOfGooSource();
      if (sourceDir != null) sourceDirText.setText(sourceDir.getAbsolutePath());

      File profileFile = PlatformSupport.detectProfileFile();
      if (profileFile != null) profileFileText.setText(profileFile.getAbsolutePath());
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

    log.log(Level.FINEST, "LocalProjectPropertiesDialog action: " + cmd);
    if (cmd.equals(CMD_CHANGE_SOURCE_DIR)) {
      changeSourceDir();
    }
    else if (cmd.equals(CMD_CHANGE_TARGET_DIR)) {
      changeTargetDir();
    }
    else if (cmd.equals(CMD_CHANGE_PROFILE_FILE)) {
      changeProfileFile();
    }
    else if (cmd.equals(CMD_OK)) {
      onOK();
    }
    else if (cmd.equals(CMD_CANCEL)) {
      dispose();
    }
  }

  private void changeSourceDir()
  {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new WogExeFileFilter());
    chooser.setDialogTitle(resourceBundle.getString("localProject.sourceDir.chooser.title"));

    if (sourceDirText.getText().length() > 0) {
      chooser.setSelectedFile(new File(sourceDirText.getText()));
    }

    if (chooser.showOpenDialog(mainWindow) != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File selectedFile = chooser.getSelectedFile();
    // TODO check better whether it exists

    log.finer("Source dir chooser selected: " + selectedFile);
    // TODO make sure it's really a source installation!
//
    sourceDirText.setText(chooser.getSelectedFile().getAbsolutePath());

    updateButtonStates();
  }

  private void showErrorDialog(String title, String msg)
  {
    JOptionPane.showMessageDialog(mainWindow, msg, title, JOptionPane.ERROR_MESSAGE);
  }

  private boolean showYesNoDialog(String title, String msg)
  {
    return JOptionPane.showConfirmDialog(mainWindow, msg, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
  }

  private void changeTargetDir()
  {
    File oldFile = null;
    if (targetDirText.getText().length() > 0) {
      oldFile = new File(targetDirText.getText());
    }

    File selectedFile = PlatformSupport.chooseLocalTargetDir(mainWindow, oldFile);

    if (selectedFile == null) {
      return;
    }

    if (!selectedFile.exists()) {
      // Doesn't exist. OK to create?
      if (!showYesNoDialog(resourceBundle.getString("localProject.targetDir.create.title"), resourceBundle.formatString("localProject.targetDir.create.message", selectedFile.getAbsolutePath()))) {
        return;
      }
    }
    else {
      // Check if it's not empty

      if (selectedFile.list().length > 0) {
        if (!showYesNoDialog(resourceBundle.getString("localProject.targetDir.notEmpty.title"), resourceBundle.formatString("localProject.targetDir.notEmpty.message", selectedFile.getAbsolutePath()))) {
          return;
        }
      }
    }

    targetDirText.setText(selectedFile.getAbsolutePath());

    updateButtonStates();
  }

  private void changeProfileFile()
  {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(resourceBundle.getString("localProject.profileFile.chooser.title"));
    chooser.setFileFilter(new ProfileFileFilter());

    if (profileFileText.getText().length() > 0) {
      chooser.setSelectedFile(new File(profileFileText.getText()));
    }

    if (chooser.showOpenDialog(mainWindow) != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File selectedFile = chooser.getSelectedFile();
    if (!ProfileData.isValidProfile(selectedFile)) {
      showErrorDialog(resourceBundle.getString("localProject.profileFile.error.title"), resourceBundle.getString("localProject.profileFile.error.message"));
      return;
    }

    profileFileText.setText(selectedFile.getAbsolutePath());
    updateButtonStates();
  }

  private boolean arePropertiesValid()
  {
    if (projectNameText.getText().length() == 0) return false;

    if (sourceDirText.getText().length() == 0) return false;
    if (targetDirText.getText().length() == 0) return false;

    if (profileFileText.getText().length() == 0) return false;
    File profileFile = new File(profileFileText.getText());
    if (!profileFile.exists()) return false;

    return true;
  }

  private void onOK()
  {
    if (!arePropertiesValid()) return;

    // Final checks

    // TODO make sure target's not in use by any other projects

    // Make sure source directory isn't the same as the target directory and that neither are parents of the other

    File sourceDir = new File(sourceDirText.getText()).getAbsoluteFile();
    File targetDir = new File(targetDirText.getText()).getAbsoluteFile();

    boolean sameDirectory = sourceDir.equals(targetDir);

    if (!sameDirectory) {
      // Make sure target is not parent of source
      File f = sourceDir;
      while ((f = f.getParentFile()) != null) {
        if (f.equals(targetDir)) {
          sameDirectory = true;
          break;
        }
      }
      // Make sure source is not parent of target
      f = targetDir;
      while ((f = f.getParentFile()) != null) {
        if (f.equals(sourceDir)) {
          sameDirectory = true;
          break;
        }
      }
    }

    if (sameDirectory) {
      showErrorDialog(resourceBundle.getString("localProject.targetDir.sameAsSource.title"), resourceBundle.getString("localProject.targetDir.sameAsSource.message"));
      return;
    }

    // Make the target directory

    if (!targetDir.isDirectory()) {
      if (!targetDir.mkdir()) {
        showErrorDialog(resourceBundle.getString("localProject.targetDir.cantCreate.title"), resourceBundle.formatString("localProject.targetDir.cantCreate.message", targetDir.getAbsolutePath()));
        return;
      }
    }

    // TODO test:
    try {
      Utilities.testDirectoryWriteable(targetDir);
    }
    catch (IOException e) {
      showErrorDialog(resourceBundle.getString("localProject.targetDir.notWritable.title"), resourceBundle.formatString("localProject.targetDir.notWritable.message", targetDir.getAbsolutePath(), e.getLocalizedMessage()));
      return;
    }

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
    LocalProject localProject = (LocalProject) project;
    localProject.setName(projectNameText.getText());
    localProject.setSourceDir(sourceDirText.getText());
    localProject.setTargetDir(targetDirText.getText());
    localProject.setProfileFile(profileFileText.getText());
  }


  private void updateButtonStates()
  {
    okButton.setEnabled(arePropertiesValid());
  }


  private abstract static class DocumentAdapter implements DocumentListener
  {
    public void insertUpdate(DocumentEvent e)
    {
      documentUpdated();
    }

    protected abstract void documentUpdated();

    public void removeUpdate(DocumentEvent e)
    {
      documentUpdated();
    }

    public void changedUpdate(DocumentEvent e)
    {
      documentUpdated();
    }
  }

  public static void main(String[] args)
  {
    GUIUtil.switchToSystemLookAndFeel();

    Project p = ProjectManager.simpleInit();

    LocalProjectPropertiesDialog dialog = new LocalProjectPropertiesDialog(null, (LocalProject) p);
    dialog.setVisible(true);
    System.exit(0);
  }

  // TODO Make choosers open on existing location
  // TODO This dialog needs help (particularly about custom dir).
}
