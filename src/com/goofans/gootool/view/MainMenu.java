/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.util.ResourceBundle;
import java.awt.event.ActionListener;

import com.goofans.gootool.MainController;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.ProjectController;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.HyperlinkLaunchingListener;

/**
 * GooTool's main menu.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MainMenu implements MenuListener
{
  //TODO move these into a config file
  private static final ResourceBundle resourceBundle = GooTool.getTextProvider();

  private final JMenuBar menuBar;
  private final JMenuItem saveMenuItem;
  private final JMenuItem saveAndLaunchMenuItem;
  private JMenuItem revertMenuItem;

  public final JMenuItem generateIdMenuItem;
  public final JMenuItem removeIdMenuItem;
  private final MainController mainController;
  private final HyperlinkLaunchingListener hyperlinkListener;

  public MainMenu(MainController mainController)
  {
    this.mainController = mainController;
    menuBar = new JMenuBar();

    JMenu menu;
    hyperlinkListener = new HyperlinkLaunchingListener(menuBar);

    menu = createMenu("mainMenu.file");

    menu.add(saveMenuItem = createMenuItem(JMenuItem.class, "mainMenu.file.save", ProjectController.CMD_SAVE));
    menu.add(saveAndLaunchMenuItem = createMenuItem(JMenuItem.class, "mainMenu.file.saveAndLaunch", ProjectController.CMD_SAVE_AND_LAUNCH)); // TODO need the right listener for this!
    menu.add(revertMenuItem = createMenuItem(JMenuItem.class, "mainMenu.file.revert", ProjectController.CMD_REVERT));

    if (PlatformSupport.getPlatform() != PlatformSupport.Platform.MACOSX) {
      menu.add(createMenuItem(JMenuItem.class, "mainMenu.file.exit", MainController.CMD_EXIT));
    }

    menuBar.add(menu);

    menu.addMenuListener(this);

    menu = createMenu("mainMenu.advanced");
    menuBar.add(menu);

    JMenu onlineIdMenuItem = createMenu("mainMenu.advanced.onlineId");
    menu.add(onlineIdMenuItem);

    onlineIdMenuItem.add(generateIdMenuItem = createMenuItem(JMenuItem.class, "mainMenu.advanced.onlineId.generate", ProjectController.CMD_GENERATE_ONLINE_ID));
    onlineIdMenuItem.add(removeIdMenuItem = createMenuItem(JMenuItem.class, "mainMenu.advanced.onlineId.remove", ProjectController.CMD_REMOVE_ONLINE_ID));

    JMenu decryptMenuItem = createMenu("mainMenu.advanced.decrypt");
    menu.add(decryptMenuItem);

    decryptMenuItem.add(createMenuItem(JMenuItem.class, "mainMenu.advanced.decrypt.binpc", MainController.CMD_DECRYPT_BIN_PC));
    decryptMenuItem.add(createMenuItem(JMenuItem.class, "mainMenu.advanced.decrypt.binmac", MainController.CMD_DECRYPT_BIN_MAC));
    decryptMenuItem.add(createMenuItem(JMenuItem.class, "mainMenu.advanced.decrypt.binltlmac", MainController.CMD_DECRYPT_PNGBINLTL_MAC));
    decryptMenuItem.add(createMenuItem(JMenuItem.class, "mainMenu.advanced.decrypt.anim", MainController.CMD_DECRYPT_ANIM));
    decryptMenuItem.add(createMenuItem(JMenuItem.class, "mainMenu.advanced.decrypt.movie", MainController.CMD_DECRYPT_MOVIE));

    JMenu encryptMenuItem = createMenu("mainMenu.advanced.encrypt");
    menu.add(encryptMenuItem);

    encryptMenuItem.add(createMenuItem(JMenuItem.class, "mainMenu.advanced.encrypt.binpc", MainController.CMD_ENCRYPT_BIN_PC));
    encryptMenuItem.add(createMenuItem(JMenuItem.class, "mainMenu.advanced.encrypt.binmac", MainController.CMD_ENCRYPT_BIN_MAC));
    encryptMenuItem.add(createMenuItem(JMenuItem.class, "mainMenu.advanced.encrypt.binltlmac", MainController.CMD_ENCRYPT_PNGBINLTL_MAC));

    menu.add(createMenuItem(JMenuItem.class, "mainMenu.advanced.localisation", MainController.CMD_LOCALISATION));

    menu = createMenu("mainMenu.help");

    menu.add(createURLMenuItem("mainMenu.help.manual", resourceBundle.getString("url.manual")));
    menu.add(createURLMenuItem("mainMenu.help.faq", resourceBundle.getString("url.faq")));
    menu.add(createURLMenuItem("mainMenu.help.troubleshooting", resourceBundle.getString("url.troubleshooting")));
    menu.add(createURLMenuItem("mainMenu.help.forum", resourceBundle.getString("url.forum")));

    menu.add(new JSeparator());

    menu.add(createMenuItem(JMenuItem.class, "mainMenu.help.gootoolUpdateCheck", MainController.CMD_GOOTOOL_UPDATE_CHECK));
    menu.add(createMenuItem(JMenuItem.class, "mainMenu.help.diagnostics", MainController.CMD_DIAGNOSTICS));

    if (PlatformSupport.getPlatform() != PlatformSupport.Platform.MACOSX) {
      menu.add(createMenuItem(JMenuItem.class, "mainMenu.help.about", MainController.CMD_ABOUT));
    }

    menuBar.add(menu);
  }

  private JMenu createMenu(String key)
  {
    JMenu menu = new JMenu(resourceBundle.getString(key));
    menu.setMnemonic(getMnemonic(key));
    return menu;
  }

  private JMenuItem createMenuItem(Class<? extends JMenuItem> itemClass, String key, String command)
  {
    return createMenuItemInternal(itemClass, key, mainController, command);
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

    menuItem.setText(resourceBundle.getString(key));
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

  private int getMnemonic(String key)
  {
    String mnemonicText = resourceBundle.getString(key + ".mnemonic"); //NON-NLS
    KeyStroke keyStroke = KeyStroke.getKeyStroke(mnemonicText);
    if (keyStroke == null) throw new RuntimeException("Invalid mnemonic " + mnemonicText + " for " + key); //NON-NLS
    return keyStroke.getKeyCode();
  }

  public void menuSelected(MenuEvent e)
  {
    if (mainController.getProjectController().getCurrentProject() == null) {
      saveMenuItem.setEnabled(false);
      saveAndLaunchMenuItem.setEnabled(false);
      revertMenuItem.setEnabled(false);
    }
    else {
      saveMenuItem.setEnabled(true);
      saveAndLaunchMenuItem.setEnabled(true);
      revertMenuItem.setEnabled(true);
    }
  }

  public void menuDeselected(MenuEvent e)
  {
  }

  public void menuCanceled(MenuEvent e)
  {
  }
}
