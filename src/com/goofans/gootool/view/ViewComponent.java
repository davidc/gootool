/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import com.goofans.gootool.model.Configuration;

/**
 * A component of our View. Each component should pass the event down to its children.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface ViewComponent
{
  void updateViewFromModel(Configuration c);

  void updateModelFromView(Configuration c);
}
