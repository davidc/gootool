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

  public BufferedImage getImageInState(String state) throws IOException
  {
    BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics2D g2 = img.createGraphics();

    int xOffset = 25;
    int yOffset = 25;

    for (BallPart part : parts) {
      if (part.isPartActiveInState(state)) {
      part.draw(g2, xOffset, yOffset);
      }
    }

    drawPoint(g2, xOffset, yOffset, Color.ORANGE);
    return img;
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

    DebugUtil.showImageWindow(new Ball("common").getImageInState("standing"));
    DebugUtil.showImageWindow(new Ball("common").getImageInState("attached"));
    DebugUtil.showImageWindow(new Ball("Ivy").getImageInState("standing"));
  }
}
