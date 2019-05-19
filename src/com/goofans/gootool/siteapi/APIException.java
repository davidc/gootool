/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.siteapi;

/**
 * An exception returned by the REST API server. These exceptions are normally generated remotely not locally,
 * except in cases where we can catch an input error early before even having to ask the server.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class APIException extends Exception
{
  public APIException(String message)
  {
    super(message);
  }

  public APIException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
