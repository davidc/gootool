package com.goofans.gootool.leveledit.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;
import java.util.List;

import com.goofans.gootool.leveledit.model.*;

/**
 * @author David Croft (david.croft@infotrek.net)
 * @version $Id$
 */
public class LevelDisplay extends JPanel// implements Scrollable
{
  private Level level;
  private Scene scene;
  private Resources resources;

  private double scale = 0.5;

  public LevelDisplay()
  {
    addMouseWheelListener(new MouseWheelListener()
    {
      public void mouseWheelMoved(MouseWheelEvent e)
      {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
          scale = (scale - (e.getWheelRotation() * 0.1));
          if (scale < 0.1) scale = 0.1;
          if (scale > 10) scale = 10;
          System.out.println("          e.getWheelRotation(); = " + e.getWheelRotation());
          // TODO center at mouse position
          repaint();
        }
      }
    });
  }

  public void setLevel(Level level)
  {
    this.level = level;
    this.scene = level.getScene();
    this.resources = level.getResources();
  }

  @Override
  public void paint(Graphics g1)
  {
    super.paint(g1);
    Graphics2D g = (Graphics2D) g1;

    int width = getWidth();
    int height = getHeight();

    if (scene == null) {
      g.setColor(Color.RED);
      g.drawString("null scene", 10, 20);
      return;
    }


    g.setColor(Color.YELLOW);
    g.drawRect(0, 0, width - 1, height - 1);


    System.out.println("scene = " + scene);

    if (showImages) drawSceneLayers(g);
    if (showGeometry) drawGeometry(g);
    if (showBoundaries) drawLines(g);
  }

  private void drawSceneLayers(Graphics2D g)
  {
    // Order them by layer TODO don't do this on every repaint!!!

//    Arrays.sort();
    List<SceneObject> sceneLayerObjects = new LinkedList<SceneObject>();
    for (SceneObject sceneObject : scene.getSceneObjects()) {
      if (sceneObject.getType() == SceneObjectType.SCENELAYER) {
        sceneLayerObjects.add(sceneObject);
      }
    }

    Collections.sort(sceneLayerObjects, new DepthSorter());

    for (SceneObject sceneObject : sceneLayerObjects) {
//    for (SceneObject sceneObject : scene.getSceneObjects()) {
      drawSceneLayer(g, sceneObject);
    }
  }

  private void drawGeometry(Graphics2D g)
  {
    Point2D.Double offset = new Point2D.Double(0, 0);
    for (SceneObject sceneObject : scene.getSceneObjects()) {
      drawGeometryObject(g, sceneObject, offset);
    }
  }

  private void drawGeometryObject(Graphics2D g, SceneObject sceneObject, Point2D.Double offset)
  {
    if (sceneObject.getType() == SceneObjectType.COMPOSITEGEOM) {
      drawCompositeGeom(g, sceneObject, offset);
    }
    else if (sceneObject.getType() == SceneObjectType.RECTANGLE) {
      drawRectangle(g, sceneObject, offset);
    }
    else if (sceneObject.getType() == SceneObjectType.CIRCLE) {
      drawCircle(g, sceneObject, offset);
    }
    else {
//      System.out.println("Don't know how to draw " + sceneObject);
    }
  }

  private void drawCompositeGeom(Graphics2D g, SceneObject sceneObject, Point2D.Double offset)
  {
    double x = (Double) sceneObject.getValue(SceneObjectType.GEOM_ATTRIBUTE_X);
    double y = (Double) sceneObject.getValue(SceneObjectType.GEOM_ATTRIBUTE_Y);

    Point2D.Double newOffset = new Point2D.Double(offset.x + x, offset.y + y);

    // TODO rotation of all children?
    for (SceneObject childObject : sceneObject.getChildObjects()) {
      drawGeometryObject(g, childObject, newOffset);
    }
  }

  public double getScale()
  {
    return scale;
  }

  public void setScale(double scale)
  {
    this.scale = scale;
    repaint();
  }

  private void drawRectangle(Graphics2D g, SceneObject sceneObject, Point2D.Double offset)
  {
    int x = worldToCanvasX(offset.x + (Double) sceneObject.getValue(SceneObjectType.GEOM_ATTRIBUTE_X));
    int y = worldToCanvasY(offset.y + (Double) sceneObject.getValue(SceneObjectType.GEOM_ATTRIBUTE_Y));
    int width = worldToCanvasScaleX((Double) sceneObject.getValue(SceneObjectType.RECTANGLE_ATTRIBUTE_WIDTH));
    int height = worldToCanvasScaleY((Double) sceneObject.getValue(SceneObjectType.RECTANGLE_ATTRIBUTE_HEIGHT));


    Double rotation = (Double) sceneObject.getValue(SceneObjectType.GEOM_ATTRIBUTE_ROTATION);
//    System.out.println("x = " + x);
//    System.out.println("y = " + y);
//    System.out.println("width = " + width);
//    System.out.println("height = " + height);

    drawPoint(g, x, y, Color.GREEN);

    g.setStroke(new BasicStroke(2));
    g.setColor(Color.BLUE);

    if (rotation != null) {
      AffineTransform identity = g.getTransform();
      g.rotate(-rotation, x, y);

      g.drawRect(x - (width / 2), y - (height / 2), width, height);

      g.setTransform(identity);
    }
    else {
      g.drawRect(x, y, width, height);
    }
  }

  private void drawSceneLayer(Graphics2D g, SceneObject sceneObject)
  {
    String name = (String) sceneObject.getValue(SceneObjectType.SCENELAYER_ATTRIBUTE_NAME);
    System.out.println("name = " + name);
//    if (!sceneObject.getValue(SceneObjectType.SCENELAYER_ATTRIBUTE_NAME).equals("signpostPole_brown")) return;

    int x = worldToCanvasX((Double) sceneObject.getValue(SceneObjectType.GEOM_ATTRIBUTE_X));
    int y = worldToCanvasY((Double) sceneObject.getValue(SceneObjectType.GEOM_ATTRIBUTE_Y));

    String imageId = (String) sceneObject.getValue(SceneObjectType.SCENELAYER_ATTRIBUTE_IMAGE);
    Image image;
    try {
      image = resources.getImage(imageId);
    }
    catch (IOException e) {
      // TODO draw invalid image here
      e.printStackTrace();
      return;
    }

    int imgWidth = image.getWidth(this);
    int imgHeight = image.getHeight(this);

    int width = worldToCanvasScaleX(imgWidth * (Double) sceneObject.getValue(SceneObjectType.SCENELAYER_ATTRIBUTE_SCALEX));
    int height = worldToCanvasScaleY(imgHeight * (Double) sceneObject.getValue(SceneObjectType.SCENELAYER_ATTRIBUTE_SCALEY));

    // TODO           new SceneObjectTypeAttribute("alpha", SceneObjectTypeAttributeType.DOUBLE, false),
// TODO           new SceneObjectTypeAttribute("colorize", SceneObjectTypeAttributeType.COLOR_RGB, false), // TODO parse rgb
//TODo           new SceneObjectTypeAttribute("depth", SceneObjectTypeAttributeType.DOUBLE, true),
//    new SceneObjectTypeAttribute("scalex", SceneObjectTypeAttributeType.DOUBLE, false),
//    new SceneObjectTypeAttribute("scaley", SceneObjectTypeAttributeType.DOUBLE, false),
//    new SceneObjectTypeAttribute("tilex", SceneObjectTypeAttributeType.BOOLEAN, false),
//    new SceneObjectTypeAttribute("tiley", SceneObjectTypeAttributeType.BOOLEAN, false),

    // TODO           GEOM_ATTRIBUTE_ROTATION,
    Double rotation = (Double) sceneObject.getValue(SceneObjectType.GEOM_ATTRIBUTE_ROTATION);

    AffineTransform identity = g.getTransform();

    System.out.println("drawing " + imageId + "(" + image + ") at " + x + "," + y);

// This time rotation is in degrees!
    g.rotate(-(rotation * (Math.PI * 2) / 360), x, y);

    g.drawImage(image, x - (width / 2), y - (height / 2), x + (width / 2), y + (height / 2), 0, 0, imgWidth, imgHeight, this);

    g.setTransform(identity);

    drawPoint(g, x, y, Color.BLACK);
  }

  private void drawCircle(Graphics2D g, SceneObject sceneObject, Point2D.Double offset)
  {
//    System.out.println("sceneObject = " + sceneObject);
    int x = worldToCanvasX(offset.x + (Double) sceneObject.getValue(SceneObjectType.GEOM_ATTRIBUTE_X));
    int y = worldToCanvasY(offset.y + (Double) sceneObject.getValue(SceneObjectType.GEOM_ATTRIBUTE_Y));
    Double radius = (Double) sceneObject.getValue(SceneObjectType.CIRCLE_ATTRIBUTE_RADIUS);
    int radiusX = worldToCanvasScaleX(radius);
    int radiusY = worldToCanvasScaleY(radius);
//    System.out.println("x = " + x);
//    System.out.println("y = " + y);
//    System.out.println("radiusX = " + radiusX);
//    System.out.println("radiusY = " + radiusY);

    drawPoint(g, x, y, Color.RED);

    g.setStroke(new BasicStroke(2));
    g.setColor(Color.BLUE);
    g.drawOval(x - radiusX, y - radiusY, radiusX * 2, radiusY * 2);
  }


  private void drawLines(Graphics2D g)
  {
    for (SceneObject sceneObject : scene.getSceneObjects()) {
      if (sceneObject.getType() == SceneObjectType.LINE) {
        drawLine(g, sceneObject);
      }
    }
  }

  private void drawLine(Graphics2D g, SceneObject sceneObject)
  {
    Point2D.Double anchor = (Point2D.Double) sceneObject.getValue(SceneObjectType.LINE_ATTRIBUTE_ANCHOR);
    Point2D.Double normal = (Point2D.Double) sceneObject.getValue(SceneObjectType.LINE_ATTRIBUTE_NORMAL);
    int x = worldToCanvasX(anchor.getX());
    int y = worldToCanvasY(anchor.getY());
    drawPoint(g, x, y, Color.RED);

    System.out.println("anchor = " + anchor);
    System.out.println("normal = " + normal);

    g.setStroke(new BasicStroke(2));
    g.setColor(Color.ORANGE);

    int x1 = (int) (x - (normal.y * 1000));
    int x2 = (int) (x + (normal.y * 1000));
    int y1 = (int) (y - (normal.x * 1000));
    int y2 = (int) (y + (normal.x * 1000));

    g.drawLine(x1, y1, x2, y2);
  }

  private void drawPoint(Graphics2D g, int x, int y, Color color)
  {
    g.setColor(color);
    g.setStroke(new BasicStroke(1));
    g.drawLine(x - 2, y - 2, x + 2, y + 2);
    g.drawLine(x + 2, y - 2, x - 2, y + 2);
//    g.drawRect(x, y, 0, 0);
  }


  /*
   * Converts an X-coordinate in world units to the actual x location we should display it on our canvas.
   */
  private int worldToCanvasX(double x)
  {
    // Let's say 0,0 top left is the scene's minX
    x -= scene.getMinX();

    // Now scale it
    x = worldToCanvasScaleX(x);

    return (int) x;
  }

  /*
  * Converts an Y-coordinate in world units to the actual y location we should display it on our canvas.
  */
  private int worldToCanvasY(double y)
  {
    // Let's say 0,0 top left is the scene's maxY
    y = scene.getMaxY() - y;

    // Now scale it
    y = worldToCanvasScaleY(y);
    return (int) y;
  }


  /*
  * Converts an X width in world units to width on our canvas
  */
  private int worldToCanvasScaleX(double x)
  {
    return (int) (x * scale);
  }

  /*
  * Converts a Y height in world units to height on our canvas
  */
  private int worldToCanvasScaleY(double y)
  {
    return (int) (y * scale);
  }

  private boolean showImages = true, showGeometry = true, showBoundaries = true;

  public void setLayerVisibility(boolean images, boolean geometry, boolean boundaries)
  {
    showImages = images;
    showGeometry = geometry;
    showBoundaries = boundaries;
    repaint();
  }

  private class DepthSorter implements Comparator<SceneObject>
  {

    public int compare(SceneObject o1, SceneObject o2)
    {
      double d1 = (Double) o1.getValue(SceneObjectType.SCENELAYER_ATTRIBUTE_DEPTH);
      double d2 = (Double) o2.getValue(SceneObjectType.SCENELAYER_ATTRIBUTE_DEPTH);
      if (d1 < d2) return -1;
      else if (d1 > d2) return 1;
      else return 0;
    }
  }
}
