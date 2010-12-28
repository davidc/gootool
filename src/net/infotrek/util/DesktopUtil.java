/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package net.infotrek.util;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection", "StringConcatenation", "HardcodedLineSeparator"})
public class DesktopUtil
{
  private static final Logger log = Logger.getLogger(DesktopUtil.class.getName());

  private static final String OS_MACOS = "Mac OS";
  private static final String OS_WINDOWS = "Windows";

  private static final String[] UNIX_BROWSE_CMDS = {
          "www-browser", // debian update-alternatives target
          "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape", "w3m", "lynx"};

  private static final String[] UNIX_OPEN_CMDS = {
          "run-mailcap", // many Unixes, run registered program from /etc/mailcap
          // Fall back to assuming it's a text file.
          "pager", // debian update-alternatives target
          "less", "more"};


  private DesktopUtil()
  {
  }

  /**
   * Launches the given URL and throws an exception if it couldn't be launched.
   *
   * @param url the URL to open
   * @throws IOException if a browser couldn't be found or if the URL failed to launch
   */
  public static void browse(final URL url) throws IOException
  {
    // Try Java 1.6 Desktop class if supported
    if (browseDesktop(url)) return;

    final String osName = System.getProperty("os.name");
    log.finer("Launching " + url + " for OS " + osName);

    if (osName.startsWith(OS_MACOS)) {
      browseMac(url);
    }
    else if (osName.startsWith(OS_WINDOWS)) {
      browseWindows(url);
    }
    else {
      //assume Unix or Linux
      browseUnix(url);
    }
  }

