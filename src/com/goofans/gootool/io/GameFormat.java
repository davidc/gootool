/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.io;

import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.XMLUtil;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import javax.imageio.ImageIO;

/**
 *  TODO should use source/target platform, not host
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GameFormat
{
  private static final Logger log = Logger.getLogger(GameFormat.class.getName());
  public static final String DEFAULT_CHARSET = "UTF-8";

  private GameFormat()
  {
  }

  public static byte[] decodeBinFile(File file) throws IOException
  {
    log.finest("decode bin file: " + file);

    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
      case LINUX:
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
      case LINUX:
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

  public static byte[] decodeProfileFile(File file) throws IOException
  {
    log.finest("decode profile file: " + file);

    byte[] decoded = null;
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
      case LINUX:
        decoded = AESBinFormat.decodeFile(file);
        break;
      case MACOSX:
        decoded = MacBinFormat.decodeFile(file);
        break;
    }
    return decoded;
  }

  public static void encodeProfileFile(File file, byte[] input) throws IOException
  {
    log.finest("encode profile file: " + file);

    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
      case LINUX:
        AESBinFormat.encodeFile(file, input);
        break;
      case MACOSX:
        MacBinFormat.encodeFile(file, input);
        break;
    }
  }

  // pass File WITHOUT binltl suffix
  public static BufferedImage decodeImage(File file) throws IOException
  {
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
      case LINUX:
        return ImageIO.read(file);
      case MACOSX:
        return MacGraphicFormat.decodeImage(new File(file.getParent(), file.getName() + ".binltl"));
    }
    return null;
  }
}

