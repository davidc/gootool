package net.infotrek.util;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * Wrappers around encoding functionality, using UTF-8 by default. These wrappers change UnsupportedEncodingException
 * to a runtime exception as every JVM is required to support UTF-8.
 *
 * @author David Croft (david@infotrek.net)
 * @version $Id: EncodingUtil.java 69 2005-05-07 16:51:27Z david $
 */
public class EncodingUtil
{
  private EncodingUtil()
  {
  }

  /**
   * Translates a string into application/x-www-form-urlencoded format using UTF-8
   *
   * @param s String to be translated.
   * @return the translated String.
   * @see java.net.URLEncoder#encode(String, String)
   */
  public static String urlEncode(String s)
  {
    try {
      return URLEncoder.encode(s, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 not supported", e);
    }
  }

  /**
   * Encodes this String into a sequence of bytes using UTF-8, storing the result into a new byte array.
   *
   * @param s The String to encode.
   * @return The resultant byte array
   * @see java.lang.String#getBytes(String)
   */

  public static byte[] getBytes(String s)
  {
    try {
      return s.getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 not supported", e);
    }
  }

  /**
   * Encodes this String into a sequence of bytes using the specified charset, storing the result into a new byte array.
   *
   * @param s The String to encode.
   * @param encoding The charset to use.
   * @return The resultant byte array
   * @see java.lang.String#getBytes(String)
   */

  public static byte[] getBytes(String s, String encoding)
  {
    try {
      return s.getBytes(encoding);
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Charset " + encoding + " not supported", e);
    }
  }
}
