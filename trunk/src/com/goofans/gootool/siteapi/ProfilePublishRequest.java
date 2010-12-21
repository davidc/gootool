/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import net.infotrek.util.EncodingUtil;
import net.infotrek.util.TextUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.profile.Profile;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A request to publish a given profile publically on goofans.com
 *
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

    Project project = ProjectManager.simpleInit();
    new ProfilePublishRequest().publishProfile(project.getProfileData().getCurrentProfile());
  }
}