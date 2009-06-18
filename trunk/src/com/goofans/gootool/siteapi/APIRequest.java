package com.goofans.gootool.siteapi;

import net.infotrek.util.EncodingUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.util.Version;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.util.Utilities;
import org.w3c.dom.Document;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class APIRequest
{
  private static final Logger log = Logger.getLogger(APIRequest.class.getName());

  protected static final DateFormat API_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH); //NON-NLS 

  protected static final URL API_CHECKVERSION;
  protected static final URL API_LOGIN_TEST;
  protected static final URL API_PROFILE_BACKUP;
  protected static final URL API_PROFILE_RESTORE;
  protected static final URL API_PROFILE_LIST;
  protected static final URL API_PROFILE_PUBLISH;

  static {
    API_DATEFORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));

    try {
      String apiBase = "http://api.goofans.com/";
      API_CHECKVERSION = new URL(apiBase + "gootool-version-check");
      API_LOGIN_TEST = new URL(apiBase + "login-test");
      API_PROFILE_BACKUP = new URL(apiBase + "profile-backup");
      API_PROFILE_RESTORE = new URL(apiBase + "profile-restore");
      API_PROFILE_LIST = new URL(apiBase + "profile-list");
      API_PROFILE_PUBLISH = new URL(apiBase + "profile-publish");
    }
    catch (MalformedURLException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private URL url;
  private Map<String, String> postParameters = new HashMap<String, String>();
  private boolean executed;

  protected APIRequest(URL url)
  {
    this.url = url;
  }

  protected void addGetParameter(String name, String value) throws APIException
  {
    StringBuilder newUrl = new StringBuilder(url.toString());

    if (url.getQuery() != null) {
      newUrl.append("&");
    }
    else {
      newUrl.append("?");
    }

    newUrl.append(EncodingUtil.urlEncode(name)).append("=").append(EncodingUtil.urlEncode(value));

    try {
      url = new URL(newUrl.toString());
    }
    catch (MalformedURLException e) {
      throw new APIException("Malformed URL setting GET parameter " + name, e);
    }
  }

  protected void addPostParameter(String name, String value)
  {
    postParameters.put(name, value);
  }

  protected Document doRequest() throws APIException
  {
    log.log(Level.FINE, "Doing " + toString());
    try {
      InputStream is = doRequestInt();
      Document doc = XMLUtil.loadDocumentFromInputStream(is);

      if (doc.getDocumentElement().getNodeName().equals("error")) {
        String message = XMLUtil.getElementString(doc.getDocumentElement(), "message");
        log.log(Level.WARNING, "API error. Message from server: " + message);

        throw new APIException(message);
      }

      return doc;
    }
    catch (IOException e) {
      throw new APIException("IOException on API call", e);
    }
  }

  protected InputStream doRequestInt() throws IOException
  {
    if (executed)
      throw new RuntimeException("Request has already been executed");
    executed = true;

    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

    urlConn.setDoInput(true);
    urlConn.setDoOutput(true);
    urlConn.setUseCaches(false);
    urlConn.setRequestProperty("GooTool-ID", ToolPreferences.getGooToolId()); //NON-NLS

    StringBuilder platformStr = new StringBuilder("GooTool;");
    platformStr.append(Version.RELEASE_FULL)
            .append(';')
            .append(System.getProperty("java.version").replaceAll(";", "_"))
            .append(';')
            .append(System.getProperty("os.name").replaceAll(";", "_"))
            .append(';')
            .append(System.getProperty("os.version").replaceAll(";", "_"))
            .append(';')
            .append(System.getProperty("os.arch").replaceAll(";", "_"));

    urlConn.setRequestProperty("User-Agent", platformStr.toString());

    if (!postParameters.isEmpty()) {
      urlConn.setRequestMethod("POST");
      StringBuilder postContent = new StringBuilder();
      for (Map.Entry<String, String> postParameter : postParameters.entrySet()) {
        if (postContent.length() > 0) postContent.append("&");
        postContent.append(EncodingUtil.urlEncode(postParameter.getKey()))
                .append("=")
                .append(EncodingUtil.urlEncode(postParameter.getValue()));
      }
      urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      urlConn.setRequestProperty("Content-Length", Integer.toString(postContent.length()));

      DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());
      out.writeBytes(postContent.toString());
      out.flush();
      out.close();
    }

    urlConn.connect();

    return urlConn.getInputStream();
  }


  public String toString()
  {
    return "APIRequest{" +
            "url=" + url +
            '}';
  }
}
