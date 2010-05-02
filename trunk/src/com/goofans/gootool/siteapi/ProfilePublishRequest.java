/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import net.infotrek.util.TextUtil;
import net.infotrek.util.EncodingUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;

import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.profile.Profile;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProfilePublishRequest extends APIRequestAuthenticated
{
  private static final Logger log = Logger.getLogger(ProfilePublishRequest.class.getName());

  private List<String> messages;

  public ProfilePublishRequest() throws APIException
  {
    super(API_PROFILE_PUBLISH);
  }

  public String publishProfile(Profile profile) throws APIException
  {
    log.log(Level.FINE, "Profile publish started");

    if (!ProfileFactory.isProfileFound()) {
      throw new APIException("Profile hasn't been located yet");
    }

    addPostParameter("profile", TextUtil.base64Encode(EncodingUtil.stringToBytesUtf8(profile.getData())));

    Document doc = doRequest();
    if (!"profile-publish-success".equalsIgnoreCase(doc.getDocumentElement().getTagName())) {
      throw new APIException("Profile backup failed");
    }

    String profileUrl = XMLUtil.getElementString(doc.getDocumentElement(), "url");

    final Element messagesEl = XMLUtil.getElement(doc.getDocumentElement(), "messages");
    messages = new ArrayList<String>();
    if (messagesEl != null) {
      final NodeList theMessages = messagesEl.getElementsByTagName("message");
      for (int i = 0; i < theMessages.getLength(); i++) {
        Element el = (Element) theMessages.item(i);
        messages.add(el.getTextContent());
      }
    }

    return profileUrl;
  }

  public List<String> getMessages()
  {
    if (messages == null) throw new IllegalStateException("Request hasn't yet been performed");
    return messages;
  }

  public static void main(String[] args) throws APIException, IOException
  {
    DebugUtil.setAllLogging();
    ProfileFactory.init();

    new ProfilePublishRequest().publishProfile(ProfileFactory.getProfileData().getCurrentProfile());

  }
}