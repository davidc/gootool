/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Thumbnail of an item in a palette.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class PaletteThumbnail extends JLabel
{

  public PaletteThumbnail(String text, Icon icon)
  {
    super(text, icon, SwingConstants.CENTER);
//    setBorder(BorderFactory.createEmptyBorder());
    setVerticalTextPosition(SwingConstants.BOTTOM);
    setHorizontalTextPosition(SwingConstants.CENTER);
    setFocusable(true);
  }

  @Override
  protected void paintComponent(Graphics g)
  {
    if (true) {
//      System.out.println("UIManager.getColor(\"Control.focus\") = " + UIManager.getColor("ToggleButton.focus"));
//      setBackground(UIManager.getColor("ToggleButton.focus"));
      setOpaque(true);
//      setForeground(Color.GREEN);
    }
    else {
      setOpaque(false);

    }
    super.paintComponent(g);    //To change body of overridden methods use File | Settings | File Templates.
  }

  @Override
  public Dimension getMinimumSize()
  {
    return getPreferredSize();
  }
}
