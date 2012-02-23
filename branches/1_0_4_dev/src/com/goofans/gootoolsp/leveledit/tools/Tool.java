/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.tools;

import java.awt.*;
import java.awt.geom.Point2D;

import com.goofans.gootoolsp.leveledit.view.LevelDisplayLayer;
import com.goofans.gootoolsp.leveledit.model.LevelContentsItem;

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
