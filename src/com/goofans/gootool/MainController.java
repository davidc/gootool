/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import net.infotrek.util.DesktopUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.addins.AddinFormatException;
import com.goofans.gootool.addins.AddinsStore;
import com.goofans.gootool.l10n.ImageLocalisationDialog;
import com.goofans.gootool.projects.LocalProject;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectConfiguration;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.siteapi.APIException;
import com.goofans.gootool.siteapi.LoginTestRequest;
import com.goofans.gootool.siteapi.VersionCheck;
import com.goofans.gootool.util.FileNameExtensionFilter;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.VersionSpec;
import com.goofans.gootool.view.AboutDialog;
import com.goofans.gootool.view.LocalProjectPropertiesDialog;
import com.goofans.gootool.view.MainWindow;
import com.goofans.gootool.wog.GamePreferences;

import static com.goofans.gootool.GameFileCodecTool.CodecType;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MainController implements ActionListener
{
  private static final Logger log = Logger.getLogger(MainController.class.getName());

  public static final String CMD_EXIT = "Exit";

  public static final String CMD_ABOUT = "Help>About";
  public static final String CMD_GOOTOOL_UPDATE_CHECK = "Help>GooToolUpdateCheck";
  public static final String CMD_DIAGNOSTICS = "Help>Diagnostics";

  public static final String CMD_GOOFANS_LOGIN = "GooFans>Login";

  public static final String CMD_LOCALISATION = "Localisation";
  public static final String CMD_GENERATE_ONLINE_ID = "GenerateOnlineId";
  public static final String CMD_REMOVE_ONLINE_ID = "RemoveOnlineId";

  public static final String CMD_DECRYPT_BIN_PC = "Decrypt>BinPC";
  public static final String CMD_DECRYPT_BIN_MAC = "Decrypt>BinMac";
  public static final String CMD_DECRYPT_PNGBINLTL_MAC = "Decrypt>PngBinLtlMac";
  public static final String CMD_DECRYPT_ANIM = "Decrypt>Anim";
  public static final String CMD_DECRYPT_MOVIE = "Decrypt>Movie";
  public static final String CMD_ENCRYPT_BIN_PC = "Encrypt>BinPC";
  public static final String CMD_ENCRYPT_BIN_MAC = "Encrypt>BinMac";
  public static final String CMD_ENCRYPT_PNGBINLTL_MAC = "Encrypt>PngBinLtlMac";

  public static final String CMD_PROJECT_SELECT = "Project>Select";
  public static final String CMD_PROJECT_ADD = "Project>New";
  public static final String CMD_PROJECT_PROPERTIES = "Project>Properties";
  public static final String CMD_PROJECT_DELETE = "Project>Delete";

  private static final GooToolResourceBundle resourceBundle = GooTool.getTextProvider();

  private MainWindow mainWindow;
  // The codecs
  private final Map<String, GameFileCodecTool> codecs = new HashMap<String, GameFileCodecTool>(8);
  private final ProjectController projectController;
  private ImageLocalisationDialog imageLocalisationPanel;

  public MainController()
  {
    codecs.put(CMD_DECRYPT_BIN_PC, new GameFileCodecTool("bin", "Encrypted Bin File (Windows/Linux)", "xml", CodecType.AES_DECODE));
    codecs.put(CMD_DECRYPT_BIN_MAC, new GameFileCodecTool("bin", "Encrypted Bin File (Mac)", "xml", CodecType.XOR_DECODE));
    codecs.put(CMD_DECRYPT_PNGBINLTL_MAC, new GameFileCodecTool("png.binltl", "Encoded Image File", "png", CodecType.PNGBINLTL_DECODE));
    codecs.put(CMD_DECRYPT_ANIM, new GameFileCodecTool("anim.binltl", "Animation File", "anim.xml", CodecType.ANIM_DECODE));
    codecs.put(CMD_DECRYPT_MOVIE, new GameFileCodecTool("movie.binltl", "Movie File", "movie.xml", CodecType.MOVIE_DECODE));
    codecs.put(CMD_ENCRYPT_BIN_PC, new GameFileCodecTool("xml", "XML Document", "bin", CodecType.AES_ENCODE));
    codecs.put(CMD_ENCRYPT_BIN_MAC, new GameFileCodecTool("xml", "XML Document", "bin", CodecType.XOR_ENCODE));
    codecs.put(CMD_ENCRYPT_PNGBINLTL_MAC, new GameFileCodecTool("png", "PNG Image File", "png.binltl", CodecType.PNGBINLTL_ENCODE));

    projectController = new ProjectController(this);
  }

  /**
   * Invoked when an action occurs.
   */
  public void actionPerformed(ActionEvent event)
  {
    String cmd = event.getActionCommand();

    log.log(Level.FINEST, "MainController action: " + cmd);

    if (cmd.equals(CMD_ABOUT)) {
      openAboutDialog();
    }
    else if (cmd.equals(CMD_EXIT)) {
      maybeExit();
    }
    else if (cmd.equals(CMD_GOOFANS_LOGIN)) {
      gooFansLogin();
    }
    else if (cmd.equals(CMD_LOCALISATION)) {
      openLocalisationDialog();
    }
    else if (cmd.equals(CMD_GOOTOOL_UPDATE_CHECK)) {
      try {
        GUIUtil.runTask(mainWindow, resourceBundle.getString("gootoolUpdateCheck.checking"), new ProgressIndicatingTask()
        {
          @Override
          public void run() throws Exception
          {
            VersionCheck versionCheck = new VersionCheck(mainWindow, true);
            versionCheck.run();
          }
        });
      }
      catch (Exception e) {
        // should never happen.
        showErrorDialog(resourceBundle.getString("gootoolUpdateCheck.error.title"), resourceBundle.formatString("gootoolUpdateCheck.error.message", e.getLocalizedMessage()));
      }
    }
    else if (cmd.equals(CMD_DIAGNOSTICS)) {
      runDiagnostics();
    }
    else if (cmd.equals(CMD_PROJECT_SELECT)) {
      selectProject();
    }
    else if (cmd.equals(CMD_PROJECT_ADD)) {
      addProject();
    }
    else if (cmd.equals(CMD_PROJECT_PROPERTIES)) {
      projectProperties();
    }
    else if (cmd.equals(CMD_PROJECT_DELETE)) {
      deleteProject();
    }
    else if (codecs.containsKey(cmd)) {
      try {
        codecs.get(cmd).runTool(mainWindow);
      }
      catch (Exception e) {
        log.log(Level.SEVERE, "Error coding file", e);
        showErrorDialog("Error coding file", e.getLocalizedMessage());
      }
    }
    else {
      showErrorDialog("MainController", "Unrecognised MainController action " + cmd);
    }
  }


  private void openLocalisationDialog()
  {
    if (imageLocalisationPanel == null) {
      imageLocalisationPanel = new ImageLocalisationDialog(mainWindow);
    }

    imageLocalisationPanel.setVisible(true);
  }

  /**
   * NB NB must be run on the GUI thread! (why?)
   *
   * @param addinFile
   */
  public void installAddin(File addinFile)
  {
    if (!addinFile.exists()) {
      log.info("File not found: " + addinFile);
      showErrorDialog(resourceBundle.getString("installAddin.notfound.title"), resourceBundle.formatString("installAddin.notfound.message", addinFile));
      return;
    }

    log.info("Opening file " + addinFile);
    Addin addin;
    try {
      addin = AddinFactory.loadAddin(addinFile);
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Error opening file " + addinFile, e);
      showErrorDialog(resourceBundle.getString("installAddin.invalid.title"),
              resourceBundle.formatString("installAddin.invalid.message", addinFile.getName(), e.getLocalizedMessage()));
      return;
    }

    // For installation, we only need to check dependencies are satisfied by available addins,
    // not necessarily enabled ones. TODO when enabling, we need to check all dependents are enabled.

//    if (!addin.areDependenciesSatisfiedBy(WorldOfGoo.getAvailableAddins())) {
//      log.info("Not installing because dependencies not satisfied");
//      showErrorDialog("Can't install " + addinFile.getName(), "Can't install this addin because its dependencies aren't satisfied");
//      return;
//    }

    String msg = resourceBundle.formatString("installAddin.confirm.message", addin.getName(), addin.getAuthor(), addin.getVersion());

    if (!showYesNoDialog(resourceBundle.getString("installAddin.confirm.title"), msg)) {
      log.info("User cancelled installation of " + addin);
      return;
    }

    try {
      for (Addin installedAddin : AddinsStore.getAvailableAddins()) {
        if (installedAddin.getId().equals(addin.getId())) {
          msg = resourceBundle.formatString("installAddin.exists.message", installedAddin.getName(), installedAddin.getVersion());

          VersionSpec installedVersion = installedAddin.getVersion();
          VersionSpec newVersion = addin.getVersion();

          if (installedVersion.compareTo(newVersion) < 0) {
            msg += resourceBundle.formatString("installAddin.exists.message.upgrade", addin.getVersion());
          }
          else if (installedVersion.compareTo(newVersion) > 0) {
            msg += resourceBundle.formatString("installAddin.exists.message.downgrade", addin.getVersion());
          }
          else {
            msg += resourceBundle.getString("installAddin.exists.message.sameVersion");
          }

          if (!showYesNoDialog(resourceBundle.getString("installAddin.exists.title"), msg)) {
            log.info("User cancelled overwriting installation of " + addin);
            return;
          }
          AddinsStore.uninstallAddin(installedAddin);
          break;
        }
      }

      AddinsStore.installAddin(addinFile, addin.getId());
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to copy to addins directory", e);
      showErrorDialog(resourceBundle.getString("installAddin.error.title"), resourceBundle.formatString("installAddin.error.message", e.getLocalizedMessage()));
      return;
    }
    catch (AddinFormatException e) {
      log.log(Level.SEVERE, "Addin format exception", e);
      showErrorDialog(resourceBundle.getString("installAddin.error.title"), resourceBundle.formatString("installAddin.error.message", e.getLocalizedMessage()));
      return;
    }

    projectController.refreshView();

    if (projectController.getCurrentProject() != null) {
      projectController.enableAddin(addin.getId());
    }

    if (addin.getType() == Addin.TYPE_LEVEL) {
      msg = resourceBundle.formatString("installAddin.installed.message.level", addin.getName());
    }
    else {
      msg = resourceBundle.formatString("installAddin.installed.mod", addin.getName());
    }

    showMessageDialog(resourceBundle.getString("installAddin.installed.title"), msg);
  }


  // TODO background task this

  private void gooFansLogin()
  {
    projectController.updateModelFromView(); // TODO

    try {
      LoginTestRequest request = new LoginTestRequest();
      request.loginTest();

      showMessageDialog(resourceBundle.getString("gooFansLogin.success.title"), resourceBundle.getString("gooFansLogin.success.message"));
      ToolPreferences.setGooFansLoginOk(true);
    }
    catch (APIException e) {
      log.log(Level.WARNING, "Login test API call failed", e);
      ToolPreferences.setGooFansLoginOk(false);
      showErrorDialog(resourceBundle.getString("gooFansLogin.error.title"), resourceBundle.formatString("gooFansLogin.error.message", e.getLocalizedMessage()));
    }

    projectController.updateViewFromModel();
  }

  void showMessageDialog(String title, String msg)
  {
    JOptionPane.showMessageDialog(mainWindow, msg, title, JOptionPane.INFORMATION_MESSAGE);
  }

  void showErrorDialog(String title, String msg)
  {
    JOptionPane.showMessageDialog(mainWindow, msg, title, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Show a "Yes or No" dialog, returning true only if the user selected "Yes".
   *
   * @param title Title bar
   * @param msg   Message
   * @return True only if the user said "yes", false if they said "No" or closed the window.
   */
  boolean showYesNoDialog(String title, String msg)
  {
    return JOptionPane.showConfirmDialog(mainWindow, msg, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
  }

  public void maybeExit()
  {
    log.fine("Exit requested");

    if (!projectController.canExit()) return;

    log.info("Exiting");
    System.exit(0);
  }

  private void removeUnavailableAddins(ProjectConfiguration config)
  {
    // Remove any addins that are enabled but don't exist
    List<Addin> availableAddins = AddinsStore.getAvailableAddins();
    boolean foundThisAddin = false;
    do {
      for (String enabledAddinName : config.getEnabledAddins()) {
        foundThisAddin = false;
        // see if this addin is available
        for (Addin availableAddin : availableAddins) {
          if (availableAddin.getId().equals(enabledAddinName)) {
            foundThisAddin = true;
            break;
          }
        }
        if (!foundThisAddin) {
          log.log(Level.WARNING, "Removed enabled addin " + enabledAddinName + " as it is not installed");
          config.disableAddin(enabledAddinName);
          break;
        }
      }
    } while (!config.getEnabledAddins().isEmpty() && !foundThisAddin);
  }


  public void openAboutDialog()
  {
    JDialog aboutDialog = new AboutDialog(mainWindow);

    aboutDialog.setVisible(true);
  }

  public void runDiagnostics()
  {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(resourceBundle.getString("diagnostics.chooser.title"));
    chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), "gootool_diagnostics.txt")); //NON-NLS
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Text file", "txt");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showSaveDialog(mainWindow);

    if (returnVal != JFileChooser.APPROVE_OPTION) {
      log.finer("User cancelled diagnostics file chooser");
      return;
    }

    File outFile = chooser.getSelectedFile();
    try {
      Diagnostics diagnostics = new Diagnostics(outFile);
      GUIUtil.runTask(mainWindow, resourceBundle.getString("diagnostics.running.title"), diagnostics);
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Unable to run diagnostics", e);
      showErrorDialog(resourceBundle.getString("diagnostics.error.title"),
              resourceBundle.formatString("diagnostics.error.message", e.getLocalizedMessage()));
      return;
    }

    DesktopUtil.openAndWarn(outFile, mainWindow);
  }


  // Called once the view is initialised

  public void setMainFrame(MainWindow mainWindow)
  {
    this.mainWindow = mainWindow;

    mainWindow.mainPanel.projectPanel.profilePanel.addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent evt)
      {
        if ("allProfilesAreOnline".equals(evt.getPropertyName()) || "anyProfilesHaveGeneratedId".equals(evt.getPropertyName())) { //NON-NLS
          updateGenerateOnlineIdMenu();
        }
      }
    });

    updateGenerateOnlineIdMenu();

    projectController.setMainWindow(mainWindow);

    mainWindow.mainPanel.updateProjectsCombo();

    // TODO load current project number from ToolPreferences

    if (ProjectManager.getProjects().isEmpty()) changeProject(null);
    else
      changeProject(ProjectManager.getProjects().get(0)); // TODO a tool preference for currently selected project
  }

  private void updateGenerateOnlineIdMenu()
  {
    mainWindow.mainMenu.generateIdMenuItem.setEnabled(!mainWindow.mainPanel.projectPanel.profilePanel.areAllProfilesOnline());
    mainWindow.mainMenu.removeIdMenuItem.setEnabled(mainWindow.mainPanel.projectPanel.profilePanel.areAnyProfilesGeneratedId());
  }

  public void bringToForeground()
  {
    log.finest("MainController is requesting focus on MainWindow");
    mainWindow.toFront();
    mainWindow.requestFocus();
  }

  public MainWindow getMainWindow()
  {
    return mainWindow;
  }

  public ProjectController getProjectController()
  {
    return projectController;
  }

  // Called when combo box changes

  private void selectProject()
  {
    System.out.println("Select project called");

    Project newProject = mainWindow.mainPanel.getSelectedProject();

    if (newProject != null && newProject.equals(projectController.getCurrentProject())) return;

    if (!projectController.canChangeProjects()) {
      mainWindow.mainPanel.setSelectedProject(projectController.getCurrentProject());
      return;
    }


    if (newProject != null) {
      projectController.setCurrentProject(newProject);
    }
  }

  // Called when controller wants to change project

  private void changeProject(Project newProject)
  {
    System.out.println("ChangeProject called");

    if (newProject != null && newProject.equals(projectController.getCurrentProject())) return;

    mainWindow.mainPanel.setSelectedProject(newProject);

    projectController.setCurrentProject(newProject);
  }

  private void addProject()
  {
    if (!projectController.canChangeProjects()) return;

    String[] options = new String[]{resourceBundle.getString("project.add.type.option.local"),
            resourceBundle.getString("project.add.type.option.ios")};

    Object option = JOptionPane.showInputDialog(mainWindow, resourceBundle.getString("project.add.type.message"), resourceBundle.getString("project.add.type.title"),
            JOptionPane.QUESTION_MESSAGE, null, options, null);

    if (option == null) return; // cancelled

    if (option == options[0]) {
      LocalProjectPropertiesDialog propsDialog = new LocalProjectPropertiesDialog(mainWindow, resourceBundle.getString("project.local.title.add"), null);

      propsDialog.setVisible(true);

      if (propsDialog.isOkButtonPressed()) {
        LocalProject project = ProjectManager.createLocalProject();
        propsDialog.saveToProject(project);

        // Initialise their preferences from their existing game config.txt file
        // then force a save

        // TODO test this!
        // TODO platform-specific location!
        try {
          GamePreferences.readGamePreferences(project.getConfiguration(), project.getSource());
        }
        catch (IOException e) {
          log.log(Level.WARNING, "Unable to initialise project properties from config.txt", e);
        }

        project.saveConfiguration();


        mainWindow.mainPanel.updateProjectsCombo();

        changeProject(project);
      }
    }
    else {
      showErrorDialog("Error", "This type of project cannot be created yet.");
    }
  }

  private void projectProperties()
  {
    Project project = projectController.getCurrentProject();

    if (project instanceof LocalProject) {
      LocalProjectPropertiesDialog propsDialog = new LocalProjectPropertiesDialog(mainWindow, resourceBundle.getString("project.local.title.properties"), (LocalProject) project);
      propsDialog.setVisible(true);

      if (propsDialog.isOkButtonPressed()) {
        propsDialog.saveToProject((LocalProject) project);
        mainWindow.mainPanel.updateProjectsCombo();
      }
    }
  }

  private void deleteProject()
  {
    Project project = projectController.getCurrentProject();

    if (!showYesNoDialog(resourceBundle.getString("project.delete.confirm.title"), resourceBundle.formatString("project.delete.confirm.message", project.getName()))) {
      return;
    }

    mainWindow.mainPanel.setSelectedProject(null);

    ProjectManager.deleteProject(project);
    mainWindow.mainPanel.updateProjectsCombo();

    // TODO select new project (or none) on the ProjectController
  }
}