  /**
   * Launches the given URL and shows a dialog to the user if a browser couldn't be found or if the URL failed to launch.
   *
   * @param url             the URL to open
   * @param parentComponent The parent Component over which the error dialog should be shown
   */
  public static void browseAndWarn(final URL url, final Component parentComponent)
  {
    try {
      browse(url);
    }
    catch (final IOException e) {
      log.log(Level.SEVERE, "Unable to browse to " + url, e);
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          JOptionPane.showMessageDialog(parentComponent, "Couldn't open a web browser:\n" + e.getLocalizedMessage(),
                  "Unable to launch web browser", JOptionPane.ERROR_MESSAGE);
        }
      });
    }
  }

  /**
   * Launches the given URL and shows a dialog to the user if a browser couldn't be found or if the URL failed to launch.
   * This version takes a String and handles the warning of malformed URLs where needed.
   *
   * @param url             the URL to open
   * @param parentComponent The parent Component over which the error dialog should be shown
   */
  public static void browseAndWarn(final String url, final Component parentComponent)
  {
    try {
      browse(new URL(url));
    }
    catch (final IOException e) {
      log.log(Level.SEVERE, "Unable to browse to " + url, e);
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          JOptionPane.showMessageDialog(parentComponent, "Couldn't open a web browser:\n" + e.getLocalizedMessage(),
                  "Unable to launch web browser", JOptionPane.ERROR_MESSAGE);
        }
      });
    }
  }

  /**
   * Opens the given File in the system default viewer application.
   *
   * @param file the File to open
   * @throws IOException if an application couldn't be found or if the File failed to launch
   */
  public static void open(final File file) throws IOException
  {
    // Try Java 1.6 Desktop class if supported
    if (openDesktop(file)) return;

    final String osName = System.getProperty("os.name");
    log.finer("Opening " + file + " for OS " + osName);

    if (osName.startsWith(OS_MACOS)) {
      openMac(file);
    }
    else if (osName.startsWith(OS_WINDOWS)) {
      openWindows(file);
    }
    else {
      //assume Unix or Linux
      openUnix(file);
    }
  }

  /**
   * Opens the given File in the system default viewer application and shows a dialog to the user if the file couldn't be opened.
   *
   * @param file            the File to open
   * @param parentComponent The parent Component over which the error dialog should be shown
   */
  public static void openAndWarn(final File file, final Component parentComponent)
  {
    try {
      open(file);
    }
    catch (final IOException e) {
      log.log(Level.SEVERE, "Unable to open " + file, e);
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          JOptionPane.showMessageDialog(parentComponent, "Couldn't open " + file + ":\n" + e.getLocalizedMessage(),
                  "Unable to open file", JOptionPane.ERROR_MESSAGE);
        }
      });
    }
  }

  /**
   * Attempt to use java.awt.Desktop by reflection. Does not link directly to Desktop class so that this class can still
   * be loaded in JRE < 1.6.
   *
   * @param url the URL to launch
   * @return true if launch successful, false if we should fall back to other methods
   * @throws IOException if Desktop was found, but the browse() call failed.
   */
  private static boolean browseDesktop(final URL url) throws IOException
  {
    final Class desktopClass = getDesktopClass();
    if (desktopClass == null) return false;

    final Object desktopInstance = getDesktopInstance(desktopClass);
    if (desktopInstance == null) return false;

    log.finer("Launching " + url + " using Desktop.browse()");

    try {
      final Method browseMethod = desktopClass.getDeclaredMethod("browse", URI.class);
      browseMethod.invoke(desktopInstance, new URI(url.toExternalForm()));
      return true;
    }
    catch (InvocationTargetException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      }
      else {
        log.log(Level.FINE, "Exception in Desktop operation", e);
        return false;
      }
    }
    catch (Exception e) {
      log.log(Level.FINE, "Exception in Desktop operation", e);
      return false;
    }
  }

  /**
   * Uses url.dll to browse to a URL under Windows.
   *
   * @param url the URL to launch
   * @throws IOException if the launch failed
   */
  private static void browseWindows(final URL url) throws IOException
  {
    log.finer("Windows invoking rundll32");
    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url.toString()});
  }

  /**
   * Attempts to locate a browser from a predefined list under Unix.
   *
   * @param url the URL to launch
   * @throws IOException if the launch failed
   */
  private static void browseUnix(final URL url) throws IOException
  {
    for (final String cmd : UNIX_BROWSE_CMDS) {
      log.finest("Unix looking for " + cmd);
      if (unixCommandExists(cmd)) {
        log.finer("Unix found " + cmd);
        Runtime.getRuntime().exec(new String[]{cmd, url.toString()});
        return;
      }
    }
    throw new IOException("Could not find a suitable web browser");
  }

  /**
   * Attempt to use com.apple.eio.FileManager by reflection.
   *
   * @param url the URL to launch
   * @throws IOException if the launch failed
   */
  private static void browseMac(final URL url) throws IOException
  {
    try {
      final Class fileMgr = getAppleFileManagerClass();
      final Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);

      log.finer("Mac invoking");
      openURL.invoke(null, url.toString());
    }
    catch (Exception e) {
      log.log(Level.WARNING, "Couldn't launch Mac URL", e);
      throw new IOException("Could not launch Mac URL: " + e.getLocalizedMessage());
    }
  }


  /**
   * Attempt to use java.awt.Desktop by reflection. Does not link directly to Desktop class so that this class can still
   * be loaded in JRE < 1.6.
   *
   * @param file the File to open
   * @return true if open successful, false if we should fall back to other methods
   * @throws IOException if Desktop was found, but the open() call failed.
   */
  private static boolean openDesktop(final File file) throws IOException
  {
    final Class desktopClass = getDesktopClass();
    if (desktopClass == null) return false;

    final Object desktopInstance = getDesktopInstance(desktopClass);
    if (desktopInstance == null) return false;

    log.finer("Opening " + file + " using Desktop.open()");

    try {
      final Method browseMethod = desktopClass.getDeclaredMethod("open", File.class);
      browseMethod.invoke(desktopInstance, file);
      return true;
    }
    catch (InvocationTargetException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      }
      else if (e.getCause() instanceof IllegalArgumentException) {
        throw new FileNotFoundException(e.getCause().getLocalizedMessage());
      }
      else {
        log.log(Level.FINE, "Exception in Desktop operation", e);
        return false;
      }
    }
    catch (Exception e) {
      log.log(Level.FINE, "Exception in Desktop operation", e);
      return false;
    }
  }

  /**
   * Uses shell32.dll to open a file under Windows.
   *
   * @param file the File to open
   * @throws IOException if the open failed
   */
  private static void openWindows(final File file) throws IOException
  {
    log.finer("Windows invoking rundll32");
    Runtime.getRuntime().exec(new String[]{"rundll32", "shell32.dll,ShellExec_RunDLL", file.getAbsolutePath()});
  }

  /**
   * Attempt to use com.apple.eio.FileManager by reflection.
   *
   * @param file the File to open
   * @throws IOException if the open failed
   */
  private static void openMac(final File file) throws IOException
  {
    // we use openURL() on the file's URL form since openURL supports file:// protocol
    browseMac(file.getAbsoluteFile().toURL());
  }


  /**
   * Attempts to locate a viewer from a predefined list under Unix.
   *
   * @param file the File to open
   * @throws IOException if the open failed
   */
  private static void openUnix(final File file) throws IOException
  {
    for (final String cmd : UNIX_OPEN_CMDS) {
      log.finest("Unix looking for " + cmd);
      if (unixCommandExists(cmd)) {
        log.finer("Unix found " + cmd);
        Runtime.getRuntime().exec(new String[]{cmd, file.getAbsolutePath()});
        return;
      }
    }
    throw new IOException("Could not find a suitable viewer");
  }

  /**
   * Find the Desktop class if it exists in this JRE.
   *
   * @return the Desktop class object, or null if it could not be found
   */
  private static Class getDesktopClass()
  {
    // NB The following String is intentionally not inlined to prevent ProGuard trying to locate the unknown class.
    final String desktopClassName = "java.awt.Desktop";
    try {
      return Class.forName(desktopClassName);
    }
    catch (ClassNotFoundException e) {
      log.fine("Desktop class not found");
      return null;
    }
  }

  /**
   * Gets a Desktop class instance if supported. We check isDesktopSupported() but for convenience we don't bother to
   * check isSupported(method); instead the caller handles any UnsupportedOperationExceptions.
   *
   * @param desktopClass the Desktop Class object
   * @return the Desktop instance, or null if it is not supported
   */
  private static Object getDesktopInstance(final Class desktopClass)
  {
    try {
      final Method isDesktopSupportedMethod = desktopClass.getDeclaredMethod("isDesktopSupported");
      log.finest("invoking isDesktopSupported");
      final boolean isDesktopSupported = (Boolean) isDesktopSupportedMethod.invoke(null);

      if (!isDesktopSupported) {
        log.finer("isDesktopSupported: no");
        return null;
      }

      final Method getDesktopMethod = desktopClass.getDeclaredMethod("getDesktop");
      return getDesktopMethod.invoke(null);
    }
    catch (Exception e) {
      log.log(Level.FINE, "Exception in Desktop operation", e);
      return null;
    }
  }

  /**
   * Finds the com.apple.eio.FileManager class on a Mac.
   *
   * @return the FileManager instance
   * @throws ClassNotFoundException if FileManager was not found
   */
  private static Class getAppleFileManagerClass() throws ClassNotFoundException
  {
    log.finest("Mac looking for com.apple.eio.FileManager");

    // NB The following String is intentionally not inlined to prevent ProGuard trying to locate the unknown class.
    final String appleClass = "com.apple.eio.FileManager";
    return Class.forName(appleClass);
  }

  /**
   * Checks whether a given executable exists, by means of the "which" command.
   *
   * @param cmd the executable to locate
   * @return true if the executable was found
   * @throws IOException if Runtime.exec() throws an IOException
   */
  private static boolean unixCommandExists(final String cmd) throws IOException
  {
    final Process whichProcess = Runtime.getRuntime().exec(new String[]{"which", cmd});

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

  @SuppressWarnings({"HardcodedFileSeparator"})
  public static void main(final String[] args) throws IOException
  {
    Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);

    DesktopUtil.browseAndWarn(new URL("http://goofans.com/"), null);
    DesktopUtil.openAndWarn(new File("dist_src/win32/lib/irrKlang/README.txt"), null);
//    DesktopUtil.browseAndWarn(new File("dist_src/win32/lib/irrKlang/README.txt").getAbsoluteFile().toURL(), null);
  }
}
