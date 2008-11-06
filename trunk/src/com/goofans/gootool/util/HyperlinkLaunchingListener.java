package com.goofans.gootool.util;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class HyperlinkLaunchingListener implements HyperlinkListener
{
  private static final Logger log = Logger.getLogger(HyperlinkLaunchingListener.class.getName());

  public void hyperlinkUpdate(HyperlinkEvent event)
  {
    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        Runtime.getRuntime().exec(event.getURL().toString());
      }
      catch (IOException e) {
        log.log(Level.WARNING, "Couldn't launch hyperlink " + event.getURL(), e);
      }
    }
  }
}
