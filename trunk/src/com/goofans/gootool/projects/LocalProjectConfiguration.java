/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.projects;

import com.goofans.gootool.model.Resolution;

/**
 * Represents the current World of Goo configuration.
 * <p/>
 * There's an "active" instance (what is currently configured on disk) and a "pending" instance (what will be applied).
 * <p/>
 * Support equals() in order to determine whether we must prompt before exit.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LocalProjectConfiguration extends ProjectConfiguration
{
  private Resolution resolution;
  private Integer refreshRate;
  private int uiInset;
  private boolean windowsVolumeControl;

  // this is a list because ordering of addins is important.

  LocalProjectConfiguration()
  {
  }

  public Resolution getResolution()
  {
    if (resolution == null) return Resolution.DEFAULT_RESOLUTION;
    return resolution;
  }

  public void setResolution(Resolution resolution)
  {
    this.resolution = resolution;
  }

  public Integer getRefreshRate()
  {
    return refreshRate;
  }

  public void setRefreshRate(Integer refreshRate)
  {
    this.refreshRate = refreshRate;
  }

  public int getUiInset()
  {
    return uiInset;
  }

  public void setUiInset(int uiInset)
  {
    this.uiInset = uiInset;
  }

  public boolean isWindowsVolumeControl()
  {
    return windowsVolumeControl;
  }

  public void setWindowsVolumeControl(boolean windowsVolumeControl)
  {
    this.windowsVolumeControl = windowsVolumeControl;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    LocalProjectConfiguration that = (LocalProjectConfiguration) o;

    if (uiInset != that.uiInset) return false;
    if (windowsVolumeControl != that.windowsVolumeControl) return false;
    if (refreshRate != null ? !refreshRate.equals(that.refreshRate) : that.refreshRate != null) return false;
    if (resolution != null ? !resolution.equals(that.resolution) : that.resolution != null) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = super.hashCode();
    result = 31 * result + (resolution != null ? resolution.hashCode() : 0);
    result = 31 * result + (refreshRate != null ? refreshRate.hashCode() : 0);
    result = 31 * result + uiInset;
    result = 31 * result + (windowsVolumeControl ? 1 : 0);
    return result;
  }

  @Override
  public Object clone()
  {
    LocalProjectConfiguration clone;
    clone = (LocalProjectConfiguration) super.clone();
    // Do any deep cloning of fields here
    return clone;
  }

  @Override
  public String toString()
  {
    return "LocalProjectConfiguration{" +
            super.toString() +
            ", resolution=" + resolution +
            ", refreshRate=" + refreshRate +
            ", uiInset=" + uiInset +
            ", windowsVolumeControl=" + windowsVolumeControl +
            '}';
  }
}
