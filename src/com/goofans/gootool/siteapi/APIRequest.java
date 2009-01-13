package com.goofans.gootool.siteapi;

import net.infotrek.util.EncodingUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class APIRequest
{
  private static final Logger log = Logger.getLogger(APIRequest.class.getName());

  public static final URL API_CHECKVERSION;
  public static final URL API_PROFILEUPLOAD;

  static {
    try {
      API_CHECKVERSION = new URL("http://api.goofans.com/gootool-version-check"); //NON-NLS
      API_PROFILEUPLOAD = new URL("http://api.goofans.com/profile-upload"); //NON-NLS
    }
    catch (MalformedURLException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private URL url;

  public APIRequest(URL url)
  {
    this.url = url;
  }

  public void addParameter(String name, String value) throws MalformedURLException
  {
    StringBuilder newUrl = new StringBuilder(url.toString());

    if (url.getQuery() != null) {
      newUrl.append("&");
    }
    else {
      newUrl.append("?");
    }

    newUrl.append(EncodingUtil.urlEncode(name)).append("=").append(EncodingUtil.urlEncode(value));

    url = new URL(newUrl.toString());
  }

  public Document doRequest() throws IOException, APIException
  {
    log.log(Level.FINE, "Doing " + toString());
    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

    urlConn.setDoInput(true);
    urlConn.setDoOutput(true);
    urlConn.setUseCaches(false);
    urlConn.addRequestProperty("GooTool-ID", ToolPreferences.getGooToolId()); //NON-NLS

    urlConn.connect();

    Document doc = XMLUtil.loadDocumentFromInputStream(urlConn.getInputStream());

    if (doc.getDocumentElement().getNodeName().equals("error")) {
      String message = XMLUtil.getElementString(doc.getDocumentElement(), "message");
      log.log(Level.WARNING, "API error. Message from server: " + message);

      throw new APIException(message);
    }

    return doc;
  }


  public String toString()
  {
    return "APIRequest{" +
            "url=" + url +
            '}';
  }
}
