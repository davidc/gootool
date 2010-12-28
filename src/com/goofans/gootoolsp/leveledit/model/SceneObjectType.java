/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.model;

import java.util.Map;
import java.util.HashMap;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
@SuppressWarnings({"HardCodedStringLiteral"})
public class SceneObjectType
{
  public static final SceneObjectTypeAttribute OPTIONAL_ATTRIBUTE_ID = new SceneObjectTypeAttribute("id", SceneObjectTypeAttributeType.STRING, false);
  /**
   * Node "radialforcefield", attribute "antigrav" is mandatory (6 occurrences found)
   * Node "linearforcefield", attribute "antigrav" is mandatory (192 occurrences found)
   * <p/>
   * Node "radialforcefield", attribute "dampeningfactor" is mandatory (6 occurrences found)
   * Node "linearforcefield", attribute "dampeningfactor" is mandatory (192 occurrences found)
   * <p/>
   * Node "radialforcefield", attribute "center" is mandatory (6 occurrences found)
   * Node "linearforcefield", attribute "center" is optional, occurrences with/without attribute: 137/55
   * <p/>
   * Node "radialforcefield", attribute "enabled" is optional, occurrences with/without attribute: 5/1
   * Node "linearforcefield", attribute "enabled" is optional, occurrences with/without attribute: 137/55
   * <p/>
   * Node "radialforcefield", attribute "geomonly" is optional, occurrences with/without attribute: 5/1
   * Node "linearforcefield", attribute "geomonly" is optional, occurrences with/without attribute: 189/3
   * <p/>
   * Node "radialforcefield", attribute "id" is mandatory (6 occurrences found)
   * Node "linearforcefield", attribute "id" is optional, occurrences with/without attribute: 137/55
   * <p/>
   * Node "radialforcefield", attribute "type" is mandatory (6 occurrences found)
   * Node "linearforcefield", attribute "type" is mandatory (192 occurrences found)
   */
  private static final SceneObjectTypeAttribute[] FORCEFIELD_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          new SceneObjectTypeAttribute("antigrav", SceneObjectTypeAttributeType.BOOLEAN, true),
          new SceneObjectTypeAttribute("dampeningfactor", SceneObjectTypeAttributeType.DOUBLE, true),
          new SceneObjectTypeAttribute("center", SceneObjectTypeAttributeType.POINT_DOUBLE, false),
          new SceneObjectTypeAttribute("enabled", SceneObjectTypeAttributeType.BOOLEAN, false),
          new SceneObjectTypeAttribute("geomonly", SceneObjectTypeAttributeType.BOOLEAN, false),
          OPTIONAL_ATTRIBUTE_ID,
          new SceneObjectTypeAttribute("type", SceneObjectTypeAttributeType.STRING, true),
  };

  /**
   * Node "radialforcefield", attribute "forceatcenter" is mandatory (6 occurrences found)
   * Node "radialforcefield", attribute "forceatedge" is mandatory (6 occurrences found)
   * Node "radialforcefield", attribute "radius" is mandatory (6 occurrences found)
   */
  private static final SceneObjectTypeAttribute[] RADIALFORCEFIELD_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          new SceneObjectTypeAttribute("forceatcenter", SceneObjectTypeAttributeType.DOUBLE, false),
          new SceneObjectTypeAttribute("forceatedge", SceneObjectTypeAttributeType.DOUBLE, false),
          new SceneObjectTypeAttribute("radius", SceneObjectTypeAttributeType.DOUBLE, false)
  };

  /**
   * Node "linearforcefield", attribute "force" is mandatory (192 occurrences found)
   * Node "linearforcefield", attribute "width" is optional, occurrences with/without attribute: 137/55
   * Node "linearforcefield", attribute "height" is optional, occurrences with/without attribute: 137/55
   * Node "linearforcefield", attribute "color" is optional, occurrences with/without attribute: 29/163
   * Node "linearforcefield", attribute "depth" is optional, occurrences with/without attribute: 23/169
   * Node "linearforcefield", attribute "rotationaldampeningfactor" is optional, occurrences with/without attribute: 1/191
   * Node "linearforcefield", attribute "water" is optional, occurrences with/without attribute: 137/55
   */
  private static final SceneObjectTypeAttribute[] LINEARFORCEFIELD_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          new SceneObjectTypeAttribute("force", SceneObjectTypeAttributeType.VECTOR_INT, true),
          new SceneObjectTypeAttribute("width", SceneObjectTypeAttributeType.DOUBLE, false),
          new SceneObjectTypeAttribute("height", SceneObjectTypeAttributeType.DOUBLE, false),
          new SceneObjectTypeAttribute("color", SceneObjectTypeAttributeType.COLOR_RGBA, false),
          new SceneObjectTypeAttribute("depth", SceneObjectTypeAttributeType.INT, false),
          new SceneObjectTypeAttribute("rotationaldampeningfactor", SceneObjectTypeAttributeType.DOUBLE, false),
          new SceneObjectTypeAttribute("water", SceneObjectTypeAttributeType.BOOLEAN, false)
  };

  /**
   * PARTICLE EFFECT
   * <p/>
   * Node "particles", attribute "depth" is optional, occurrences with/without attribute: 173/40
   * Node "particles", attribute "effect" is optional, occurrences with/without attribute: 173/40
   * Node "particles", attribute "enabled" is optional, occurrences with/without attribute: 1/212
   * Node "particles", attribute "id" is optional, occurrences with/without attribute: 41/172
   * Node "particles", attribute "overball" is optional, occurrences with/without attribute: 40/173
   * Node "particles", attribute "pos" is optional, occurrences with/without attribute: 87/126
   * Node "particles", attribute "pretick" is optional, occurrences with/without attribute: 171/42
   * Node "particles", attribute "states" is optional, occurrences with/without attribute: 40/173
   */

  private static final SceneObjectTypeAttribute[] PARTICLES_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          new SceneObjectTypeAttribute("depth", SceneObjectTypeAttributeType.DOUBLE, false),
          new SceneObjectTypeAttribute("effect", SceneObjectTypeAttributeType.STRING, false),
          new SceneObjectTypeAttribute("enabled", SceneObjectTypeAttributeType.BOOLEAN, false),
          OPTIONAL_ATTRIBUTE_ID,
          new SceneObjectTypeAttribute("overball", SceneObjectTypeAttributeType.BOOLEAN, false),
          new SceneObjectTypeAttribute("pos", SceneObjectTypeAttributeType.POINT_DOUBLE, false),
          new SceneObjectTypeAttribute("pretick", SceneObjectTypeAttributeType.INT, false),
          new SceneObjectTypeAttribute("states", SceneObjectTypeAttributeType.STRING, false)
  };

  public static final SceneObjectTypeAttribute GEOM_ATTRIBUTE_ROTATION = new SceneObjectTypeAttribute("rotation", SceneObjectTypeAttributeType.DOUBLE, false);
  public static final SceneObjectTypeAttribute GEOM_ATTRIBUTE_X = new SceneObjectTypeAttribute("x", SceneObjectTypeAttributeType.DOUBLE, true);
  public static final SceneObjectTypeAttribute GEOM_ATTRIBUTE_Y = new SceneObjectTypeAttribute("y", SceneObjectTypeAttributeType.DOUBLE, true);

  /**
   * SCENE IMAGE
   * Node "SceneLayer", attribute "alpha" is optional, occurrences with/without attribute: 1499/1
   * Node "SceneLayer", attribute "anim" is optional, occurrences with/without attribute: 282/1218
   * Node "SceneLayer", attribute "animdelay" is optional, occurrences with/without attribute: 23/1477
   * Node "SceneLayer", attribute "animspeed" is optional, occurrences with/without attribute: 282/1218
   * Node "SceneLayer", attribute "colorize" is optional, occurrences with/without attribute: 1499/1
   * Node "SceneLayer", attribute "context" is optional, occurrences with/without attribute: 1/1499
   * Node "SceneLayer", attribute "depth" is mandatory (1500 occurrences found)
   * Node "SceneLayer", attribute "id" is optional, occurrences with/without attribute: 97/1403
   * Node "SceneLayer", attribute "image" is mandatory (1500 occurrences found)
   * Node "SceneLayer", attribute "name" is optional, occurrences with/without attribute: 1499/1
   * Node "SceneLayer", attribute "rotation" is optional, occurrences with/without attribute: 1499/1
   * Node "SceneLayer", attribute "scalex" is optional, occurrences with/without attribute: 1499/1
   * Node "SceneLayer", attribute "scaley" is optional, occurrences with/without attribute: 1499/1
   * Node "SceneLayer", attribute "tilex" is optional, occurrences with/without attribute: 29/1471
   * Node "SceneLayer", attribute "tiley" is optional, occurrences with/without attribute: 8/1492
   * Node "SceneLayer", attribute "x" is mandatory (1500 occurrences found)
   * Node "SceneLayer", attribute "y" is mandatory (1500 occurrences found)
   */
  public static final SceneObjectTypeAttribute SCENELAYER_ATTRIBUTE_IMAGE = new SceneObjectTypeAttribute("image", SceneObjectTypeAttributeType.STRING, true);
  public static final SceneObjectTypeAttribute SCENELAYER_ATTRIBUTE_SCALEX = new SceneObjectTypeAttribute("scalex", SceneObjectTypeAttributeType.DOUBLE, false);
  public static final SceneObjectTypeAttribute SCENELAYER_ATTRIBUTE_SCALEY = new SceneObjectTypeAttribute("scaley", SceneObjectTypeAttributeType.DOUBLE, false);
  public static final SceneObjectTypeAttribute SCENELAYER_ATTRIBUTE_DEPTH = new SceneObjectTypeAttribute("depth", SceneObjectTypeAttributeType.DOUBLE, true);
  public static final SceneObjectTypeAttribute SCENELAYER_ATTRIBUTE_NAME = new SceneObjectTypeAttribute("name", SceneObjectTypeAttributeType.STRING, false);
  private static final SceneObjectTypeAttribute[] SCENELAYER_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          new SceneObjectTypeAttribute("alpha", SceneObjectTypeAttributeType.DOUBLE, false),
          new SceneObjectTypeAttribute("anim", SceneObjectTypeAttributeType.STRING, false),
          new SceneObjectTypeAttribute("animdelay", SceneObjectTypeAttributeType.DOUBLE, false),
          new SceneObjectTypeAttribute("animspeed", SceneObjectTypeAttributeType.DOUBLE, false),
          new SceneObjectTypeAttribute("colorize", SceneObjectTypeAttributeType.COLOR_RGB, false), // TODO parse rgb
          new SceneObjectTypeAttribute("context", SceneObjectTypeAttributeType.STRING, false),
          SCENELAYER_ATTRIBUTE_DEPTH,
          OPTIONAL_ATTRIBUTE_ID,
          SCENELAYER_ATTRIBUTE_IMAGE, // todo should be "resource" type
          SCENELAYER_ATTRIBUTE_NAME,
          GEOM_ATTRIBUTE_ROTATION,
          SCENELAYER_ATTRIBUTE_SCALEX,
          SCENELAYER_ATTRIBUTE_SCALEY,
          new SceneObjectTypeAttribute("tilex", SceneObjectTypeAttributeType.BOOLEAN, false),
          new SceneObjectTypeAttribute("tiley", SceneObjectTypeAttributeType.BOOLEAN, false),
          GEOM_ATTRIBUTE_X,
          GEOM_ATTRIBUTE_Y
  };

  private static final SceneObjectTypeAttribute GEOM_ATTRIBUTE_ID = new SceneObjectTypeAttribute("id", SceneObjectTypeAttributeType.STRING, true);
  /**
   * Attributes on all geometry types;
   * Node "compositegeom", attribute "id" is mandatory (64 occurrences found)
   * Node "circle", attribute "id" is mandatory (187 occurrences found)
   * Node "rectangle", attribute "id" is mandatory (650 occurrences found)
   * Node "compositegeom", attribute "image" is optional, occurrences with/without attribute: 9/55
   * Node "circle", attribute "image" is optional, occurrences with/without attribute: 20/167
   * Node "rectangle", attribute "image" is optional, occurrences with/without attribute: 129/521
   * Node "compositegeom", attribute "imagepos" is optional, occurrences with/without attribute: 9/55
   * Node "circle", attribute "imagepos" is optional, occurrences with/without attribute: 20/167
   * Node "rectangle", attribute "imagepos" is optional, occurrences with/without attribute: 129/521
   * Node "compositegeom", attribute "imagerot" is optional, occurrences with/without attribute: 9/55
   * Node "circle", attribute "imagerot" is optional, occurrences with/without attribute: 20/167
   * Node "rectangle", attribute "imagerot" is optional, occurrences with/without attribute: 119/531
   * Node "compositegeom", attribute "imagescale" is optional, occurrences with/without attribute: 9/55
   * Node "circle", attribute "imagescale" is optional, occurrences with/without attribute: 20/167
   * Node "rectangle", attribute "imagescale" is optional, occurrences with/without attribute: 119/531
   * Node "compositegeom", attribute "material" is mandatory (64 occurrences found)
   * Node "circle", attribute "material" is optional, occurrences with/without attribute: 118/69
   * Node "rectangle", attribute "material" is optional, occurrences with/without attribute: 383/267
   * Node "compositegeom", attribute "nogeomcollisions" is optional, occurrences with/without attribute: 7/57
   * Node "circle", attribute "nogeomcollisions" is optional, occurrences with/without attribute: 3/184
   * Node "rectangle", attribute "nogeomcollisions" is optional, occurrences with/without attribute: 13/637
   * Node "compositegeom", attribute "static" is mandatory (64 occurrences found)
   * Node "circle", attribute "static" is optional, occurrences with/without attribute: 118/69
   * Node "rectangle", attribute "static" is optional, occurrences with/without attribute: 399/251
   * Node "compositegeom", attribute "tag" is optional, occurrences with/without attribute: 51/13
   * Node "circle", attribute "tag" is optional, occurrences with/without attribute:69/118
   * Node "rectangle", attribute "tag" is optional, occurrences with/without attribute: 180/470
   * Node "compositegeom", attribute "x" is mandatory (64 occurrences found)
   * Node "circle", attribute "x" is mandatory (187 occurrences found)
   * Node "rectangle", attribute "x" is mandatory (650 occurrences found)
   * Node "compositegeom", attribute "y" is mandatory (64 occurrences found)
   * Node "circle", attribute "y" is mandatory (187 occurrences found)
   * Node "rectangle", attribute "y" is mandatory (650 occurrences found)
   */
  private static final SceneObjectTypeAttribute[] GEOM_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          GEOM_ATTRIBUTE_ID,
          new SceneObjectTypeAttribute("image", SceneObjectTypeAttributeType.STRING, false),
          new SceneObjectTypeAttribute("imagepos", SceneObjectTypeAttributeType.POINT_DOUBLE, false),
          new SceneObjectTypeAttribute("imagerot", SceneObjectTypeAttributeType.DOUBLE, false),
          new SceneObjectTypeAttribute("imagescale", SceneObjectTypeAttributeType.POINT_DOUBLE, false),
          new SceneObjectTypeAttribute("material", SceneObjectTypeAttributeType.STRING, false),
          new SceneObjectTypeAttribute("nogeomcollisions", SceneObjectTypeAttributeType.BOOLEAN, false),
          new SceneObjectTypeAttribute("static", SceneObjectTypeAttributeType.BOOLEAN, false),
          new SceneObjectTypeAttribute("tag", SceneObjectTypeAttributeType.STRING, false),
          GEOM_ATTRIBUTE_X,
          GEOM_ATTRIBUTE_Y
  };

  /**
   * Node "compositegeom", attribute "rotation" is mandatory (64 occurrences found)
   * Node "compositegeom", attribute "rotspeed" is optional, occurrences with/without attribute: 3/61
   * 13 attributes found
   * Node "compositegeom" have [0-5] occurrences of child tag "circle"
   * Node "compositegeom" have [0-25] occurrences of child tag "rectangle"
   */
  private static final SceneObjectTypeAttribute[] COMPOSITEGEOM_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          new SceneObjectTypeAttribute("rotation", SceneObjectTypeAttributeType.DOUBLE, true),
          new SceneObjectTypeAttribute("rotspeed", SceneObjectTypeAttributeType.DOUBLE, false),
  };

  /**
   * Node "circle", attribute "contacts" is optional, occurrences with/without attribute: 4/183
   * Node "circle", attribute "mass" is optional, occurrences with/without attribute: 29/158
   * Node "circle", attribute "radius" is mandatory (187 occurrences found)
   * Node "circle", attribute "rotspeed" is optional, occurrences with/without attribute: 20/167
   */
  public static final SceneObjectTypeAttribute CIRCLE_ATTRIBUTE_RADIUS = new SceneObjectTypeAttribute("radius", SceneObjectTypeAttributeType.DOUBLE, true);
  private static final SceneObjectTypeAttribute[] CIRCLE_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          new SceneObjectTypeAttribute("contacts", SceneObjectTypeAttributeType.BOOLEAN, false),
          new SceneObjectTypeAttribute("mass", SceneObjectTypeAttributeType.DOUBLE, false),
          CIRCLE_ATTRIBUTE_RADIUS,
          new SceneObjectTypeAttribute("rotspeed", SceneObjectTypeAttributeType.DOUBLE, false),

  };

  /**
   * Node "rectangle", attribute "collide" is optional, occurrences with/without attribute: 11/639
   * Node "rectangle", attribute "contacts" is optional, occurrences with/without attribute: 28/622
   * Node "rectangle", attribute "mass" is optional, occurrences with/without attribute: 142/508
   * Node "rectangle", attribute "rotation" is optional, occurrences with/without attribute: 636/14
   * Node "rectangle", attribute "rotspeed" is optional, occurrences with/without attribute: 3/647
   * Node "rectangle", attribute "width" is mandatory (650 occurrences found)
   * Node "rectangle", attribute "height" is mandatory (650 occurrences found)
   * 18 attributes found
   */
  public static final SceneObjectTypeAttribute RECTANGLE_ATTRIBUTE_WIDTH = new SceneObjectTypeAttribute("width", SceneObjectTypeAttributeType.DOUBLE, true);
  public static final SceneObjectTypeAttribute RECTANGLE_ATTRIBUTE_HEIGHT = new SceneObjectTypeAttribute("height", SceneObjectTypeAttributeType.DOUBLE, true);
  private static final SceneObjectTypeAttribute[] RECTANGLE_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          new SceneObjectTypeAttribute("collide", SceneObjectTypeAttributeType.BOOLEAN, false),
          new SceneObjectTypeAttribute("contacts", SceneObjectTypeAttributeType.BOOLEAN, false),
          new SceneObjectTypeAttribute("mass", SceneObjectTypeAttributeType.DOUBLE, false),
          GEOM_ATTRIBUTE_ROTATION,
          new SceneObjectTypeAttribute("rotspeed", SceneObjectTypeAttributeType.DOUBLE, false),
          RECTANGLE_ATTRIBUTE_WIDTH,
          RECTANGLE_ATTRIBUTE_HEIGHT,
  };

  public static final SceneObjectTypeAttribute ATTRIBUTE_ANCHOR = new SceneObjectTypeAttribute("anchor", SceneObjectTypeAttributeType.POINT_DOUBLE, true);
  /**
   * Node "line", attribute "anchor" is mandatory (137 occurrences found)
   * Node "line", attribute "id" is mandatory (137 occurrences found)
   * Node "line", attribute "material" is mandatory (137 occurrences found)
   * Node "line", attribute "normal" is mandatory (137 occurrences found)
   * Node "line", attribute "static" is mandatory (137 occurrences found)
   * Node "line", attribute "tag" is optional, occurrences with/without attribute: 96/41
   */
  public static final SceneObjectTypeAttribute LINE_ATTRIBUTE_NORMAL = new SceneObjectTypeAttribute("normal", SceneObjectTypeAttributeType.POINT_DOUBLE, true);
  private static final SceneObjectTypeAttribute[] LINE_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          ATTRIBUTE_ANCHOR,
          GEOM_ATTRIBUTE_ID,
          new SceneObjectTypeAttribute("material", SceneObjectTypeAttributeType.STRING, true),
          LINE_ATTRIBUTE_NORMAL,
          new SceneObjectTypeAttribute("static", SceneObjectTypeAttributeType.BOOLEAN, true),
          new SceneObjectTypeAttribute("tag", SceneObjectTypeAttributeType.STRING, false),
  };

  /**
   * Node "hinge", attribute "anchor" is mandatory (92 occurrences found)
   * Node "hinge", attribute "body1" is mandatory (92 occurrences found)
   * Node "hinge", attribute "body2" is optional, occurrences with/without attribute: 51/41
   * Node "hinge", attribute "bounce" is optional, occurrences with/without attribute: 11/81
   * Node "hinge", attribute "histop" is optional, occurrences with/without attribute: 12/80
   * Node "hinge", attribute "id" is optional, occurrences with/without attribute: 11/81
   * Node "hinge", attribute "lostop" is optional, occurrences with/without attribute: 12/80
   */
  private static final SceneObjectTypeAttribute[] HINGE_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          ATTRIBUTE_ANCHOR,
          new SceneObjectTypeAttribute("body1", SceneObjectTypeAttributeType.STRING, true),
          new SceneObjectTypeAttribute("body2", SceneObjectTypeAttributeType.STRING, false),
          new SceneObjectTypeAttribute("bounce", SceneObjectTypeAttributeType.DOUBLE, false),
          new SceneObjectTypeAttribute("histop", SceneObjectTypeAttributeType.INT, false),
          new SceneObjectTypeAttribute("lostop", SceneObjectTypeAttributeType.INT, false),
          new SceneObjectTypeAttribute("id", SceneObjectTypeAttributeType.STRING, false)
  };


  /**
   * Node "buttongroup", attribute "id" is mandatory (16 occurrences found)
   * Node "buttongroup", attribute "osx" is mandatory (16 occurrences found)
   * 2 attributes found
   * Node "buttongroup" have [1-12] occurrences of child tag "button"
   * 1 child tags found for node "buttongroup"
   */
  private static final SceneObjectTypeAttribute[] BUTTONGROUP_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          new SceneObjectTypeAttribute("id", SceneObjectTypeAttributeType.STRING, true),
          new SceneObjectTypeAttribute("osx", SceneObjectTypeAttributeType.POINT_DOUBLE, true),
  };

  /**
   * Node "button", attribute "alpha" is optional, occurrences with/without attribute: 70/7
   * Node "button", attribute "anchor" is optional, occurrences with/without attribute: 1/76
   * Node "button", attribute "armed" is optional, occurrences with/without attribute: 2/75
   * Node "button", attribute "colorize" is optional, occurrences with/without attribute: 70/7
   * Node "button", attribute "context" is optional, occurrences with/without attribute: 2/75
   * Node "button", attribute "depth" is mandatory (77 occurrences found)
   * Node "button", attribute "disabled" is optional, occurrences with/without attribute: 6/71
   * Node "button", attribute "down" is optional, occurrences with/without attribute: 1/76
   * Node "button", attribute "downarmed" is optional, occurrences with/without attribute: 1/76
   * Node "button", attribute "downover" is optional, occurrences with/without attribute: 1/76
   * Node "button", attribute "font" is optional, occurrences with/without attribute: 8/69
   * Node "button", attribute "id" is mandatory (77 occurrences found)
   * Node "button", attribute "latch" is optional, occurrences with/without attribute: 1/76
   * Node "button", attribute "onclick" is mandatory (77 occurrences found)
   * Node "button", attribute "onmouseenter" is optional, occurrences with/without attribute: 59/18
   * Node "button", attribute "onmouseexit" is optional, occurrences with/without attribute: 59/18
   * Node "button", attribute "over" is optional, occurrences with/without attribute: 76/1
   * Node "button", attribute "overlay" is optional, occurrences with/without attribute: 6/71
   * Node "button", attribute "rotation" is optional, occurrences with/without attribute: 75/2
   * Node "button", attribute "scalex" is mandatory (77 occurrences found)
   * Node "button", attribute "scaley" is mandatory (77 occurrences found)
   * Node "button", attribute "screenspace" is optional, occurrences with/without attribute: 3/74
   * Node "button", attribute "text" is optional, occurrences with/without attribute: 8/69
   * Node "button", attribute "textcolorup" is optional, occurrences with/without attribute: 1/76
   * Node "button", attribute "textcolorupover" is optional, occurrences with/without attribute: 1/76
   * Node "button", attribute "tooltip" is optional, occurrences with/without attribute: 3/74
   * Node "button", attribute "up" is mandatory (77 occurrences found)
   * Node "button", attribute "visible" is optional, occurrences with/without attribute: 1/76
   * Node "button", attribute "x" is mandatory (77 occurrences found)
   * Node "button", attribute "y" is mandatory (77 occurrences found)
   * 30 attributes found
   */
  private static final SceneObjectTypeAttribute[] BUTTON_ATTRIBUTES = new SceneObjectTypeAttribute[]{
          //TODO
  };

  /**
   * Node "scene" have [0-86] occurrences of child tag "SceneLayer" done
   * Node "scene" have [0-3] occurrences of child tag "button"
   * Node "scene" have [0-2] occurrences of child tag "buttongroup"
   * Node "scene" have [0-15] occurrences of child tag "circle" done
   * Node "scene" have [0-5] occurrences of child tag "compositegeom" done
   * Node "scene" have [0-17] occurrences of child tag "hinge" done
   * Node "scene" have [0-6] occurrences of child tag "label"
   * Node "scene" have [0-4] occurrences of child tag "line" done
   * Node "scene" have [0-19] occurrences of child tag "linearforcefield" done
   * Node "scene" have [0-1] occurrences of child tag "motor"
   * Node "scene" have [0-10] occurrences of child tag "particles" done
   * Node "scene" have [0-3] occurrences of child tag "radialforcefield" done
   * Node "scene" have [0-75] occurrences of child tag "rectangle" done
   */
  private static final Map<String, SceneObjectType> SCENE_OBJECT_TYPES = new HashMap<String, SceneObjectType>();

  private static final SceneObjectType FORCEFIELD_SUPERCLASS = new SceneObjectType("xxsc_forcefield", FORCEFIELD_ATTRIBUTES, null, false);
  public static SceneObjectType RADIALFORCEFIELD = new SceneObjectType("radialforcefield", RADIALFORCEFIELD_ATTRIBUTES, FORCEFIELD_SUPERCLASS, false);
  public static final SceneObjectType LINEARFORCEFIELD = new SceneObjectType("linearforcefield", LINEARFORCEFIELD_ATTRIBUTES, FORCEFIELD_SUPERCLASS, false);

  public static final SceneObjectType PARTICLES = new SceneObjectType("particles", PARTICLES_ATTRIBUTES, null, false);

  public static final SceneObjectType SCENELAYER = new SceneObjectType("SceneLayer", SCENELAYER_ATTRIBUTES, null, false);

  private static final SceneObjectType GEOM_SUPERCLASS = new SceneObjectType("xxsc_geom", GEOM_ATTRIBUTES, null, false);
  public static final SceneObjectType COMPOSITEGEOM = new SceneObjectType("compositegeom", COMPOSITEGEOM_ATTRIBUTES, GEOM_SUPERCLASS, true);
  public static final SceneObjectType CIRCLE = new SceneObjectType("circle", CIRCLE_ATTRIBUTES, GEOM_SUPERCLASS, false);
  public static final SceneObjectType RECTANGLE = new SceneObjectType("rectangle", RECTANGLE_ATTRIBUTES, GEOM_SUPERCLASS, false);

  public static final SceneObjectType LINE = new SceneObjectType("line", LINE_ATTRIBUTES, null, false);

  public static final SceneObjectType HINGE = new SceneObjectType("hinge", HINGE_ATTRIBUTES, null, false);

  public static final SceneObjectType BUTTONGROUP = new SceneObjectType("buttongroup", BUTTONGROUP_ATTRIBUTES, null, true);
  public static final SceneObjectType BUTTON = new SceneObjectType("button", BUTTON_ATTRIBUTES, null, false);

  static {
    addSceneObjectType(RADIALFORCEFIELD);
    addSceneObjectType(LINEARFORCEFIELD);
    addSceneObjectType(PARTICLES);
    addSceneObjectType(SCENELAYER);
    addSceneObjectType(COMPOSITEGEOM);
    addSceneObjectType(CIRCLE);
    addSceneObjectType(RECTANGLE);
    addSceneObjectType(LINE);
    addSceneObjectType(HINGE);
  }

  private static void addSceneObjectType(SceneObjectType type)
  {
    SCENE_OBJECT_TYPES.put(type.getXmlElementName(), type);
  }

  public static SceneObjectType getSceneObjectTypeByName(String name)
  {
    return SCENE_OBJECT_TYPES.get(name);
  }


  private final String xmlElementName;
  private final SceneObjectTypeAttribute[] attributes;
  private final SceneObjectType superclass;
  private final boolean canHaveChildren;

  private SceneObjectType(String xmlElementName, SceneObjectTypeAttribute[] attributes, SceneObjectType superclass, boolean canHaveChildren)
  {
    this.xmlElementName = xmlElementName;
    this.attributes = attributes;
    this.superclass = superclass;
    this.canHaveChildren = canHaveChildren;
  }

  public String getXmlElementName()
  {
    return xmlElementName;
  }

  public SceneObjectTypeAttribute[] getAttributes()
  {
    return attributes;
  }

  public SceneObjectType getSuperclass()
  {
    return superclass;
  }

  public boolean isCanHaveChildren()
  {
    return canHaveChildren;
  }

  @Override
  @SuppressWarnings({"StringConcatenation"})
  public String toString()
  {
    return "SceneObjectType{" +
            "xmlElementName='" + xmlElementName + '\'' +
            '}';
  }
}
