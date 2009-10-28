package com.goofans.gootool.view;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

import com.goofans.gootool.Controller;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.HyperlinkLaunchingListener;

/**
 * TODO internationalise the mnemonics!
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

  public MainMenu(Controller controller)
  {
    menuBar = new JMenuBar();

    JMenu menu;
    JMenuItem menuItem;
    HyperlinkLaunchingListener hyperlinkListener = new HyperlinkLaunchingListener(menuBar);

    ResourceBundle resourceBundle = GooTool.getTextProvider();

    menu = new JMenu(resourceBundle.getString("mainMenu.file"));
    menu.setMnemonic(KeyEvent.VK_F);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.file.save"));
    menuItem.setMnemonic(KeyEvent.VK_S);
    menuItem.setActionCommand(Controller.CMD_SAVE);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.file.saveAndLaunch"));
    menuItem.setMnemonic(KeyEvent.VK_L);
    menuItem.setActionCommand(Controller.CMD_SAVE_AND_LAUNCH);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.file.revert"));
    menuItem.setMnemonic(KeyEvent.VK_R);
    menuItem.setActionCommand(Controller.CMD_REVERT);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    JMenuItem iphoneMenuItem = new JMenu(resourceBundle.getString("mainMenu.file.iphone"));
    iphoneMenuItem.setMnemonic(KeyEvent.VK_I);
    iphoneMenuItem.setEnabled(false);
    menu.add(iphoneMenuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.file.iphone.prepare"));
    menuItem.setMnemonic(KeyEvent.VK_P);
    menuItem.setEnabled(false);
    iphoneMenuItem.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.file.iphone.deploy"));
    menuItem.setMnemonic(KeyEvent.VK_D);
    menuItem.setEnabled(false);
    iphoneMenuItem.add(menuItem);

    if (PlatformSupport.getPlatform() != PlatformSupport.Platform.MACOSX) {
      menuItem = new JMenuItem(resourceBundle.getString("mainMenu.file.exit"));
      menuItem.setMnemonic(KeyEvent.VK_X);
      menuItem.setActionCommand(Controller.CMD_EXIT);
      menuItem.addActionListener(controller);
      menu.add(menuItem);
    }

    menuBar.add(menu);

    menu = new JMenu(resourceBundle.getString("mainMenu.advanced"));
    menu.setMnemonic(KeyEvent.VK_V);
    menuBar.add(menu);

    JMenu decryptMenuItem = new JMenu(resourceBundle.getString("mainMenu.advanced.decrypt"));
    decryptMenuItem.setMnemonic(KeyEvent.VK_D);
    menu.add(decryptMenuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.advanced.decrypt.binpc"));
    menuItem.setMnemonic(KeyEvent.VK_P);
    menuItem.setActionCommand(Controller.CMD_DECRYPT_BIN_PC);
    menuItem.addActionListener(controller);
    decryptMenuItem.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.advanced.decrypt.binmac"));
    menuItem.setMnemonic(KeyEvent.VK_M);
    menuItem.setActionCommand(Controller.CMD_DECRYPT_BIN_MAC);
    menuItem.addActionListener(controller);
    decryptMenuItem.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.advanced.decrypt.binltlmac"));
    menuItem.setMnemonic(KeyEvent.VK_I);
    menuItem.setActionCommand(Controller.CMD_DECRYPT_PNGBINLTL_MAC);
    menuItem.addActionListener(controller);
    decryptMenuItem.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.advanced.decrypt.anim"));
    menuItem.setMnemonic(KeyEvent.VK_A);
    menuItem.setActionCommand(Controller.CMD_DECRYPT_ANIM);
    menuItem.addActionListener(controller);
    decryptMenuItem.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.advanced.decrypt.movie"));
    menuItem.setMnemonic(KeyEvent.VK_V);
    menuItem.setActionCommand(Controller.CMD_DECRYPT_MOVIE);
    menuItem.addActionListener(controller);
    decryptMenuItem.add(menuItem);

    JMenu encryptMenuItem = new JMenu(resourceBundle.getString("mainMenu.advanced.encrypt"));
    encryptMenuItem.setMnemonic(KeyEvent.VK_E);
    menu.add(encryptMenuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.advanced.encrypt.binpc"));
    menuItem.setMnemonic(KeyEvent.VK_P);
    menuItem.setActionCommand(Controller.CMD_ENCRYPT_BIN_PC);
    menuItem.addActionListener(controller);
    encryptMenuItem.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.advanced.encrypt.binmac"));
    menuItem.setMnemonic(KeyEvent.VK_M);
    menuItem.setActionCommand(Controller.CMD_ENCRYPT_BIN_MAC);
    menuItem.addActionListener(controller);
    encryptMenuItem.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.advanced.encrypt.binltlmac"));
    menuItem.setMnemonic(KeyEvent.VK_I);
    menuItem.setActionCommand(Controller.CMD_ENCRYPT_PNGBINLTL_MAC);
    menuItem.addActionListener(controller);
    encryptMenuItem.add(menuItem);

    translatorModeMenuItem = new JCheckBoxMenuItem(resourceBundle.getString("mainMenu.advanced.translatorMode"));
    translatorModeMenuItem.setMnemonic(KeyEvent.VK_T);
    translatorModeMenuItem.setActionCommand(Controller.CMD_TRANSLATOR_MODE);
    translatorModeMenuItem.addActionListener(controller);
    menu.add(translatorModeMenuItem);

    JMenu testMenuItem = new JMenu("Testing");
    testMenuItem.setMnemonic(KeyEvent.VK_T);
    menu.add(testMenuItem);

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
      Class.forName("com.goofans.gootoolsp.movie.TimelineEditor");
      menuItem.setActionCommand("tled");
      menuItem.addActionListener(controller);
    }
    catch (ClassNotFoundException e) {
      menuItem.setEnabled(false);
    }
    testMenuItem.add(menuItem);

    menuItem = new JMenuItem("transform editor");
    try {
      Class.forName("com.goofans.gootoolsp.movie.TransformEditor");
      menuItem.setActionCommand("xfed");
      menuItem.addActionListener(controller);
    }
    catch (ClassNotFoundException e) {
      menuItem.setEnabled(false);
    }
    testMenuItem.add(menuItem);

    menuItem = new JMenuItem("level editor");
    try {
      Class.forName("com.goofans.gootoolsp.leveledit.view.LevelEditor");
      menuItem.setActionCommand("leved");
      menuItem.addActionListener(controller);
    }
    catch (ClassNotFoundException e) {
      menuItem.setEnabled(false);
    }
    testMenuItem.add(menuItem);

    menu = new JMenu(resourceBundle.getString("mainMenu.help"));
    menu.setMnemonic(KeyEvent.VK_H);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.help.manual"));
    menuItem.setMnemonic(KeyEvent.VK_M);
    menuItem.setActionCommand(URL_MANUAL);
    menuItem.addActionListener(hyperlinkListener);
    menu.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.help.faq"));
    menuItem.setMnemonic(KeyEvent.VK_F);
    menuItem.setActionCommand(URL_FAQ);
    menuItem.addActionListener(hyperlinkListener);
    menu.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.help.troubleshooting"));
    menuItem.setMnemonic(KeyEvent.VK_T);
    menuItem.setActionCommand(URL_TROUBLESHOOTING);
    menuItem.addActionListener(hyperlinkListener);
    menu.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.help.forum"));
    menuItem.setMnemonic(KeyEvent.VK_O);
    menuItem.setActionCommand(URL_FORUM);
    menuItem.addActionListener(hyperlinkListener);
    menu.add(menuItem);

    menu.add(new JSeparator());

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.help.checkForUpdates"));
    menuItem.setMnemonic(KeyEvent.VK_U);
    menuItem.setActionCommand(Controller.CMD_CHECK_FOR_UPDATES);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    menuItem = new JMenuItem(resourceBundle.getString("mainMenu.help.diagnostics"));
    menuItem.setMnemonic(KeyEvent.VK_D);
    menuItem.setActionCommand(Controller.CMD_DIAGNOSTICS);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    if (PlatformSupport.getPlatform() != PlatformSupport.Platform.MACOSX) {
      menuItem = new JMenuItem(resourceBundle.getString("mainMenu.help.about"));
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
