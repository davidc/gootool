/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.ios;

import net.infotrek.util.BinaryPlistParser;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.profile.ProfileData;
import com.goofans.gootool.util.ProgressListener;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.wog.WorldBuilder;
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
  private static final int CONNECT_TIMEOUT = 5000; //msec

  static {
    JSch.setLogger(new JSchLogger());
  }

  private final IosConnectionParameters params;
  private final JSch jsch;
  private Session session;
  private ChannelSftp sftp;

  private String wogDir;
  private boolean jailbrokenWog = false;
  private String prefsFile = null;
  private int prefsFileSize;
  private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

  public IosConnection(IosConnectionParameters params)
  {
    this.params = params;

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

  public IosConnectionParameters getParams()
  {
    return params;
  }

  /**
   * Tests that the connection is still connected and usable by sending a dummy command
   *
   * @return true if the connection is working, otherwise false.
   */
  public boolean testConnection()
  {
    if (sftp == null || session == null) return false;

    if (!(sftp.isConnected() && session.isConnected())) return false;

    try {
      //This won't work, because this only SENDS a keepalive, it doesn't check for a response.
      //session.sendKeepAliveMsg();

      // Instead, we do a stat() on the root directory and ignore the results. If connection is broken, an exception will have been thrown.
      sftp.stat("/");

      return true;
    }
    catch (SftpException e) {
      log.log(Level.WARNING, "SSH connection lost during keepalive", e);
      return false;
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

  void connect() throws IosException
  {
    if (session != null && session.isConnected()) return;

    try {
      session = jsch.getSession(SSH_USER, params.host, SSH_PORT);
    }
    catch (JSchException e) {
      throw new IosException("Unable to initialise session for connection to " + params.host, e);
    }

    // username and password will be given via UserInfo interface.
    UserInfo ui = new MyUserInfo();
    session.setUserInfo(ui);

    log.log(Level.FINER, "Connecting to iOS device at " + params.host);

    try {
      session.connect(CONNECT_TIMEOUT);
    }
    catch (JSchException e) {
      throw new IosException("Unable to connect to " + params.host + ". Reason: " + e.getLocalizedMessage(), e);
    }

    log.log(Level.FINER, "Connected to iOS device");

    try {
      sftp = (ChannelSftp) session.openChannel(SSH_CHANNEL_TYPE_SFTP);
      log.log(Level.FINER, "Opening SFTP channel");
      sftp.connect();
    }
    catch (JSchException e) {
      throw new IosException("Unable to open SFTP channel on " + params.host, e);
    }

    log.log(Level.INFO, "SSH connected to " + params.host + ", server version " + session.getServerVersion() + ", SFTP protocol version " + sftp.version());
  }

  public synchronized boolean locateWog() throws IosException
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
      if (attrs.isDir()) {
        wogDir = "/Applications";
        jailbrokenWog = true;
      }
    }
    catch (SftpException e) {
      // not found
    }

    if (wogDir == null) {
      log.log(Level.WARNING, "World of Goo not found on " + params.host);
      return false;
    }
    else {
      log.log(Level.INFO, "World of Goo found at " + wogDir + " on " + params.host);
      return true;
    }
  }

  private synchronized void locateProfile() throws IosException
  {
    if (prefsFile != null) return;

    if (!locateWog()) return;

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

  public ProfileData getProfileData() throws IosException, IOException
  {
    locateProfile();
    if (prefsFile == null) return null;

    InputStream is;
    try {
      is = sftp.get(prefsFile);
    }
    catch (SftpException e) {
      throw new IosException("Unable to get preferences file " + prefsFile, e);
    }

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

  private Vector<ChannelSftp.LsEntry> ls(String dir) throws IosException
  {
    //noinspection unchecked
    try {
      return (Vector<ChannelSftp.LsEntry>) sftp.ls(dir);
    }
    catch (SftpException e) {
      throw new IosException("Unable to list directory " + dir, e);
    }
  }

  public void storeOriginalFiles(File zipfile, ProgressListener listener) throws IosException, IOException
  {
    if (!locateWog()) throw new IosException("World of Goo was not found");

    boolean failure = true;

    ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipfile)));
    try {
      zos.setMethod(ZipOutputStream.STORED);

      log.log(Level.FINE, "Counting source files");

      List<OriginalFile> originalFiles = new ArrayList<OriginalFile>(WorldBuilder.ESTIMATED_SOURCE_FILES);

      long totalSize = prepareDir(wogDir + "/wog.app", "", originalFiles);

      log.log(Level.INFO, originalFiles.size() + " source files (" + totalSize + " total bytes) to copy");

      ByteArrayOutputStream tmpbuf = new ByteArrayOutputStream(8196);
      CRC32 crc32 = new CRC32();
      long doneSize = 0;

      for (int i = 0; i < originalFiles.size(); i++) {
        OriginalFile originalFile = originalFiles.get(i);

//        System.out.println("Progress: " + i + " of " + originalFiles.size());
        if (listener != null && i % 10 == 0) {
          listener.progressStep((100f * doneSize) / totalSize);
        }

        byte[] data;

        // Prepare the zip entry
        ZipEntry ze = new ZipEntry(originalFile.zipLocation);
        long mtime = originalFile.attrs.getMTime() * 1000L;
        System.out.println("new Date(mtime) = " + new Date(mtime));
        System.out.println("originalFile.attrs.getMTime() = " + mtime);
        ze.setTime(mtime); // TODO this is not working, it gives 1/1/1980

        // Get the data
        if (originalFile.attrs.isDir()) {
          data = EMPTY_BYTE_ARRAY;

          ze.setSize(0);
        }
        else {
          // Get the file into our tmpbuf
          tmpbuf.reset();
          log.finest("Downloading " + originalFile.iosLocation);
          sftp.get(originalFile.iosLocation, tmpbuf);
          data = tmpbuf.toByteArray(); // TODO this is inefficient since it returns a COPY of the data.

          long size = originalFile.attrs.getSize();
          ze.setSize(size);
          doneSize += size;
        }

        // Build the CRC32
        crc32.reset();
        crc32.update(data);
        ze.setCrc(crc32.getValue());

        // Output to zip file
        zos.putNextEntry(ze);
        zos.write(data);
        zos.closeEntry();
      }

      failure = false;
    }
    catch (SftpException e) {
      throw new IosException("Can't retrieve files from " + params.host, e);
    }
    finally {
      try {
        zos.close();
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Unable to close zip output stream", e);
      }

      if (failure) {
        try {
          Utilities.deleteFileIfExists(zipfile);
        }
        catch (IOException e) {
          log.log(Level.WARNING, "Unable to remove part-completed output ZIP file " + zipfile, e);
        }
      }
    }
  }

  private long prepareDir(String dir, String prefix, List<OriginalFile> originalFiles) throws IosException
  {
    long totalSize = 0;

    Vector<ChannelSftp.LsEntry> entries = ls(dir);

    for (ChannelSftp.LsEntry entry : entries) {
      if (isSkippedSourceFile(entry)) continue;

      SftpATTRS attrs = entry.getAttrs();

      if (!attrs.isLink()) {
        OriginalFile of = new OriginalFile();
        of.iosLocation = dir + "/" + entry.getFilename();
        of.zipLocation = prefix + entry.getFilename();
        of.attrs = attrs;
        originalFiles.add(of);

        // TODO test that this is now storing the directories as zip entries too
        if (attrs.isDir()) {
          totalSize += prepareDir(dir + "/" + entry.getFilename(), prefix + entry.getFilename() + "/", originalFiles);
        }
        else {
          totalSize += attrs.getSize();
        }
      }
    }

    return totalSize;
  }

  private boolean isSkippedSourceFile(ChannelSftp.LsEntry entry)
  {
    if (".".equals(entry.getFilename()) || "..".equals(entry.getFilename())) return true;

    // Don't need signed resources since we're jailbroken - and they'd be wrong anyway.
    if ("_CodeSignature".equals(entry.getFilename())) return true;

    // TODO: Skip SC_Info too?
    return false;
  }

  private static class OriginalFile
  {
    public String iosLocation;
    public String zipLocation;
    public SftpATTRS attrs;
  }


// TODO all popups should be modal and with an owner window
// TODO move to another class

  // TODO when writing files back, do they need to be owned by root?

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
      if (params.password != null) {
        passwd = params.password;
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
      if (params.password != null && prompt.length == 1) {
        return new String[]{params.password};
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

  public static void main(String[] args) throws IosException, IOException
  {
    IosConnection ios = new IosConnection(new IosConnectionParameters("192.168.2.162", null));
    try {
//    ProfileData data = ios.getProfileData();
//    System.out.println("data = " + data);

      ios.storeOriginalFiles(new File("source.zip"), null);
    }
    finally {
      ios.close();
    }
  }
}
