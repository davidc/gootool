package com.goofans.gootool.view;

import javax.swing.*;
import java.util.ResourceBundle;

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
public class MainMenu
{
  private final JMenuBar menuBar;
  public JMenuItem translatorModeMenuItem;

  //TODO move these into a config file
  private static final String URL_MANUAL = "http://goofans.com/gootool/about";
  private static final String URL_FAQ = "http://goofans.com/gootool/faq";
  private static final String URL_TROUBLESHOOTING = "http://goofans.com/gootool/troubleshooting";
  private static final String URL_FORUM = "http://goofans.com/forum";
  private static final ResourceBundle resourceBundle = GooTool.getTextProvider();
  private final Controller controller;
  private final HyperlinkLaunchingListener hyperlinkListener;

  public MainMenu(Controller controller)
  {
    this.controller = controller;
    menuBar = new JMenuBar();

    JMenu menu;
    hyperlinkListener = new HyperlinkLaunchingListener(menuBar);

    menu = createMenu("file");

    menu.add(createMenuItem("file.save", Controller.CMD_SAVE));
    menu.add(createMenuItem("file.saveAndLaunch", Controller.CMD_SAVE_AND_LAUNCH));
    menu.add(createMenuItem("file.revert", Controller.CMD_REVERT));

    JMenu iphoneMenuItem = createMenu("file.iphone");
    iphoneMenuItem.setEnabled(false);
    menu.add(iphoneMenuItem);

    iphoneMenuItem.add(createMenuItem("file.iphone.prepare", null));
    iphoneMenuItem.add(createMenuItem("file.iphone.deploy", null));

    if (PlatformSupport.getPlatform() != PlatformSupport.Platform.MACOSX) {
      menu.add(createMenuItem("file.exit", Controller.CMD_EXIT));
    }

    menuBar.add(menu);

    menu = createMenu("advanced");
    menuBar.add(menu);

    JMenu decryptMenuItem = createMenu("advanced.decrypt");
    menu.add(decryptMenuItem);

    decryptMenuItem.add(createMenuItem("advanced.decrypt.binpc", Controller.CMD_DECRYPT_BIN_PC));
    decryptMenuItem.add(createMenuItem("advanced.decrypt.binmac", Controller.CMD_DECRYPT_BIN_MAC));
    decryptMenuItem.add(createMenuItem("advanced.decrypt.binltlmac", Controller.CMD_DECRYPT_PNGBINLTL_MAC));
    decryptMenuItem.add(createMenuItem("advanced.decrypt.anim", Controller.CMD_DECRYPT_ANIM));
    decryptMenuItem.add(createMenuItem("advanced.decrypt.movie", Controller.CMD_DECRYPT_MOVIE));

    JMenu encryptMenuItem = createMenu("advanced.encrypt");
    menu.add(encryptMenuItem);

    encryptMenuItem.add(createMenuItem("advanced.encrypt.binpc", Controller.CMD_ENCRYPT_BIN_PC));
    encryptMenuItem.add(createMenuItem("advanced.encrypt.binmac", Controller.CMD_ENCRYPT_BIN_MAC));
    encryptMenuItem.add(createMenuItem("advanced.encrypt.binltlmac", Controller.CMD_ENCRYPT_PNGBINLTL_MAC));

    translatorModeMenuItem = new JCheckBoxMenuItem(getMenuText("advanced.translatorMode"));
    translatorModeMenuItem.setMnemonic(getMnemonic("advanced.translatorMode"));
    translatorModeMenuItem.setActionCommand(Controller.CMD_TRANSLATOR_MODE);
    translatorModeMenuItem.addActionListener(controller);
    menu.add(translatorModeMenuItem);

    menu = createMenu("help");

    menu.add(createURLMenuItem("help.manual", URL_MANUAL));
    menu.add(createURLMenuItem("help.faq", URL_FAQ));
    menu.add(createURLMenuItem("help.troubleshooting", URL_TROUBLESHOOTING));
    menu.add(createURLMenuItem("help.forum", URL_FORUM));

    menu.add(new JSeparator());

    menu.add(createMenuItem("help.gootoolUpdateCheck", Controller.CMD_GOOTOOL_UPDATE_CHECK));
    menu.add(createMenuItem("help.diagnostics", Controller.CMD_DIAGNOSTICS));

    if (PlatformSupport.getPlatform() != PlatformSupport.Platform.MACOSX) {
      menu.add(createMenuItem("help.about", Controller.CMD_ABOUT));
    }

    menuBar.add(menu);
  }

  private JMenu createMenu(String key)
  {
    JMenu menu = new JMenu(getMenuText(key));
    menu.setMnemonic(getMnemonic(key));
    return menu;
  }

  private JMenuItem createMenuItem(String key, String command)
  {
    JMenuItem menuItem = new JMenuItem(getMenuText(key));
    menuItem.setMnemonic(getMnemonic(key));
    if (command == null) {
      menuItem.setEnabled(false);
    }
    else {
      menuItem.setActionCommand(command);
      menuItem.addActionListener(controller);
    }
    return menuItem;
  }

  private JMenuItem createURLMenuItem(String key, String url)
  {
    JMenuItem menuItem;
    menuItem = new JMenuItem(getMenuText(key));
    menuItem.setMnemonic(getMnemonic(key));
    menuItem.setActionCommand(url);
    menuItem.addActionListener(hyperlinkListener);
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
