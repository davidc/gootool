package com.goofans.gootool.platform;

import com.goofans.gootool.Controller;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class WindowsSupport extends PlatformSupport
{
//  private static final Logger log = Logger.getLogger(WindowsSupport.class.getName());

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String[] PROFILE_SEARCH_PATHS = {
          // NEW locations (under profile)
          "%LOCALAPPDATA%\\2DBoy\\WorldOfGoo", // generic, appdata
          "%USERPROFILE%\\AppData\\Local\\2DBoy\\WorldOfGoo", // vista
          "%USERPROFILE%\\Local Settings\\Application Data\\2DBoy\\WorldOfGoo", // xp

          // OLD locations (under All Users)
          "%ProgramData%\\2DBoy\\WorldOfGoo", // generic all users, vista (c:\programdata...)
          "%ALLUSERSPROFILE%\\Application Data\\2DBoy\\WorldOfGoo", // generic all users, xp but not internationalised C:\Documents and Settings\All Users\Application Data\2DBoy\WorldOfGoo
          "C:\\ProgramData\\2DBoy\\WorldOfGoo", // fixed, vista
          "C:\\Documents and Settings\\All Users\\Application Data\\2DBoy\\WorldOfGoo", // fixed, xp

          "%HOME%/.PlayOnLinux/wineprefix/WorldOfGoo/drive_c/windows/profiles/%USERNAME%/Application Data/2DBoy/WorldOfGoo", // PlayOnLinux, new format
          "%HOME%/.PlayOnLinux/wineprefix/WorldOfGoo/drive_c/windows/profiles/All Users/Application Data/2DBoy/WorldOfGoo", // PlayOnLinux, old format

          "%HOME%/.wine/drive_c/windows/profiles/%USERNAME%/Application Data/2DBoy/WorldOfGoo", //wine, new format
          "%HOME%/.wine/drive_c/windows/profiles/All Users/Application Data/2DBoy/WorldOfGoo", //wine, old format
  };

  private Controller controller;

  private SingleInstance singleInstance = new SingleInstance();

  WindowsSupport()
  {
  }

  protected boolean doPreStartup(String[] args)
  {
    return singleInstance.singleInstance(args);
  }

  protected void doStartup(Controller controller)
  {
    this.controller = controller;
  }

  public String[] doGetProfileSearchPaths()
  {
    return PROFILE_SEARCH_PATHS;
  }
}
