package com.goofans.gootool.versioncheck;

import net.infotrek.util.EncodingUtil;

import javax.swing.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.*;

import com.goofans.gootool.util.*;
import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.GooFansAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class VersionCheck implements Runnable
{
  private static final Logger log = Logger.getLogger(VersionCheck.class.getName());

  private URL url;

  private boolean completed = false;
  private Exception failureReason = null;
  private boolean upToDate = false;

  private boolean alwaysAlertUser;
  private Window parentWindow;

  public VersionCheck(Window parentWindow, boolean alwaysAlertUser) throws MalformedURLException
  {
    this.url = new URL("http://api.goofans.com/gootool-version-check?version=" + EncodingUtil.urlEncode(Version.RELEASE.toString()));
    this.alwaysAlertUser = alwaysAlertUser;
    this.parentWindow = parentWindow;
  }

  public void run()
  {
    log.log(Level.FINE, "Checking " + url);
    try {
      URLConnection urlConn = GooFansAPI.createAPIConnection(url);

      Document doc = XMLUtil.loadDocumentFromReader(new InputStreamReader(urlConn.getInputStream()));

      if (findError(doc)) return;
      if (findUpToDate(doc)) return;
      if (findUpdateAvailable(doc)) return;

      completed = true;
      failureReason = new Exception("No result received from server!");
    }
    catch (Exception e) {
      log.log(Level.WARNING, "Unable to check version", e);
      completed = true;
      failureReason = e;
    }
  }

  private boolean findError(Document doc) throws IOException
  {
    if (doc.getDocumentElement().getNodeName().equals("error")) {
      String message = XMLUtil.getElementStringRequired(doc.getDocumentElement(), "message");
      log.log(Level.WARNING, "Unable to check version. Message from server: " + message);

      completed = true;
      failureReason = new Exception("Server message: " + message);
      return true;
    }
    return false;
  }

  private boolean findUpToDate(Document doc)
  {
    if (XMLUtil.getElement(doc.getDocumentElement(), "up-to-date") != null) {
      log.log(Level.FINE, "We are up to date");
      completed = true;
      upToDate = true;

      if (alwaysAlertUser) {
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            JOptionPane.showMessageDialog(parentWindow, "You are running the latest version " + Version.RELEASE_FULL, "GooTool up to date", JOptionPane.INFORMATION_MESSAGE);
          }
        });
      }

      return true;
    }
    return false;
  }

  private boolean findUpdateAvailable(Document doc) throws IOException
  {
    Element updateAvailableEl = XMLUtil.getElement(doc.getDocumentElement(), "update-available");
    if (updateAvailableEl == null) {
      return false;
    }

    final VersionSpec newVersion = new VersionSpec(XMLUtil.getElementStringRequired(updateAvailableEl, "version"));
    final String message = XMLUtil.getElementString(updateAvailableEl, "message");

    log.log(Level.FINE, "New version available, ver=" + newVersion + ", message=" + message);
    completed = true;
    upToDate = false;

    if (alwaysAlertUser || !ToolPreferences.isIgnoreUpdate(newVersion)) {
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          new NewVersionDialog(parentWindow, newVersion, message).setVisible(true);
        }
      });
    }
    return true;
  }

  public boolean isUpToDate() throws Exception
  {
    if (!completed) throw new IllegalStateException("Version check hasn't completed");

    if (failureReason != null) throw new Exception("Version check failed", failureReason);

    return upToDate;
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws Exception
  {
    DebugUtil.setAllLogging();
    GUIUtil.switchToSystemLookAndFeel();

    VersionCheck vc = new VersionCheck(null, true);

    Thread t = new Thread(vc);
    t.start();
    t.join();

    System.out.println("Completed.");
    System.out.println("vc.isUpToDate() = " + vc.isUpToDate());
  }
}
