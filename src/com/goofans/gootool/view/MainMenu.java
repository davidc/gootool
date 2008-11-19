package com.goofans.gootool.view;

import com.goofans.gootool.Controller;
import com.goofans.gootool.util.HyperlinkLaunchingListener;

import javax.swing.*;
import java.awt.event.KeyEvent;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MainMenu
{
  private JMenuBar menuBar;
  public JMenuItem translatorModeMenuItem;

  public MainMenu(Controller controller)
  {
    menuBar = new JMenuBar();

    JMenu menu;
    JMenuItem menuItem;
    HyperlinkLaunchingListener hyperlinkListener = new HyperlinkLaunchingListener();

    menu = new JMenu("File");
    menu.setMnemonic(KeyEvent.VK_F);

    menuItem = new JMenuItem("Save");
    menuItem.setMnemonic(KeyEvent.VK_S);
    menuItem.setActionCommand(Controller.CMD_SAVE);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    menuItem = new JMenuItem("Save and Launch");
    menuItem.setMnemonic(KeyEvent.VK_L);
    menuItem.setActionCommand(Controller.CMD_SAVE_AND_LAUNCH);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    menuItem = new JMenuItem("Revert to saved configuration");
    menuItem.setMnemonic(KeyEvent.VK_R);
    menuItem.setActionCommand(Controller.CMD_REVERT);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    menuItem = new JMenuItem("Exit");
    menuItem.setMnemonic(KeyEvent.VK_X);
    menuItem.setActionCommand(Controller.CMD_EXIT);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    menuBar.add(menu);

    menu = new JMenu("Advanced");
    menu.setMnemonic(KeyEvent.VK_V);

    translatorModeMenuItem = new JCheckBoxMenuItem("Translator mode");
    translatorModeMenuItem.setMnemonic(KeyEvent.VK_T);
    translatorModeMenuItem.setActionCommand(Controller.CMD_TRANSLATOR_MODE);
    translatorModeMenuItem.addActionListener(controller);
    menu.add(translatorModeMenuItem);

    menuBar.add(menu);

    menu = new JMenu("Help");
    menu.setMnemonic(KeyEvent.VK_H);

    menuItem = new JMenuItem("Manual");
    menuItem.setMnemonic(KeyEvent.VK_M);
    menuItem.setActionCommand("http://goofans.com/gootool/about");
    menuItem.addActionListener(hyperlinkListener);
    menu.add(menuItem);

    menuItem = new JMenuItem("FAQ");
    menuItem.setMnemonic(KeyEvent.VK_F);
    menuItem.setActionCommand("http://goofans.com/gootool/faq");
    menuItem.addActionListener(hyperlinkListener);
    menu.add(menuItem);

    menuItem = new JMenuItem("Troubleshooting");
    menuItem.setMnemonic(KeyEvent.VK_T);
    menuItem.setActionCommand("http://goofans.com/gootool/troubleshooting");
    menuItem.addActionListener(hyperlinkListener);
    menu.add(menuItem);

    menuItem = new JMenuItem("Forum");
    menuItem.setMnemonic(KeyEvent.VK_O);
    menuItem.setActionCommand("http://goofans.com/forum");
    menuItem.addActionListener(hyperlinkListener);
    menu.add(menuItem);

    menu.add(new JSeparator());

    menuItem = new JMenuItem("Check for Updates");
    menuItem.setMnemonic(KeyEvent.VK_U);
    menuItem.setActionCommand(Controller.CMD_CHECK_FOR_UPDATES);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    menuItem = new JMenuItem("About GooTool");
    menuItem.setMnemonic(KeyEvent.VK_A);
    menuItem.setActionCommand(Controller.CMD_ABOUT);
    menuItem.addActionListener(controller);
    menu.add(menuItem);

    menuBar.add(menu);
  }

  public JMenuBar getJMenuBar()
  {
    return menuBar;
  }
}
