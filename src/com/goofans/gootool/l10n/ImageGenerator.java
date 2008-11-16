package com.goofans.gootool.l10n;

import javax.imageio.ImageIO;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.font.GlyphVector;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.image.GaussianFilter;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ImageGenerator
{
  private static final int WORKING_SCALE = 1;

  private static final Pattern COLOR_PATTERN = Pattern.compile("^#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})(,([0-9.]+))?$", Pattern.CASE_INSENSITIVE);

  private static final String XML_ADDTEXT = "add-text";
  private static final String XML_ADDTEXT_STRING = "string";
  private static final String XML_ADDTEXT_FONT = "font";
  private static final String XML_ADDTEXT_FONT_ATTR_NAME = "name";
  private static final String XML_ADDTEXT_FONT_ATTR_SIZE = "size";
  private static final String XML_ADDTEXT_FONT_ATTR_STRETCH = "stretch";
  private static final String XML_ADDTEXT_FONT_ATTR_OUTLINE = "outline";

  private static final String XML_LAYER = "layer";
  private static final String XML_GAUSSIANBLUR = "gaussian-blur";
  private static final String XML_GAUSSIANBLUR_ATTR_RADIUS = "radius";

  //  private BufferedImage workImage;
  private BufferedImage finalImage;
  private BufferedImage srcImage;
  private int srcWidth;
  private int srcHeight;
  private int workWidth;
  private int workHeight;
//  private Graphics2D g;

  private FontManager fontManager;
  public Map<String, String> language;

  public ImageGenerator(File sourceFile, Element el, FontManager fontManager) throws IOException, FontFormatException
  {
//    System.out.println("sourceFile = " + sourceFile);
    srcImage = ImageIO.read(sourceFile);

    srcWidth = srcImage.getWidth();
    srcHeight = srcImage.getHeight();

    workWidth = srcWidth * WORKING_SCALE;
    workHeight = srcHeight * WORKING_SCALE;

    this.fontManager = fontManager;
  }

  public void drawText(Graphics2D g, String text, Font font, float fontSize, double stretch, float outline, Color color, Point pos, double rotation) throws IOException, FontFormatException
  {
    if (stretch == 0) stretch = 1;
    Font f = font.deriveFont(fontSize * WORKING_SCALE);

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


    int drawAtX = pos.x * WORKING_SCALE;
    int drawAtY = pos.y * WORKING_SCALE;

    AffineTransform identity = g.getTransform();

    GlyphVector glyphVector = f.createGlyphVector(g.getFontRenderContext(), text);

    Rectangle2D bounds = glyphVector.getOutline().getBounds2D();
    System.out.println("bounds = " + bounds);

    double glyphVectorWidth = (bounds.getWidth()) * stretch;
    double glyphVectorHeight = bounds.getHeight();

    double x = drawAtX - (((float) glyphVectorWidth) / 2) - bounds.getX();
    double y = drawAtY - (((float) glyphVectorHeight) / 2) - bounds.getY();
    System.out.println("x = " + x);
    System.out.println("y = " + y);

    if (rotation != 0) {
      g.rotate((rotation * Math.PI) / 180, drawAtX, drawAtY);
    }
    g.translate(x, y);
    g.scale(stretch, 1f);

//    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//            RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(color);
    if (outline > 0) {
      Shape shape = glyphVector.getOutline();//x, (float) (y - bounds.getHeight()));

      g.setStroke(new BasicStroke(outline * WORKING_SCALE));
      g.draw(shape);
    }
    else {
      g.drawGlyphVector(glyphVector, 0, 0);
    }
    g.setTransform(identity);
//    drawPoint(g, drawAtX, drawAtY, Color.RED);

//    g2.drawImage(workImage, AffineTransform.getScaleInstance(1d/WORKING_SCALE, 1d/WORKING_SCALE), null);
//    g2.setPaint(new TexturePaint(workImage, new Rectangle2D.Float(0, 0, workWidth, workHeight)));
//    g2.scale(1d/WORKING_SCALE, 1d/WORKING_SCALE);
//    g2.setColor(Color.BLUE);
//    g2.fillRect(0, 0, workWidth, workHeight);

//    g.setColor(Color.GREEN);
//    g.drawRect(-((int)bounds.getWidth())/2, -((int)bounds.getHeight())/2, (int) bounds.getWidth() - 1, (int) bounds.getHeight() - 1);
  }

//  public BufferedImage getWorkImage()
//  {
//    return workImage;
//  }

  public BufferedImage getFinalImage()
  {
    return finalImage;
  }

  public void writeImage(String name) throws IOException
  {
    ImageIO.write(finalImage, "PNG", new File(name));

  }

  private void drawPoint(Graphics2D g, int x, int y, Color color)
  {
    g.setColor(color);
    g.setStroke(new BasicStroke(1));
    g.drawLine(x - 2, y - 2, x + 2, y + 2);
    g.drawLine(x + 2, y - 2, x - 2, y + 2);
//    g.drawRect(x, y, 0, 0);
  }

  public void process(Element el, Map<String, String> language) throws IOException, FontFormatException, XPathExpressionException
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

        if (itemEl.getTagName().equals(XML_ADDTEXT)) {
          doTextNode(itemEl, g);
        }
        else if (itemEl.getTagName().equals(XML_LAYER)) {
          g.drawImage(processLayer(itemEl), 0, 0, null);
        }
        else if (itemEl.getTagName().equals(XML_GAUSSIANBLUR)) {
          float radius = (float) XMLUtil.getAttributeDoubleRequired(itemEl, XML_GAUSSIANBLUR_ATTR_RADIUS);
          layerImage = new GaussianFilter(radius).filter(layerImage, null);
          g = layerImage.createGraphics();
        }
      }
    }
    return layerImage;
  }

  private void doTextNode(Element addTextEl, Graphics2D g) throws XPathExpressionException, IOException, FontFormatException
  {
    Element fontElement = getElementRequired(addTextEl, XML_ADDTEXT_FONT);

    String fontName = XMLUtil.getAttributeStringRequired(fontElement, XML_ADDTEXT_FONT_ATTR_NAME);
    Font font = fontManager.getFont(fontName);
    float fontSize = (float) XMLUtil.getAttributeDoubleRequired(fontElement, XML_ADDTEXT_FONT_ATTR_SIZE);
    double stretch = XMLUtil.getAttributeDouble(fontElement, XML_ADDTEXT_FONT_ATTR_STRETCH, 1d);
    float outline = XMLUtil.getAttributeDouble(fontElement, XML_ADDTEXT_FONT_ATTR_OUTLINE, 0d).floatValue();

    String string = getElementStringRequired(addTextEl, XML_ADDTEXT_STRING);

    Color color = parseColor(addTextEl.getElementsByTagName("color").item(0).getTextContent().trim());
    int xPos = Integer.parseInt(addTextEl.getElementsByTagName("x-position").item(0).getTextContent().trim());
    int yPos = Integer.parseInt(addTextEl.getElementsByTagName("y-position").item(0).getTextContent().trim());
    float rotation = getOptionalFloat(addTextEl, "rotation");

    String text = null;
    int openBracketsPos = string.indexOf('[');
    if (openBracketsPos > 0) {
      String realString = string.substring(0, openBracketsPos);
      String wholeText = language.get(realString);
      if (wholeText != null) {
        String[] bits = wholeText.split("\\|");

        int offset = Integer.valueOf(string.substring(openBracketsPos + 1, string.indexOf(']')));
        System.out.println("offset = " + offset);

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

    drawText(g, text, font, fontSize, stretch, outline, color, new Point(xPos, yPos), rotation);
  }


  private Element getElementRequired(Element el, String tagName) throws IOException
  {
    NodeList nodes = el.getElementsByTagName(tagName);
    if (nodes.getLength() == 0) throw new IOException("element " + tagName + " not found");
    return (Element) nodes.item(0);
  }


  private String getElementStringRequired(Element el, String tagName) throws IOException
  {
    return getElementRequired(el, tagName).getTextContent().trim();
  }


  // TODO make getElementRequired etc in XMLUtil
  private static float getOptionalFloat(Element addTextEl, String tagName)
  {
    NodeList list = addTextEl.getElementsByTagName(tagName);
    if (list.getLength() == 0) return 0;
    return Float.parseFloat(list.item(0).getTextContent().trim());
  }

  private static Color parseColor(String s) throws IOException
  {
    Matcher matcher = COLOR_PATTERN.matcher(s);
    if (!matcher.matches()) {
      throw new IOException("Invalid color specification " + s);
    }

    int alpha = 255;
    if (matcher.group(5) != null) {
      System.out.println("matcher.group(5) = " + matcher.group(5));
      alpha = (int) ((Float.parseFloat(matcher.group(5))) * 255);
    }
    System.out.println("alpha = " + alpha);

    return new Color(Integer.valueOf(matcher.group(1), 16),
            Integer.valueOf(matcher.group(2), 16),
            Integer.valueOf(matcher.group(3), 16),
            alpha);
  }
}
