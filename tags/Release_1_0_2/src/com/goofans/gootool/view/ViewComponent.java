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
