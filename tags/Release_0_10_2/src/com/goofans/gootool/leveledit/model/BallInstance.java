package com.goofans.gootool.leveledit.model;

import java.io.IOException;

import org.w3c.dom.Element;
import com.goofans.gootool.util.XMLUtil;

/**
 * Node "BallInstance", attribute "angle" is mandatory (2702 occurrences found)
 * Node "BallInstance", attribute "discovered" is optional, occurrences with/without attribute: 568/2134
 * Node "BallInstance", attribute "id" is mandatory (2702 occurrences found)
 * Node "BallInstance", attribute "type" is mandatory (2702 occurrences found)
 * Node "BallInstance", attribute "x" is mandatory (2702 occurrences found)
 * Node "BallInstance", attribute "y" is mandatory (2702 occurrences found)
 * 6 attributes found
 * 0 child tags found for node "BallInstance"
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BallInstance extends LevelContentsItem
{
  private double angle;
  private Boolean discovered;
  public String id;
  public String type;
  public double x;
  public double y;

  public BallInstance(String type, double x, double y)
  {
    this.type = type;
    this.x = x;
    this.y = y;
  }

  public BallInstance(Element element) throws IOException
  {
    angle = XMLUtil.getAttributeDoubleRequired(element, "angle");
    discovered = XMLUtil.getAttributeBoolean(element, "discovered", null);
    id = XMLUtil.getAttributeStringRequired(element, "id");
    type = XMLUtil.getAttributeStringRequired(element, "type");
    x = XMLUtil.getAttributeDoubleRequired(element, "x");
    y = XMLUtil.getAttributeDoubleRequired(element, "y");
  }

  @Override
  public String toString()
  {
    return "BallInstance, id='" + id + '\'' +
            ", type='" + type + '\'' +
            ", x=" + x +
            ", y=" + y;
  }
}
