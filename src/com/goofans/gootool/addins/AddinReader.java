package com.goofans.gootool.addins;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;

/**
 * AddinFileReader should be used for all access to goomod files, since it transparently handles the switch to an extracted
 * directory where necessary. It's a thin facade around the various IO operations.
 * <p/>
 * TODO: Make AddinInstaller use this.
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
   * Closes the addin once work is complete.
   *
   * @throws java.io.IOException if the addin could not be closed successfully.
   */
  public void close() throws IOException;
}
