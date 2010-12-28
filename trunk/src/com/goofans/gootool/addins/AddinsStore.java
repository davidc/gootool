/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.addins;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.Utilities;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AddinsStore
{
  private static final Logger log = Logger.getLogger(AddinsStore.class.getName());

  public static final String STORAGE_DIR_ADDINS = "addins";
  public static final String GOOMOD_EXTENSION = "goomod";
  public static final String GOOMOD_EXTENSION_WITH_DOT = "." + GOOMOD_EXTENSION;

  public static List<Addin> availableAddins = new LinkedList<Addin>();
  private static File addinsDir = null;

  private AddinsStore()
  {
  }

  /**
   * Returns the new directory that GooTool stores installed addins from version 1.1 onward.
   *
   * @return The addin directory.
   * @throws IOException if the addin directory couldn't be determined or created.
   */
  public static File getAddinsDir()
  {
    if (addinsDir == null) throw new RuntimeException("Addins directory requested before initialisation"); //NON-NLS
    return addinsDir;
  }

  public static synchronized void initAddinsDir() throws IOException
  {
    addinsDir = new File(PlatformSupport.getToolStorageDirectory(), STORAGE_DIR_ADDINS);
    Utilities.mkdirsOrException(addinsDir);
  }

  /**
   * Returns all available addins from the user's available addin store.
   *
   * @return List of available addins.
   */
  public static List<Addin> getAvailableAddins()
  {
    return Collections.unmodifiableList(availableAddins);
  }

  public static void updateAvailableAddins()
  {
    availableAddins = new LinkedList<Addin>();

    File[] files = addinsDir.listFiles();

    for (File file : files) {
      if (file.isFile() && file.getName().endsWith(GOOMOD_EXTENSION_WITH_DOT)) {
        try {
          updateAddAddin(file);
        }
        catch (AddinFormatException e) {
          log.log(Level.WARNING, "Ignoring invalid addin " + file + " in addins dir", e);
        }
        catch (IOException e) {
          log.log(Level.WARNING, "Ignoring unreadable addin " + file + " in addins dir", e);
        }
      }
    }
  }

  private static void updateAddAddin(File file) throws AddinFormatException, IOException
  {
    availableAddins.add(AddinFactory.loadAddin(file));
  }

  public static File getAddinInstalledFile(String addinId) throws IOException
  {
    return new File(getAddinsDir(), addinId + GOOMOD_EXTENSION_WITH_DOT);
  }

  public static void installAddin(File addinFile, String addinId) throws IOException, AddinFormatException
  {
    // Check we don't already have an addin with this ID
    for (Addin availableAddin : availableAddins) {
      if (availableAddin.getId().equals(addinId)) {
        throw new IOException("An addin with id " + addinId + " already exists!");
      }
    }

    File destFile = getAddinInstalledFile(addinId);

    log.log(Level.INFO, "Installing addin " + addinId + " from " + addinFile + " to " + destFile);

    Utilities.copyFile(addinFile, destFile);

    availableAddins.add(AddinFactory.loadAddin(destFile));
  }

  public static void uninstallAddin(Addin addin) throws IOException
  {
    File addinFile = addin.getDiskFile();
    log.log(Level.INFO, "Uninstalling addin, deleting " + addinFile);

    if (!addinFile.delete()) {
      throw new IOException("Couldn't delete " + addinFile);
    }

    for (Addin availableAddin : availableAddins) {
      if (availableAddin.getId().equals(addin.getId())) {
        availableAddins.remove(availableAddin);
        break;
      }
    }
  }

  public static Addin getAddinById(String id)
  {
    for (Addin addin : availableAddins) {
      if (addin.getId().equals(id)) {
        return addin;
      }
    }
    return null;
  }
}
