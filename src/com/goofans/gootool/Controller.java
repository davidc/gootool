package com.goofans.gootool;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.view.AboutDialog;
import com.goofans.gootool.view.MainFrame;
import com.goofans.gootool.view.AddinPropertiesDialog;
import com.goofans.gootool.view.ProgressDialog;
import com.goofans.gootool.wog.WorldOfGoo;
import com.goofans.gootool.wog.ConfigurationWriter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Controller implements ActionListener
{
  private static final Logger log = Logger.getLogger(Controller.class.getName());

  public static final String CMD_EXIT = "Exit";
  public static final String CMD_ABOUT = "About";

  public static final String CMD_ADDIN_INSTALL = "Addin>Install";
  public static final String CMD_ADDIN_PROPERTIES = "Addin>Properties";
  public static final String CMD_ADDIN_UNINSTALL = "Addin>Uninstall";
  public static final String CMD_ADDIN_ENABLE = "Addin>Enable";
  public static final String CMD_ADDIN_DISABLE = "Addin>Disable";

  public static final String CMD_SAVE = "Save";
  public static final String CMD_SAVE_AND_LAUNCH = "Save&Launch";
  public static final String CMD_REVERT = "Revert";

  private MainFrame mainFrame;

  // The configuration currently live on disk.
  private Configuration liveConfig;

  // The configuration we're editing
  private Configuration editorConfig;


  public Controller()
  {
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
  }


  private Addin getSelectedAddin()
  {
    int selectedRow = mainFrame.addinsPanel.addinTable.getSelectedRow();
    return getDisplayAddins().get(selectedRow);
  }

  private void installAddin()
  {
    JFileChooser chooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter("World of Goo Mods", WorldOfGoo.GOOMOD_EXTENSION);
    chooser.setFileFilter(filter);
    int returnVal = chooser.showOpenDialog(mainFrame);

    if (returnVal != JFileChooser.APPROVE_OPTION) {
      log.finer("User cancelled chooser");
      return;
    }

    File addinFile = chooser.getSelectedFile();
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

    returnVal = showYesNoDialog("Install Addin?", msg.toString());
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

    showMessageDialog("Addin " + addin.getName() + " installed!");

    refreshView();
  }

  private void uninstallAddin()
  {
    Addin addin = getSelectedAddin();
    log.info("Uninstall " + addin);
    if (liveConfig.isEnabledAdddin(addin.getId())) {
      showErrorDialog("Error", "This addin is currently enabled. You must disable it and save before uninstalling it.");
      return;
    }

    try {
      WorldOfGoo.uninstallAddin(addin);
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to uninstall addin", e);
      showErrorDialog("Error uninstalling addin", e.getLocalizedMessage());
      return;
    }

    showMessageDialog("Addin " + addin.getName() + " uninstalled!");

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


  private void showMessageDialog(String msg)
  {
    JOptionPane.showMessageDialog(mainFrame, msg);
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

    try {
      if (!WorldOfGoo.getCustomDir().exists()) {
        showMessageDialog("Since this is the first time you have run GooTool, it wiil take quite a long time for the initial save.");
      }
    }
    catch (IOException e) {
      // shouldn't happen
      log.log(Level.SEVERE, "Couldn't get custom dir", e);
    }

    final ConfigurationWriter configWriter = new ConfigurationWriter();

    final ProgressDialog progressDialog = new ProgressDialog(mainFrame, "Building your World of Goo");
    configWriter.addListener(progressDialog);


    new Thread()
    {
      public void run()
      {
        try {
          configWriter.writeConfiguration(editorConfig);
          SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              progressDialog.setVisible(false);
            }
          });
        }
        catch (final IOException e) {
          log.log(Level.SEVERE, "Error writing configuration", e);
          SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              showErrorDialog("Error writing configuration", e.getMessage());
              progressDialog.setVisible(false);
            }
          });
        }
      }
    }.start();

    progressDialog.setVisible(true);

    try {
      liveConfig = WorldOfGoo.readConfiguration();
    }
    catch (IOException e) {
      showErrorDialog("Error re-reading configuration!", "We recommend you restart GooTool. " + e.getMessage());
    }
    editorConfig = new Configuration(liveConfig);
    updateViewFromModel(editorConfig);

    // TODO refresh liveconfig from live

    if (launch) {
      // TODO must block here until
      // if no errors
      try {
        WorldOfGoo.launch();
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Error launching WoG", e);
        showErrorDialog("Error launching World of Goo", e.getLocalizedMessage());
      }
    }
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
}
