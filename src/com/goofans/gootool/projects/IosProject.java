/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.projects;

import net.infotrek.util.TextUtil;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import com.goofans.gootool.facades.Source;
import com.goofans.gootool.facades.Target;
import com.goofans.gootool.io.Codec;
import com.goofans.gootool.io.ImageCodec;
import com.goofans.gootool.util.Utilities;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class IosProject extends Project
{
  private static final Logger log = Logger.getLogger(IosProject.class.getName());

  private static final String PREF_KEY_HOST = "host";
  private static final String PREF_KEY_PASSWORD = "password";

  private ProjectConfiguration savedConfiguration; // todo will be IosProjectConfiguration

  IosProject(Preferences prefsNode)
  {
    super(prefsNode);
  }

  public String getHost()
  {
    return prefsNode.get(PREF_KEY_HOST, null);
  }

  public void setHost(String host)
  {
    prefsNode.put(PREF_KEY_HOST, host);
    Utilities.flushPrefs(prefsNode);
  }

  public String getPassword()
  {
    String enc = prefsNode.get(PREF_KEY_PASSWORD, null);
    if (enc == null) return null;

    try {
      return new String(TextUtil.base64Decode(enc));
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Base64 encoding exception in IOS root password, removing");
      return null;
    }
  }

  public void setPassword(String password)
  {
    prefsNode.put(PREF_KEY_PASSWORD, TextUtil.base64Encode(password.getBytes()));
    Utilities.flushPrefs(prefsNode);
  }

  @Override
  public byte[] getProfileBytes() throws IOException
  {
    return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void setProfileBytes(byte[] profileBytes) throws IOException
  {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public ProjectConfiguration getSavedConfiguration()
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void saveConfiguration(ProjectConfiguration c)
  {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean readyToBuild()
  {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Source getSource()
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Target getTarget()
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Codec getCodecForGameXml()
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Codec getCodecForProfile()
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public ImageCodec getImageCodec()
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getGameXmlFilename(String baseName)
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getGamePngFilename(String baseName)
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getGameSoundFilename(String baseName)
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getGameMusicFilename(String baseName)
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public String getGameAnimMovieFilename(String baseName)
  {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
