/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import javax.swing.*;
import java.util.ResourceBundle;
import java.awt.event.ActionListener;

import com.goofans.gootool.Controller;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.HyperlinkLaunchingListener;

/**
 * GooTool's main menu.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
@SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection", "StringConcatenation"})
public class MainMenu
{
  //TODO move these into a config file
  private static final String URL_MANUAL = "http://goofans.com/gootool/about";
  private static final String URL_FAQ = "http://goofans.com/gootool/faq";
  private static final String URL_TROUBLESHOOTING = "http://goofans.com/gootool/troubleshooting";
  private static final String URL_FORUM = "http://goofans.com/forum";
  private static final ResourceBundle resourceBundle = GooTool.getTextProvider();

  private final JMenuBar menuBar;
  public final JMenuItem translatorModeMenuItem;
  public final JMenuItem generateIdMenuItem;
  public final JMenuItem removeIdMenuItem;

  private final Controller controller;
  private final HyperlinkLaunchingListener hyperlinkListener;

  public MainMenu(Controller controller)
  {
    this.controller = controller;
    menuBar = new JMenuBar();

    JMenu menu;
    hyperlinkListener = new HyperlinkLaunchingListener(menuBar);

    menu = createMenu("file");

    menu.add(createMenuItem(JMenuItem.class, "file.save", Controller.CMD_SAVE));
    menu.add(createMenuItem(JMenuItem.class, "file.saveAndLaunch", Controller.CMD_SAVE_AND_LAUNCH));
    menu.add(createMenuItem(JMenuItem.class, "file.revert", Controller.CMD_REVERT));

    JMenu iphoneMenuItem = createMenu("file.iphone");
    iphoneMenuItem.setEnabled(false);
    menu.add(iphoneMenuItem);

    iphoneMenuItem.add(createMenuItem(JMenuItem.class, "file.iphone.prepare", null));
    iphoneMenuItem.add(createMenuItem(JMenuItem.class, "file.iphone.deploy", null));

    if (PlatformSupport.getPlatform() != PlatformSupport.Platform.MACOSX) {
      menu.add(createMenuItem(JMenuItem.class, "file.exit", Controller.CMD_EXIT));
    }

    menuBar.add(menu);

    menu = createMenu("advanced");
    menuBar.add(menu);

    JMenu onlineIdMenuItem = createMenu("advanced.onlineId");
    menu.add(onlineIdMenuItem);

    onlineIdMenuItem.add(generateIdMenuItem = createMenuItem(JMenuItem.class, "advanced.onlineId.generate", Controller.CMD_GENERATE_ONLINE_ID));
    onlineIdMenuItem.add(removeIdMenuItem = createMenuItem(JMenuItem.class, "advanced.onlineId.remove", Controller.CMD_REMOVE_ONLINE_ID));

    JMenu decryptMenuItem = createMenu("advanced.decrypt");
    menu.add(decryptMenuItem);

    decryptMenuItem.add(createMenuItem(JMenuItem.class, "advanced.decrypt.binpc", Controller.CMD_DECRYPT_BIN_PC));
    decryptMenuItem.add(createMenuItem(JMenuItem.class, "advanced.decrypt.binmac", Controller.CMD_DECRYPT_BIN_MAC));
    decryptMenuItem.add(createMenuItem(JMenuItem.class, "advanced.decrypt.binltlmac", Controller.CMD_DECRYPT_PNGBINLTL_MAC));
    decryptMenuItem.add(createMenuItem(JMenuItem.class, "advanced.decrypt.anim", Controller.CMD_DECRYPT_ANIM));
    decryptMenuItem.add(createMenuItem(JMenuItem.class, "advanced.decrypt.movie", Controller.CMD_DECRYPT_MOVIE));

    JMenu encryptMenuItem = createMenu("advanced.encrypt");
    menu.add(encryptMenuItem);

    encryptMenuItem.add(createMenuItem(JMenuItem.class, "advanced.encrypt.binpc", Controller.CMD_ENCRYPT_BIN_PC));
    encryptMenuItem.add(createMenuItem(JMenuItem.class, "advanced.encrypt.binmac", Controller.CMD_ENCRYPT_BIN_MAC));
    encryptMenuItem.add(createMenuItem(JMenuItem.class, "advanced.encrypt.binltlmac", Controller.CMD_ENCRYPT_PNGBINLTL_MAC));

    menu.add(translatorModeMenuItem = createMenuItem(JCheckBoxMenuItem.class, "advanced.translatorMode", Controller.CMD_TRANSLATOR_MODE));

    menu = createMenu("help");

    menu.add(createURLMenuItem("help.manual", URL_MANUAL));
    menu.add(createURLMenuItem("help.faq", URL_FAQ));
    menu.add(createURLMenuItem("help.troubleshooting", URL_TROUBLESHOOTING));
    menu.add(createURLMenuItem("help.forum", URL_FORUM));

    menu.add(new JSeparator());

    menu.add(createMenuItem(JMenuItem.class, "help.gootoolUpdateCheck", Controller.CMD_GOOTOOL_UPDATE_CHECK));
    menu.add(createMenuItem(JMenuItem.class, "help.diagnostics", Controller.CMD_DIAGNOSTICS));

    if (PlatformSupport.getPlatform() != PlatformSupport.Platform.MACOSX) {
      menu.add(createMenuItem(JMenuItem.class, "help.about", Controller.CMD_ABOUT));
    }

    menuBar.add(menu);
  }

  private JMenu createMenu(String key)
  {
    JMenu menu = new JMenu(getMenuText(key));
    menu.setMnemonic(getMnemonic(key));
    return menu;
  }

  private JMenuItem createMenuItem(Class<? extends JMenuItem> itemClass, String key, String command)
  {
    return createMenuItemInternal(itemClass, key, controller, command);
  }

  private JMenuItem createURLMenuItem(String key, String url)
  {
    return createMenuItemInternal(JMenuItem.class, key, hyperlinkListener, url);
  }

  private JMenuItem createMenuItemInternal(Class<? extends JMenuItem> itemClass, String key, ActionListener listener, String command)
  {
    JMenuItem menuItem;
    try {
      menuItem = itemClass.newInstance();
    }
    catch (Exception e) {
      throw new RuntimeException("Unable to instantiate " + itemClass.getName());
    }

    menuItem.setText(getMenuText(key));
    menuItem.setMnemonic(getMnemonic(key));
    if (command == null) {
      menuItem.setEnabled(false);
    }
    else {
      menuItem.setActionCommand(command);
      menuItem.addActionListener(listener);
    }
    return menuItem;
  }

  public JMenuBar getJMenuBar()
  {
    return menuBar;
  }

  private String getMenuText(String key)
  {
    return resourceBundle.getString("mainMenu." + key);
  }

  private int getMnemonic(String key)
  {
    String mnemonicText = resourceBundle.getString("mainMenu." + key + ".mnemonic");
    KeyStroke keyStroke = KeyStroke.getKeyStroke(mnemonicText);
    if (keyStroke == null) throw new RuntimeException("Invalid mnemonic " + mnemonicText + " for " + key);
    return keyStroke.getKeyCode();
  }
}
