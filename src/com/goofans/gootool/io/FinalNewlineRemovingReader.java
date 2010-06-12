/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.io;

import java.io.*;

import com.goofans.gootool.util.Utilities;

/**
 * A reader that can be inserted into the pipeline that returns the input reader verbatim, minus any
 * final CR or LF characters at the end of the file. If there are more than one, this will remove up
 * to the limit (default 20) sequential CR or LF characters at the end.
 * <p/>
 * Since this class involves a lot of single-character reading, it is recommended that a BufferedReader
 * is part of the source chain.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class FinalNewlineRemovingReader extends Reader
{
  private final PushbackReader reader;
  private static final int DEFAULT_BUFFER_BYTES = 20;

  private final int bufferSize;

  /**
   * Constructs a reader that will remove up to the default number of CR/LF characters (20) at the end of file.
   *
   * @param reader The reader to read from.
   */
  public FinalNewlineRemovingReader(Reader reader)
  {
    this(reader, DEFAULT_BUFFER_BYTES);
  }

  /**
   * Constructs a reader that will remove up to the given number of CR/LF characters at the end of the file.
   *
   * @param reader     The reader to read from.
   * @param bufferSize The maximum number of characters to remove.
   */
  public FinalNewlineRemovingReader(Reader reader, int bufferSize)
  {
    this.bufferSize = bufferSize;
    this.reader = new PushbackReader(reader, bufferSize);
  }


  /**
   * Read a single character.  This method will block until a character is
   * available, an I/O error occurs, or the end of the stream is reached.
   * Any CR or LF characters at the end of the file (up to the limit) are not returned.
   *
   * @return The character read, as an integer in the range 0 to 65535
   *         (<tt>0x00-0xffff</tt>), or -1 if the end of the stream has
   *         been reached
   * @throws IOException If an I/O error occurs
   */
  @Override
  public int read() throws IOException
  {
    int ch = reader.read();
    if (ch != 10 && ch != 13) return ch;

    // Look ahead for either a non-newline, or EOF

    int[] consumedChars = new int[bufferSize];
    int numConsumed = 0;
    do {
      int nextCh = reader.read();
      if (nextCh == -1) return -1;
      consumedChars[numConsumed++] = nextCh;

      if (nextCh != 10 && nextCh != 13) {
        break;
      }
    } while (numConsumed < bufferSize);

    // push everything back
    while (numConsumed > 1) {
      reader.unread(consumedChars[--numConsumed]);
    }
    return consumedChars[0];
  }

  /**
   * Read characters into a portion of an array.  This method will block
   * until some input is available, an I/O error occurs, or the end of the
   * stream is reached.
   * <p/>
   * This method wraps around read() and thus is quite inefficient - a
   * BufferedReader in the chain is recommended.
   *
   * @param cbuf Destination buffer
   * @param off  Offset at which to start storing characters
   * @param len  Maximum number of characters to read
   * @return The number of characters read, or -1 if the end of the
   *         stream has been reached
   * @throws IOException If an I/O error occurs
   */
  @Override
  public int read(char[] cbuf, int off, int len) throws IOException
  {
    int read = 0;
    while (read < len) {
      int ch = read();
      if (ch == -1) return (read == 0 ? -1 : read);

      cbuf[off + read] = (char) ch;
      read++;
    }

    return read;
  }

  @Override
  public boolean ready() throws IOException
  {
    return reader.ready();
  }

  @Override
  public void close() throws IOException
  {
    reader.close();
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "HardcodedFileSeparator"})
  public static void main(String[] args) throws IOException
  {
    File f = new File("addins/src/com.goofans.davidc.jingleballs/merge/properties/fx.xml.xsl");
    Reader r = new FinalNewlineRemovingReader(new FileReader(f), 30);

    System.out.println(">>" + Utilities.readReaderIntoString(r) + "<<");
    r.close();
  }
}
