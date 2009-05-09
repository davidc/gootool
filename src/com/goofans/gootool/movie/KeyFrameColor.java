package com.goofans.gootool.movie;


import java.util.Map;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class KeyFrameColor extends KeyFrame
{
  public KeyFrameColor(byte[] contents, int offset, int stringTableOffset)
  {
    super(contents, offset, stringTableOffset);
  }

  @Override
  protected void setFrameXMLAttributes(Map<String, String> attributes)
  {
    attributes.put("color", String.valueOf(color));
  }
}