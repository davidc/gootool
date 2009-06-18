package com.goofans.gootool.platform;

import net.infotrek.util.prefs.FilePreferencesFactory;

import com.goofans.gootool.Controller;

import java.util.logging.Logger;
import java.util.prefs.PreferencesFactory;
import java.util.prefs.Preferences;

/**
 * Platform support abstraction class. Automatically detects the current platform unless overridden with -Dgootool.platform.
 * <p/>
 * Also handles setting up an alternative preferences store if -Dgootool.prefs is set.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public abstract class PlatformSupport
{
  private static final Logger log = Logger.getLogger(PlatformSupport.class.getName());

  public enum Platform
  {
    WINDOWS, MACOSX, LINUX
  }

  private static Platform platform;
  private static PlatformSupport support;

  static {
    /* Platform forcing via -Dgootool.platform */

    String forcePlatform = System.getProperty("gootool.platform");

    if (forcePlatform != null) {
      if (forcePlatform.equalsIgnoreCase("windows")) {
        platform = Platform.WINDOWS;
      }
      else if (forcePlatform.equalsIgnoreCase("macosx")) {
        platform = Platform.MACOSX;
      }
      else if (forcePlatform.equalsIgnoreCase("linux")) {
        platform = Platform.LINUX;
      }
      else {
        log.warning("Unknown gootool.platform " + forcePlatform + ", valid values are: WINDOWS, LINUX, MACOSX");
      }
    }

    if (platform != null) {
      log.warning("Forcing platform: " + platform);
    }
    else {
      String lcOSName = System.getProperty("os.name").toLowerCase();
      if (lcOSName.startsWith("mac os x")) {
        platform = Platform.MACOSX;
      }
      else if (lcOSName.startsWith("windows")) {
        platform = Platform.WINDOWS;
      }
      else if (lcOSName.startsWith("linux")) {
        platform = Platform.LINUX;
      }
      else {
        throw new RuntimeException("GooTool does not support your OS, " + lcOSName);
      }
    }

    switch (platform) {
      case WINDOWS:
        support = new WindowsSupport();
        break;
      case MACOSX:
        support = new MacOSXSupport();
        break;
      case LINUX:
        support = new LinuxSupport();
        break;
    }

    log.fine("Platform detected: " + platform);


    /* File preferences via -Dgootool.prefs */

    String prefsFile = System.getProperty("gootool.prefs");
    if (prefsFile != null) {
      System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());
      System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, prefsFile);
      log.info("Preferences will be stored in " + FilePreferencesFactory.getPreferencesFile());
    }
    else {
      log.finest("Preferences will be stored using system defaults (" + Preferences.systemRoot().getClass() + ")");
    }
  }

  protected PlatformSupport()
  {
  }

  public static Platform getPlatform()
  {
    if (platform == null) throw new RuntimeException("Platform isn't initialised yet");
    return platform;
  }


  public static boolean preStartup(String[] args)
  {
    return support.doPreStartup(args);
  }

  protected abstract boolean doPreStartup(String[] args);

  public static void startup(Controller controller)
  {
    support.doStartup(controller);
  }

  protected abstract void doStartup(Controller controller);

  public static String[] getProfileSearchPaths()
  {
    return support.doGetProfileSearchPaths();
  }

  protected abstract String[] doGetProfileSearchPaths();
}
