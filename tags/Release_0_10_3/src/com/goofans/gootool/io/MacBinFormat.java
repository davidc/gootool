package com.goofans.gootool.io;

import com.goofans.gootool.util.Utilities;

import java.io.File;
import java.io.IOException;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MacBinFormat
{
  public static byte[] decodeFile(File file) throws IOException
  {
    byte[] inputBytes = Utilities.readFile(file);
    return decode(inputBytes);
  }

  private static byte[] decode(byte[] inputBytes) throws IOException
  {
    int length = inputBytes.length;
    byte[] outputBytes = new byte[length];

    int salt = (((length & 1) << 6) | ((length & 2) << 3) | (length & 4)) ^ 0xab;

    for (int i = 0; i < length; ++i) {
      byte inByte = inputBytes[i];
      outputBytes[i] = (byte) (salt ^ inByte);

      salt = ((salt & 0x7f) << 1 | (salt & 0x80) >> 7) ^ inByte;
    }

//    return new String(outputBytes, 0, length, CHARSET);
    return outputBytes;
  }

  public static void encodeFile(File file, byte[] inputBytes) throws IOException
  {
    byte[] bytes = encode(inputBytes);
    Utilities.writeFile(file, bytes);
  }

  private static byte[] encode(byte[] inputBytes) throws IOException
  {
//    byte[] inputBytes = inputBytes.getBytes(CHARSET);
    int length = inputBytes.length;
    byte[] outputBytes = new byte[length];

    int salt = (((length & 1) << 6) | ((length & 2) << 3) | (length & 4)) ^ 0xab;

    for (int i = 0; i < length; ++i) {
      byte inByte = inputBytes[i];
      byte newByte = (byte) (salt ^ inByte);
      outputBytes[i] = newByte;

      salt = ((salt & 0x7f) << 1 | (salt & 0x80) >> 7) ^ newByte;
    }

    return outputBytes;
  }

  @SuppressWarnings({"HardCodedStringLiteral", "UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    String s = new String(decodeFile(new File("IvyTower.level.bin")), GameFormat.DEFAULT_CHARSET);
    System.out.println("s = " + s);

    byte[] inputBytes = Utilities.readFile(new File("IvyTower.level.bin"));
    System.out.print(new String(decode(encode(decode(inputBytes)))));

  }
}
