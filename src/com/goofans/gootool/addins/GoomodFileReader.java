package com.goofans.gootool.addins;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

/**
 * Reads an addin in the standard .goomod (zip) format).
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GoomodFileReader implements AddinReader
{
  private final ZipFile zipFile;

  public GoomodFileReader(File goomodFile) throws IOException
  {
    zipFile = new ZipFile(goomodFile);

  }

  public InputStream getInputStream(String fileName) throws FileNotFoundException, IOException
  {
    ZipEntry zipEntry = zipFile.getEntry(fileName);
    if (zipEntry == null) {
      throw new FileNotFoundException("File " + fileName + " not found in addin");
    }

    return zipFile.getInputStream(zipEntry);
  }

  public void close() throws IOException
  {
    zipFile.close();
  }
}
