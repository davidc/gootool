package com.goofans.gootool.util;

import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utilities for use only in test cases (psvm etc)
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class DebugUtil
{
  public static void setAllLogging()
  {
//    Logger.getLogger("").setLevel(Level.ALL);
    Logger.getLogger("com.goofans").setLevel(Level.ALL);
    Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);
  }
}
