/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import net.infotrek.util.TextUtil;

import java.io.IOException;
import java.util.logging.Logger;

import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;

/**
 * A request to restore the profile file from the given backup ID on the server.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProfileRestoreRequest extends APIRequestAuthenticated
{
  private static final Logger log = Logger.getLogger(ProfileRestoreRequest.class.getName());

  public ProfileRestoreRequest() throws APIException
  {
    super(API_PROFILE_RESTORE);
  }

  public void restoreProfile(Project project, int backupId) throws APIException
  {
    addPostParameter("backup_id", String.valueOf(backupId));

    Document doc = doRequest();
    if (!"profile-restore-success".equalsIgnoreCase(doc.getDocumentElement().getTagName())) { //NON-NLS
      throw new APIException("Profile restore failed");
    }

    byte[] profileContent;
    try {
      profileContent = TextUtil.base64Decode(XMLUtil.getElementStringRequired(doc.getDocumentElement(), "profile"));
    }
    catch (IOException e) {
      throw new APIException("No profile content returned by server");
    }

    try {
      if (project.getProfileData() == null) {
        throw new APIException("You don't have a profile on this computer yet. Create one before restoring.");
      }
    }
    catch (IOException e) {
      throw new APIException("You don't have a profile on this computer yet. Create one before restoring.", e);
    }

    try {
      project.setProfileBytes(profileContent);
    }
    catch (IOException e) {
      throw new APIException("Unable to write the profile: " + e.getMessage(), e);
    }
  }

  public static void main(String[] args) throws APIException, IOException
  {
    DebugUtil.setAllLogging();

    Project project = ProjectManager.simpleInit();
    new ProfileRestoreRequest().restoreProfile(project, 1);
  }
}
