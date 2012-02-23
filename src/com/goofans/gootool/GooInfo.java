/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.addins.AddinFormatException;
import com.goofans.gootool.profile.*;
import com.goofans.gootool.util.Version;
import com.goofans.gootool.wog.WorldOfGoo;

/**
 * Command-line interface to some GooTool functions.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
public class GooInfo
{
  private static final Logger log = Logger.getLogger(GooInfo.class.getName());

  private static ProfileData profileData = null;
  private static Profile selectedProfile = null;
  private static DrawType drawType = DrawType.FULL;
  private static ArrayList<String> arglist = null;
  private static int currentArg = 0;
  private static WorldOfGoo worldOfGoo = null;

  private enum DrawType
  {
    FULL, THUMB, TRANS
  }

  private GooInfo()
  {
  }

  public static void main(String[] args)
  {
    boolean doneSomething = false; // true if we've done something useful and don't need to bomb with a syntax error

    try {
      arglist = new ArrayList<String>(Arrays.asList(args));
      log.info("gooinfo: " + arglist);

      ProfileFactory.init();
      if (!ProfileFactory.isProfileFound()) {
        System.err.println("Cannot locate profile file automatically. Run GooTool manually once to detect it.");
        System.exit(1);
      }

      profileData = null;
      selectedProfile = null;
      drawType = DrawType.FULL;

      for (currentArg = 0; currentArg < arglist.size(); currentArg++) {
        String arg = arglist.get(currentArg);

        if ("-version".equalsIgnoreCase(arg)) {
          commandVersion();
          doneSomething = true;
        }
        else if ("-listprofiles".equalsIgnoreCase(arg)) {
          commandListProfiles();
          doneSomething = true;
        }
        else if ("-profile".equalsIgnoreCase(arg)) {
          switchProfile();
        }
        else if ("-dumpProfile".equalsIgnoreCase(arg)) {
          commandDumpProfile();
          doneSomething = true;
        }
        else if ("-drawtype".equalsIgnoreCase(arg)) {
          switchDrawType();
        }
        else if ("-drawtower".equalsIgnoreCase(arg)) {
          commandDrawTower();
          doneSomething = true;
        }
        else if ("-validateaddin".equalsIgnoreCase(arg)) {
          commandValidateAddin();
          doneSomething = true;
        }
        else {
          dieSyntax();
        }
      }

      if (!doneSomething) {
        dieSyntax();
      }
    }
    catch (Throwable t) {
      log.log(Level.SEVERE, "Uncaught exception", t);
      System.err.println("Uncaught exception (" + t.getClass().getName() + "):\n" + t.getLocalizedMessage());
      System.exit(1);
    }
  }

  private static void dieSyntax()
  {
    System.err.println("Syntax: gooinfo [<switches>] <command> [[<switches>] <command> ...]");
    System.err.println("Switches:");
    System.err.println(" -profile <0-2>        Selects the profile for subsequent commands");
    System.err.println(" -drawType <full/transparent/thumbnail>");
    System.err.println("                       Selects the drawing type for subsequent commands");

    System.err.println("");
    System.err.println("Commands:");
    System.err.println(" -version              Returns GooTool's version number");
    System.err.println(" -listProfiles         Lists the available profiles");
    System.err.println(" -dumpProfile          Dumps the profile data");
    System.err.println(" -drawTower <filename> Draws the tower to the given PNG file");
    System.err.println(" -validateAddin <filename>");
    System.err.println("                       Does some basic validation of the goomod file");

    System.exit(1);
  }

  private static synchronized void initProfileData() throws IOException
  {
    if (profileData == null)
      profileData = ProfileFactory.getProfileData();
  }

  private static synchronized void initWorldOfGoo()
  {
    if (worldOfGoo == null) {
      worldOfGoo = WorldOfGoo.getTheInstance();
      worldOfGoo.init();

      if (!worldOfGoo.isWogFound()) {
        System.err.println("World of Goo couldn't be located. Run GooTool first and save.");
        System.exit(1);
      }
    }
  }

  private static void commandVersion()
  {
    System.out.println("Version " + Version.RELEASE_FULL);
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
    System.out.println("Released " + df.format(Version.RELEASE_DATE));
    System.out.println("Built " + df.format(Version.BUILD_DATE));
  }

  private static void commandListProfiles() throws IOException
  {
    initProfileData();
    Profile[] profiles = profileData.getProfiles();
    for (int j = 0; j < profiles.length; ++j) {
      Profile profile = profiles[j];
      if (profile != null) {
        System.out.println(j + " " + profile.getName());
      }
    }
  }

  private static void switchProfile() throws IOException
  {
    if (currentArg + 1 >= arglist.size()) {
      System.err.println("-profile requires an argument (0-" + (ProfileData.MAX_PROFILES - 1) + ")");
      System.exit(1);
    }
    int profileNumber = Integer.parseInt(arglist.get(++currentArg));
    if (profileNumber < 0 || profileNumber >= ProfileData.MAX_PROFILES) {
      System.err.println("-profile requires an argument (0-" + (ProfileData.MAX_PROFILES - 1) + ")");
      System.exit(1);
    }

    initProfileData();

    selectedProfile = profileData.getProfiles()[profileNumber];
    if (selectedProfile == null) {
      System.err.println("Profile " + profileNumber + " is not active");
      System.exit(1);
    }
//          System.out.println("Selecting profile " + selectedProfile.getName());
  }

  private static void commandDumpProfile()
  {
    if (selectedProfile == null) {
      System.err.println("No profile selected; use -profile <num> first");
      System.exit(1);
    }

    System.out.println("name " + selectedProfile.getName());
    System.out.println("flags " + selectedProfile.getFlags());
    System.out.println("playTime " + selectedProfile.getPlayTime());
    System.out.println("levels " + selectedProfile.getLevels());
    for (String skippedLevel : selectedProfile.getSkippedLevels()) {
      System.out.println("skippedLevel " + skippedLevel);
    }
    for (LevelAchievement levelAchievement : selectedProfile.getLevelAchievements()) {
      System.out.println("completedLevel " + levelAchievement.getLevelId() + " " + levelAchievement.getMostBalls() + " " + levelAchievement.getLeastMoves() + " " + levelAchievement.getLeastTime());
    }
    System.out.println("newBalls " + selectedProfile.getNewBalls());
    Tower tower = selectedProfile.getTower();
    System.out.println("towerHeight " + tower.getHeight());
    System.out.println("towerTotalBalls " + tower.getTotalBalls());
    System.out.println("towerUsedNodeBalls " + tower.getUsedNodeBalls());
    System.out.println("towerUsedStrandBalls " + tower.getUsedStrandBalls());
  }

  private static void switchDrawType()
  {
    if (currentArg + 1 >= arglist.size()) {
      System.err.println("-drawType requires an argument: full, transparent or thumbnail");
      System.exit(1);
    }
    String drawTypeStr = arglist.get(++currentArg);
    if ("full".equalsIgnoreCase(drawTypeStr)) drawType = DrawType.FULL;
    else if ("transparent".equalsIgnoreCase(drawTypeStr)) drawType = DrawType.TRANS;
    else if ("thumbnail".equalsIgnoreCase(drawTypeStr)) drawType = DrawType.THUMB;
    else {
      System.err.println("-drawType requires an argument: full, transparent or thumbnail");
      System.exit(1);
    }
  }

  private static void commandDrawTower() throws IOException
  {
    if (selectedProfile == null) {
      System.err.println("No profile selected; use -profile <num> first");
      System.exit(1);
    }

    if (currentArg + 1 >= arglist.size()) {
      System.err.println("-drawTower requires a filename argument");
      System.exit(1);
    }

    File file = new File(arglist.get(++currentArg));

    /* Simple pre-flight check that the file can be created */

    if (file.exists()) {
      if (!file.delete()) {
        System.err.println("Can't delete existing file " + file);
        System.exit(1);
      }
    }

    if (!file.createNewFile()) {
      System.err.println("Can't write to file " + file);
      System.exit(1);
    }

    //noinspection ResultOfMethodCallIgnored
    file.delete(); // Delete to avoid leaving an empty file if the following render fails.

    /* Initialise WoG */

    initWorldOfGoo();

    /* Render the image */

    TowerRenderer tr = new TowerRenderer(selectedProfile.getTower());
    tr.render();

    BufferedImage image = null;
    switch (drawType) {
      case FULL:
        image = tr.getPretty();
        break;
      case THUMB:
        image = tr.getThumbnail();
        break;
      case TRANS:
        image = tr.getFullSize();
        break;
    }

    ImageIO.write(image, "PNG", file);
  }

  private static void commandValidateAddin() throws IOException
  {
    if (currentArg + 1 >= arglist.size()) {
      System.err.println("-validateAddin requires a filename argument");
      System.exit(1);
    }

    File file = new File(arglist.get(++currentArg));

    if (!file.exists()) {
      System.err.println("File " + file + " does not exist");
      System.exit(1);
    }

    try {
      Addin addin = AddinFactory.loadAddin(file);
    }
    catch (AddinFormatException e) {
      log.log(Level.SEVERE, "Unable to read addin", e);
      System.err.println(e.getLocalizedMessage());
      System.exit(2);
    }
  }
}