package com.goofans.gootool.wog;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.addins.AddinFormatException;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.model.Language;
import com.goofans.gootool.model.Resolution;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.XMLUtil;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.*;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Encapsulates the static data about World of Goo, i.e. path and version.
 * <p/>
 * World of Goo doesn't have any registry entries, so we have to guess at common locations.
 * <p/>
 * It also doesn't have any version data that I can find, so we use the file mtime to determine the version.
 * <p/>
 * // orig version exe 2,191,360
 *
 * @author David Croft (david.croft@infotrek.net)
 * @version $Id$
 */
public class WorldOfGoo
{
  private static final Logger log = Logger.getLogger(WorldOfGoo.class.getName());

  private static final String[] SEARCH_PATHS = {"%ProgramFiles%\\WorldOfGoo", "%ProgramFiles%\\World Of Goo",
          "%SystemDrive%\\Program Files\\WorldOfGoo", "%SystemDrive%\\Program Files\\World Of Goo",
          "%SystemDrive%\\Games\\WorldOfGoo", "%SystemDrive%\\Games\\World Of Goo"
  };

  private static boolean wogFound;
  private static File wogDir;
  private static File addinsDir;

  private static List<Addin> availableAddins;

  private static final String EXE_FILENAME = "WorldOfGoo.exe";
  private static final String USER_CONFIG_FILE = "properties/config.txt";
  private static final String GOOTOOL_CONFIG_FILE = "properties/gootool.txt";

  private static final String ADDIN_DIR = "addins";

  private static final String PROP_ALLOW_WIDESCREEN = "allow_widescreen";
  private static final String PROP_SKIP_OPENING_MOVIE = "skip_opening_movie";
  private static final String PROP_WATERMARK = "watermark";

  private static final XPathExpression USER_CONFIG_XPATH_LANGUAGE;
  private static final XPathExpression USER_CONFIG_XPATH_SCREENWIDTH;
  private static final XPathExpression USER_CONFIG_XPATH_SCREENHEIGHT;
  private static final XPathExpression USER_CONFIG_XPATH_UIINSET;

  public static final String GOOMOD_EXTENSION = "goomod";
  private static final String GOOMOD_EXTENSION_WITH_DOT = "." + GOOMOD_EXTENSION;

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

  /**
   * Attempts to locate WoG in various default locations.
   */
  public static void init()
  {
    for (String searchPath : SEARCH_PATHS) {
//      searchPath = searchPath.replace("%ProgramFiles%", System.getenv("ProgramFiles"));
//      searchPath = searchPath.replace("%SystemDrive%", System.getenv("SystemDrive"));
      String newSearchPath = Utilities.expandEnvVars(searchPath);

      if (locateWogAtPath(new File(newSearchPath))) {
        log.info("Found WoG through default search of \"" + searchPath + "\" at: " + wogDir);
        return;
      }
    }
  }

  private static boolean locateWogAtPath(File searchPath)
  {
    File f = new File(searchPath, EXE_FILENAME);

    log.finest("looking for wog at " + f);
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

    addinsDir = new File(wogDir, ADDIN_DIR);

    updateAvailableAddins();
  }

  public static void updateAvailableAddins()
  {
    availableAddins = new LinkedList<Addin>();

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
    log.info("Found WoG through user selection at: " + wogDir);
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
    File exe = new File(getWogDir(), EXE_FILENAME);
    Runtime.getRuntime().exec(exe.getAbsolutePath(), null, wogDir);
  }

  public static File getWogDir() throws IOException
  {
    if (!wogFound) {
      throw new IOException("WoG isn't found yet");
    }
    return wogDir;
  }

