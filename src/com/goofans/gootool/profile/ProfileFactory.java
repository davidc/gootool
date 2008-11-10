package com.goofans.gootool.profile;

import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.GooTool;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

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
          "%ProgramData%\\2DBoy\\WorldOfGoo", // generic all users, vista (c:\programdata...)
          "%ALLUSERSPROFILE%\\Application Data\\2DBoy\\WorldOfGoo", // generic all users, xp but not internationalised C:\Documents and Settings\All Users\Application Data\2DBoy\WorldOfGoo
          "C:\\ProgramData\\2DBoy\\WorldOfGoo", // fixed, vista
          "C:\\Documents and Settings\\All Users\\Application Data\\2DBoy\\WorldOfGoo", // fixed, xp
  };
  private static final String PROFILE_DAT_FILENAME = "pers2.dat";
  private static final String PREF_PROFILE_FILE = "profile_file";

  private static File profileFile;

  private ProfileFactory()
  {
  }

  public static void init()
  {
    Preferences p = Preferences.userNodeForPackage(GooTool.class);
    String userProfile = p.get(PREF_PROFILE_FILE, null);

    if (userProfile != null) {
      File file = new File(userProfile);

      if (locateProfileAtFile(file)) {
        log.info("Found profile at stored location of \"" + file + "\"");
        return;
      }
    }

    for (String searchPath : SEARCH_PATHS) {

      File file = new File(Utilities.expandEnvVars(searchPath), PROFILE_DAT_FILENAME);
      if (locateProfileAtFile(file)) {
        log.info("Found profile through default search of \"" + searchPath + "\" at: " + file);
        return;
      }
    }
    log.log(Level.INFO, "Couldn't find the user's profile in any default location");
  }

  public static boolean locateProfileAtFile(File file)
  {
    if (!file.exists()) {
      return false;
    }

    // Attempt to read it in
    try {
      new ProfileData(file);

      // We successfully loaded without exception, now save its location.
      Preferences p = Preferences.userNodeForPackage(GooTool.class);
      p.put(PREF_PROFILE_FILE, file.getAbsolutePath());

      profileFile = file;
      return true;
    }
    catch (IOException e) {
      log.log(Level.WARNING, "Unable to read profile at " + file, e);
      return false;
    }
  }

  public static boolean isProfileFound()
  {
    return profileFile != null;
  }

  public static ProfileData getProfileData() throws IOException
  {
    if (profileFile == null) throw new IOException("Profile isn't loaded yet!");
    return new ProfileData(profileFile);
  }

  public static File getProfileFile()
  {
    return profileFile;
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    for (String searchPath : SEARCH_PATHS) {
      System.out.println("searchPath = " + searchPath);
      System.out.println("expandEndVars(searchPath) = " + Utilities.expandEnvVars(searchPath));
    }

//    System.out.println("findProfileData() = " + findProfileData());
    ProfileFactory.init();
    System.out.println("getProfileData() = " + getProfileData());
  }
}