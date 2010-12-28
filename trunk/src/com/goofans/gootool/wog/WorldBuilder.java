/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.wog;

import com.goofans.gootool.BillboardUpdater;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.GooToolResourceBundle;
import com.goofans.gootool.addins.*;
import com.goofans.gootool.facades.Source;
import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.facades.Target;
import com.goofans.gootool.facades.TargetFile;
import com.goofans.gootool.io.Codec;
import com.goofans.gootool.platform.LinuxSupport;
import com.goofans.gootool.platform.MacOSXSupport;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.projects.*;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.ProgressListener;
import com.goofans.gootool.util.Utilities;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the actual writing of the configuration to the World of Goo directory.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class WorldBuilder extends ProgressIndicatingTask
{
  private static final Logger log = Logger.getLogger(WorldBuilder.class.getName());

  private static final String[] RESOURCE_DIRS = new String[]{"properties", "res", "libs"};

  private static final List<String> SKIPPED_FILES = Arrays.asList("Thumbs.db"); //NON-NLS

  private static final int ESTIMATED_SOURCE_FILES = 2500;

  private static final String IRRKLANG_DLL = "irrKlang.dll";
  private static final String REAL_IRRKLANG_DLL = "RealIrrKlang.dll";
  private static final String INTRO_MOVIE_DIR = "res/movie/2dboyLogo";
  private static final String MAC_ICON_FILE = "Contents/Resources/gooicon.icns";

  private final Project project;

  private List<SourceFile> filesToCopy;
  private final GooToolResourceBundle resourceBundle;
  private final ProjectConfiguration config;
  private final Source source;
  private final Target target;
  private final SourceFile sourceRealRoot;
  private final TargetFile targetRealRoot;

  public WorldBuilder(Project project)
  {
    this.project = project;
    this.config = project.getConfiguration();
    resourceBundle = GooTool.getTextProvider();
    source = project.getSource();
    target = project.getTarget();
    sourceRealRoot = source.getRealRoot();
    targetRealRoot = target.getRealRoot();
  }

  @Override
  public void run() throws Exception
  {
    saveConfig();

    copyGameFiles();

    writeConfig();

    installAddins();

    log.log(Level.INFO, "Configuration writer work complete");
  }

  // Writes the "custom" folder inside WoG. Might take a long time on first run.

  private void copyGameFiles() throws IOException
  {
//    File customDir = worldOfGoo.getCustomDir();

    // TODO:
//    if (wogDir.getCanonicalPath().equals(customDir.getCanonicalPath())) {
//      throw new IOException("Custom directory cannot be the same as the source directory!");
//    }

    log.info("Building from " + source + " to " + target);

    log.fine("Source root: " + sourceRealRoot);
    log.fine("Target root: " + targetRealRoot);

    beginStep(resourceBundle.getString("worldBuilder.step.preparing"), false);

    /*
     * First build a list of everything to copy, so we have an estimate for the progress bar
     */
    filesToCopy = new ArrayList<SourceFile>(ESTIMATED_SOURCE_FILES);

    // TODO: OR project instanceof IosProject
    if (project instanceof LocalProject && PlatformSupport.getPlatform() == PlatformSupport.Platform.MACOSX) {
//      getFilesInFolder(wogDir, filesToCopy, "");
      addFilesToCopy(sourceRealRoot);
    }
    else {
      // WINDOWS/LINUX
      for (String resourceDirName : RESOURCE_DIRS) {
        SourceFile resourceDir = sourceRealRoot.getChild(resourceDirName);
//        File resourceDir = new File(wogDir, resourceDirName);
        if (resourceDir != null) {
          addFilesToCopy(resourceDir);
        }
      }

      /* Add all files (but not directories) in the root directory */
      for (SourceFile file : sourceRealRoot.list()) {
        if (file.isFile() && !SKIPPED_FILES.contains(file.getName())) {
          filesToCopy.add(file);
        }
      }
    }

    // TODO Project needs a getPropertiesDir() and getResDir() as it differs based on target platform. There should be no use of PlatformSupport (host platform) in this class!! 

    log.fine(filesToCopy.size() + " files in source directories");

    /*
     * Now reomve any files that we are going to overwrite or remove anyway.
     */
    // TODO we can also skip files that are overwritten by addins.

    removeSkippedFiles();
    log.fine(filesToCopy.size() + " files after skipping");

    /*
     * Now copy files from source to target.
     */

    beginStep(resourceBundle.getString("worldBuilder.step.copying"), true);

    targetRealRoot.mkdir();

    int copied = 0;

    for (int i = 0; i < filesToCopy.size(); i++) {
      SourceFile srcFile = filesToCopy.get(i);

      if (i % 50 == 0) {
        progressStep((100f * i) / filesToCopy.size());
      }

      TargetFile destFile = targetRealRoot.getChild(srcFile.getFullName());

      if (srcFile.isDirectory()) {
        if (!destFile.isDirectory()) {
          destFile.mkdir();
          copied++;
        }
      }
      else {
        if (!destFile.isFile() || srcFile.lastModified() != destFile.lastModified()) {
          System.out.println(srcFile + " -> " + destFile);
          copyFile(srcFile, destFile);
          copied++;
        }
      }
    }


    // TODO remove files/dirs that only exist in dest dir

    log.fine(copied + " files copied");

    // Some hacks for local projects!

    if (project instanceof LocalProject) {

      // Windows hack: If the user already had a RealIrrKlang.dll in the source directory, they must have manually
      // installed Maks' volume control in the past, so move that to irrKlang.dll (#0000219)
      if (PlatformSupport.getPlatform() == PlatformSupport.Platform.WINDOWS) {
        SourceFile realIrrKlangFile = sourceRealRoot.getChild(REAL_IRRKLANG_DLL);
        if (realIrrKlangFile.isFile()) {
          copyFile(realIrrKlangFile, targetRealRoot.getChild(IRRKLANG_DLL));
        }
      }

      if (PlatformSupport.getPlatform() == PlatformSupport.Platform.MACOSX) {
        // Make the EXE files executable
        for (String exeFilename : MacOSXSupport.EXE_FILENAMES) {
          TargetFile exe = targetRealRoot.getChild(exeFilename);
          if (exe.isFile()) {
            exe.makeExecutable();
          }
        }

        // Add the Mac icon
        InputStream is = getClass().getResourceAsStream("/customapp.icns"); //NON-NLS
        try {
          TargetFile targetFile = targetRealRoot.getChild(MAC_ICON_FILE); //NON-NLS
          OutputStream os = targetFile.write();
          try {
            Utilities.copyStreams(is, os);
          }
          finally {
            os.close();
          }
        }
        finally {
          is.close();
        }
      }
      else if (PlatformSupport.getPlatform() == PlatformSupport.Platform.LINUX) {
        // Make the script executable
        TargetFile script = targetRealRoot.getChild(LinuxSupport.SCRIPT_FILENAME);
        script.makeExecutable();

        // Make the EXE files executable
        for (String exeFilename : LinuxSupport.EXE_FILENAMES) {
          TargetFile exe = targetRealRoot.getChild(exeFilename);
          if (exe.isFile()) {
            exe.makeExecutable();
          }
        }
      }
    }

    progressStep(100f);
  }

  private void copyFile(SourceFile srcFile, TargetFile destFile) throws IOException
  {
    InputStream is = srcFile.read();
    try {
      OutputStream os = destFile.write();
      try {
        Utilities.copyStreams(is, os);
      }
      finally {
        os.close();
      }
    }
    finally {
      is.close();
    }
  }

  private void addFilesToCopy(SourceFile sourceFile)
  {
    // Copy the directory (mkdir)
    filesToCopy.add(sourceFile);

    for (SourceFile f : sourceFile.list()) {
      if (f.isDirectory()) {
        addFilesToCopy(f);
      }
      else if (f.isFile() && !SKIPPED_FILES.contains(f.getName())) {
        filesToCopy.add(f);
      }
    }
  }

  private void removeSkippedFiles()
  {
    for (int i = 0; i < filesToCopy.size(); i++) {
      SourceFile s = filesToCopy.get(i);
      if ((config.isSkipOpeningMovie() && s.getFullName().startsWith(INTRO_MOVIE_DIR)) // If we're skipping the opening movie.  NON-NLS
              ) {
        filesToCopy.remove(i);
        i--;
      }
    }

    // On Mac, no need to copy the icns file since we're going to overwrite it again.
    filesToCopy.remove(sourceRealRoot.getChild(MAC_ICON_FILE)); //NON-NLS
  }

  private void saveConfig()
  {
    beginStep(resourceBundle.getString("worldBuilder.step.saveConfig"), false);

    project.saveConfiguration();
    Project project = ProjectManager.getProjects().get(0); // TODO HACK
    ((LocalProject) project).saveConfiguration();
  }

  /*
   * Copies from the main config.txt, writes out to custom/properties/config.txt with changes. 
   */

  private void writeConfig() throws IOException
  {
    beginStep(resourceBundle.getString("worldBuilder.step.writeConfig"), false);

    TargetFile targetGameRoot = target.getGameRoot();

    GamePreferences.writeGamePreferences(project, config, source, target);

    /* If we're skipping opening movie, we need to remove res/movie/2dboy */
    // TODO this should be done in another step
    if (config.isSkipOpeningMovie()) {
      TargetFile movieDir = targetGameRoot.getChild(INTRO_MOVIE_DIR);
      if (movieDir.isDirectory()) {
        for (TargetFile movieFile : movieDir.list()) {
          movieFile.delete();
        }
        movieDir.delete();
      }
    }

    /* If we have a watermark, we need to modify properties/text.xml.bin */
    if (config.getWatermark().length() > 0) {
      TargetFile textFile = targetGameRoot.getChild(project.getGameXmlFilename("properties/text.xml"));
      InputStreamReader xslReader = new InputStreamReader(getClass().getResourceAsStream("/watermark.xsl"));
      try {
        Codec codec = project.getCodecForGameXml();

        byte[] decodedSource;
        InputStream is = textFile.read();
        try {
          decodedSource = codec.decode(Utilities.readStream(is));
        }
        finally {
          is.close();
        }


        Merger merger = new Merger(decodedSource, xslReader);
        merger.setTransformParameter("watermark", config.getWatermark());
        merger.merge();

        byte[] result = merger.getResult();

        byte[] encodedTarget = codec.encode(result);
        OutputStream os = textFile.write();
        try {
          os.write(encodedTarget);
        }
        finally {
          os.close();
        }
      }
      catch (TransformerException e) {
        throw new IOException("Unable to merge watermark");
      }
      finally {
        xslReader.close();
      }
    }

    /* Add new irrKlang.dll if Windows volume control enabled */
    if (project instanceof LocalProject && PlatformSupport.getPlatform() == PlatformSupport.Platform.WINDOWS && ((LocalProjectConfiguration) config).isWindowsVolumeControl()) {
      log.log(Level.FINER, "Copying custom irrKlang.dll");

      TargetFile installedIrrKlangFile = targetRealRoot.getChild(IRRKLANG_DLL);
      TargetFile realIrrKlangFile = targetRealRoot.getChild(REAL_IRRKLANG_DLL);

      try {
        realIrrKlangFile.delete();
      }
      catch (IOException e) {
        // ok to fail if it didn't exist
      }

      installedIrrKlangFile.renameTo(realIrrKlangFile);

      InputStream is = new FileInputStream(new File("lib\\irrKlang\\irrKlang.dll"));
      try {
        OutputStream os = installedIrrKlangFile.write();
        try {
          Utilities.copyStreams(is, os);
        }
        finally {
          os.close();
        }

      }
      finally {
        is.close();
      }
    }
  }

  private void installAddins() throws AddinFormatException
  {
    AddinInstaller installer = new AddinInstaller(project);

    /* we need the addins in reverse order, as the earlier ones are higher priority */

    List<String> addins = config.getEnabledAddins();

    for (int i = addins.size() - 1; i >= 0; --i) {
      String id = addins.get(i);
      beginStep(resourceBundle.formatString("worldBuilder.step.mergeAddin", id), true);
      progressStep(((addins.size() - 1) - i) / addins.size());

      List<Addin> availableAddins = AddinsStore.getAvailableAddins();
      boolean addinFound = false;
      for (Addin addin : availableAddins) {
        if (addin.getId().equals(id)) {
          try {
            installer.installAddin(addin);
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

    if (!config.isBillboardsDisabled()) {
      beginStep(resourceBundle.getString("worldBuilder.step.installBillboards"), false);
      log.info("Installing billboards goomod");

      try {
        File addinFile = new File(PlatformSupport.getToolStorageDirectory(), BillboardUpdater.BILLBOARDS_GOOMOD_FILENAME);

        if (addinFile.exists()) {
          Addin addin = AddinFactory.loadAddin(addinFile);
          installer.installAddin(addin);
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

    LocalProjectConfiguration c = null;// TODO worldOfGoo.readConfiguration();
    c.setSkipOpeningMovie(true);
    c.setWatermark("hi there!");

    for (Addin addin : AddinsStore.getAvailableAddins()) {
      System.out.println("addin.getId() = " + addin.getId());
    }

    // TODO the following tests an addin from an extracted directory and needs reworking 
    // Remove any installed net.davidc.madscientist.dejavu
    String addinId = "net.davidc.madscientist.dejavu";
//    WorldOfGoo.DEBUGremoveAddinById(addinId);

    Addin addin = AddinFactory.loadAddinFromDir(new File("addins/src/net.davidc.madscientist.dejavu"));
//    WorldOfGoo.DEBUGaddAvailableAddin(addin);


    // Should end up as a football, since earlier is priority
//    c.enableAddin("com.2dboy.talic.football");
//    c.enableAddin("com.2dboy.talic.basketball");
    c.enableAddin(addinId);

    WorldBuilder writer = new WorldBuilder(ProjectManager.simpleInit());

    writer.setParentComponent(null);

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
