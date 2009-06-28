package com.goofans.gootool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.addins.AddinFormatException;
import com.goofans.gootool.siteapi.APIException;
import com.goofans.gootool.siteapi.AddinUpdatesCheckRequest;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.wog.WorldOfGoo;

/**
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
        Map<String, AddinUpdatesCheckRequest.AvailableUpdate> updates = checkRequest.checkUpdates(checkList);
        update = updates.get(BILLBOARDS_ADDIN_ID);
      }
      catch (APIException e) {
        log.log(Level.SEVERE, "Error in addin update check request", e);
        return;
      }

      if (update == null) {
        log.log(Level.WARNING, "Billboard addin update not found!");
        return;
      }

      ToolPreferences.setBillboardLastCheck(System.currentTimeMillis());

      File billboardModFile;
      try {
        billboardModFile = WorldOfGoo.getTheInstance().getCustomGameFile(BILLBOARDS_GOOMOD_FILENAME);
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Unable to locate billboards.goomod file");
        return;
      }

      try {
        if (billboardModFile.exists()) {
          // Already have a billboard mod file, see if it's up to date
          Addin billboardAddin = AddinFactory.loadAddin(billboardModFile);

          if (billboardAddin.getVersion().compareTo(update.version) >= 0) {
            log.log(Level.INFO, "Billboard addin already up to date (" + billboardAddin.getVersion() + ")");
            return;
          }
          log.fine("Downloading billboards because our version " + billboardAddin.getVersion() + " is lower than " + update.version);
        }
        else {
          log.fine("Downloading billboards because we have no copy");
        }
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Unable to read billboards.goomod file");
        return;
      }
      catch (AddinFormatException e) {
        log.log(Level.WARNING, "billboards.goomod is in invalid format, forcing re-download");
      }

      log.log(Level.INFO, "Billboards update is available, downloading " + update.downloadUrl);

      try {
        InputStream downloadStream = new URL(update.downloadUrl).openStream();
        FileOutputStream outputStream = new FileOutputStream(billboardModFile);
        Utilities.copyStreams(downloadStream, outputStream);
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Unable to download billboard goomod", e);
      }

      log.log(Level.INFO, "Billboards version " + update.version + " downloaded to " + billboardModFile);
    }
  }

  public static synchronized void maybeUpdateBillboards()
  {
    if (ToolPreferences.isBillboardDisable())
      return;

    final WorldOfGoo wog = WorldOfGoo.getTheInstance();
    if (!wog.isCustomDirSet()) {
      log.log(Level.WARNING, "Aborting update check as no custom dir set yet");
      return;
    }

    long nextCheck = ToolPreferences.getBillboardLastCheck() + UPDATE_INTERVAL;
    long now = System.currentTimeMillis();

    log.finer("maybe update billboards. nextCheck = " + nextCheck + ", current time = " + now);

    File billboardModFile;
    try {
      billboardModFile = WorldOfGoo.getTheInstance().getCustomGameFile(BILLBOARDS_GOOMOD_FILENAME);
    }
    catch (IOException e) {
      // won't happen due to isCustomDirSet check above
      return;
    }

    if (now < nextCheck && billboardModFile.exists())
      return;

    log.log(Level.INFO, "Billboard update check is due");

    new Thread(new BillboardUpdater()).start();
  }

  public static void main(String[] args)
  {
    WorldOfGoo.getTheInstance().init();

    BillboardUpdater.maybeUpdateBillboards();
  }

}
