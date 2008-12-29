package com.goofans.gootool.wog;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.addins.AddinFormatException;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.model.Language;
import com.goofans.gootool.model.Resolution;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.GooTool;

import javax.xml.xpath.*;
import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

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
public class WorldOfGoo
{
  private static final Logger log = Logger.getLogger(WorldOfGoo.class.getName());

  private static final String[] SEARCH_PATHS = {"%ProgramFiles%\\WorldOfGoo", "%ProgramFiles%\\World Of Goo",
          "%SystemDrive%\\Program Files\\WorldOfGoo", "%SystemDrive%\\Program Files\\World Of Goo",
          "%SystemDrive%\\Games\\WorldOfGoo", "%SystemDrive%\\Games\\World Of Goo",
          "%HOME%/.PlayOnLinux/wineprefix/WorldOfGoo/drive_c/Program Files/WorldOfGoo", // PlayOnLinux
          "%HOME%/.wine/drive_c/Program Files/WorldOfGoo" // wine
  };

  private static boolean wogFound;
  private static File wogDir;
  private static File addinsDir;
  private static File customDir;

  private static List<Addin> availableAddins = new LinkedList<Addin>();

  private static final String EXE_FILENAME = "WorldOfGoo.exe";
  static final String USER_CONFIG_FILE = "properties/config.txt";
//  static final String GOOTOOL_CONFIG_FILE = "properties/gootool.txt";

  private static final String ADDIN_DIR = "addins";//TODO put this inside custom
//  private static final String CUSTOM_DIR = "custom";

  static final String PREF_LASTVERSION = "gootool_version";
  static final String PREF_ALLOW_WIDESCREEN = "allow_widescreen";
  static final String PREF_SKIP_OPENING_MOVIE = "skip_opening_movie";
  static final String PREF_WATERMARK = "watermark";
  static final String PREF_LANGUAGE = "language";
  static final String PREF_SCREENWIDTH = "screen_width";
  static final String PREF_SCREENHEIGHT = "screen_height";
  static final String PREF_UIINSET = "ui_inset";
  static final String PREF_ADDINS = "addins";

  static final XPathExpression USER_CONFIG_XPATH_LANGUAGE;
  static final XPathExpression USER_CONFIG_XPATH_SCREENWIDTH;
  static final XPathExpression USER_CONFIG_XPATH_SCREENHEIGHT;
  static final XPathExpression USER_CONFIG_XPATH_UIINSET;

  public static final String GOOMOD_EXTENSION = "goomod";
  private static final String GOOMOD_EXTENSION_WITH_DOT = "." + GOOMOD_EXTENSION;
  private static final String PREF_WOG_DIR = "wog_dir";
  private static final String PREF_CUSTOM_DIR = "custom_dir";

  static {
    XPath path = XPathFactory.newInstance().newXPath();
    try {
      USER_CONFIG_XPATH_LANGUAGE = path.compile("/config/param[@name='language']/@value");
      USER_CONFIG_XPATH_SCREENWIDTH = path.compile("/config/param[@name='screen_width']/@value");
      USER_CONFIG_XPATH_SCREENHEIGHT = path.compile("/config/param[@name='screen_height']/@value");
      USER_CONFIG_XPATH_UIINSET = path.compile("/config/param[@name='ui_inset']/@value");
    }
    catch (XPathExpressionException e) {
      throw new ExceptionInInitializerError(e);
    }
  }


  private WorldOfGoo()
  {
  }

  public static boolean isWogFound()
  {
    return wogFound;
  }

  public static boolean isCustomDirSet()
  {
    return customDir != null;
  }

