package com.goofans.gootool.platform;

import javax.swing.*;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import com.goofans.gootool.Controller;
import com.goofans.gootool.GooTool;

import java.io.File;
import java.util.logging.Logger;

/**
 * See http://developer.apple.com/documentation/Java/Reference/1.5.0/appledoc/api/index.html
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

  private Controller controller;

  MacOSXSupport()
  {
  }

  protected boolean doPreStartup(String[] args)
  {
    // Mac OS X handles single instance, and our ApplicationListener handles file opening,
    // so we always permit startup.
    return true;
  }

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

  public String[] doGetProfileSearchPaths()
  {
    return PROFILE_SEARCH_PATHS;
  }
}
