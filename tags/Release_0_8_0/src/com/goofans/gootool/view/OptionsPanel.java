package com.goofans.gootool.view;

import com.goofans.gootool.model.Language;
import com.goofans.gootool.model.Resolution;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.Controller;

import javax.swing.*;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class OptionsPanel
{
  private static final Logger log = Logger.getLogger(OptionsPanel.class.getName());

  private JComboBox languageCombo;
  private JComboBox resolutionCombo;
  private JCheckBox allowWidescreen;
  public JPanel rootPanel;
  private JTextField uiInset;
  private JCheckBox skipOpeningMovieCheckBox;
  private JTextField watermark;

  private Controller controller;

  public OptionsPanel(Controller controller)
  {
    this.controller = controller;
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
