/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.ToolPreferences;

/**
 * Task to download the user's latest ratings from the site and update the registry cache.
 * Scheduled shortly after startup and occasionally thereafter by the StartupTask.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class RatingUpdateTask implements Runnable
{
  private static final Logger log = Logger.getLogger(RatingUpdateTask.class.getName());

  public void run()
  {
    if (ToolPreferences.isGooFansLoginOk()) {
      log.log(Level.FINE, "User is logged in, getting their ratings"); //NON-NLS
      try {
        RatingListRequest ratingListRequest = new RatingListRequest();
        Map<String, Integer> ratings = ratingListRequest.getRatings();
        ToolPreferences.setRatings(ratings);
      }
      catch (APIException e) {
        log.log(Level.SEVERE, "Unable to get user ratings", e); //NON-NLS
      }
    }
    else {
      log.log(Level.FINE, "User not logged in, not getting ratings"); //NON-NLS
    }
  }

  @Override
  public String toString()
  {
    return "RatingUpdateTask";
  }
}
