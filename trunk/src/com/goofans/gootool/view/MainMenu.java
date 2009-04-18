package com.goofans.gootool.view;

import com.goofans.gootool.Controller;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.TextProvider;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.HyperlinkLaunchingListener;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * TODO internationalise the mnemonics!
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MainMenu
{
  private JMenuBar menuBar;
  public JMenuItem translatorModeMenuItem;
  private static final String URL_MANUAL = "http://goofans.com/gootool/about";
  private static final String URL_FAQ = "http://goofans.com/gootool/faq";
  private static final String URL_TROUBLESHOOTING = "http://goofans.com/gootool/troubleshooting";
  private static final String URL_FORUM = "http://goofans.com/forum";

  public MainMenu(Controller controller)
  {
    menuBar = new JMenuBar();

    JMenu menu;
    JMenuItem menuItem;
    HyperlinkLaunchingListener hyperlinkListener = new HyperlinkLaunchingListener(menuBar);

    TextProvider textProvider = GooTool.getTextProvider();

    menu = new JMenu(textProvider.getText("mainMenu.file"));
    menu.setMnemonic(KeyEvent.VK_F);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.file.save"));
    menuItem.setMnemonic(KeyEvent.VK_S);
    menuItem.setActionCommand(Controller.CMD_SAVE);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.file.saveAndLaunch"));
    menuItem.setMnemonic(KeyEvent.VK_L);
    menuItem.setActionCommand(Controller.CMD_SAVE_AND_LAUNCH);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.file.revert"));
    menuItem.setMnemonic(KeyEvent.VK_R);
    menuItem.setActionCommand(Controller.CMD_REVERT);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    if (PlatformSupport.getPlatform() != PlatformSupport.Platform.MACOSX) {
      menuItem = new JMenuItem(textProvider.getText("mainMenu.file.exit"));
      menuItem.setMnemonic(KeyEvent.VK_X);
      menuItem.setActionCommand(Controller.CMD_EXIT);
      menuItem.addActionListener(controller);
      menu.add(menuItem);
    }

    menuBar.add(menu);

    menu = new JMenu(textProvider.getText("mainMenu.advanced"));
    menu.setMnemonic(KeyEvent.VK_V);
    menuBar.add(menu);

    JMenu decryptMenuItem = new JMenu(textProvider.getText("mainMenu.advanced.decrypt"));
    decryptMenuItem.setMnemonic(KeyEvent.VK_D);
    menu.add(decryptMenuItem);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.advanced.decrypt.binpc"));
    menuItem.setMnemonic(KeyEvent.VK_P);
    menuItem.setActionCommand(Controller.CMD_DECRYPT_BIN_PC);
    menuItem.addActionListener(controller);
    decryptMenuItem.add(menuItem);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.advanced.decrypt.binmac"));
    menuItem.setMnemonic(KeyEvent.VK_M);
    menuItem.setActionCommand(Controller.CMD_DECRYPT_BIN_MAC);
    menuItem.addActionListener(controller);
    decryptMenuItem.add(menuItem);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.advanced.decrypt.binltlmac"));
    menuItem.setMnemonic(KeyEvent.VK_I);
    menuItem.setActionCommand(Controller.CMD_DECRYPT_PNGBINLTL_MAC);
    menuItem.addActionListener(controller);
    decryptMenuItem.add(menuItem);

    JMenu encryptMenuItem = new JMenu(textProvider.getText("mainMenu.advanced.encrypt"));
    encryptMenuItem.setMnemonic(KeyEvent.VK_E);
    menu.add(encryptMenuItem);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.advanced.encrypt.binpc"));
    menuItem.setMnemonic(KeyEvent.VK_P);
    menuItem.setActionCommand(Controller.CMD_ENCRYPT_BIN_PC);
    menuItem.addActionListener(controller);
    encryptMenuItem.add(menuItem);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.advanced.encrypt.binmac"));
    menuItem.setMnemonic(KeyEvent.VK_M);
    menuItem.setActionCommand(Controller.CMD_ENCRYPT_BIN_MAC);
    menuItem.addActionListener(controller);
    encryptMenuItem.add(menuItem);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.advanced.encrypt.binltlmac"));
    menuItem.setMnemonic(KeyEvent.VK_I);
    menuItem.setActionCommand(Controller.CMD_ENCRYPT_PNGBINLTL_MAC);
    menuItem.addActionListener(controller);
    encryptMenuItem.add(menuItem);

    translatorModeMenuItem = new JCheckBoxMenuItem(textProvider.getText("mainMenu.advanced.translatorMode"));
    translatorModeMenuItem.setMnemonic(KeyEvent.VK_T);
    translatorModeMenuItem.setActionCommand(Controller.CMD_TRANSLATOR_MODE);
    translatorModeMenuItem.addActionListener(controller);
    menu.add(translatorModeMenuItem);

    JMenu testMenuItem = new JMenu("Testing");
    testMenuItem.setMnemonic(KeyEvent.VK_T);
    menu.add(testMenuItem);

    menuItem = new JMenuItem("anim decode");
    try {
      Class.forName("com.goofans.gootool.movie.BinImageAnimation");
      menuItem.setActionCommand("animdecode");
      menuItem.addActionListener(controller);
    }
    catch (ClassNotFoundException e) {
      menuItem.setEnabled(false);
    }
    testMenuItem.add(menuItem);

    menuItem = new JMenuItem("anim encode");
    try {
      Class.forName("com.goofans.gootool.movie.BinImageAnimation");
      menuItem.setActionCommand("animencode");
      menuItem.addActionListener(controller);
    }
    catch (ClassNotFoundException e) {
      menuItem.setEnabled(false);
    }
    testMenuItem.add(menuItem);

    menuItem = new JMenuItem("movie decode");
    try {
      Class.forName("com.goofans.gootool.movie.BinMovie");
      menuItem.setActionCommand("movdecode");
      menuItem.addActionListener(controller);
    }
    catch (ClassNotFoundException e) {
      menuItem.setEnabled(false);
    }
    testMenuItem.add(menuItem);

    menuItem = new JMenuItem("movie encode");
    try {
      Class.forName("com.goofans.gootool.movie.BinMovie");
      menuItem.setActionCommand("movencode");
      menuItem.addActionListener(controller);
    }
    catch (ClassNotFoundException e) {
      menuItem.setEnabled(false);
    }
    testMenuItem.add(menuItem);

    menuItem = new JMenuItem("timeline editor");
    try {
      Class.forName("com.goofans.gootool.movie.TimelineEditor");
      menuItem.setActionCommand("tled");
      menuItem.addActionListener(controller);
    }
    catch (ClassNotFoundException e) {
      menuItem.setEnabled(false);
    }
    testMenuItem.add(menuItem);

    menuItem = new JMenuItem("transform editor");
    try {
      Class.forName("com.goofans.gootool.movie.TransformEditor");
      menuItem.setActionCommand("xfed");
      menuItem.addActionListener(controller);
    }
    catch (ClassNotFoundException e) {
      menuItem.setEnabled(false);
    }
    testMenuItem.add(menuItem);

    menuItem = new JMenuItem("level editor");
    try {
      Class.forName("com.goofans.gootool.leveledit.view.LevelEditor");
      menuItem.setActionCommand("leved");
      menuItem.addActionListener(controller);
    }
    catch (ClassNotFoundException e) {
      menuItem.setEnabled(false);
    }
    testMenuItem.add(menuItem);

    menu = new JMenu(textProvider.getText("mainMenu.help"));
    menu.setMnemonic(KeyEvent.VK_H);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.help.manual"));
    menuItem.setMnemonic(KeyEvent.VK_M);
    menuItem.setActionCommand(URL_MANUAL);
    menuItem.addActionListener(hyperlinkListener);
    menu.add(menuItem);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.help.faq"));
    menuItem.setMnemonic(KeyEvent.VK_F);
    menuItem.setActionCommand(URL_FAQ);
    menuItem.addActionListener(hyperlinkListener);
    menu.add(menuItem);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.help.troubleshooting"));
    menuItem.setMnemonic(KeyEvent.VK_T);
    menuItem.setActionCommand(URL_TROUBLESHOOTING);
    menuItem.addActionListener(hyperlinkListener);
    menu.add(menuItem);

    menuItem = new JMenuItem(textProvider.getText("mainMenu.help.forum"));
    menuItem.setMnemonic(KeyEvent.VK_O);
    menuItem.setActionCommand(URL_FORUM);
    menuItem.addActionListener(hyperlinkListener);
    menu.add(menuItem);

    menu.add(new JSeparator());

    menuItem = new JMenuItem(textProvider.getText("mainMenu.help.checkForUpdates"));
    menuItem.setMnemonic(KeyEvent.VK_U);
    menuItem.setActionCommand(Controller.CMD_CHECK_FOR_UPDATES);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    if (PlatformSupport.getPlatform() != PlatformSupport.Platform.MACOSX) {
      menuItem = new JMenuItem(textProvider.getText("mainMenu.help.about"));
      menuItem.setMnemonic(KeyEvent.VK_A);
      menuItem.setActionCommand(Controller.CMD_ABOUT);
      menuItem.addActionListener(controller);
      menu.add(menuItem);
    }

    menuBar.add(menu);
  }

  public JMenuBar getJMenuBar()
  {
    return menuBar;
  }
}
