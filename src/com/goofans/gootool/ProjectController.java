/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import net.infotrek.util.DesktopUtil;
import net.infotrek.util.TextUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinsStore;
import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.facades.TargetFile;
import com.goofans.gootool.model.ProjectModel;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.profile.GenerateOnlineIds;
import com.goofans.gootool.projects.LocalProject;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectConfiguration;
import com.goofans.gootool.siteapi.*;
import com.goofans.gootool.util.FileNameExtensionFilter;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.view.AddinPropertiesDialog;
import com.goofans.gootool.view.AddinUpdatesChooser;
import com.goofans.gootool.view.MainWindow;
import com.goofans.gootool.view.ProjectPanel;
import com.goofans.gootool.wog.WorldBuilder;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProjectController implements ActionListener
{
  private static final Logger log = Logger.getLogger(ProjectController.class.getName());

  public static final String CMD_ADDIN_INSTALL = "Addin>Install";
  public static final String CMD_ADDIN_UPDATECHECK = "Addin>UpdateCheck";
  public static final String CMD_ADDIN_PROPERTIES = "Addin>Properties";
  public static final String CMD_ADDIN_UNINSTALL = "Addin>Uninstall";
  public static final String CMD_ADDIN_ENABLE = "Addin>Enable";
  public static final String CMD_ADDIN_DISABLE = "Addin>Disable";

  public static final String CMD_GOOFANS_BACKUP = "GooFans>Backup";
  public static final String CMD_GOOFANS_RESTORE = "GooFans>Restore";
  public static final String CMD_GOOFANS_PUBLISH = "GooFans>Publish";

  public static final String CMD_SAVE = "Save";
  public static final String CMD_SAVE_AND_LAUNCH = "Save&Launch";
  public static final String CMD_REVERT = "Revert";

  public static final String CMD_GENERATE_ONLINE_ID = "GenerateOnlineId";
  public static final String CMD_REMOVE_ONLINE_ID = "RemoveOnlineId"; // TODO move these two to a button

  private static final GooToolResourceBundle resourceBundle = GooTool.getTextProvider();

  // The configuration currently live on disk.
  private ProjectConfiguration liveConfig;

  private ProjectModel projectModel;

  private final MainController mainController;

  private Project currentProject;
  private ProjectPanel projectPanel;

  public ProjectController(MainController mainController)
  {
    this.mainController = mainController;
  }

  public void setMainWindow(MainWindow w)
  {
    projectPanel = mainController.getMainWindow().mainPanel.projectPanel;
    projectPanel.initController(this);
  }

  public void setCurrentProject(Project p)
  {
    this.currentProject = p;

    System.out.println("Project Controller Setting current project " + p);

    if (p == null) {
      projectModel = null;
    }
    else {
      loadProjectModel();

      warnIfDemo();
    }
    projectPanel.profilePanel.projectChanged(projectModel);

    updateViewFromModel();
  }

  private void warnIfDemo()
  {
    // A file that will not exist in the demo version.
    SourceFile flagFile = currentProject.getSource().getGameRoot().getChild(currentProject.getGameXmlFilename("res/levels/island3/island3.level"));

    if (flagFile == null || !flagFile.isFile()) {
      String[] options = new String[]{resourceBundle.getString("project.demoWarning.upgrade"),
              resourceBundle.getString("project.demoWarning.proceed")};

      int r = JOptionPane.showOptionDialog(mainController.getMainWindow(),
              resourceBundle.getString("project.demoWarning.message"), resourceBundle.getString("project.demoWarning.title"),
              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);

      if (r == JOptionPane.YES_OPTION) {
        DesktopUtil.browseAndWarn(resourceBundle.getString("url.demoUpgrade"), mainController.getMainWindow());
      }
    }
  }


  public void closeProject()
  {
    this.currentProject = null;
    projectModel = null;

    updateViewFromModel();
  }

  private void loadProjectModel()
  {
    // TODO maybe put this in a progressindicatingtask?

    liveConfig = currentProject.getSavedConfiguration(); // TODO don't cast, use PC not LPC
    projectModel = new ProjectModel((ProjectConfiguration) liveConfig.clone());
  }

  /**
   * Invoked when an action occurs.
   */
  public void actionPerformed(ActionEvent event)
  {
    if (currentProject == null) {
      log.log(Level.SEVERE, "Action " + event.getActionCommand() + " requested while no project loaded");
      mainController.showErrorDialog("Invalid state", "can't perform action " + event.getActionCommand() + " with no project loaded!");
      return;
    }

    String cmd = event.getActionCommand();

    log.log(Level.FINEST, "ProjectController action: " + cmd);

    if (cmd.equals(CMD_SAVE)) {
      save(false);
    }
    else if (cmd.equals(CMD_SAVE_AND_LAUNCH)) {
      save(true);
    }
    else if (cmd.equals(CMD_REVERT)) {
      log.info("Reverting configuration to saved");
      projectModel = new ProjectModel((ProjectConfiguration) liveConfig.clone());
      updateViewFromModel();
    }
    else if (cmd.equals(CMD_ADDIN_INSTALL)) {
      installAddin();
    }
    else if (cmd.equals(CMD_ADDIN_UPDATECHECK)) {
      addinUpdateCheck();
    }
    else if (cmd.equals(CMD_ADDIN_ENABLE)) {
      enableAddin();
    }
    else if (cmd.equals(CMD_ADDIN_DISABLE)) {
      disableAddin();
    }
    else if (cmd.equals(CMD_ADDIN_PROPERTIES)) {
      new AddinPropertiesDialog(mainController.getMainWindow(), projectPanel.addinsPanel.getSelectedAddin()).setVisible(true);
//      Addin addin = getSelectedAddin();
//      showMessageDialog("Addin details: " + addin);
    }
    else if (cmd.equals(CMD_ADDIN_UNINSTALL)) {
      uninstallAddin();
    }
    else if (cmd.equals(CMD_GOOFANS_BACKUP)) {
      gooFansBackup();
    }
    else if (cmd.equals(CMD_GOOFANS_RESTORE)) {
      gooFansRestore();
    }
    else if (cmd.equals(CMD_GOOFANS_PUBLISH)) {
      gooFansPublish();
    }
    else if (cmd.equals(CMD_GENERATE_ONLINE_ID)) {
      generateOnlineId();
    }
    else if (cmd.equals(CMD_REMOVE_ONLINE_ID)) {
      removeOnlineId();
    }
    else {
      mainController.showErrorDialog("ProjectController", "Unrecognised ProjectController action " + cmd);
    }
  }

  private void addinUpdateCheck()
  {
    final Map<String, AddinUpdatesCheckRequest.AvailableUpdate>[] updates = new Map[1];

    try {
      GUIUtil.runTask(mainController.getMainWindow(), resourceBundle.getString("addinUpdateCheck.checking.title"), new ProgressIndicatingTask()
      {
        @Override
        public void run() throws Exception
        {
          beginStep(resourceBundle.getString("addinUpdateCheck.checking.message"), false);
          AddinUpdatesCheckRequest checkRequest = new AddinUpdatesCheckRequest();

          updates[0] = checkRequest.checkUpdates();
        }
      });
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Error checking for updates", e);
      mainController.showErrorDialog(resourceBundle.getString("addinUpdateCheck.error.title"), resourceBundle.formatString("addinUpdateCheck.error.message", e.getLocalizedMessage()));
      return;
    }

    AddinUpdatesChooser dialog = new AddinUpdatesChooser(mainController.getMainWindow(), updates[0]);
    dialog.setVisible(true);

    // Refresh the addins table
    refreshView();
  }

  private void installAddin()
  {
    JFileChooser chooser = new JFileChooser(ToolPreferences.getMruAddinDir());
    chooser.setMultiSelectionEnabled(true);
    FileNameExtensionFilter filter = new FileNameExtensionFilter("World of Goo Mods", AddinsStore.GOOMOD_EXTENSION);
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(mainController.getMainWindow());

    if (returnVal != JFileChooser.APPROVE_OPTION) {
      log.finer("User cancelled chooser");
      return;
    }

    ToolPreferences.setMruAddinDir(chooser.getCurrentDirectory().getPath());

    File[] selectedFiles = chooser.getSelectedFiles();

    for (File addinFile : selectedFiles) {
      mainController.installAddin(addinFile);
    }
  }

  private void uninstallAddin()
  {
    Addin addin = projectPanel.addinsPanel.getSelectedAddin();
    log.info("Uninstall " + addin);

    try {
      AddinsStore.uninstallAddin(addin);
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to uninstall addin", e);
      mainController.showErrorDialog(resourceBundle.getString("uninstallAddin.error.title"), resourceBundle.formatString("uninstallAddin.error.message", e.getLocalizedMessage()));
      return;
    }

    mainController.showMessageDialog(resourceBundle.getString("uninstallAddin.uninstalled.title"), resourceBundle.formatString("uninstallAddin.uninstalled.message", addin.getName()));

    disableAddin(addin.getId());
    refreshView();
  }

  private void enableAddin()
  {
    Addin addin = projectPanel.addinsPanel.getSelectedAddin();
    enableAddinInternal(addin);
  }

  public void enableAddin(String id)
  {
    System.out.println("id = " + id);
    Addin addin = AddinsStore.getAddinById(id);
    enableAddinInternal(addin);
  }

  private void enableAddinInternal(Addin addin)
  {
    // TODO better checking of satisfaction here (use editor config, i.e. what's enabled)

    if (!addin.areDependenciesSatisfiedBy(AddinsStore.getAvailableAddins())) {
      log.info("Not installing because dependencies not satisfied");
      mainController.showErrorDialog(resourceBundle.getString("enableAddin.dependencies.title"), resourceBundle.formatString("enableAddin.dependencies.message", addin.getName()));
      return;
    }

    projectModel.enableAddin(addin.getId());
    projectPanel.addinsPanel.refreshAddinsTable();
  }

  private void disableAddin()
  {
    projectModel.disableAddin(projectPanel.addinsPanel.getSelectedAddin().getId());
    projectPanel.addinsPanel.refreshAddinsTable();
  }

  public void disableAddin(String id)
  {
    projectModel.disableAddin(id);
    projectPanel.addinsPanel.refreshAddinsTable();
  }

  // TODO background task this

  private void gooFansBackup()
  {
    String description = JOptionPane.showInputDialog(mainController.getMainWindow(),
            resourceBundle.getString("gooFansBackup.description.message"),
            resourceBundle.getString("gooFansBackup.description.title"),
            JOptionPane.QUESTION_MESSAGE);
    if (description == null) return;

    try {
      ProfileBackupRequest request = new ProfileBackupRequest();
      request.backupProfile(currentProject, description);
      mainController.showMessageDialog(resourceBundle.getString("gooFansBackup.success.title"), resourceBundle.getString("gooFansBackup.success.message"));
    }
    catch (APIException e) {
      log.log(Level.WARNING, "Backup failed", e);
      mainController.showErrorDialog(resourceBundle.getString("gooFansBackup.error.title"), resourceBundle.formatString("gooFansBackup.error.message", e.getLocalizedMessage()));
    }
  }

  // TODO background task this

  private void gooFansRestore()
  {
    try {
      ProfileListRequest listRequest = new ProfileListRequest();

      List<ProfileListRequest.BackupInstance> backups = listRequest.listBackups();
      if (backups.isEmpty()) {
        mainController.showErrorDialog(resourceBundle.getString("gooFansRestore.noBackups.title"), resourceBundle.getString("gooFansRestore.noBackups.message"));
        return;
      }

      Object[] values = backups.toArray();

      Object selected = JOptionPane.showInputDialog(mainController.getMainWindow(),
              resourceBundle.getString("gooFansRestore.select.message"),
              resourceBundle.getString("gooFansRestore.select.title"),
              JOptionPane.QUESTION_MESSAGE, null, values, backups.get(backups.size() - 1));
      if (selected == null) return;

      ProfileListRequest.BackupInstance instance = (ProfileListRequest.BackupInstance) selected;

      if (JOptionPane.showConfirmDialog(mainController.getMainWindow(),
              resourceBundle.formatString("gooFansRestore.confirm.message", instance.description),
              resourceBundle.getString("gooFansRestore.confirm.title"),
              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
        return;

      ProfileRestoreRequest restoreRequest = new ProfileRestoreRequest();

      restoreRequest.restoreProfile(currentProject, instance.id);

      projectPanel.profilePanel.loadProfiles(); // TODO ugh!

      refreshView();

      mainController.showMessageDialog(resourceBundle.getString("gooFansRestore.success.title"), resourceBundle.getString("gooFansRestore.success.message"));
    }
    catch (APIException e) {
      log.log(Level.WARNING, "Restore failed", e);
      mainController.showErrorDialog(resourceBundle.getString("gooFansRestore.error.title"), resourceBundle.formatString("gooFansRestore.error.message", e.getLocalizedMessage()));
    }
  }

  // TODO background task this

  private void gooFansPublish()
  {
    try {
      ProfilePublishRequest request = new ProfilePublishRequest();
      String profileUrl = request.publishProfile(projectPanel.profilePanel.getSelectedProfile()); // TODO ugh!
      StringBuilder sb = new StringBuilder();
      for (String msg : request.getMessages()) {
        sb.append(TextUtil.stripHtmlTags(msg)).append('\n');
      }

      sb.append(resourceBundle.getString("gooFansPublish.success.message"));

      if (mainController.showYesNoDialog(resourceBundle.getString("gooFansPublish.success.title"), sb.toString())) {
        DesktopUtil.browseAndWarn(profileUrl, mainController.getMainWindow());
      }
    }
    catch (APIException e) {
      log.log(Level.WARNING, "Publish failed", e);
      mainController.showErrorDialog(resourceBundle.getString("gooFansPublish.error.title"), resourceBundle.formatString("gooFansPublish.error.message", e.getLocalizedMessage()));
    }
  }


  /* public void reorderAddins(final int srcRow, final int destRow)
  {
    log.fine("Reordering addins, " + srcRow + " to " + destRow);

//    List<String> addins = editorConfig.getEnabledAddins();

//    List<Addin> addins = WorldOfGoo.getAvailableAddins();

//    Addin removed = addins.remove(srcRow);
//    log.fine("Addin being moved is " + removed);
//    if (destRow > srcRow) destRow--;
//    addins.add(destRow, removed);

    String fromId = getDisplayAddins().get(srcRow).getId();
    String toId = getDisplayAddins().get(destRow).getId();

    log.fine("Reordering " + fromId + " before " + toId);

    int fromEntry = -1;
    int toEntry = -1;

    List<String> enabledAddins = editorConfig.getEnabledAddins();

    log.fine("Enabled addins: " + enabledAddins);

    for (int i = 0; i < enabledAddins.size(); i++) {
      String s = enabledAddins.get(i);

      if (fromId.equals(s)) fromEntry = i;
      if (toId.equals(s)) toEntry = i;
    }

    log.fine("Moving from pos " + fromEntry + " to " + toEntry);

    if (fromEntry == -1 || toEntry == -1) {
      log.fine("Can't reorder, something wasn't found");
      return;
    }

    // TODO we should still let them drag to items at the end (disabled ones)

    String removed = enabledAddins.remove(srcRow);
    log.finest("removed = " + removed);
    if (toEntry > fromEntry) toEntry--;
    enabledAddins.add(toEntry, removed);

    mainController.getMainWindow().mainPanel.projectPanel.addinsPanel.updateViewFromModel(editorConfig);
  }*/

  public boolean canExit()
  {
    if (projectModel == null) return true;

    updateModelFromView();

    System.out.println("projectModel.getEditorConfig() = " + projectModel.getEditorConfig());
    System.out.println("liveConfig = " + liveConfig);

    if (!projectModel.getEditorConfig().equals(liveConfig)) {
      if (!mainController.showYesNoDialog(resourceBundle.getString("exit.unsaved.title"), resourceBundle.getString("exit.unsaved.message"))) {
        log.fine("User cancelled exit");
        return false;
      }
    }
    return true;
  }

  public boolean canChangeProjects()
  {
    // TODO need different messages since we're not quitting!

    return canExit();
  }

  private void save(boolean launch)
  {
    System.out.println("in save");
    updateModelFromView();

    projectModel.removeUnavailableAddins();

    if (!currentProject.readyToBuild()) return;

    TargetFile testFile = currentProject.getTarget().getGameRoot().getChild(currentProject.getGameXmlFilename("properties/text.xml"));

    if (!testFile.isFile()) {
      mainController.showMessageDialog(resourceBundle.getString("firstBuild.title"), resourceBundle.getString("firstBuild.message"));
    }

    WorldBuilder configWriter = new WorldBuilder(currentProject, projectModel.getEditorConfig());

    boolean errored = false;

    try {
      GUIUtil.runTask(mainController.getMainWindow(), resourceBundle.getString("worldBuilder.progress.title"), configWriter);
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Error writing configuration", e);
      mainController.showErrorDialog(resourceBundle.getString("worldBuilder.error.title"), e.getMessage() + " (" + e.getClass().getName() + ")");
      errored = true;
    }

    loadProjectModel();

    updateViewFromModel();

    if (launch && !errored && currentProject instanceof LocalProject) {
      try {
        PlatformSupport.launch((LocalProject) currentProject);
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Error launching World of Goo", e);
        mainController.showErrorDialog(resourceBundle.getString("launch.error.title"), e.getLocalizedMessage());
      }
    }
  }


  private void updateGenerateOnlineIdMenu()
  {
    mainController.getMainWindow().mainMenu.generateIdMenuItem.setEnabled(!projectPanel.profilePanel.areAllProfilesOnline());
    mainController.getMainWindow().mainMenu.removeIdMenuItem.setEnabled(projectPanel.profilePanel.areAnyProfilesGeneratedId());
  }


  /**
   * Gets the data model for the currently-selected project. May be null if no project currently loaded.
   *
   * @return
   */
  public ProjectModel getProjectModel()
  {
    return projectModel;
  }

  void refreshView()
  {
    // First update the model from the view, to ensure things like text fields are preserved.
    updateModelFromView();
    updateViewFromModel();
  }

  void updateViewFromModel()
  {
    projectPanel.updateViewFromModel(projectModel);
  }

  void updateModelFromView()
  {
    projectPanel.updateModelFromView(projectModel);
  }


  public void generateOnlineId()
  {
    if (!mainController.showYesNoDialog(resourceBundle.getString("generateOnlineId.confirm.title"), resourceBundle.getString("generateOnlineId.confirm.message"))) {
      return;
    }

    try {
      GenerateOnlineIds.generateOnlineIds(currentProject);
    }
    catch (IOException e) {
      mainController.showErrorDialog(resourceBundle.getString("generateOnlineId.error.title"), resourceBundle.formatString("generateOnlineId.error.message", e.getLocalizedMessage()));
      return;
    }

    projectPanel.profilePanel.loadProfiles(); // TODO ugh!

    refreshView();
    mainController.showMessageDialog(resourceBundle.getString("generateOnlineId.success.title"), resourceBundle.getString("generateOnlineId.success.message"));
  }

  public void removeOnlineId()
  {
    if (!mainController.showYesNoDialog(resourceBundle.getString("removeOnlineId.confirm.title"), resourceBundle.getString("removeOnlineId.confirm.message"))) {
      return;
    }

    try {
      GenerateOnlineIds.removeGeneratedOnlineIds(currentProject);
    }
    catch (IOException e) {
      mainController.showErrorDialog(resourceBundle.getString("removeOnlineId.error.title"), resourceBundle.formatString("removeOnlineId.error.message", e.getLocalizedMessage()));
      return;
    }

    projectPanel.profilePanel.loadProfiles(); // TODO ugh!

    refreshView();
    mainController.showMessageDialog(resourceBundle.getString("removeOnlineId.success.title"), resourceBundle.getString("removeOnlineId.success.message"));
  }

//  public void addinsUpdated()
//  {
//    mainController.getMainWindow().mainPanel.projectPanel.addinsPanel.refreshTable();
//  }

  public Project getCurrentProject()
  {
    return currentProject;
  }
}
