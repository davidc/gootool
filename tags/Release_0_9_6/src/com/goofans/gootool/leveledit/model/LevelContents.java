package com.goofans.gootool.leveledit.model;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LevelContents
{

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
    rootElement.getElementsByTagName("BallInstance");

  }
}
