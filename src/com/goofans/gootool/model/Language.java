package com.goofans.gootool.model;

import java.util.List;
import java.util.Collections;
import java.util.Arrays;

/**
 * A language the game can be played in. Immutable after construction. Also contains static methods to look up languages.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Language
{
  private final String code;
  private final String displayName;

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

  @Override
  public String toString()
  {
    return displayName;
  }

  public static final Language DEFAULT_LANGUAGE;

  @SuppressWarnings({"HardCodedStringLiteral"})
  private static final List<Language> LANGUAGES = Collections.unmodifiableList(Arrays.asList(
          DEFAULT_LANGUAGE = new Language("en", "English"),
          new Language("es", "Spanish"),
          new Language("de", "German"),
          new Language("fr", "French"),
          new Language("it", "Italian"),
          new Language("nl", "Dutch"),
          new Language("ru", "Russian")
  ));

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
