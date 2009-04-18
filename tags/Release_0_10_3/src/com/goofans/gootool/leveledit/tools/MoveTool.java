package com.goofans.gootool.leveledit.tools;

import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import com.goofans.gootool.leveledit.view.LevelDisplayLayer;
import com.goofans.gootool.leveledit.model.LevelContentsItem;

/**
 * Tool to move the currently selected item(s).
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MoveTool implements Tool
{
  private static final Cursor MOVE_CURSOR = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
  private static final Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

  public LevelDisplayLayer[] getHitLayers()
  {
    return null;
  }
  
  // Gets the cursor at the given point, in world coordinates
  public Cursor getCursorAtPoint(Point displayCoords, Point2D.Double worldCoords, LevelContentsItem hoverItem)
  {
    if (hoverItem != null) {
      return MOVE_CURSOR;
    }
    else {
      return DEFAULT_CURSOR;
    }
  }
}