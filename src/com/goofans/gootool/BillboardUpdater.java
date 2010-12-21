/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.addins.AddinFormatException;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectConfiguration;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.siteapi.APIException;
import com.goofans.gootool.siteapi.AddinUpdatesCheckRequest;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.Utilities;

/**
 * Checks the billboards goomod file and updates it if a later version is available.
 * TODO this should use the tool storage directory, not the custom directory!
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BillboardUpdater implements Runnable
{
  private static final long UPDATE_INTERVAL = 3 * 24 * 3600 * 1000; // 3 days

  private static final Logger log = Logger.getLogger(BillboardUpdater.class.getName());
  private static final String BILLBOARDS_ADDIN_ID = "com.goofans.billboards";

  private static final Object sync = new Object();
  public static final String BILLBOARDS_GOOMOD_FILENAME = "billboards.goomod";

  public void run()
  {
    AddinUpdatesCheckRequest.AvailableUpdate update;

    synchronized (sync) {
      try {
        AddinUpdatesCheckRequest checkRequest = new AddinUpdatesCheckRequest();
        ArrayList<String> checkList = new ArrayList<String>(1);
        checkList.add(BILLBOARDS_ADDIN_ID);
        Map<String, AddinUpdatesCheckRequest.AvailableUpdate> updates = checkRequest.checkUpdatesById(checkList);
        update = updates.get(BILLBOARDS_ADDIN_ID);
      }
      catch (APIException e) {
        log.log(Level.SEVERE, "Error in addin update check request", e); //NON-NLS
        return;
      }

      if (update == null) {
        log.log(Level.WARNING, "Billboard addin update not found!"); //NON-NLS
        return;
      }

      ToolPreferences.setBillboardLastCheck(System.currentTimeMillis());

      File billboardModFile;
      try {
        billboardModFile = new File(PlatformSupport.getToolStorageDirectory(), BILLBOARDS_GOOMOD_FILENAME);
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Unable to locate billboards.goomod file in run"); //NON-NLS
        return;
      }

      try {
        if (billboardModFile.exists()) {
          // Already have a billboard mod file, see if it's up to date
          Addin billboardAddin = AddinFactory.loadAddin(billboardModFile);

          if (billboardAddin.getVersion().compareTo(update.version) >= 0) {
            log.log(Level.INFO, "Billboard addin already up to date (" + billboardAddin.getVersion() + ")"); //NON-NLS
            return;
          }
          log.fine("Downloading billboards because our version " + billboardAddin.getVersion() + " is lower than " + update.version); //NON-NLS
        }
        else {
          log.fine("Downloading billboards because we have no copy"); //NON-NLS
        }
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Unable to read billboards.goomod file"); //NON-NLS
        return;
      }
      catch (AddinFormatException e) {
        log.log(Level.WARNING, "billboards.goomod is in invalid format, forcing re-download"); //NON-NLS
      }

      log.log(Level.INFO, "Billboards update is available, downloading " + update.downloadUrl); //NON-NLS

      try {
        Utilities.downloadFile(new URL(update.downloadUrl), billboardModFile);
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Unable to download billboard goomod", e); //NON-NLS
      }

      log.log(Level.INFO, "Billboards version " + update.version + " downloaded to " + billboardModFile); //NON-NLS
    }
  }

  public static synchronized void maybeUpdateBillboards()
  {
    // If all projects have their billboards disabled, don't download.
    boolean billboardsEnabled = false;
    for (Project project : ProjectManager.getProjects()) {
      if (project != null) {
        ProjectConfiguration c = project.getConfiguration();
        if (!c.isBillboardsDisabled()) {
          billboardsEnabled = true;
          break;
        }
      }
    }

    if (!billboardsEnabled) {
      log.log(Level.FINE, "Not updating billboards as no projects have billboards enabled"); //NON-NLS
      return;
    }

    long nextCheck = ToolPreferences.getBillboardLastCheck() + UPDATE_INTERVAL;
    long now = System.currentTimeMillis();

    log.finer("maybe update billboards. nextCheck = " + nextCheck + ", current time = " + now); //NON-NLS

    File billboardModFile = null;
    try {
      billboardModFile = new File(PlatformSupport.getToolStorageDirectory(), BILLBOARDS_GOOMOD_FILENAME);
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to locate billboards.goomod file for maybeUpdateBillboards"); //NON-NLS
      return;
    }

    if (now < nextCheck && billboardModFile.exists())
      return;

    log.log(Level.INFO, "Billboard update check is due"); //NON-NLS

    GooTool.executeTaskInThreadPool(new BillboardUpdater());
  }

  @Override
  public String toString()
  {
    return "BillboardUpdater";
  }

  public static void main(String[] args)
  {
    DebugUtil.setAllLogging();
    GooTool.initExecutors();

    BillboardUpdater.maybeUpdateBillboards();
  }
}
