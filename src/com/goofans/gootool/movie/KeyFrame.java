package com.goofans.gootool.movie;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class KeyFrame
{
  private static final int INTERPOLATION_NONE = 0;
  private static final int INTERPOLATION_LINEAR = 1;

  private float x;
  private float y;
  private float angle;
  private int alpha;
  private int color;
  private int nextFrameIndex;
  private int soundStrIndex;
  private int interpolationType;

  public KeyFrame(byte[] contents, int offset, int stringTableOffset)
  {
    x = BinaryFormat.getFloat(contents, offset + 0);
    y = BinaryFormat.getFloat(contents, offset + 4);
    angle = BinaryFormat.getFloat(contents, offset + 8);
    alpha = BinaryFormat.getInt(contents, offset + 12);
    color = BinaryFormat.getInt(contents, offset + 16);
    nextFrameIndex = BinaryFormat.getInt(contents, offset + 20);
    soundStrIndex = BinaryFormat.getInt(contents, offset + 24);
//    String soundStr = BinaryFormat.getString(contents, stringTableOffset + soundStrIndex);
//    System.out.println("soundStr = " + soundStr);
    interpolationType = BinaryFormat.getInt(contents, offset + 28);
  }

  @Override
  public String toString()
  {
    return "KeyFrame{" +
            "x=" + x +
            ", y=" + y +
            ", angle=" + angle +
            ", alpha=" + alpha +
            ", color=" + color +
            ", nextFrameIndex=" + nextFrameIndex +
            ", soundStrIndex=" + soundStrIndex +
            ", interpolationType=" + interpolationType +
            '}';
  }
}
