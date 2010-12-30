/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.io;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Superclass for all game image codecs.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface ImageCodec
{
  BufferedImage readImage(InputStream is) throws IOException;

  void writeImage(BufferedImage image, OutputStream os) throws IOException;
}
