package com.goofans.gootool;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.MalformedURLException;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.ProfileFileFilter;
import com.goofans.gootool.util.WogExeFileFilter;
import com.goofans.gootool.view.AboutDialog;
import com.goofans.gootool.view.AddinPropertiesDialog;
import com.goofans.gootool.view.MainFrame;
import com.goofans.gootool.wog.ConfigurationWriterTask;
import com.goofans.gootool.wog.WorldOfGoo;
import com.goofans.gootool.siteapi.VersionCheck;

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

  public static final String CMD_ADDIN_INSTALL = "Addin>Install";
  public static final String CMD_ADDIN_PROPERTIES = "Addin>Properties";
  public static final String CMD_ADDIN_UNINSTALL = "Addin>Uninstall";
  public static final String CMD_ADDIN_ENABLE = "Addin>Enable";
  public static final String CMD_ADDIN_DISABLE = "Addin>Disable";

  public static final String CMD_CHANGE_INSTALL_DIR = "Options>InstallDir";
  public static final String CMD_CHANGE_CUSTOM_DIR = "Options>CustomDir";
  public static final String CMD_CHANGE_PROFILE_FILE = "Options>ProfileFile";

  public static final String CMD_SAVE = "Save";
  public static final String CMD_SAVE_AND_LAUNCH = "Save&Launch";
  public static final String CMD_REVERT = "Revert";

  public static final String CMD_TRANSLATOR_MODE = "ToggleTranslator";

  private MainFrame mainFrame;

  // The configuration currently live on disk.
  private Configuration liveConfig;

  // The configuration we're editing
  private Configuration editorConfig;
  private TextProvider textProvider;


  public Controller()
  {
    textProvider = GooTool.getTextProvider();
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
    else if (cmd.equals(CMD_TRANSLATOR_MODE)) {
      boolean enabled = mainFrame.mainMenu.translatorModeMenuItem.isSelected();
      updateImageLocalisationPanel(enabled);
      ToolPreferences.setL10nEnabled(enabled);
    }
    else if (cmd.equals(CMD_CHECK_FOR_UPDATES)) {
      VersionCheck versionCheck = null;
      try {
        versionCheck = new VersionCheck(mainFrame, true);
      }
      catch (MalformedURLException e) {
        showErrorDialog("Error checking version", e.getLocalizedMessage());
      }
      new Thread(versionCheck).start();
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

  private void installAddin()
  {
    if (!ensureCustomDirIsSet()) return;

    JFileChooser chooser = new JFileChooser();
    chooser.setMultiSelectionEnabled(true);
    FileNameExtensionFilter filter = new FileNameExtensionFilter("World of Goo Mods", WorldOfGoo.GOOMOD_EXTENSION);
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(mainFrame);

    if (returnVal != JFileChooser.APPROVE_OPTION) {
      log.finer("User cancelled chooser");
      return;
    }

    File[] selectedFiles = chooser.getSelectedFiles();

    for (File addinFile : selectedFiles) {
      installAddin(addinFile);
    }

    refreshView();
  }

  private boolean ensureCustomDirIsSet()
  {
    if (!WorldOfGoo.isCustomDirSet()) {
      showMessageDialog(textProvider.getText("customdir.select.title"), textProvider.getText("customdir.select.message"));
      changeCustomDir();
      if (!WorldOfGoo.isCustomDirSet()) {
        return false;
      }
    }
    return true;
  }

  private void installAddin(File addinFile)
  {
    if (!addinFile.exists()) {
      log.info("File not found: " + addinFile);
      showErrorDialog("File not found", addinFile + " not found");
      return;
    }

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

    // TODO check we already have an addin by this ID, if so, we must ask to replace it. (warn if downgrading)

    StringBuilder msg = new StringBuilder("Are you sure you wish to install this addin?\n");
    msg.append("Name: ").append(addin.getName()).append("\n");
    msg.append("Author: ").append(addin.getAuthor()).append("\n");
    msg.append("Version: ").append(addin.getVersion()).append("\n");

    int returnVal = showYesNoDialog("Install Addin?", msg.toString());
    if (returnVal != JOptionPane.YES_OPTION) {
      log.info("User cancelled installation of " + addin);
      return;
    }

    try {
      WorldOfGoo.installAddin(addinFile, addin.getId());
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to copy to addins directory", e);
      showErrorDialog("Error installing addin", e.getLocalizedMessage());
      return;
    }

    showMessageDialog("Addin installed", "Addin " + addin.getName() + " installed!");
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
      WorldOfGoo.uninstallAddin(addin);
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
    File wogDir;
    try {
      wogDir = WorldOfGoo.getWogDir();
    }
    catch (IOException e) {
      log.log(Level.WARNING, "Can't get wogdir", e);
      showErrorDialog("Find World of Goo first", "Please select your World of Goo installation first!");
      return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Choose a directory to save into");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    if (chooser.showSaveDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File selectedFile = chooser.getSelectedFile();

    if (selectedFile.equals(wogDir)) {
      showErrorDialog("Bad choice", "You can't install to same directory that World of Goo's already in! Make a new directory.");
      return;
    }

    if (!selectedFile.exists()) {
      if (!selectedFile.mkdir()) {
        showErrorDialog("Can't create directory", "Couldn't create the directory " + selectedFile.getAbsolutePath());
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
      WorldOfGoo.setCustomDir(selectedFile);
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Can't set custom dir to " + selectedFile, e);
      showErrorDialog("Can't set custom directory", "Can't use that directory: " + e.getLocalizedMessage());
    }

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
      if (!new File(WorldOfGoo.getCustomDir(), "WorldOfGoo.exe").exists()) {
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
      liveConfig = WorldOfGoo.readConfiguration();
    }
    catch (IOException e) {
      showErrorDialog("Error re-reading configuration!", "We recommend you restart GooTool. " + e.getMessage());
      errored = true;
    }
    editorConfig = new Configuration(liveConfig);
    updateViewFromModel(editorConfig);

    if (launch && !errored) {
      try {
        WorldOfGoo.launch();
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Error launching WoG", e);
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

  private JDialog aboutDialog;

  private void openAboutDialog()
  {
    if (aboutDialog == null) {
      aboutDialog = new AboutDialog(mainFrame);
    }

    aboutDialog.setVisible(true);
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
    JFileChooser chooser = new JFileChooser();
    chooser.setFileFilter(new WogExeFileFilter());

    if (chooser.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION) {
      return -2;
    }

    File selectedFile = chooser.getSelectedFile();
    try {
      WorldOfGoo.init(selectedFile.getParentFile());
      return 0;
    }
    catch (FileNotFoundException e) {
      log.info("WoG not found at " + selectedFile + " (" + selectedFile.getParentFile() + ")");

      JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "File not found", JOptionPane.ERROR_MESSAGE);
      return -1;
    }
  }
}
