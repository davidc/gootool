/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.projects;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.MainController;
import com.goofans.gootool.facades.Source;
import com.goofans.gootool.facades.Target;
import com.goofans.gootool.io.Codec;
import com.goofans.gootool.io.ImageCodec;
import com.goofans.gootool.model.Language;
import com.goofans.gootool.profile.ProfileData;
import com.goofans.gootool.util.Utilities;

/**
 * Superclass for all project types handled by GooTool.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public abstract class Project
{
  static final String PREF_KEY_NAME = "name";

  static final String PREF_KEY_LANGUAGE = "language";
  static final String PREF_KEY_SKIP_OPENING_MOVIE = "skip_opening_movie";
  static final String PREF_KEY_WATERMARK = "watermark";
  static final String PREF_KEY_BILLBOARDS_DISABLED = "billboard_disable";
  static final String PREF_KEY_ADDINS = "addins";

  protected final Preferences prefsNode;
  protected final File storageDir;

  public Project(Preferences prefsNode, File storageDir)
  {
    this.prefsNode = prefsNode;
    this.storageDir = storageDir;
  }

  public String getName()
  {
    return prefsNode.get(PREF_KEY_NAME, GooTool.getTextProvider().getString("project.name.default"));
  }

  public void setName(String name)
  {
    prefsNode.put(PREF_KEY_NAME, name);
    Utilities.flushPrefs(prefsNode);
  }

  /**
   * Get the DECODED bytes of the project's profile.
   *
   * @return the decoded bytes of the profile.
   * @throws IOException if the profile could not be loaded.
   */
  public abstract byte[] getProfileBytes() throws IOException;

  /**
   * Pass in UNENCRYPTED bytes
   *
   * @param profileBytes the unencrypted bytes of the profile to store.
   * @throws IOException if the profile could not be loaded.
   */
  public abstract void setProfileBytes(byte[] profileBytes) throws IOException;

  public ProfileData getProfileData() throws IOException
  {
    byte[] profileBytes = getProfileBytes();
    if (profileBytes == null) return null;

    return new ProfileData(profileBytes);
  }

  public boolean isProfileValid()
  {
    try {
      return getProfileData() != null;
    }
    catch (IOException e) {
      return false;
    }
  }

  public abstract ProjectConfiguration getSavedConfiguration();

  protected void loadProjectConfiguration(ProjectConfiguration c)
  {
    if (c == null) throw new RuntimeException("loadProjectConfiguration was passed a null project");

    String languageStr = prefsNode.get(PREF_KEY_LANGUAGE, null);
    if (languageStr != null) c.setLanguage(Language.getLanguageByCode(languageStr));

    c.setSkipOpeningMovie(prefsNode.getBoolean(PREF_KEY_SKIP_OPENING_MOVIE, c.isSkipOpeningMovie()));
    c.setWatermark(prefsNode.get(PREF_KEY_WATERMARK, c.getWatermark()));
    c.setBillboardsDisabled(prefsNode.getBoolean(PREF_KEY_BILLBOARDS_DISABLED, c.isBillboardsDisabled()));

    String addins = prefsNode.get(PREF_KEY_ADDINS, null);
    if (addins != null) {
      c.disableAllAddins();
      StringTokenizer tok = new StringTokenizer(addins, ",");
      while (tok.hasMoreTokens()) {
        c.enableAddin(tok.nextToken());
      }
    }
  }

  public abstract void saveConfiguration(ProjectConfiguration c);

  protected void saveProjectConfiguration(ProjectConfiguration c)
  {
    if (c.getLanguage() != null) {
      prefsNode.put(PREF_KEY_LANGUAGE, c.getLanguage().getCode());
    }

    prefsNode.putBoolean(PREF_KEY_SKIP_OPENING_MOVIE, c.isSkipOpeningMovie());
    prefsNode.put(PREF_KEY_WATERMARK, c.getWatermark());
    prefsNode.putBoolean(PREF_KEY_BILLBOARDS_DISABLED, c.isBillboardsDisabled());

    StringBuilder sb = new StringBuilder();
    for (String s : c.getEnabledAddins()) {
      if (sb.length() != 0) sb.append(',');
      sb.append(s);
    }

    prefsNode.put(PREF_KEY_ADDINS, sb.toString());

    // child is responsible for flushing prefs
  }

  public String toString()
  {
    return getName();
  }

  /**
   * Called by the MainController to ask whether this project is ready to be built. GUI interaction is allowed here, for example to show errors
   * as to why the project cannot be built. Also used for iOS projects to download the initial zipfile.
   *
   * @return
   */
  public abstract boolean readyToBuild(MainController mainController);

  /**
   * Gets the Source representing the source files for this project. The caller is responsible for closing it after use.
   *
   * @return the Source for this project.
   */
  public abstract Source getSource() throws IOException;

  /**
   * Gets the Source representing the target files for this project. The caller is responsible for closing it after use.
   *
   * @return the Target for this project.
   */
  public abstract Target getTarget() throws IOException;

  public abstract Codec getCodecForGameXml();

  // TODO we should not expose this, since it's not even a file on iOS!
  @Deprecated
  public abstract Codec getCodecForProfile();

  public abstract ImageCodec getImageCodec();

  /**
   * Gets the filename of an XML file. May have .bin appended for non-iOS platforms.
   *
   * @param baseName Filename, without trailing .bin.
   * @return The filename that should be used in this project.
   */
  public abstract String getGameXmlFilename(String baseName);

  /**
   * Gets the filename of a PNG file. Will have .png appended, or .png.binltl appended for Mac OS X.
   *
   * @param baseName Filename, without trailing .png but without trailing .binltl.
   * @return Where it is on this installation.
   */
  public abstract String getGamePngFilename(String baseName);

  /**
   * Gets the filename of a sound file (.ogg normally, .aac for iOS).
   *
   * @param baseName Filename, without trailing .ogg.
   * @return The filename on this installation.
   */
  public abstract String getGameSoundFilename(String baseName);

  /**
   * Gets the filename of a music file (.ogg normally, .aifc for iOS).
   *
   * @param baseName Filename, without trailing .ogg.
   * @return The filename on this installation.
   */
  public abstract String getGameMusicFilename(String baseName);

  /**
   * Gets the filename of an animation or movie file (.binltl normally, .binltl64 for Linux 64-bit).
   *
   * @param baseName Filename, without trailing .binltl.
   * @return The filename on this installation.
   */
  public abstract String getGameAnimMovieFilename(String baseName);
}