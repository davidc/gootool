package com.goofans.gootoolsp.leveledit.view;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

import com.goofans.gootoolsp.leveledit.view.render.Renderer;
import com.goofans.gootoolsp.leveledit.view.render.BallRenderer;

import com.goofans.gootoolsp.leveledit.model.*;
import com.goofans.gootoolsp.leveledit.resource.BallTransferable;
import com.goofans.gootoolsp.leveledit.resource.Ball;
import com.goofans.gootoolsp.leveledit.edits.NewBallEdit;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LevelDisplay extends JPanel implements Scrollable, FocusListener
{
  private static final Logger log = Logger.getLogger(LevelDisplay.class.getName());

  private Level level;
  private Scene scene;
  private Resources resources;

  private static Map<LevelDisplayLayer, Renderer> renderers = new TreeMap<LevelDisplayLayer, Renderer>();
  static Map<LevelDisplayLayer, Class[]> layerContents = new TreeMap<LevelDisplayLayer, Class[]>(); // TODO move out of this class, LevelEditor uses it too

  static {
    renderers.put(LevelDisplayLayer.BALLS, new BallRenderer());
    layerContents.put(LevelDisplayLayer.BALLS, new Class[]{BallInstance.class});
  }

  private double scale = 0.5;

  private Set<LevelDisplayLayer> visibleLayers;
  private boolean focused;

  private Ball dragBall;
  private Point dragPoint;

  private LevelEditor editor;
  private static final double GRID_PITCH_X = 50d; // TODO Config somewhere, persisted.
  private static final double GRID_PITCH_Y = 50d;

  public LevelDisplay(LevelEditor editor)
  {
    this.editor = editor;

    visibleLayers = new HashSet<LevelDisplayLayer>(LevelDisplayLayer.values().length);
    for (LevelDisplayLayer layer : LevelDisplayLayer.values()) {
      if (layer.isDefaultVisible()) {
        visibleLayers.add(layer);
      }
    }

    addMouseWheelListener(new MouseWheelListener()
    {
      public void mouseWheelMoved(MouseWheelEvent e)
      {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
          scale = (scale - (e.getWheelRotation() * 0.1));
          if (scale < 0.1) scale = 0.1;
          if (scale > 10) scale = 10;
          // TODO center at mouse position
          repaint();
        }
      }
    });
    addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        requestFocusInWindow();
      }
    });

    addFocusListener(this);

//    addHierarchyBoundsListener(new HierarchyBoundsAdapter()
//    {
//      @Override
//      public void ancestorResized(HierarchyEvent e)
//      {
//        if (e.getChanged() == getParent()) {
//          System.out.println("parent resized");
//          setSize(getParent().getSize());
//        }
//      }
//    });

    setTransferHandler(new LevelDisplayTransferHandler());
    try {
      getDropTarget().addDropTargetListener(new DropTargetAdapter()
      {
        @Override
        public void dragOver(DropTargetDragEvent dtde)
        {
          Transferable transferable = dtde.getTransferable();
//          if (transferable instanceof BallTransferable) {
          try {
            Ball newBall = (Ball) transferable.getTransferData(BallTransferable.FLAVOR);
            if (dragBall != newBall || !dtde.getLocation().equals(dragPoint)) {
              // Snap to nearest drag point
              Point newDragPoint = dtde.getLocation();

              if (LevelDisplay.this.editor.isSnapGrid()) {
                // TODO the drop location should be stored in WORLD coordinates so we can get it exactly bang on 50,50 which won't happen otherwise due to scaling
                // Also should snap to nearest, not floor it.
                newDragPoint.x -= (newDragPoint.x % 50);
                newDragPoint.y -= (newDragPoint.y % 50);
              }

              // Only repaint if drag moved (which won't happen so often if we're snapping)
              if (dragBall != newBall || !newDragPoint.equals(dragPoint)) {
                dragBall = newBall;
                dragPoint = newDragPoint;
                repaint();// TODO only repaint the current and previous drop location
                System.out.println("Repaint requetsed");
              }
            }
          }
          catch (UnsupportedFlavorException e) {
            return;
          }
          catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return;
          }
        }

        @Override
        public void dragExit(DropTargetEvent dte)
        {
          dragBall = null;
        }

        public void drop(DropTargetDropEvent dtde)
        {
          dragBall = null;
          repaint();
        }
      });
    }
    catch (TooManyListenersException e) {
      throw new RuntimeException("Listener already registered on LevelDisplay");
    }
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

//    System.out.println("scene = " + scene);

    if (visibleLayers.contains(LevelDisplayLayer.IMAGES)) drawSceneLayers(g);
