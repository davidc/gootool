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
