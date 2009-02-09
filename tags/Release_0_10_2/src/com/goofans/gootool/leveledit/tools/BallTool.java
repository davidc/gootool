package com.goofans.gootool.leveledit.tools;

import java.awt.*;
import java.awt.geom.Point2D;

import com.goofans.gootool.leveledit.view.LevelDisplayLayer;
import com.goofans.gootool.leveledit.model.LevelContentsItem;
import com.goofans.gootool.leveledit.model.BallInstance;

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