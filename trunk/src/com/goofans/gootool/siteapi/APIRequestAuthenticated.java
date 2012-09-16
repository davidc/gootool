/*
 * Copyright (c) 2008, 2009, 2010, 2011, 2012 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import java.net.URL;
import java.util.logging.Logger;

import com.goofans.gootool.ToolPreferences;

/**
 * Superclass for requests against the goofans.com API that requires a user login.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public abstract class APIRequestAuthenticated extends APIRequest
{
  private static final Logger log = Logger.getLogger(APIRequestAuthenticated.class.getName());

  protected APIRequestAuthenticated(URL url) throws APIException
  {
    super(url);

    String username = ToolPreferences.getGooFansUsername();
    String password = ToolPreferences.getGooFansPassword();

    if (username == null || password == null) {
      throw new APIException("No GooFans username or password set");
    }

    addPostParameter(PARAM_USERNAME, username);
    addPostParameter(PARAM_PASSWORD, password);
    log.finest("Instantiated authenticated APIRequest for user " + username + " to " + url);
  }
}
