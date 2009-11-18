package com.goofans.gootool.siteapi;

import java.net.URL;
import java.util.logging.Logger;

import com.goofans.gootool.ToolPreferences;

/**
 * An API request that requires a user login.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class APIRequestAuthenticated extends APIRequest
{
  private static final Logger log = Logger.getLogger(APIRequestAuthenticated.class.getName());

  private static final String PARAM_USERNAME = "u";
  private static final String PARAM_PASSWORD = "p";

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
