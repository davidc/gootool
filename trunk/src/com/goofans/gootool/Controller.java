package com.goofans.gootool;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.view.AboutDialog;
import com.goofans.gootool.view.MainFrame;
import com.goofans.gootool.view.AddinPropertiesDialog;
import com.goofans.gootool.wog.WorldOfGoo;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author David Croft (david.croft@infotrek.net)
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
      save();
    }
    else if (cmd.equals(CMD_SAVE_AND_LAUNCH)) {
      save();
      // if no errors
      try {
        WorldOfGoo.launch();
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Error launching WoG", e);
        showErrorDialog("Error launching World of Goo", e.getLocalizedMessage());
      }
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
      // TODO now force all UI elements to update
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
    return WorldOfGoo.getAvailableAddins().get(selectedRow);
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

    if (!addin.areDependenciesSatisfiedBy(WorldOfGoo.getAvailableAddins())) {
      log.info("Not installing because dependencies not satisfied");
      showErrorDialog("Can't enable " + addin.getName(), "Can't enable this addin because its dependencies aren't satisfied");
      return;
    }

//    updateModelFromView(editorConfig);
//    editorConfig.enableAddin(addin.getId());
//    mainFrame.addinsPanel.updateViewFromModel(editorConfig);
    showErrorDialog("Can't enable " + addin.getName(), "Enabling addins is disabled in this release. Check goo.davidc.net soon!");
  }


  private void disableAddin()
  {
    updateModelFromView(editorConfig);
    editorConfig.disableAddin(getSelectedAddin().getId());
    mainFrame.addinsPanel.updateViewFromModel(editorConfig);
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


  public void reorderAddins(int srcRow, int destRow)
  {
    log.fine("Reordering addins, " + srcRow + " to " + destRow);

//    List<String> addins = editorConfig.getEnabledAddins();
    List<Addin> addins = WorldOfGoo.getAvailableAddins();

    Addin removed = addins.remove(srcRow);
    log.fine("Addin being moved is " + removed);
    if (destRow > srcRow) destRow--;
    addins.add(destRow, removed);

    // TODO reorder it in our Configuration
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

  public void save()
  {
    updateModelFromView(editorConfig);
    // TODO now write this out to WorldOfGoo.save. And copyFile it to liveConfig if no failure.
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
  }

  public void setInitialConfiguration(Configuration c)
  {
    liveConfig = c;
    editorConfig = new Configuration(c);

    updateViewFromModel(editorConfig);
  }

  public Configuration getLiveConfig()
  {
    return liveConfig;
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
}
