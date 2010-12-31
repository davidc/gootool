/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import com.goofans.gootool.projects.Project;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface ProjectPropertiesDialog
{
  void setVisible(boolean b);

  boolean isOkButtonPressed();

  void saveToProject(Project project);
}
