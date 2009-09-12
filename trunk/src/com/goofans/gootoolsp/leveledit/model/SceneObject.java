package com.goofans.gootoolsp.leveledit.model;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;
import java.util.List;

import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class SceneObject
{
  private Map<SceneObjectTypeAttribute, Object> values = new HashMap<SceneObjectTypeAttribute, Object>();
  private SceneObjectType type;

  private java.util.List<SceneObject> childObjects;

  public SceneObject(Element el) throws IOException
  {
    type = SceneObjectType.getSceneObjectTypeByName(el.getNodeName());
    if (type == null) throw new IOException("Unknown scene object element " + el.getNodeName());

    readType(el, type);

    if (type.isCanHaveChildren()) {
      childObjects = new LinkedList<SceneObject>();

      NodeList childNodes = el.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node node = childNodes.item(i);
        if (node instanceof Element) {
          Element childEl = (Element) node;
          SceneObject sceneObject = new SceneObject(childEl);
          childObjects.add(sceneObject);
        }
      }
    }
  }

  private void readType(Element el, SceneObjectType currentType) throws IOException
  {
    // If we have a superclass, read that first
    if (currentType.getSuperclass() != null) {
      readType(el, currentType.getSuperclass());
    }

    SceneObjectTypeAttribute[] attributes = currentType.getAttributes();
    for (SceneObjectTypeAttribute attribute : attributes) {
      String valueStr = XMLUtil.getAttributeString(el, attribute.getXmlAttribute(), null);

      if (attribute.isRequired() && valueStr == null) {
        throw new IOException("Required attribute " + attribute.getXmlAttribute() + " missing.");
      }

      if (valueStr == null) {
        values.put(attribute, null);
      }
      else {
        Object value = parseAttribute(attribute, valueStr);
        values.put(attribute, value);
      }
    }

    // TODO if CanHaveChildren
  }

  private Object parseAttribute(SceneObjectTypeAttribute attribute, String valueStr) throws IOException
  {
    try {
      switch (attribute.getType()) {
        case BOOLEAN:
          return Boolean.valueOf(valueStr);

        case COLOR_RGBA:
          return parseColorRGBA(valueStr);

        case COLOR_RGB:
          return parseColorRGB(valueStr);

        case DOUBLE:
          Double d = Double.valueOf(valueStr);
          if (d.isNaN() || d.isInfinite()) return null;
          return d;

        case INT:
          // TODO what if it's acfcidentally a double?
          return Integer.valueOf(valueStr);

        case STRING:
          return valueStr;

        case VECTOR_INT:
          return parseVectorInt(valueStr);

        case POINT_DOUBLE:
          return parsePointDouble(valueStr);
      }
    }
    catch (NumberFormatException e) {
      throw new IOException("Invalid value for " + attribute.getXmlAttribute() + ":  " + valueStr);
    }

    throw new RuntimeException("Fell off end of enum"); // unreachable
  }

  private Color parseColorRGB(String value) throws IOException
  {
    if (value == null) return null;

    String[] bits = value.split(",", 3);

    try {
      return new Color(Integer.valueOf(bits[0]),
              Integer.valueOf(bits[1]),
              Integer.valueOf(bits[2]));
    }
    catch (NumberFormatException e) {
      throw new IOException("Invalid value for color: " + value);
    }
  }

  private Color parseColorRGBA(String value) throws IOException
  {
    if (value == null) return null;

    String[] bits = value.split(",", 4);

    try {
      return new Color(Integer.valueOf(bits[0]),
              Integer.valueOf(bits[1]),
              Integer.valueOf(bits[2]),
              255 - Integer.valueOf(bits[1]));
    }
    catch (NumberFormatException e) {
      throw new IOException("Invalid value for color: " + value);
    }

    // TODO Test WaterLock, it has funny double values that really should be ints.
  }

  private Point parseVectorInt(String value) throws IOException
  {
    String[] bits = value.split(",", 2);

    Point point = new Point();

    try {
      point.x = Integer.valueOf(bits[0]);
      point.y = Integer.valueOf(bits[1]);
    }
    catch (NumberFormatException e) {
      throw new IOException("Invalid value for vector: " + value);
    }

    return point;
  }

  private Point2D.Double parsePointDouble(String value) throws IOException
  {
    String[] bits = value.split(",", 2);

    Point2D.Double point = new Point2D.Double();

    try {
      point.x = Double.valueOf(bits[0]);
      point.y = Double.valueOf(bits[1]);
    }
    catch (NumberFormatException e) {
      throw new IOException("Invalid value for point: " + value);
    }

    return point;
  }

  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append("SceneObject of type ").append(type.getXmlElementName()).append("[");

    boolean first = true;
    for (SceneObjectTypeAttribute attribute : values.keySet()) {
      if (!first) sb.append(", ");
      sb.append(attribute.getXmlAttribute()).append("=").append(values.get(attribute));
      first = false;
    }
    return sb.toString();
  }

  public SceneObjectType getType()
  {
    return type;
  }

  public Map<SceneObjectTypeAttribute, Object> getValues()
  {
    return values;
  }

  public Object getValue(SceneObjectTypeAttribute attribute)
  {
    return values.get(attribute);
  }

  public List<SceneObject> getChildObjects()
  {
    return childObjects;
  }
}
