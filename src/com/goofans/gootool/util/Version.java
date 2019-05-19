/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.util;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.Locale;
import java.text.SimpleDateFormat;

/**
 * Static access to the release/build version information. These are pulled from the release.properties
 * and build.properties files that are generated on release tag and build respectively. 
 *
 * FULL includes the full version and the type (-dev etc).
 * FRIENDLY is like FULL but without the final version component (SVN revision).
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Version
{
  public static final int RELEASE_MAJOR;
  public static final int RELEASE_MINOR;
  public static final int RELEASE_MICRO;
  public static final int RELEASE_REVISION;
  public static final String RELEASE_TYPE;
  public static final Date RELEASE_DATE;

  public static final VersionSpec RELEASE;

  public static final String RELEASE_FULL;
  public static final String RELEASE_FRIENDLY;

  public static final String BUILD_USER;
  public static final Date BUILD_DATE;
  public static final String BUILD_JAVA;
  public static final String BUILD_OS;

  static {
    try {
      Properties p = new Properties();

      InputStream releasePropertiesResource = Version.class.getResourceAsStream("/release.properties"); //NON-NLS
      p.load(Version.class.getResourceAsStream("/build.properties")); //NON-NLS	      InputStream releasePropertiesResource = Version.class.getResourceAsStream("/release.properties"); //NON-NLS
      if (releasePropertiesResource == null) {
        throw new ExceptionInInitializerError("release.properties file not found");
      }
      p.load(releasePropertiesResource);

      InputStream buildPropertiesResource = Version.class.getResourceAsStream("/build.properties"); //NON-NLS
      if (buildPropertiesResource == null) {
        throw new ExceptionInInitializerError("build.properties file not found");
      }
      p.load(buildPropertiesResource);

      RELEASE_MAJOR = Integer.parseInt(p.getProperty("release.major", "0"));
      RELEASE_MINOR = Integer.parseInt(p.getProperty("release.minor", "0"));
      RELEASE_MICRO = Integer.parseInt(p.getProperty("release.micro", "0"));
      RELEASE_REVISION = Integer.parseInt(p.getProperty("release.revision", "0"));

      RELEASE_TYPE = p.getProperty("release.type");
      RELEASE_DATE = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).parse(p.getProperty("release.date"));
      RELEASE = new VersionSpec(new int[]{RELEASE_MAJOR, RELEASE_MINOR, RELEASE_MICRO, RELEASE_REVISION});

      String releaseFull;
      releaseFull = RELEASE.toString();
      if (RELEASE_TYPE.length() > 0) {
        releaseFull += "-" + RELEASE_TYPE;
      }
      RELEASE_FULL = releaseFull;

      StringBuilder releaseFriendly = new StringBuilder();
      releaseFriendly.append(RELEASE_MAJOR).append(".").append(RELEASE_MINOR).append(".").append(RELEASE_MICRO);
      if (RELEASE_TYPE.length() > 0) {
        releaseFriendly.append("-").append(RELEASE_TYPE);
      }
      RELEASE_FRIENDLY = releaseFriendly.toString();

      BUILD_USER = p.getProperty("build.user");
      BUILD_DATE = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH).parse(p.getProperty("build.date"));
      BUILD_JAVA = p.getProperty("build.java");
      BUILD_OS = p.getProperty("build.os");

    }
    catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private Version()
  {
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  public static void main(String[] args)
  {
    System.out.println("RELEASE_REVISION = " + RELEASE_REVISION);
    System.out.println("RELEASE = " + RELEASE);
  }
}
