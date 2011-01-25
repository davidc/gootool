/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.projects;

import net.infotrek.util.TextUtil;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.GooToolResourceBundle;
import com.goofans.gootool.MainController;
import com.goofans.gootool.facades.IosSource;
import com.goofans.gootool.facades.Source;
import com.goofans.gootool.facades.Target;
import com.goofans.gootool.io.Codec;
import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.io.ImageCodec;
import com.goofans.gootool.ios.IosConnection;
import com.goofans.gootool.ios.IosConnectionFactory;
import com.goofans.gootool.ios.IosConnectionParameters;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.ProgressListener;
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

  private IosProjectConfiguration savedConfiguration;

  public /* TODO NOT PUBLIC */ IosProject(Preferences prefsNode, File storageDir)
  {
    super(prefsNode, storageDir);
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
      log.log(Level.SEVERE, "Base64 encoding exception in iOS root password, removing");
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
    return null;
  }

  @Override
  public void setProfileBytes(byte[] profileBytes) throws IOException
  {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public ProjectConfiguration getSavedConfiguration()
  {
    if (savedConfiguration == null) {
      savedConfiguration = new IosProjectConfiguration();
//TODO      loadProjectConfiguration(savedConfiguration);
    }
    return savedConfiguration;
  }

  // We don't override loadProjectConfiguration since we currently have nothing to do

  @Override
  public void saveConfiguration(ProjectConfiguration c)
  {
//TODO    saveProjectConfiguration(c);
    savedConfiguration = null;
  }

  // We don't override saveProjectConfiguration since we currently have nothing to do

  @Override
  public boolean readyToBuild(MainController mainController)
  {
    if (!initSourceCache(mainController)) return false;

    return true;
  }

  private boolean initSourceCache(MainController mainController)
  {
    if (cacheZipFile().exists()) return true;

    IosConnectionParameters connectionParams = new IosConnectionParameters(getHost(), getPassword());

    ProgressIndicatingTask firstTimeTask = new DownloadSourceCacheTask(connectionParams, mainController);

    GooToolResourceBundle resourceBundle = GooTool.getTextProvider();
    try {
      GUIUtil.runTask(mainController.getMainWindow(), resourceBundle.getString("ios.firstTime.title"), firstTimeTask);
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Exception in iOS cache download", e);
      JOptionPane.showMessageDialog(mainController.getMainWindow(),
              resourceBundle.formatString("ios.firstTime.error.message", e.getLocalizedMessage(), e.getCause() != null ? e.getCause().getLocalizedMessage() : ""),
              resourceBundle.getString("ios.firstTime.error.title"),
              JOptionPane.ERROR_MESSAGE);
      return false;
    }

    return true;
  }

  @Override
  public Source getSource() throws IOException
  {
    return new IosSource(cacheZipFile());
  }

  private File cacheZipFile()
  {
    return new File(storageDir, "cache.zip");
  }

  @Override
  public Target getTarget() throws IOException
  {
    throw new IOException("IosProject.getTarget not yet implemented");
  }

  @Override
  public Codec getCodecForGameXml()
  {
    return GameFormat.NULL_BIN_CODEC;
  }

  @Override
  public Codec getCodecForProfile()
  {
    return GameFormat.NULL_BIN_CODEC;
  }

  @Override
  public ImageCodec getImageCodec()
  {
    return GameFormat.NULL_IMAGE_CODEC;
  }

  @Override
  public String getGameXmlFilename(String baseName)
  {
    return baseName;
  }

  @Override
  public String getGamePngFilename(String baseName)
  {
    return baseName + ".png";
  }

  @Override
  public String getGameSoundFilename(String baseName)
  {
    return baseName + ".aac";
  }

  @Override
  public String getGameMusicFilename(String baseName)
  {
    return baseName + ".aifc";
  }

  @Override
  public String getGameAnimMovieFilename(String baseName)
  {
    return baseName + ".binltl";
  }

  // Time to download through GUI (25/01/2011)

  private class DownloadSourceCacheTask extends ProgressIndicatingTask implements ProgressListener
  {
    private final IosConnectionParameters connectionParams;
    private final MainController mainController;

    public DownloadSourceCacheTask(IosConnectionParameters connectionParams, MainController mainController)
    {
      this.connectionParams = connectionParams;
      this.mainController = mainController;
    }

    @Override
    public void run() throws Exception
    {
      ResourceBundle resourceBundle = GooTool.getTextProvider();

      beginStep(resourceBundle.getString("ios.firstTime.status.connecting"), false);

      IosConnection connection = IosConnectionFactory.getConnection(connectionParams);

      beginStep(resourceBundle.getString("ios.firstTime.status.locating"), false);

      if (!connection.locateWog()) {
        JOptionPane.showMessageDialog(mainController.getMainWindow(),
                resourceBundle.getString("ios.firstTime.notFound.message"),
                resourceBundle.getString("ios.firstTime.notFound.title"),
                JOptionPane.ERROR_MESSAGE);
        return;
      }

      beginStep(resourceBundle.getString("ios.firstTime.status.caching"), true);

      JOptionPane.showMessageDialog(mainController.getMainWindow(),
              resourceBundle.getString("ios.firstTime.promptDownload.message"),
              resourceBundle.getString("ios.firstTime.promptDownload.title"),
              JOptionPane.INFORMATION_MESSAGE);

      File tempFile = File.createTempFile("gooioscache", null);
      connection.storeOriginalFiles(tempFile, this);

      IosConnectionFactory.returnConnection(connection);

      Utilities.moveFile(tempFile, cacheZipFile());
    }
  }
}
