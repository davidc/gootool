/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.facades;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface TargetFile extends ReadableFile
{
  String getName();

  boolean isFile();
  boolean isDirectory();

  long lastModified();


  /**
   * Gets the child of the given name, even if it doesn't exist (since we might want to create it). Differs from SourceFile in this respect.
   * @param name
   * @return
   */
  TargetFile getChild(String name);


  OutputStream write() throws IOException;

  /**
   * Deletes this file. If this is a directory, deletes all children and then deletes this file.
   */
  void delete() throws IOException;

  /**
   * Creates a directory with this name. Throws an exception if a file exists or directory couldn't be created.
   * @throws IOException
   */
  void mkdir() throws IOException;

  /**
   * Creates this directory and all needed parents directory. Throws an exception if a file exists or directory couldn't be created.
   */
  void mkdirs() throws IOException;

  /**
   * Sets the +x bit to make this file executable. Does nothing if not appropriate for this platform.
   */
  void makeExecutable() throws IOException;

  void renameTo(TargetFile newTargetFile) throws IOException;

  /**
   * Gets the parent directory of this one, or null if we're the root of this target.
   * @return
   */
  TargetFile getParentDirectory();

  /**
   * Gets the children of this node. If this is not a directory, returns null. If it is a directory but empty, returns an empty list.
   *
   * @return
   */
  List<TargetFile> list();
}
