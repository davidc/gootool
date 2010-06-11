/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.util;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Miscellaneous utilities, mostly IO-related.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Utilities
{
  private static final Logger log = Logger.getLogger(Utilities.class.getName());

  private Utilities()
  {
  }

  /**
   * Expands the string with any environment variables (of the form %envvars%) substituted with their values.
   * <p/>
   * If a variable expands into something with another %envvar% inside it, this is expanded recursively.
   *
   * @param s the string to expand, with env vars enclosed within % signs.
   * @return The expanded string, or null of any of the env vars wasn't found.
   */
  public static String expandEnvVars(String s)
  {
    int first, second;

    while ((first = s.indexOf('%')) != -1) {
      second = s.indexOf('%', first + 1);
      if (second == -1) return s;

      String var = s.substring(first + 1, second);
      StringBuilder sb = new StringBuilder(s.substring(0, first));
      String envVar = System.getenv(var);
      if (envVar == null) return null;
      sb.append(envVar);

      sb.append(s.substring(second + 1));
      s = sb.toString();
    }
    return s;
  }

  /**
   * Reads the file into a byte array.
   *
   * @param file The file to read from.
   * @return Byte array of the file's contents.
   * @throws IOException if the file cannot be opened, or cannot be fully read.
   */
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

  /**
   * Write the byte array to the given file.
   *
   * @param file  The file to write to.
   * @param bytes The byte array to write.
   * @throws IOException if the file cannot be opened, or cannot be fully written to.
   */
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
   * Copies the file. Sets the last modified time of the new file to the same as the old file if possible, fails silently if not.
   *
   * @param from Source file.
   * @param to   Destination file.
   * @throws IOException if the copy failed.
   */
  public static void copyFile(File from, File to) throws IOException
  {
    FileChannel in = (new FileInputStream(from)).getChannel();
    try {
      FileChannel out = (new FileOutputStream(to)).getChannel();
      try {
        long count = in.size();
        if (in.transferTo(0, count, out) != count) {
          throw new IOException("Couldn't copy the whole file");
        }
      }
      finally {
        out.close();
      }
    }
    finally {
      in.close();
    }
    //noinspection ResultOfMethodCallIgnored
    to.setLastModified(from.lastModified());
  }

  /**
   * Moves the file. May not be atomic if renameTo isn't atomic on the OS or isn't supported (e.g. separate file systems).
   *
   * @param from Source file.
   * @param to   Destination file.
   * @throws IOException if the move failed.
   */
  public static void moveFile(File from, File to) throws IOException
  {
    if (to.exists()) {
      throw new IOException("Destination file exists: " + to);
    }

    // Try to directly rename it
    if (!from.renameTo(to)) {
      // Couldn't rename, perhaps on another filesystem. Copy and delete instead.
      copyFile(from, to);
      if (!from.delete()) {
        throw new IOException("Unable to delete source file " + from);
      }
    }
  }

  /**
   * Delete the given file if it exists, otherwise do nothing. Throws an exception if deletion failed.
   *
   * @param file The file to delete.
   * @throws IOException if file exists and the deletion failed.
   */
  public static void deleteFileIfExists(File file) throws IOException
  {
    if (file.exists()) {
      if (!file.delete()) {
        throw new IOException("Unable to delete output file " + file);
      }
    }
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

  /**
   * Buffered copy from one stream to another.
   *
   * @param is The input stream.
   * @param os The output stream.
   * @throws IOException if the copy failed.
   */
  public static void copyStreams(InputStream is, OutputStream os) throws IOException
  {
    byte[] buf = new byte[BUFSIZ];

    int numRead;
    while ((numRead = is.read(buf, 0, BUFSIZ)) != -1) {
      os.write(buf, 0, numRead);
    }
  }

  /**
   * Reads the remainder of the stream into a String, assuming UTF-8 encoding.
   *
   * @param is The stream to read from.
   * @return The String read from the stream.
   * @throws IOException if the stream couldn't be read.
   */
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

  /**
   * Reads the remainder of the reader into a String.
   *
   * @param r The reader to read from.
   * @return The String read from the reader.
   * @throws IOException if the reader couldn't be read.
   */
  public static String readReaderIntoString(Reader r) throws IOException
  {
    StringBuilder sb = new StringBuilder();

    char[] buf = new char[BUFSIZ];

    int numRead;
    while ((numRead = r.read(buf, 0, BUFSIZ)) != -1) {
      sb.append(buf, 0, numRead);
    }

    return sb.toString();
  }

  /**
   * Makes the given directory (and any necessary parents) and throws an exception if this failed.
   * Does not do anything if the directory already exists.
   *
   * @param dir The directory (with parents) to create.
   * @throws IOException if the directory (or any parents) doesn't exist and could not be created.
   */
  public static void mkdirsOrException(File dir) throws IOException
  {
    if (!dir.isDirectory() && !dir.mkdirs()) {
      throw new IOException("Couldn't create directory " + dir);
    }
  }

  /**
   * Downloads a URL to a file. Waits for the full download before "atomically" moving it to the given output file, thus preventing
   * other readers reading a partially-downloaded file. (Note that it's not truly atomic, especially if renameTo fails due to the files
   * being on separate file systems).
   *
   * @param url        the URL to download.
   * @param outputFile the File to save the download into.
   * @throws IOException if the download failed or the file was not writable.
   */
  public static void downloadFile(URL url, File outputFile) throws IOException
  {
    File tempFile = downloadFileToTemp(url);

    // Move temp file into place
    deleteFileIfExists(outputFile);
    moveFile(tempFile, outputFile);
  }

  /**
   * Downloads a URL to a temporary file.
   *
   * @param url the URL to download.
   * @return the temporary File with the contents.
   * @throws IOException if the download failed.
   */
  public static File downloadFileToTemp(URL url) throws IOException
  {
    // Generate a temporary file
    File tempFile = File.createTempFile("goodownload-", null);

    log.fine("Downloading " + url + " to " + tempFile);

    // Download to temp file
    InputStream downloadStream = url.openStream();
    try {
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      try {
        copyStreams(downloadStream, outputStream);
      }
      finally {
        outputStream.close();
      }
    }
    finally {
      downloadStream.close();
    }
    return tempFile;
  }

  /**
   * Test that the given directory exists and can be written in.
   *
   * @param dir the directory to test.
   * @throws IOException if the test failed.
   */
  public static void testDirectoryWriteable(File dir) throws IOException
  {
    if (!dir.isDirectory()) {
      throw new IOException("Not a directory: " + dir);
    }

    //test write
    File testFile = new File(dir, "writeTest");
    FileOutputStream os = new FileOutputStream(testFile);
    try {
      os.write(65);
    }
    finally {
      os.close();
    }

    if (!testFile.delete()) {
      throw new IOException("Can't delete test file " + testFile);
    }
  }
}
