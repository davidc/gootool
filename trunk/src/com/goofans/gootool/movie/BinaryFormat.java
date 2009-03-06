package com.goofans.gootool.movie;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BinaryFormat
{
  static float getFloat(byte[] arr, int offset)
  {
    int accum = 0;
    for (int shiftBy = 0; shiftBy < 32; shiftBy += 8, offset++) {
      accum |= ((long) (arr[offset] & 0xff)) << shiftBy;
    }
    return Float.intBitsToFloat(accum);
  }

  static int getInt(byte[] arr, int offset)
  {

    int accum = 0;
    for (int shiftBy = 0; shiftBy < 32; shiftBy += 8, offset++) {
      accum |= ((long) (arr[offset] & 0xff)) << shiftBy;
    }
    return accum;
  }

  static String getString(byte[] contents, int i)
  {
    StringBuilder sb = new StringBuilder();
    while (contents[i] != 0) {
      sb.append((char) contents[i]);
      ++i;
    }
    return sb.toString();
  }
}
