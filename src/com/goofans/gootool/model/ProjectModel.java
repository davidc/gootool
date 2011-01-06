/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.model;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinsStore;
import com.goofans.gootool.projects.ProjectConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProjectModel
{
  private static final Logger log = Logger.getLogger(ProjectModel.class.getName());

  // The configuration we're editing
  private final ProjectConfiguration editorConfig;

  public ProjectModel(ProjectConfiguration configuration)
  {
    this.editorConfig = configuration;
  }

  public ProjectConfiguration getEditorConfig()
  {
    return editorConfig;
  }

  public List<Addin> getDisplayAddins()
  {
    List<Addin> availableAddins = new ArrayList<Addin>(AddinsStore.getAvailableAddins());
    List<Addin> displayAddins = new ArrayList<Addin>(availableAddins.size());

    /* First, all the enabled addins, in order */

    for (String id : getEditorConfig().getEnabledAddins()) {
      for (Addin availableAddin : availableAddins) {
        if (availableAddin.getId().equals(id)) {
          displayAddins.add(availableAddin);
          availableAddins.remove(availableAddin);
          break;
        }
      }
    }

    /* Then any remaining addins */

    for (Addin availableAddin : availableAddins) {
      displayAddins.add(availableAddin);
    }

    return displayAddins;
  }


  public void enableAddin(String id)
  {
    editorConfig.enableAddin(id);
  }

  public void disableAddin(String id)
  {
    editorConfig.disableAddin(id);
  }

  public void removeUnavailableAddins()
  {
    // Remove any addins that are enabled but don't exist
    List<Addin> availableAddins = AddinsStore.getAvailableAddins();
    boolean foundThisAddin = false;
    do {
      for (String enabledAddinName : editorConfig.getEnabledAddins()) {
        foundThisAddin = false;
        // see if this addin is available
        for (Addin availableAddin : availableAddins) {
          if (availableAddin.getId().equals(enabledAddinName)) {
            foundThisAddin = true;
            break;
          }
        }
        if (!foundThisAddin) {
          log.log(Level.WARNING, "Removed enabled addin " + enabledAddinName + " as it is not installed");
          editorConfig.disableAddin(enabledAddinName);
          break;
        }
      }
    } while (!editorConfig.getEnabledAddins().isEmpty() && !foundThisAddin);
  }
}