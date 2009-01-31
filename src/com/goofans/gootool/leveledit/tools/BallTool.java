package com.goofans.gootool.leveledit.tools;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Tool to add new balls to level.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BallTool implements Tool
{
  // Gets the cursor at the given point, in world coordinates
  public Cursor getCursorAtPoint(Point2D.Double p)
  {
    return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
  }
}