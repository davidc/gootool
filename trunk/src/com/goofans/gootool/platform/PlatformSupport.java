package com.goofans.gootool.platform;

import com.goofans.gootool.Controller;

import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public abstract class PlatformSupport
{
  private static final Logger log = Logger.getLogger(PlatformSupport.class.getName());

  public enum Platform
  {
    WINDOWS, MACOSX
  }

  private static final Platform platform;
  private static final PlatformSupport support;

  static {
    String lcOSName = System.getProperty("os.name").toLowerCase();
    if (lcOSName.startsWith("mac os x")) {
      platform = Platform.MACOSX;
      support = new MacOSXSupport();
    }
    else {
      platform = Platform.WINDOWS;
      support = new WindowsSupport();
    }

    log.fine("Platform detected: " + platform);
  }

  protected PlatformSupport()
  {
  }

  public static Platform getPlatform()
  {
    return platform;
  }


  public static boolean preStartup(String[] args)
  {
    return support.doPreStartup(args);
  }

  protected abstract boolean doPreStartup(String[] args);

  public static void startup(Controller controller)
  {
    support.doStartup(controller);
  }

  protected abstract void doStartup(Controller controller);

  public static String[] getProfileSearchPaths()
  {
    return support.doGetProfileSearchPaths();
  }

  protected abstract String[] doGetProfileSearchPaths();
}
