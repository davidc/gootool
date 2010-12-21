/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.facades;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LocalSourceFile implements SourceFile
{
  private final LocalSource source;
  private final File backingFile;

  public LocalSourceFile(LocalSource source, File backingFile)
  {
    this.source = source;
    this.backingFile = backingFile;
  }

  public String getName()
  {
    return backingFile.getName();
  }

  public String getFullName()
  {
    String rootPrefix = ((LocalSourceFile) source.getRoot()).backingFile.getAbsolutePath();
    System.out.println("rootPrefix = " + rootPrefix);
    String myAbsolutePath = backingFile.getAbsolutePath();
    System.out.println("myAbsolutePath = " + myAbsolutePath);

    if (!myAbsolutePath.startsWith(rootPrefix)) throw new RuntimeException("Oddness abounds: local source file " + myAbsolutePath + " is not a child of root " + rootPrefix);

    System.out.println("myAbsolutePath.substring(rootPrefix.length()) = " + myAbsolutePath.substring(rootPrefix.length()));
    return myAbsolutePath.substring(rootPrefix.length());
  }

  public boolean isFile()
  {
    return backingFile.isFile();
  }

  public boolean isDirectory()
  {
    return backingFile.isDirectory();
  }

  public long lastModified()
  {
    return backingFile.lastModified();
  }

  public SourceFile getChild(String name)
  {
    File child = new File(backingFile, name);
    if (!child.exists()) return null;

    return new LocalSourceFile(source, child);
  }

  public SourceFile getParentDirectory()
  {
    // Are we already the root?
    if (((LocalSourceFile) source.getRoot()).backingFile.equals(this.backingFile)) return null;

    File parentFile = backingFile.getParentFile();
    if (parentFile == null) return null;

    return new LocalSourceFile(source, parentFile);
  }

  public List<SourceFile> list()
  {
    if (!backingFile.isDirectory()) return null;

    File[] backingList = backingFile.listFiles();
    if (backingList == null) return null;

    List<SourceFile> list = new ArrayList<SourceFile>(backingList.length);

    for (File file : backingList) {
      list.add(new LocalSourceFile(source, file));
    }

    return list;
  }

  public InputStream read() throws IOException
  {
    return new FileInputStream(backingFile);
  }

  @Override
  public String toString()
  {
    return "LocalSourceFile{" +
            "backingFile=" + backingFile +
            '}';
  }
}
