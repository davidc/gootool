/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.l10n;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.goofans.gootool.image.GaussianFilter;
import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ImageGenerator
{
  private static final Logger log = Logger.getLogger(ImageGenerator.class.getName());

  private static final int WORKING_SCALE = 1;

  @SuppressWarnings({"HardCodedStringLiteral"})
  private static final Pattern COLOR_PATTERN = Pattern.compile("^#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})(,([0-9.]+))?$", Pattern.CASE_INSENSITIVE);

  private static final String XML_DRAW = "draw";

  private static final String XML_DRAW_POS_ATTR_X = "x";
  private static final String XML_DRAW_POS_ATTR_Y = "y";
  private static final String XML_DRAW_POS_ATTR_X_JUSTIFY = "x-justify";
  private static final String XML_DRAW_POS_ATTR_Y_JUSTIFY = "y-justify";

  private static final String XML_DRAW_FIXEDPOSITION = "fixed-position";

  private static final String XML_DRAW_FITTOBOX = "fit-to-box";
  private static final String XML_DRAW_FITTOBOX_ATTR_WIDTH = "width";
  private static final String XML_DRAW_FITTOBOX_ATTR_HEIGHT = "height";
  private static final String XML_DRAW_FITTOBOX_ATTR_ALLOWGROW = "allow-grow";

  private static final String XML_DRAW_TEXT = "text";
  private static final String XML_DRAW_TEXT_STRING = "string";
  private static final String XML_DRAW_TEXT_FONT = "font";
  private static final String XML_DRAW_TEXT_FONT_ATTR_NAME = "name";
  private static final String XML_DRAW_TEXT_FONT_ATTR_SIZE = "size";
  private static final String XML_DRAW_TEXT_FONT_ATTR_STRETCH = "stretch";
  private static final String XML_DRAW_TEXT_FONT_ATTR_OUTLINE = "outline";
  private static final String XML_DRAW_TEXT_COLOR = "color";
  private static final String XML_DRAW_TEXT_ROTATION = "rotation";
  private static final String XML_DRAW_TEXT_ARCH = "arch";
  private static final String XML_DRAW_TEXT_ARCH_ATTR_HEIGHT = "height";
  private static final String XML_DRAW_TEXT_ARCH_ATTR_ANGLE = "angle";

  private static final String XML_LAYER = "layer";
  private static final String XML_GAUSSIANBLUR = "gaussian-blur";
  private static final String XML_GAUSSIANBLUR_ATTR_RADIUS = "radius";

  //  private BufferedImage workImage;
  private BufferedImage finalImage;
  private final BufferedImage srcImage;
  private final int srcWidth;
  private final int srcHeight;
  private final int workWidth;
  private final int workHeight;
//  private Graphics2D g;

  private final FontManager fontManager;
  private Map<String, String> language;

  private final Element el;

  private final boolean debug;

  public ImageGenerator(File sourceFile, Element el, FontManager fontManager, boolean debug) throws IOException
  {
//    System.out.println("sourceFile = " + sourceFile);
    try {
      srcImage = ImageIO.read(sourceFile);
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Can't read " + sourceFile, e);
      throw new IOException("Can't read " + sourceFile + ": " + e.getLocalizedMessage());
    }

    srcWidth = srcImage.getWidth();
    srcHeight = srcImage.getHeight();

    workWidth = srcWidth * WORKING_SCALE;
    workHeight = srcHeight * WORKING_SCALE;

    this.el = el;
    this.fontManager = fontManager;
    this.debug = debug;
  }

  public void drawText(Graphics2D g, String text, Font font, float fontSize, double stretch, float outline, Color color, Position pos, double rotation, double archHeight, double archAngle)
  {
    if (stretch == 0) stretch = 1;
    Font f = font.deriveFont(fontSize * WORKING_SCALE);

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    AffineTransform identity = g.getTransform();

    GlyphVector glyphVector = f.createGlyphVector(g.getFontRenderContext(), text);

    Rectangle2D bounds = glyphVector.getOutline().getBounds2D();
    // Modify it to add our stretch
    bounds = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth() * stretch, bounds.getHeight());

    double scale = pos.getScale(bounds);

    // Modify bounds to take into account of our scaling factor (e.g. for FitToBox)
    bounds = new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth() * scale, bounds.getHeight() * scale);

    double x = pos.getBottomLeftX(bounds);
    double y = pos.getBottomLeftY(bounds);
