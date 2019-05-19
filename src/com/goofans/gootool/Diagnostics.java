/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;

import com.goofans.gootool.siteapi.ProfileListRequest;
import com.goofans.gootool.siteapi.VersionCheck;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.ProgressListener;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.Version;
import com.goofans.gootool.wog.WorldOfGoo;

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

  public Diagnostics(File outFile) throws IOException
  {
    this.out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
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
    dumpDirectories();
    dumpLogfiles();
    runConnectivityTests();

    // TODO:
    // WoG config
    // Installed addins and their zip contents

    out.close();
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

  private void dumpDirectories()
  {
    beginStep("Dumping World of Goo directory", false);
    out.println("--- Source World of Goo ---");
    out.println();
    WorldOfGoo wog = WorldOfGoo.getTheInstance();
    if (wog.isWogFound()) {
      try {
        listDir(out, wog.getWogDir());
      }
      catch (IOException e) {
        out.println("Can't list WoG dir:");
        e.printStackTrace(out);
      }
    }
    else {
      out.println("No WoG dir set.");
    }
    out.println();

    beginStep("Dumping custom directory", false);
    out.println("--- Custom World of Goo ---");
    out.println();
    if (wog.isCustomDirSet()) {
      try {
        listDir(out, wog.getCustomDir());
      }
      catch (IOException e) {
        out.println("Can't list custom dir:");
        e.printStackTrace(out);
      }
    }
    else {
      out.println("No custom dir set.");
    }
    out.println();
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
        ProfileListRequest listRequest = new ProfileListRequest();
        List<ProfileListRequest.BackupInstance> backups = listRequest.listBackups();
        out.println("ProfileListRequest succeeded. Number of backups: " + backups.size());
      }
      catch (Exception e) {
        out.println("ProfileListRequest test failed:");
        e.printStackTrace(out);
      }
    }
    else {
      out.println("Skipping ProfileListRequest test as user is not logged into GooFans.");
    }
    out.println();
  }

  private static void listDir(PrintStream out, File wogDir)
  {
    out.println(wogDir + " >>>");
    listDir(out, wogDir, "");
  }

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static void listDir(PrintStream out, File dir, String prefix)
  {
    File[] files = dir.listFiles();

    // Files and unknowns first
    for (File file : files) {
      if (file.isFile()) {
        out.format("%8d f %s%n", file.length(), prefix + file.getName());
      }
      else if (!file.isDirectory()) {
        out.format("%8d ? %s%n", file.length(), prefix + file.getName());
      }
    }

    // Then subdirectories
    for (File file : files) {
      if (file.isDirectory()) {
        out.format("         d %s%n", prefix + file.getName() + "/");
        listDir(out, file, "  " + prefix + file.getName() + "/");
      }
    }
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    WorldOfGoo.getTheInstance().init();

    Diagnostics d = new Diagnostics(System.out);

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
