package com.goofans.gootool.util;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class URLLauncher
{
  private static final Logger log = Logger.getLogger(URLLauncher.class.getName());

  private static final String[] UNIX_BROWSER_CMDS = {
          "www-browser", // debian update-alternatives target
          "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};

  private URLLauncher()
  {
  }


  /**
   * Launches the given URL and throws an exception if it couldn't be launched.
   *
   * @param url the URL to open
   * @throws IOException if a browser couldn't be found or if the URL failed to launch
   */
  public static void launch(URL url) throws IOException
  {
    String osName = System.getProperty("os.name");
    log.finer("Launching " + url + " for OS " + osName);

    if (osName.startsWith("Mac OS")) {
      launchMac(url);
    }
    else if (osName.startsWith("Windows")) {
      launchWindows(url);
    }
    else {
      //assume Unix or Linux
      launchUnix(url);
    }
  }

  /**
   * Launches the given URL and shows a dialog to the user if a browser couldn't be found or if the URL failed to launch.
   *
   * @param url the URL to open
   */
  public static void launchAndWarn(URL url)
  {
    try {
      launch(url);
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to launch " + url, e);
      JOptionPane.showMessageDialog(null, "Couldn't open a web browser:\n" + e.getLocalizedMessage(), "Unable to launch web browser", JOptionPane.ERROR_MESSAGE);
    }
  }


  private static void launchMac(URL url) throws IOException
  {
    try {
      log.finer("Mac looking for com.apple.eio.FileManager");
      Class fileMgr = Class.forName("com.apple.eio.FileManager");
      Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});

      log.finer("Mac invoking");
      openURL.invoke(null, url.toString());
    }
    catch (Exception e) {
      throw new IOException("Could not launch Mac browser: " + e.getLocalizedMessage(), e);
    }
  }

  private static void launchWindows(URL url) throws IOException
  {
    log.finer("Windows invoking rundll32");
    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()});
  }

  private static void launchUnix(URL url) throws IOException
  {
    for (String cmd : UNIX_BROWSER_CMDS) {
      log.finer("Unix looking for " + cmd);
      if (checkCommandExists(cmd)) {
        log.finer("Unix found " + cmd);
        Runtime.getRuntime().exec(new String[]{cmd, url.toString()});
        return;
      }
    }
    throw new IOException("Could not find a suitable web browser");
  }

  private static boolean checkCommandExists(String cmd) throws IOException
  {
    Process whichProcess = Runtime.getRuntime().exec(new String[]{"which", cmd});

    boolean finished = false;
    do {
      try {
        whichProcess.waitFor();
        finished = true;
      }
      catch (InterruptedException e) {
        log.log(Level.WARNING, "Interrupted waiting for which to complete", e);
      }
    } while (!finished);

    return whichProcess.exitValue() == 0;
  }

  public static void main(String[] args) throws IOException
  {
    DebugUtil.setAllLogging();

    URL url = new URL("http://goofans.com/");
    URLLauncher.launch(url);
//    URLLauncher.launchAndWarn(url);
  }
}
