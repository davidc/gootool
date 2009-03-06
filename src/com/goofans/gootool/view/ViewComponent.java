package com.goofans.gootool.view;

import com.goofans.gootool.model.Configuration;

/**
 * A component of our View.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface ViewComponent
{
  void updateViewFromModel(Configuration c);

  void updateModelFromView(Configuration c);
}
