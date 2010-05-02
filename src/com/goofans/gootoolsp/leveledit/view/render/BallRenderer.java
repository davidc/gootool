/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.view.render;

import java.awt.*;
import java.awt.geom.Ellipse2D;

import com.goofans.gootoolsp.leveledit.model.BallInstance;
import com.goofans.gootoolsp.leveledit.model.LevelContentsItem;
import com.goofans.gootoolsp.leveledit.view.LevelDisplay;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BallRenderer extends Renderer
{
  private static final int HIT_TOLERANCE = 2;

  public void render(Graphics2D g, LevelDisplay levelDisplay, LevelContentsItem item)
  {
    BallInstance ball = (BallInstance) item;
    Ellipse2D.Float shape = getShape(levelDisplay, ball);

    // TODO find the Ball so we can get the right Shape and size.

    g.setStroke(new BasicStroke(2));
    g.setColor(Color.RED);
    g.drawOval((int) shape.x, (int) shape.y, (int) shape.width, (int) shape.height);
  }

  public Shape getHitBox(LevelDisplay levelDisplay, LevelContentsItem item)
  {
    BallInstance ball = (BallInstance) item;

    Ellipse2D.Float shape = getShape(levelDisplay, ball);

    shape.x -= HIT_TOLERANCE;
    shape.y -= HIT_TOLERANCE;
    shape.width += HIT_TOLERANCE * 2;
    shape.height += HIT_TOLERANCE * 2;

    return shape;
    // todo not only circle
  }

  public Shape getNegativeHitBox(LevelDisplay levelDisplay, LevelContentsItem item)
  {
    BallInstance ball = (BallInstance) item;

    Ellipse2D.Float shape = getShape(levelDisplay, ball);

    shape.x += HIT_TOLERANCE;
    shape.y += HIT_TOLERANCE;
    shape.width -= HIT_TOLERANCE * 2;
    shape.height -= HIT_TOLERANCE * 2;

    return shape;
    // todo not only circle
  }

  private Ellipse2D.Float getShape(LevelDisplay levelDisplay, BallInstance ball)
  {
    Ellipse2D.Float shape = new Ellipse2D.Float();
    double radius = 40;
    shape.width = levelDisplay.worldToCanvasScaleX(radius) * HIT_TOLERANCE;
    shape.height = levelDisplay.worldToCanvasScaleY(radius) * HIT_TOLERANCE;

    shape.x = levelDisplay.worldToCanvasX(ball.x) - (shape.width / HIT_TOLERANCE);
    shape.y = levelDisplay.worldToCanvasY(ball.y) - (shape.height / HIT_TOLERANCE);

    return shape;
  }
}
