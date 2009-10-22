package com.goofans.gootool.util;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Miscellaneous utilities.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Utilities
{
  private static final Logger log = Logger.getLogger(Utilities.class.getName());

  /**
   * Expands the string with any %envvars% expanded.
   *
   * @param s the string to expand, with env vars enclosed within % signs
   * @return The expanded string, or null of any env var wasn't found
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
   * Moves the file. May not be atomic if renameTo isn't atomic or isn't supported (e.g. separate file systems).
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
   * @param file The file to delete.
   * @throws IOException if the deletion failed.
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
   * @param dir The directory (with parents) to create
   * @throws IOException if the directory (or any parents) could not be created
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
   * @param outputFile the File to save the download into
   * @throws IOException if the download failed or the file was not writeable.
   */
  public static void downloadFile(URL url, File outputFile) throws IOException
  {
    log.fine("Downloading " + url + " to " + outputFile);

    // Generate a temporary file
    File tempFile = File.createTempFile("goodownload-", null);

    // Download to temp file
    InputStream downloadStream = url.openStream();
    try {
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      copyStreams(downloadStream, outputStream);
      try {
        outputStream.close();
      }
      finally {
        outputStream.close();
      }
    }
    finally {
      downloadStream.close();
    }

    // Move temp file into place
    deleteFileIfExists(outputFile);
    moveFile(tempFile, outputFile);
  }
}
