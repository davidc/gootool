/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

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
          new Language("zh", "Chinese"),
          new Language("cs", "Czech"),
          new Language("da", "Danish"),
          new Language("nl", "Dutch"),
          new Language("eo", "Esperanto"),
          new Language("fi", "Finnish"),
          new Language("fr", "French"),
          new Language("ka", "Georgian"),
          new Language("de", "German"),
          new Language("he", "Hebrew"),
          new Language("hu", "Hungarian"),
          new Language("it", "Italian"),
          new Language("ja", "Japanese"),
          new Language("no", "Norwegian"),
          new Language("pl", "Polish"),
          new Language("pt", "Portugese"),
          new Language("pt_BR", "Portuguese (Brazilian)"),
          new Language("ru", "Russian"),
          new Language("es", "Spanish"),
          new Language("sv", "Swedish"),
          new Language("tp", "Toki Pona"),
          new Language("uk", "Ukrainian"),
          new Language("vi", "Vietnamese")
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
