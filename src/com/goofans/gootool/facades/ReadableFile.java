/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.facades;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface ReadableFile
{
  /**
   * Gets the children of this node. If this is not a directory, returns null. If it is a directory but empty, returns an empty list.
   * @return
   */
//  List<TargetFile> list();

  InputStream read() throws IOException;
}
