/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import java.util.ResourceBundle;
import java.util.Enumeration;
import java.text.MessageFormat;

/**
 * GooTool's ResourceBundle with a couple of convenience methods. Formerly "TextProvider".
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GooToolResourceBundle extends ResourceBundle
{
  private final ResourceBundle resources;

  public GooToolResourceBundle(String baseName)
  {
    resources = ResourceBundle.getBundle(baseName);
  }

  public String formatString(String key, Object... args)
  {
    return MessageFormat.format(resources.getString(key), args);
  }

  @Override
  protected Object handleGetObject(String key)
  {
    return resources.getString(key);
  }

  @Override
  public Enumeration<String> getKeys()
  {
    return resources.getKeys();
  }
}
