package com.goofans.gootool.platform;

import com.goofans.gootool.Controller;

import java.io.*;
import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.nio.channels.FileLock;
import java.nio.channels.FileChannel;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class WindowsSupport extends PlatformSupport
{
  private static final Logger log = Logger.getLogger(WindowsSupport.class.getName());

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
          "%HOME%/.PlayOnLinux/wineprefix/WorldOfGoo/drive_c/windows/profiles/All Users/Application Data/2DBoy/WorldOfGoo", // PlayOnLinux, new format

          "%HOME%/.wine/drive_c/windows/profiles/%USERNAME%/Application Data/2DBoy/WorldOfGoo", //wine, new format
          "%HOME%/.wine/drive_c/windows/profiles/All Users/Application Data/2DBoy/WorldOfGoo", //wine, old format
  };

  private static final String LOCK_FILE = "gootool.lock";
  private static final String TEST_FILE = "gootool.test";

  private Controller controller;

  protected boolean doPreStartup(String[] args)
  {
    String tmpDir = System.getProperty("java.io.tmpdir");
    FileOutputStream fos;

    // First do a quick test to make sure we can write anything to the tmpdir
    // (since the FileNotFoundException below covers both file being locked and
    // dir being unwriteable

    File testFile = new File(tmpDir, TEST_FILE + new Random().nextInt());
    log.finest("Testing tmpdir at " + testFile);

    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(testFile, "rws");
      FileChannel channel = randomAccessFile.getChannel();
      FileLock lock = channel.tryLock();
      if (lock == null) {
        log.log(Level.SEVERE, "Unable to lock temp file " + testFile);
        throw new RuntimeException("Unable to write to lock temp file " + testFile);
      }
      
      fos = new FileOutputStream(randomAccessFile.getFD());
      fos.write('a');
      fos.close();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to write to temp directory " + tmpDir, e);
      throw new RuntimeException("Unable to write to temp directory " + tmpDir);
    }
//    testFile.delete();

    // Now try opening the real lockfile, if we can't write, another process has it open.
    File lockFile = new File(tmpDir, LOCK_FILE);
    log.finest("Attempting lock at " + lockFile);

    try {
      fos = new FileOutputStream(lockFile);
      // TODO
    }
    catch (FileNotFoundException e) {
//      log.log(Level.WARNING)
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return true;
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
