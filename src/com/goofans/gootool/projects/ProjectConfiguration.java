/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.projects;

import java.util.ArrayList;
import java.util.List;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinsStore;
import com.goofans.gootool.model.Language;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProjectConfiguration implements Cloneable
{
  private Language language;
  private boolean skipOpeningMovie;
  private String watermark;
  private boolean billboardsDisabled;
  private List<String> enabledAddins = new ArrayList<String>();

  ProjectConfiguration()
  {
  }

  ProjectConfiguration(ProjectConfiguration c)
  {
    language = c.language;
    skipOpeningMovie = c.skipOpeningMovie;
    watermark = c.watermark;
    billboardsDisabled = c.billboardsDisabled;

    enabledAddins = new ArrayList<String>(c.enabledAddins);
  }


  public Language getLanguage()
  {
    if (language == null) return Language.DEFAULT_LANGUAGE;
    return language;
  }

  public void setLanguage(Language language)
  {
    this.language = language;
  }

  public boolean isSkipOpeningMovie()
  {
    return skipOpeningMovie;
  }

  public void setSkipOpeningMovie(boolean skipOpeningMovie)
  {
    this.skipOpeningMovie = skipOpeningMovie;
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

  public boolean isBillboardsDisabled()
  {
    return billboardsDisabled;
  }

  public void setBillboardsDisabled(boolean billboardsDisabled)
  {
    this.billboardsDisabled = billboardsDisabled;
  }

  public boolean isEnabledAddin(String id)
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

  public void disableAllAddins()
  {
    enabledAddins.clear();
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

    ProjectConfiguration that = (ProjectConfiguration) o;

    if (billboardsDisabled != that.billboardsDisabled) return false;
    if (skipOpeningMovie != that.skipOpeningMovie) return false;
    if (enabledAddins != null ? !enabledAddins.equals(that.enabledAddins) : that.enabledAddins != null) return false;
    if (language != null ? !language.equals(that.language) : that.language != null) return false;
    if (watermark != null ? !watermark.equals(that.watermark) : that.watermark != null) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = language != null ? language.hashCode() : 0;
    result = 31 * result + (skipOpeningMovie ? 1 : 0);
    result = 31 * result + (watermark != null ? watermark.hashCode() : 0);
    result = 31 * result + (billboardsDisabled ? 1 : 0);
    result = 31 * result + (enabledAddins != null ? enabledAddins.hashCode() : 0);
    return result;
  }

  @Override
  public Object clone()
  {
    ProjectConfiguration clone;
    try {
      clone = (ProjectConfiguration) super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new RuntimeException("Unable to clone!", e);
    }
    clone.enabledAddins = new ArrayList<String>(this.enabledAddins);
    return clone;
  }

  @Override
  public String toString()
  {
    return "ProjectConfiguration{" +
            "language=" + language +
            ", skipOpeningMovie=" + skipOpeningMovie +
            ", watermark='" + watermark + '\'' +
            ", billboardsDisabled=" + billboardsDisabled +
            ", enabledAddins=" + enabledAddins +
            '}';
  }
}
