package com.goofans.gootool.profile;

import com.goofans.gootool.util.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProfileFactory
{
  private static final Logger log = Logger.getLogger(ProfileFactory.class.getName());

  private static final String[] SEARCH_PATHS = {
          // NEW locations (under profile)
          "%LOCALAPPDATA%\\2DBoy\\WorldOfGoo", // generic, appdata
          "%USERPROFILE%\\AppData\\Local\\2DBoy\\WorldOfGoo", // vista
          "%USERPROFILE%\\Local Settings\\Application Data\\2DBoy\\WorldOfGoo", // xp

          // OLD locations (under All Users)
          "%ProgramData%\\2DBoy\\WorldOfGoo", // generic all users (old) (vista: c:\programdata... TODO check XP : Allusers\appdata?)
          "C:\\ProgramData\\2DBoy\\WorldOfGoo", // fixed, vista
          "C:\\Documents and Settings\\All Users\\Application Data\\2DBoy\\WorldOfGoo", // fixed, xp
  };
  private static final String PROFILE_DAT_FILENAME = "pers2.dat";

  private ProfileFactory()
  {
  }

  public static ProfileData findProfileData()
  {
    for (String searchPath : SEARCH_PATHS) {

      File file = new File(Utilities.expandEnvVars(searchPath), PROFILE_DAT_FILENAME);
      if (file.exists()) {
        log.info("Found profile through default search of \"" + searchPath + "\" at: " + file);
        try {
          return new ProfileData(file);
        }
        catch (IOException e) {
          log.log(Level.SEVERE, "Couldn't load profile from " + file, e);
        }
      }
    }
    log.log(Level.INFO, "Couldn't find the user's profile in any default location");
    return null;
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args)
  {
    for (String searchPath : SEARCH_PATHS) {
      System.out.println("searchPath = " + searchPath);
      System.out.println("expandEndVars(searchPath) = " + Utilities.expandEnvVars(searchPath));
    }

    System.out.println("findProfileData() = " + findProfileData());
  }
}