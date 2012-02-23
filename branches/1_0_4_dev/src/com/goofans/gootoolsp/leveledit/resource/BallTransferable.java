/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.resource;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * DND transferable object for balls.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BallTransferable implements Transferable
{
  public static final DataFlavor FLAVOR = new DataFlavor(Ball.class, null);

  private final Ball ball;

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
