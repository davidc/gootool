package com.goofans.gootool.movie;

import net.infotrek.util.XMLStringBuffer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public abstract class KeyFrame
{
  protected static final int INTERPOLATION_NONE = 0;
  protected static final int INTERPOLATION_LINEAR = 1;

  float x;
  float y;
  float angle;
  int alpha;
  int color;
  int nextFrameIndex;
  int soundStrIndex;
  int interpolationType;
  String soundStr;

  public KeyFrame(byte[] contents, int offset, int stringTableOffset)
  {
    x = BinaryFormat.getFloat(contents, offset + 0);
    y = BinaryFormat.getFloat(contents, offset + 4);
    angle = BinaryFormat.getFloat(contents, offset + 8);
    alpha = BinaryFormat.getInt(contents, offset + 12);
    color = BinaryFormat.getInt(contents, offset + 16);
    nextFrameIndex = BinaryFormat.getInt(contents, offset + 20);
    soundStrIndex = BinaryFormat.getInt(contents, offset + 24);
    if (soundStrIndex > 0) {
      soundStr = BinaryFormat.getString(contents, stringTableOffset + soundStrIndex);
    }
    interpolationType = BinaryFormat.getInt(contents, offset + 28);
  }

  public void toXML(XMLStringBuffer xml, int frame)
  {
    Map<String, String> attributes = new LinkedHashMap<String, String>(8);
    attributes.put("frame", String.valueOf(frame));
    attributes.put("nextframe", String.valueOf(nextFrameIndex));

    if (interpolationType == INTERPOLATION_LINEAR) attributes.put("interpolation", "linear");

    setFrameXMLAttributes(attributes);

    xml.addEmptyElement("keyframe", attributes);
  }

  protected abstract void setFrameXMLAttributes(Map<String, String> attributes);

  @Override
  public String toString()
  {
    String itStr = (interpolationType == INTERPOLATION_LINEAR ? "LINEAR" : interpolationType == INTERPOLATION_NONE ? "NONE" : String.valueOf(interpolationType));

    return "KeyFrame{" +
            "x=" + x +
            ", y=" + y +
            ", angle=" + angle +
            ", alpha=" + alpha +
            ", color=" + color +
            ", nextFrameIndex=" + nextFrameIndex +
            ", soundStrIndex=" + soundStrIndex + (soundStrIndex > 0 ? "(" + soundStr + ")" : "") +
            ", interpolationType=" + itStr +
            '}';
  }
}
