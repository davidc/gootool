/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.facades;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface SourceFile extends ReadableFile
{
  /**
   * Gets the leaf name of this file in its parent directory.
   *
   * @return
   */
  String getName();

  /**
   * Gets the full name, including path, from the root, excluding any leading /.
   *
   * @return
   */
  String getFullName();

  boolean isFile();

  boolean isDirectory();

  long getSize();

  /**
   * Returns the last modified time, or 0 if the file doesn't have a last modified time somehow.
   *
   * @return Modified time in milliseconds since January 1, 1970, 00:00:00 GMT, or 0 if unavailable
   */
  long lastModified();

  /**
   * Gets the child of the given name, or null if it does not exist. (this differs from new File() which would return a file, but exists() would be true).
   * This method expects any path components to be separated by "/", regardless of platform separator.
   *
   * @param name
   * @return
   */
  SourceFile getChild(String name);

  /**
   * Gets the parent directory of this one, or null if we're the root of this target.
   *
   * @return
   */
  SourceFile getParentDirectory();

  /**
   * Gets the children of this node. If this is not a directory, returns null. If it is a directory but empty, returns an empty list.
   *
   * @return
   */
  List<SourceFile> list() throws IOException;

  InputStream read() throws IOException;
}
