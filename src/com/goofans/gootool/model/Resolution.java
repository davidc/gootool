package com.goofans.gootool.model;

import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;
import java.util.logging.Logger;
import java.awt.*;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Resolution implements Comparable
{
  private static final Logger log = Logger.getLogger(Resolution.class.getName());

  private final int width;
  private final int height;

  private Resolution(int width, int height)
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

    int widthFactor = width / gcd;
    int heightFactor = height / gcd;

    if (widthFactor == 5 && heightFactor == 3) {
      gcd /= 3;
    }
    else if (widthFactor == 8 && heightFactor == 5) {
      gcd /= 2;
    }

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
    if (this.width < that.width)
      return -1;
    else if (this.width > that.width)
      return 1;
    else if (this.height < that.height)
      return -1;
    else if (this.height > that.height)
      return 1;
    else
      return 0;
  }


  private static final Set<Resolution> RESOLUTIONS;
  private static final Set<Integer> REFRESH_RATES;
  public static final Resolution DEFAULT_RESOLUTION;

  static {
    Set<Resolution> resolutions = new TreeSet<Resolution>();
    Set<Integer> refreshRates = new TreeSet<Integer>();

    // Make sure there's always a 800x600 resolution!
    resolutions.add(DEFAULT_RESOLUTION = new Resolution(800, 600));

    for (GraphicsDevice screenDevice : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {

      for (DisplayMode displayMode : screenDevice.getDisplayModes()) {
        Resolution resolution = new Resolution(displayMode.getWidth(), displayMode.getHeight());
        resolutions.add(resolution);
        
        refreshRates.add(displayMode.getRefreshRate());
      }
    }

    RESOLUTIONS = Collections.unmodifiableSet(resolutions);
    REFRESH_RATES = Collections.unmodifiableSet(refreshRates);

    log.finer("System resolutions " + RESOLUTIONS);
    log.finer("Refresh rates " + REFRESH_RATES);
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

  public static Set<Integer> getSystemRefreshRates()
  {
    return REFRESH_RATES;
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  public static void main(String[] args)
  {
    System.out.println("getSystemResolutions() = " + getSystemResolutions());
    System.out.println("getSystemRefreshRates() = " + getSystemRefreshRates());
  }
}
