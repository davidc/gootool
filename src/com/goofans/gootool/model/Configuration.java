/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.model;

import java.util.ArrayList;
import java.util.List;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinsStore;

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
public class Configuration
{
  private Language language;
  private Resolution resolution;
  private Integer refreshRate;
  private boolean allowWidescreen;
  private int uiInset;
  private boolean skipOpeningMovie;
  private String watermark;
  private boolean windowsVolumeControl;

  // this is a list because ordering of addins is important.
  private List<String> enabledAddins = new ArrayList<String>();

  public Configuration()
  {
  }

  public Configuration(Configuration c)
  {
    language = c.language;
    resolution = c.resolution;
    refreshRate = c.refreshRate;
    allowWidescreen = c.allowWidescreen;
    uiInset = c.uiInset;
    skipOpeningMovie = c.skipOpeningMovie;
    watermark = c.watermark;
    windowsVolumeControl = c.windowsVolumeControl;

    enabledAddins = new ArrayList<String>(c.enabledAddins);
  }

  public Language getLanguage()
  {
    return language;
  }

  public void setLanguage(Language language)
  {
    this.language = language;
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

  public String getWatermark()
  {
    return watermark;
  }

  public void setWatermark(String watermark)
  {
    if (watermark == null) watermark = "";
    this.watermark = watermark;
  }

  public boolean isAllowWidescreen()
  {
    return allowWidescreen;
  }

  public void setAllowWidescreen(boolean allowWidescreen)
  {
    this.allowWidescreen = allowWidescreen;
  }

  public boolean isSkipOpeningMovie()
  {
    return skipOpeningMovie;
  }

  public void setSkipOpeningMovie(boolean skipOpeningMovie)
  {
    this.skipOpeningMovie = skipOpeningMovie;
  }

  public boolean isWindowsVolumeControl()
  {
    return windowsVolumeControl;
  }

  public void setWindowsVolumeControl(boolean windowsVolumeControl)
  {
    this.windowsVolumeControl = windowsVolumeControl;
  }

  public boolean isEnabledAdddin(String id)
  {
    return (enabledAddins.contains(id.intern()));
  }

  public void enableAddin(String id)
  {
    String idInterned = id.intern();
    if (!enabledAddins.contains(idInterned)) {
      enabledAddins.add(idInterned);
    }
  }

  public void disableAddin(String id)
  {
    enabledAddins.remove(id.intern());
  }

  public List<String> getEnabledAddins()
  {
    return enabledAddins;
  }

  public List<Addin> getEnabledAddinsAsAddins()
  {
    List<Addin> availableAddins = AddinsStore.getAvailableAddins();
    List<Addin> addins = new ArrayList<Addin>(enabledAddins.size());

    for (String enabledAddinId : enabledAddins) {
      for (Addin availableAddin : availableAddins) {
        if (availableAddin.getId().equals(enabledAddinId)) {
          addins.add(availableAddin);
        }
      }
    }
    return addins;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Configuration that = (Configuration) o;

    if (allowWidescreen != that.allowWidescreen) return false;
    if (skipOpeningMovie != that.skipOpeningMovie) return false;
    if (uiInset != that.uiInset) return false;
    if (windowsVolumeControl != that.windowsVolumeControl) return false;
    if (language != null ? !language.equals(that.language) : that.language != null) return false;
    if (resolution != null ? !resolution.equals(that.resolution) : that.resolution != null) return false;
    if (refreshRate != null ? !refreshRate.equals(that.refreshRate) : that.refreshRate != null) return false;
    if (watermark != null ? !watermark.equals(that.watermark) : that.watermark != null) return false;

    return enabledAddins.equals(that.enabledAddins);
  }

  @Override
  public int hashCode()
  {
    int result;
    result = (language != null ? language.hashCode() : 0);
    result = 31 * result + (resolution != null ? resolution.hashCode() : 0);
    result = 31 * result + (refreshRate != null ? refreshRate.hashCode() : 0);
    result = 31 * result + (watermark != null ? watermark.hashCode() : 0);
    result = 31 * result + (allowWidescreen ? 1 : 0);
    result = 31 * result + uiInset;
    result = 31 * result + (skipOpeningMovie ? 1 : 0);
    result = 31 * result + (windowsVolumeControl ? 1 : 0);
    result = 31 * result + enabledAddins.hashCode();
    return result;
  }
}
