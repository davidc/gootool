/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.tools;

import java.awt.*;
import java.awt.geom.Point2D;

import com.goofans.gootoolsp.leveledit.view.LevelDisplayLayer;
import com.goofans.gootoolsp.leveledit.model.LevelContentsItem;

/**
 * Tool to select an item.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class SelectTool implements Tool
{
  private static final Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

  public LevelDisplayLayer[] getHitLayers()
  {
    return null;
  }

  // Gets the cursor at the given point, in world coordinates
  public Cursor getCursorAtPoint(Point displayCoords, Point2D.Double worldCoords, LevelContentsItem hoverItem)
  {
    return DEFAULT_CURSOR;
  }
}