package com.goofans.gootool.leveledit.ui;

import java.awt.*;

/**
 * This layout manager arranges its components in a grid. The width and height of every cell is the same, and is based on the largest
 * of the preferred sizes of all components. The number of columns then depends on the available width in the parent.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class WrappingGridLayout extends GridLayout
{

  int hgap;
  int vgap;

  public WrappingGridLayout(int hgap, int vgap)
  {
    this.hgap = hgap;
    this.vgap = vgap;
  }

  private static final int PREFERRED_COLUMNS = 5;

  public Dimension preferredLayoutSize(Container parent)
  {
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      Dimension cellSize = getCellSize(parent);

      int ncomponents = parent.getComponentCount();
      int rows = ncomponents / PREFERRED_COLUMNS;
      if (ncomponents % PREFERRED_COLUMNS > 0) rows++;
      return new Dimension(insets.left + insets.right + PREFERRED_COLUMNS * cellSize.width + (PREFERRED_COLUMNS - 1) * hgap,
              insets.top + insets.bottom + rows * cellSize.height + (rows - 1) * vgap);
    }
  }

  public Dimension preferredLayoutSizeForWidth(Container parent, int width)
  {
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      Dimension cellSize = getCellSize(parent);

      int ncols = width / (cellSize.width + hgap);
      int ncomponents = parent.getComponentCount();
      if (ncols < 1) ncols = 1;
      int nrows = ncomponents / ncols;
      if (ncomponents % ncols > 0) nrows++;
      return new Dimension(insets.left + insets.right + ncols * cellSize.width + (ncols - 1) * hgap,
              insets.top + insets.bottom + nrows * cellSize.height + (nrows - 1) * vgap);
    }
  }

  private Dimension getCellSize(Container parent)
  {
    int ncomponents = parent.getComponentCount();

    Dimension cellSize = new Dimension(0, 0);
    for (int i = 0; i < ncomponents; i++) {
      Component comp = parent.getComponent(i);
      Dimension d = comp.getPreferredSize();
      if (cellSize.width < d.width) {
        cellSize.width = d.width;
      }
      if (cellSize.height < d.height) {
        cellSize.height = d.height;
      }
    }
    return cellSize;
  }

  public Dimension minimumLayoutSize(Container parent)
  {
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      Dimension cellSize = getCellSize(parent);
      int parentWidth = parent.getSize().width;
      int columns = Math.max(parentWidth / cellSize.width, 1);
      int ncomponents = parent.getComponentCount();
      int rows = ncomponents / columns;
      if (ncomponents % columns > 0) rows++;

      return new Dimension(insets.left + insets.right + columns * cellSize.width + (columns - 1) * hgap,
              insets.top + insets.bottom + rows * cellSize.height + (rows - 1) * vgap);
    }
  }

  public void layoutContainer(Container parent)
  {
    synchronized (parent.getTreeLock()) {
      int ncomponents = parent.getComponentCount();
      if (ncomponents == 0) {
        return;
      }

      Dimension cellSize = getCellSize(parent);
      int parentWidth = parent.getSize().width;
      int ncols = parentWidth / cellSize.width;
      if (ncols < 1) ncols = 1;

      int nrows = ncomponents / ncols;
      if (ncomponents % ncols > 0) nrows++;
      setColumns(ncols);
      setRows(nrows);
      Insets insets = parent.getInsets();

      int w = parent.getWidth() - (insets.left + insets.right);
      int h = parent.getHeight() - (insets.top + insets.bottom);
      w = (w - (ncols - 1) * hgap) / ncols;
      h = (h - (nrows - 1) * vgap) / nrows;

      if (w < cellSize.width) {
        w = cellSize.width;
      }
//      if (h < cellSize.height) {
      h = cellSize.height;
//      }

      for (int c = 0, x = insets.left; c < ncols; c++, x += w + hgap) {
        for (int r = 0, y = insets.top; r < nrows; r++, y += h + vgap) {
          int i = r * ncols + c;
          if (i < ncomponents) {
            parent.getComponent(i).setBounds(x, y, w, h);
          }
        }
      }
    }
  }
}