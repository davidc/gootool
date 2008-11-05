package net.infotrek.gootool.addins;

/**
 * @author David Croft (david.croft@infotrek.net)
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
