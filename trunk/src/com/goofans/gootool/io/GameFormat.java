/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.io;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GameFormat
{
  public static final String DEFAULT_CHARSET = "UTF-8";

  public static final Codec AES_BIN_CODEC = new AESBinFormat();
  public static final Codec MAC_BIN_CODEC = new MacBinFormat();
  public static final Codec NULL_BIN_CODEC = new NullBinFormat();

  public static final ImageCodec NULL_IMAGE_CODEC = new NullImageCodec();
  public static final ImageCodec MAC_IMAGE_CODEC = new MacImageCodec();

  public static final String PNG_FORMAT = "PNG";

  private GameFormat()
  {
  }
}

