/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.projects;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import com.goofans.gootool.facades.LocalSource;
import com.goofans.gootool.facades.LocalTarget;
import com.goofans.gootool.facades.Source;
import com.goofans.gootool.facades.Target;
import com.goofans.gootool.io.Codec;
import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.io.ImageCodec;
import com.goofans.gootool.model.Resolution;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.Utilities;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LocalProject extends Project
{
  static final String PREF_KEY_SOURCE_DIR = "source_dir";
  static final String PREF_KEY_TARGET_DIR = "target_dir";
  static final String PREF_KEY_PROFILE_FILE = "profile_file";

  static final String PREF_KEY_SCREENWIDTH = "screen_width";
  static final String PREF_KEY_SCREENHEIGHT = "screen_height";
  static final String PREF_KEY_REFRESHRATE = "refresh_rate";
  static final String PREF_KEY_UIINSET = "ui_inset";
  static final String PREF_KEY_WINDOWS_VOLUME_CONTROL = "windows_volume_control";

  private LocalProjectConfiguration savedConfiguration;

  LocalProject(Preferences prefsNode, File storageDir)
  {
    super(prefsNode, storageDir);
  }

  public String getSourceDir()
  {
    return prefsNode.get(PREF_KEY_SOURCE_DIR, null);
  }

  public void setSourceDir(String sourceDir)
  {
    prefsNode.put(PREF_KEY_SOURCE_DIR, sourceDir);
    Utilities.flushPrefs(prefsNode);
  }

  public String getTargetDir()
  {
    return prefsNode.get(PREF_KEY_TARGET_DIR, null);
  }

  public void setTargetDir(String targetDir)
  {
    prefsNode.put(PREF_KEY_TARGET_DIR, targetDir);
    Utilities.flushPrefs(prefsNode);
  }

  public String getProfileFile()
  {
    return prefsNode.get(PREF_KEY_PROFILE_FILE, null);
  }

  public void setProfileFile(String profileFile)
  {
    prefsNode.put(PREF_KEY_PROFILE_FILE, profileFile);
    Utilities.flushPrefs(prefsNode);
  }

  @Override
  public byte[] getProfileBytes() throws IOException
  {
    String profileFilePath = getProfileFile();
    if (profileFilePath == null) return null;

    File profileFile = new File(profileFilePath);
    if (!profileFile.exists()) return null;

    return getCodecForProfile().decodeFile(profileFile);
  }

  @Override
  public void setProfileBytes(byte[] profileBytes) throws IOException
  {
    File profileFile = new File(getProfileFile());
    if (!profileFile.exists()) throw new IOException("Profile not found yet");

    getCodecForProfile().encodeFile(profileFile, profileBytes);
  }

  @Override
  public synchronized ProjectConfiguration getSavedConfiguration()
  {
    if (savedConfiguration == null) {
      savedConfiguration = new LocalProjectConfiguration();
      loadProjectConfiguration(savedConfiguration);
    }
    return savedConfiguration;
  }

  @Override
  protected void loadProjectConfiguration(ProjectConfiguration c)
  {
    if (!(c instanceof LocalProjectConfiguration))
      throw new RuntimeException("LocalProject can only load LocalProjectConfigurations");

    super.loadProjectConfiguration(c);

    LocalProjectConfiguration lpc = (LocalProjectConfiguration) c;

    Resolution configResolution = lpc.getResolution();
    int width;
    int height;
    if (configResolution != null) {
      width = prefsNode.getInt(PREF_KEY_SCREENWIDTH, configResolution.getWidth());
      height = prefsNode.getInt(PREF_KEY_SCREENHEIGHT, configResolution.getHeight());
      lpc.setResolution(Resolution.getResolutionByDimensions(width, height));
    }
    else {
      lpc.setResolution(Resolution.DEFAULT_RESOLUTION);
    }

    lpc.setRefreshRate(prefsNode.getInt(PREF_KEY_REFRESHRATE, 60));
    lpc.setUiInset(prefsNode.getInt(PREF_KEY_UIINSET, lpc.getUiInset()));
    lpc.setWindowsVolumeControl(prefsNode.getBoolean(PREF_KEY_WINDOWS_VOLUME_CONTROL, false));
  }

  @Override
  public void saveConfiguration(ProjectConfiguration c)
  {
    saveProjectConfiguration(c);
    savedConfiguration = null;
  }

  @Override
  protected void saveProjectConfiguration(ProjectConfiguration c)
  {
    if (!(c instanceof LocalProjectConfiguration))
      throw new RuntimeException("LocalProject can only save LocalProjectConfigurations");

    super.saveProjectConfiguration(c);

    LocalProjectConfiguration lpc = (LocalProjectConfiguration) c;


    Resolution resolution = lpc.getResolution();
    if (resolution != null) {
      prefsNode.putInt(PREF_KEY_SCREENWIDTH, resolution.getWidth());
      prefsNode.putInt(PREF_KEY_SCREENHEIGHT, resolution.getHeight());
    }

    prefsNode.putInt(PREF_KEY_REFRESHRATE, lpc.getRefreshRate());
    prefsNode.putInt(PREF_KEY_UIINSET, lpc.getUiInset());
    prefsNode.putBoolean(PREF_KEY_WINDOWS_VOLUME_CONTROL, lpc.isWindowsVolumeControl());

    Utilities.flushPrefs(prefsNode);
  }

  @Override
  public boolean readyToBuild()
  {

    // TODO validate here

    // TODO make sure source and target dirs exist

    //@@TODO
    // check source and target dir exist
    return true;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Source getSource()
  {
    return new LocalSource(new File(getSourceDir()));
  }

  @Override
  public Target getTarget()
  {
    return new LocalTarget(new File(getTargetDir()));
  }

  @Override
  public Codec getCodecForGameXml()
  {
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
      case LINUX:
        return GameFormat.AES_BIN_CODEC;
      case MACOSX:
        return GameFormat.MAC_BIN_CODEC;
    }
    return null;
  }

  @Override
  public Codec getCodecForProfile()
  {
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
      case LINUX:
        return GameFormat.AES_BIN_CODEC;
      case MACOSX:
        return GameFormat.MAC_BIN_CODEC;
    }
    return null;
  }

  @Override
  public ImageCodec getImageCodec()
  {
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
      case LINUX:
        return GameFormat.NULL_IMAGE_CODEC;
      case MACOSX:
        return GameFormat.MAC_IMAGE_CODEC;
    }
    return null;
  }

  @Override
  public String getGameXmlFilename(String baseName)
  {
    return baseName + ".bin"; //NON-NLS
  }

  @Override
  public String getGamePngFilename(String baseName)
  {
    if (PlatformSupport.getPlatform() == PlatformSupport.Platform.MACOSX) {
      return baseName + ".png.binltl"; //NON-NLS
    }
    else {
      return baseName + ".png"; //NON-NLS
    }
  }

  @Override
  public String getGameSoundFilename(String baseName)
  {
    return baseName + ".ogg"; //NON-NLS
  }

  @Override
  public String getGameMusicFilename(String baseName)
  {
    return baseName + ".ogg"; //NON-NLS
  }

  @Override
  public String getGameAnimMovieFilename(String baseName)
  {
    //TODO if linux-64, return binltl64
    return baseName + ".binltl"; //NON-NLS
  }
}