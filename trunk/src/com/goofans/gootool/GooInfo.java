/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

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

  private enum DrawType
  {
    FULL, THUMB, TRANS
  }

  private GooInfo()
  {
  }

  public static void main(String[] args)
  {
    WorldOfGoo wog = null; // non-null if initialised and wog found
    boolean doneSomething = false; // true if we've done something useful and don't need to bomb with a syntax error

    try {
      ArrayList<String> arglist = new ArrayList<String>(Arrays.asList(args));
      log.info("gooinfo: " + arglist);

      ProfileFactory.init();
      if (!ProfileFactory.isProfileFound()) {
        System.err.println("Cannot locate profile file automatically. Run GooTool manually once to detect it.");
        System.exit(1);
      }

      ProfileData profileData = null;
      Profile selectedProfile = null;
      DrawType drawType = DrawType.FULL;

      for (int i = 0, arglistSize = arglist.size(); i < arglistSize; i++) {
        String arg = arglist.get(i);

        if ("-listprofiles".equalsIgnoreCase(arg)) {
          if (profileData == null)
            profileData = ProfileFactory.getProfileData();
          Profile[] profiles = profileData.getProfiles();
          for (int j = 0; j < profiles.length; ++j) {
            Profile profile = profiles[j];
            if (profile != null) {
              System.out.println(j + " " + profile.getName());
            }
          }

          doneSomething = true;
        }
        else if ("-profile".equalsIgnoreCase(arg)) {
          if (i + 1 >= arglistSize) {
            System.err.println("-profile requires an argument (0-" + (ProfileData.MAX_PROFILES - 1) + ")");
            System.exit(1);
          }
          int profileNumber = Integer.parseInt(arglist.get(++i));
          if (profileNumber < 0 || profileNumber >= ProfileData.MAX_PROFILES) {
            System.err.println("-profile requires an argument (0-" + (ProfileData.MAX_PROFILES - 1) + ")");
            System.exit(1);
          }

          if (profileData == null)
            profileData = ProfileFactory.getProfileData();

          selectedProfile = profileData.getProfiles()[profileNumber];
          if (selectedProfile == null) {
            System.err.println("Profile " + profileNumber + " is not active");
            System.exit(1);
          }
//          System.out.println("Selecting profile " + selectedProfile.getName());
        }
        else if ("-dumpProfile".equalsIgnoreCase(arg)) {
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

          doneSomething = true;
        }
        else if ("-drawtype".equalsIgnoreCase(arg)) {
          if (i + 1 >= arglistSize) {
            System.err.println("-drawType requires an argument: full, transparent or thumbnail");
            System.exit(1);
          }
          String drawTypeStr = arglist.get(++i);
          if ("full".equalsIgnoreCase(drawTypeStr)) drawType = DrawType.FULL;
          else if ("transparent".equalsIgnoreCase(drawTypeStr)) drawType = DrawType.TRANS;
          else if ("thumbnail".equalsIgnoreCase(drawTypeStr)) drawType = DrawType.THUMB;
          else {
            System.err.println("-drawType requires an argument: full, transparent or thumbnail");
            System.exit(1);
          }
        }
        else if ("-drawtower".equalsIgnoreCase(arg)) {
          if (selectedProfile == null) {
            System.err.println("No profile selected; use -profile <num> first");
            System.exit(1);
          }

          if (i + 1 >= arglistSize) {
            System.err.println("-drawTower requires a filename argument");
            System.exit(1);
          }

          File file = new File(arglist.get(++i));

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

          if (wog == null) {
            wog = WorldOfGoo.getTheInstance();
            wog.init();

            if (!wog.isWogFound()) {
              System.err.println("World of Goo couldn't be located. Run GooTool first and save.");
              System.exit(1);
            }
          }

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

          doneSomething = true;
        }
        else if ("-version".equalsIgnoreCase(arg)) {
          System.out.println("Version " + Version.RELEASE_FULL);
          DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
          System.out.println("Released " + df.format(Version.RELEASE_DATE));
          System.out.println("Built " + df.format(Version.BUILD_DATE));
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
    System.err.println(" -drawType <full, transparent, thumbnail>");
    System.err.println("                       Selects the drawing type for subsequent commands");

    System.err.println("");
    System.err.println("Commands:");
    System.err.println(" -listProfiles         Lists the available profiles");
    System.err.println(" -dumpProfile          Dumps the profile data");
    System.err.println(" -drawTower <filename> Draws the tower to the given PNG file");
    System.err.println(" -version              Returns GooTool's version number");

    System.exit(1);
  }
}