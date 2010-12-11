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
public class BigEndianBinaryBuffer implements BinaryBuffer
{
  private final byte[] buf;
  private int pos;

  public BigEndianBinaryBuffer(byte[] buf)
  {
    if (buf == null) throw new RuntimeException("buffer passed to constructor is null");
    this.buf = buf;
  }

  public void setPosition(int pos)
  {
    if (pos < 0 || pos >= buf.length) throw new RuntimeException("setPosition out of bounds: " + pos);
    this.pos = pos;
  }

  public int getLength()
  {
    return buf.length;
  }

  public byte[] getBuffer()
  {
    return buf;
  }

  public short getByte()
  {
    return (short) (buf[pos++] & 0xff);
  }

  public short getByte(int offset)
  {
    return (short) (buf[offset] & 0xff);
  }

  public int getShort()
  {
    int value = getShort(pos);
    pos += 2;
    return value;
  }

  public int getShort(int offset)
  {
    return ((buf[offset] << 8) | (buf[offset + 1]) & 0xff);
  }

  public long getInt()
  {
    long value = getInt(pos);
    pos += 4;
    return value;
  }

  public long getInt(int offset)
  {
    return (((long) buf[offset] & 0xff) << 24) | (((long)buf[offset + 1] & 0xff) << 16) | (((long)buf[offset + 2] & 0xff) << 8) | ((buf[offset + 3]) & 0xff);
  }

  public long getLong()
  {
    return ((long) (getInt()) << 32) | (getInt() & 0xFFFFFFFFL);
  }

  public long getLong(int offset)
  {
    return ((long) (getInt(offset)) << 32) + (getInt(offset + 4) & 0xFFFFFFFFL);
  }

  public double getDouble()
  {
    return Double.longBitsToDouble(getLong());
  }

  public double getDouble(int offset)
  {
    return Double.longBitsToDouble(getLong(offset));
  }

  public String getString(int length, String charset) throws UnsupportedEncodingException
  {
    String s = getString(pos, length, charset);
    pos += length;
    return s;
  }

  public String getString(int offset, int length, String charset) throws UnsupportedEncodingException
  {
    return new String(buf, offset, length, "ASCII");
  }

  public byte[] getBytes(int length)
  {
    byte[] data = getBytes(pos, length);
    pos += length;
    return data;
  }

  public byte[] getBytes(int offset, int length)
  {
    byte[] data = new byte[length];
    System.arraycopy(buf, offset, data, 0, length);
    return data;
  }
}
