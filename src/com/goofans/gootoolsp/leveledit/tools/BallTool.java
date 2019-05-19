/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.tools;

import java.awt.*;
import java.awt.geom.Point2D;

import com.goofans.gootoolsp.leveledit.view.LevelDisplayLayer;
import com.goofans.gootoolsp.leveledit.model.LevelContentsItem;
import com.goofans.gootoolsp.leveledit.model.BallInstance;

/**
 * Tool to add new balls to level.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BallTool implements Tool
{
  private static final LevelDisplayLayer[] HIT_LAYERS = new LevelDisplayLayer[]{LevelDisplayLayer.BALLS};
  private static final Cursor CROSSHAIR_CURSOR = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
  private static final Cursor MOVE_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

  public LevelDisplayLayer[] getHitLayers()
  {
    return HIT_LAYERS;
  }

  // Gets the cursor at the given point, in world coordinates
  public Cursor getCursorAtPoint(Point displayCoords, Point2D.Double worldCoords, LevelContentsItem hoverItem)
  {
    if (hoverItem instanceof BallInstance) {
      return MOVE_CURSOR;
    }
    else {
      return CROSSHAIR_CURSOR;
    }
  }
}