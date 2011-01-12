/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.facades;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class IosSourceFile implements SourceFile
{
  public static final char ZIP_DIR_SEP = '/';
  private final IosSource source;
  private final ZipFile zipFile;
  private final String fullName; // Contains trailing slash if this is a directory
  private final ZipEntry zipEntry;

  public IosSourceFile(IosSource source, ZipFile zipFile, String fullName) throws IOException
  {
    // TODO barf if a fullName contains two // in a row

    this.source = source;
    this.zipFile = zipFile;
    this.fullName = fullName;

    if (fullName.length() == 0) {
      // this is the root
      zipEntry = null;
    }
    else {
      zipEntry = zipFile.getEntry(fullName);
      if (zipEntry == null) throw new IOException("Requested entry " + fullName + " does not exist in zip file");
    }

    // Now zipEntry is only null if we are the root, so other methods can assume zipEntry==null ==> root
  }

  public String getName()
  {
    if (zipEntry == null) {
      return "";
    }

    int end = fullName.length();
    if (fullName.charAt(fullName.length() - 1) == ZIP_DIR_SEP) {
      end = fullName.length() - 1;
    }

    int lastSlash = fullName.lastIndexOf(ZIP_DIR_SEP, end - 1);
    int start = lastSlash == -1 ? 0 : lastSlash + 1;

    return fullName.substring(start, end);

  }

  public String getFullName()
  {
    return fullName; // TODO this is actual the same as zipEntry.getName()
  }

  public boolean isFile()
  {
    return zipEntry != null && !zipEntry.isDirectory();
  }

  public boolean isDirectory()
  {
    return zipEntry == null || zipEntry.isDirectory();
  }

  public long getSize()
  {
    return zipEntry == null ? 0 : zipEntry.getSize();
  }

  public long lastModified()
  {
    if (zipEntry == null) return 0;
    long time = zipEntry.getTime();
    if (time < 0) return 0; // -1 (zipfile api) changed to 0 (our api)
    return time;
  }

  public SourceFile getChild(String name)
  {
    if (!isDirectory()) return null;

    String childFileName;
    if (zipEntry == null) {
      childFileName = name;
    }
    else {
      childFileName = fullName + name;
    }
    String childDirName = childFileName + ZIP_DIR_SEP;

    try {
      // Look for it as a directory first, otherwise we get a fake file ZipEntry with the same name as the directory.
      ZipEntry childEntry = zipFile.getEntry(childDirName);
      if (childEntry != null) {
        return new IosSourceFile(source, zipFile, childDirName);
      }

      // Look for it as a file
      childEntry = zipFile.getEntry(childFileName);
      if (childEntry != null) {
        return new IosSourceFile(source, zipFile, childFileName);
      }

      return null;
    }
    catch (IOException e) {
      return null;
    }
  }

  public SourceFile getParentDirectory()
  {
    if (zipEntry == null) {
      return null; // we are the root
    }

    try {
      int index;
      if (fullName.charAt(fullName.length() - 1) == ZIP_DIR_SEP) {
        index = fullName.lastIndexOf(ZIP_DIR_SEP, fullName.length() - 2);
      }
      else {
        index = fullName.lastIndexOf(ZIP_DIR_SEP);
      }

      if (index == -1) {
        return new IosSourceFile(source, zipFile, "");
      }
      else {
        return new IosSourceFile(source, zipFile, fullName.substring(0, index) + ZIP_DIR_SEP);
      }
    }
    catch (IOException e) {
      throw new RuntimeException("No parent for " + fullName + " found in zip file");
    }
  }

  public List<SourceFile> list()
  {
    if (!isDirectory()) return null;

    List<SourceFile> list = new ArrayList<SourceFile>();

    String prefix = zipEntry == null ? "" : fullName;
    int prefixLength = prefix.length();

    Enumeration<? extends ZipEntry> childEntries = zipFile.entries();
    while (childEntries.hasMoreElements()) {
      ZipEntry childEntry = childEntries.nextElement();

      /**
       * This gets all entries in the zipfile. We narrow it down into five cases:
       * Case 1 - It doesn't begin with us as its prefix, ignore.
       * Case 2 - It begins with us, and is the same length as us, so it is us, ignore.
       * Case 3 - It begins with us, and has no further slash, so it's a child of ours - a file.
       * Case 4 - It begins with us, and has a single further slash at the end of the string, so it's a child of ours - a directory.
       * Case 5 - It begins with us, has a slash and then more characters, so it's a child of on of our children, ignore.
       */


      String childName = childEntry.getName();
      if (childName.startsWith(prefix) && childName.length() > prefixLength) { // Discard Cases 1 and 2

        // Search for first subsequent occurrence of a slash
        int slashPos = childName.indexOf(ZIP_DIR_SEP, prefixLength);

        if (slashPos == -1 || slashPos == childName.length() - 1) { // Cases 3 and 4
          try {
            list.add(new IosSourceFile(source, zipFile, childName));
          }
          catch (IOException e) {
            throw new RuntimeException("Unable to add child entry " + fullName + " to list");
          }
        }
      }
    }

    return list;
  }

  public InputStream read() throws IOException
  {
    if (zipEntry == null) throw new IOException("Can't read the root directory of a zip file");
    return zipFile.getInputStream(zipEntry);
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IosSourceFile that = (IosSourceFile) o;

    if (!fullName.equals(that.fullName)) return false;
    return source.equals(that.source);
  }

  @Override
  public int hashCode()
  {
    int result = source.hashCode();
    result = 31 * result + fullName.hashCode();
    return result;
  }

  @Override
  public String toString()
  {
    return "IosSourceFile{" +
            "source=" + source +
            ", zipFile=" + zipFile +
            ", fullName='" + fullName + '\'' +
            ", zipEntry=" + zipEntry +
            '}';
  }
}
