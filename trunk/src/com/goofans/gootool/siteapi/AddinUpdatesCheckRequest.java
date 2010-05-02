/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.VersionSpec;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.wog.WorldOfGoo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AddinUpdatesCheckRequest extends APIRequest
{
  private static final Logger log = Logger.getLogger(AddinUpdatesCheckRequest.class.getName());

  public AddinUpdatesCheckRequest() throws APIException
  {
    super(API_ADDIN_UPDATES_CHECK);
  }

  public Map<String, AvailableUpdate> checkUpdates() throws APIException
  {

    final List<Addin> availableAddins = WorldOfGoo.getAvailableAddins();
    return checkUpdates(availableAddins);
  }

  public Map<String, AvailableUpdate> checkUpdates(List<Addin> addins) throws APIException
  {
    List<String> checkAddins = new ArrayList<String>(addins.size());

    for (Addin addin : addins) {
      checkAddins.add(addin.getId());
    }

    return checkUpdatesById(checkAddins);
  }

  public Map<String, AvailableUpdate> checkUpdatesById(List<String> addinIds) throws APIException
  {
    log.log(Level.FINE, "Addin update check started");

    if (addinIds.isEmpty()) {
      throw new APIException("No addins to check!");
    }

    StringBuilder sb = new StringBuilder();
    for (String addinId : addinIds) {
      if (sb.length() > 0) sb.append(",");
      sb.append(addinId);
    }
    addPostParameter("addins", sb.toString());

    Document doc = doRequest();

    if (!"updates-check-results".equalsIgnoreCase(doc.getDocumentElement().getTagName())) {
      throw new APIException("Update check failed");
    }

    Map<String, AvailableUpdate> updates = new TreeMap<String, AvailableUpdate>();
    for (String addinId : addinIds) {
      updates.put(addinId, null);
    }

    final NodeList theUpdates = doc.getDocumentElement().getElementsByTagName("addin");

    for (int i = 0; i < theUpdates.getLength(); i++) {
      Element el = (Element) theUpdates.item(i);

      if (el.getElementsByTagName("not-found").getLength() == 0) {
        AvailableUpdate update;
        try {
          update = new AvailableUpdate();
          update.id = el.getAttribute("id");
          update.version = new VersionSpec(XMLUtil.getElementStringRequired(el, "version"));
          synchronized (API_DATEFORMAT) {
            update.releaseDate = API_DATEFORMAT.parse(XMLUtil.getElementStringRequired(el, "release-date"));
          }
          update.downloadUrl = XMLUtil.getElementStringRequired(el, "download-url");
        }
        catch (ParseException e) {
          // unparseable date
          log.log(Level.WARNING, "Unable to parse date from server", e);
          continue;
        }
        catch (IOException e) {
          // missing parameter from server
          log.log(Level.WARNING, "Missing parameter", e);
          continue;
        }
        catch (NumberFormatException e) {
          // invalid version format on server side, ignore this addin
          continue;
        }

        if (updates.containsKey(update.id)) {
          updates.put(update.id, update);
        }
      }
      else {
//        System.out.println("not found = " + el.getAttribute("id"));
      }
    }

    return updates;
  }


  public static class AvailableUpdate
  {
    public String id;
    public VersionSpec version;
    public Date releaseDate;
    public String downloadUrl;

    @Override
    public String toString()
    {
      return "AvailableUpdate{" +
              "id='" + id + '\'' +
              ", version=" + version +
              ", releaseDate=" + releaseDate +
              ", downloadUrl='" + downloadUrl + '\'' +
              '}';
    }
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
  public static void main(String[] args) throws APIException, IOException
  {
    DebugUtil.setAllLogging();
    final WorldOfGoo wog = WorldOfGoo.getTheInstance();
    wog.init();

//    new AddinUpdatesCheckRequest().checkUpdatesById(Arrays.asList(new String[]{"goas", "com.goofans.billboards"}));
    final Map<String, AvailableUpdate> updates = new AddinUpdatesCheckRequest().checkUpdates();

    for (Addin addin : WorldOfGoo.getAvailableAddins()) {
      AvailableUpdate update = updates.get(addin.getId());
      if (update != null) {
        System.out.println("addin.getId() = " + addin.getId());
        System.out.println("addin.getVersion() = " + addin.getVersion());
        System.out.println("update.version = " + update.version);
        if (update.version.compareTo(addin.getVersion()) > 0) {
          System.out.println("Update available!!!");
        }
      }
    }

    System.out.println();
    System.out.println("-- Update map --");
    for (Map.Entry<String, AvailableUpdate> update : updates.entrySet()) {
      System.out.println("update.getKey() = " + update.getKey());
      System.out.println("update.getValue() = " + update.getValue());
    }
  }

}