package com.goofans.gootool.leveledit.tools;

import java.awt.*;
import java.awt.geom.Point2D;

import com.goofans.gootool.leveledit.view.LevelDisplayLayer;
import com.goofans.gootool.leveledit.model.LevelContentsItem;

/**
 * Tool to pan the current viewport.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class PanTool implements Tool
{
  private static final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

  public LevelDisplayLayer[] getHitLayers()
  {
    return null;
  }

  // Gets the cursor at the given point, in world coordinates
  public Cursor getCursorAtPoint(Point displayCoords, Point2D.Double worldCoords, LevelContentsItem hoverItem)
  {
    return HAND_CURSOR;
  }
}
