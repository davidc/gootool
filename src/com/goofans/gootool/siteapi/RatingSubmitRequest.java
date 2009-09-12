package com.goofans.gootool.siteapi;

import java.io.IOException;

import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.util.DebugUtil;
import org.w3c.dom.Document;

/**
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
    if (!doc.getDocumentElement().getTagName().equalsIgnoreCase("rating-submit-success")) {
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