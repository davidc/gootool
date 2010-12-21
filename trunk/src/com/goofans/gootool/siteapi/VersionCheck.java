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

import com.goofans.gootool.GooTool;
import com.goofans.gootool.GooToolResourceBundle;
import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.util.*;
import com.goofans.gootool.view.NewVersionDialog;
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
      log.log(Level.WARNING, "Unable to check version", e); //NON-NLS
      completed = true;
      failureReason = e;

      if (alwaysAlertUser) {
        GooToolResourceBundle resourceBundle = GooTool.getTextProvider();
        JOptionPane.showMessageDialog(parentWindow, resourceBundle.formatString("checkVersion.error.message", e.getLocalizedMessage()), resourceBundle.getString("checkVersion.error.title"), JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public void runUpdateCheck() throws APIException, IOException
  {
    APIRequest request = new APIRequest(APIRequest.API_CHECKVERSION);
    //noinspection DuplicateStringLiteralInspection
    request.addGetParameter("version", Version.RELEASE.toString());

    log.log(Level.FINE, "Check version " + request); //NON-NLS

    Document doc = request.doRequest();

    if (findUpToDate(doc)) return;
    if (findUpdateAvailable(doc)) return;

    throw new APIException("No result received from server!");
  }

  private boolean findUpToDate(Document doc)
  {
    if (XMLUtil.getElement(doc.getDocumentElement(), "up-to-date") != null) { //NON-NLS
      log.log(Level.FINE, "We are up to date"); //NON-NLS
      completed = true;
      upToDate = true;

      if (alwaysAlertUser) {
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            GooToolResourceBundle resourceBundle = GooTool.getTextProvider();
            JOptionPane.showMessageDialog(parentWindow, resourceBundle.formatString("checkVersion.upToDate.message", Version.RELEASE_FRIENDLY), resourceBundle.getString("checkVersion.upToDate.title"), JOptionPane.INFORMATION_MESSAGE);
          }
        });
      }

      return true;
    }
    return false;
  }

  private boolean findUpdateAvailable(Document doc) throws IOException
  {
    Element updateAvailableEl = XMLUtil.getElement(doc.getDocumentElement(), "update-available"); //NON-NLS
    if (updateAvailableEl == null) {
      return false;
    }

    newVersion = new VersionSpec(XMLUtil.getElementStringRequired(updateAvailableEl, "version")); //NON-NLS
    newVersionMessage = XMLUtil.getElementString(updateAvailableEl, "message"); //NON-NLS
    newVersionDownloadUrl = XMLUtil.getElementString(updateAvailableEl, "download-url"); //NON-NLS

    log.log(Level.FINE, "New version available, ver=" + newVersion + ", message=" + newVersionMessage + ", downloadUrl=" + newVersionDownloadUrl); //NON-NLS
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

  @Override
  public String toString()
  {
    return "VersionCheck task";
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
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
