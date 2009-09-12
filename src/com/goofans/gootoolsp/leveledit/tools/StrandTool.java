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
public class StrandTool implements Tool
{
  private static final LevelDisplayLayer[] HIT_LAYERS = new LevelDisplayLayer[]{LevelDisplayLayer.BALLS, LevelDisplayLayer.STRANDS};

  private static final Cursor CROSSHAIR_CURSOR = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
  private static final Cursor INVALID_CURSOR;

  static {
    Cursor INVALID_CURSOR1;
    try {
      INVALID_CURSOR1 = Cursor.getSystemCustomCursor("Invalid.32x32");
    }
    catch (AWTException e) {
      INVALID_CURSOR1 = Cursor.getDefaultCursor();
    }
    INVALID_CURSOR = INVALID_CURSOR1;
  }

  public LevelDisplayLayer[] getHitLayers()
  {
    return HIT_LAYERS;
  }

  // Gets the cursor at the given point, in world coordinates
  public Cursor getCursorAtPoint(Point displayCoords, Point2D.Double worldCoords, LevelContentsItem hoverItem)
  {
    if (hoverItem instanceof BallInstance) {
      return CROSSHAIR_CURSOR;
    }
    else {
      return INVALID_CURSOR;
    }
  }
}