//    System.out.println("x = " + x);
//    System.out.println("y = " + y);

    if (rotation != 0) {
      g.rotate(deg2Rad(rotation), pos.getRotationCenterX(bounds), pos.getRotationCenterY(bounds));
    }

    if (debug) {
      pos.debugDrawBounds(g, Color.RED);
//      if (archHeight > 0) {
//        g.setColor(Color.RED);
//        int dx = (int) (x + (bounds.getWidth() / 2));
//        g.setStroke(new BasicStroke(2));
//        g.drawLine(dx, (int) y, dx, (int) (y-archHeight));
//      }
    }

    g.translate(x, y);
    g.scale(scale * stretch, scale);

//    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//            RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(color);

    if (outline > 0) {
      g.setStroke(new BasicStroke(outline * WORKING_SCALE));
    }

    if (archHeight == 0) {
      if (outline > 0) {
        Shape shape = glyphVector.getOutline(0, 0);//x, (float) (y - bounds.getHeight()));

//        g.setStroke(new BasicStroke(outline * WORKING_SCALE));
        g.draw(shape);
      }
      else {
        g.drawGlyphVector(glyphVector, 0, 0);
      }

    }
    else {
      // Arched text!

      double spreadAngle = deg2Rad(archAngle);

      for (int i = 0; i < glyphVector.getNumGlyphs(); i++) {

        float proportionAcross = ((float) i) / (glyphVector.getNumGlyphs() - 1);

        float glyphOffsetY = (float) (Math.sin(Math.PI * proportionAcross) * archHeight);
        Shape glyph = glyphVector.getGlyphOutline(i, 0, glyphOffsetY);

        AffineTransform curTransform = g.getTransform();

        double angle = (spreadAngle * proportionAcross) - (spreadAngle / 2);

        double midGlyphX = glyph.getBounds().getX() + (glyph.getBounds().getWidth() / 2);
        double midGlyphY = glyph.getBounds().getY() + (glyph.getBounds().getHeight() / 2);
//      drawPoint(g, (int)midGlyphX,  (int)midGlyphY, Color.ORANGE);

        g.rotate(angle, midGlyphX, midGlyphY);

        if (outline > 0) {
          g.draw(glyph);
        }
        else {
          g.fill(glyph);
        }

        g.setTransform(curTransform);
      }
    }


    g.setTransform(identity);
//    drawPoint(g, centerAtX, centerAtY, Color.RED);

//    g2.drawImage(workImage, AffineTransform.getScaleInstance(1d/WORKING_SCALE, 1d/WORKING_SCALE), null);
//    g2.setPaint(new TexturePaint(workImage, new Rectangle2D.Float(0, 0, workWidth, workHeight)));
//    g2.scale(1d/WORKING_SCALE, 1d/WORKING_SCALE);
//    g2.setColor(Color.BLUE);
//    g2.fillRect(0, 0, workWidth, workHeight);

//    g.setColor(Color.GREEN);
//    g.drawRect(-((int)bounds.getWidth())/2, -((int)bounds.getHeight())/2, (int) bounds.getWidth() - 1, (int) bounds.getHeight() - 1);
  }

  private double deg2Rad(double angle)
  {
    return (angle * Math.PI) / 180;
  }

