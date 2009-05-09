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
