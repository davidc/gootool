/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
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
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;

import com.goofans.gootool.Controller;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.model.Language;
import com.goofans.gootool.model.Resolution;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.ui.HyperlinkLabel;
import com.goofans.gootool.wog.WorldOfGoo;

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
  private JTextField goofansUsername;
  private JPasswordField goofansPassword;
  private JButton gooFansLoginButton;
  private JCheckBox windowsVolumeControlCheckBox;
  private JPanel soundPanel;
  private HyperlinkLabel windowsVolumeControlHyperlink;
  private JCheckBox disableBillboardsCheckBox;
  private JComboBox refreshRateCombo;

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

    gooFansLoginButton.addActionListener(controller);
    gooFansLoginButton.setActionCommand(Controller.CMD_GOOFANS_LOGIN);

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
    else {
      soundPanel.setVisible(false);
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

    // Set selected refresh rate
    for (int i = 0; i < refreshRateCombo.getItemCount(); ++i) {
      if (((RefreshRate) refreshRateCombo.getItemAt(i)).refreshRate == c.getRefreshRate()) {
        refreshRateCombo.setSelectedIndex(i);
        break;
      }
    }

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

    goofansUsername.setText(ToolPreferences.getGooFansUsername());
    goofansPassword.setText(ToolPreferences.getGooFansPassword());

    windowsVolumeControlCheckBox.setSelected(c.isWindowsVolumeControl());

    disableBillboardsCheckBox.setSelected(ToolPreferences.isBillboardDisable());
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
    c.setRefreshRate(((RefreshRate)refreshRateCombo.getSelectedItem()).refreshRate);

    ToolPreferences.setGooFansUsername(goofansUsername.getText());
    ToolPreferences.setGooFansPassword(new String(goofansPassword.getPassword()));

    c.setWindowsVolumeControl(windowsVolumeControlCheckBox.isSelected());

    ToolPreferences.setBillboardDisable(disableBillboardsCheckBox.isSelected());
  }

  private void createUIComponents()
  {
    windowsVolumeControlHyperlink = new HyperlinkLabel(GooTool.getTextProvider().getString("options.sound.readme"));
  }
}
