/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.platform;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import com.goofans.gootool.Controller;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.util.Utilities;

import java.io.File;
import java.util.logging.Logger;
import java.util.List;

/**
 * Support routines for Mac OS X
 *
 * See http://developer.apple.com/documentation/Java/Reference/1.5.0/appledoc/api/index.html for the ApplicationListener API.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MacOSXSupport extends PlatformSupport implements ApplicationListener
{
  private static final Logger log = Logger.getLogger(MacOSXSupport.class.getName());

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String[] PROFILE_SEARCH_PATHS = {
          "%HOME%/Library/Application Support/WorldOfGoo/"
  };

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String TOOL_STORAGE_DIRECTORY = "%HOME%/Library/Application Support/GooTool/";

  private Controller controller;

  MacOSXSupport()
  {
  }

  @Override
  protected boolean doPreStartup(List<String> args)
  {
    // Mac OS X handles single instance, and our ApplicationListener handles file opening,
    // so we always permit startup.
    return true;
  }

  @Override
  protected void doStartup(Controller controller)
  {
    this.controller = controller;

    Application app = new Application();
    app.addAboutMenuItem();
    app.addApplicationListener(this);
    log.fine("Mac: listener added");
  }

  public void handleAbout(ApplicationEvent event)
  {
    log.fine("Mac: handleAbout");
    controller.openAboutDialog();
    event.setHandled(true);
  }

  public void handleOpenApplication(ApplicationEvent event)
  {
  }

  public void handleReOpenApplication(ApplicationEvent event)
  {
  }

  public void handleOpenFile(ApplicationEvent event)
  {
    log.fine("Mac: handleOpenFile");
    final File addinFile = new File(event.getFilename());
    GooTool.queueTask(new Runnable()
    {
      public void run()
      {
        controller.installAddin(addinFile);
      }
    });
    event.setHandled(true);
  }

  public void handlePreferences(ApplicationEvent event)
  {
  }

  public void handlePrintFile(ApplicationEvent event)
  {
  }

  public void handleQuit(ApplicationEvent event)
  {
    log.fine("Mac: handleQuit");
    controller.maybeExit();
//    event.setHandled(true);  this causes us to always quit even if they cancel
  }

  @Override
  public String[] doGetProfileSearchPaths()
  {
    return PROFILE_SEARCH_PATHS;
  }

  @Override
  public File doGetToolStorageDirectory()
  {
    return new File(Utilities.expandEnvVars(TOOL_STORAGE_DIRECTORY));
  }
}
