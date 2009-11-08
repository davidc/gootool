package com.goofans.gootool.movie;

import net.infotrek.util.XMLStringBuffer;

import java.util.Map;
import java.util.LinkedHashMap;
import java.io.IOException;

import org.w3c.dom.Element;
import com.goofans.gootool.util.XMLUtil;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BinActor
{
  /* Constants for actorType */
  private static final int ACTORTYPE_IMAGE = 0;
  private static final int ACTORTYPE_TEXT = 1;
  private static final String ACTORTYPE_IMAGE_STR = "image";
  private static final String ACTORTYPE_TEXT_STR = "text";

  /* Constants for labelJustification */
  private static final int ALIGN_LEFT = 0;
  private static final int ALIGN_CENTER = 1;
  private static final int ALIGN_RIGHT = 2;
  private static final String ALIGN_LEFT_STR = "left";
  private static final String ALIGN_CENTER_STR = "center";
  private static final String ALIGN_RIGHT_STR = "right";

  /* XML elements and attributes */
  private static final String ACTOR_ATTR_TYPE = "type";
  private static final String ACTOR_ATTR_DEPTH = "depth";
  private static final String ACTOR_ATTR_ALIGN = "align";
  private static final String ACTOR_IMAGE = "image";
  private static final String ACTOR_IMAGE_ATTR_ID = "id";
  private static final String ACTOR_TEXT = "text";
  private static final String ACTOR_TEXT_ATTR_FONT = "font";
  private static final String ACTOR_TEXT_ATTR_MAX_WIDTH = "max-width";
  private static final String ACTOR_TEXT_ATTR_WRAP_WIDTH = "wrap-width";

  /* Fields */
  private int actorType;
  private String imageStr;
  private String labelStr;
  private String fontStr;
  private float labelMaxWidth;
  private float labelWrapWidth;
  private int labelJustification;
  private float depth;

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

  public BinActor(Element actorEl) throws IOException
  {
    String actorTypeStr = XMLUtil.getAttributeStringRequired(actorEl, ACTOR_ATTR_TYPE);
    if (ACTORTYPE_IMAGE_STR.equals(actorTypeStr)) actorType = ACTORTYPE_IMAGE;
    else if (ACTORTYPE_TEXT_STR.equals(actorTypeStr)) actorType = ACTORTYPE_TEXT;
    else throw new IOException("Invalid actor type " + actorTypeStr);

    depth = XMLUtil.getAttributeFloatRequired(actorEl, ACTOR_ATTR_DEPTH);

    String alignStr = XMLUtil.getAttributeStringRequired(actorEl, ACTOR_ATTR_ALIGN);
    if (ALIGN_LEFT_STR.equals(actorTypeStr)) labelJustification = ALIGN_LEFT;
    if (ALIGN_CENTER_STR.equals(actorTypeStr)) labelJustification = ALIGN_CENTER;
    if (ALIGN_RIGHT_STR.equals(actorTypeStr)) labelJustification = ALIGN_RIGHT;
    else throw new IOException("Invalid actor align " + alignStr);

    switch (actorType) {
      case ACTORTYPE_IMAGE:
        Element imageEl = XMLUtil.getElementRequired(actorEl, ACTOR_IMAGE);
        imageStr = XMLUtil.getAttributeStringRequired(imageEl, ACTOR_IMAGE_ATTR_ID);
        break;
      case ACTORTYPE_TEXT:
        Element textEl = XMLUtil.getElementRequired(actorEl, ACTOR_TEXT);
        fontStr = XMLUtil.getAttributeStringRequired(textEl, ACTOR_TEXT_ATTR_FONT);
        labelMaxWidth = XMLUtil.getAttributeFloat(textEl, ACTOR_TEXT_ATTR_MAX_WIDTH, -1.0f);
        labelWrapWidth = XMLUtil.getAttributeFloat(textEl, ACTOR_TEXT_ATTR_WRAP_WIDTH, -1.0f);
        labelStr = textEl.getTextContent().trim();
        break;
    }
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
   * @param xml  XMLStringBuffer to write into
   * @param anim the BinImageAnimation associated with this actor.
   */
  public void toXML(XMLStringBuffer xml, BinImageAnimation anim)
  {
    Map<String, String> attributes = new LinkedHashMap<String, String>();
    attributes.put(ACTOR_ATTR_TYPE, (actorType == ACTORTYPE_IMAGE ? ACTORTYPE_IMAGE_STR : actorType == ACTORTYPE_TEXT ? ACTORTYPE_TEXT_STR : "invalid"));
    attributes.put(ACTOR_ATTR_DEPTH, String.valueOf(depth));
    attributes.put(ACTOR_ATTR_ALIGN, (labelJustification == ALIGN_LEFT ? ALIGN_LEFT_STR : labelJustification == ALIGN_CENTER ? ALIGN_CENTER_STR : labelJustification == ALIGN_RIGHT ? ALIGN_RIGHT_STR : "unknown"));
    xml.push("actor", attributes);

    attributes.clear();
    switch (actorType) {
      case ACTORTYPE_IMAGE:
        attributes.put(ACTOR_IMAGE_ATTR_ID, imageStr);
        xml.addEmptyElement(ACTOR_IMAGE, attributes);
        break;
      case ACTORTYPE_TEXT:
        attributes.put(ACTOR_TEXT_ATTR_FONT, fontStr);
        if (labelMaxWidth > -1) {
          attributes.put(ACTOR_TEXT_ATTR_MAX_WIDTH, String.valueOf(labelMaxWidth));
        }
        if (labelWrapWidth > -1) {
          attributes.put(ACTOR_TEXT_ATTR_WRAP_WIDTH, String.valueOf(labelWrapWidth));
        }
        xml.addRequired(ACTOR_TEXT, labelStr, attributes);
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
