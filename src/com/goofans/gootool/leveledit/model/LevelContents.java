package com.goofans.gootool.leveledit.model;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.LinkedList;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LevelContents
{
  private static final Map<String, Class<? extends LevelContentsItem>> LEVEL_CONTENT_TYPES = new TreeMap<String, Class<? extends LevelContentsItem>>();

  static {
    LEVEL_CONTENT_TYPES.put("ballinstance", BallInstance.class);
    LEVEL_CONTENT_TYPES.put("strand", Strand.class);
  }

  private List<LevelContentsItem> items = new LinkedList<LevelContentsItem>();

  public LevelContents(Document d) throws IOException
  {
    Element rootElement = d.getDocumentElement();

    if (!rootElement.getNodeName().equals("level")) {
      throw new IOException("Root element isn't a level");
    }

    // TODo ballsrequired="15" letterboxed="false" visualdebug="false" autobounds="false" textcolor="255,255,255" texteffects="true" timebugprobability="0.333" strandgeom="true" allowskip="true" >

    /*
     Node "level", attribute "allowskip" is optional, occurrences with/without attribute: 55/49
     Node "level", attribute "autobounds" is optional, occurrences with/without attribute: 57/47
     Node "level", attribute "ballsrequired" is optional, occurrences with/without attribute: 57/47
     Node "level", attribute "cursor1color" is optional, occurrences with/without attribute: 7/97
     Node "level", attribute "cursor2color" is optional, occurrences with/without attribute: 7/97
     Node "level", attribute "cursor3color" is optional, occurrences with/without attribute: 7/97
     Node "level", attribute "cursor4color" is optional, occurrences with/without attribute: 7/97
     Node "level", attribute "cutscene" is optional, occurrences with/without attribute: 10/94
     Node "level", attribute "depends" is optional, occurrences with/without attribute: 46/58
     Node "level", attribute "id" is optional, occurrences with/without attribute: 47/57
     Node "level", attribute "letterboxed" is optional, occurrences with/without attribute: 57/47
     Node "level", attribute "name" is optional, occurrences with/without attribute:47/57
     Node "level", attribute "ocd" is optional, occurrences with/without attribute: 46/58
     Node "level", attribute "oncomplete" is optional, occurrences with/without attribute: 3/101
     Node "level", attribute "skipeolsequence" is optional, occurrences with/withoutattribute: 1/103
     Node "level", attribute "strandgeom" is optional, occurrences with/without attribute: 56/48
     Node "level", attribute "text" is optional, occurrences with/without attribute:47/57
     Node "level", attribute "textcolor" is optional, occurrences with/without attribute: 57/47
     Node "level", attribute "texteffects" is optional, occurrences with/without attribute: 49/55
     Node "level", attribute "timebugprobability" is optional, occurrences with/without attribute: 57/47
     Node "level", attribute "visualdebug" is optional, occurrences with/without attribute: 57/47
     21 attributes found
     Node "level" have [0-208] occurrences of child tag "BallInstance"
     Node "level" have [0-90] occurrences of child tag "Strand"
     Node "level" have [1-2] occurrences of child tag "camera"
     Node "level" have [0-1] occurrences of child tag "endoncollision"
     Node "level" have [0-1] occurrences of child tag "endonmessage"
     Node "level" have [0-6] occurrences of child tag "fire"
     Node "level" have [0-1] occurrences of child tag "levelexit"
     Node "level" have [0-1] occurrences of child tag "loopsound"
     Node "level" have [0-1] occurrences of child tag "music"
     Node "level" have [0-1] occurrences of child tag "pipe"
     Node "level" have [0-4] occurrences of child tag "signpost"
     Node "level" have [0-1] occurrences of child tag "targetheight"
     12 child tags found for node "level"
     */


    NodeList childNodes = rootElement.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element) {
        Element el = (Element) node;
        String elName = el.getNodeName().toLowerCase();

        Class clazz = LEVEL_CONTENT_TYPES.get(elName);
        if (clazz == null) {
          System.out.println("Warning, no class found for item " + elName);
        }
        else {
          try {
            Constructor<? extends LevelContentsItem> constructor = clazz.getConstructor(Element.class);
            LevelContentsItem contentsItem = constructor.newInstance(el);
            items.add(contentsItem);
          }
          catch (NoSuchMethodException e) {
            throw new RuntimeException("Class " + clazz + " has no constructor(Element)");
          }
          catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
          }
          catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
          }
          catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
          }
        }

      }

    }
//    rootElement.getElementsByTagName("BallInstance");

  }

  public List<LevelContentsItem> getLevelContents(Class<? extends LevelContentsItem> clazz)
  {
    List<LevelContentsItem> list = new LinkedList<LevelContentsItem>();
    for (LevelContentsItem item : items) {
      if (clazz.isAssignableFrom(item.getClass())) {
        list.add(item);
      }

    }
    return list;
  }

  public BallInstance getBallById(String id)
  {
    for (LevelContentsItem item : items) {
      if (item instanceof BallInstance) {
        BallInstance ball = (BallInstance) item;
        if (ball.id.equals(id)) {
          return ball;
        }
      }
    }
    return null;
  }

  public void addItem(LevelContentsItem item)
  {
    items.add(item);
  }
}
