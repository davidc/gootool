package com.goofans.gootool;

import net.infotrek.util.TextUtil;

import java.util.prefs.Preferences;
import java.util.Random;
import java.util.logging.Logger;

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

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args)
  {
    System.out.println("getGooToolId() = " + getGooToolId());
  }
}
