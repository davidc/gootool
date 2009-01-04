package com.goofans.gootool.view;

import com.goofans.gootool.model.Language;
import com.goofans.gootool.model.Resolution;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.Controller;
import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.wog.WorldOfGoo;

import javax.swing.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.Set;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.File;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class OptionsPanel implements ViewComponent
{
  private static final Logger log = Logger.getLogger(OptionsPanel.class.getName());

  private JComboBox languageCombo;
  private JComboBox resolutionCombo;
  private JCheckBox allowWidescreen;
  public JPanel rootPanel;
  private JTextField uiInset;
  private JCheckBox skipOpeningMovieCheckBox;
  private JTextField watermark;
  private JTextField installDirText;
  private JButton changeInstallDirButton;
  private JTextField customDirText;
  private JButton changeCustomDirButton;
  private JTextField profileFileText;
  private JButton changeProfileFileButton;

  public OptionsPanel(Controller controller)
  {
    for (Language language : Language.getSupportedLanguages()) {
      languageCombo.addItem(language);
    }

    allowWidescreen.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        updateResolutions();
      }
    });

    changeInstallDirButton.addActionListener(controller);
    changeInstallDirButton.setActionCommand(Controller.CMD_CHANGE_INSTALL_DIR);

    changeCustomDirButton.addActionListener(controller);
    changeCustomDirButton.setActionCommand(Controller.CMD_CHANGE_CUSTOM_DIR);

    changeProfileFileButton.addActionListener(controller);
    changeProfileFileButton.setActionCommand(Controller.CMD_CHANGE_PROFILE_FILE);
  }

  private void updateResolutions()
  {
    Set<Resolution> resolutions = Resolution.getSystemResolutions();

    boolean includeWidescreen = allowWidescreen.isSelected();

    // get current value to restore later, if possible
    Object curValue = resolutionCombo.getSelectedItem();
    log.finest("previously selected resolution is " + curValue);

    resolutionCombo.removeAllItems();
    for (Resolution resolution : resolutions) {
      if (includeWidescreen || !resolution.isWidescreen()) {
        log.finest("adding resolution " + resolution);
        resolutionCombo.addItem(resolution);
      }
    }

    resolutionCombo.setSelectedItem(curValue);
  }

  public void updateViewFromModel(Configuration c)
  {
    languageCombo.setSelectedItem(c.getLanguage());

    uiInset.setText(String.valueOf(c.getUiInset()));// TODO validate input

    skipOpeningMovieCheckBox.setSelected(c.isSkipOpeningMovie());
    watermark.setText(c.getWatermark());

// NB order matters here:
    allowWidescreen.setSelected(c.isAllowWidescreen());
    updateResolutions();
    resolutionCombo.setSelectedItem(c.getResolution());

    try {
      installDirText.setText(WorldOfGoo.getTheInstance().getWogDir().getAbsolutePath());
    }
    catch (IOException e) {
      installDirText.setText("");
    }

    try {
      customDirText.setText(WorldOfGoo.getTheInstance().getCustomDir().getAbsolutePath());
    }
    catch (IOException e) {
      customDirText.setText("");
    }

    File file = ProfileFactory.getProfileFile();
    if (file != null) {
      profileFileText.setText(file.getAbsolutePath());
    }
    else {
      profileFileText.setText("");
    }
  }

  public void updateModelFromView(Configuration c)
  {
    c.setLanguage((Language) languageCombo.getSelectedItem());
    try {
      c.setUiInset(Integer.valueOf(uiInset.getText()));
    }
    catch (NumberFormatException e) {
      c.setUiInset(0);
    }
    c.setSkipOpeningMovie(skipOpeningMovieCheckBox.isSelected());
    c.setWatermark(watermark.getText());
    c.setAllowWidescreen(allowWidescreen.isSelected());
    c.setResolution((Resolution) resolutionCombo.getSelectedItem());
  }
}
