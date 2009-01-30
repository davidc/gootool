package com.goofans.gootool.siteapi;

import net.infotrek.util.TextUtil;

import java.io.IOException;
import java.io.File;
import java.util.logging.Logger;

import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.io.GameFormat;
import org.w3c.dom.Document;

/**
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

  public void restoreProfile(int backupId) throws APIException
  {
    addPostParameter("backup_id", String.valueOf(backupId));

    Document doc = doRequest();
    if (!doc.getDocumentElement().getTagName().equalsIgnoreCase("profile-restore-success")) {
      throw new APIException("Profile restore failed");
    }

    byte[] profileContent;
    try {
      profileContent = TextUtil.base64Decode(XMLUtil.getElementStringRequired(doc.getDocumentElement(), "profile"));
    }
    catch (IOException e) {
      throw new APIException("No profile content returned by server");
    }

    if (!ProfileFactory.isProfileFound()) {
      throw new APIException("You don't have a profile on this computer yet. Create one before restoring.");
    }
    
    try {
      GameFormat.encodeProfileFile(ProfileFactory.getProfileFile(), profileContent);
    }
    catch (IOException e) {
      throw new APIException("Unable to write the profile: " + e.getMessage(), e);
    }
  }

  public static void main(String[] args) throws APIException, IOException
  {
    DebugUtil.setAllLogging();
    ProfileFactory.init();

    new ProfileRestoreRequest().restoreProfile(1);
  }
}
