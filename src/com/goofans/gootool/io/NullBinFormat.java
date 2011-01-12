/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.io;

import java.io.IOException;

/**
 * A do-nothing codec for platforms without any encryption (i.e. iOS).
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class NullBinFormat extends Codec
{
  @Override
  public byte[] decode(byte[] inputBytes) throws IOException
  {
    return inputBytes;
  }

  @Override
  public byte[] encode(byte[] inputBytes) throws IOException
  {
    return inputBytes;
  }
}
