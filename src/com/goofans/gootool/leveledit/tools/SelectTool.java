package com.goofans.gootool.leveledit.tools;

import java.awt.*;
import java.awt.geom.Point2D;

import com.goofans.gootool.leveledit.view.LevelDisplayLayer;
import com.goofans.gootool.leveledit.model.LevelContentsItem;

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