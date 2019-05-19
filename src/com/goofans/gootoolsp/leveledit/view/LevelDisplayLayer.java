/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.view;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public enum LevelDisplayLayer
{
  IMAGES("Images", true),
  BALLS("Balls", true),
  STRANDS("Strands", true),
  GEOMETRY("Geometry", true),
  BOUNDARIES("Boundaries", true),
  HINGES("Hinges", true),
  VIEWPORT("Viewport", true);

  private final String displayName;
  private final boolean defaultVisible;

  LevelDisplayLayer(String displayName, boolean defaultVisible)
  {
    this.displayName = displayName;
    this.defaultVisible = defaultVisible;
  }

  public boolean isDefaultVisible()
  {
    return defaultVisible;
  }

  @Override
  public String toString()
  {
    return displayName;
  }
}
