package com.goofans.gootool.leveledit.tools;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Tool to move the currently selected item(s).
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MoveTool implements Tool
{
  // Gets the cursor at the given point, in world coordinates
  public Cursor getCursorAtPoint(Point2D.Double p)
  {
    return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
  }
}