//  public BufferedImage getWorkImage()
//  {
//    return workImage;
//  }

  public BufferedImage getFinalImage()
  {
    return finalImage;
  }

  public void writeImage(File file) throws IOException
  {
    Utilities.mkdirsOrException(file.getParentFile());
    ImageIO.write(finalImage, GameFormat.PNG_FORMAT, file);
  }

  private void drawPoint(Graphics2D g, int x, int y, Color color)
  {
    g.setColor(color);
    g.setStroke(new BasicStroke(1));
    g.drawLine(x - 2, y - 2, x + 2, y + 2);
    g.drawLine(x + 2, y - 2, x - 2, y + 2);
//    g.drawRect(x, y, 0, 0);
  }

  public void process(Map<String, String> language) throws IOException, FontFormatException, XPathExpressionException
  {
    this.language = language;

    BufferedImage workImage = makeLayerImage();
    Graphics2D g = workImage.createGraphics();

    g.drawImage(srcImage, 0, 0, workWidth - 1, workHeight - 1, 0, 0, srcWidth - 1, srcHeight - 1, null);

    BufferedImage finalLayer = processLayer(el);

    g.drawImage(finalLayer, 0, 0, null);

//    Font f = getFont("TCCEB.TTF");

//    drawText(text, f, 30.0f, 1.3f, 6, new Color(42, 42, 42), new Point(85, 36), 180);
//    drawText(text, f, 30.0f, 1.3f, 0, new Color(153, 153, 153), new Point(85, 36), 180);

    finalImage = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_4BYTE_ABGR);
    Graphics2D g2 = finalImage.createGraphics();
