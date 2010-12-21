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
public class LocalTargetFile implements TargetFile
{
  private LocalTarget target;
  private File backingFile;

  public LocalTargetFile(LocalTarget target, File backingFile)
  {
    this.target = target;
    this.backingFile = backingFile;
  }

  public String getName()
  {
    return backingFile.getName();
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

  public TargetFile getChild(String name)
  {
    return new LocalTargetFile(target, new File(backingFile, name));
  }

  public InputStream read() throws IOException
  {
    return new FileInputStream(backingFile);
  }

  public OutputStream write() throws IOException
  {
    return new FileOutputStream(backingFile);
  }

  public void delete() throws IOException
  {
    if (!backingFile.delete())
      throw new IOException("File " + backingFile + " could not be deleted");
  }

  public void mkdir() throws IOException
  {
    if (!backingFile.isDirectory()) {
      if (!backingFile.mkdir()) {
        throw new IOException("Directory " + backingFile + " could not be created");
      }
    }
  }

  public void mkdirs() throws IOException
  {
    if (!backingFile.isDirectory()) {
      if (!backingFile.mkdirs()) {
        throw new IOException("Directory " + backingFile + " could not be created");
      }
    }
  }

  public void makeExecutable() throws IOException
  {
    Runtime.getRuntime().exec(new String[]{"chmod", "+x", backingFile.getAbsolutePath()}); //NON-NLS
  }

  public void renameTo(TargetFile newTargetFile) throws IOException
  {
    if (!(newTargetFile instanceof LocalTargetFile)) throw new RuntimeException("Renaming to a non-local target file!");

    File targetBackingFile = ((LocalTargetFile) newTargetFile).backingFile;

    if (!backingFile.renameTo(targetBackingFile))
      throw new IOException("Renaming " + backingFile + " to " + targetBackingFile + " failed");
  }

  public TargetFile getParentDirectory()
  {
    // Are we already the root?
    if (((LocalTargetFile) target.getRoot()).backingFile.equals(this.backingFile)) return null;

    File parentFile = backingFile.getParentFile();
    if (parentFile == null) return null;

    return new LocalTargetFile(target, parentFile);
  }

  public List<TargetFile> list()
  {
    if (!backingFile.isDirectory()) return null;

    File[] backingList = backingFile.listFiles();
    if (backingList == null) return null;

    List<TargetFile> list = new ArrayList<TargetFile>(backingList.length);

    for (File file : backingList) {
      list.add(new LocalTargetFile(target, file));
    }

    return list;
  }

  @Override
  public String toString()
  {
    return "LocalTargetFile{" +
            "backingFile=" + backingFile +
            '}';
  }
}
