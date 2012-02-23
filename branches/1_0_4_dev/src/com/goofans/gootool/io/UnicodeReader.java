/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.io;

/**
 * Based on http://koti.mbnet.fi/akini/java/unicodereader/
 *
 * version: 1.1 / 2007-01-25
 * - changed BOM recognition ordering (longer boms first)
 *
 * Original pseudocode   : Thomas Weidenfeller
 * Implementation tweaked: Aki Nieminen
 *
 * http://www.unicode.org/unicode/faq/utf_bom.html
 * BOMs:
 *   00 00 FE FF    = UTF-32, big-endian
 *   FF FE 00 00    = UTF-32, little-endian
 *   EF BB BF       = UTF-8,
 *   FE FF          = UTF-16, big-endian
 *   FF FE          = UTF-16, little-endian
 *
 * Win2k Notepad:
 *   Unicode format = UTF-16LE
 */

import java.io.*;
import java.util.logging.Logger;

/**
 * Generic unicode textreader, which will use BOM mark
 * to identify the encoding to be used. If BOM is not found
 * then use a given default or system encoding.
 */
public class UnicodeReader extends Reader
{
  private static final Logger log = Logger.getLogger(UnicodeReader.class.getName());

  private final PushbackInputStream internalIn;
  private InputStreamReader internalIn2 = null;
  private final String defaultEnc;

  private static final int BOM_SIZE = 4;
  private static final String ENCODING_UTF_32BE = "UTF-32BE";
  private static final String ENCODING_UTF_32LE = "UTF-32LE";
  private static final String ENCODING_UTF_8 = "UTF-8";
  private static final String ENCODING_UTF_16BE = "UTF-16BE";
  private static final String ENCODING_UTF_16LE = "UTF-16LE";

  /**
   * @param in         inputstream to be read
   * @param defaultEnc default encoding if stream does not have
   *                   BOM marker. Give NULL to use system-level default.
   */
  public UnicodeReader(InputStream in, String defaultEnc)
  {
    internalIn = new PushbackInputStream(in, BOM_SIZE);
    this.defaultEnc = defaultEnc;
  }

  public String getDefaultEncoding()
  {
    return defaultEnc;
  }

  /**
   * Get stream encoding or NULL if stream is uninitialized.
   * Call init() or read() method to initialize it.
   *
   * @return the name of the encoding detected, or null if the stream is not yet initialised
   */
  public String getEncoding()
  {
    if (internalIn2 == null) return null;
    return internalIn2.getEncoding();
  }

  /**
   * Read-ahead four bytes and check for BOM marks. Extra bytes are
   * unread back to the stream, only BOM bytes are skipped.
   *
   * @throws java.io.IOException if an IOException occurs on the underlying stream
   */
  protected void init() throws IOException
  {
    if (internalIn2 != null) return;

    String encoding;
    byte[] bom = new byte[BOM_SIZE];
    int n, unread;
    n = internalIn.read(bom, 0, bom.length);

    if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) &&
            (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
      encoding = ENCODING_UTF_32BE;
      unread = n - 4;
    }
    else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) &&
            (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
      encoding = ENCODING_UTF_32LE;
      unread = n - 4;
    }
    else if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) &&
            (bom[2] == (byte) 0xBF)) {
      encoding = ENCODING_UTF_8;
      unread = n - 3;
    }
    else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
      encoding = ENCODING_UTF_16BE;
      unread = n - 2;
    }
    else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
      encoding = ENCODING_UTF_16LE;
      unread = n - 2;
    }
    else {
      // Unicode BOM mark not found, unread all bytes
      encoding = defaultEnc;
      unread = n;
    }
//    if (unread != n) {
//      log.finest("skipped BOM, " + (n - unread) + " bytes");
//    }
    //System.out.println("read=" + n + ", unread=" + unread);

    if (unread > 0) internalIn.unread(bom, (n - unread), unread);

    // Use given encoding
    if (encoding == null) {
      internalIn2 = new InputStreamReader(internalIn);
    }
    else {
      internalIn2 = new InputStreamReader(internalIn, encoding);
    }
  }

  @Override
  public void close() throws IOException
  {
    init();
    internalIn2.close();
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException
  {
    init();
    return internalIn2.read(cbuf, off, len);
  }
}
