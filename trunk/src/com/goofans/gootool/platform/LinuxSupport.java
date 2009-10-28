package com.goofans.gootool.platform;

import java.util.List;

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

  private final SingleInstance singleInstance = new SingleInstance();

  LinuxSupport()
  {
  }

  @Override
  protected boolean doPreStartup(List<String> args)
  {
    return singleInstance.singleInstance(args);
  }

  @Override
  protected void doStartup(Controller controller)
  {
  }

  @Override
  public String[] doGetProfileSearchPaths()
  {
    return PROFILE_SEARCH_PATHS;
  }
}
