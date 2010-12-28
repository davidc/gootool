/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.addins;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.*;

/**
 * Reads an addin in the standard .goomod (zip) format.
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

  public InputStream getInputStream(String fileName) throws IOException
  {
    ZipEntry zipEntry = zipFile.getEntry(fileName);
    if (zipEntry == null) {
      throw new FileNotFoundException("File " + fileName + " not found in addin");
    }

    return zipFile.getInputStream(zipEntry);
  }

  public boolean fileExists(String fileName)
  {
    ZipEntry zipEntry = zipFile.getEntry(fileName);
    return zipEntry != null;
  }

  public Iterator<String> getEntriesInDirectory(final String directory, final List<String> skip)
  {
    final Enumeration<? extends ZipEntry> zipEnumeration = zipFile.entries();

    return new Iterator<String>()
    {
      ZipEntry nextEntry = null;

      public boolean hasNext()
      {
        while (nextEntry != null || zipEnumeration.hasMoreElements()) {
          peekNext();
          if (isNextValid()) {
            return true;
          }
          nextEntry = null; // discard it, it's not valid
        }
        return false;
      }

      public String next()
      {
        // by calling hasNext, we ensure nextEntry is set, and that is a valid entry.
        if (!hasNext()) {
          throw new NoSuchElementException();
        }

        String retval = nextEntry.getName().substring(directory.length());
        nextEntry = null;
        return retval;
      }

      // This function reads the next entry from the source enumeration if we haven't already read it.
      // Caller ensures the source enumeration is not empty.
      private void peekNext()
      {
        if (nextEntry == null)
          nextEntry = zipEnumeration.nextElement();
      }

      // This function returns whether we want to return the nextEntry from this enumeration.
      // True if is a file (not a directory), and no path component is on the skip list.
      // Caller ensures nextEntry is already set.
      private boolean isNextValid()
      {
        if (nextEntry.isDirectory()) return false;

        StringTokenizer tok = new StringTokenizer(nextEntry.getName(), "/");
        while (tok.hasMoreTokens()) {
          if (skip.contains(tok.nextToken())) {
            return false;
          }
        }

        return nextEntry.getName().startsWith(directory);
      }

      public void remove()
      {
        throw new UnsupportedOperationException();
      }
    };
  }

  public void close() throws IOException
  {
    zipFile.close();
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "HardcodedFileSeparator", "DuplicateStringLiteralInspection"})
  public static void main(String[] args) throws IOException
  {
    AddinReader addinReader = new GoomodFileReader(new File("addins/dist/com.goofans.davidc.jingleballs_1.3.goomod"));
    Iterator<String> entries = addinReader.getEntriesInDirectory("override/", Arrays.asList("XmasProduct", "tree.png"));
    while (entries.hasNext()) {
      String s = entries.next();
      System.out.println("s = " + s);
    }
  }
}
