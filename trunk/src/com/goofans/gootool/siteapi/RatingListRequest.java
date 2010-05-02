/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class RatingListRequest extends APIRequestAuthenticated
{
  public RatingListRequest() throws APIException
  {
    super(API_RATING_LIST);
  }

  public Map<String, Integer> getRatings() throws APIException
  {
    Document doc = doRequest();

    if (!"rating-list-success".equalsIgnoreCase(doc.getDocumentElement().getTagName())) {
      throw new APIException("Get ratings failed");
    }

    NodeList ratingNodes = doc.getElementsByTagName("rating");
    Map<String, Integer> ratings = new TreeMap<String, Integer>();

    for (int i = 0; i < ratingNodes.getLength(); i++) {
      Element el = (Element) ratingNodes.item(i);

      try {
        String addinId = XMLUtil.getAttributeStringRequired(el, "addin-id");
        int vote = XMLUtil.getAttributeIntegerRequired(el, "vote");
        ratings.put(addinId, vote);
      }
      catch (IOException e) {
        throw new APIException("Invalid tag", e);
      }
    }

    return ratings;
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws APIException, IOException
  {
    DebugUtil.setAllLogging();
    ProfileFactory.init();

    Map<String, Integer> ratings = new RatingListRequest().getRatings();
    for (Map.Entry<String, Integer> rating : ratings.entrySet()) {
      System.out.println(rating.getKey() + " = " + rating.getValue());
    }
  }
}