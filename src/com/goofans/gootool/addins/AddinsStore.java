/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
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

  private AddinsStore()
  {
  }

  /**
   * Returns the new directory that GooTool stores installed addins from version 1.1 onward.
   *
   * @return The addin directory.
   * @throws IOException if the addin directory couldn't be determined or created.
   */
  public static File getAddinsDir() throws IOException
  {
    File addinsDir = new File(PlatformSupport.getToolStorageDirectory(), STORAGE_DIR_ADDINS);
    Utilities.mkdirsOrException(addinsDir);
    return addinsDir;
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

    File addinsDir;
    try {
      addinsDir = getAddinsDir();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "No addinsDir", e);
      throw new RuntimeException(e);
    }

    File[] files = addinsDir.listFiles();

    for (File file : files) {
      if (file.isFile() && file.getName().endsWith(GOOMOD_EXTENSION_WITH_DOT)) {
        try {
          availableAddins.add(AddinFactory.loadAddin(file));
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
}
