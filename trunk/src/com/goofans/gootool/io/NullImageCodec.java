/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.io;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class NullImageCodec implements ImageCodec
{
  NullImageCodec()
  {
  }

  public BufferedImage readImage(InputStream is) throws IOException
  {
    try {
      return ImageIO.read(is);
    }
    finally {
      is.close();
    }
  }

  public void writeImage(BufferedImage image, OutputStream os) throws IOException
  {
    try {
      ImageIO.write(image, GameFormat.PNG_FORMAT, os);
    }
    finally {
      os.close();
    }
  }
}