  public static void writeConfiguration(Configuration c) throws IOException
  {
    writeUserConfig(c);
    writePrivateConfig(c);
  }


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
        log.info("Found language " + language);
      }
      int screenWidth = ((Double) USER_CONFIG_XPATH_SCREENWIDTH.evaluate(document, XPathConstants.NUMBER)).intValue();
      int screenHeight = ((Double) USER_CONFIG_XPATH_SCREENHEIGHT.evaluate(document, XPathConstants.NUMBER)).intValue();

      Resolution res = Resolution.getResolutionByDimensions(screenWidth, screenHeight);
      log.info("Found selected resolution " + res);
      c.setResolution(res);

      int ui_inset = ((Double) USER_CONFIG_XPATH_UIINSET.evaluate(document, XPathConstants.NUMBER)).intValue();
      c.setUiInset(ui_inset);

      log.info("Found selected ui_inset " + ui_inset);

    }
    catch (XPathExpressionException e) {
      throw new IOException("Unable to execute XPath", e);
    }
  }

  private static void readPrivateConfig(Configuration c) throws IOException
  {
    // Load our private parameters out of our own config file
    Properties p = new Properties();
    File toolConfig = new File(getWogDir(), GOOTOOL_CONFIG_FILE);
    if (toolConfig.exists()) {
      FileInputStream is = new FileInputStream(toolConfig);
      p.load(is);
      is.close();
      c.setAllowWidescreen(Boolean.valueOf(p.getProperty(PROP_ALLOW_WIDESCREEN)));
      c.setSkipOpeningMovie(Boolean.valueOf(p.getProperty(PROP_SKIP_OPENING_MOVIE)));
      c.setWatermark(p.getProperty(PROP_WATERMARK));
    }
    else {
      c.setAllowWidescreen(false);
      c.setSkipOpeningMovie(false);
      c.setWatermark("");
    }
  }

  private static void writeUserConfig(Configuration c) throws IOException
  {
    // Load the users's config.txt
    Document document = XMLUtil.loadDocumentFromFile(new File(getWogDir(), USER_CONFIG_FILE));

    try {
      Node n = (Node) USER_CONFIG_XPATH_LANGUAGE.evaluate(document, XPathConstants.NODE);
      n.setNodeValue(c.getLanguage().getCode());

      n = (Node) USER_CONFIG_XPATH_SCREENWIDTH.evaluate(document, XPathConstants.NODE);
      n.setNodeValue(String.valueOf(c.getResolution().getWidth()));

      n = (Node) USER_CONFIG_XPATH_SCREENHEIGHT.evaluate(document, XPathConstants.NODE);
      n.setNodeValue(String.valueOf(c.getResolution().getHeight()));

      n = (Node) USER_CONFIG_XPATH_UIINSET.evaluate(document, XPathConstants.NODE);
      n.setNodeValue(String.valueOf(c.getUiInset()));
    }
    catch (XPathExpressionException e) {
      throw new IOException("Unable to execute XPath", e);
    }

    try {
      XMLUtil.writeDocumentToFile(document, new File(getWogDir(), USER_CONFIG_FILE + ".new"));
    }
    catch (TransformerException e) {
      throw new IOException("Unable to write config file", e);
    }

  }

  private static void writePrivateConfig(Configuration c) throws IOException
  {
    // Tool properties
    Properties p = new Properties();
    p.setProperty(PROP_ALLOW_WIDESCREEN, String.valueOf(c.isAllowWidescreen()));
    p.setProperty(PROP_SKIP_OPENING_MOVIE, String.valueOf(c.isSkipOpeningMovie()));
    p.setProperty(PROP_WATERMARK, c.getWatermark());

    FileOutputStream os = new FileOutputStream(new File(getWogDir(), GOOTOOL_CONFIG_FILE));
    p.store(os, "GooTool config");
    os.close();
  }

  public static List<Addin> getAvailableAddins()
  {
    return availableAddins;
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    init();
    init(new File("c:\\games\\world of goo"));
    Configuration c = readConfiguration();
    System.out.println("c = " + c);

    writeConfiguration(c);
  }

  private static File getAddinInstalledFile(String addinId)
  {
    return new File(addinsDir, addinId + GOOMOD_EXTENSION_WITH_DOT);
  }

  public static void installAddin(File addinFile, String addinId) throws IOException
  {
    // Check we don't already have an addin with this ID

    for (Addin availableAddin : availableAddins) {
      if (availableAddin.getId().equals(addinId)) {
        throw new IOException("An addin with id " + addinId + " already exists!");
      }
    }

    File destFile = getAddinInstalledFile(addinId);

    log.log(Level.INFO, "Installing addin " + addinId + " to " + addinFile);

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
}
