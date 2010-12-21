/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.facades;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface Source
{
  // TODO we need a getResRoot() getPropertiesRoot() etc?

  SourceFile getRoot();
}
