package com.goofans.gootool.addins;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;

/**
 * AddinFileReader should be used for all access to goomod files, since it transparently handles the switch to an extracted
 * directory where necessary. It's a thin facade around the various IO operations.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface AddinReader
{
  /**
   * Gets an InputStream for the requested file.
   *
   * @param fileName the file name to read, path separated by "/".
   * @return an InputStream for this file.
   * @throws java.io.FileNotFoundException if the requested file does not exist.
   * @throws java.io.IOException           if the file could not be read for another reason.
   */
  public InputStream getInputStream(String fileName) throws FileNotFoundException, IOException;

  /**
   * Gives an iterator for all the files that are descendants of the given directory name (including files in subdirectories).
   * Skips all files and directories on the skip list.
   *
   * @param directory The directory to search under, including trailing "/".
   * @param skip A list of file/directory names to skip
   * @return An iterator of valid files.
   */
  public Iterator<String> getEntriesInDirectory(String directory, List<String> skip);

  /**
   * Closes the addin once work is complete.
   *
   * @throws java.io.IOException if the addin could not be closed successfully.
   */
  public void close() throws IOException;
}
