package com.goofans.gootool.util;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * FIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIX
 * FIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIX
 * FIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIX
 * FIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIX
 * FIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIX
 * FIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIX
 * FIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIXFIX FIX
 *
 * @author David Croft (david.croft@infotrek.net)
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class DebugUtil
{
  public static void setAllLogging()
  {
    Logger.getLogger("").setLevel(Level.ALL);
    Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
  }
}
