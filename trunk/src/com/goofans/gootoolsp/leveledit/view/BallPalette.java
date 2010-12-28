/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.goofans.gootool.facades.TargetFile;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootoolsp.leveledit.resource.Ball;
import com.goofans.gootoolsp.leveledit.ui.WrappingGridLayout;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BallPalette extends JComponent implements Scrollable
{
  private int thumbnailSize = 50;
  List<BallPaletteBall> paletteEntries = new ArrayList<BallPaletteBall>();

  public BallPalette()
  {
    final WrappingGridLayout layout = new WrappingGridLayout(5, 5);
    setLayout(layout);

    addHierarchyBoundsListener(new HierarchyBoundsAdapter()
    {
      @Override
      public void ancestorResized(HierarchyEvent e)
      {
        if (e.getChanged() == getParent()) {
          // Our preferred width is always our parent's width
          // Our preferred height then depends on our layout.

          int parentWidth = getParent().getSize().width;
          Dimension preferredSize = layout.preferredLayoutSizeForWidth(BallPalette.this, parentWidth);
          setPreferredSize(preferredSize);
        }
      }
    });
  }

  public void addBalls() throws IOException
  {
    TargetFile ballsDir = ProjectManager.simpleInit().getTarget().getGameRoot().getChild("res/balls"); //NON-NLS

    List<TargetFile> ballsDirs = ballsDir.list();

    for (TargetFile dir : ballsDirs) {
      if (dir.isDirectory() && !dir.getName().startsWith("_")) {
        Ball ball = new Ball(dir.getName());
        final BallPaletteBall button = new BallPaletteBall(dir.getName(), ball);
        button.setToolTipText(dir.getName());
        add(button);

        paletteEntries.add(button);

        button.addMouseListener(new MouseAdapter()
        {
          @Override
          public void mousePressed(MouseEvent e)
          {
            for (BallPaletteBall paletteEntry : paletteEntries) {
              paletteEntry.setSelected(paletteEntry.equals(button));
            }
            notifySelectionListeners(button);
          }
        });

      }
//      if (++i > 20) return;
    }
  }

  public static void main(String[] args) throws IOException
  {
    GUIUtil.switchToSystemLookAndFeel();

    JFrame frame = new JFrame("Ball Palette"); //NON-NLS
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    BallPalette palette = new BallPalette();
    palette.addBalls();
    frame.add(palette);
    frame.pack();
    frame.setVisible(true);
  }

  public Dimension getPreferredScrollableViewportSize()
  {
    return new Dimension(500, 100);
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
  {
    return 10;
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
  {
    return 100;
  }

  public boolean getScrollableTracksViewportWidth()
  {
    return false;
  }

  public boolean getScrollableTracksViewportHeight()
  {
    return false;
  }

  public void setThumbnailSize(int value)
  {
    if (value != thumbnailSize) {
      thumbnailSize = value;

      // TODO need to revalidate the scrollbars as well
      invalidate();
      repaint();
    }
  }

  public int getThumbnailSize()
  {
    return thumbnailSize;
  }


  private final List<SelectionListener> selectionListeners = new ArrayList<SelectionListener>();

  public void addSelectionListener(SelectionListener l)
  {
    selectionListeners.add(l);
  }

  private void notifySelectionListeners(BallPaletteBall ball)
  {
    for (SelectionListener l : selectionListeners) {
      l.ballSelected(ball);
    }
  }

  public static interface SelectionListener
  {
    public void ballSelected(BallPaletteBall ball);
  }
}
