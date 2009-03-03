package com.goofans.gootool.siteapi;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.view.NewVersionDialog;
import com.goofans.gootool.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class VersionCheck implements Runnable
{
  private static final Logger log = Logger.getLogger(VersionCheck.class.getName());

  private boolean completed = false;
  private Exception failureReason = null;
  private boolean upToDate = false;

  private boolean alwaysAlertUser;
  private Frame parentWindow;

  public VersionCheck(Frame parentWindow, boolean alwaysAlertUser) throws MalformedURLException
  {
    this.alwaysAlertUser = alwaysAlertUser;
    this.parentWindow = parentWindow;
  }

  public void run()
  {
    try {
      APIRequest request = new APIRequest(APIRequest.API_CHECKVERSION);
      request.addGetParameter("version", Version.RELEASE.toString());

      log.log(Level.FINE, "Checkversion " + request);

      Document doc = request.doRequest();

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
            JOptionPane.showMessageDialog(parentWindow, "You are running the latest version " + Version.RELEASE_FULL, "GooTool is up to date", JOptionPane.INFORMATION_MESSAGE);
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
    final String downloadUrl = XMLUtil.getElementString(updateAvailableEl, "download-url");

    log.log(Level.FINE, "New version available, ver=" + newVersion + ", message=" + message + ", downloadUrl=" + downloadUrl);
    completed = true;
    upToDate = false;

    if (alwaysAlertUser || !ToolPreferences.isIgnoreUpdate(newVersion)) {
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          new NewVersionDialog(parentWindow, newVersion, message, downloadUrl).setVisible(true);
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