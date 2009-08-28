package com.goofans.gootool.wog;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.BillboardUpdater;
import com.goofans.gootool.addins.*;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.model.Resolution;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.*;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Handles the actual writing of the configuration to the World of Goo directory.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ConfigurationWriterTask extends ProgressIndicatingTask
{
  private static final Logger log = Logger.getLogger(ConfigurationWriterTask.class.getName());

  private static final String[] resourceDirs = new String[]{"properties", "res", "libs"};

  private static final List<String> skippedFiles = Arrays.asList("Thumbs.db");

  private final Configuration configuration;

  public ConfigurationWriterTask(Configuration configuration)
  {
    this.configuration = configuration;
  }

  public void run() throws Exception
  {
    writePrivateConfig(configuration);

    copyGameFiles();

    writeUserConfig(configuration);

    installAddins(configuration);

    log.log(Level.INFO, "Configuration writer work complete");
  }

  // Writes the "custom" folder inside WoG. Might take a long time on first run.
  private void copyGameFiles() throws IOException
  {
    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    File wogDir = worldOfGoo.getWogDir();
    File customDir = worldOfGoo.getCustomDir();
    log.info("Copying game files from " + wogDir + " to " + customDir);

    customDir.mkdir();

    beginStep("Building list of source files", false);

    /* First build a list of everything to copy, so we have an estimate for the progress bar */
    List<String> filesToCopy = new ArrayList<String>(2500);

    if (PlatformSupport.getPlatform() == PlatformSupport.Platform.MACOSX) {
      getFilesInFolder(wogDir, filesToCopy, "");
    }
    else {
      // WINDOWS/LINUX
      for (String resourceDirName : resourceDirs) {
        File resourceDir = new File(wogDir, resourceDirName);
        if (resourceDir.exists()) {
          getFilesInFolder(resourceDir, filesToCopy, resourceDirName);
        }
      }

      /* Add all files (but not directories) in the root directory */
      for (File file : wogDir.listFiles()) {
        if (file.isFile() && !skippedFiles.contains(file.getName())) {
          filesToCopy.add(file.getName());
        }
      }
    }

    log.fine(filesToCopy.size() + " files in source directories");

    // Now truncate what we are going to overwrite or remove anyway.
    // TODO we can also skip files that are overwritten by addins.

    for (int i = 0; i < filesToCopy.size(); i++) {
      String s = filesToCopy.get(i);
      if ((configuration.isSkipOpeningMovie() && s.startsWith("res/movie/2dboyLogo")) // If we're skipping the opening movie
              ) {
        filesToCopy.remove(i);
        i--;
      }
    }
    log.fine(filesToCopy.size() + " files after skipping");

    beginStep("Copying game files to custom folder", true);

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
          Utilities.copyFile(srcFile, destFile);
          copied++;
        }
      }
    }

    // TODO remove files/dirs that only exist in dest dir

    log.fine(copied + " files copied");

    if (PlatformSupport.getPlatform() == PlatformSupport.Platform.MACOSX) {
      // Make the EXE files executable
      for (String exeFilename : WorldOfGooMacOSX.EXE_FILENAMES) {
        File exe = new File(customDir, exeFilename);
        if (exe.exists()) {
          Runtime.getRuntime().exec(new String[]{"chmod", "+x", exe.getAbsolutePath()});
        }
      }

      // Add the Mac icon
      InputStream is = getClass().getResourceAsStream("/customapp.icns");
      OutputStream os = new FileOutputStream(customDir + "/Contents/Resources/gooicon.icns");
      Utilities.copyStreams(is, os);
    }
    else if (PlatformSupport.getPlatform() == PlatformSupport.Platform.LINUX) {
      // Make the script executable
      File script = new File(customDir, WorldOfGooLinux.SCRIPT_FILENAME);
      Runtime.getRuntime().exec(new String[]{"chmod", "+x", script.getAbsolutePath()});

      // Make the EXE files executable
      for (String exeFilename : WorldOfGooLinux.EXE_FILENAMES) {
        File exe = new File(customDir, exeFilename);
        if (exe.exists()) {
          Runtime.getRuntime().exec(new String[]{"chmod", "+x", exe.getAbsolutePath()});
        }
      }
    }

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
      else if (f.isFile() && !skippedFiles.contains(f.getName())) {
        filesToCopy.add(dirStem + "/" + f.getName());
      }
    }
  }

  private void writePrivateConfig(Configuration c)
  {
    beginStep("Writing tool preferences", false);

    // Tool preferences
    Preferences p = Preferences.userNodeForPackage(GooTool.class);
    log.log(Level.FINEST, "ConfigurationWriterTask got p: " + this);

    p.put(WorldOfGoo.PREF_LASTVERSION, Version.RELEASE.toString());
    p.putBoolean(WorldOfGoo.PREF_ALLOW_WIDESCREEN, c.isAllowWidescreen());
    p.putBoolean(WorldOfGoo.PREF_SKIP_OPENING_MOVIE, c.isSkipOpeningMovie());
    p.put(WorldOfGoo.PREF_WATERMARK, c.getWatermark());

    if (c.getLanguage() != null) {
      p.put(WorldOfGoo.PREF_LANGUAGE, c.getLanguage().getCode());
    }

    Resolution resolution = c.getResolution();
    if (resolution != null) {
      p.putInt(WorldOfGoo.PREF_SCREENWIDTH, resolution.getWidth());
      p.putInt(WorldOfGoo.PREF_SCREENHEIGHT, resolution.getHeight());
    }
    p.putInt(WorldOfGoo.PREF_UIINSET, c.getUiInset());

    p.putBoolean(WorldOfGoo.PREF_WINDOWS_VOLUME_CONTROL, c.isWindowsVolumeControl());

    StringBuilder sb = new StringBuilder();
    for (String s : c.getEnabledAddins()) {
      if (sb.length() != 0) sb.append(',');
      sb.append(s);
    }

    p.put(WorldOfGoo.PREF_ADDINS, sb.toString());
  }

  /*
   * Copies from the main config.txt, writes out to custom/properties/config.txt with changes. 
   */
  private void writeUserConfig(Configuration c) throws IOException
  {
    beginStep("Writing game preferences", false);
    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    worldOfGoo.writeGamePreferences(c);

    /* If we're skipping opening movie, we need to remove res/movie/2dboy */
    if (c.isSkipOpeningMovie()) {
      File movieDir = worldOfGoo.getCustomGameFile("res/movie/2dboyLogo/");
      if (movieDir.exists()) {
        Utilities.rmdirAll(movieDir);
      }
    }

    /* If we have a watermark, we need to modify properties/text.xml.bin */
    if (c.getWatermark().length() > 0) {
      File textFile = worldOfGoo.getCustomGameFile("properties/text.xml.bin");
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

    /* Add new irrKlang.dll if Windows volume control enabled */
    if (PlatformSupport.getPlatform() == PlatformSupport.Platform.WINDOWS && configuration.isWindowsVolumeControl()) {
      log.log(Level.FINER, "Copying custom irrKlang.dll");

      File installedIrrKlangFile = worldOfGoo.getCustomGameFile("irrKlang.dll");
      File realIrrKlangFile = worldOfGoo.getCustomGameFile("RealIrrKlang.dll");

      //noinspection ResultOfMethodCallIgnored
      realIrrKlangFile.delete(); // ok to fail if it didn't exist
      if (!installedIrrKlangFile.renameTo(realIrrKlangFile)) {
        throw new IOException("Unable to rename irrKlang.dll to RealIrrKlang.dll");
      }

      Utilities.copyFile(new File("lib\\irrKlang\\irrKlang.dll"), installedIrrKlangFile);
    }
  }

  private void installAddins(Configuration c) throws AddinFormatException
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
          catch (IOException e) {
            throw new AddinFormatException("IOException in " + addin.getName() + ":\n" + e.getMessage(), e);
          }
          catch (AddinFormatException e) {
            throw new AddinFormatException("Addin format exception in " + addin.getName() + ":\n" + e.getMessage(), e);
          }
          addinFound = true;
          break;
        }
      }
      if (!addinFound) {
        throw new AddinFormatException("Couldn't locate addin " + id + " to install");
      }
    }

    if (!ToolPreferences.isBillboardDisable()) {
      log.info("Installing billboards goomod");
      try {
        File addinFile = WorldOfGoo.getTheInstance().getCustomGameFile(BillboardUpdater.BILLBOARDS_GOOMOD_FILENAME);
        if (addinFile.exists()) {
          Addin addin = AddinFactory.loadAddin(addinFile);
          AddinInstaller.installAddin(addin);
        }
      }
      catch (IOException e) {
        throw new AddinFormatException("Couldn't install billboard addin", e);
      }
    }
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "HardcodedFileSeparator", "DuplicateStringLiteralInspection"})
  public static void main(String[] args) throws Exception
  {
    DebugUtil.setAllLogging();

    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    worldOfGoo.init();
    worldOfGoo.setCustomDir(new File("C:\\BLAH\\"));

    Configuration c = worldOfGoo.readConfiguration();
    c.setSkipOpeningMovie(true);
    c.setWatermark("hi there!");

    for (Addin addin : WorldOfGoo.getAvailableAddins()) {
      System.out.println("addin.getId() = " + addin.getId());
    }

    // Remove any installed net.davidc.madscientist.dejavu
    String addinId = "net.davidc.madscientist.dejavu";
    WorldOfGoo.DEBUGremoveAddinById(addinId);

    Addin addin = AddinFactory.loadAddinFromDir(new File("addins/src/net.davidc.madscientist.dejavu"));
    WorldOfGoo.DEBUGaddAvailableAddin(addin);

    // Should end up as a football, since earlier is priority
//    c.enableAddin("com.2dboy.talic.football");
//    c.enableAddin("com.2dboy.talic.basketball");
    c.enableAddin(addinId);

    ConfigurationWriterTask writer = new ConfigurationWriterTask(c);

    writer.addListener(new ProgressListener()
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

    writer.run();
  }
}
