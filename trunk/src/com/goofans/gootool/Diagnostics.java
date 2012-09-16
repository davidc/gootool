/*
 * Copyright (c) 2008, 2009, 2010, 2011, 2012 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import net.infotrek.util.EncodingUtil;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;

import com.goofans.gootool.facades.Source;
import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.facades.Target;
import com.goofans.gootool.projects.IosProject;
import com.goofans.gootool.projects.LocalProject;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.siteapi.ProfileListRequest;
import com.goofans.gootool.siteapi.RatingListRequest;
import com.goofans.gootool.siteapi.VersionCheck;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.ProgressListener;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.Version;

/**
 * Produces a file with a diagnostic information.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
@SuppressWarnings({"HardCodedStringLiteral", "StringConcatenation", "DuplicateStringLiteralInspection"})
public class Diagnostics extends ProgressIndicatingTask
{
  PrintStream out;
  private static final int LOGFILE_BYTES = 8192;

  public Diagnostics(PrintStream out)
  {
    this.out = out;
  }

  @Override
  public void run()
  {
    beginStep("Producing report", false);

    out.println("=== GooTool Diagnostic Report ===");
    out.println();

    dumpGooTool();
    dumpPreferences();
    dumpJavaEnvironment();
    dumpOSEnvironment();
    dumpProjects();
    dumpLogfiles();
    runConnectivityTests();

    // TODO:
    // WoG config
    // Installed addins and their zip contents

    // TODO project configuration
  }

  private void dumpGooTool()
  {
    out.println("--- GooTool ---");
    out.println();
    out.println("Release: " + Version.RELEASE_FULL + " (" + Version.RELEASE_DATE + ")");
    out.println("Build: " + Version.BUILD_DATE + " by " + Version.BUILD_USER + " using " + Version.BUILD_JAVA + " on " + Version.BUILD_OS);
    out.println();
  }

  private void dumpPreferences()
  {
    beginStep("Dumping prefs", false);
    out.println("--- Tool Preferences ---");
    out.println();
    try {
      ToolPreferences.list(out);
    }
    catch (BackingStoreException e) {
      e.printStackTrace(out);
    }
    out.println();
  }

  private void dumpJavaEnvironment()
  {
    beginStep("Dumping Java environment", false);
    out.println("--- Java environment ---");
    out.println();
    out.println("Version: " + System.getProperty("java.version") + " from " + System.getProperty("java.vendor") + " in " + System.getProperty("java.home"));
    out.println("OS: " + System.getProperty("os.name") + " version " + System.getProperty("os.version") + " on " + System.getProperty("os.arch"));
    out.println();
    out.println("System properties:");
    System.getProperties().list(out);
    out.println();
  }

  private void dumpOSEnvironment()
  {
    beginStep("Dumping OS environment", false);
    out.println("--- OS Environment ---");
    out.println();
    Map<String, String> env = System.getenv();
    for (Map.Entry<String, String> envEntry : env.entrySet()) {
      out.println(envEntry.getKey() + "=" + envEntry.getValue());
    }
    out.println();
  }

  private void dumpProjects()
  {
    beginStep("Dumping projects", false);
    out.println("--- Projects ---");
    //TODO current Project

    List<Project> projects = ProjectManager.getProjects();

    for (int i = 0; i < projects.size(); i++) {
      Project project = projects.get(i);
      if (project != null) {
        out.println("--- Project " + i + " of type " + project.getClass().getName() + " ---");
        out.println();
        out.println("Name: " + project.getName());
        out.println("Codec for Game XML: " + project.getCodecForGameXml().getClass().getName());
        out.println("Codec for Images: " + project.getImageCodec().getClass().getName());
        out.println("Codec for Profile: " + project.getCodecForProfile().getClass().getName());
        out.println("Filename for XML: " + project.getGameXmlFilename("base"));
        out.println("Filename for PNG: " + project.getGamePngFilename("base"));
        out.println("Filename for Music: " + project.getGameMusicFilename("base"));
        out.println("Filename for Sound: " + project.getGameSoundFilename("base"));
        out.println("Filename for Anim/Movie: " + project.getGameAnimMovieFilename("base"));
        try {
          out.println("Profile bytes: " + EncodingUtil.bytesToStringUtf8(project.getProfileBytes())); // TODO if possible, only do this if we've cached iOS password
        }
        catch (IOException e) {
          out.println("Unable to get profile bytes:");
          e.printStackTrace(out);
        }
        out.println();

        if (project instanceof LocalProject) {
          LocalProject localProject = (LocalProject) project;
          out.println("(LocalProject) Profile file: " + localProject.getProfileFile());
          out.println("(LocalProject) Source dir: " + localProject.getSourceDir());
          out.println("(LocalProject) Target dir: " + localProject.getTargetDir());
        }
        else if (project instanceof IosProject) {
          IosProject iosProject = (IosProject) project;
          out.println("(IosProject) Host:  " + iosProject.getHost());
          out.println("(IosProject) Password: " + (iosProject.getPassword() == null ? "unset" : "set"));
        }
        out.println();

        out.println("--- Active configuration for project " + i + " ---");
        out.println();
        out.println(project.getSavedConfiguration());
        out.println();

        try {
          Source source = project.getSource();
          try {
            out.println("--- Source for project " + i + "---");
            out.println();
            out.println("Source: " + source);
            out.println("Real root: " + source.getRealRoot());
            out.println("Game root: " + source.getGameRoot());

            listDir(source.getRealRoot(), "");
          }
          finally {
            source.close();
          }
        }
        catch (IOException e) {
          out.println("Warning: unable to get or close source");
          e.printStackTrace(out);
        }
        out.println();

        // TODO dump target as well
        try {
          Target target = project.getTarget();
          try {
            out.println("Target: " + target);
          }
          finally {
            target.close();
          }
        }
        catch (IOException e) {
          out.println("Warning: unable to get or close target");
          e.printStackTrace(out);
        }
      }
    }
  }

  private void dumpLogfiles()
  {
    beginStep("Dumping logfiles", false);

    String tmpDir = System.getProperty("java.io.tmpdir");
    if (tmpDir == null) {
      tmpDir = System.getProperty("user.home");
    }

    for (int i = 0; i < 10; ++i) {

      File logFile = new File(tmpDir, "gootool" + i + ".log");
      if (logFile.exists()) {
        try {
          FileInputStream is = new FileInputStream(logFile);
          if (logFile.length() > LOGFILE_BYTES) {
            out.println("--- " + logFile.getAbsolutePath() + " (last " + LOGFILE_BYTES + " of " + logFile.length() + " bytes) ---");
            //noinspection ResultOfMethodCallIgnored
            is.skip(logFile.length() - LOGFILE_BYTES);
          }
          else {
            out.println("--- " + logFile.getAbsolutePath() + " (all " + logFile.length() + " bytes) ---");
          }
          out.println();
          Utilities.copyStreams(is, out);
          out.println();
        }
        catch (IOException e) {
          out.println("Can't dump logfile " + logFile.getAbsolutePath() + ":");
          e.printStackTrace(out);
        }
      }
    }
  }

  private void runConnectivityTests()
  {
    beginStep("Running connectivity tests", false);

    out.println("--- Connectivity test ---");
    out.println();
    try {
      VersionCheck versionCheck = new VersionCheck(null, false);
      versionCheck.runUpdateCheck();
      out.println("VersionCheck succeeded. isUpToDate? " + versionCheck.isUpToDate());
    }
    catch (Exception e) {
      out.println("VersionCheck test failed:");
      e.printStackTrace(out);
      out.println();
    }

    if (ToolPreferences.isGooFansLoginOk()) {
      try {
        ProfileListRequest backupsRequest = new ProfileListRequest();
        List<ProfileListRequest.BackupInstance> backups = backupsRequest.listBackups();
        out.println("ProfileListRequest succeeded. Number of backups: " + backups.size());
      }
      catch (Exception e) {
        out.println("ProfileListRequest test failed:");
        e.printStackTrace(out);
      }

      try {
        RatingListRequest ratingsRequest = new RatingListRequest();
        Map<String, Integer> ratings = ratingsRequest.getRatings();
        out.println("RatingListRequest succeeded. Number of ratings: " + ratings.size());
      }
      catch (Exception e) {
        out.println("RatingListRequest test failed:");
        e.printStackTrace(out);
      }

    }
    else {
      out.println("Skipping ProfileListRequest and RatingListRequest tests as user is not logged into GooFans.");
    }
    out.println();
  }

  @SuppressWarnings({"HardcodedFileSeparator"})
  private void listDir(SourceFile dir, String prefix)
  {
    List<SourceFile> files = dir.list();

    // Files and unknowns first
    for (SourceFile file : files) {
      if (file.isFile()) {
        out.format("%8d f %s%n", file.getSize(), prefix + file.getName());
      }
      else if (!file.isDirectory()) {
        out.format("%8d ? %s%n", file.getSize(), prefix + file.getName());
      }
    }

    // Then subdirectories
    for (SourceFile file : files) {
      if (file.isDirectory()) {
        out.format("         d %s%n", prefix + file.getName() + "/");
        listDir(file, "  " + prefix + file.getName() + "/");
      }
    }
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {

    Diagnostics d = new Diagnostics(System.out);

    d.setParentComponent(null);

    d.addListener(new ProgressListener()
    {
      public void beginStep(String taskDescription, boolean progressAvailable)
      {
        System.out.println("BEGIN: " + taskDescription);
      }

      public void progressStep(float percent)
      {
      }
    });

    d.run();
  }
}
