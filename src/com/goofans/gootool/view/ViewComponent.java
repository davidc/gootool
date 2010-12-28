/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import com.goofans.gootool.MainController;
import com.goofans.gootool.ProjectController;
import com.goofans.gootool.model.ProjectModel;
import com.goofans.gootool.projects.LocalProjectConfiguration;

/**
 * A component of our View. Each component should pass the event down to its children.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface ViewComponent
{
  // Called once and once only to tell it who the controller is
  void initController(ProjectController projectController);

  void updateViewFromModel(ProjectModel model);

  void updateModelFromView(ProjectModel model);
}
