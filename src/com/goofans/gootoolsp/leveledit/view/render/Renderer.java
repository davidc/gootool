/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.view.render;

import java.awt.*;

import com.goofans.gootoolsp.leveledit.view.LevelDisplay;
import com.goofans.gootoolsp.leveledit.model.LevelContentsItem;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public abstract class Renderer
{
  public abstract void render(Graphics2D g, LevelDisplay levelDisplay, LevelContentsItem item);

  /* Gets the hit-box in display coordinates */
  public abstract Shape getHitBox(LevelDisplay levelDisplay, LevelContentsItem item);
  public abstract Shape getNegativeHitBox(LevelDisplay levelDisplay, LevelContentsItem item);
}