  /**
   * Attempts to locate WoG in various default locations.
   */
  public static void init()
  {
    Preferences p = getPreferences();
    String userWogDir = p.get(PREF_WOG_DIR, null);

    if (userWogDir != null) {
      if (locateWogAtPath(new File(userWogDir))) {
        log.info("Found World of Goo at stored location of \"" + userWogDir + "\" at: " + wogDir);
        return;
      }
    }

    for (String searchPath : SEARCH_PATHS) {
      String newSearchPath = Utilities.expandEnvVars(searchPath);

      if (newSearchPath != null && locateWogAtPath(new File(newSearchPath))) {
        log.info("Found World of Goo through default search of \"" + searchPath + "\" at: " + wogDir);
        return;
      }
    }
  }

  public static Preferences getPreferences()
  {
    return Preferences.userNodeForPackage(GooTool.class);
  }

  private static boolean locateWogAtPath(File searchPath)
  {
    File f = new File(searchPath, EXE_FILENAME);

    log.finest("looking for World of Goo at " + f);
    if (f.exists()) {
      foundWog(searchPath);
      return true;
    }
    return false;
  }

  /* We've found WoG at the given path. Read in some bits */
  private static void foundWog(File searchPath)
  {
    wogFound = true;
    wogDir = searchPath;

    Preferences p = getPreferences();
    p.put(PREF_WOG_DIR, wogDir.getAbsolutePath());

    p = Preferences.userNodeForPackage(GooTool.class);
    String customDirPref = p.get(PREF_CUSTOM_DIR, null);

    try {
      if (customDirPref != null) {
        setCustomDir(new File(customDirPref));
      }
      // try and put custom inside. TODO Don't do this in production, use a chooser always
//      setCustomDir(new File(wogDir, CUSTOM_DIR));
    }
    catch (IOException e) {
      log.log(Level.WARNING, "Can't use saved custom directory " + customDirPref, e);
    }
  }

  private static void updateAvailableAddins()
  {
    availableAddins = new LinkedList<Addin>();

    if (addinsDir == null) {
      return;
    }

    if (!addinsDir.exists()) {
      addinsDir.mkdir();
    }
    else {
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
    }
  }

  /**
   * Attempts to locate WoG in the user-supplied location.
   *
   * @param path Path to exe, excluding exe itself
   * @throws java.io.FileNotFoundException if WorldOfGoo.exe wasn't found at this path
   */
  public static void init(File path) throws FileNotFoundException
  {
    if (!locateWogAtPath(path)) {
      throw new FileNotFoundException("WorldOfGoo.exe was not found at " + path);
    }
    log.info("Found World of Goo through user selection at: " + wogDir);
  }

  public static Configuration readConfiguration() throws IOException
  {
    Configuration c = new Configuration();
    readUserConfig(c);
    readPrivateConfig(c);
    return c;
  }

  public static File getResource(String resourceName) throws IOException
  {
    File file = new File(getWogDir(), resourceName);
    if (!file.exists()) {
      throw new FileNotFoundException();
    }
    return file;
  }

  public static void launch() throws IOException
  {
    File exe = new File(getCustomDir(), EXE_FILENAME);
    log.log(Level.FINE, "Launching " + exe + " in " + customDir);

    // TODO why does this take forever when launchind under IDEA?
    ProcessBuilder pb = new ProcessBuilder(exe.getAbsolutePath());
    pb.directory(customDir);
    pb.start();
  }

  public static File getWogDir() throws IOException
  {
    if (!wogFound) {
      throw new IOException("World of Goo isn't found yet");
    }
    return wogDir;
  }

  public static void setCustomDir(File customDir) throws IOException
  {
    if (customDir.exists() && !customDir.isDirectory()) throw new IOException(customDir + " isn't a directory");
    if (!customDir.exists() && !customDir.mkdir()) throw new IOException("Can't create " + customDir);

    //test write
    File testFile = new File(customDir, "writeTest");
    FileOutputStream os = new FileOutputStream(testFile);
    os.write(65);
    os.close();

    testFile.delete();
    WorldOfGoo.customDir = customDir;

    Preferences p = getPreferences();
    p.put(PREF_CUSTOM_DIR, customDir.getAbsolutePath());

    addinsDir = new File(customDir, ADDIN_DIR);

    updateAvailableAddins();
  }

