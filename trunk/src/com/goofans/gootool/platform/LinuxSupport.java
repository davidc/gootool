/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.platform;

import java.util.List;

import com.goofans.gootool.Controller;

/**
 * Support routines for Linux.
 *
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

  LinuxSupport()
  {
  }

  @Override
  protected boolean doPreStartup(List<String> args)
  {
    return SingleInstance.getInstance().singleInstance(args);
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
