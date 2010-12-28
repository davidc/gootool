/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.movie;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public enum TransformType
{
  SCALE(0),
  ROTATE(1),
  TRANSLATE(2);

  private final int value;

  TransformType(int value)
  {
    this.value = value;
  }

  public static TransformType getByValue(int value)
  {
    for (TransformType tt : TransformType.values()) {
      if (tt.value == value) return tt;
    }
    return null;
  }
}
