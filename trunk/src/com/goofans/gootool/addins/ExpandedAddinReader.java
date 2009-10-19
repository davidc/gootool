package com.goofans.gootool.addins;

import java.io.*;

/**
 * Reads an addin expanded on disk.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ExpandedAddinReader implements AddinReader
{
  File rootDirectory;

  public ExpandedAddinReader(File rootDirectory)
  {
    this.rootDirectory = rootDirectory;
  }

  public InputStream getInputStream(String fileName) throws FileNotFoundException, IOException
  {
    File file = new File(rootDirectory, fileName);

    // Not necessary, FileInputStream's constructor also throws FNF
//    if (!file.exists()) {
//      throw new FileNotFoundException("File "+ fileName + " not found in expanded addin");
//    }

    return new FileInputStream(file);
  }

  public void close() throws IOException
  {
  }
}
