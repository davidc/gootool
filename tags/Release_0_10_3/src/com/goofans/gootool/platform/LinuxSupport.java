package com.goofans.gootool.platform;

import com.goofans.gootool.Controller;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LinuxSupport extends PlatformSupport
{
//  private static final Logger log = Logger.getLogger(LinuxSupport.class.getName());

  @SuppressWarnings({"HardcodedFileSeparator"})
  private static final String[] PROFILE_SEARCH_PATHS = {
          "%HOME%/.WorldOfGoo"
  };

  private Controller controller;

  private SingleInstance singleInstance = new SingleInstance();

  LinuxSupport()
  {
  }

  protected boolean doPreStartup(String[] args)
  {
    return singleInstance.singleInstance(args);
  }

  protected void doStartup(Controller controller)
  {
    this.controller = controller;
  }

  public String[] doGetProfileSearchPaths()
  {
    return PROFILE_SEARCH_PATHS;
  }
}
