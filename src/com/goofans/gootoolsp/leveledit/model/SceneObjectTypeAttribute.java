/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.model;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class SceneObjectTypeAttribute
{
  private final SceneObjectTypeAttributeType type;
  private final String xmlAttribute;
  private final boolean required;

  public SceneObjectTypeAttribute(String xmlAttribute, SceneObjectTypeAttributeType type, boolean required)
  {
    this.type = type;
    this.xmlAttribute = xmlAttribute;
    this.required = required;
  }

  public SceneObjectTypeAttributeType getType()
  {
    return type;
  }

  public String getXmlAttribute()
  {
    return xmlAttribute;
  }

  public boolean isRequired()
  {
    return required;
  }
}
