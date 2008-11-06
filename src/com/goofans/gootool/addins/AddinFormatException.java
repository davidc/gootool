package com.goofans.gootool.addins;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AddinFormatException extends Exception
{
  public AddinFormatException()
  {
  }

  public AddinFormatException(String message)
  {
    super(message);
  }

  public AddinFormatException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public AddinFormatException(Throwable cause)
  {
    super(cause);
  }
}