  public static File getCustomDir() throws IOException
  {
    if (customDir == null) {
      throw new IOException("Custom dir isn't selected yet");
    }
    return customDir;
  }


  /*
   * Loads the defaults from main config.txt. These are overwritten by our preferences if we have any.
   */
  private static void readUserConfig(Configuration c) throws IOException
  {
    // Load the users's config.txt
    Document document = XMLUtil.loadDocumentFromFile(new File(getWogDir(), USER_CONFIG_FILE));

    try {
      String language = USER_CONFIG_XPATH_LANGUAGE.evaluate(document);
      if ("".equals(language)) {
        c.setLanguage(Language.DEFAULT_LANGUAGE);
      }
      else {
        c.setLanguage(Language.getLanguageByCode(language));
        log.fine("Found language " + language);
      }
      int screenWidth = ((Double) USER_CONFIG_XPATH_SCREENWIDTH.evaluate(document, XPathConstants.NUMBER)).intValue();
      int screenHeight = ((Double) USER_CONFIG_XPATH_SCREENHEIGHT.evaluate(document, XPathConstants.NUMBER)).intValue();

      Resolution res = Resolution.getResolutionByDimensions(screenWidth, screenHeight);
      log.fine("Found selected resolution " + res);
      c.setResolution(res);

      int ui_inset = ((Double) USER_CONFIG_XPATH_UIINSET.evaluate(document, XPathConstants.NUMBER)).intValue();
      c.setUiInset(ui_inset);

      log.fine("Found selected ui_inset " + ui_inset);

    }
    catch (XPathExpressionException e) {
      log.log(Level.SEVERE, "Unable to execute XPath", e);
      throw new IOException("Unable to execute XPath: " + e.getLocalizedMessage());
    }
  }

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
    c.setWatermark(p.get(WorldOfGoo.PREF_WATERMARK, ""));

    String languageStr = p.get(WorldOfGoo.PREF_LANGUAGE, null);
    if (languageStr != null) c.setLanguage(Language.getLanguageByCode(languageStr));

    Resolution configResolution = c.getResolution();
    int width;
    int height;
    if (configResolution != null) {
      width = p.getInt(WorldOfGoo.PREF_SCREENWIDTH, configResolution.getWidth());
      height = p.getInt(WorldOfGoo.PREF_SCREENHEIGHT, configResolution.getHeight());
    }
    else {
      width = 800;
      height = 600;
    }
    c.setResolution(Resolution.getResolutionByDimensions(width, height));
    c.setUiInset(p.getInt(WorldOfGoo.PREF_UIINSET, c.getUiInset()));

    String addins = p.get(WorldOfGoo.PREF_ADDINS, null);
    if (addins != null) {
      StringTokenizer tok = new StringTokenizer(addins, ",");
      while (tok.hasMoreTokens()) {
        c.enableAddin(tok.nextToken());
      }
    }
  }


  public static List<Addin> getAvailableAddins()
  {
    return Collections.unmodifiableList(availableAddins);
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

  private static File getAddinInstalledFile(String addinId) throws IOException
  {
    if (addinsDir == null) {
      throw new IOException("Addins directory isn't selected yet");
    }
    return new File(addinsDir, addinId + GOOMOD_EXTENSION_WITH_DOT);
  }

  public static void installAddin(File addinFile, String addinId) throws IOException
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

    WorldOfGoo.updateAvailableAddins();
  }

  public static void uninstallAddin(Addin addin) throws IOException
  {
    File addinFile = addin.getDiskFile();
    log.log(Level.INFO, "Uninstalling addin, deleting " + addinFile);

    if (!addinFile.delete()) {
      throw new IOException("Couldn't delete " + addinFile);
    }

    WorldOfGoo.updateAvailableAddins();
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    init();
    init(new File("c:\\games\\world of goo"));
    Configuration c = readConfiguration();
    System.out.println("c = " + c);

//    writeConfiguration(c);
  }
}
