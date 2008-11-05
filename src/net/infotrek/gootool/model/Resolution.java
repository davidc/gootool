package net.infotrek.gootool.model;

import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;
import java.util.logging.Logger;
import java.awt.*;

/**
 * @author David Croft (david.croft@infotrek.net)
 * @version $Id$
 */
public class Resolution implements Comparable
{
  private static final Logger log = Logger.getLogger(Resolution.class.getName());

  private int width;
  private int height;

  public Resolution(int width, int height)
  {
    this.width = width;
    this.height = height;
  }

  public int getWidth()
  {
    return width;
  }

  public int getHeight()
  {
    return height;
  }

  public boolean isWidescreen()
  {
    return (height * 4) / 3 != width;
  }

  public String getAspectRatio()
  {
    int gcd = GCD(width, height);

    return (width / gcd) + ":" + (height / gcd);
  }

  private int GCD(int a, int b)
  {
    if (b == 0) return a;
    return GCD(b, a % b);
  }


  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(width);
    sb.append("x");
    sb.append(height);

    // figure out aspect ratio

    sb.append(" (").append(getAspectRatio()).append(")");
    return sb.toString();
  }

  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Resolution that = (Resolution) o;

    if (height != that.height) return false;
    if (width != that.width) return false;

    return true;
  }

  public int hashCode()
  {
    int result;
    result = width;
    result = 31 * result + height;
    return result;
  }

  public int compareTo(Object o)
  {
    Resolution that = (Resolution) o;
    if (this.getWidth() < that.getWidth())
      return -1;
    else if (this.getWidth() > that.getWidth())
      return 1;
    else
      return 0;
  }


  private static final Set<Resolution> RESOLUTIONS;

  static {
    DisplayMode[] displayModes = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayModes();

    Set<Resolution> resolutions = new TreeSet<Resolution>();

    for (DisplayMode displayMode : displayModes) {
      resolutions.add(new Resolution(displayMode.getWidth(), displayMode.getHeight()));
    }
    RESOLUTIONS = Collections.unmodifiableSet(resolutions);

    log.finer("System resolutions " + RESOLUTIONS);
  }

  public static Set<Resolution> getSystemResolutions()
  {
    return RESOLUTIONS;
  }

  public static Resolution getResolutionByDimensions(int w, int h)
  {
    for (Resolution resolution : RESOLUTIONS) {
      if (resolution.getWidth() == w && resolution.getHeight() == h) {
        return resolution;
      }
    }
    return null;
  }
}
