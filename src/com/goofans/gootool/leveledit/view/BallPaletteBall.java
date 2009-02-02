package com.goofans.gootool.leveledit.view;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;

import com.goofans.gootool.leveledit.resource.Ball;
import com.goofans.gootool.leveledit.resource.BallTransferable;

/**
 * Thumbnail of an item in a palette.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BallPaletteBall extends JLabel
{
  private Ball ball;
  private int currentIconSize;
  private boolean selected;

  public BallPaletteBall(String text, Ball ball)
  {
    super(text, SwingConstants.CENTER);
    this.ball = ball;
//    setBorder(BorderFactory.createEmptyBorder());
    setVerticalTextPosition(SwingConstants.BOTTOM);
    setHorizontalTextPosition(SwingConstants.CENTER);
    setFocusable(true);

    setTransferHandler(new BallPaletteBallTransferHandler());

    addMouseMotionListener(new MouseMotionAdapter()
    {
      @Override
      public void mouseDragged(MouseEvent e)
      {
        e.consume();
        getTransferHandler().exportAsDrag((JComponent) e.getSource(), e, TransferHandler.COPY);
      }
    });
  }

  @Override
  protected void paintComponent(Graphics g)
  {
    updateIcon();
    if (isSelected()) {
//      System.out.println("UIManager.getColor(\"Control.focus\") = " + UIManager.getColor("Control.focus"));
//      setBackground(UIManager.getColor("Control.focus"));
//      setBackground(Color.BLUE);
//      setOpaque(true);
//      setForeground(Color.GREEN);
    }
    else {
    }
    super.paintComponent(g);    //To change body of overridden methods use File | Settings | File Templates.
  }

  @Override
  public Dimension getMinimumSize()
  {
    return getPreferredSize();
  }

  @Override
  public Dimension getPreferredSize()
  {
    updateIcon();
    return super.getPreferredSize();
  }

  private void updateIcon()
  {
    BallPalette palette = (BallPalette) getParent();
    int newSize = palette.getThumbnailSize();
    if (newSize != currentIconSize) {
      // recreate icon
      setIcon(new ImageIcon(ball.getImageInState("walking", new Dimension(newSize, newSize))));
      currentIconSize = newSize;
    }
  }

  @Override
  public Dimension getMaximumSize()
  {
    updateIcon();
    return super.getMaximumSize();
  }

  public boolean isSelected()
  {
    return selected;
  }

  public void setSelected(boolean selected)
  {
    if (selected != this.selected) {
      this.selected = selected;
      repaint();

      if (selected) {
        setBackground(Color.BLUE);
        setOpaque(true);
      }
      else {
        setOpaque(false);

      }
    }
  }

  private static class BallPaletteBallTransferHandler extends TransferHandler
  {
    @Override
    public int getSourceActions(JComponent c)
    {
      return COPY;
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
    {
      return false;
    }

    @Override
    protected Transferable createTransferable(JComponent c)
    {
      Ball ball = ((BallPaletteBall) c).ball;
      return new BallTransferable(ball);
    }
  }
}