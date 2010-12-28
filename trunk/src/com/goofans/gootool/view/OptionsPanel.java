/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import net.infotrek.util.DesktopUtil;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Set;
import java.util.logging.Logger;

import com.goofans.gootool.MainController;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.ProjectController;
import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.model.ProjectModel;
import com.goofans.gootool.model.Language;
import com.goofans.gootool.model.Resolution;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.projects.LocalProjectConfiguration;
import com.goofans.gootool.projects.ProjectConfiguration;
import com.goofans.gootool.ui.HyperlinkLabel;

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
  private JTextField goofansUsername;
  private JPasswordField goofansPassword;
  private JButton gooFansLoginButton;
  private JCheckBox windowsVolumeControlCheckBox;
  private JPanel soundPanel;
  private HyperlinkLabel windowsVolumeControlHyperlink;
  private JCheckBox disableBillboardsCheckBox;
  private JComboBox refreshRateCombo;
  private JPanel displayPanel;

  public OptionsPanel()
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

    if (PlatformSupport.getPlatform() == PlatformSupport.Platform.WINDOWS) {
      final File f = new File("lib\\irrKlang\\README.txt");
      if (f.exists()) {
        windowsVolumeControlHyperlink.addHyperlinkListener(new HyperlinkListener()
        {
          public void hyperlinkUpdate(HyperlinkEvent e)
          {
            DesktopUtil.openAndWarn(f, rootPanel);
          }
        });
      }
      else {
        log.warning("Can't locate " + f.getAbsolutePath());
        windowsVolumeControlHyperlink.setVisible(false);
      }
    }

    Set<Integer> refreshRates = Resolution.getSystemRefreshRates();
    for (int refreshRate : refreshRates) {
      refreshRateCombo.addItem(new RefreshRate(refreshRate));
    }
  }

  private static class RefreshRate
  {
    int refreshRate;

    private RefreshRate(int refreshRate)
    {
      this.refreshRate = refreshRate;
    }

    @Override
    public String toString()
    {
      return refreshRate + " Hz";
    }

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

  public void initController(ProjectController projectController)
  {
    gooFansLoginButton.addActionListener(projectController);
    gooFansLoginButton.setActionCommand(MainController.CMD_GOOFANS_LOGIN); //@@ TODO this needs to hit the maincontroller
  }

  public void updateViewFromModel(ProjectModel model)
  {
    ProjectConfiguration c = model.getEditorConfig();

    languageCombo.setSelectedItem(c.getLanguage());
    skipOpeningMovieCheckBox.setSelected(c.isSkipOpeningMovie());
    watermark.setText(c.getWatermark());
    disableBillboardsCheckBox.setSelected(c.isBillboardsDisabled());

    if (c instanceof LocalProjectConfiguration) {
      LocalProjectConfiguration lpc = (LocalProjectConfiguration) c;

      uiInset.setText(String.valueOf(lpc.getUiInset()));// TODO validate input

      // NB order matters here:
      allowWidescreen.setSelected(ToolPreferences.isAllowWidescreen()); // TODO move this off this screen into tool-wide prefernces screen
      updateResolutions();
      resolutionCombo.setSelectedItem(lpc.getResolution());

      // Set selected refresh rate
      if (lpc.getRefreshRate() != null) {
        for (int i = 0; i < refreshRateCombo.getItemCount(); ++i) {
          if (((RefreshRate) refreshRateCombo.getItemAt(i)).refreshRate == lpc.getRefreshRate()) {
            refreshRateCombo.setSelectedIndex(i);
            break;
          }
        }
      }
      windowsVolumeControlCheckBox.setSelected(lpc.isWindowsVolumeControl());

      displayPanel.setVisible(true);
      soundPanel.setVisible(PlatformSupport.getPlatform() == PlatformSupport.Platform.WINDOWS);
    }
    else {
      displayPanel.setVisible(false);
      soundPanel.setVisible(false);
    }
  }

  public void updateModelFromView(ProjectModel model)
  {
    ProjectConfiguration c = model.getEditorConfig();

    c.setLanguage((Language) languageCombo.getSelectedItem());
    c.setSkipOpeningMovie(skipOpeningMovieCheckBox.isSelected());
    c.setWatermark(watermark.getText());
    c.setBillboardsDisabled(disableBillboardsCheckBox.isSelected());

    if (c instanceof LocalProjectConfiguration) {
      LocalProjectConfiguration lpc = (LocalProjectConfiguration) c;

      try {
        lpc.setUiInset(Integer.valueOf(uiInset.getText()));
      }
      catch (NumberFormatException e) {
        lpc.setUiInset(0);
      }

      ToolPreferences.setAllowWidescreen(allowWidescreen.isSelected()); // TODO move this off this screen
      lpc.setResolution((Resolution) resolutionCombo.getSelectedItem());
      lpc.setRefreshRate(((RefreshRate) refreshRateCombo.getSelectedItem()).refreshRate);

//      ToolPreferences.setGooFansUsername(goofansUsername.getText()); // TODO move elsewhere
//      ToolPreferences.setGooFansPassword(new String(goofansPassword.getPassword()));

      lpc.setWindowsVolumeControl(windowsVolumeControlCheckBox.isSelected());
    }
  }

  private void createUIComponents()
  {
    windowsVolumeControlHyperlink = new HyperlinkLabel(GooTool.getTextProvider().getString("options.sound.readme"));
  }
}
