package com.goofans.gootool.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author David Croft (david.croft@infotrek.net)
 * @version $Id$
 */
public class Language
{
  private String code;
  private String displayName;

  public Language(String code, String displayName)
  {
    this.code = code;
    this.displayName = displayName;
  }

  public String getCode()
  {
    return code;
  }

  public String getDisplayName()
  {
    return displayName;
  }

  public String toString()
  {
    return displayName;
  }


  private final static List<Language> LANGUAGES;
  public final static Language DEFAULT_LANGUAGE;

  static {
    List<Language> languages = new ArrayList<Language>();

    DEFAULT_LANGUAGE = new Language("en", "English");
    languages.add(DEFAULT_LANGUAGE);
    languages.add(new Language("es", "Spanish"));
    LANGUAGES = Collections.unmodifiableList(languages);

    // TODO populate this from http://cyanyde.com/~scaevolus/WorldOfGoo/properties/text.xml
  }

  public static List<Language> getSupportedLanguages()
  {
    return LANGUAGES;
  }

  public static Language getLanguageByCode(String code)
  {
    for (Language language : LANGUAGES) {
      if (language.getCode().equals(code))
        return language;
    }
    return null;
  }
}
