/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import net.infotrek.util.TextUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.util.DebugUtil;
import org.w3c.dom.Document;

/**
 * A request to backup the profile file to the server.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProfileBackupRequest extends APIRequestAuthenticated
{
  private static final Logger log = Logger.getLogger(ProfileBackupRequest.class.getName());

  public ProfileBackupRequest() throws APIException
  {
    super(API_PROFILE_BACKUP);
  }

  public void backupProfile(String description) throws APIException
  {
    log.log(Level.FINE, "Profile upload " + description);

    if (!ProfileFactory.isProfileFound()) {
      throw new APIException("Profile hasn't been located yet");
    }

    byte[] profile;
    try {
      profile = GameFormat.decodeProfileFile(ProfileFactory.getProfileFile());
    }
    catch (IOException e) {
      throw new APIException("Profile decoding failed", e);
    }

    addPostParameter("profile", TextUtil.base64Encode(profile));
    addPostParameter("description", description);

    Document doc = doRequest();
    if (!"profile-backup-success".equalsIgnoreCase(doc.getDocumentElement().getTagName())) {
      throw new APIException("Profile backup failed");
    }

//    System.out.println("Utilities.readStreamIntoString(doRequestInt()) = " + Utilities.readStreamIntoString(doRequestInt()));
  }

  public static void main(String[] args) throws APIException, IOException
  {
    DebugUtil.setAllLogging();
    ProfileFactory.init();

    new ProfileBackupRequest().backupProfile("test 213");

  }
}
