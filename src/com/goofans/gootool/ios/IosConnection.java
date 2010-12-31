/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.ios;

import net.infotrek.util.BinaryPlistParser;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.profile.ProfileData;
import com.goofans.gootool.util.Utilities;
import com.jcraft.jsch.*;

/**
 * TODO should really do connection pooling here.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class IosConnection
{
  private static final Logger log = Logger.getLogger(IosConnection.class.getName());

  private static final String KNOWN_HOSTS_FILE = "ssh_known_hosts";
  private static final String SSH_USER = "root";
  private static final int SSH_PORT = 22;
  private static final String SSH_CHANNEL_TYPE_SFTP = "sftp";

  private final String host;
  private final String password;
  private final JSch jsch;
  private Session session;
  private ChannelSftp sftp;

  private String wogDir = null;
  private boolean jailbrokenWog = false;
  private String prefsFile = null;
  private int prefsFileSize;


  public IosConnection(String host, String password)
  {
    this.host = host;
    if (password != null && password.length() > 0) {
      this.password = password;
    }
    else {
      this.password = null;
    }

    jsch = new JSch();

    initKnownHosts();
  }

  private void initKnownHosts()
  {
    try {
      File knownHostsFile = new File(PlatformSupport.getToolStorageDirectory(), KNOWN_HOSTS_FILE);

      boolean haveKnownHostsFile = knownHostsFile.exists();

      if (!haveKnownHostsFile) {
        haveKnownHostsFile = knownHostsFile.createNewFile();
        if (haveKnownHostsFile)
          log.log(Level.INFO, "Created SSH known hosts file at " + knownHostsFile);
        else
          log.log(Level.WARNING, "Can't create SSH known hosts file at " + knownHostsFile);
      }

      if (haveKnownHostsFile) jsch.setKnownHosts(knownHostsFile.getAbsolutePath());
    }
    catch (Exception e) {
      log.log(Level.WARNING, "Unable to initialise SSH known hosts file", e);
    }
  }

  public void close()
  {
    if (sftp != null) {
      sftp.disconnect();
      sftp = null;
    }

    if (session != null) {
      session.disconnect();
      session = null;
    }
  }

  private void connect() throws JSchException
  {
    if (session != null && session.isConnected()) return;

    session = jsch.getSession(SSH_USER, host, SSH_PORT);

    // username and password will be given via UserInfo interface.
    UserInfo ui = new MyUserInfo();
    session.setUserInfo(ui);

    log.log(Level.FINER, "Connecting to IOS device at " + host);

    session.connect();

    log.log(Level.FINER, "Connected to IOS device");

    sftp = (ChannelSftp) session.openChannel(SSH_CHANNEL_TYPE_SFTP);

    log.log(Level.FINER, "Opening SFTP channel");
    sftp.connect();

    log.log(Level.INFO, "SSH connected to " + host + ", server version " + session.getServerVersion() + ", SFTP protocol version " + sftp.version());
  }

  private synchronized boolean locateWog() throws JSchException, SftpException
  {
    if (wogDir != null) return true;

    connect();

//        SftpProgressMonitor monitor = new MyProgressMonitor();
//        int mode = ChannelSftp.OVERWRITE;
//            c.get(p1, p2, monitor, mode);
//          if (cmd.equals("rename")) c.rename(p1, p2);
//          if (cmd.equals("stat")) attrs = c.stat(p1);

    // Find wog.app

    Vector<ChannelSftp.LsEntry> appDirs = ls("/var/mobile/Applications");

    for (ChannelSftp.LsEntry dir : appDirs) {
      try {
        SftpATTRS attrs = sftp.stat(dir.getLongname() + "/wog.app");
        if (attrs.isDir()) {
          wogDir = dir.getLongname();
          break;
        }
      }
      catch (SftpException e) {
        // not found
      }
    }

    try {
      SftpATTRS attrs = sftp.stat("/Applications/wog.app");
      System.out.println("attrs = " + attrs);
      if (attrs.isDir()) {
        wogDir = "/Applications";
        jailbrokenWog = true;
      }
    }
    catch (SftpException e) {
      // not found
    }

    if (wogDir == null) {
      log.log(Level.WARNING, "World of Goo not found on " + host);
      return false;
    }
    else {
      log.log(Level.INFO, "World of Goo found at " + wogDir + " on " + host);
      return true;
    }
  }

  private synchronized void locateProfile() throws JSchException, SftpException
  {
    if (prefsFile != null) return;

    locateWog();
    if (wogDir == null) return;

    String prefsFile;
    if (jailbrokenWog) {
      prefsFile = "/var/mobile/Library/Preferences/com.2dboy.worldofgoo.plist";
    }
    else {
      prefsFile = wogDir + "Library/Preferences/com.2dboy.worldofgoo.plist";
    }


    SftpATTRS attrs;
    try {
      attrs = sftp.stat(prefsFile);
    }
    catch (SftpException e) {
      log.log(Level.SEVERE, "Prefs file not found at " + prefsFile);
//      throw new RuntimeException("prefs file not found - did you run wog yet?"); // TODO
      return;
    }

    this.prefsFile = prefsFile;
    this.prefsFileSize = (int) attrs.getSize();
  }


  public ProfileData getProfileData() throws JSchException, SftpException, IOException
  {
    locateProfile();
    if (prefsFile == null) return null;

    InputStream is = sftp.get(prefsFile);

    ByteArrayOutputStream os = new ByteArrayOutputStream(prefsFileSize);
    Utilities.copyStreams(is, os);
    is.close();
    os.close();

    BinaryPlistParser p = new BinaryPlistParser(os.toByteArray());
    Map root = (Map) p.parsePlist();
    byte[] pers2dat = (byte[]) root.get("pers2.dat");

    ProfileData profile = new ProfileData(pers2dat);

    return profile;

    // TODO Symbolic link our prefs to their originals on DEPLOY
  }

  /**
   * Tests the connection to the IOS host.
   *
   * @return True if connected successfully and World of Goo was found, false if connected successfully but no World of Goo found.
   * @throws Exception if any problem occurred (e.g. bad password) connecting to the device.
   */
  public boolean testConnection() throws Exception
  {
    connect();
    return locateWog();
  }

  private Vector<ChannelSftp.LsEntry> ls(String dir) throws SftpException
  {
    //noinspection unchecked
    return (Vector<ChannelSftp.LsEntry>) sftp.ls(dir);
  }

