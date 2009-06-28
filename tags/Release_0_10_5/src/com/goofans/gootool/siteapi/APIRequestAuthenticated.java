package com.goofans.gootool.siteapi;

import java.net.URL;
import java.util.logging.Logger;

import com.goofans.gootool.ToolPreferences;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class APIRequestAuthenticated extends APIRequest
{
  private static final Logger log = Logger.getLogger(ProfileBackupRequest.class.getName());

  public APIRequestAuthenticated(URL url) throws APIException
  {
    super(url);

    String username = ToolPreferences.getGooFansUsername();
    String password = ToolPreferences.getGooFansPassword();

    if (username == null || password == null) {
      throw new APIException("No GooFans username or password set");
    }

    addPostParameter("u", username);
    addPostParameter("p", password);
    log.finest("Instantiated authenticated APIRequest for user " + username + " to " + url);
  }
}
