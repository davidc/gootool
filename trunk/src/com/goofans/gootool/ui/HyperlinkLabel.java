package com.goofans.gootool.ui;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import java.net.URL;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.*;

/**
 * Hyperlink label. The whole label becomes a hyperlink.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class HyperlinkLabel extends JLabel
{
  private URL url;

  public HyperlinkLabel(String text)
  {
    super("<html><a href=\"#\">" + text + "</a></html>");

    setCursor(new Cursor(Cursor.HAND_CURSOR));

    addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent me)
      {
        HyperlinkEvent he = new HyperlinkEvent(me.getSource(), HyperlinkEvent.EventType.ACTIVATED, url);
        fireHyperlinkUpdate(he);
      }
    });
  }

  public URL getURL()
  {
    return url;
  }

  public void setURL(URL url)
  {
    this.url = url;
  }

  /**
   * Adds a hyperlink listener for notification of any changes, for example
   * when a link is selected and entered.
   *
   * @param listener the listener
   */
  public synchronized void addHyperlinkListener(HyperlinkListener listener)
  {
    listenerList.add(HyperlinkListener.class, listener);
  }

  /**
   * Removes a hyperlink listener.
   *
   * @param listener the listener
   */
  public synchronized void removeHyperlinkListener(HyperlinkListener listener)
  {
    listenerList.remove(HyperlinkListener.class, listener);
  }

  /**
   * Returns an array of all the <code>HyperLinkListener</code>s added
   * to this JEditorPane with addHyperlinkListener().
   *
   * @return all of the <code>HyperLinkListener</code>s added or an empty
   *         array if no listeners have been added
   * @since 1.4
   */
  public synchronized HyperlinkListener[] getHyperlinkListeners()
  {
    return (HyperlinkListener[]) listenerList.getListeners(
            HyperlinkListener.class);
  }

  /**
   * Notifies all listeners that have registered interest for
   * notification on this event type.  This is normally called
   * by the currently installed <code>EditorKit</code> if a content type
   * that supports hyperlinks is currently active and there
   * was activity with a link.  The listener list is processed
   * last to first.
   *
   * @param e the event
   * @see javax.swing.event.EventListenerList
   */
  public void fireHyperlinkUpdate(HyperlinkEvent e)
  {
    // Guaranteed to return a non-null array
    Object[] listeners = listenerList.getListenerList();
    // Process the listeners last to first, notifying
    // those that are interested in this event
    for (int i = listeners.length - 2; i >= 0; i -= 2) {
      if (listeners[i] == HyperlinkListener.class) {
        ((HyperlinkListener) listeners[i + 1]).hyperlinkUpdate(e);
      }
    }
  }
}
