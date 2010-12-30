/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.io;

import com.goofans.gootool.facades.ReadableFile;
import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.facades.TargetFile;
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

  public static final Codec AES_BIN_CODEC = new AESBinFormat();
  public static final Codec MAC_BIN_CODEC = new MacBinFormat();

  private GameFormat()
  {
  }

  @Deprecated
  public static byte[] decodeProfileFile(File file) throws IOException
  {
    log.finest("decode profile file: " + file);

    byte[] decoded = null;
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
      case LINUX:
        decoded = AES_BIN_CODEC.decodeFile(file);
        break;
      case MACOSX:
        decoded = MAC_BIN_CODEC.decodeFile(file);
        break;
    }
    return decoded;
  }

  // pass File WITH binltl suffix if required
  @Deprecated
  public static BufferedImage decodeImage(SourceFile file) throws IOException
  {
    if (file == null) throw new RuntimeException("null image passed to decodeImage");
    if (!file.isFile()) throw new IOException("File " + file + " not found in decodeImage");
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
      case LINUX:
        return ImageIO.read(file.read());
      case MACOSX:
        return MacGraphicFormat.decodeImage(file.read()); //NON-NLS
    }
    return null;
  }
}

