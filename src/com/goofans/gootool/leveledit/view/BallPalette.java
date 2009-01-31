package com.goofans.gootool.leveledit.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyBoundsAdapter;
import java.awt.event.HierarchyEvent;
import java.io.File;
import java.io.IOException;

import com.goofans.gootool.leveledit.resource.Ball;
import com.goofans.gootool.leveledit.ui.PaletteThumbnail;
import com.goofans.gootool.leveledit.ui.WrappingGridLayout;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.wog.WorldOfGoo;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BallPalette extends JComponent implements Scrollable
{
  public BallPalette() throws IOException
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
    File ballsDir = WorldOfGoo.getTheInstance().getCustomGameFile("res/balls");

    File[] ballsDirs = ballsDir.listFiles();
    int i = 0;
    for (File dir : ballsDirs) {
      if (dir.isDirectory() && !dir.getName().startsWith("_")) {
        System.out.println("dir.getName() = " + dir.getName());
        Ball ball = new Ball(dir.getName());
        PaletteThumbnail button = new PaletteThumbnail(dir.getName(), new ImageIcon(ball.getImageInState("walking", new Dimension(50,50))));
        button.setToolTipText(dir.getName());
        add(button);
      }
//      if (++i > 20) return;
    }
  }

  public static void main(String[] args) throws IOException
  {
    GUIUtil.switchToSystemLookAndFeel();
    WorldOfGoo.getTheInstance().init();

    JFrame frame = new JFrame("Ball Palette");
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
}