//    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
//                   RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
//    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//            RenderingHints.VALUE_ANTIALIAS_ON);
//    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
//            RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    g2.drawImage(workImage, 0, 0, srcWidth - 1, srcHeight - 1, 0, 0, workWidth - 1, workHeight - 1, null);

  }

  private BufferedImage makeLayerImage()
  {
    return new BufferedImage(workWidth, workHeight, BufferedImage.TYPE_4BYTE_ABGR);
  }

  private BufferedImage processLayer(Element el) throws IOException, XPathExpressionException, FontFormatException
  {
    BufferedImage layerImage = makeLayerImage();
    Graphics2D g = layerImage.createGraphics();

    NodeList childNodes = el.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element) {
        Element itemEl = (Element) node;

        if (itemEl.getTagName().equals(XML_DRAW)) {
          doDraw(itemEl, g);
        }
        else if (itemEl.getTagName().equals(XML_LAYER)) {
          g.drawImage(processLayer(itemEl), 0, 0, null);
        }
        else if (itemEl.getTagName().equals(XML_GAUSSIANBLUR)) {
          float radius = (float) XMLUtil.getAttributeDoubleRequired(itemEl, XML_GAUSSIANBLUR_ATTR_RADIUS);
          layerImage = new GaussianFilter(radius).filter(layerImage, null);
          g = layerImage.createGraphics();
        }
        /*     else if (itemEl.getTagName().equals("circle")) {
          CircleFilter filter = new CircleFilter();
          filter.setAngle(-ImageMath.PI / 2 + ImageMath.PI / 8); // 22 degrees
          filter.setCentreX(0.5f);
          filter.setCentreY(1.0f);
          filter.setSpreadAngle(ImageMath.PI / 4); // 45 degrees
          filter.setHeight(50);
          filter.setRadius(100);

          System.out.println("filter = " + filter);

          layerImage = filter.filter(layerImage, null);
          System.out.println("XX layerImage.getWidth() = " + layerImage.getWidth());
          System.out.println("XX layerImage.getHeight() = " + layerImage.getHeight());
          g = layerImage.createGraphics();
        }*/
      }
    }
    return layerImage;
  }

  private void doDraw(Element drawEl, Graphics2D g) throws IOException, XPathExpressionException, FontFormatException
  {
    Position pos = getPosition(drawEl);

    for (int i = 0; i < drawEl.getChildNodes().getLength(); i++) {
      Node node = drawEl.getChildNodes().item(i);
      if (node.getNodeName().equals(XML_DRAW_TEXT)) {

        doTextNode((Element) node, g, pos);

        if (debug) {
          pos.debugDrawBounds(g, Color.ORANGE);
        }
      }
    }

//    if (drawItemEl == null) {
//      throw new IOException("Nothing specified to draw!");
//    }
  }

  private Position getPosition(Element drawEl) throws IOException
  {
    Element fixedPositionElement = XMLUtil.getElement(drawEl, XML_DRAW_FIXEDPOSITION);
    if (fixedPositionElement != null) {
      double xPos = XMLUtil.getAttributeIntegerRequired(fixedPositionElement, XML_DRAW_POS_ATTR_X);
      double yPos = XMLUtil.getAttributeIntegerRequired(fixedPositionElement, XML_DRAW_POS_ATTR_Y);
      int xJustify = decodeXJustify(XMLUtil.getAttributeString(fixedPositionElement, XML_DRAW_POS_ATTR_X_JUSTIFY, "center"));
      int yJustify = decodeYJustify(XMLUtil.getAttributeString(fixedPositionElement, XML_DRAW_POS_ATTR_Y_JUSTIFY, "middle"));
      return new FixedPosition(xPos, yPos, xJustify, yJustify);
    }

    Element fitToBoxElement = XMLUtil.getElement(drawEl, XML_DRAW_FITTOBOX);
    if (fitToBoxElement != null) {
      double xPos = XMLUtil.getAttributeIntegerRequired(fitToBoxElement, XML_DRAW_POS_ATTR_X);
      double yPos = XMLUtil.getAttributeIntegerRequired(fitToBoxElement, XML_DRAW_POS_ATTR_Y);
      double width = XMLUtil.getAttributeIntegerRequired(fitToBoxElement, XML_DRAW_FITTOBOX_ATTR_WIDTH);
      double height = XMLUtil.getAttributeIntegerRequired(fitToBoxElement, XML_DRAW_FITTOBOX_ATTR_HEIGHT);
      int xJustify = decodeXJustify(XMLUtil.getAttributeString(fitToBoxElement, XML_DRAW_POS_ATTR_X_JUSTIFY, "center"));
      int yJustify = decodeYJustify(XMLUtil.getAttributeString(fitToBoxElement, XML_DRAW_POS_ATTR_Y_JUSTIFY, "middle"));
      boolean allowGrow = XMLUtil.getAttributeBoolean(fitToBoxElement, XML_DRAW_FITTOBOX_ATTR_ALLOWGROW, true);
      return new FitToBoxPosition(xPos, yPos, width, height, xJustify, yJustify, allowGrow);
    }

    throw new IOException("No position specified on " + drawEl.getTagName());
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  private int decodeXJustify(String val) throws IOException
  {
    if ("left".equalsIgnoreCase(val)) return FixedPosition.X_JUSTIFY_LEFT;
    if ("center".equalsIgnoreCase(val)) return FixedPosition.X_JUSTIFY_CENTER;
    if ("right".equalsIgnoreCase(val)) return FixedPosition.X_JUSTIFY_RIGHT;
    throw new IOException("Invalid x-justify value " + val);
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  private int decodeYJustify(String val) throws IOException
  {
    if ("top".equalsIgnoreCase(val)) return FixedPosition.Y_JUSTIFY_TOP;
    if ("middle".equalsIgnoreCase(val)) return FixedPosition.Y_JUSTIFY_MIDDLE;
    if ("bottom".equalsIgnoreCase(val)) return FixedPosition.Y_JUSTIFY_BOTTOM;
    throw new IOException("Invalid y-justify value " + val);
  }

  private void doTextNode(Element addTextEl, Graphics2D g, Position pos) throws IOException, FontFormatException
  {
    Element fontElement = XMLUtil.getElementRequired(addTextEl, XML_DRAW_TEXT_FONT);

    String fontName = XMLUtil.getAttributeStringRequired(fontElement, XML_DRAW_TEXT_FONT_ATTR_NAME);
    Font font = fontManager.getFont(fontName);
    float fontSize = (float) XMLUtil.getAttributeDoubleRequired(fontElement, XML_DRAW_TEXT_FONT_ATTR_SIZE);
    double stretch = XMLUtil.getAttributeDouble(fontElement, XML_DRAW_TEXT_FONT_ATTR_STRETCH, 1d);
    float outline = XMLUtil.getAttributeDouble(fontElement, XML_DRAW_TEXT_FONT_ATTR_OUTLINE, 0d).floatValue();

    String string = XMLUtil.getElementStringRequired(addTextEl, XML_DRAW_TEXT_STRING);

    Color color = parseColor(XMLUtil.getElementStringRequired(addTextEl, XML_DRAW_TEXT_COLOR));

    double rotation = XMLUtil.getElementDouble(addTextEl, XML_DRAW_TEXT_ROTATION, 0);

    Element archElement = XMLUtil.getElement(addTextEl, XML_DRAW_TEXT_ARCH);
    double archHeight = 0;
    double archAngle = 0;
    if (archElement != null) {
      archHeight = XMLUtil.getAttributeDoubleRequired(archElement, XML_DRAW_TEXT_ARCH_ATTR_HEIGHT);
      archAngle = XMLUtil.getAttributeDoubleRequired(archElement, XML_DRAW_TEXT_ARCH_ATTR_ANGLE);
    }


    String text = null;
    int openBracketsPos = string.indexOf('[');
    if (openBracketsPos > 0) {
      String realString = string.substring(0, openBracketsPos);
      String wholeText = language.get(realString);
      if (wholeText != null) {
        String[] bits = wholeText.split("\\|");

        int offset = Integer.valueOf(string.substring(openBracketsPos + 1, string.indexOf(']')));

        if (offset > bits.length) {
          text = "!!offset " + offset + "!!";
        }
        else {
          text = bits[offset - 1];
        }
      }
    }
    else {
      text = language.get(string);
    }
    if (text == null) text = "!!MISSING!!";

    drawText(g, text, font, fontSize, stretch, outline, color, pos, rotation, archHeight, archAngle);
  }

  private static Color parseColor(String s) throws IOException
  {
    Matcher matcher = COLOR_PATTERN.matcher(s);
    if (!matcher.matches()) {
      throw new IOException("Invalid color specification " + s);
    }

    int alpha = 255;
    if (matcher.group(5) != null) {
      alpha = (int) ((Float.parseFloat(matcher.group(5))) * 255);
    }

    return new Color(Integer.valueOf(matcher.group(1), 16),
            Integer.valueOf(matcher.group(2), 16),
            Integer.valueOf(matcher.group(3), 16),
            alpha);
  }

  abstract class Position
  {
    public static final int X_JUSTIFY_LEFT = 1;
    public static final int X_JUSTIFY_CENTER = 2;
    public static final int X_JUSTIFY_RIGHT = 3;
    public static final int Y_JUSTIFY_TOP = 4;
    public static final int Y_JUSTIFY_MIDDLE = 5;
    public static final int Y_JUSTIFY_BOTTOM = 6;
    public int xJustify;
    public int yJustify;

    Position(int xJustify, int yJustify)
    {
      this.xJustify = xJustify;
      this.yJustify = yJustify;
    }

    public abstract void debugDrawBounds(Graphics2D g, Color orange);

    public abstract double getRotationCenterX(Rectangle2D bounds);

    public abstract double getRotationCenterY(Rectangle2D bounds);

    public abstract double getBottomLeftX(Rectangle2D bounds);

    public abstract double getBottomLeftY(Rectangle2D bounds);

    public abstract double getScale(Rectangle2D bounds);
  }

  class FixedPosition extends Position
  {

    public double x;
    public double y;

    FixedPosition(double x, double y, int xJustify, int yJustify)
    {
      super(xJustify, yJustify);
      this.x = x;
      this.y = y;
    }

    public void debugDrawBounds(Graphics2D g, Color color)
    {
      drawPoint(g, (int) x, (int) y, color);
    }

    public double getRotationCenterX(Rectangle2D bounds)
    {
      return x;
    }

    public double getRotationCenterY(Rectangle2D bounds)
    {
      return y;
    }

    public double getBottomLeftX(Rectangle2D bounds)
    {
      if (xJustify == X_JUSTIFY_LEFT) {
        return (x * WORKING_SCALE) - bounds.getX();
      }
      else if (xJustify == X_JUSTIFY_RIGHT) {
        return (x * WORKING_SCALE) - bounds.getWidth();
      }
      else {
        // center
        double centerAtX = x * WORKING_SCALE;
        return centerAtX - ((bounds.getWidth()) / 2) - bounds.getX();
      }
    }

    public double getBottomLeftY(Rectangle2D bounds)
    {
      if (yJustify == Y_JUSTIFY_TOP) {
        double centerAtY = y * WORKING_SCALE;
        return centerAtY + bounds.getHeight();// - bounds.getY();
      }
      else if (yJustify == Y_JUSTIFY_BOTTOM) {
        double centerAtY = y * WORKING_SCALE;
        return centerAtY - bounds.getHeight() - bounds.getY();

      }
      else {
        // middle
        double centerAtY = y * WORKING_SCALE;
        return centerAtY - (bounds.getHeight() / 2) - bounds.getY();
      }
    }

    public double getScale(Rectangle2D bounds)
    {
      return 1;
    }
  }

  class FitToBoxPosition extends Position
  {
    public double x;
    public double y;
    public double width;
    public double height;
    public boolean allowGrow;

    FitToBoxPosition(double x, double y, double width, double height, int xJustify, int yJustify, boolean allowGrow)
    {
      super(xJustify, yJustify);

      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.allowGrow = allowGrow;
    }

    public void debugDrawBounds(Graphics2D g, Color color)
    {
      g.setColor(color);
      g.setStroke(new BasicStroke(1));
      g.drawRect((int) x, (int) y, (int) width - 1, (int) height - 1);
    }

    public double getRotationCenterX(Rectangle2D bounds)
    {
      if (xJustify == X_JUSTIFY_LEFT) {
        return x;
      }
      else if (xJustify == X_JUSTIFY_RIGHT) {
        return x + width;
      }
      else {
        //center
        return x + (width / 2);
      }
    }

    public double getRotationCenterY(Rectangle2D bounds)
    {
      if (yJustify == Y_JUSTIFY_TOP) {
        return y;
      }
      else if (yJustify == Y_JUSTIFY_BOTTOM) {
        return y + height;
      }
      else {
        //middle
        return y + (height / 2);
      }
    }

    public double getBottomLeftX(Rectangle2D bounds)
    {
      double baseX = x - bounds.getX();
      if (xJustify == X_JUSTIFY_LEFT) {
        return baseX;
      }
      else if (xJustify == X_JUSTIFY_RIGHT) {
        return baseX + (width - bounds.getWidth());
      }
      else {
        //center
        return baseX + (width - bounds.getWidth()) / 2;
      }
    }

    public double getBottomLeftY(Rectangle2D bounds)
    {
      if (yJustify == Y_JUSTIFY_TOP) {
        return y + (bounds.getHeight());
      }
      else if (yJustify == Y_JUSTIFY_BOTTOM) {
        return y + height;
      }
      else {
        //middle
        return y + (height / 2) + (bounds.getHeight() / 2);
      }
    }

    public double getScale(Rectangle2D bounds)
    {
//      double curWidth = bounds.getWidth();
//      double curHeight = bounds.getHeight();
//      System.out.println("curWidth = " + curWidth);
//      System.out.println("curHeight = " + curHeight);

      double scaleToFitX = width / bounds.getWidth();
//      System.out.println("scaleToFitX = " + scaleToFitX);
      double scaleToFitY = height / bounds.getHeight();
//      System.out.println("scaleToFitY = " + scaleToFitY);

      double scale = Math.min(scaleToFitX, scaleToFitY);
      if (scale > 1 && !allowGrow) return 1;
      return scale;
    }
  }
}
