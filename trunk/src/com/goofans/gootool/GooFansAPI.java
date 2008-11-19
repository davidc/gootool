package com.goofans.gootool;

import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GooFansAPI
{
  private GooFansAPI()
  {
  }

  public static URLConnection createAPIConnection(URL url) throws IOException
  {
    URLConnection urlConn = url.openConnection();

    urlConn.setDoInput(true);
    urlConn.setDoOutput(true);
    urlConn.setUseCaches(false);
    urlConn.addRequestProperty("GooTool-ID", ToolPreferences.getGooToolId());
    return urlConn;
  }
}
