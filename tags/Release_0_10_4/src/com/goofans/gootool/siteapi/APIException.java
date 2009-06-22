package com.goofans.gootool.siteapi;

/**
 * For API exceptions.
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