//    if (visibleLayers.contains(LevelDisplayLayer.BALLS)) drawBalls(g);
    if (visibleLayers.contains(LevelDisplayLayer.STRANDS)) drawStrands(g);
    if (visibleLayers.contains(LevelDisplayLayer.GEOMETRY)) drawGeometry(g);
    if (visibleLayers.contains(LevelDisplayLayer.BOUNDARIES)) drawLines(g);
    if (visibleLayers.contains(LevelDisplayLayer.HINGES)) drawHinges(g);
    if (visibleLayers.contains(LevelDisplayLayer.VIEWPORT)) drawViewport(g);

    for (LevelDisplayLayer layer : LevelDisplayLayer.values()) {
      if (visibleLayers.contains(layer)) {
        Renderer renderer = renderers.get(layer);
        if (renderer != null) {
          for (Class clazz : layerContents.get(layer)) {
            List<LevelContentsItem> contents = level.getLevelContents().getLevelContents(clazz);
            for (LevelContentsItem content : contents) {
              renderer.render(g, this, content);

              Shape hitbox = renderer.getHitBox(this, content);
              drawShape(g, hitbox, Color.GREEN);
              hitbox = renderer.getNegativeHitBox(this, content);
              drawShape(g, hitbox, Color.BLACK);
            }
          }
        }
      }
    }

    if (dragBall != null) {
      drawDragBall(g);
    }

    if (editor.isShowGrid()) {
      drawGrid(g);
    }

    if (focused) {
      g.setColor(Color.BLACK);
      g.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[]{2, 2}, 0));
      g.drawRect(0, 0, width - 1, height - 1);
    }
  }

  private void drawGrid(Graphics2D g)
  {
    Point gridCentre = new Point(0, 0); // TODO implement
    g.setColor(new Color(0, 0, 0, 128));
    g.setStroke(new BasicStroke(1));
    // Get display area bounds in world coordinates
    Rectangle bounds = g.getClipBounds();
    // TODO we're currently showing the grid in display coordinates!
    for (double x = bounds.getMinX() - (bounds.getMinX() % GRID_PITCH_X); x <= bounds.getMaxX(); x += GRID_PITCH_X) {
      g.drawLine((int) x, (int) bounds.getMinY(), (int) x, (int) bounds.getMaxY());
    }
    for (double y = bounds.getMinY() - (bounds.getMinY() % GRID_PITCH_X); y <= bounds.getMaxY(); y += GRID_PITCH_Y) {
      g.drawLine((int) bounds.getMinX(), (int) y, (int) bounds.getMaxX(), (int) y);
    }
  }

  private void drawShape(Graphics2D g, Shape hitbox, Color color)
  {
    g.setColor(color);
    g.setStroke(new BasicStroke(1));
    if (hitbox instanceof Ellipse2D.Float) {
      Ellipse2D.Float ellipse = (Ellipse2D.Float) hitbox;
      g.drawOval((int) ellipse.x, (int) ellipse.y, (int) ellipse.width, (int) ellipse.height);
    }
  }

  private void drawDragBall(Graphics2D g)
  {
    g.setStroke(new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, new float[]{5, 5}, 0));
    g.setColor(Color.RED);

    Shape s = dragBall.getOutlineShape();
    Rectangle2D bounds = s.getBounds2D();

    if (s instanceof Ellipse2D.Double) {
      double radius = bounds.getWidth() / 2;
      int radiusX = worldToCanvasScaleX(radius);
      int radiusY = worldToCanvasScaleY(radius);
      g.drawOval(dragPoint.x - radiusX, dragPoint.y - radiusY, radiusX * 2, radiusY * 2);
    }
    else if (s instanceof Rectangle2D.Double) {
      int width = worldToCanvasScaleX(bounds.getWidth());
      int height = worldToCanvasScaleY(bounds.getHeight());
      g.drawRect(dragPoint.x - (width / 2), dragPoint.y - (height / 2), width, height);
    }
  }

  private void drawViewport(Graphics2D g)
  {
    int x = worldToCanvasX(scene.getMinX());
    int y = worldToCanvasY(scene.getMinY());
    int width = worldToCanvasScaleX(scene.getMaxX() - scene.getMinX());
    int height = worldToCanvasScaleY(scene.getMaxY() - scene.getMinY());

    g.setStroke(new BasicStroke(2));
    g.setColor(Color.BLACK);
    g.drawRect(x, y - height, width, height);
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

  private void drawBalls(Graphics2D g)
  {
    List<LevelContentsItem> balls = level.getLevelContents().getLevelContents(BallInstance.class);
    for (LevelContentsItem item : balls) {
      BallInstance ball = (BallInstance) item;
      drawBall(g, ball);
    }
  }

  private void drawBall(Graphics2D g, BallInstance ball)
  {
    int x = worldToCanvasX(ball.x);
    int y = worldToCanvasY(ball.y);
    double radius = 40;
    int radiusX = worldToCanvasScaleX(radius);
    int radiusY = worldToCanvasScaleY(radius);

    // TODO find the Ball so we can get the right Shape and size. 

    g.setStroke(new BasicStroke(2));
    g.setColor(Color.RED);
    g.drawOval(x - radiusX, y - radiusY, radiusX * 2, radiusY * 2);
  }

  private void drawStrands(Graphics2D g)
  {
    List<LevelContentsItem> balls = level.getLevelContents().getLevelContents(Strand.class);
    for (LevelContentsItem item : balls) {
      Strand strand = (Strand) item;
      drawStrand(g, strand);
    }
  }

  private void drawStrand(Graphics2D g, Strand strand)
  {
    BallInstance ball1 = level.getLevelContents().getBallById(strand.gb1);
    BallInstance ball2 = level.getLevelContents().getBallById(strand.gb2);

    if (ball1 == null || ball2 == null) {
      System.out.println("TODO: strand with invalid balls");
    }

    int x1 = worldToCanvasX(ball1.x);
    int y1 = worldToCanvasY(ball1.y);

    int x2 = worldToCanvasX(ball2.x);
    int y2 = worldToCanvasY(ball2.y);

    g.setStroke(new BasicStroke(2));
    g.setColor(Color.RED);
    g.drawLine(x1, y1, x2, y2);
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

//    drawPoint(g, x, y, Color.GREEN);

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
//    System.out.println("name = " + name);
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

//    System.out.println("drawing " + imageId + "(" + image + ") at " + x + "," + y);

// This time rotation is in degrees!
    g.rotate(-(rotation * (Math.PI * 2) / 360), x, y);

    g.drawImage(image, x - (width / 2), y - (height / 2), x + (width / 2), y + (height / 2), 0, 0, imgWidth, imgHeight, this);

    g.setTransform(identity);

//    drawPoint(g, x, y, Color.BLACK);
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

//    drawPoint(g, x, y, Color.RED);

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
    Point2D.Double anchor = (Point2D.Double) sceneObject.getValue(SceneObjectType.ATTRIBUTE_ANCHOR);
    Point2D.Double normal = (Point2D.Double) sceneObject.getValue(SceneObjectType.LINE_ATTRIBUTE_NORMAL);
    int x = worldToCanvasX(anchor.getX());
    int y = worldToCanvasY(anchor.getY());
//    drawPoint(g, x, y, Color.RED);

//    System.out.println("anchor = " + anchor);
//    System.out.println("normal = " + normal);

    g.setStroke(new BasicStroke(2));
    g.setColor(Color.ORANGE);

    int x1 = (int) (x - (normal.y * 1000));
    int x2 = (int) (x + (normal.y * 1000));
    int y1 = (int) (y - (normal.x * 1000));
    int y2 = (int) (y + (normal.x * 1000));

    g.drawLine(x1, y1, x2, y2);
  }

  private void drawHinges(Graphics2D g)
  {

    for (SceneObject sceneObject : scene.getSceneObjects()) {
      if (sceneObject.getType() == SceneObjectType.HINGE) {
        drawHinge(g, sceneObject);
      }
    }
  }

  private void drawHinge(Graphics2D g, SceneObject sceneObject)
  {
    System.out.println("sceneObject = " + sceneObject);
    Point2D.Double anchor = (Point2D.Double) sceneObject.getValue(SceneObjectType.ATTRIBUTE_ANCHOR);
    int x = worldToCanvasX(anchor.getX());
    int y = worldToCanvasY(anchor.getY());
    drawPoint(g, x, y, Color.RED);

    System.out.println("anchor = " + anchor);

    g.setStroke(new BasicStroke(2));
    g.setColor(Color.ORANGE);

    g.drawLine(x - 4, y - 4, x + 4, y + 4);
    g.drawLine(x + 4, y - 4, x - 4, y + 4);

    // TODO draw lines to body1/2
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
  public int worldToCanvasX(double x)
  {
    // Let's say 0,0 top left is the scene's minX
    x -= scene.getMinX();

    // Now scale it
    x = worldToCanvasScaleX(x);

    return (int) x;
  }

  public double canvasToWorldX(int x)
  {
    double ox = canvasToWorldScaleX(x);
    ox += scene.getMinX();
    return ox;
  }

  /*
  * Converts an Y-coordinate in world units to the actual y location we should display it on our canvas.
  */
  public int worldToCanvasY(double y)
  {
    // Let's say 0,0 top left is the scene's maxY
    y = scene.getMaxY() - y;

    // Now scale it
    y = worldToCanvasScaleY(y);
    return (int) y;
  }

  public double canvasToWorldY(int y)
  {
    double oy = canvasToWorldScaleY(y);
    oy = scene.getMaxY() - oy;
    return oy;
  }


  /*
  * Converts an X width in world units to width on our canvas
  */
  public int worldToCanvasScaleX(double x)
  {
    return (int) (x * scale);
  }

  public double canvasToWorldScaleX(int x)
  {
    return x / scale;
  }

  /*
  * Converts a Y height in world units to height on our canvas
  */
  public int worldToCanvasScaleY(double y)
  {
    return (int) (y * scale);
  }

  public double canvasToWorldScaleY(int y)
  {
    return y / scale;
  }

  public boolean isLayerVisibile(LevelDisplayLayer layer)
  {
    return visibleLayers.contains(layer);
  }

  public void setLayerVisibile(LevelDisplayLayer layer, Boolean visible)
  {
    if (visible) visibleLayers.add(layer);
    else visibleLayers.remove(layer);

    repaint();
  }

  public Dimension getPreferredScrollableViewportSize()
  {
    return new Dimension(500, 500);
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
  {
    return 10;
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
  {
    return 100;
  }

  public boolean getScrollableTracksViewportWidth()
  {
    return false;
  }

  public boolean getScrollableTracksViewportHeight()
  {
    return false;
  }

  @Override
  public Dimension getPreferredSize()
  {
    return new Dimension(500, 500);
  }

  public void focusGained(FocusEvent e)
  {
    focused = true;
    repaint();
  }

  public void focusLost(FocusEvent e)
  {
    focused = false;
    repaint();
  }

  /**
   * Checks for a hit at the given point in one of the allowed layers.
   *
   * @param point         The display coordinates to check
   * @param layersToCheck The layers in which to check (or null for all).
   * @return the item that hit, or null of nothing hit
   */
  public LevelContentsItem checkHit(Point point, LevelDisplayLayer[] layersToCheck)
  {
    /* Reverse ordering so we hit higher stuff first */
    for (int i = LevelDisplayLayer.values().length - 1; i >= 0; i--) {
      LevelDisplayLayer layer = LevelDisplayLayer.values()[i];
      LevelContentsItem item = null;

      if (layersToCheck == null) {
        item = checkHit(point, layer);
      }
      else {
        /* Are we testing this layer */
        for (LevelDisplayLayer testLayer : layersToCheck) {
          if (layer == testLayer) {
            item = checkHit(point, layer);
          }
        }
      }
      if (item != null) return item;
    }


    return null;
  }

  /**
   * Checks for a hit at the given point in the specified layers.
   *
   * @param point The display coordinates to check
   * @param layer The layer in which to check.
   * @return the item that hit, or null of nothing hit
   */
  public LevelContentsItem checkHit(Point point, LevelDisplayLayer layer)
  {
    Renderer renderer = renderers.get(layer);
    if (renderer == null) return null;

    /* Go through the items in this layer, again in reverse order */

    for (int i = layerContents.get(layer).length - 1; i >= 0; i--) {
      Class clazz = layerContents.get(layer)[i];
      List<LevelContentsItem> contents = level.getLevelContents().getLevelContents(clazz);

      /* And now go through the contents of this layer in reverse order too */

      for (int j = contents.size() - 1; j >= 0; j--) {
        LevelContentsItem content = contents.get(j);

        Shape hitbox = renderer.getHitBox(this, content);

        if (hitbox.contains(point)) {
//          hitbox = renderer.getNegativeHitBox(this, content);
//          if (!hitbox.contains(point)) {
          return content;
//          }
        }
      }
    }
    return null;
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

  private class LevelDisplayTransferHandler extends TransferHandler
  {
    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
    {
      for (DataFlavor flavor : transferFlavors) {
        if (flavor == BallTransferable.FLAVOR) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean importData(JComponent comp, Transferable t)
    {
      try {
        Ball ball = (Ball) t.getTransferData(BallTransferable.FLAVOR);
        BallInstance newInstance = new BallInstance(ball.getBallName(), canvasToWorldX(dragPoint.x), canvasToWorldY(dragPoint.y));
        NewBallEdit edit = new NewBallEdit(level, newInstance);
        editor.doUndoableEdit(edit);
        return true;
      }
      catch (UnsupportedFlavorException e) {
        // shouldn't happen
      }
      catch (IOException e) {
        log.log(java.util.logging.Level.WARNING, "Can't import data from DnD", e);
      }
      return false;
    }
  }
}
