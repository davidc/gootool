/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package net.infotrek.util;

import java.io.UnsupportedEncodingException;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface BinaryBuffer
{
  void setPosition(int pos);

  int getLength();

  byte[] getBuffer();

  short getByte();

  short getByte(int offset);

  int getShort();

  int getShort(int offset);

  long getInt();

  long getInt(int offset);

  long getLong();

  long getLong(int offset);

  double getDouble();

  double getDouble(int offset);

  String getString(int length, String charset) throws UnsupportedEncodingException;

  String getString(int offset, int length, String charset) throws UnsupportedEncodingException;

  byte[] getBytes(int length);

  byte[] getBytes(int offset, int length);
}