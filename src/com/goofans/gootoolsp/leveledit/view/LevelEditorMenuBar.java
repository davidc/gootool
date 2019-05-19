/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.view;

import javax.swing.*;

import java.util.ResourceBundle;
import java.util.MissingResourceException;

import com.goofans.gootool.GooTool;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LevelEditorMenuBar extends JMenuBar
{
  private final ResourceBundle resourceBundle;
  JCheckBoxMenuItem snapGridItem;
  JCheckBoxMenuItem showGridItem;

  public LevelEditorMenuBar(LevelEditor editor)
  {
    JMenu menu;
    JMenuItem menuItem;

    resourceBundle = GooTool.getTextProvider();

    menu = new JMenu(getMenuText("view"));
    menu.setMnemonic(getMnemonic("view"));
    add(menu);

    showGridItem = new JCheckBoxMenuItem();
    prepareMenuItem(showGridItem, "view.showGrid", LevelEditor.CMD_SHOW_GRID, editor);
    menu.add(showGridItem);

    snapGridItem = new JCheckBoxMenuItem();
    prepareMenuItem(snapGridItem, "view.snapGrid", LevelEditor.CMD_SNAP_GRID, editor);
    menu.add(snapGridItem);
  }

  private void prepareMenuItem(JMenuItem menuItem, String key, String command, LevelEditor editor)
  {
    menuItem.setText(getMenuText(key));
    menuItem.setMnemonic(getMnemonic(key));
    menuItem.setActionCommand(command);
    menuItem.addActionListener(editor);
    menuItem.setAccelerator(getAccelerator(key));
  }

  private String getMenuText(String key)
  {
    return resourceBundle.getString("leveledit.menu." + key);
  }

  private int getMnemonic(String key)
  {
    String mnemonicText = resourceBundle.getString("leveledit.menu." + key + ".mnemonic");
    KeyStroke keyStroke = KeyStroke.getKeyStroke(mnemonicText);
    if (keyStroke == null) throw new RuntimeException("Invalid mnemonic " + mnemonicText + " for " + key);
    return keyStroke.getKeyCode();
  }

  private KeyStroke getAccelerator(String key)
  {
    try {
      String accelText = resourceBundle.getString("leveledit.menu." + key + ".accelerator");
      KeyStroke keyStroke = KeyStroke.getKeyStroke(accelText);
      if (keyStroke == null) throw new RuntimeException("Invalid accelerator " + accelText + " for " + key);
      return keyStroke;
    }
    catch (MissingResourceException e) {
      return null;
    }
  }
}