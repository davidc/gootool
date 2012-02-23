/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.resource;

import java.util.*;
import java.util.List;
import java.io.IOException;
import java.awt.*;

import org.w3c.dom.Node;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootoolsp.leveledit.model.Resources;

/**
 * <part name="body"
 * layer="0"
 * x="0" y="0"
 * stretch="16,2,0.5"
 * image="IMAGE_BALL_POKEY_BODY"
 * scale="0.65"
 * rotate="true"
 * state="climbing,walking,falling,dragging,detaching,standing,tank,sleeping,stuck,stuck_attached,stuck_detaching,pipe"
 * />
 * <p/>
 * <part name="lefteye"
 * layer="2"
 * rotate="true"
 * eye="true"
 * pupil="IMAGE_BALL_GENERIC_PUPIL1"
 * pupilinset="12"
 * x="-12,-8" y="-5,5"
 * xrange="-18,0" yrange="-12,12"
 * image="IMAGE_BALL_GENERIC_EYE_GLASS_1,IMAGE_BALL_GENERIC_EYE_GLASS_2,IMAGE_BALL_GENERIC_EYE_GLASS_3"
 * state="climbing,walking,falling,dragging,detaching,standing,tank"
 * scale="0.5"
 * />
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BallPart
{
  private final Resources resources;

  private final String name;
  private final int layer;
  private final double x1, x2, y1, y2; // xrange, range
  private final List<String> imageRes;
  private final Image mainImage;
  private final boolean rotate;
  private final Set<String> states;
  private final double scale;

  private final boolean isEye;
  private final String pupilRes;
  private final Image pupilImage;
  private final int pupilInset;

  // TODO stretch

  public BallPart(Node partNode, Resources resources) throws IOException
  {
    this.resources = resources;

    name = XMLUtil.getAttributeStringRequired(partNode, "name");
    layer = XMLUtil.getAttributeIntegerRequired(partNode, "layer");

    String x = XMLUtil.getAttributeStringRequired(partNode, "x");
    String[] bits = x.split(",", 2);
    x1 = Double.parseDouble(bits[0]);
    if (bits.length > 1) x2 = Double.parseDouble(bits[1]);
    else x2 = x1;

    String y = XMLUtil.getAttributeStringRequired(partNode, "y");
    bits = y.split(",", 2);
    y1 = Double.parseDouble(bits[0]);// todo catch NFE
    if (bits.length > 1) y2 = Double.parseDouble(bits[1]);
    else y2 = y1;

    String imageResStr = XMLUtil.getAttributeStringRequired(partNode, "image");
    imageRes = new ArrayList<String>();
    StringTokenizer tok = new StringTokenizer(imageResStr, ",");
    while (tok.hasMoreTokens()) {
      imageRes.add(tok.nextToken());
    }

    scale = XMLUtil.getAttributeDoubleRequired(partNode, "scale");
    rotate = XMLUtil.getAttributeBoolean(partNode, "rotate", false);

    isEye = XMLUtil.getAttributeBoolean(partNode, "eye", false);

    if (isEye) {
      pupilRes = XMLUtil.getAttributeStringRequired(partNode, "pupil");
      pupilInset = XMLUtil.getAttributeIntegerRequired(partNode, "pupilinset");
    }
    else {
      pupilRes = null;
      pupilInset = 0;
    }

    String statesStr = XMLUtil.getAttributeString(partNode, "state", null);
    if (statesStr != null) {
      states = new TreeSet<String>();
      tok = new StringTokenizer(statesStr, ",");
      while (tok.hasMoreTokens()) {
        String state = tok.nextToken();
        states.add(state);
      }
    }
    else {
      states = null;
    }

    /* We need to preload all the images since we can't be throwing IOExceptions in our paint methods */
    mainImage = getResourceImage(imageRes.get(0));
    if (isEye) {
      pupilImage = getResourceImage(pupilRes);
    }
    else {
      pupilImage = null;
    }
  }

  public boolean isPartActiveInState(String state)
  {
    return states == null || states.contains(state);
  }

  // TODO draw at higher scale?
  public void draw(Graphics2D g2, Point offset, double drawScale)
  {
    int xCentre = (int) (offset.x + (drawScale * ((x1 + x2) / 2)));
    int yCentre = (int) (offset.y - (drawScale * ((y1 + y2) / 2)));

    drawImage(g2, mainImage, xCentre, yCentre, drawScale);

    if (isEye) {
      drawImage(g2, pupilImage, xCentre, yCentre, drawScale);
    }
  }

  private void drawImage(Graphics2D g2, Image image, int xCentre, int yCentre, double drawScale)
  {
    int width = (int) (image.getWidth(null) * scale * drawScale);
    int height = (int) (image.getHeight(null) * scale * drawScale);

//    System.out.println("image " + imageResName + " width=" + width + " height=" + height + ", scale = " + scale);

    int x = xCentre - (width / 2);
    int y = yCentre - (height / 2);

    g2.drawImage(image, x, y, width, height, null);
  }

  private Image getResourceImage(String imageResName) throws IOException
  {
    Image image = resources.getImage(imageResName);
    if (image == null) image = Resources.getGlobalResources().getImage(imageResName);
    if (image == null) throw new IOException("No image found for resource " + imageResName);
    return image;
  }

  public Ball.Bounds getBounds()
  {
    int width = (int) (mainImage.getWidth(null) * scale);
    int height = (int) (mainImage.getHeight(null) * scale);

    Ball.Bounds b = new Ball.Bounds();

    double x = (x1 + x2) / 2;
    b.minx = x - (width / 2);
    b.maxx = x + (width / 2);

    double y = (y1 + y2) / 2;
    b.miny = y - (height / 2);
    b.maxy = y + (height / 2);

    return b;
  }


  public static class LayerComparator implements Comparator<BallPart>
  {
    public int compare(BallPart o1, BallPart o2)
    {
      if (o1.layer < o2.layer)
        return -1;
      else if (o1.layer < o2.layer)
        return 1;
      else return 0;
    }
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    BallPart ballPart = (BallPart) o;

    return name.equals(ballPart.name);
  }

  @Override
  public int hashCode()
  {
    return name.hashCode();
  }

  @SuppressWarnings({"StringConcatenation"})
  @Override
  public String toString()
  {
    return "BallPart{" +
            "name='" + name + '\'' +
            ", layer=" + layer +
            ", x1=" + x1 +
            ", x2=" + x2 +
            ", y1=" + y1 +
            ", y2=" + y2 +
            ", imageRes=" + imageRes +
            ", rotate=" + rotate +
            ", states=" + states +
            ", scale=" + scale +
            ", isEye=" + isEye +
            ", pupilRes='" + pupilRes + '\'' +
            ", pupilInset=" + pupilInset +
            '}';
  }
}
