package net.infotrek.gootool.util;

import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.io.IOException;

/**
 * @author David Croft (david.croft@infotrek.net)
* @version $Id$
*/
public class HyperlinkLaunchingListener implements HyperlinkListener
{
  public void hyperlinkUpdate(HyperlinkEvent e)
  {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      try {
        Runtime.getRuntime().exec(e.getURL().toString());
      }
      catch (IOException e1) {
        e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
    }
  }
}
