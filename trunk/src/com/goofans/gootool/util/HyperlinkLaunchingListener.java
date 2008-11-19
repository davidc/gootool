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
      URLLauncher.launchAndWarn(event.getURL(), null); // TODO not null
    }
  }
}
