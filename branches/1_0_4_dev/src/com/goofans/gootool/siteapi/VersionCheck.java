/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.view.NewVersionDialog;
import com.goofans.gootool.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * API call to check whether we have the latest version of GooTool.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class VersionCheck implements Runnable
{
  private static final Logger log = Logger.getLogger(VersionCheck.class.getName());

  private final Frame parentWindow;
  private final boolean alwaysAlertUser;

  private boolean completed = false;
  private Exception failureReason = null;
  private boolean upToDate = false;

  private VersionSpec newVersion;
  private String newVersionMessage;
  private String newVersionDownloadUrl;

  public VersionCheck(Frame parentWindow, boolean alwaysAlertUser)
  {
    this.alwaysAlertUser = alwaysAlertUser;
    this.parentWindow = parentWindow;
  }

  public void run()
  {
    try {
      runUpdateCheck();

      if (!upToDate && (alwaysAlertUser || !ToolPreferences.isIgnoreUpdate(newVersion))) {
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            new NewVersionDialog(parentWindow, newVersion, newVersionMessage, newVersionDownloadUrl).setVisible(true);
          }
        });
      }
    }
    catch (Exception e) {
      log.log(Level.WARNING, "Unable to check version", e);
      completed = true;
      failureReason = e;

      if (alwaysAlertUser) {
        JOptionPane.showMessageDialog(parentWindow, "Can't check version: " + e.getLocalizedMessage(), "Can't check version", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public void runUpdateCheck() throws APIException, IOException
  {
    APIRequest request = new APIRequest(APIRequest.API_CHECKVERSION);
    request.addGetParameter("version", Version.RELEASE.toString());

    log.log(Level.FINE, "Checkversion " + request);

    Document doc = request.doRequest();

    if (findUpToDate(doc)) return;
    if (findUpdateAvailable(doc)) return;

    throw new APIException("No result received from server!");
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
            JOptionPane.showMessageDialog(parentWindow, "You are running the latest version " + Version.RELEASE_FRIENDLY, "GooTool is up to date", JOptionPane.INFORMATION_MESSAGE);
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

    newVersion = new VersionSpec(XMLUtil.getElementStringRequired(updateAvailableEl, "version"));
    newVersionMessage = XMLUtil.getElementString(updateAvailableEl, "message");
    newVersionDownloadUrl = XMLUtil.getElementString(updateAvailableEl, "download-url");

    log.log(Level.FINE, "New version available, ver=" + newVersion + ", message=" + newVersionMessage + ", downloadUrl=" + newVersionDownloadUrl);
    completed = true;
    upToDate = false;

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
