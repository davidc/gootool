package com.goofans.gootool.util;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class HyperlinkLaunchingListener implements HyperlinkListener, ActionListener
{
//  private static final Logger log = Logger.getLogger(HyperlinkLaunchingListener.class.getName());

  public void hyperlinkUpdate(HyperlinkEvent event)
  {
    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      URLLauncher.launchAndWarn(event.getURL(), null); // TODO not null
    }
  }

  public void actionPerformed(ActionEvent e)
  {
    URLLauncher.launchAndWarn(e.getActionCommand(), null);
  }
}
