/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.platform;

import net.infotrek.util.prefs.FilePreferencesFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import com.goofans.gootool.MainController;
import com.goofans.gootool.profile.ProfileData;
import com.goofans.gootool.projects.LocalProject;
import com.goofans.gootool.util.Utilities;

/**
 * Platform support abstraction class. This handles features of the HOST platform, i.e. the platform GooTool is running on.
 * Automatically detects the current platform unless overridden with -Dgootool.platform.
 * <p/>
 * Also handles setting up an alternative preferences store if -preferences &lt;file&gt; is set on command line.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public abstract class PlatformSupport
{
  private static final Logger log = Logger.getLogger(PlatformSupport.class.getName());

  private static final String PROFILE_DAT_FILENAME = "pers2.dat";

  public enum Platform
  {
    WINDOWS, MACOSX, LINUX
  }

  private static Platform platform;
  private static PlatformSupport support;

  static {
    /* Platform forcing via -Dgootool.platform */

    String forcePlatform = System.getProperty("gootool.platform"); //NON-NLS

    if (forcePlatform == null) {
      platform = null;
    }
    else {
      if ("windows".equalsIgnoreCase(forcePlatform)) { //NON-NLS
        platform = Platform.WINDOWS;
      }
      else if ("macosx".equalsIgnoreCase(forcePlatform)) { //NON-NLS
        platform = Platform.MACOSX;
      }
      else if ("linux".equalsIgnoreCase(forcePlatform)) { //NON-NLS
        platform = Platform.LINUX;
      }
      else {
        log.severe("Unknown gootool.platform " + forcePlatform);
        platform = null;
        throw new ExceptionInInitializerError("Invalid gootool.platform " + forcePlatform + ", valid values are: WINDOWS, LINUX, MACOSX");
      }
    }

    if (platform != null) {
      log.warning("Forcing platform: " + platform);
    }
    else {
      String lcOSName = System.getProperty("os.name").toLowerCase(); //NON-NLS
      if (lcOSName.startsWith("mac os x")) { //NON-NLS
        platform = Platform.MACOSX;
      }
      else if (lcOSName.startsWith("windows")) { //NON-NLS
        platform = Platform.WINDOWS;
      }
      else if (lcOSName.startsWith("linux")) { //NON-NLS
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
      default:
        support = null;
    }

    log.fine("Platform detected: " + platform);
  }

  protected PlatformSupport()
  {
  }

  public static Platform getPlatform()
  {
    if (platform == null) throw new RuntimeException("Platform isn't initialised yet");
    return platform;
  }

  public static boolean preStartup(List<String> args)
  {
    String prefsFile = null;

    for (int i = 0; i < args.size(); i++) {
      String arg = args.get(i);

      /* File preferences via -preferences */
      if ("-preferences".equals(arg)) { //NON-NLS
        if (i + 1 >= args.size()) {
          throw new RuntimeException("Must specify a filename when using -preferences");
        }
        args.remove(i);
        prefsFile = args.get(i);
        args.remove(i);
        i--;
      }
    }

    if (prefsFile != null) {
      System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());
      System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, prefsFile);
      log.info("Preferences will be stored in " + FilePreferencesFactory.getPreferencesFile());
    }
    else {
      log.finest("Preferences will be stored using system defaults (" + Preferences.systemRoot().getClass() + ")");
    }

    return support.doPreStartup(args);
  }

  protected abstract boolean doPreStartup(List<String> args);

  public static void startup(MainController mainController)
  {
    support.doStartup(mainController);
  }

  protected abstract void doStartup(MainController mainController);

  public static String[] getProfileSearchPaths()
  {
    return support.doGetProfileSearchPaths();
  }

  protected abstract String[] doGetProfileSearchPaths();

  public static File getToolStorageDirectory() throws IOException
  {
    File dir = support.doGetToolStorageDirectory();
    Utilities.mkdirsOrException(dir);
    return dir;
  }

  protected abstract File doGetToolStorageDirectory() throws IOException;

  public static File chooseLocalTargetDir(Component mainFrame, File defaultFile)
  {
    return support.doChooseLocalTargetDir(mainFrame, defaultFile);
  }

  protected abstract File doChooseLocalTargetDir(Component mainFrame, File defaultFile);

  public static File detectWorldOfGooSource()
  {
    return support.doDetectWorldOfGooSource();
  }

  protected abstract File doDetectWorldOfGooSource();

  public static File detectProfileFile()
  {
    for (String searchPath : getProfileSearchPaths()) {
      String expandedPath = Utilities.expandEnvVars(searchPath);

      if (expandedPath != null) {
        File file = new File(expandedPath, PROFILE_DAT_FILENAME);
        log.finest("Looking for profile at " + file);
        if (ProfileData.isValidProfile(file)) {
          log.info("Found profile through default search of \"" + searchPath + "\" at: " + file);
          return file;
        }
      }
    }
    log.log(Level.INFO, "Couldn't find the user's profile in any default location");

    return null;
  }

  public static void launch(LocalProject project) throws IOException
  {
    File targetDir = new File(project.getTargetDir());
    if (!targetDir.isDirectory()) throw new IOException("Target directory " + targetDir + " isn't a directory");

    support.doLaunch(targetDir);
  }

  protected abstract void doLaunch(File targetDir) throws IOException;
}
