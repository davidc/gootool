package com.goofans.gootoolsp.leveledit.view;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public enum LevelDisplayLayer
{
  IMAGES("Images", true),
  BALLS("Balls", true),
  STRANDS("Strands", true),
  GEOMETRY("Geometry", true),
  BOUNDARIES("Boundaries", true),
  HINGES("Hinges", true),
  VIEWPORT("Viewport", true);

  private final String displayName;
  private final boolean defaultVisible;

  LevelDisplayLayer(String displayName, boolean defaultVisible)
  {
    this.displayName = displayName;
    this.defaultVisible = defaultVisible;
  }

  public boolean isDefaultVisible()
  {
    return defaultVisible;
  }

  @Override
  public String toString()
  {
    return displayName;
  }
}
