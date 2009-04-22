package com.goofans.gootool.util;

import net.infotrek.util.DesktopUtil;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class HyperlinkLaunchingListener implements HyperlinkListener, ActionListener
{
  private Component parentComponent; // for the parent of the error joptionpane

  public HyperlinkLaunchingListener(Component parentComponent)
  {
    this.parentComponent = parentComponent;
  }

  public void hyperlinkUpdate(HyperlinkEvent event)
  {
    if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      DesktopUtil.browseAndWarn(event.getURL(), parentComponent);
    }
  }

  public void actionPerformed(ActionEvent e)
  {
    DesktopUtil.browseAndWarn(e.getActionCommand(), parentComponent);
  }
}
