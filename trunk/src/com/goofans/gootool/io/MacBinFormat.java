/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.io;

import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.util.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Encrypt/decrypt .bin files in XOR format (Mac).
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MacBinFormat extends Codec
{
  MacBinFormat()
  {
  }

  public byte[] decode(byte[] inputBytes)
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

  public byte[] encode(byte[] inputBytes)
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

  @SuppressWarnings({"HardCodedStringLiteral", "UseOfSystemOutOrSystemErr", "DuplicateStringLiteralInspection"})
  public static void main(String[] args) throws IOException
  {
    MacBinFormat codec = new MacBinFormat();

    String s = new String(codec.decodeFile(new File("IvyTower.level.bin")), GameFormat.DEFAULT_CHARSET);
    System.out.println("s = " + s);

    byte[] inputBytes = Utilities.readFile(new File("IvyTower.level.bin"));
    System.out.print(new String(codec.decode(codec.encode(codec.decode(inputBytes)))));
  }
}
