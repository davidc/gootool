package com.goofans.gootool.io;

import com.goofans.gootool.platform.PlatformSupport;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GameFormat
{
  private static final Logger log = Logger.getLogger(GameFormat.class.getName());

  public static String decodeBinFile(File file) throws IOException
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

  public static void encodeBinFile(File file, String input) throws IOException
  {
    log.finest("encode bin file: " + file);
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
        AESBinFormat.encodeFile(file, input);
        return;
      case MACOSX:
        MacBinFormat.encodeFile(file, input);
        return;
    }
  }

  public static String decodeProfileFile(File file) throws IOException
  {
    log.finest("decode profile file: " + file);
    switch (PlatformSupport.getPlatform()) {
      case WINDOWS:
        return AESBinFormat.decodeFile(file);
      case MACOSX:
        return MacBinFormat.decodeFile(file);
    }
    return null;
  }

}
