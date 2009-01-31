package com.goofans.gootool.leveledit.resource;

import java.util.*;
import java.util.List;
import java.io.IOException;
import java.awt.*;

import org.w3c.dom.Node;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.leveledit.model.Resources;

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
  private Resources resources;

  private String name;
  private int layer;
  private double x1, x2, y1, y2; // xrange, range
  private List<String> imageRes;
  private boolean rotate;
  private Set<String> states;
  private double scale;

  private boolean isEye;
  private String pupilRes;
  private int pupilInset;

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

    String statesStr = XMLUtil.getAttributeString(partNode, "state", null);
    if (statesStr != null) {
      states = new TreeSet<String>();
      tok = new StringTokenizer(statesStr, ",");
      while (tok.hasMoreTokens()) {
        String state = tok.nextToken();
        states.add(state);
      }
    }
  }

  public boolean isPartActiveInState(String state)
  {
    return states == null || states.contains(state);
  }

  // TODO draw at higher scale?
  public void draw(Graphics2D g2, int xOffset, int yOffset, double drawScale) throws IOException
  {
    String imageResName = imageRes.get(0);

    int xCentre = (int) (xOffset + (drawScale * ((x1 + x2) / 2)));
    int yCentre = (int) (yOffset - (drawScale * ((y1 + y2) / 2)));

    drawImage(g2, imageResName, xCentre, yCentre, drawScale);

    if (isEye) {
      drawImage(g2, pupilRes, xCentre, yCentre, drawScale);
    }
  }

  private void drawImage(Graphics2D g2, String imageResName, int xCentre, int yCentre, double drawScale) throws IOException
  {
    Image image = getResourceImage(imageResName);
    int width = (int) (image.getWidth(null) * scale * drawScale);
    int height = (int) (image.getHeight(null) * scale * drawScale);

    System.out.println("image " + imageResName + " width=" + width + " height=" + height + ", scale = " + scale);

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

  public Ball.Bounds getBounds() throws IOException
  {
    String imageResName = imageRes.get(0);
    Image image = getResourceImage(imageResName);
    int width = (int) (image.getWidth(null) * scale);
    int height = (int) (image.getHeight(null) * scale);

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

    if (!name.equals(ballPart.name)) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    return name.hashCode();
  }

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
