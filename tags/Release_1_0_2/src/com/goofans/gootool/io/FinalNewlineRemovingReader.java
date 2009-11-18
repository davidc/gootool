package com.goofans.gootool.io;

import java.io.*;

import com.goofans.gootool.util.Utilities;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class FinalNewlineRemovingReader extends Reader
{
  private final PushbackReader reader;
  private static final int BUFFER_BYTES = 20;

  public FinalNewlineRemovingReader(Reader reader)
  {
    this.reader = new PushbackReader(reader, BUFFER_BYTES);
  }

  @Override
  public int read() throws IOException
  {
    int ch = reader.read();
    if (ch != 10 && ch != 13) return ch;

    // Look ahead for either a non-newline, or EOF

//    int nextCh = reader.read();
//    if (nextCh == -1) return -1;
//    if (nextCh != 10 && nextCh != 13) {
//      reader.unread(nextCh);
//      return ch;
//    }

//    int nextCh2 = reader.read();
//    if (nextCh2 == -1) return -1;
//    reader.unread(nextCh2);
//    reader.unread(nextCh);
//    return ch;

    int[] consumedChars = new int[BUFFER_BYTES];
    int numConsumed = 0;
    do {
      int nextCh = reader.read();
      if (nextCh == -1) return -1;
      consumedChars[numConsumed++] = nextCh;

      if (nextCh != 10 && nextCh != 13) {
        break;
      }
//      numConsumed++;
    } while (numConsumed < BUFFER_BYTES);

    // push everything back
    while (numConsumed > 1) {
      reader.unread(consumedChars[--numConsumed]);
    }
    return consumedChars[0];
  }

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
    Reader r = new FinalNewlineRemovingReader(new FileReader(f));

    System.out.println(">>" + Utilities.readReaderIntoString(r) + "<<");
  }
}
