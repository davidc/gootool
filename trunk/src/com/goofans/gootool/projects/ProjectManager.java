/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.projects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.Utilities;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProjectManager
{
  private static final Logger log = Logger.getLogger(ProjectManager.class.getName());

  private static final Preferences PREFS = Preferences.userNodeForPackage(ProjectManager.class);

  private static final String PREF_KEY_CURRENT_PROJECT = "current_project";
  private static final String PREF_KEY_SUFFIX_TYPE = "_type";
  private static final String PREF_TYPE_LOCAL = "local";
  private static final String PREF_TYPE_IOS = "ios";

  private static List<Project> projects = null;


  private ProjectManager()
  {
  }

  public static List<Project> getProjects()
  {
    initProjects();
    return Collections.unmodifiableList(projects);
  }

  private static synchronized void initProjects()
  {
    if (projects != null) return;

    projects = new ArrayList<Project>(8);

    String[] keys;
    try {
      keys = PREFS.keys();
    }
    catch (BackingStoreException e) {
      log.log(Level.SEVERE, "Unable to load project nodes", e);
      throw new RuntimeException("Unable to load project nodes", e);
    }

    for (String key : keys) {
      if (key.endsWith(PREF_KEY_SUFFIX_TYPE)) {
        int projectId = Integer.parseInt(key.substring(0, key.length() - PREF_KEY_SUFFIX_TYPE.length()));
        while (projects.size() - 1 < projectId) projects.add(null);

        String projectType = PREFS.get(key, null);
        if (PREF_TYPE_LOCAL.equals(projectType)) {
          projects.set(projectId, new LocalProject(getProjectPrefs(projectId), getProjectStorageDir(projectId)));
        }
        else if (PREF_TYPE_IOS.equals(projectType)) {
          projects.set(projectId, new IosProject(getProjectPrefs(projectId), getProjectStorageDir(projectId)));
        }
        else {
          throw new RuntimeException("Unrecognised stored project type " + projectType);
        }
      }
    }
  }

  public static LocalProject createLocalProject() throws IOException
  {
    initProjects();
    int nextId = getNextId();
    log.log(Level.INFO, "Creating new local project " + nextId);

    LocalProject project = new LocalProject(getProjectPrefs(nextId), getProjectStorageDir(nextId));

    createProjectInternal(nextId, project, PREF_TYPE_LOCAL);

    return project;
  }

  public static IosProject createIosProject() throws IOException
  {
    initProjects();
    int nextId = getNextId();
    log.log(Level.INFO, "Creating new iOS project " + nextId);

    IosProject project = new IosProject(getProjectPrefs(nextId), getProjectStorageDir(nextId));

    createProjectInternal(nextId, project, PREF_TYPE_IOS);

    return project;
  }

  private static Preferences getProjectPrefs(int id)
  {
    return PREFS.node("" + id);
  }

  private static void createProjectInternal(int id, Project project, String type) throws IOException
  {
    PREFS.put(id + PREF_KEY_SUFFIX_TYPE, type);
    projects.set(id, project);

    Utilities.flushPrefs(PREFS);

    File storageDir = getProjectStorageDir(id);
    if (storageDir.exists()) {
      Utilities.rmdirAll(storageDir);
    }
    Utilities.mkdirsOrException(storageDir);
  }

  private static File getProjectStorageDir(int id)
  {
    File projectsDir;
    try {
      projectsDir = new File(PlatformSupport.getToolStorageDirectory(), "projects");
    }
    catch (IOException e) {
      throw new RuntimeException("Can't get GooTool storage directory");
    }
    return new File(projectsDir, "" + id);
  }

  private static int getNextId()
  {
    int nextId = -1;

    for (int i = 0; i < projects.size(); i++) {
      if (projects.get(i) == null) {
        nextId = i;
        break;
      }
    }

    if (nextId == -1) {
      nextId = projects.size();
      projects.add(null);
    }
    return nextId;
  }

  public static void deleteProject(Project project) throws IOException
  {
    initProjects();

//    if (id < 0 || id >= projects.size() || projects.get(id) == null) {
//      throw new RuntimeException("Attempt to delete non-existent project " + id);
//    }

    int id = getIdForProject(project);
    if (id == -1) throw new RuntimeException("Attempt to delete non-existent or already-deleted project " + project.getName());

    log.log(Level.INFO, "Deleting project " + project.getName() + " id " + id);

    // Remove project from our local list
    projects.set(id, null);

    // Remove this project's prefs and our pref for its type
    Preferences projectPrefs = getProjectPrefs(id);
    try {
      projectPrefs.removeNode();
      projectPrefs.flush();

      PREFS.remove(id + PREF_KEY_SUFFIX_TYPE);
    }
    catch (BackingStoreException e) {
      log.log(Level.SEVERE, "Unable to remove prefs node for project " + id);
      throw new RuntimeException("Unable to remove prefs node for project", e);
    }

    Utilities.flushPrefs(PREFS);

    File storageDir = getProjectStorageDir(id);
    if (storageDir.exists()) {
      Utilities.rmdirAll(storageDir);
    }

    // Remove any trailing null entries from the list (there may be more than one if there was a hole just before the end)
    while (!projects.isEmpty() && projects.get(projects.size() - 1) == null) {
      projects.remove(projects.size() - 1);
    }
  }

  private static int getIdForProject(Project project)
  {
    int id = -1;
    for (int i = 0; i < projects.size(); i++) {
      if (projects.get(i) == project) {
        id = i;
      }
    }
    return id;
  }

  /**
   * Migrates preferences from pre-1.1.0 single-project versions to new project number 0.
   */
  public static void migratePreferences1_1_0()
  {
    log.log(Level.INFO, GooTool.getTextProvider().getString("launcher.upgrade.1.1.0.status"));

    Preferences oldPrefs = GooTool.getPreferences();

    Preferences newPrefs = getProjectPrefs(0);

    migratePrefString(oldPrefs, "wog_dir", newPrefs, LocalProject.PREF_KEY_SOURCE_DIR);
    migratePrefString(oldPrefs, "custom_dir", newPrefs, LocalProject.PREF_KEY_TARGET_DIR);
    migratePrefString(oldPrefs, LocalProject.PREF_KEY_PROFILE_FILE, newPrefs, LocalProject.PREF_KEY_PROFILE_FILE);

    migratePrefString(oldPrefs, LocalProject.PREF_KEY_LANGUAGE, newPrefs, LocalProject.PREF_KEY_LANGUAGE);
    migratePrefInt(oldPrefs, LocalProject.PREF_KEY_SCREENWIDTH, newPrefs, LocalProject.PREF_KEY_SCREENWIDTH);
    migratePrefInt(oldPrefs, LocalProject.PREF_KEY_SCREENHEIGHT, newPrefs, LocalProject.PREF_KEY_SCREENHEIGHT);
    migratePrefInt(oldPrefs, LocalProject.PREF_KEY_REFRESHRATE, newPrefs, LocalProject.PREF_KEY_REFRESHRATE);
    migratePrefInt(oldPrefs, LocalProject.PREF_KEY_UIINSET, newPrefs, LocalProject.PREF_KEY_UIINSET);
    migratePrefBoolean(oldPrefs, LocalProject.PREF_KEY_WINDOWS_VOLUME_CONTROL, newPrefs, LocalProject.PREF_KEY_WINDOWS_VOLUME_CONTROL);

    migratePrefBoolean(oldPrefs, Project.PREF_KEY_SKIP_OPENING_MOVIE, newPrefs, Project.PREF_KEY_SKIP_OPENING_MOVIE);
    migratePrefString(oldPrefs, Project.PREF_KEY_WATERMARK, newPrefs, Project.PREF_KEY_WATERMARK);
    migratePrefString(oldPrefs, Project.PREF_KEY_BILLBOARDS_DISABLED, newPrefs, Project.PREF_KEY_BILLBOARDS_DISABLED);
    migratePrefString(oldPrefs, Project.PREF_KEY_ADDINS, newPrefs, Project.PREF_KEY_ADDINS);

    newPrefs.put(Project.PREF_KEY_NAME, GooTool.getTextProvider().getString("project.name.migrated"));

    PREFS.put("0" + PREF_KEY_SUFFIX_TYPE, PREF_TYPE_LOCAL);

    Utilities.flushPrefs(PREFS);
    Utilities.flushPrefs(newPrefs);
    Utilities.flushPrefs(oldPrefs);
  }

  private static void migratePrefString(Preferences oldPrefs, String oldPrefKey, Preferences newPrefs, String newPrefKey)
  {
    String s = oldPrefs.get(oldPrefKey, null);
    if (s != null) {
      newPrefs.put(newPrefKey, s);
      oldPrefs.remove(oldPrefKey);
    }
  }

  private static void migratePrefBoolean(Preferences oldPrefs, String oldPrefKey, Preferences newPrefs, String newPrefKey)
  {
    if (oldPrefs.get(oldPrefKey, null) != null) {
      boolean b = oldPrefs.getBoolean(oldPrefKey, false);
      newPrefs.putBoolean(newPrefKey, b);
      oldPrefs.remove(oldPrefKey);
    }
  }

  private static void migratePrefInt(Preferences oldPrefs, String oldPrefKey, Preferences newPrefs, String newPrefKey)
  {
    if (oldPrefs.get(oldPrefKey, null) != null) {
      int i = oldPrefs.getInt(oldPrefKey, 0);
      newPrefs.putInt(newPrefKey, i);
      oldPrefs.remove(oldPrefKey);
    }
  }

  /**
   * Gets the currently selected project, if one exists. If not, returns first available project, or null if there are no projects.
   *
   * @return A Project, or null if there are no projects.
   */
  public static Project getCurrentProject()
  {
    System.out.println("Get current project");
    initProjects();
    if (projects.isEmpty()) return null;

    // If they have a saved current_project, and it's still valid, use it

    int currentProjectId = PREFS.getInt(PREF_KEY_CURRENT_PROJECT, -1);
    if (currentProjectId >= 0 && projects.get(currentProjectId) != null) {
      System.out.println("Get current project, saved (" + currentProjectId + "): " + projects.get(currentProjectId));
      return projects.get(currentProjectId);
    }

    // No saved current_project, use the first existing project.

    for (Project project : projects) {
      if (project != null) {
        System.out.println("Get current project, first available: " + project);
        return project;
      }
    }

    return null;
  }

  /**
   * Sets the currently-selected project, for use on next startup.
   *
   * @param project The current project.
   */
  public static void setCurrentProject(Project project)
  {
    int id = getIdForProject(project);
    if (id == -1) throw new RuntimeException("Attempt to set current project to non-existent or already-deleted project " + project.getName());

    PREFS.putInt(PREF_KEY_CURRENT_PROJECT, id);
    Utilities.flushPrefs(PREFS);
  }

  /**
   * Returns the currently-selected project. Only for use by psvm test cases.
   *
   * @return the current Project.
   */
  public static Project simpleInit()
  {
    return getCurrentProject();
  }
}
