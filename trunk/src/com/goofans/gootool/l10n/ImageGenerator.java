package com.goofans.gootool.l10n;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.awt.font.GlyphVector;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ImageGenerator
{
  private static final int WORKING_SCALE = 1;
  private BufferedImage workImage;
  private BufferedImage finalImage;
  private BufferedImage srcImage;
  private int srcWidth;
  private int srcHeight;
  private int workWidth;
  private int workHeight;
  private Graphics2D g;

  public ImageGenerator(File sourceFile) throws IOException, FontFormatException
  {
    System.out.println("sourceFile = " + sourceFile);
    srcImage = ImageIO.read(sourceFile);

    srcWidth = srcImage.getWidth();
    srcHeight = srcImage.getHeight();

    workWidth = srcWidth * WORKING_SCALE;
    workHeight = srcHeight * WORKING_SCALE;
  }

  public void reset()
  {
    workImage = new BufferedImage(workWidth, workHeight, BufferedImage.TYPE_4BYTE_ABGR);
    g = workImage.createGraphics();
    g.drawImage(srcImage, 0, 0, workWidth - 1, workHeight - 1, 0, 0, srcWidth - 1, srcHeight - 1, null);
  }

  public void finish() throws IOException, FontFormatException
  {
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

  public void drawText(String text, Font font, float fontSize, float stretch, float outline, Color color, Point pos, double rotation) throws IOException, FontFormatException
  {
    if (stretch == 0) stretch = 1;
    Font f = font.deriveFont(fontSize * WORKING_SCALE);

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


    int drawAtX = pos.x * WORKING_SCALE;
    int drawAtY = pos.y * WORKING_SCALE;

    AffineTransform identity = g.getTransform();


    GlyphVector glyphVector = f.createGlyphVector(g.getFontRenderContext(), text);

    Rectangle2D size = glyphVector.getOutline().getBounds2D();
    // TODO take into account X and Y of bounds!!!
    System.out.println("size = " + size);

    float x = drawAtX - (((float) size.getWidth() * stretch) / 2);
    float y = drawAtY - (((float) size.getHeight()) / 2);
    System.out.println("x = " + x);
    System.out.println("y = " + y);

//    g.rotate(Math.PI);
    if (rotation != 0) {
      g.rotate((rotation * Math.PI) / 180, drawAtX, drawAtY);
    }
//    g.rotate(Math.PI, -size.getWidth()/2, -size.getHeight()/2);
    g.translate(x, y + size.getHeight());
    g.scale(stretch, 1f);

//    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//            RenderingHints.VALUE_ANTIALIAS_ON);
    g.setColor(color);
    if (outline > 0) {
      Shape shape = glyphVector.getOutline();//x, (float) (y - size.getHeight()));

      g.setStroke(new BasicStroke(6 * WORKING_SCALE));
      g.draw(shape);
    }
    else {
      g.setFont(f);
      g.drawString(text, 0, 0);
    }
    g.setTransform(identity);
//    drawPoint(g, drawAtX, drawAtY, Color.RED);

//    g2.drawImage(workImage, AffineTransform.getScaleInstance(1d/WORKING_SCALE, 1d/WORKING_SCALE), null);
//    g2.setPaint(new TexturePaint(workImage, new Rectangle2D.Float(0, 0, workWidth, workHeight)));
//    g2.scale(1d/WORKING_SCALE, 1d/WORKING_SCALE);
//    g2.setColor(Color.BLUE);
//    g2.fillRect(0, 0, workWidth, workHeight);

//    g.setColor(Color.GREEN);
//    g.drawRect(-((int)size.getWidth())/2, -((int)size.getHeight())/2, (int) size.getWidth() - 1, (int) size.getHeight() - 1);

  }

  public BufferedImage getWorkImage()
  {
    return workImage;
  }

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


}
