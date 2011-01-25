/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.ios;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.GooToolResourceBundle;
import com.goofans.gootool.util.ProgressIndicatingTask;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class TestIosConnection extends ProgressIndicatingTask
{
  private final String host;
  private final String password;

  private boolean wogFound = false;

  public TestIosConnection(String host, String password)
  {
    this.host = host;
    this.password = password;
  }

  @Override
  public void run() throws Exception
  {
    GooToolResourceBundle resourceBundle = GooTool.getTextProvider();

    beginStep(resourceBundle.getString("projectProps.ios.device.testConnection.connecting"), false);
    IosConnection con = new IosConnection(new IosConnectionParameters(host, password));

    try {
      con.connect();

      beginStep(resourceBundle.getString("projectProps.ios.device.testConnection.searching"), false);

      wogFound = con.locateWog();
    }
    finally {
      con.close();
    }
  }

  public boolean isWogFound()
  {
    return wogFound;
  }
}
