/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import net.infotrek.util.TextUtil;

import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import java.util.Random;
import java.util.Map;
import java.util.TreeMap;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.io.PrintStream;

import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.VersionSpec;

/**
 * This is specifically for preferences that relate to GooTool's state (e.g. whether translator mode is enabled).
 * It is NOT for preferences that affect game building (such as "skip opening movie") - such things belong in ProjectConfiguration and WorldBuilder.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
@SuppressWarnings({"StaticMethodOnlyUsedInOneClass"})
public class ToolPreferences
{
  private static final Logger log = Logger.getLogger(ToolPreferences.class.getName());

  private static final Preferences PREFS = Preferences.userNodeForPackage(GooTool.class);

  private static final String PREF_GOOTOOL_ID = "gootool_random_id";
  private static final String PREF_IGNORE_UPDATE = "gootool_ignore_update";
  private static final String PREF_MRU_ADDIN_DIR = "gootool_mru_addin_dir";
  private static final String PREF_MRU_TOWER_DIR = "gootool_mru_tower_dir";
  private static final String PREF_WINDOW_POSITION = "gootool_window_position";

  private static final String PREF_GOOFANS_USERNAME = "goofans_username";
  private static final String PREF_GOOFANS_PASSWORD = "goofans_password";
  private static final String PREF_GOOFANS_LOGINOK = "goofans_loginok";

  private static final String PREF_BILLBOARDS_LASTCHECK = "billboard_lastcheck";
  private static final String PREF_ALLOW_WIDESCREEN = "allow_widescreen";

  private static final String PREF_RATINGS = "ratings";
  private static final String RATINGS_SEPARATOR = "|";
  private static final String RATINGS_VALUE_SEPARATOR = "=";

  private ToolPreferences()
  {
  }

  public static synchronized String getGooToolId()
  {
    String id = PREFS.get(PREF_GOOTOOL_ID, null);
    if (id != null) return id;

    // base64 converts each 3  bytes to 4 bytes, so we want to use a multiple of 3 bytes (24 bits).
    // 24 bytes gives us 192 bits, leaving a resulting string length 32
    Random r = new Random();

    byte[] idBytes = new byte[24];
    r.nextBytes(idBytes);

    id = TextUtil.base64Encode(idBytes);

    PREFS.put(PREF_GOOTOOL_ID, id);
    return id;
  }

  /**
   * Returns whether the user has chosen to ignore this update version.
   *
   * @param version The version to check.
   * @return true if the user is ignoring this version.
   */
  public static boolean isIgnoreUpdate(VersionSpec version)
  {
    log.finer("Is ignoring update? " + version);

    String ignoreVersion = PREFS.get(PREF_IGNORE_UPDATE, null);
    log.finer("Current setting: " + ignoreVersion);

    return ignoreVersion != null && ignoreVersion.equals(version.toString());
  }

  /**
   * User does not want to be notified of this version's availability again.
   *
   * @param version the version to ignore
   */
  public static void setIgnoreUpdate(VersionSpec version)
  {
    log.fine("Ignoring update " + version);
    PREFS.put(PREF_IGNORE_UPDATE, version.toString());
    Utilities.flushPrefs(PREFS);
  }

  public static String getMruAddinDir()
  {
    return PREFS.get(PREF_MRU_ADDIN_DIR, null);
  }

  public static void setMruAddinDir(String mruDir)
  {
    PREFS.put(PREF_MRU_ADDIN_DIR, mruDir);
    Utilities.flushPrefs(PREFS);
  }

  public static String getMruTowerDir()
  {
    return PREFS.get(PREF_MRU_TOWER_DIR, null);
  }

  public static void setMruTowerDir(String mruDir)
  {
    PREFS.put(PREF_MRU_TOWER_DIR, mruDir);
    Utilities.flushPrefs(PREFS);
  }

  public static String getWindowPosition()
  {
    return PREFS.get(PREF_WINDOW_POSITION, null);
  }

  public static void setWindowPosition(String windowPosition)
  {
    PREFS.put(PREF_WINDOW_POSITION, windowPosition);
    Utilities.flushPrefs(PREFS);
  }

  public static String getGooFansUsername()
  {
    return PREFS.get(PREF_GOOFANS_USERNAME, null);
  }

  public static void setGooFansUsername(String username)
  {
    PREFS.put(PREF_GOOFANS_USERNAME, username);
    Utilities.flushPrefs(PREFS);
  }

  public static String getGooFansPassword()
  {
    String enc = PREFS.get(PREF_GOOFANS_PASSWORD, null);
    if (enc == null) return null;

    try {
      return new String(TextUtil.base64Decode(enc));
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Base64 encoding exception in password, removing");
      return null;
    }
  }

  public static void setGooFansPassword(String password)
  {
    PREFS.put(PREF_GOOFANS_PASSWORD, TextUtil.base64Encode(password.getBytes()));
    Utilities.flushPrefs(PREFS);
  }

  public static boolean isGooFansLoginOk()
  {
    return PREFS.getBoolean(PREF_GOOFANS_LOGINOK, false);
  }

  public static void setGooFansLoginOk(boolean ok)
  {
    PREFS.putBoolean(PREF_GOOFANS_LOGINOK, ok);
    Utilities.flushPrefs(PREFS);
  }

  public static long getBillboardLastCheck()
  {
    return PREFS.getLong(PREF_BILLBOARDS_LASTCHECK, 0);
  }

  public static void setBillboardLastCheck(long lastCheck)
  {
    PREFS.putLong(PREF_BILLBOARDS_LASTCHECK, lastCheck);
    Utilities.flushPrefs(PREFS);
  }

  public static Map<String, Integer> getRatings()
  {
    String ratingsReg = PREFS.get(PREF_RATINGS, null);
    if (ratingsReg == null) {
      return new TreeMap<String, Integer>();
    }

    StringTokenizer tok = new StringTokenizer(ratingsReg, RATINGS_SEPARATOR);

    Map<String, Integer> ratings = new TreeMap<String, Integer>();

    while (tok.hasMoreTokens()) {
      StringTokenizer tok2 = new StringTokenizer(tok.nextToken(), RATINGS_VALUE_SEPARATOR);
      String addinId = tok2.nextToken();
      String vote = tok2.nextToken();

      ratings.put(addinId, Integer.valueOf(vote));
    }

    return ratings;
  }

  public static void setRatings(Map<String, Integer> ratings)
  {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Integer> rating : ratings.entrySet()) {
      if (sb.length() > 0) sb.append(RATINGS_SEPARATOR);
      sb.append(rating.getKey()).append(RATINGS_VALUE_SEPARATOR).append(rating.getValue());
    }
    PREFS.put(PREF_RATINGS, sb.toString());
    Utilities.flushPrefs(PREFS);
  }

  public static boolean isAllowWidescreen()
  {
    return PREFS.getBoolean(PREF_ALLOW_WIDESCREEN, false);
  }

  public static void setAllowWidescreen(boolean allow)
  {
    PREFS.putBoolean(PREF_ALLOW_WIDESCREEN, allow);
    Utilities.flushPrefs(PREFS);
  }

  /**
   * Prints preferences to the specified output stream, hiding the user's GooFans password.
   * This method is useful for debugging.
   *
   * @param out an output stream.
   * @throws BackingStoreException if the BackingStore cannot be reacehd
   */
  public static void list(PrintStream out) throws BackingStoreException
  {
    String[] prefKeys = PREFS.keys();
    for (String prefKey : prefKeys) {
      out.print(prefKey + "=");
      if (prefKey.equals(PREF_GOOFANS_PASSWORD)) {
        out.println("[hidden]"); //NON-NLS
      }
      else {
        out.println(PREFS.get(prefKey, null));
      }
    }
  }
}
