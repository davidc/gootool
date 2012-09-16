/*
 * Copyright (c) 2008, 2009, 2010, 2011, 2012 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import java.io.IOException;
import java.util.logging.Logger;

import com.goofans.gootool.util.DebugUtil;
import org.w3c.dom.Document;

/**
 * A simple authenticated request that does nothing, but does throw an APIException if the user login failed.
 * Used when the user enters their username/password to check that it is correct.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LoginTestRequest extends APIRequest
{
  private static final Logger log = Logger.getLogger(LoginTestRequest.class.getName());

  public LoginTestRequest(String username, String password) throws APIException
  {
    super(API_LOGIN_TEST);

    if (username == null || password == null) {
      throw new APIException("No GooFans username or password set");
    }

    addPostParameter(PARAM_USERNAME, username);
    addPostParameter(PARAM_PASSWORD, password);
    log.finest("Instantiated authenticated LoginTestRequest for user " + username);
  }

  public void loginTest() throws APIException
  {
    Document doc = doRequest();

    if (!"login-test-success".equalsIgnoreCase(doc.getDocumentElement().getTagName())) { //NON-NLS
      throw new APIException("Login test failed"); //NON-NLS
    }
  }

  public static void main(String[] args) throws APIException, IOException
  {
    DebugUtil.setAllLogging();

    new LoginTestRequest("blah", "blah").loginTest();
  }
}
