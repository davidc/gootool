package com.goofans.gootool.movie;

import net.infotrek.util.XMLStringBuffer;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BinActor
{
  private static final int ACTORTYPE_IMAGE = 0;
  private static final int ACTORTYPE_TEXT = 1;

  private static final int ALIGN_LEFT = 0;
  private static final int ALIGN_CENTER = 1;
  private static final int ALIGN_RIGHT = 2;

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

    validate();
  }

  private void validate()
  {
    switch (actorType) {
      case ACTORTYPE_TEXT:
        assert (imageStr == null);
        assert (labelStr != null && labelStr.length() > 0);
        assert (fontStr != null && fontStr.length() > 0);
        break;
      case ACTORTYPE_IMAGE:
        assert (imageStr != null && imageStr.length() > 0);
        assert (labelStr == null);
        assert (fontStr == null);
        assert (labelMaxWidth == -1);
        assert (labelWrapWidth == -1);
        break;
      default:
        throw new AssertionError("invalid actor type");
    }
    if (labelJustification != ALIGN_LEFT && labelJustification != ALIGN_CENTER && labelJustification != ALIGN_RIGHT) {
      throw new AssertionError("invalid actor align");
    }
  }

  /**
   * Produces a &lt;actor&gt; element, not a well-formed document.
   *
   * @param xml XMLStringBuffer to write into
   * @param anim
   */
  public void toXML(XMLStringBuffer xml, BinImageAnimation anim)
  {
    Map<String, String> attributes = new LinkedHashMap<String, String>();
    attributes.put("type", (actorType == ACTORTYPE_IMAGE ? "image" : actorType == ACTORTYPE_TEXT ? "text" : "invalid"));
    attributes.put("depth", String.valueOf(depth));
    attributes.put("align", (labelJustification == ALIGN_LEFT ? "left" : labelJustification == ALIGN_CENTER ? "center" : labelJustification == ALIGN_RIGHT ? "right" : "unknown"));
    xml.push("actor", attributes);

    attributes.clear();
    switch (actorType) {
      case ACTORTYPE_IMAGE:
        attributes.put("id", imageStr);
        xml.addEmptyElement("image", attributes);
        break;
      case ACTORTYPE_TEXT:
        attributes.put("font", fontStr);
        if (labelMaxWidth > -1) {
          attributes.put("max-width", String.valueOf(labelMaxWidth));
        }
        if (labelWrapWidth > -1) {
          attributes.put("wrap-width", String.valueOf(labelWrapWidth));
        }
        xml.addRequired("text", labelStr, attributes);
        break;
    }
    anim.toXML(xml);
    
    xml.pop("actor");
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
