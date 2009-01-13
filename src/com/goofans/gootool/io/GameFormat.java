package com.goofans.gootool.io;

import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.XMLUtil;

import java.io.*;
import java.util.logging.Logger;

import org.w3c.dom.Document;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GameFormat
{
  private static final Logger log = Logger.getLogger(GameFormat.class.getName());
  public static final String DEFAULT_CHARSET = "UTF-8";

  public static byte[] decodeBinFile(File file) throws IOException
  {
    log.finest("decode bin file: " + file);
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
        return AESBinFormat.decodeFile(file);
      case MACOSX:
        return MacBinFormat.decodeFile(file);
    }
    return null;
  }

  public static void encodeBinFile(File file, byte[] input) throws IOException
  {
    log.finest("encode bin file: " + file);

    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
        AESBinFormat.encodeFile(file, input);
        break;
      case MACOSX:
        MacBinFormat.encodeFile(file, input);
        break;
    }
  }

  public static Document decodeXmlBinFile(File file) throws IOException
  {
    byte[] decoded = decodeBinFile(file);
    InputStream is = new ByteArrayInputStream(decoded);
    return XMLUtil.loadDocumentFromInputStream(is);
  }

  public static String decodeProfileFile(File file) throws IOException
  {
    log.finest("decode profile file: " + file);
    byte[] decoded = null;
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
        decoded = AESBinFormat.decodeFile(file);
        break;
      case MACOSX:
        decoded = MacBinFormat.decodeFile(file);
        break;
    }
    return new String(decoded, DEFAULT_CHARSET);
  }
}
