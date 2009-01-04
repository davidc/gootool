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
    HyperlinkLaunchingListener hyperlinkListener = new HyperlinkLaunchingListener();

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

    translatorModeMenuItem = new JCheckBoxMenuItem(textProvider.getText("mainMenu.advanced.translatorMode"));
    translatorModeMenuItem.setMnemonic(KeyEvent.VK_T);
    translatorModeMenuItem.setActionCommand(Controller.CMD_TRANSLATOR_MODE);
    translatorModeMenuItem.addActionListener(controller);
    menu.add(translatorModeMenuItem);

    menuBar.add(menu);

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
