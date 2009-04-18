package com.goofans.gootool.movie;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BinActor
{
  private static final int ACTORTYPE_IMAGE = 0;
  private static final int ACTORTYPE_TEXT = 1;

  int actorType;
  String imageStr;
  String labelStr;
  String fontStr;
  float labelMaxWidth;
  float labelWrapWidth;
  int labelJustification;
  float depth;

  public BinActor(int actorType, String imageStr, String labelStr, String fontStr, float labelMaxWidth, float labelWrapWidth, int labelJustification, float depth)
  {
    this.actorType = actorType;
    this.imageStr = imageStr;
    this.labelStr = labelStr;
    this.fontStr = fontStr;
    this.labelMaxWidth = labelMaxWidth;
    this.labelWrapWidth = labelWrapWidth;
    this.labelJustification = labelJustification;
    this.depth = depth;
  }

  @Override
  public String toString()
  {
    String atStr = (actorType == ACTORTYPE_IMAGE ? "IMAGE" : actorType == ACTORTYPE_TEXT ? "TEXT" : String.valueOf(actorType));

    return "BinActor{" +
            "actorType=" + atStr +
            ", imageStr='" + imageStr + '\'' +
            ", labelStr='" + labelStr + '\'' +
            ", fontStr='" + fontStr + '\'' +
            ", labelMaxWidth=" + labelMaxWidth +
            ", labelWrapWidth=" + labelWrapWidth +
            ", labelJustification=" + labelJustification +
            ", depth=" + depth +
            '}';
  }
}
