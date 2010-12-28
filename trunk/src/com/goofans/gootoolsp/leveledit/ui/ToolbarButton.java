/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ToolbarButton extends JLabel
{
  private final Icon backgroundSelected;
  private boolean mouseOver;
  private boolean isSelected;

  private String actionCommand;

  public ToolbarButton(Icon image, Icon backgroundSelected)
  {
    super(image);
//    setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
//    setOpaque(true);
//    setBackground(SELECTED_COLOUR);
    this.backgroundSelected = backgroundSelected;

    addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseEntered(MouseEvent e)
      {
        mouseOver = true;
        repaint();
      }

      @Override
      public void mouseExited(MouseEvent e)
      {
        mouseOver = false;
        repaint();
      }

      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (isEnabled()) {
          fireActionPerformed(e);
        }
      }
    });
  }

  @Override
  public Dimension getMinimumSize()
  {
    Icon image = getIcon();
    Insets insets = getInsets();

    int width = Math.max(image.getIconWidth(), backgroundSelected.getIconWidth());
    int height = Math.max(image.getIconHeight(), backgroundSelected.getIconHeight());
    return new Dimension(width + insets.left + insets.right, height + insets.top + insets.bottom);
  }

  @Override
  public Dimension getPreferredSize()
  {
    return getMinimumSize();
  }

  @Override
  public Dimension getMaximumSize()
  {
    return getMinimumSize();
  }

  @Override
  protected void paintComponent(Graphics g)
  {
    int x = (getWidth() - backgroundSelected.getIconWidth()) / 2;
    int y = (getHeight() - backgroundSelected.getIconHeight()) / 2;

    if (mouseOver) {
      backgroundSelected.paintIcon(this, g, x, y);
    }
    if (isSelected || mouseOver) {
      backgroundSelected.paintIcon(this, g, x, y);
    }

    super.paintComponent(g);    //To change body of overridden methods use File | Settings | File Templates.
  }


  /**
   * Adds an <code>ActionListener</code> to the button.
   *
   * @param l the <code>ActionListener</code> to be added
   */
  public void addActionListener(ActionListener l)
  {
    listenerList.add(ActionListener.class, l);
  }

  public void setActionCommand(String actionCommand)
  {
    this.actionCommand = actionCommand;
  }

  protected void fireActionPerformed(InputEvent underlyingEvent)
  {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    ActionEvent e = null;
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == ActionListener.class) {
        // Lazily create the event:
        if (e == null) {
          e = new ActionEvent(this,
                  ActionEvent.ACTION_PERFORMED,
                  actionCommand,
                  underlyingEvent.getWhen(),
                  underlyingEvent.getModifiers());
        }
        ((ActionListener) listeners[i + 1]).actionPerformed(e);
      }
    }
  }

  public boolean isSelected()
  {
    return isSelected;
  }

  public void setSelected(boolean selected)
  {
    if (selected != isSelected) {
      isSelected = selected;
      repaint();
    }
  }
}
