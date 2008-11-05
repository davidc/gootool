package net.infotrek.gootool.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the current WoG configuration.
 * <p/>
 * There's an "active" instance (what is currently configured on disk) and a "pending" instance (what will be applied).
 * <p/>
 * Needs to support equals() in order to determine whether we must prompt before exit.
 *
 * @author David Croft (david.croft@infotrek.net)
 * @version $Id$
 */
public class Configuration
{
  private Language language;
  private Resolution resolution;
  private boolean allowWidescreen;
  private int uiInset;
  private boolean skipOpeningMovie;
  private String watermark;

  // this is a list because ordering of addins is important.
  private List<String> enabledAddins = new ArrayList<String>();

  public Configuration()
  {
//    addins.add(new Addin("One"));
//    addins.add(new Addin("Two"));
//    addins.add(new Addin("Three"));
//    addins.add(new Addin("Four"));
//    addins.add(new Addin("Five"));
//    addins.add(new Addin("Six"));
//    addins.add(new Addin("Seven"));
  }

  public Configuration(Configuration c)
  {
    language = c.language;
    resolution = c.resolution;
    allowWidescreen = c.allowWidescreen;
    uiInset = c.uiInset;
    skipOpeningMovie = c.skipOpeningMovie;
    watermark = c.watermark;

//    enabledAddins = new LinkedList<String>(c.enabledAddins);
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
    return resolution;
  }

  public void setResolution(Resolution resolution)
  {
    this.resolution = resolution;
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

  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Configuration that = (Configuration) o;

    if (allowWidescreen != that.allowWidescreen) return false;
    if (skipOpeningMovie != that.skipOpeningMovie) return false;
    if (uiInset != that.uiInset) return false;
    if (language != null ? !language.equals(that.language) : that.language != null) return false;
    if (resolution != null ? !resolution.equals(that.resolution) : that.resolution != null) return false;
    if (watermark != null ? !watermark.equals(that.watermark) : that.watermark != null) return false;

    if (!enabledAddins.equals(that.enabledAddins)) return false;

    return true;
  }

  public int hashCode()
  {
    int result;
    result = (language != null ? language.hashCode() : 0);
    result = 31 * result + (resolution != null ? resolution.hashCode() : 0);
    result = 31 * result + (watermark != null ? watermark.hashCode() : 0);
    result = 31 * result + (allowWidescreen ? 1 : 0);
    result = 31 * result + uiInset;
    result = 31 * result + (skipOpeningMovie ? 1 : 0);
    result = 31 * result + enabledAddins.hashCode();
    return result;
  }
}
