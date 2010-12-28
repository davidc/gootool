/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.io;

import java.io.*;

import com.goofans.gootool.facades.ReadableFile;
import com.goofans.gootool.facades.TargetFile;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;

/**
 * Superclass that all game-file encryption codecs inherit from. Provides standard implementation of decoding/encoding from files.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public abstract class Codec
{
  public byte[] decodeFile(File file) throws IOException
  {
    byte[] inputBytes = Utilities.readFile(file);
    return decode(inputBytes);
  }

  public byte[] decodeFile(ReadableFile file) throws IOException
  {
    InputStream is = file.read();
    try {
      byte[] inputBytes = Utilities.readStream(is);
      return decode(inputBytes);
    }
    finally {
      is.close();
    }
  }

  @Deprecated // TODO inline this
  public Document decodeFileToXML(ReadableFile file) throws IOException
  {
    byte[] decoded = decodeFile(file);
    InputStream is = new ByteArrayInputStream(decoded);
    return XMLUtil.loadDocumentFromInputStream(is);
  }


  public abstract byte[] decode(byte[] inputBytes) throws IOException;

  public void encodeFile(File file, byte[] input) throws IOException
  {
    byte[] bytes = encode(input);
    Utilities.writeFile(file, bytes);
  }

  public void encodeFile(TargetFile file, byte[] input) throws IOException
  {
    OutputStream os = file.write();
    try {
      os.write(encode(input));
    }
    finally {
      os.close();
    }
  }

  public abstract byte[] encode(byte[] inputBytes) throws IOException;
}
