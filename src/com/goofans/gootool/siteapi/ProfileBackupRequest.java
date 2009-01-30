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

    String profile;
    try {
      profile = GameFormat.decodeProfileFile(ProfileFactory.getProfileFile());
    }
    catch (IOException e) {
      throw new APIException("Profile decoding failed", e);
    }

    addPostParameter("profile", TextUtil.base64Encode(TextUtil.stringToBytesUtf8(profile)));
    addPostParameter("description", description);

    Document doc = doRequest();
    if (!doc.getDocumentElement().getTagName().equalsIgnoreCase("profile-backup-success")) {
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
