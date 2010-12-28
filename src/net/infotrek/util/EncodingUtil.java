/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package net.infotrek.util;

import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * Wrappers around encoding functionality, using UTF-8 by default. These wrappers change UnsupportedEncodingException
 * to a runtime exception as every JVM is required to support UTF-8.
 *
 * @author David Croft (david@infotrek.net)
 * @version $Id$
 */
public class EncodingUtil
{
  private static final String UTF_8 = "UTF-8";

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
      return URLEncoder.encode(s, UTF_8);
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
      return s.getBytes(UTF_8);
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

  public static byte[] stringToBytesUtf8(String s)
  {
    try {
      return s.getBytes(UTF_8);
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 unsupported!");
    }
  }

  public static String bytesToStringUtf8(byte[] b)
  {
    try {
      return new String(b, UTF_8);
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException("UTF-8 unsupported!");
    }
  }
}
