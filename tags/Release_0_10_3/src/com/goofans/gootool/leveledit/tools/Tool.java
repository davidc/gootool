package com.goofans.gootool.leveledit.tools;

import java.awt.*;
import java.awt.geom.Point2D;

import com.goofans.gootool.leveledit.view.LevelDisplayLayer;
import com.goofans.gootool.leveledit.model.LevelContentsItem;

/**
 * Interface implemented by all Tools.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface Tool
{
  /* Returns the layers that this tool will hit elements in. Null for all layers. GetCursor will be sent the top-most item selected */
  public LevelDisplayLayer[] getHitLayers();

  // Gets the cursor at the given point, in display and world coordinates
  public Cursor getCursorAtPoint(Point displayCoords, Point2D.Double worldCoords, LevelContentsItem hoverItem);
}
