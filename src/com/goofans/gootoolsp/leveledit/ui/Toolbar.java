/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.ui;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

import com.goofans.gootoolsp.leveledit.tools.Tool;
import com.goofans.gootoolsp.leveledit.tools.SelectTool;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.DebugUtil;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Toolbar extends JToolBar implements ActionListener
{
//  private int orientation;

  //  private Map<String, Tool> tools;
  private final Map<Tool, ToolbarButton> toolButtons;
//  private List<Object> buttons;

  private Tool currentTool;

//  private int maxIconWidth, maxIconHeight;
//  private GridBagConstraints constraints;

  private final Icon buttonBackgroundSelected;

  /**
   * Constructor
   *
   * @param orientation SwingConstants.HORIZONTAL or SwingConstants.VERTICAL
   */
  public Toolbar(int orientation) throws IOException
  {
    super(orientation);

//    tools = new HashMap<String, Tool>();
    toolButtons = new HashMap<Tool, ToolbarButton>();
//    buttons = new LinkedList<Object>();
//    this.orientation = orientation;

    setBackground(new Color(214, 214, 214));
//    setOpaque(true);
//    setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    setMargin(new Insets(4, 4, 4, 4));

//    GridBagLayout layout = new GridBagLayout();
//    setLayout(layout);

//    constraints = new GridBagConstraints();
//    if (orientation == SwingConstants.VERTICAL) {
//      constraints.gridx = 0;
//      constraints.gridy = GridBagConstraints.RELATIVE;
//      constraints.fill = GridBagConstraints.HORIZONTAL;
//    }
//    else {
//      constraints.gridx = GridBagConstraints.RELATIVE;
//      constraints.gridy = 0;
//      constraints.fill = GridBagConstraints.VERTICAL;
//    }

    buttonBackgroundSelected = new ImageIcon(ImageIO.read(Toolbar.class.getResourceAsStream("/gootoolsp/leveledit/toolbar/bgselected.png")));
  }


  public void addTool(Tool tool, String tooltip, Icon icon)
  {
    if (toolButtons.containsKey(tool)) {
      throw new IllegalArgumentException("A tool being added already exists");
    }

    ToolbarButton button = new ToolbarButton(icon, buttonBackgroundSelected);
    button.setToolTipText(tooltip);

    button.addActionListener(this);
    button.setActionCommand(String.valueOf(System.identityHashCode(tool)));

//    constraints.insets = new Insets(0, 0, 0, 0);
//    add(button, constraints);
    add(button);

//    tools.put(cmd, tool);
    toolButtons.put(tool, button);
  }

  public void xaddSeparator()
  {
//    constraints.insets = new Insets(2, 2, 2, 2);
    JSeparator sep = new JSeparator();
    add(sep);//, constraints);
//    if (this.orientation == SwingConstants.HORIZONTAL) {
//      add(new JSeparator(SwingConstants.VERTICAL));//, constraints);
//    }
//    else {
//      add(new JSeparator(SwingConstants.HORIZONTAL));//, constraints);
//    }
  }

//  @Override
//  public Dimension getPreferredSize()
//  {
//    int width = getInsets().left + getInsets().right;
//
//
//    return super.getPreferredSize();    //To change body of overridden methods use File | Settings | File Templates.
//  }
//
//  @Override
//  public Dimension getMinimumSize()
//  {
//    return getPreferredSize();

  //  }


  public Tool getCurrentTool()
  {
    return currentTool;
  }

  public void setCurrentTool(Tool currentTool)
  {
    this.currentTool = currentTool;
    updateCurrentTool();
  }

  public void actionPerformed(ActionEvent event)
  {
    int toolId;
    try {
      toolId = Integer.valueOf(event.getActionCommand());
    }
    catch (NumberFormatException e) {
      return;
    }

    for (Tool tool : toolButtons.keySet()) {
      if (System.identityHashCode(tool) == toolId) {
        currentTool = tool;
      }
    }

    updateCurrentTool();
  }

  private void updateCurrentTool()
  {
    ToolbarButton currentButton = toolButtons.get(currentTool);
    for (ToolbarButton otherButton : toolButtons.values()) {
      otherButton.setSelected(currentButton == otherButton);
    }
  }


  public static void main(String[] args) throws IOException
  {
    GUIUtil.switchToSystemLookAndFeel();

    Toolbar bar = new Toolbar(SwingConstants.VERTICAL);

    quickAddTool(bar, "pan");
    bar.addSeparator();
    quickAddTool(bar, "select");
    quickAddTool(bar, "selectmulti");
    quickAddTool(bar, "move");
    quickAddTool(bar, "rotate");
    bar.addSeparator();
    quickAddTool(bar, "ball");
    quickAddTool(bar, "strand");
    quickAddTool(bar, "geomcircle");
    quickAddTool(bar, "geomrectangle");
    quickAddTool(bar, "pipe");
    bar.addSeparator();
    quickAddTool(bar, "camera");
//    quickAddTool(bar, "zoomin");
//    quickAddTool(bar, "zoomout");

    JPanel panel = new JPanel();
    panel.add(bar);
    DebugUtil.showPanelWindow(panel);

    bar = new Toolbar(SwingConstants.HORIZONTAL);
    quickAddTool(bar, "levelnew");
    quickAddTool(bar, "levelopen");
    quickAddTool(bar, "levelclone");
    quickAddTool(bar, "levelsave");
    quickAddTool(bar, "levellaunch");
    bar.addSeparator();
    quickAddTool(bar, "undo");
    quickAddTool(bar, "redo");
    bar.addSeparator();
    quickAddTool(bar, "zoomout");
    quickAddTool(bar, "zoomin");

    panel = new JPanel();
    panel.add(bar);
    DebugUtil.showPanelWindow(panel);
  }

  private static void quickAddTool(Toolbar bar, String toolName) throws IOException
  {
    bar.addTool(new SelectTool(), toolName, new ImageIcon(ImageIO.read(Toolbar.class.getResourceAsStream("/gootoolsp/leveledit/toolbar/" + toolName + ".png"))));
  }
}
