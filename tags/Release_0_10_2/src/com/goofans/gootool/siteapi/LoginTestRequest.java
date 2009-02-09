package com.goofans.gootool.siteapi;

import java.io.IOException;
import java.util.logging.Logger;

import com.goofans.gootool.util.DebugUtil;
import org.w3c.dom.Document;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LoginTestRequest extends APIRequestAuthenticated
{
  private static final Logger log = Logger.getLogger(ProfileBackupRequest.class.getName());

  public LoginTestRequest() throws APIException
  {
    super(API_LOGIN_TEST);
  }

  public void loginTest() throws APIException
  {
    Document doc = doRequest();

    if (!doc.getDocumentElement().getTagName().equalsIgnoreCase("login-test-success")) {
      throw new APIException("Login test failed");
    }
  }

  public static void main(String[] args) throws APIException, IOException
  {
    DebugUtil.setAllLogging();

    new LoginTestRequest().loginTest();
  }
}
