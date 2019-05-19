/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import java.io.IOException;

import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.util.DebugUtil;
import org.w3c.dom.Document;

/**
 * A request to submit a new addin rating for this user.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class RatingSubmitRequest extends APIRequestAuthenticated
{
  public RatingSubmitRequest() throws APIException
  {
    super(API_RATING_SUBMIT);
  }

  /**
   * Submits a rating to goofans.com.
   *
   * @param addinId the ID of the addin to rate
   * @param vote    the rating (0-100)
   * @throws APIException on API error
   */
  public void submitRating(String addinId, int vote) throws APIException
  {
    addPostParameter("addin_id", addinId);
    addPostParameter("vote", Integer.toString(vote));

    Document doc = doRequest();
    if (!"rating-submit-success".equalsIgnoreCase(doc.getDocumentElement().getTagName())) {
      throw new APIException("Rating submission failed");
    }
  }

  public static void main(String[] args) throws APIException, IOException
  {
    DebugUtil.setAllLogging();
    ProfileFactory.init();

    new RatingSubmitRequest().submitRating("com.goofans.davidc.jingleballs", 20);
  }
}