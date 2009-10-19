package com.goofans.gootool.wog;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.addins.AddinFormatException;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.model.Language;
import com.goofans.gootool.model.Resolution;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.GooTool;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.*;

/**
 * Encapsulates the static data about World of Goo, i.e. path and version.
 * <p/>
 * World of Goo doesn't have any registry entries, so we have to guess at common locations.
 * <p/>
 * It also doesn't have any version data that I can find, so we use the file mtime to determine the version.
 * <p/>
 * // orig version exe 2,191,360
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public abstract class WorldOfGoo
{
  private static final Logger log = Logger.getLogger(WorldOfGoo.class.getName());

  public static final String GOOMOD_EXTENSION = "goomod";
  protected static final String GOOMOD_EXTENSION_WITH_DOT = "." + GOOMOD_EXTENSION;
  private static final String USER_CONFIG_FILE = "properties/config.txt";


  private static final WorldOfGoo theInstance;

  static {
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
        theInstance = new WorldOfGooWindows();
        break;
      case MACOSX:
        theInstance = new WorldOfGooMacOSX();
        break;
      case LINUX:
        theInstance = new WorldOfGooLinux();
        break;
      default:
        theInstance = null;
    }
  }


  private static List<Addin> availableAddins = new LinkedList<Addin>();

  // TODO these should move into a new Preferences class
  static final String PREF_LASTVERSION = "gootool_version";
  static final String PREF_ALLOW_WIDESCREEN = "allow_widescreen";
  static final String PREF_SKIP_OPENING_MOVIE = "skip_opening_movie";
  static final String PREF_WATERMARK = "watermark";
  static final String PREF_LANGUAGE = "language";
  static final String PREF_SCREENWIDTH = "screen_width";
  static final String PREF_SCREENHEIGHT = "screen_height";
  static final String PREF_REFRESHRATE = "refresh_rate";
  static final String PREF_UIINSET = "ui_inset";
  static final String PREF_ADDINS = "addins";
  static final String PREF_WINDOWS_VOLUME_CONTROL = "windows_volume_control";


  protected WorldOfGoo()
  {
  }

  public static WorldOfGoo getTheInstance()
  {
    return theInstance;
  }

  public abstract void init();

  public abstract void init(File path) throws FileNotFoundException;

  public abstract boolean isWogFound();

  public abstract boolean isCustomDirSet();

  public static Preferences getPreferences()
  {
    return Preferences.userNodeForPackage(GooTool.class);
  }

  public abstract File getGameFile(String pathname) throws IOException;

  public abstract File getCustomGameFile(String pathname) throws IOException;

  protected abstract File getAddinInstalledDir() throws IOException;

  protected void updateInstalledAddins()
  {
    availableAddins = new LinkedList<Addin>();

    File addinsDir;
    try {
      addinsDir = getAddinInstalledDir();
    }
    catch (IOException e) {
      log.log(Level.WARNING, "No addinInstalledDir", e);
      return;
    }

//    if (!addinsDir.exists()) {
//      addinsDir.mkdir();
//    }
//    else {
    File[] files = addinsDir.listFiles();

    for (File file : files) {
      if (file.isFile() && file.getName().endsWith(GOOMOD_EXTENSION_WITH_DOT)) {
        try {
          availableAddins.add(AddinFactory.loadAddin(file));
        }
        catch (AddinFormatException e) {
          log.log(Level.SEVERE, "Ignoring invalid addin " + file + "in addins dir", e);
        }
        catch (IOException e) {
          log.log(Level.SEVERE, "Ignoring invalid addin " + file + "in addins dir", e);
        }
      }
    }
//    }
  }

  public Configuration readConfiguration() throws IOException
  {
    Configuration c = new Configuration();
    readGamePreferences(c);
    readPrivateConfig(c);
    return c;
  }

  public void readGamePreferences(Configuration c) throws IOException
  {
    GamePreferences.readGamePreferences(c, getGameFile(USER_CONFIG_FILE));
  }

  public void writeGamePreferences(Configuration c) throws IOException
  {
    GamePreferences.writeGamePreferences(c, getCustomGameFile(USER_CONFIG_FILE));
  }

  public abstract void launch() throws IOException;

  public abstract File getWogDir() throws IOException;

  public abstract void setCustomDir(File customDir) throws IOException;

  public abstract File getCustomDir() throws IOException;

  public abstract boolean isFirstCustomBuild() throws IOException;

  private static void readPrivateConfig(Configuration c)
  {
    Preferences p = getPreferences();

//    String versionStr = p.get(WorldOfGoo.PREF_LASTVERSION, null);
//    if (versionStr != null) {
//      VersionSpec lastVersion = new VersionSpec(versionStr);
    // Here we can put any upgrade stuff
//    }

    c.setAllowWidescreen(p.getBoolean(PREF_ALLOW_WIDESCREEN, c.isAllowWidescreen()));
    c.setSkipOpeningMovie(p.getBoolean(PREF_SKIP_OPENING_MOVIE, c.isSkipOpeningMovie()));
    c.setWatermark(p.get(PREF_WATERMARK, ""));

    String languageStr = p.get(PREF_LANGUAGE, null);
    if (languageStr != null) c.setLanguage(Language.getLanguageByCode(languageStr));

    Resolution configResolution = c.getResolution();
    int width;
    int height;
    if (configResolution != null) {
      width = p.getInt(PREF_SCREENWIDTH, configResolution.getWidth());
      height = p.getInt(PREF_SCREENHEIGHT, configResolution.getHeight());
    }
    else {
      width = 800;
      height = 600;
    }
    c.setResolution(Resolution.getResolutionByDimensions(width, height));
    c.setRefreshRate(p.getInt(PREF_REFRESHRATE, 60));
    c.setUiInset(p.getInt(PREF_UIINSET, c.getUiInset()));

    c.setWindowsVolumeControl(p.getBoolean(PREF_WINDOWS_VOLUME_CONTROL, false));

    String addins = p.get(PREF_ADDINS, null);
    if (addins != null) {
      StringTokenizer tok = new StringTokenizer(addins, ",");
      while (tok.hasMoreTokens()) {
        c.enableAddin(tok.nextToken());
      }
    }
  }


  // ONLY FOR USE BY TEST CASES !!!!!
  public static void DEBUGaddAvailableAddin(Addin a)
  {
    availableAddins.add(a);
  }

  // ONLY FOR USE BY TEST CASES !!!!!
  public static void DEBUGremoveAddinById(String id)
  {
    for (Addin availableAddin : availableAddins) {
      if (availableAddin.getId().equals(id)) {
        availableAddins.remove(availableAddin);
        return;
      }
    }
  }

  public static List<Addin> getAvailableAddins()
  {
    return Collections.unmodifiableList(availableAddins);
  }

  protected abstract File getAddinInstalledFile(String addinId) throws IOException;


  public void installAddin(File addinFile, String addinId) throws IOException
  {
    // Check we don't already have an addin with this ID
    //TODO replace it
    for (Addin availableAddin : availableAddins) {
      if (availableAddin.getId().equals(addinId)) {
        throw new IOException("An addin with id " + addinId + " already exists!");
      }
    }

    File destFile = getAddinInstalledFile(addinId);

    log.log(Level.INFO, "Installing addin " + addinId + " from " + addinFile + " to " + destFile);

    Utilities.copyFile(addinFile, destFile);

    updateInstalledAddins();
  }

  public void uninstallAddin(Addin addin) throws IOException
  {
    File addinFile = addin.getDiskFile();
    log.log(Level.INFO, "Uninstalling addin, deleting " + addinFile);

    if (!addinFile.delete()) {
      throw new IOException("Couldn't delete " + addinFile);
    }

    updateInstalledAddins();
  }

  public abstract File chooseCustomDir(Component mainFrame);


  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
//    init();
//    init(new File("c:\\games\\world of goo"));
    WorldOfGoo worldOfGoo = getTheInstance();
    Configuration c = worldOfGoo.readConfiguration();
    System.out.println("c = " + c);

//    writeConfiguration(c);
  }
}
