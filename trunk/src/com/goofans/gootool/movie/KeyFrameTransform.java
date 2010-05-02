/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.movie;


import java.util.Map;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class KeyFrameTransform extends KeyFrame
{
  private final TransformType transformType;

  public KeyFrameTransform(byte[] contents, int offset, int stringTableOffset, TransformType transformType)
  {
    super(contents, offset, stringTableOffset);
    this.transformType = transformType;
  }

  @Override
  protected void setFrameXMLAttributes(Map<String, String> attributes)
  {
    if (transformType == TransformType.ROTATE) {
      attributes.put("angle", String.valueOf(angle));
    }
    else {
      attributes.put("x", String.valueOf(x));
      attributes.put("y", String.valueOf(y));
    }
  }
}
