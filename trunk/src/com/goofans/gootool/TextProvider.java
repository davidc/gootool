package com.goofans.gootool;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.ListResourceBundle;
import java.text.MessageFormat;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class TextProvider // TODO extends ResourceBundle
{
  private final ResourceBundle resources;

  public TextProvider(String baseName)
  {
    resources = ResourceBundle.getBundle(baseName);
  }

  public String getText(String key, Object... args)
  {
    return MessageFormat.format(resources.getString(key), args);
  }

  public String getOptionalText(String key, Object... args)
  {
    try {
      return getText(key, args);
    }
    catch (MissingResourceException e) {
      return null;
    }
  }
}
