package com.goofans.gootool.leveledit.resource;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.leveledit.model.Resources;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.wog.WorldOfGoo;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Ball
{
  private String ballName;
  private File ballDir;
  private Resources resources;

  // TODO shape for bounding box check

  private List<BallPart> parts;

  public Ball(String ballName) throws IOException
  {
    this.ballName = ballName;
    ballDir = WorldOfGoo.getTheInstance().getCustomGameFile("res/balls/" + ballName);
    if (!ballDir.isDirectory()) {
      throw new IOException("Ball dir " + ballDir + " doesn't exist");
    }

    Document resDoc = GameFormat.decodeXmlBinFile(new File(ballDir, "resources.xml.bin"));
    resources = new Resources(resDoc);

    Document ballDoc = GameFormat.decodeXmlBinFile(new File(ballDir, "balls.xml.bin"));

    if (!ballName.equals(XMLUtil.getAttributeStringRequired(ballDoc.getDocumentElement(), "name"))) {
      throw new IOException("Ball name in xml doc doesn't equal ball dir");
    }

    parts = new LinkedList<BallPart>();

    NodeList partNodes = ballDoc.getElementsByTagName("part");
    for (int i = 0; i < partNodes.getLength(); i++) {
      parts.add(new BallPart(partNodes.item(i), resources));
    }
    Collections.sort(parts, new BallPart.LayerComparator());
  }

  public BufferedImage getImageInState(String state, Dimension imgSize) throws IOException
  {
    BufferedImage img = new BufferedImage(imgSize.width, imgSize.height, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics2D g2 = img.createGraphics();

    int xOffset = imgSize.width / 2;
    int yOffset = imgSize.height / 2;

    double scale = 1.0d;

    Bounds bounds = getBoundsInState(state);

    System.out.println("bounds.minx = " + bounds.minx);
    System.out.println("bounds.maxx = " + bounds.maxx);
    System.out.println("bounds.miny = " + bounds.miny);
    System.out.println("bounds.maxy = " + bounds.maxy);

    System.out.println("xOffset = " + xOffset);
    System.out.println("yOffset = " + yOffset);

//    if ((bounds.getWidth() * scale) > imgSize.width) {
//      scale = imgSize.width / bounds.getWidth();
//    }
//    if ((bounds.getHeight() * scale) > imgSize.height) {
//      scale = imgSize.height / bounds.getHeight();
//    }

    System.out.println("bounds.getWidth() = " + bounds.getWidth());
    System.out.println("bounds.getHeight() = " + bounds.getHeight());
    scale = Math.min(1.0d, Math.min(imgSize.width / bounds.getWidth(), imgSize.height / bounds.getHeight()));
    System.out.println("scale = " + scale);

    xOffset += (int) (scale * ((bounds.minx + bounds.maxx) / 2));
    yOffset += (int) (scale * ((bounds.miny + bounds.maxy) / 2));


    for (BallPart part : parts) {
      if (part.isPartActiveInState(state)) {
        part.draw(g2, xOffset, yOffset, scale);
      }
    }

    drawPoint(g2, xOffset, yOffset, Color.ORANGE);
    return img;
  }

  public Bounds getBoundsInState(String state) throws IOException
  {
    BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics2D g2 = img.createGraphics();

    Bounds b = new Bounds();

    for (BallPart part : parts) {
      if (part.isPartActiveInState(state)) {
        Bounds b2 = part.getBounds();
        b.minx = Math.min(b.minx, b2.minx);
        b.maxx = Math.max(b.maxx, b2.maxx);
        b.miny = Math.min(b.miny, b2.miny);
        b.maxy = Math.max(b.maxy, b2.maxy);
      }
    }

    return b;
  }

  public static class Bounds
  {
    public double minx, miny, maxx, maxy;

    public double getWidth()
    {
      return maxx - minx;
    }

    public double getHeight()
    {
      return maxy - miny;
    }
  }

  private void drawPoint(Graphics2D g, int x, int y, Color color)
  {
    g.setColor(color);
    g.setStroke(new BasicStroke(1));
    g.drawLine(x - 2, y - 2, x + 2, y + 2);
    g.drawLine(x + 2, y - 2, x - 2, y + 2);
//    g.drawRect(x, y, 0, 0);
  }

  public static void main(String[] args) throws IOException
  {
    WorldOfGoo.getTheInstance().init();

//    DebugUtil.showImageWindow(new Ball("common").getImageInState("standing", new Dimension(20, 20)));
//    DebugUtil.showImageWindow(new Ball("common").getImageInState("attached", new Dimension(20, 20)));
//    DebugUtil.showImageWindow(new Ball("RectHead").getImageInState("standing", new Dimension(20, 20)));
    DebugUtil.showImageWindow(new Ball("IconWindowRect").getImageInState("standing", new Dimension(50, 50)));
    DebugUtil.showImageWindow(new Ball("IconWindowRect").getImageInState("standing", new Dimension(500, 500)));
  }
}
