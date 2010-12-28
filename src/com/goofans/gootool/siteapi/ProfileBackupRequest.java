/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.util.DebugUtil;
import net.infotrek.util.TextUtil;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

  // TODO default description should include the project name
  public void backupProfile(Project project, String description) throws APIException
  {
    log.log(Level.FINE, "Profile upload " + description);

    byte[] profile ;
    try {
      profile = project.getProfileBytes();
    }
    catch (IOException e) {
      throw new APIException("Can't read profile", e);
    }

    if (profile == null) {
      throw new APIException("Profile hasn't been located yet");
    }

    addPostParameter("profile", TextUtil.base64Encode(profile));
    addPostParameter("description", description);

    Document doc = doRequest();
    if (!"profile-backup-success".equalsIgnoreCase(doc.getDocumentElement().getTagName())) { //NON-NLS
      throw new APIException("Profile backup failed");
    }

//    System.out.println("Utilities.readStreamIntoString(doRequestInt()) = " + Utilities.readStreamIntoString(doRequestInt()));
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public static void main(String[] args) throws APIException, IOException
  {
    DebugUtil.setAllLogging();

    Project project = ProjectManager.simpleInit();
    new ProfileBackupRequest().backupProfile(project, "test 213");

  }
}
