/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.addins;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AddinFormatException extends Exception
{
  public AddinFormatException(String message)
  {
    super(message);
  }

  public AddinFormatException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
