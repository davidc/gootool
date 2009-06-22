package com.goofans.gootool;

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class TextProvider
{
  private ResourceBundle resources;

  public TextProvider(String baseName)
  {
    resources = ResourceBundle.getBundle(baseName);
  }

  public String getText(String key, Object... args)
  {
    return MessageFormat.format(resources.getString(key), args);
  }

}