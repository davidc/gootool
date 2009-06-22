package com.goofans.gootool.leveledit.resource;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import com.goofans.gootool.leveledit.view.BallPaletteBall;

/**
 * DND transferable object for balls.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BallTransferable implements Transferable
{
  public static final DataFlavor FLAVOR = new DataFlavor(Ball.class, null);

  private Ball ball;

  public BallTransferable(Ball ball)
  {
    this.ball = ball;
  }

  public Ball getBall()
  {
    return ball;
  }

  public DataFlavor[] getTransferDataFlavors()
  {
    return new DataFlavor[]{FLAVOR};
  }

  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    return (flavor.equals(FLAVOR));
  }

  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
  {
    if (flavor == FLAVOR) {
      return ball;
    }
    else {
      throw new UnsupportedFlavorException(flavor);
    }
  }
}
