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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import static com.goofans.gootool.GameFileCodecTool.CodecType;
import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.profile.GenerateOnlineIds;
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
  public static final String CMD_GOOTOOL_UPDATE_CHECK = "Help>GooToolUpdateCheck";
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

  private static final GooToolResourceBundle resourceBundle = GooTool.getTextProvider();

  private MainFrame mainFrame;

  // The configuration currently live on disk.
  private Configuration liveConfig;

  // The configuration we're editing
  private Configuration editorConfig;

  // The codecs
  private final Map<String, GameFileCodecTool> codecs = new HashMap<String, GameFileCodecTool>(6);

  public Controller()
  {
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
    else if (cmd.equals(CMD_GENERATE_ONLINE_ID)) {
      generateOnlineId();
    }
    else if (cmd.equals(CMD_REMOVE_ONLINE_ID)) {
      removeOnlineId();
    }
    else if (cmd.equals(CMD_GOOTOOL_UPDATE_CHECK)) {
      try {
        GUIUtil.runTask(mainFrame, resourceBundle.getString("gootoolUpdateCheck.checking"), new ProgressIndicatingTask()
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
        showErrorDialog(resourceBundle.getString("gootoolUpdateCheck.error.title"), resourceBundle.formatString("gootoolUpdateCheck.error.message", e.getLocalizedMessage()));
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
      GUIUtil.runTask(mainFrame, resourceBundle.getString("addinUpdateCheck.checking.title"), new ProgressIndicatingTask()
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
      showErrorDialog(resourceBundle.getString("addinUpdateCheck.error.title"),
              resourceBundle.formatString("addinUpdateCheck.error.message", e.getLocalizedMessage()));
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
      showMessageDialog(resourceBundle.getString("customdir.select.title"), resourceBundle.getString("customdir.select.message"));
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
      showErrorDialog(resourceBundle.getString("installAddin.notfound.title"), resourceBundle.formatString("installAddin.notfound.message", addinFile));
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

    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();

    try {
      for (Addin installedAddin : WorldOfGoo.getAvailableAddins()) {
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
          worldOfGoo.uninstallAddin(installedAddin, false);
          break;
        }
      }

      worldOfGoo.installAddin(addinFile, addin.getId(), false);
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to copy to addins directory", e);
      showErrorDialog(resourceBundle.getString("installAddin.error.title"), resourceBundle.formatString("installAddin.error.message", e.getLocalizedMessage()));
      return;
    }

    editorConfig.enableAddin(addin.getId());

    if (addin.getType() == Addin.TYPE_LEVEL) {
      msg = resourceBundle.formatString("installAddin.installed.message.level", addin.getName());
    }
    else {
      msg = resourceBundle.formatString("installAddin.installed.mod", addin.getName());
    }

    showMessageDialog(resourceBundle.getString("installAddin.installed.title"), msg);
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
      showErrorDialog(resourceBundle.getString("uninstallAddin.error.title"),
              resourceBundle.formatString("uninstallAddin.error.message", e.getLocalizedMessage()));
      return;
    }

    showMessageDialog(resourceBundle.getString("uninstallAddin.uninstalled.title"),
            resourceBundle.formatString("uninstallAddin.uninstalled.message", addin.getName()));

    editorConfig.disableAddin(addin.getId());
    refreshView();
  }

  private void enableAddin()
  {
    Addin addin = getSelectedAddin();

    // TODO better checking of satisfaction here (use editor config, i.e. what's enabled)

    if (!addin.areDependenciesSatisfiedBy(WorldOfGoo.getAvailableAddins())) {
      log.info("Not installing because dependencies not satisfied");
      showErrorDialog(resourceBundle.getString("enableAddin.dependencies.title"), resourceBundle.formatString("enableAddin.dependencies.message", addin.getName()));
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
      showErrorDialog(resourceBundle.getString("changeCustomDir.sourceNotFound.title"),
              resourceBundle.getString("changeCustomDir.sourceNotFound.message"));
      return;
    }

    File selectedFile = worldOfGoo.chooseCustomDir(mainFrame);

    if (selectedFile == null) {
      return;
    }

    if (selectedFile.equals(wogDir)) {
      showErrorDialog(resourceBundle.getString("changeCustomDir.sameAsSource.title"),
              resourceBundle.getString("changeCustomDir.sameAsSource.message"));
      return;
    }

    if (!selectedFile.exists()) {
      if (!selectedFile.mkdir()) {
        showErrorDialog(resourceBundle.getString("changeCustomDir.cantCreate.title"),
                resourceBundle.formatString("changeCustomDir.cantCreate.message", selectedFile.getAbsolutePath()));
        return;
      }
    }

    // Check if it's not empty

    if (selectedFile.list().length > 0) {
      if (!showYesNoDialog(resourceBundle.getString("changeCustomDir.notEmpty.title"),
              resourceBundle.formatString("changeCustomDir.notEmpty.message", selectedFile.getAbsolutePath()))) {
        return;
      }
    }
    else {
      if (!showYesNoDialog(resourceBundle.getString("changeCustomDir.confirm.title"),
              resourceBundle.formatString("changeCustomDir.confirm.message", selectedFile.getAbsolutePath()))) {
        return;
      }
    }

    try {
      worldOfGoo.setCustomDir(selectedFile);
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Can't set custom dir to " + selectedFile, e);
      showErrorDialog(resourceBundle.getString("changeCustomDir.error.title"),
              resourceBundle.getString("changeCustomDir.error.message") + e.getLocalizedMessage());
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
      showErrorDialog(resourceBundle.getString("changeProfileFile.error.title"),
              resourceBundle.getString("changeProfileFile.error.message"));
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

      showMessageDialog(resourceBundle.getString("gooFansLogin.success.title"), resourceBundle.getString("gooFansLogin.success.message"));
      ToolPreferences.setGooFansLoginOk(true);
    }
    catch (APIException e) {
      log.log(Level.WARNING, "Login test failed", e);
      ToolPreferences.setGooFansLoginOk(false);
      showErrorDialog(resourceBundle.getString("gooFansLogin.error.title"), resourceBundle.formatString("gooFansLogin.error.message", e.getLocalizedMessage()));
    }

    updateViewFromModel(editorConfig);
  }

  // TODO background task this
  private void gooFansBackup()
  {
    String description = JOptionPane.showInputDialog(mainFrame,
            resourceBundle.getString("gooFansBackup.description.message"),
            resourceBundle.getString("gooFansBackup.description.title"),
            JOptionPane.QUESTION_MESSAGE);
    if (description == null) return;

    try {
      ProfileBackupRequest request = new ProfileBackupRequest();
      request.backupProfile(description);
      showMessageDialog(resourceBundle.getString("gooFansBackup.succcess.title"),
              resourceBundle.getString("gooFansBackup.succcess.message"));
    }
    catch (APIException e) {
      log.log(Level.WARNING, "Backup failed", e);
      showErrorDialog(resourceBundle.getString("gooFansBackup.error.title"),
              resourceBundle.formatString("gooFansBackup.error.message", e.getLocalizedMessage()));
    }
  }

  // TODO background task this
  private void gooFansRestore()
  {
    try {
      ProfileListRequest listRequest = new ProfileListRequest();

      List<ProfileListRequest.BackupInstance> backups = listRequest.listBackups();
      if (backups.isEmpty()) {
        showErrorDialog(resourceBundle.getString("gooFansRestore.noBackups.title"),
                resourceBundle.getString("gooFansRestore.noBackups.message"));
        return;
      }

      Object[] values = backups.toArray();

      Object selected = JOptionPane.showInputDialog(mainFrame,
              resourceBundle.getString("gooFansRestore.select.message"),
              resourceBundle.getString("gooFansRestore.select.title"),
              JOptionPane.QUESTION_MESSAGE, null, values, backups.get(backups.size() - 1));
      if (selected == null) return;

      ProfileListRequest.BackupInstance instance = (ProfileListRequest.BackupInstance) selected;

      if (JOptionPane.showConfirmDialog(mainFrame,
              resourceBundle.formatString("gooFansRestore.confirm.message", instance.description),
              resourceBundle.getString("gooFansRestore.confirm.title"),
              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
        return;

      ProfileRestoreRequest restoreRequest = new ProfileRestoreRequest();

      restoreRequest.restoreProfile(instance.id);

      mainFrame.profilePanel.loadProfiles();

      refreshView();

      showMessageDialog(resourceBundle.getString("gooFansRestore.success.title"), resourceBundle.getString("gooFansRestore.success.message"));
    }
    catch (APIException e) {
      log.log(Level.WARNING, "Restore failed", e);
      showErrorDialog(resourceBundle.getString("gooFansRestore.error.title"), resourceBundle.formatString("gooFansRestore.error.message", e.getLocalizedMessage()));
    }
  }

  // TODO background task this
  private void gooFansPublish()
  {
    try {
      ProfilePublishRequest request = new ProfilePublishRequest();
      String profileUrl = request.publishProfile(mainFrame.profilePanel.getSelectedProfile());
      StringBuilder sb = new StringBuilder();
      for (String msg : request.getMessages()) {
        sb.append(TextUtil.stripHtmlTags(msg)).append('\n');
      }

      sb.append(resourceBundle.getString("gooFansPublish.success.message"));

      if (showYesNoDialog(resourceBundle.getString("gooFansPublish.success.title"), sb.toString())) {
        DesktopUtil.browseAndWarn(profileUrl, mainFrame);
      }
    }
    catch (APIException e) {
      log.log(Level.WARNING, "Publish failed", e);
      showErrorDialog(resourceBundle.getString("gooFansPublish.error.title"),
              resourceBundle.formatString("gooFansPublish.error.message", e.getLocalizedMessage()));
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

  /**
   * Show a "Yes or No" dialog, returning true only if the user selected "Yes".
   *
   * @param title Title bar
   * @param msg   Message
   * @return True only if the user said "yes", false if they said "No" or closed the window.
   */
  private boolean showYesNoDialog(String title, String msg)
  {
    return JOptionPane.showConfirmDialog(mainFrame, msg, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
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
      if (!showYesNoDialog(resourceBundle.getString("exit.unsaved.title"),
              resourceBundle.getString("exit.unsaved.message"))) {
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
        showMessageDialog(resourceBundle.getString("firstbuild.title"), resourceBundle.getString("firstbuild.message"));
      }
    }
    catch (IOException e) {
      // shouldn't happen
      log.log(Level.SEVERE, "Couldn't get custom dir", e);
      return;
    }

    ConfigurationWriterTask configWriter = new ConfigurationWriterTask(editorConfig);

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
      showErrorDialog("Error re-reading configuration!", "We recommend you exit GooTool immediately. " + e.getMessage());
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
    JDialog aboutDialog = new AboutDialog(mainFrame);

    aboutDialog.setVisible(true);
  }

  public void runDiagnostics()
  {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(resourceBundle.getString("diagnostics.chooser.title"));
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
      GUIUtil.runTask(mainFrame, resourceBundle.getString("diagnostics.running.title"), diagnostics);
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Unable to run diagnostics", e);
      showErrorDialog(resourceBundle.getString("diagnostics.error.title"),
              resourceBundle.formatString("diagnostics.error.message", e.getLocalizedMessage()));
      return;
    }

    DesktopUtil.openAndWarn(outFile, mainFrame);
  }


  public void setMainFrame(MainFrame mainFrame)
  {
    this.mainFrame = mainFrame;

    mainFrame.profilePanel.addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent evt)
      {
        if ("allProfilesAreOnline".equals(evt.getPropertyName()) || "anyProfilesHaveGeneratedId".equals(evt.getPropertyName())) {
          updateGenerateOnlineIdMenu();
        }
      }
    });

    updateViewFromModel(editorConfig);

    boolean enabled = ToolPreferences.isL10nEnabled();
    updateImageLocalisationPanel(enabled);
    mainFrame.mainMenu.translatorModeMenuItem.setSelected(enabled);
    updateGenerateOnlineIdMenu();
  }

  private void updateGenerateOnlineIdMenu()
  {
    mainFrame.mainMenu.generateIdMenuItem.setEnabled(!mainFrame.profilePanel.areAllProfilesOnline());
    mainFrame.mainMenu.removeIdMenuItem.setEnabled(mainFrame.profilePanel.areAnyProfilesGeneratedId());
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

      showErrorDialog("File not found", "Couldn't find World of Goo: " + e.getLocalizedMessage());
      return -1;
    }
  }

  public void bringToForeground()
  {
    log.finest("Controller is requesting focus on MainFrame");
    mainFrame.toFront();
    mainFrame.requestFocus();
  }

  public void generateOnlineId()
  {
    if (!showYesNoDialog(resourceBundle.getString("generateOnlineId.confirm.title"),
            resourceBundle.getString("generateOnlineId.confirm.message"))) {
      return;
    }

    try {
      GenerateOnlineIds.generateOnlineIds();
    }
    catch (IOException e) {
      showErrorDialog(resourceBundle.getString("generateOnlineId.error.title"),
              resourceBundle.formatString("generateOnlineId.error.message", e.getLocalizedMessage()));
      return;
    }

    mainFrame.profilePanel.loadProfiles();

    refreshView();
    showMessageDialog(resourceBundle.getString("generateOnlineId.success.title"),
            resourceBundle.getString("generateOnlineId.success.message"));
  }

  public void removeOnlineId()
  {
    if (!showYesNoDialog(resourceBundle.getString("removeOnlineId.confirm.title"),
            resourceBundle.getString("removeOnlineId.confirm.message"))) {
      return;
    }

    try {
      GenerateOnlineIds.removeGeneratedOnlineIds();
    }
    catch (IOException e) {
      showErrorDialog(resourceBundle.getString("removeOnlineId.error.title"),
              resourceBundle.formatString("removeOnlineId.error.message", e.getLocalizedMessage()));
      return;
    }

    mainFrame.profilePanel.loadProfiles();

    refreshView();
    showMessageDialog(resourceBundle.getString("removeOnlineId.success.title"),
            resourceBundle.getString("removeOnlineId.success.message"));
  }
}
