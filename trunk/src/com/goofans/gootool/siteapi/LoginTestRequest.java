package com.goofans.gootool.siteapi;

import java.io.IOException;
import java.util.logging.Logger;

import com.goofans.gootool.util.DebugUtil;
import org.w3c.dom.Document;

/**
 * A simple authenticated request that does nothing, but does throw an APIException if the user login failed.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LoginTestRequest extends APIRequestAuthenticated
{
  public LoginTestRequest() throws APIException
  {
    super(API_LOGIN_TEST);
  }

  public void loginTest() throws APIException
  {
    Document doc = doRequest();

    if (!"login-test-success".equalsIgnoreCase(doc.getDocumentElement().getTagName())) {
      throw new APIException("Login test failed");
    }
  }

  public static void main(String[] args) throws APIException, IOException
  {
    DebugUtil.setAllLogging();

    new LoginTestRequest().loginTest();
  }
}
