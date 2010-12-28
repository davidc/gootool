/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.addins;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;

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

  public InputStream getInputStream(String fileName) throws IOException
  {
    File file = new File(rootDirectory, fileName);

    // Not necessary, FileInputStream's constructor also throws FNF
//    if (!file.exists()) {
//      throw new FileNotFoundException("File "+ fileName + " not found in expanded addin");
//    }

    return new FileInputStream(file);
  }

  public boolean fileExists(String fileName)
  {
    File file = new File(rootDirectory, fileName);
    return file.exists();
  }

  public Iterator<String> getEntriesInDirectory(String directory, List<String> skip)
  {
    File dir = new File(rootDirectory, directory);
    List<String> entries = new ArrayList<String>();

    if (dir.isDirectory()) {
      getEntries(dir, null, entries, skip);
    }

    return entries.iterator();
  }

  private void getEntries(File dir, String prefix, List<String> entries, List<String> skip)
  {
    File[] dirFiles = dir.listFiles();
    for (File file : dirFiles) {
      if (!skip.contains(file.getName())) {
        String fileName = (prefix != null ? prefix + "/" : "") + file.getName();

        if (file.isDirectory()) {
          getEntries(file, fileName, entries, skip);
        }
        else if (file.isFile()) {
          entries.add(fileName);
        }
      }
    }
  }

  public void close() throws IOException
  {
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "HardcodedFileSeparator", "DuplicateStringLiteralInspection"})
  public static void main(String[] args)
  {
    AddinReader addinReader = new ExpandedAddinReader(new File("addins/src/com.goofans.davidc.jingleballs"));
    Iterator<String> entries = addinReader.getEntriesInDirectory("override/", Arrays.asList(".svn"));
    while (entries.hasNext()) {
      String s = entries.next();
      System.out.println("s = " + s);
    }
  }
}