// TODO all popups should be modal and with an owner window
// TODO move to another class

  public class MyUserInfo implements UserInfo, UIKeyboardInteractive
  {
    public String getPassword()
    {
      return passwd;
    }

    public boolean promptYesNo(String str)
    {
      Object[] options = {"Yes", "No"};
      int foo = JOptionPane.showOptionDialog(null,
              str,
              "Warning",
              JOptionPane.DEFAULT_OPTION,
              JOptionPane.WARNING_MESSAGE,
              null, options, options[0]);
      return foo == 0;
    }

    String passwd;
    JTextField passwordField = (JTextField) new JPasswordField(20);

    public String getPassphrase()
    {
      return null;
    }

    public boolean promptPassphrase(String message)
    {
      return true;
    }

    public boolean promptPassword(String message)
    {
      if (password != null) {
        passwd = password;
        return true;
      }
      Object[] ob = {passwordField};
      int result =
              JOptionPane.showConfirmDialog(null, ob, message,
                      JOptionPane.OK_CANCEL_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        passwd = passwordField.getText();
        return true;
      }
      else {
        return false;
      }
    }

    public void showMessage(String message)
    {
      JOptionPane.showMessageDialog(null, message);
    }

    final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1,
            GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0);
    private Container panel;

    public String[] promptKeyboardInteractive(String destination,
                                              String name,
                                              String instruction,
                                              String[] prompt,
                                              boolean[] echo)
    {
      if (password != null && prompt.length == 1) {
        return new String[]{password};
      }

      panel = new JPanel();
      panel.setLayout(new GridBagLayout());

      gbc.weightx = 1.0;
      gbc.gridwidth = GridBagConstraints.REMAINDER;
      gbc.gridx = 0;
      panel.add(new JLabel(instruction), gbc);
      gbc.gridy++;

      gbc.gridwidth = GridBagConstraints.RELATIVE;

      JTextField[] texts = new JTextField[prompt.length];
      for (int i = 0; i < prompt.length; i++) {
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.weightx = 1;
        panel.add(new JLabel(prompt[i]), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1;
        if (echo[i]) {
          texts[i] = new JTextField(20);
        }
        else {
          texts[i] = new JPasswordField(20);
        }
        panel.add(texts[i], gbc);
        gbc.gridy++;
      }

      if (JOptionPane.showConfirmDialog(null, panel,
              destination + ": " + name,
              JOptionPane.OK_CANCEL_OPTION,
              JOptionPane.QUESTION_MESSAGE)
              == JOptionPane.OK_OPTION) {
        String[] response = new String[prompt.length];
        for (int i = 0; i < prompt.length; i++) {
          response[i] = texts[i].getText();
        }
        return response;
      }
      else {
        return null;  // cancel
      }
    }
  }

  public static class MyProgressMonitor implements SftpProgressMonitor
  {
    ProgressMonitor monitor;
    long count = 0;
    long max = 0;

    public void init(int op, String src, String dest, long max)
    {
      this.max = max;
      monitor = new ProgressMonitor(null,
              ((op == SftpProgressMonitor.PUT) ?
                      "put" : "get") + ": " + src,
              "", 0, (int) max);
      count = 0;
      percent = -1;
      monitor.setProgress((int) this.count);
      monitor.setMillisToDecideToPopup(1000);
    }

    private long percent = -1;

    public boolean count(long count)
    {
      this.count += count;

      if (percent >= this.count * 100 / max) {
        return true;
      }
      percent = this.count * 100 / max;

      monitor.setNote("Completed " + this.count + "(" + percent + "%) out of " + max + ".");
      monitor.setProgress((int) this.count);

      return !(monitor.isCanceled());
    }

    public void end()
    {
      monitor.close();
    }
  }

  // TODO new IOSCommunicationException

  public static void main(String[] args) throws JSchException, IOException, SftpException
  {
    IosConnection ios = new IosConnection("192.168.2.162", null);

    ProfileData data = ios.getProfileData();
    ios.close();
    System.out.println("data = " + data);
  }
}
