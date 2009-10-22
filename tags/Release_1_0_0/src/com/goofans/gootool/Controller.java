package com.goofans.gootool;

import net.infotrek.util.DesktopUtil;
import net.infotrek.util.TextUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.goofans.gootool.GameFileCodecTool.CodecType;
import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.siteapi.*;
import com.goofans.gootool.util.*;
import com.goofans.gootool.view.AboutDialog;
import com.goofans.gootool.view.AddinPropertiesDialog;
import com.goofans.gootool.view.AddinUpdatesChooser;
import com.goofans.gootool.view.MainFrame;
import com.goofans.gootool.wog.ConfigurationWriterTask;
import com.goofans.gootool.wog.WorldOfGoo;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Controller implements ActionListener
{
  private static final Logger log = Logger.getLogger(Controller.class.getName());

  public static final String CMD_EXIT = "Exit";

  public static final String CMD_ABOUT = "Help>About";
  public static final String CMD_CHECK_FOR_UPDATES = "Help>CheckForUpdates";
  public static final String CMD_DIAGNOSTICS = "Help>Diagnostics";

  public static final String CMD_ADDIN_INSTALL = "Addin>Install";
  public static final String CMD_ADDIN_UPDATECHECK = "Addin>UpdateCheck";
  public static final String CMD_ADDIN_PROPERTIES = "Addin>Properties";
  public static final String CMD_ADDIN_UNINSTALL = "Addin>Uninstall";
  public static final String CMD_ADDIN_ENABLE = "Addin>Enable";
  public static final String CMD_ADDIN_DISABLE = "Addin>Disable";

  public static final String CMD_CHANGE_INSTALL_DIR = "Options>InstallDir";
  public static final String CMD_CHANGE_CUSTOM_DIR = "Options>CustomDir";
  public static final String CMD_CHANGE_PROFILE_FILE = "Options>ProfileFile";

  public static final String CMD_GOOFANS_LOGIN = "GooFans>Login";
  public static final String CMD_GOOFANS_BACKUP = "GooFans>Backup";
  public static final String CMD_GOOFANS_RESTORE = "GooFans>Restore";
  public static final String CMD_GOOFANS_PUBLISH = "GooFans>Publish";

  public static final String CMD_SAVE = "Save";
  public static final String CMD_SAVE_AND_LAUNCH = "Save&Launch";
  public static final String CMD_REVERT = "Revert";

  public static final String CMD_TRANSLATOR_MODE = "ToggleTranslator";

  public static final String CMD_DECRYPT_BIN_PC = "Decrypt>BinPC";
  public static final String CMD_DECRYPT_BIN_MAC = "Decrypt>BinMac";
  public static final String CMD_DECRYPT_PNGBINLTL_MAC = "Decrypt>PngBinLtlMac";
  public static final String CMD_DECRYPT_ANIM = "Decrypt>Anim";
  public static final String CMD_DECRYPT_MOVIE = "Decrypt>Movie";
  public static final String CMD_ENCRYPT_BIN_PC = "Encrypt>BinPC";
  public static final String CMD_ENCRYPT_BIN_MAC = "Encrypt>BinMac";
  public static final String CMD_ENCRYPT_PNGBINLTL_MAC = "Encrypt>PngBinLtlMac";

  private MainFrame mainFrame;

  // The configuration currently live on disk.
  private Configuration liveConfig;

  // The configuration we're editing
  private Configuration editorConfig;
  private final TextProvider textProvider;

  // The codecs
  private final Map<String, GameFileCodecTool> codecs = new HashMap<String, GameFileCodecTool>(6);

  public Controller()
  {
    textProvider = GooTool.getTextProvider();

    codecs.put(CMD_DECRYPT_BIN_PC, new GameFileCodecTool("bin", "Encrypted Bin File (Windows/Linux)", "xml", CodecType.AES_DECODE));
    codecs.put(CMD_DECRYPT_BIN_MAC, new GameFileCodecTool("bin", "Encrypted Bin File (Mac)", "xml", CodecType.XOR_DECODE));
    codecs.put(CMD_DECRYPT_PNGBINLTL_MAC, new GameFileCodecTool("png.binltl", "Encoded Image File", "png", CodecType.PNGBINLTL_DECODE));
    codecs.put(CMD_DECRYPT_ANIM, new GameFileCodecTool("anim.binltl", "Animation File", "anim.xml", CodecType.ANIM_DECODE));
    codecs.put(CMD_DECRYPT_MOVIE, new GameFileCodecTool("movie.binltl", "Movie File", "movie.xml", CodecType.MOVIE_DECODE));
    codecs.put(CMD_ENCRYPT_BIN_PC, new GameFileCodecTool("xml", "XML Document", "bin", CodecType.AES_ENCODE));
    codecs.put(CMD_ENCRYPT_BIN_MAC, new GameFileCodecTool("xml", "XML Document", "bin", CodecType.XOR_ENCODE));
    codecs.put(CMD_ENCRYPT_PNGBINLTL_MAC, new GameFileCodecTool("png", "PNG Image File", "png.binltl", CodecType.PNGBINLTL_ENCODE));
  }

  /**
   * Invoked when an action occurs.
   */
  public void actionPerformed(ActionEvent event)
  {
    String cmd = event.getActionCommand();

    if (cmd.equals(CMD_ABOUT)) {
      openAboutDialog();
    }
    else if (cmd.equals(CMD_SAVE)) {
      save(false);
    }
    else if (cmd.equals(CMD_SAVE_AND_LAUNCH)) {
      save(true);
    }
    else if (cmd.equals(CMD_EXIT)) {
      maybeExit();
    }
    else if (cmd.equals(CMD_ADDIN_PROPERTIES)) {
      new AddinPropertiesDialog(mainFrame, getSelectedAddin()).setVisible(true);
//      Addin addin = getSelectedAddin();
//      showMessageDialog("Addin details: " + addin);
    }
    else if (cmd.equals(CMD_REVERT)) {
      log.info("Reverting");
      editorConfig = new Configuration(liveConfig);
      updateViewFromModel(editorConfig);
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
    else if (cmd.equals(CMD_ADDIN_UNINSTALL)) {
      uninstallAddin();
    }
    else if (cmd.equals(CMD_CHANGE_INSTALL_DIR)) {
      changeInstallDir();
    }
    else if (cmd.equals(CMD_CHANGE_CUSTOM_DIR)) {
      changeCustomDir();
    }
    else if (cmd.equals(CMD_CHANGE_PROFILE_FILE)) {
      changeProfileFile();
    }
    else if (cmd.equals(CMD_GOOFANS_LOGIN)) {
      gooFansLogin();
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
    else if (cmd.equals(CMD_TRANSLATOR_MODE)) {
      boolean enabled = mainFrame.mainMenu.translatorModeMenuItem.isSelected();
      updateImageLocalisationPanel(enabled);
      ToolPreferences.setL10nEnabled(enabled);
    }
    else if (cmd.equals(CMD_CHECK_FOR_UPDATES)) {
      try {
        GUIUtil.runTask(mainFrame, "Checking for updates...", new ProgressIndicatingTask()
        {
          @Override
          public void run() throws Exception
          {
            VersionCheck versionCheck = new VersionCheck(mainFrame, true);
            versionCheck.run();
          }
        });
      }
      catch (Exception e) {
        // should never happen.
        showErrorDialog("Can't check version", e.getLocalizedMessage());
      }
    }
    else if (cmd.equals(CMD_DIAGNOSTICS)) {
      runDiagnostics();
    }
    else if (codecs.containsKey(cmd)) {
      try {
        codecs.get(cmd).runTool(mainFrame);
      }
      catch (Exception e) {
        log.log(Level.SEVERE, "Error coding file", e);
        showErrorDialog("Error coding file", e.getLocalizedMessage());
      }
    }
  }

  private void updateImageLocalisationPanel(boolean enabled)
  {
    if (enabled) {
      mainFrame.tabbedPane.add("Image localisation", mainFrame.imageLocalisationPanel.rootPanel);
    }
    else {
      mainFrame.tabbedPane.remove(mainFrame.imageLocalisationPanel.rootPanel);
    }
  }


  private Addin getSelectedAddin()
  {
    int selectedRow = mainFrame.addinsPanel.addinTable.getSelectedRow();
    return getDisplayAddins().get(selectedRow);
  }

  private void addinUpdateCheck()
  {
    final Map<String, AddinUpdatesCheckRequest.AvailableUpdate>[] updates = new Map[1];

    try {
      GUIUtil.runTask(mainFrame, "Checking for Updates", new ProgressIndicatingTask()
      {
        @Override
        public void run() throws Exception
        {
          beginStep("Checking for updates", false);
          AddinUpdatesCheckRequest checkRequest = new AddinUpdatesCheckRequest();

          updates[0] = checkRequest.checkUpdates();
        }
      });
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Error checking for updates", e);
      showErrorDialog("Error checking for updates", e.getLocalizedMessage());
      return;
    }

    AddinUpdatesChooser dialog = new AddinUpdatesChooser(mainFrame, updates[0]);
    dialog.setVisible(true);

    // Refresh the addins table
    refreshView();
  }

  private void installAddin()
  {
    if (!ensureCustomDirIsSet()) return;

    JFileChooser chooser = new JFileChooser(ToolPreferences.getMruAddinDir());
    chooser.setMultiSelectionEnabled(true);
    FileNameExtensionFilter filter = new FileNameExtensionFilter("World of Goo Mods", WorldOfGoo.GOOMOD_EXTENSION);
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(mainFrame);

    if (returnVal != JFileChooser.APPROVE_OPTION) {
      log.finer("User cancelled chooser");
      return;
    }

    ToolPreferences.setMruAddinDir(chooser.getCurrentDirectory().getPath());

    File[] selectedFiles = chooser.getSelectedFiles();

    for (File addinFile : selectedFiles) {
      installAddin(addinFile);
    }
  }

  private boolean ensureCustomDirIsSet()
  {
    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    if (!worldOfGoo.isCustomDirSet()) {
      showMessageDialog(textProvider.getText("customdir.select.title"), textProvider.getText("customdir.select.message"));
      changeCustomDir();
      if (!worldOfGoo.isCustomDirSet()) {
        return false;
      }
    }
    return true;
  }

  /**
   * NB NB must be run on the GUI thread!
   *
   * @param addinFile
   */
  public void installAddin(File addinFile)
  {
    if (!addinFile.exists()) {
      log.info("File not found: " + addinFile);
      showErrorDialog("File not found", addinFile + " not found");
      return;
    }

    if (!ensureCustomDirIsSet()) return;

    log.info("Opening file " + addinFile);
    Addin addin;
    try {
      addin = AddinFactory.loadAddin(addinFile);
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Error opening file " + addinFile, e);
      showErrorDialog("Error opening " + addinFile.getName(), "Invalid addin: " + e.getLocalizedMessage());
      return;
    }

    // For installation, we only need to check dependencies are satisfied by available addins,
    // not necessarily enabled ones. TODO when enabling, we need to check all dependents are enabled.

//    if (!addin.areDependenciesSatisfiedBy(WorldOfGoo.getAvailableAddins())) {
//      log.info("Not installing because dependencies not satisfied");
//      showErrorDialog("Can't install " + addinFile.getName(), "Can't install this addin because its dependencies aren't satisfied");
//      return;
//    }

    StringBuilder msg = new StringBuilder("Are you sure you wish to install this addin?\n");
    msg.append("Name: ").append(addin.getName()).append("\n");
    msg.append("Author: ").append(addin.getAuthor()).append("\n");
    msg.append("Version: ").append(addin.getVersion()).append("\n");

    int returnVal = showYesNoDialog("Install Addin?", msg.toString());
    if (returnVal != JOptionPane.YES_OPTION) {
      log.info("User cancelled installation of " + addin);
      return;
    }

    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();

    try {
      for (Addin installedAddin : WorldOfGoo.getAvailableAddins()) {
        if (installedAddin.getId().equals(addin.getId())) {
          msg = new StringBuilder();
          msg.append("Addin ").append(installedAddin.getName()).append(" version ").append(installedAddin.getVersion());
          msg.append(" already exists.\n");

          VersionSpec installedVersion = installedAddin.getVersion();
          VersionSpec newVersion = addin.getVersion();

          if (installedVersion.compareTo(newVersion) < 0) {
            msg.append("Would you like to upgrade it to version ").append(addin.getVersion());
          }
          else if (installedVersion.compareTo(newVersion) > 0) {
            msg.append("Would you like to replace it with the earlier version ").append(addin.getVersion());
          }
          else {
            msg.append("Would you like to replace it");
          }
          msg.append("?");

          returnVal = showYesNoDialog("Replace Addin?", msg.toString());
          if (returnVal != JOptionPane.YES_OPTION) {
            log.info("User cancelled overwriting installation of " + addin);
            return;
          }
          worldOfGoo.uninstallAddin(installedAddin, false);
          break;
        }
      }

      worldOfGoo.installAddin(addinFile, addin.getId(), false);
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to copy to addins directory", e);
      showErrorDialog("Error installing addin", e.getLocalizedMessage());
      return;
    }

    editorConfig.enableAddin(addin.getId());

    msg = new StringBuilder();
    msg.append("Addin ").append(addin.getName()).append(" installed and enabled!");
    if (addin.getType() == Addin.TYPE_LEVEL) {
      msg.append("\nYour new level will appear in Chapter 1, at the far top-left.");
    }
    msg.append("\nDon't forget to save!");

    showMessageDialog("Addin installed", msg.toString());
    refreshView();
  }

  private void uninstallAddin()
  {
    Addin addin = getSelectedAddin();
    log.info("Uninstall " + addin);

    // this isn't really necessary, we just won't have it there when we rebuild.
//    if (liveConfig.isEnabledAdddin(addin.getId())) {
//      showErrorDialog("Error", "This addin is currently enabled. You must disable it and save before uninstalling it.");
//      return;
//    }

    try {
      WorldOfGoo.getTheInstance().uninstallAddin(addin, false);
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to uninstall addin", e);
      showErrorDialog("Error uninstalling addin", e.getLocalizedMessage());
      return;
    }

    showMessageDialog("Addin uninstalled", "Addin " + addin.getName() + " uninstalled!");

    editorConfig.disableAddin(addin.getId());
    refreshView();
  }

  private void enableAddin()
  {
    Addin addin = getSelectedAddin();

    // TODO better checking of satisfaction here (use editor config, i.e. what's enabled

    if (!addin.areDependenciesSatisfiedBy(WorldOfGoo.getAvailableAddins())) {
      log.info("Not installing because dependencies not satisfied");
      showErrorDialog("Can't enable " + addin.getName(), "Can't enable this addin because its dependencies aren't satisfied");
      return;
    }

//    showErrorDialog("Can't enable " + addin.getName(), "Enabling addins is disabled in this release. Check back at www.goofans.com soon!");

    updateModelFromView(editorConfig);
    editorConfig.enableAddin(addin.getId());
    updateViewFromModel(editorConfig);
  }

  private void disableAddin()
  {
    updateModelFromView(editorConfig);
    editorConfig.disableAddin(getSelectedAddin().getId());
    updateViewFromModel(editorConfig);
  }


  private void changeInstallDir()
  {
    if (askToLocateWog() == 0) {
      refreshView();
    }
  }

  private void changeCustomDir()
  {
    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();

    File wogDir;
    try {
      wogDir = worldOfGoo.getWogDir();
    }
    catch (IOException e) {
      log.log(Level.WARNING, "Can't get World of Goo dir", e);
      showErrorDialog("Find World of Goo first", "Please select your World of Goo installation first!");
      return;
    }

    File selectedFile = worldOfGoo.chooseCustomDir(mainFrame);

    if (selectedFile == null) {
      return;
    }

    if (selectedFile.equals(wogDir)) {
      showErrorDialog("Bad choice", "You can't install to same directory that World of Goo's already in! Make a new directory.");
      return;
    }

    if (!selectedFile.exists()) {
      if (!selectedFile.mkdir()) {
        showErrorDialog("Can't create directory", "Couldn't create the directory " + selectedFile.getAbsolutePath());
        return;
      }
    }

    // Check if it's not empty

    if (selectedFile.list().length > 0) {
      if (showYesNoDialog("Directory not empty", "Warning! This directory isn't empty.\nAre you sure you wish to install into " + selectedFile.getAbsolutePath() + "?\n\nALL FILES IN THIS DIRECTORY WILL BE DELETED.") != JOptionPane.YES_OPTION) {
        return;
      }
    }
    else {
      if (showYesNoDialog("Confirm directory selection", "Are you sure you wish to install into " + selectedFile.getAbsolutePath() + "?") != JOptionPane.YES_OPTION) {
        return;
      }
    }

    try {
      worldOfGoo.setCustomDir(selectedFile);
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Can't set custom dir to " + selectedFile, e);
      showErrorDialog("Can't set custom directory", "Can't use that directory: " + e.getLocalizedMessage());
    }

    BillboardUpdater.maybeUpdateBillboards();

    refreshView();
  }

  private void changeProfileFile()
  {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new ProfileFileFilter());

    if (chooser.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File selectedFile = chooser.getSelectedFile();
    if (!ProfileFactory.locateProfileAtFile(selectedFile)) {
      JOptionPane.showMessageDialog(null, "Sorry, this isn't a valid profile file", "Profile not found", JOptionPane.ERROR_MESSAGE);
      return;
    }

    refreshView();
  }

  // TODO background task this
  private void gooFansLogin()
  {
    updateModelFromView(editorConfig);

    try {
      LoginTestRequest request = new LoginTestRequest();
      request.loginTest();

      showMessageDialog("Login success", "You are now logged in to goofans.com");
      ToolPreferences.setGooFansLoginOk(true);
    }
    catch (APIException e) {
      log.log(Level.WARNING, "Login test failed", e);
      ToolPreferences.setGooFansLoginOk(false);
      showErrorDialog("Login failed", e.getLocalizedMessage());
    }

    updateViewFromModel(editorConfig);
  }

  // TODO background task this
  private void gooFansBackup()
  {
    String description = JOptionPane.showInputDialog(mainFrame, "Enter an optional description for this backup", "Description", JOptionPane.QUESTION_MESSAGE);
    if (description == null) return;

    try {
      ProfileBackupRequest request = new ProfileBackupRequest();
      request.backupProfile(description);
      showMessageDialog("Backup complete", "Your backup is now stored at GooFans.com and can be restored on this or any other computer later.");
    }
    catch (APIException e) {
      log.log(Level.WARNING, "Backup failed", e);
      showErrorDialog("Backup failed", e.getLocalizedMessage());
    }
  }

  // TODO background task this
  private void gooFansRestore()
  {
    try {
      ProfileListRequest listRequest = new ProfileListRequest();

      List<ProfileListRequest.BackupInstance> backups = listRequest.listBackups();
      if (backups.isEmpty()) {
        showErrorDialog("No backups", "You have not yet created any backups.");
        return;
      }

      Object[] values = backups.toArray();

      Object selected = JOptionPane.showInputDialog(mainFrame, "Select backup to restore", "Select backup", JOptionPane.QUESTION_MESSAGE, null, values, backups.get(backups.size() - 1));
      if (selected == null) return;

      ProfileListRequest.BackupInstance instance = (ProfileListRequest.BackupInstance) selected;

      if (JOptionPane.showConfirmDialog(mainFrame, "Your current profile will be DELETED and replaced with the following backup:\n" + instance.description + "\n\nAre you sure you wish to proceed?", "Confirm profile restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
        return;

      ProfileRestoreRequest restoreRequest = new ProfileRestoreRequest();

      restoreRequest.restoreProfile(instance.id);

      mainFrame.profilePanel.loadProfiles();

      showMessageDialog("Restore complete", "Restore complete.");
    }
    catch (APIException e) {
      log.log(Level.WARNING, "Restore failed", e);
      showErrorDialog("Restore failed", e.getLocalizedMessage());
    }
  }

  // TODO background task this
  private void gooFansPublish()
  {
    try {
      ProfilePublishRequest request = new ProfilePublishRequest();
      final String profileUrl = request.publishProfile(mainFrame.profilePanel.getSelectedProfile());
      StringBuilder sb = new StringBuilder();
      for (String msg : request.getMessages()) {
        sb.append(TextUtil.stripHtmlTags(msg)).append('\n');
      }

      sb.append("\nWould you like to view your published profile now?");

      final int answer = showYesNoDialog("Profile published", sb.toString());

      if (answer == JOptionPane.YES_OPTION) {
        DesktopUtil.browseAndWarn(profileUrl, mainFrame);
      }
    }
    catch (APIException e) {
      log.log(Level.WARNING, "Backup failed", e);
      showErrorDialog("Backup failed", e.getLocalizedMessage());
    }
  }

  private void showMessageDialog(String title, String msg)
  {
    JOptionPane.showMessageDialog(mainFrame, msg, title, JOptionPane.INFORMATION_MESSAGE);
  }

  private void showErrorDialog(String title, String msg)
  {
    JOptionPane.showMessageDialog(mainFrame, msg, title, JOptionPane.ERROR_MESSAGE);
  }

  private int showYesNoDialog(String title, String msg)
  {
    return JOptionPane.showConfirmDialog(mainFrame, msg, title, JOptionPane.YES_NO_OPTION);
  }


  public void reorderAddins(final int srcRow, final int destRow)
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

    mainFrame.addinsPanel.updateViewFromModel(editorConfig);
  }

  public void maybeExit()
  {
    log.fine("Exit requested");

    updateModelFromView(editorConfig);

    if (!editorConfig.equals(liveConfig)) {
      String msg = "You have unsaved changes. Are you sure you wish to quit?";
      String title = "Unsaved changes";
      int returnVal = showYesNoDialog(title, msg);
      if (returnVal != JOptionPane.YES_OPTION) {
        log.fine("User cancelled exit");
        return;
      }
    }

    log.info("Exiting");
    System.exit(0);
  }

  private void save(boolean launch)
  {
    updateModelFromView(editorConfig);
    // TODO validate here

    removeUnavailableAddins(editorConfig);

    if (!ensureCustomDirIsSet()) return;

    try {
      if (WorldOfGoo.getTheInstance().isFirstCustomBuild()) {
        // TODO better message for Mac
        showMessageDialog(textProvider.getText("firstbuild.title"), textProvider.getText("firstbuild.message"));
      }
    }
    catch (IOException e) {
      // shouldn't happen
      log.log(Level.SEVERE, "Couldn't get custom dir", e);
      return;
    }

    final ConfigurationWriterTask configWriter = new ConfigurationWriterTask(editorConfig);

    boolean errored = false;

    try {
      GUIUtil.runTask(mainFrame, "Building your World of Goo", configWriter);
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Error writing configuration", e);
      showErrorDialog("Error writing configuration", e.getMessage() + " (" + e.getClass().getName() + ")");
      errored = true;
    }

    try {
      liveConfig = WorldOfGoo.getTheInstance().readConfiguration();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Error reading configuration", e);
      showErrorDialog("Error re-reading configuration!", "We recommend you restart GooTool. " + e.getMessage());
      errored = true;
    }
    editorConfig = new Configuration(liveConfig);
    updateViewFromModel(editorConfig);

    if (launch && !errored) {
      try {
        WorldOfGoo.getTheInstance().launch();
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Error launching World of Goo", e);
        showErrorDialog("Error launching World of Goo", e.getLocalizedMessage());
      }
    }
  }

  private void removeUnavailableAddins(Configuration config)
  {
    // Remove any addins that are enabled but don't exist
    List<Addin> availableAddins = WorldOfGoo.getAvailableAddins();
    boolean foundThisAddin;
    do {
      foundThisAddin = false;
      for (String enabledAddinName : config.getEnabledAddins()) {
        // see if this addin is available
        for (Addin availableAddin : availableAddins) {
          if (availableAddin.getId().equals(enabledAddinName)) {
            foundThisAddin = true;
            break;
          }
        }
        if (!foundThisAddin) {
          log.log(Level.WARNING, "Removed unavailable enabled addin " + enabledAddinName);
          config.disableAddin(enabledAddinName);
          break;
        }
      }
    } while (!foundThisAddin && config.getEnabledAddins().size() > 0);
  }


  public void openAboutDialog()
  {
    JDialog aboutDialog = new AboutDialog(mainFrame);

    aboutDialog.setVisible(true);
  }

  public void runDiagnostics()
  {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Save diagnostic report");
    chooser.setSelectedFile(new File(chooser.getCurrentDirectory(), "gootool_diagnostics.txt"));
    FileNameExtensionFilter filter = new FileNameExtensionFilter("Text file", "txt");
    chooser.setFileFilter(filter);
    int returnVal = chooser.showSaveDialog(mainFrame);

    if (returnVal != JFileChooser.APPROVE_OPTION) {
      log.finer("User cancelled diagnostics file chooser");
      return;
    }

    File outFile = chooser.getSelectedFile();
    try {
      Diagnostics diagnostics = new Diagnostics(outFile);
      GUIUtil.runTask(mainFrame, "Running diagnostics", diagnostics);
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Unable to run diagnostics", e);
      showErrorDialog("Unable to run diagnostics", "Unable to run diagnostics: " + e.getLocalizedMessage());
      return;
    }

    DesktopUtil.openAndWarn(outFile, mainFrame);
  }


  public void setMainFrame(MainFrame mainFrame)
  {
    this.mainFrame = mainFrame;
    updateViewFromModel(editorConfig);

    boolean enabled = ToolPreferences.isL10nEnabled();
    updateImageLocalisationPanel(enabled);
    mainFrame.mainMenu.translatorModeMenuItem.setSelected(enabled);
  }

  public void setInitialConfiguration(Configuration c)
  {
    liveConfig = c;
    editorConfig = new Configuration(c);

//    updateViewFromModel(editorConfig);
  }

  public Configuration getEditorConfig()
  {
    return editorConfig;
  }

  private void refreshView()
  {
    // First update the model from the view, to ensure things like text fields are preserved.
    updateModelFromView(editorConfig);
    updateViewFromModel(editorConfig);
  }

  private void updateViewFromModel(Configuration c)
  {
    mainFrame.updateViewFromModel(c);
  }

  private void updateModelFromView(Configuration c)
  {
    mainFrame.updateModelFromView(c);
  }

  public List<Addin> getDisplayAddins()
  {
    List<Addin> availableAddins = new ArrayList<Addin>(WorldOfGoo.getAvailableAddins());
    List<Addin> displayAddins = new ArrayList<Addin>(availableAddins.size());

    /* First, all the enabled addins, in order */

    for (String id : getEditorConfig().getEnabledAddins()) {
      for (Addin availableAddin : availableAddins) {
        if (availableAddin.getId().equals(id)) {
          displayAddins.add(availableAddin);
          availableAddins.remove(availableAddin);
          break;
        }
      }
    }

    /* Then any remaining addins */

    for (Addin availableAddin : availableAddins) {
      displayAddins.add(availableAddin);
    }

    return displayAddins;
  }

  /*
   * returns 0 if found, -1 if not found, -2 if user cancelled dialog
   */
  public int askToLocateWog()
  {
    log.finer("Asking to locate WoG");
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new WogExeFileFilter());

    log.finer("Chooser opening");
    if (chooser.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
      log.finer("Chooser cancelled");
      return -2;
    }

    File selectedFile = chooser.getSelectedFile();
    log.finer("Chooser selected: " + selectedFile);
    try {
      WorldOfGoo.getTheInstance().init(selectedFile);
      return 0;
    }
    catch (FileNotFoundException e) {
      log.info("World of Goo not found at " + selectedFile + " (" + selectedFile.getParentFile() + ")");

      JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "File not found", JOptionPane.ERROR_MESSAGE);
      return -1;
    }
  }

  public void bringToForeground()
  {
    log.finest("Controller is requesting focus on MainFrame");
    mainFrame.toFront();
    mainFrame.requestFocus();
  }
}
