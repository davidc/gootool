/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.text.DateFormat;
import java.text.ParseException;

import com.goofans.gootool.profile.ProfileFactory;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

/**
 * A request to list the user's stored profile backups.
 * 
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProfileListRequest extends APIRequestAuthenticated
{
  private static final Logger log = Logger.getLogger(ProfileListRequest.class.getName());

  public ProfileListRequest() throws APIException
  {
    super(API_PROFILE_LIST);
  }

  public List<BackupInstance> listBackups() throws APIException
  {
    Document doc = doRequest();
    if (!"profile-list-success".equalsIgnoreCase(doc.getDocumentElement().getTagName())) {
      throw new APIException("Listing backups failed");
    }

    NodeList backupNodes = doc.getElementsByTagName("backup");
    List<BackupInstance> backups = new ArrayList<BackupInstance>(backupNodes.getLength());

    DateFormat displayDateFormat = DateFormat.getDateTimeInstance();

    for (int i = 0; i < backupNodes.getLength(); i++) {
      Element el = (Element) backupNodes.item(i);

      BackupInstance instance = new BackupInstance();

      String uploadDateStr;
      String description;
      try {
        instance.id = XMLUtil.getElementIntegerRequired(el, "backup-id");
        uploadDateStr = XMLUtil.getElementStringRequired(el, "upload-date");
        description = XMLUtil.getElementString(el, "description");
      }
      catch (IOException e) {
        throw new APIException("Invalid tag", e);
      }

      Date uploadDate;
      try {
        synchronized (API_DATEFORMAT) {
          uploadDate = API_DATEFORMAT.parse(uploadDateStr);
        }

      }
      catch (ParseException e) {
        throw new APIException("Error parsing backup date " + uploadDateStr);
      }

      StringBuilder sb = new StringBuilder();
      sb.append(displayDateFormat.format(uploadDate))
              .append(" - ")
              .append(description);

      instance.description = sb.toString();

      backups.add(instance);
    }

    return backups;
  }

  public static class BackupInstance
  {
    public int id;
    public String description;

    @Override
    public String toString()
    {
      return description;
    }
  }


  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  public static void main(String[] args) throws APIException, IOException
  {
    DebugUtil.setAllLogging();
    ProfileFactory.init();

    List<BackupInstance> backups = new ProfileListRequest().listBackups();
    for (BackupInstance backup : backups) {
      System.out.println("backup.id = " + backup.id);
      System.out.println("backup = " + backup.description);
    }
  }
}