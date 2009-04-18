package com.goofans.gootool.l10n;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.wog.WorldOfGoo;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ImageL10nPanel implements ActionListener
{
  private JTextField inputDirectoryTextField;
  private JButton inputDirectoryChangeButton;
  private JTextField wikiBaseURLTextField;
  private JTable languagesTable;
  private JButton colorChooser;
  private JButton buildAndViewButton;
  private JTextField outputDirectoryTextField;
  private JButton outputDirectoryChangeButton;
  private JButton buildAndSaveButton;
  public JPanel rootPanel;
  private JButton languageAddButton;
  private JButton languageRemoveButton;
  private JCheckBox debugModeCheckBox;
  public LanguagesTableModel languagesDataModel;

  private static final String PREF_L10N_WIKI_BASE = "l10n_wiki_base";

  private static final String CMD_ADD_LANGUAGE = "AddLanguage";
  private static final String CMD_REMOVE_LANGUAGE = "RemoveLanguage";
  private static final String CMD_BUILD_AND_VIEW = "BuildAndView";
  private static final String CMD_BUILD_AND_SAVE = "BuildAndSave";
  private static final String CMD_CHANGE_INPUT = "ChangeInputDir";
  private static final String CMD_CHANGE_OUTPUT = "ChangeOutputDir";
  private static final String CMD_CHANGE_COLOR = "ChangeColor";

  public ImageL10nPanel()
  {
    languagesDataModel = new LanguagesTableModel();
    languagesTable.setModel(languagesDataModel);

    languagesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    languagesTable.getColumnModel().getColumn(0).setPreferredWidth(40);
    languagesTable.getColumnModel().getColumn(1).setPreferredWidth(50);
    languagesTable.getColumnModel().getColumn(2).setPreferredWidth(150);

    inputDirectoryChangeButton.setActionCommand(CMD_CHANGE_INPUT);
    inputDirectoryChangeButton.addActionListener(this);

    languageAddButton.setActionCommand(CMD_ADD_LANGUAGE);
    languageAddButton.addActionListener(this);

    languageRemoveButton.setActionCommand(CMD_REMOVE_LANGUAGE);
    languageRemoveButton.addActionListener(this);

    buildAndViewButton.setActionCommand(CMD_BUILD_AND_VIEW);
    buildAndViewButton.addActionListener(this);

    buildAndSaveButton.setActionCommand(CMD_BUILD_AND_SAVE);
    buildAndSaveButton.addActionListener(this);

    Preferences p = WorldOfGoo.getPreferences();
    wikiBaseURLTextField.setText(p.get(PREF_L10N_WIKI_BASE, "http://hell.student.utwente.nl/wog/mediawiki/"));

    colorChooser.setBackground(Color.WHITE);
    colorChooser.setForeground(Color.WHITE);
    colorChooser.setActionCommand(CMD_CHANGE_COLOR);
    colorChooser.addActionListener(this);

    outputDirectoryChangeButton.setActionCommand(CMD_CHANGE_OUTPUT);
    outputDirectoryChangeButton.addActionListener(this);
  }

  public void actionPerformed(ActionEvent event)
  {
    String cmd = event.getActionCommand();
    if (cmd.equals(CMD_ADD_LANGUAGE)) {
      languagesDataModel.addRow();
    }
    else if (cmd.equals(CMD_REMOVE_LANGUAGE)) {
//      System.out.println("languagesTable.getSelectedRow() = " + languagesTable.getSelectedRow());
      if (languagesTable.getSelectedRow() != -1) {
        languagesDataModel.removeRow(languagesTable.getSelectedRow());
      }
    }
    else if (cmd.equals(CMD_BUILD_AND_VIEW)) {
      buildAndView();
    }
    else if (cmd.equals(CMD_BUILD_AND_SAVE)) {
      buildAndSave();
    }
    else if (cmd.equals(CMD_CHANGE_INPUT)) {
      selectInputDirectory();
    }
    else if (cmd.equals(CMD_CHANGE_OUTPUT)) {
      selectOutputDirectory();
    }
    else if (cmd.equals(CMD_CHANGE_COLOR)) {
      Color color = JColorChooser.showDialog(rootPanel, "Choose background color", colorChooser.getBackground());
      colorChooser.setBackground(color);
      colorChooser.setForeground(color);
    }
  }

  private void buildAndView()
  {
    File inputDirectory = new File(inputDirectoryTextField.getText());
    if (!inputDirectory.exists()) {
      JOptionPane.showMessageDialog(rootPanel, "Input directory doesn't exist: " + inputDirectory, "Directory not found", JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      Map<String, Map<String, String>> languages = downloadTranslations();

      ImageTool imageTool = new ImageTool(inputDirectory, null, languages, colorChooser.getBackground(), debugModeCheckBox.isSelected());

      GUIUtil.runTask(null, "Preparing images", imageTool);

      imageTool.showWindow();
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(rootPanel, "Exception: " + e.getClass() + ": " + e.getLocalizedMessage());
    }
  }

  private void buildAndSave()
  {
    File inputDirectory = new File(inputDirectoryTextField.getText());
    if (!inputDirectory.exists()) {
      JOptionPane.showMessageDialog(rootPanel, "Input directory doesn't exist: " + inputDirectory, "Directory not found", JOptionPane.ERROR_MESSAGE);
      return;
    }

    File outputDirectory = new File(outputDirectoryTextField.getText());
    if (!outputDirectory.exists()) {
      JOptionPane.showMessageDialog(rootPanel, "Output directory doesn't exist: " + outputDirectory, "Directory not found", JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      Map<String, Map<String, String>> languages = downloadTranslations();

      ImageTool imageTool = new ImageTool(inputDirectory, outputDirectory, languages, null, false);

      GUIUtil.runTask(null, "Preparing images", imageTool);

      JOptionPane.showMessageDialog(rootPanel, "Build complete in " + outputDirectory + "!", "Build complete", JOptionPane.INFORMATION_MESSAGE);
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(rootPanel, "Exception: " + e.getClass() + ": " + e.getLocalizedMessage());
    }
  }

  private void selectInputDirectory()
  {
    JFileChooser chooser = new JFileChooser(inputDirectoryTextField.getText());
    chooser.setFileFilter(new FileFilter()
    {
      public boolean accept(File f)
      {
        return (f.getName().equals("l10n_images.xml")) || f.isDirectory();
      }

      public String getDescription()
      {
        return "l10n_images.xml file";
      }
    });

    if (chooser.showOpenDialog(rootPanel) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    inputDirectoryTextField.setText(chooser.getSelectedFile().getParentFile().getAbsolutePath());
  }

  private void selectOutputDirectory()
  {
    JFileChooser chooser = new JFileChooser(outputDirectoryTextField.getText());
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

    if (chooser.showOpenDialog(rootPanel) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    outputDirectoryTextField.setText(chooser.getSelectedFile().getAbsolutePath());
  }

  private Map<String, Map<String, String>> downloadTranslations() throws Exception
  {
    final String wikiBase = wikiBaseURLTextField.getText();
    final Map<String, Map<String, String>> languages = new HashMap<String, Map<String, String>>();

    GUIUtil.runTask(null, "Downloading translations", new ProgressIndicatingTask()
    {
      public void run() throws Exception
      {
        for (LanguagesTableModel.L10nLanguage language : languagesDataModel.getLanguages()) {
          if (language.enabled) {
            beginStep("Downloading " + language.wikiPage, false);
            Map<String, String> translations = TranslationDownloader.getTranslations(wikiBase, language.wikiPage, true);
            languages.put(language.code, translations);
          }
        }
      }
    });
    return languages;
  }
}
