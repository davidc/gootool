package com.goofans.gootoolsp.leveledit.view;

import javax.swing.*;

import com.goofans.gootool.TextProvider;
import com.goofans.gootool.GooTool;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LevelEditorMenuBar extends JMenuBar
{
  private TextProvider textProvider;
  JCheckBoxMenuItem snapGridItem;
  JCheckBoxMenuItem showGridItem;

  public LevelEditorMenuBar(LevelEditor editor)
  {
    JMenu menu;
    JMenuItem menuItem;

    textProvider = GooTool.getTextProvider();

    menu = new JMenu(getText("view"));
    menu.setMnemonic(getMnemonic("view"));
    add(menu);

    showGridItem = new JCheckBoxMenuItem(getText("view.showGrid"));
    showGridItem.setMnemonic(getMnemonic("view.showGrid"));
    showGridItem.setActionCommand(LevelEditor.CMD_SHOW_GRID);
    showGridItem.addActionListener(editor);
    showGridItem.setAccelerator(getAccelerator("view.showGrid"));
    menu.add(showGridItem);

    snapGridItem = new JCheckBoxMenuItem(getText("view.snapGrid"));
    snapGridItem.setMnemonic(getMnemonic("view.snapGrid"));
    snapGridItem.setActionCommand(LevelEditor.CMD_SNAP_GRID);
    snapGridItem.addActionListener(editor);
    snapGridItem.setAccelerator(getAccelerator("view.snapGrid"));
    menu.add(snapGridItem);
  }

  private String getText(String key)
  {
    return textProvider.getText("leveledit.menu." + key);
  }

  private int getMnemonic(String key)
  {
    String shortcutText = textProvider.getText("leveledit.menu." + key + ".shortcut");
    KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcutText);
    if (keyStroke == null) throw new RuntimeException("Invalid shortcut " + shortcutText + " for " + key);
    return keyStroke.getKeyCode();
  }

  private KeyStroke getAccelerator(String key)
  {
    String accelText = textProvider.getOptionalText("leveledit.menu." + key + ".accelerator");
    if (accelText != null && accelText.length() > 0) {
      KeyStroke keyStroke = KeyStroke.getKeyStroke(accelText);
      if (keyStroke == null) throw new RuntimeException("Invalid accelerator " + accelText + " for " + key);
      return keyStroke;
    }

    return null;
  }
}