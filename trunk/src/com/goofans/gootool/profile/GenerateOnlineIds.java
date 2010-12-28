/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.profile;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectManager;

/**
 * Updates the user's profile file to add online IDs to each profile that is missing one.
 * Uses a fixed 8-character prefix, so auto-generated IDs can be identified and later removed.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GenerateOnlineIds
{
  private static final Random RANDOM = new SecureRandom();
  private static final String MAGIC_PREFIX = "ffff0000";
  private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7',
          '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  private GenerateOnlineIds()
  {
  }

  public static void generateOnlineIds(Project project) throws IOException
  {
    ProfileData profileData = project.getProfileData();

    for (Profile profile : profileData.getProfiles()) {
      if (profile != null && profile.getOnlineId() == null) {
        profile.setOnlineId(generateId());
      }
    }

    project.setProfileBytes(profileData.toData());
  }

  @SuppressWarnings({"MagicNumber"})
  private static String generateId()
  {
    byte[] bytes = new byte[16];
    RANDOM.nextBytes(bytes);

    char[] chars = new char[32];
    for (int i = 0; i < 16; ++i) {
      int theByte = bytes[i];
      chars[i * 2] = HEX_DIGITS[theByte & 0xf];
      chars[i * 2 + 1] = HEX_DIGITS[theByte >> 4 & 0xf];
    }

    for (int i = 0; i < MAGIC_PREFIX.length(); ++i) {
      chars[i] = MAGIC_PREFIX.charAt(i);
    }

    return new String(chars);
  }

  public static void removeGeneratedOnlineIds(Project project) throws IOException
  {
    ProfileData profileData = project.getProfileData();

    for (Profile profile : profileData.getProfiles()) {
      if (profile != null) {
        if (isGeneratedId(profile.getOnlineId())) {
          profile.setOnlineId(null);
        }
      }
    }

    project.setProfileBytes(profileData.toData());
  }

  public static boolean isGeneratedId(String onlineId)
  {
    return onlineId != null && onlineId.startsWith(MAGIC_PREFIX);
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    System.out.println(generateId());
    System.out.println(generateId());
    System.out.println(generateId());
    System.out.println(generateId());
    System.out.println(generateId());
    System.out.println(generateId());

    Project project = ProjectManager.simpleInit();

    generateOnlineIds(project);
    removeGeneratedOnlineIds(project);
    generateOnlineIds(project);
  }
}
