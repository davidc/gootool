package com.goofans.gootool;

import net.infotrek.util.TextUtil;

import java.util.prefs.Preferences;
import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

import com.goofans.gootool.util.VersionSpec;

/**
 * This is specifically for preferences that relate to GooTool's state (e.g. whether translator mode is enabled).
 * It is NOT for preferences that affect game building (such as "skip opening movie") - such things belong in Configuration and ConfigurationWriterTask.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ToolPreferences
{
  private static final Logger log = Logger.getLogger(ToolPreferences.class.getName());

  private static final Preferences PREFS = Preferences.userNodeForPackage(GooTool.class);

  private static final String PREF_GOOTOOL_ID = "gootool_random_id";
  private static final String PREF_IGNORE_UPDATE = "gootool_ignore_update";
  private static final String PREF_L10N_MODE = "gootool_l10n_enabled";
  private static final String PREF_MRU_ADDIN_DIR = "gootool_mru_addin_dir";
  private static final String PREF_MRU_TOWER_DIR = "gootool_mru_tower_dir";

  private static final String PREF_WOG_DIR = "wog_dir";
  private static final String PREF_CUSTOM_DIR = "custom_dir";

  private static final String PREF_GOOFANS_USERNAME = "goofans_username";
  private static final String PREF_GOOFANS_PASSWORD = "goofans_password";
  private static final String PREF_GOOFANS_LOGINOK = "goofans_loginok";

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

    if (ignoreVersion == null) return false;

    return ignoreVersion.equals(version.toString());
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
  }

  public static boolean isL10nEnabled()
  {
    return PREFS.getBoolean(PREF_L10N_MODE, false);
  }

  public static void setL10nEnabled(boolean enabled)
  {
    PREFS.putBoolean(PREF_L10N_MODE, enabled);
  }

  public static String getMruAddinDir()
  {
    return PREFS.get(PREF_MRU_ADDIN_DIR, null);
  }

  public static void setMruTowerDir(String mruDir)
  {
    PREFS.put(PREF_MRU_TOWER_DIR, mruDir);
  }

  public static String getMruTowerDir()
  {
    return PREFS.get(PREF_MRU_TOWER_DIR, null);
  }

  public static void setMruAddinDir(String mruDir)
  {
    PREFS.put(PREF_MRU_ADDIN_DIR, mruDir);
  }

  public static String getWogDir()
  {
    return PREFS.get(PREF_WOG_DIR, null);
  }

  public static void setWogDir(String wogDir)
  {
    PREFS.put(PREF_WOG_DIR, wogDir);
  }

  public static String getCustomDir()
  {
    return PREFS.get(PREF_CUSTOM_DIR, null);
  }

  public static void setCustomDir(String customDir)
  {
    PREFS.put(PREF_CUSTOM_DIR, customDir);
  }

  public static String getGooFansUsername()
  {
    return PREFS.get(PREF_GOOFANS_USERNAME, null);
  }

  public static void setGooFansUsername(String username)
  {
    PREFS.put(PREF_GOOFANS_USERNAME, username);
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
  }

  public static boolean isGooFansLoginOk()
  {
    return PREFS.getBoolean(PREF_GOOFANS_LOGINOK, false);
  }

  public static void setGooFansLoginOk(boolean ok)
  {
    PREFS.putBoolean(PREF_GOOFANS_LOGINOK, ok);
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args)
  {
    System.out.println("getGooToolId() = " + getGooToolId());
  }
}
