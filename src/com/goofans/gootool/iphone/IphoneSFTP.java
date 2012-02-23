/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.iphone;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

import com.jcraft.jsch.*;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class IphoneSFTP
{

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "HardcodedFileSeparator"})
  public static void main(String[] args) throws JSchException, SftpException
  {
    String host = "192.168.5.12";
    String user = "root";
    int port = 22;

    JSch jsch = new JSch();
    Session session = jsch.getSession(user, host, port);

    // username and password will be given via UserInfo interface.
    UserInfo ui = new MyUserInfo();
    session.setUserInfo(ui);

    session.connect();

    try {
      Channel channel = session.openChannel("sftp");
      channel.connect();
      ChannelSftp c = (ChannelSftp) channel;

      try {
        c.cd("/var/mobile");

        @SuppressWarnings({"unchecked"})
        Vector<ChannelSftp.LsEntry> lsEntries = (Vector<ChannelSftp.LsEntry>) c.ls(".");

        for (ChannelSftp.LsEntry lsEntry : lsEntries) {
          System.out.println("lsEntry.getFilename() = " + lsEntry.getFilename());
          System.out.println("lsEntry.getLongname() = " + lsEntry.getLongname());
          System.out.println("lsEntry.getAttrs().getPermissionsString() = " + lsEntry.getAttrs().getPermissionsString());
        }


        // c.rm(), c.rmdir()
        // c.chgrp(), c.chown(), c.chmod()

//        SftpProgressMonitor monitor = new MyProgressMonitor();
//        int mode = ChannelSftp.OVERWRITE;
//            c.get(p1, p2, monitor, mode);
//          if (cmd.equals("rename")) c.rename(p1, p2);
//          if (cmd.equals("stat")) attrs = c.stat(p1);
        System.out.println("SFTP protocol version " + c.version());

      }
      finally {
        c.disconnect();
      }
    }
    finally {
      session.disconnect();
    }

  }

  public static class MyUserInfo implements UserInfo, UIKeyboardInteractive
  {
    public String getPassword()
    {
      return passwd;
    }

    public boolean promptYesNo(String str)
    {
      Object[] options = {"yes", "no"};
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

    final GridBagConstraints gbc =
            new GridBagConstraints(0, 0, 1, 1, 1, 1,
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
}
