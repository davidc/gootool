package com.goofans.gootool.util;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.channels.FileChannel;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Utilities
{
  private static final Logger log = Logger.getLogger(Utilities.class.getName());

  public static String expandEnvVars(String s)
  {
    int first, second;

    while ((first = s.indexOf('%')) != -1) {
      second = s.indexOf('%', first + 1);
      if (second == -1) return s;

      String var = s.substring(first + 1, second);
      StringBuilder sb = new StringBuilder(s.substring(0, first));
//      sb.append("XX").append(var).append("XX");
      sb.append(System.getenv(var));

      sb.append(s.substring(second + 1));
      s = sb.toString();
    }
    return s;
  }

  public static byte[] readFile(File file) throws IOException
  {
    int fileSize = (int) file.length();
    InputStream is = new FileInputStream(file);

    try {
      byte[] inputBytes = new byte[fileSize];

      int offset = 0;
      int numRead;

      while (offset < fileSize && ((numRead = is.read(inputBytes, offset, fileSize - offset)) != -1)) {
        offset += numRead;
        log.log(Level.FINER, "got " + numRead + " bytes from " + file.getName());
      }

      if (offset < fileSize) {
        throw new IOException("Short read of " + file + ", expected " + fileSize + " but got " + offset);
      }

      return inputBytes;
    }
    finally {
      is.close();
    }
  }

  public static void writeFile(File file, byte[] bytes) throws IOException
  {
    OutputStream os = new FileOutputStream(file);
    try {
      os.write(bytes);
    }
    finally {
      os.close();
    }
  }

  /**
   * Copies the file. Sets the last modified time of the new file to the same as the old file.
   */
  public static void copyFile(File from, File to) throws IOException
  {
    FileChannel in = (new FileInputStream(from)).getChannel();
    try {
      FileChannel out = (new FileOutputStream(to)).getChannel();
      try {
        in.transferTo(0, in.size(), out);
      }
      finally {
        out.close();
      }
    }
    finally {
      in.close();
    }
    to.setLastModified(from.lastModified());
  }

  // Removes a directory by removing all files in it first. TODO doesn't yet recurse(not sure if I want this)
  public static void rmdirAll(File dir) throws IOException
  {
    for (File file : dir.listFiles()) {
      if (!file.delete()) {
        throw new IOException("Can't delete " + file);
      }
    }
    if (!dir.delete()) {
      throw new IOException("Can't delete " + dir);
    }
  }

  private static final int BUFSIZ = 4096;

  public static void copyStreams(InputStream is, OutputStream os) throws IOException
  {
    byte[] buf = new byte[BUFSIZ];

    int numRead;
    while ((numRead = is.read(buf, 0, BUFSIZ)) != -1) {
      os.write(buf, 0, numRead);
    }
  }

  public static String readStreamIntoString(InputStream is) throws IOException
  {
    StringBuilder sb = new StringBuilder();

    char[] buf = new char[BUFSIZ];
    BufferedReader r = new BufferedReader(new InputStreamReader(is));

    int numRead;
    while ((numRead = r.read(buf, 0, BUFSIZ)) != -1) {
      sb.append(buf, 0, numRead);
    }

    return sb.toString();
  }
}
