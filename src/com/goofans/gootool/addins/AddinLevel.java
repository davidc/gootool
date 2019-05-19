/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.addins;

import java.util.Map;

/**
 * A level provided by an addin.
 * Immutable after construction. There are setters for the purpose of easier construction in AddinFactory, but they are package-local.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AddinLevel
{
  private final String dir;
  private final Map<String, String> names;
  private final Map<String, String> subtitles;
  private final String ocd;
  private String cutscene;
  private boolean skipEolSequence = false;

  public AddinLevel(String dir, Map<String, String> names, Map<String, String> subtitles, String ocd)
  {
    this.dir = dir;
    this.names = names;
    this.subtitles = subtitles;
    this.ocd = ocd;
  }

  public String getDir()
  {
    return dir;
  }

  public Map<String, String> getNames()
  {
    return names;
  }

  public Map<String, String> getSubtitles()
  {
    return subtitles;
  }

  public String getOcd()
  {
    return ocd;
  }

  void setCutscene(String cutscene)
  {
    this.cutscene = cutscene;
  }

  public String getCutscene()
  {
    return cutscene;
  }

  void setSkipEolSequence(boolean skipEolSequence)
  {
    this.skipEolSequence = skipEolSequence;
  }

  public boolean isSkipEolSequence()
  {
    return skipEolSequence;
  }
}