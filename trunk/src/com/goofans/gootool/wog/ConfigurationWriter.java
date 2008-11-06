package com.goofans.gootool.wog;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.addins.Merger;
import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinInstaller;
import com.goofans.gootool.addins.AddinFormatException;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.Version;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Handles the actual writing of the configuration to the World of Goo directory.
 * <p/>
 * res/movie/2dboyLogo
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ConfigurationWriter
{
  private static final Logger log = Logger.getLogger(ConfigurationWriter.class.getName());

  private List<ConfigurationProgressListener> listeners = new ArrayList<ConfigurationProgressListener>();

  private static final String[] resourceDirs = new String[]{"properties", "res"};

  public ConfigurationWriter()
  {
  }

  public void addListener(ConfigurationProgressListener listener)
  {
    listeners.add(listener);
  }

  public void removeListener(ConfigurationProgressListener listener)
  {
    listeners.remove(listener);
  }

  private void beginStep(String taskDescription, boolean progressAvailable)
  {
    log.log(Level.INFO, "Beginning step " + taskDescription);
    for (ConfigurationProgressListener listener : listeners) {
      listener.beginStep(taskDescription, progressAvailable);
    }
  }

  private void progressStep(float percent)
  {
    for (ConfigurationProgressListener listener : listeners) {
      listener.progressStep(percent);
    }
  }

  public void writeConfiguration(Configuration c) throws IOException
  {
    beginStep("Writing tool preferences", false);
    writePrivateConfig(c);

    beginStep("Copying game files to custom folder", true);
    copyGameFiles();

    beginStep("Writing game preferences", false);
    writeUserConfig(c);

    installAddins(c);
  }

  // Writes the "custom" folder inside WoG. Might take a long time on first run.
  private void copyGameFiles() throws IOException
  {
    File wogDir = WorldOfGoo.getWogDir();
    File customDir = WorldOfGoo.getCustomDir();

    customDir.mkdir();

    /* First build a list of everything to copy, so we have an estimate for the progress bar */
    List<String> filesToCopy = new ArrayList<String>(2500);

    for (String resourceDir : resourceDirs) {
      getFilesInFolder(new File(wogDir, resourceDir), filesToCopy, resourceDir);
    }

    /* Add all files (but not directories) in the root directory */
    for (File file : wogDir.listFiles()) {
      if (file.isFile()) {
        filesToCopy.add(file.getName());
      }
    }

    log.fine(filesToCopy.size() + " files in source directories");

    /* Now copy original files from source directory */

    int copied = 0;

    for (int i = 0; i < filesToCopy.size(); i++) {
      String fileToCopy = filesToCopy.get(i);
      if (i % 50 == 0) {
        progressStep((100f * i) / filesToCopy.size());
      }

      File srcFile = new File(wogDir, fileToCopy);
      File destFile = new File(customDir, fileToCopy);

//      System.out.println(srcFile + " -> " + destFile);

      if (srcFile.isDirectory()) {
        if (!destFile.isDirectory()) {
          if (!destFile.mkdir()) {
            throw new IOException("Couldn't create " + destFile);
          }
          copied++;
        }
      }
      else {
        if (!destFile.exists() || srcFile.lastModified() != destFile.lastModified()) {
//          System.out.println("copying");
          Utilities.copyFile(srcFile, destFile);
          copied++;
        }
      }
    }

    // TODO? remove files/dirs that only exist in dest dir

    log.fine(copied + " files copied");

    progressStep(100f);
  }

  private void getFilesInFolder(File file, List<String> filesToCopy, String dirStem)
  {
    // Copy the directory (mkdir)
    filesToCopy.add(dirStem);

    for (File f : file.listFiles()) {
      if (f.isDirectory()) {
        getFilesInFolder(f, filesToCopy, dirStem + "/" + f.getName());
      }
      else if (f.isFile()) {
        filesToCopy.add(dirStem + "/" + f.getName());
      }
    }
  }

  private void writePrivateConfig(Configuration c) throws IOException
  {
    // Tool preferences
    Preferences p = Preferences.userNodeForPackage(GooTool.class);

    p.put(WorldOfGoo.PREF_LASTVERSION, Version.RELEASE.toString());
    p.putBoolean(WorldOfGoo.PREF_ALLOW_WIDESCREEN, c.isAllowWidescreen());
    p.putBoolean(WorldOfGoo.PREF_SKIP_OPENING_MOVIE, c.isSkipOpeningMovie());
    p.put(WorldOfGoo.PREF_WATERMARK, c.getWatermark());

    p.put(WorldOfGoo.PREF_LANGUAGE, c.getLanguage().getCode());
    // todo what if resolution is null
    p.putInt(WorldOfGoo.PREF_SCREENWIDTH, c.getResolution().getWidth());
    p.putInt(WorldOfGoo.PREF_SCREENHEIGHT, c.getResolution().getHeight());
    p.putInt(WorldOfGoo.PREF_UIINSET, c.getUiInset());
  }

  /*
   * Copies from the main config.txt, writes out to custom/properties/config.txt with changes. 
   */
  private void writeUserConfig(Configuration c) throws IOException
  {
    // Load the users's config.txt
    Document document = XMLUtil.loadDocumentFromFile(new File(WorldOfGoo.getWogDir(), WorldOfGoo.USER_CONFIG_FILE));

    try {
      Node n = (Node) WorldOfGoo.USER_CONFIG_XPATH_LANGUAGE.evaluate(document, XPathConstants.NODE);
      n.setNodeValue(c.getLanguage().getCode());

      n = (Node) WorldOfGoo.USER_CONFIG_XPATH_SCREENWIDTH.evaluate(document, XPathConstants.NODE);
      n.setNodeValue(String.valueOf(c.getResolution().getWidth()));

      n = (Node) WorldOfGoo.USER_CONFIG_XPATH_SCREENHEIGHT.evaluate(document, XPathConstants.NODE);
      n.setNodeValue(String.valueOf(c.getResolution().getHeight()));

      n = (Node) WorldOfGoo.USER_CONFIG_XPATH_UIINSET.evaluate(document, XPathConstants.NODE);
      n.setNodeValue(String.valueOf(c.getUiInset()));
    }
    catch (XPathExpressionException e) {
      throw new IOException("Unable to execute XPath", e);
    }

    try {
      XMLUtil.writeDocumentToFile(document, new File(WorldOfGoo.getCustomDir(), WorldOfGoo.USER_CONFIG_FILE));
    }
    catch (TransformerException e) {
      throw new IOException("Unable to write config file", e);
    }

    /* If we're skipping opening movie, we need to remove res/movie/2dboy */
    if (c.isSkipOpeningMovie()) {
      File movieDir = new File(WorldOfGoo.getCustomDir(), "res/movie/2dboyLogo/");
      Utilities.rmdirAll(movieDir);
    }

    /* If we have a watermark, we need to modify properties/text.xml.bin */
    if (c.getWatermark().length() > 0) {
      File textFile = new File(WorldOfGoo.getCustomDir(), "properties/text.xml.bin");
      try {
        Merger merger = new Merger(textFile, new InputStreamReader(getClass().getResourceAsStream("/watermark.xsl")));
        merger.setTransformParameter("watermark", c.getWatermark());
        merger.merge();
        merger.writeEncoded(textFile);
      }
      catch (TransformerException e) {
        throw new IOException("Unable to merge watermark");
      }
    }
  }

  private void installAddins(Configuration c) throws IOException
  {
    /* we need the addins in reverse order, as the earlier ones are higher priority */

    List<String> addins = c.getEnabledAddins();

    for (int i = addins.size() - 1; i >= 0; --i) {
      String id = addins.get(i);
      beginStep("Merging addin " + id, false);

      List<Addin> availableAddins = WorldOfGoo.getAvailableAddins();
      boolean addinFound = false;
      for (Addin addin : availableAddins) {
        if (addin.getId().equals(id)) {
          try {
            AddinInstaller.installAddin(addin);
          }
          catch (AddinFormatException e) {
            throw new IOException("Addin format exception", e);
          }
          addinFound = true;
          break;
        }
      }
      if (!addinFound) {
        throw new IOException("Couldn't locate addin " + id + " to install");
      }
    }
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    Logger.getLogger("").setLevel(Level.ALL);
    Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);

    WorldOfGoo.init();
    ConfigurationWriter writer = new ConfigurationWriter();

    writer.addListener(new ConfigurationProgressListener()
    {
      @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
      public void beginStep(String taskDescription, boolean progressAvailable)
      {
        System.out.println("beginning " + taskDescription + ", " + progressAvailable);
      }

      @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
      public void progressStep(float percent)
      {
        System.out.println("progress: " + percent);
      }
    });

    Configuration c = WorldOfGoo.readConfiguration();

    c.setSkipOpeningMovie(true);
    c.setWatermark("hi there!");

    for (Addin addin : WorldOfGoo.getAvailableAddins()) {
      System.out.println("addin.getId() = " + addin.getId());
    }

    // Should end up as a football, since earlier is priority
    c.enableAddin("com.2dboy.talic.football");
    c.enableAddin("com.2dboy.talic.basketball");
    c.enableAddin("net.davidc.test.merger");

    writer.writeConfiguration(c);
  }
}
