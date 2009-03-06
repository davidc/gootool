package com.goofans.gootool.profile;

import com.goofans.gootool.wog.WorldOfGoo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class TowerRenderer implements ImageObserver
{
  private static final Logger log = Logger.getLogger(TowerRenderer.class.getName());

  private static final int PADDING_X = 0;
  private static final int PADDING_Y = 0;

  private static final int WORLD_SCALE_TO_PIXELS = 2;

  private static final int PRETTY_SKIP_GROUND_HEIGHT = 50;
  private static final int PRETTY_PADDING_X = 40;
  private static final int PRETTY_PADDING_Y = 40;//top only

  private static final Dimension THUMBNAIL_MAXSIZE = new Dimension(200, 300);

  private Tower t;
  private Dimension fullSizeDimension;
  private BufferedImage fullSize;
  private BufferedImage pretty;
  private BufferedImage thumbnail;

  private BufferedImage ballImage;
  private BufferedImage strandImage;
  private BufferedImage groundImage;
  private BufferedImage skyImage;

  private int xOffset;
  private int yOffset;
  private int ballNudgeX;
  private int ballNudgeY;
  private int strandHeight;
  private int strandXOffset;

  public TowerRenderer(Tower t) throws IOException
  {
    this.t = t;

    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();

    // Pick up the images out of the wog folder
    File ballFile = worldOfGoo.getGameFile("res/balls/Drained/body.png");
    ballImage = ImageIO.read(ballFile);

    // Balls need to be nudged so they are centered on the given position.
    ballNudgeX = -(ballImage.getWidth() / 2);
    ballNudgeY = -(ballImage.getHeight() / 2);

    File strandFile = worldOfGoo.getGameFile("res/balls/Drained/spring_goo.png");
    strandImage = ImageIO.read(strandFile);

    // Strands need to be drawn with origin on their start point, and stretched
    strandHeight = strandImage.getHeight();
    strandXOffset = -(strandImage.getWidth() / 2);


    File groundFile = worldOfGoo.getGameFile("res/levels/wogcd/groundTile.png");
    groundImage = ImageIO.read(groundFile);

    File skyFile = worldOfGoo.getGameFile("res/levels/wogcd/skytile.png");
    skyImage = ImageIO.read(skyFile);

  }

  public void render()
  {
    // OK go through the balls and find the one furthest left, furthest right, highest, lowest
    double leftBound = 0, rightBound = 0, lowerBound = 0, upperBound = 0;

    for (Tower.Ball ball : t.getBalls()) {
      if (ball.inStructure) {
        if (ball.xPos < leftBound) leftBound = ball.xPos;
        if (ball.xPos > rightBound) rightBound = ball.xPos;
        if (ball.yPos < lowerBound) lowerBound = ball.yPos;
        if (ball.yPos > upperBound) upperBound = ball.yPos;
      }
    }

    log.log(Level.FINER, "leftBound = " + leftBound);
    log.log(Level.FINER, "rightBound = " + rightBound);
    log.log(Level.FINER, "lowerBound = " + lowerBound);
    log.log(Level.FINER, "upperBound = " + upperBound);

    int width = (int) (rightBound - leftBound);
    int height = (int) (upperBound - lowerBound);

    xOffset = (int) -leftBound;
    yOffset = (int) upperBound;

    // Scale from world coordinates to pixel coordinates
    width /= WORLD_SCALE_TO_PIXELS;
    height /= WORLD_SCALE_TO_PIXELS;
    xOffset /= WORLD_SCALE_TO_PIXELS;
    yOffset /= WORLD_SCALE_TO_PIXELS;

    // Half a ball hangs off each side, so we add a full ball's width and shift accordingly
    width += ballImage.getWidth();
    height += ballImage.getHeight();
    xOffset += (ballImage.getWidth() / 2);
    yOffset += (ballImage.getHeight() / 2);

    // We might want some padding too, these are in Pixels;
    width += PADDING_X * 2;
    height += PADDING_Y * 2;
    xOffset += PADDING_X;
    yOffset += PADDING_Y;


    fullSizeDimension = new Dimension(width, height);
    log.log(Level.FINER, "fullSizeDimension = " + fullSizeDimension);
    fullSize = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);

    Graphics2D g = fullSize.createGraphics();

    drawStrands(g);
    drawBalls(g);

    createPretty();
    createThumbnail();
  }

  private void drawStrands(Graphics2D g)
  {
    // Draw Strands
    for (Tower.Strand strand : t.getStrands()) {
      //"transform to world coords" function
      drawStrand(g,
              getPixelCoords(strand.firstBall.xPos, strand.firstBall.yPos),
              getPixelCoords(strand.secondBall.xPos, strand.secondBall.yPos),
              strandImage);
    }
  }

  private void drawBalls(Graphics2D g)
  {
    // Draw balls
    for (Tower.Ball ball : t.getBalls()) {
      if (ball.inStructure) {
//        int x = (int) (ball.xPos + xOffset);
//        int y = (int) (yOffset - ball.yPos);
        Point p = getPixelCoords(ball.xPos, ball.yPos);
        drawBall(g, p);
      }
    }
  }

  // Given "world" coordinates, map them to a point on our canvas.
  private Point getPixelCoords(double x, double y)
  {
    int x2 = ((int) (x / WORLD_SCALE_TO_PIXELS) + xOffset);
    int y2 = (yOffset - (int) (y / WORLD_SCALE_TO_PIXELS));
    return new Point(x2, y2);
  }

  private void drawBall(Graphics2D g, Point p)
  {
    g.drawImage(ballImage, p.x + ballNudgeX, p.y + ballNudgeY, this);
  }


  private void drawStrand(Graphics2D g, Point start, Point end, BufferedImage strandImage)
  {
    AffineTransform identity = g.getTransform();

    double angle = Math.atan2(end.y - start.y, end.x - start.x) - (Math.PI / 2);
    double length = Math.sqrt(Math.pow(end.x - start.x, 2) + Math.pow(end.y - start.y, 2));

    double scale = length / strandHeight;
//    System.out.println("length = " + length);
//    System.out.println("scale = " + scale);
//double angle = Math.PI / 4;

    // Translate to the start position
    g.translate(start.x, start.y);
    // Rotate by the desired angle
    g.rotate(angle);
    // Scale the Y length
    g.scale(1, scale); // don't stretch the "width"

    // Draw it centered around our new origin
    g.drawImage(strandImage, strandXOffset, 0, this);

    g.setTransform(identity);

  }


  private void createPretty()
  {

//    pretty = new BufferedImage(100, 100, BufferedImage.TYPE_4BYTE_ABGR);
//    g = pretty.createGraphics();

//    g.drawImage(ballImage, 50, 50, this);
    int width = fullSizeDimension.width + (2 * PRETTY_PADDING_X);
    int height = fullSizeDimension.height + groundImage.getHeight() + ballNudgeY + PRETTY_PADDING_Y - PRETTY_SKIP_GROUND_HEIGHT;

    pretty = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = pretty.createGraphics();

    // Draw the sky
    int skyY = 0;
    while (skyY < height) {
      int skyX = 0;
      while (skyX < width) {
        g.drawImage(skyImage, skyX, skyY, null);
        skyX += skyImage.getWidth();
      }
      skyY += skyImage.getHeight();
    }

    // Draw the ground
    int groundX = 0;
    int groundY = fullSizeDimension.height + PRETTY_PADDING_Y + ballNudgeY; // shift it back up so balls don't float midair
    while (groundX < width) {
      g.drawImage(groundImage, groundX, groundY, null);
      groundX += groundImage.getWidth() - 1;
    }

    g.drawImage(fullSize, PRETTY_PADDING_X, PRETTY_PADDING_Y, null);

  }

  private void createThumbnail()
  {
    int prettyWidth = pretty.getWidth();
    int prettyHeight = pretty.getHeight();

    double scale1 = THUMBNAIL_MAXSIZE.width / (double) prettyWidth;
//    if (((double) prettyHeight) * scale > THUMBNAIL_MAXSIZE.height) {
      double scale2 = THUMBNAIL_MAXSIZE.height / ((double) prettyHeight);
//    }
    double scale = Math.min(scale1, scale2);
    log.finer("Thumbnail scale = " + scale);

    // if the image is already tiny
    if (scale > 1) scale = 1;

    int width = (int) (prettyWidth * scale);
    int height = (int) (prettyHeight * scale);
    log.finer("Thumbnail size = " + width + " x " + height);

    thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g = thumbnail.createGraphics();

    g.drawImage(pretty, 0, 0, width - 1, height - 1, this);
  }


  public BufferedImage getFullSize()
  {
    return fullSize;
  }

  public Dimension getFullSizeDimension()
  {
    return fullSizeDimension;
  }

  public BufferedImage getThumbnail()
  {
    return thumbnail;
  }

  public BufferedImage getPretty()
  {
    return pretty;
  }

  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
  {
    return false;
  }

  private void rotateTest() throws IOException
  {
    File strandFile = WorldOfGoo.getTheInstance().getGameFile("res/balls/Drained/spring_goo.png");

    BufferedImage strandImage = ImageIO.read(strandFile);

    BufferedImage test = new BufferedImage(500, 500, BufferedImage.TYPE_4BYTE_ABGR);

//    BufferedImage strandRotated = new BufferedImage(strandImage.getWidth(), strandImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR));
//    Graphics2D gStrand = strandRotated.createGraphics();
//    gStrand.rotate();

    Graphics2D g = test.createGraphics();

    g.setColor(Color.WHITE);
    g.fillRect(0, 0, 500, 500);

//    int startX = 300;
//    int startY = 30;
//    int endX = 100;
//    int endY = 300;
    drawRandomStrand(g, strandImage);
    drawRandomStrand(g, strandImage);
    drawRandomStrand(g, strandImage);
    drawRandomStrand(g, strandImage);
    drawRandomStrand(g, strandImage);

    ImageIO.write(test, "PNG", new File("rotate.png"));
  }

  private void drawRandomStrand(Graphics2D g, BufferedImage strandImage)
  {
    Random random = new Random();
    Point start = new Point(random.nextInt(500), random.nextInt(500));
    Point end = new Point(random.nextInt(500), random.nextInt(500));

    drawStrand(g, start, end, strandImage);

    drawPoint(g, start.x, start.y, Color.RED);
    drawPoint(g, end.x, end.y, Color.GREEN);
  }

  private void drawPoint(Graphics2D g, int x, int y, Color color)
  {
    g.setColor(color);
    g.drawLine(x - 2, y - 2, x + 2, y + 2);
    g.drawLine(x + 2, y - 2, x - 2, y + 2);
//    g.drawRect(x, y, 0, 0);
  }


  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    WorldOfGoo.getTheInstance().init();

    Tower t = new Tower("_b:Drained:-919.68:408.96:0.03:0.01:b:Drained:-1861.54:131.05:0.01:-0.01:b:Drained:-1065.30:556.08:0.01:0.01:b:Drained:-1748.46:179.49:0.01:-0.00:b:Drained:-457.06:712.98:-0.00:0.00:b:Drained:-1766.93:40.95:0.01:-0.01:b:Drained:-715.67:346.38:0.01:-0.00:b:Drained:-1085.73:14.68:0.01:0.00:b:Drained:-986.20:655.48:0.02:-0.01:b:Drained:-686.37:95.81:0.00:0.01:b:Drained:-1290.29:237.87:-1.98:0.25:b:Drained:-2077.43:241.31:0.01:-0.00:b:Drained:-509.72:169.91:-0.01:-0.01:b:Drained:-446.44:478.36:0.01:-0.01:b:Drained:-511.37:411.27:0.00:0.01:b:Drained:-713.69:729.93:0.03:0.00:b:Drained:-767.40:15.06:-0.02:0.00:b:Drained:-298.73:602.88:-0.01:-0.02:b:Drained:-111.62:151.49:0.01:0.00:b:Drained:521.70:194.08:0.01:0.00:b:Drained:1917.25:15.54:0.74:0.00:b:Drained:-384.28:511.76:0.00:-0.01:b:Drained:-1312.95:487.43:0.02:0.01:b:Drained:-32.27:12.27:-0.01:-0.00:b:Drained:-1119.56:125.69:0.01:-0.00:b:Drained:-360.68:775.26:0.02:-0.00:b:Drained:-194.44:512.87:0.01:-0.01:b:Drained:-2203.18:38.03:0.01:0.00:b:Drained:-1547.11:371.88:0.02:-0.00:b:Drained:-297.63:16.37:-0.00:0.00:b:Drained:-555.72:199.20:0.01:-0.01:b:Drained:-30.98:442.31:-0.02:-0.01:b:Drained:-795.34:245.52:0.02:0.01:b:Drained:-96.95:274.60:-0.01:-0.00:b:Drained:-759.14:440.80:0.01:0.02:b:Drained:-1932.59:251.88:0.01:-0.00:b:Drained:-531.33:519.56:0.01:0.00:b:Drained:-348.06:451.52:0.00:-0.01:b:Drained:-446.42:209.88:0.02:-0.01:b:Drained:-900.61:160.01:0.01:-0.00:b:Drained:-495.66:108.98:0.01:-0.02:b:Drained:-1370.23:247.96:0.02:0.00:b:Drained:-439.06:372.20:0.01:-0.00:b:Drained:-1261.61:234.26:0.01:-0.00:b:Drained:-1236.23:116.85:0.01:-0.00:b:Drained:-1659.55:286.49:0.01:-0.01:b:Drained:-1219.20:17.38:0.01:0.00:b:Drained:-2138.99:139.66:0.02:0.00:b:Drained:-42.57:351.46:-0.00:-0.01:b:Drained:-1415.44:13.18:0.01:0.00:b:Drained:-319.46:512.54:0.02:-0.02:b:Drained:-640.77:207.12:-0.01:-0.01:b:Drained:-900.52:191.56:-0.00:0.03:b:Drained:-921.18:23.24:0.01:0.02:b:Drained:-135.15:353.59:-0.00:-0.04:b:Drained:-676.73:207.91:0.01:-0.00:b:Drained:-569.76:115.37:-0.01:-0.01:b:Drained:-1408.98:358.70:0.01:0.00:b:Drained:-849.11:326.40:-0.00:0.02:b:Drained:63.87:409.92:-0.03:-0.01:b:Drained:-409.27:101.42:-0.03:0.00:b:Drained:-2066.90:16.03:0.01:0.00:b:Drained:-763.54:146.81:0.01:0.00:b:Drained:109.40:74.84:0.59:1.91:b:Drained:144.40:467.24:-0.00:-0.02:b:Drained:400.27:260.61:0.01:-0.02:b:Drained:-1359.16:571.10:0.03:-0.00:b:Drained:-138.26:18.29:-0.01:-0.00:b:Drained:-246.32:699.88:0.01:-0.02:b:Drained:456.54:414.98:0.01:-0.01:b:Drained:-1589.52:497.00:0.01:0.01:b:Drained:-765.67:246.00:-0.00:0.01:b:Drained:-2339.34:297.60:0.01:-0.01:b:Drained:-677.64:441.76:0.01:0.00:b:Drained:84.08:218.15:-0.01:-0.01:b:Drained:341.45:390.70:-0.00:-0.03:b:Drained:-353.50:628.00:0.01:-0.02:b:Drained:221.55:50.36:0.01:-0.01:b:Drained:650.01:247.80:-0.00:-0.00:b:Drained:-476.41:422.37:0.02:0.01:b:Drained:363.36:513.08:-0.01:0.01:b:Drained:-1473.59:150.89:0.01:-0.00:b:Drained:-1301.27:371.76:0.00:0.01:b:Drained:-340.54:612.68:0.02:0.01:b:Drained:66.54:717.77:-0.20:1.99:b:Drained:-2220.69:236.50:0.01:0.00:b:Drained:-928.05:284.09:0.01:-0.00:b:Drained:-905.69:53.40:-0.01:0.03:b:Drained:343.89:128.95:0.02:-0.02:b:Drained:-1171.90:334.05:0.01:0.01:b:Drained:-1999.64:127.43:0.01:0.00:b:Drained:168.90:670.35:0.00:-0.01:b:Drained:-1182.37:448.73:0.02:0.01:b:Drained:-231.13:266.14:-0.00:-0.01:b:Drained:-1058.13:314.27:0.03:0.01:b:Drained:-443.53:619.06:0.01:-0.01:b:Drained:-606.38:554.99:0.01:-0.00:b:Drained:-625.83:432.26:0.01:-0.00:b:Drained:-864.56:710.62:0.02:-0.00:b:Drained:-1610.08:165.65:0.01:-0.01:b:Drained:-207.58:109.15:0.01:0.00:b:Drained:-1040.81:438.86:0.01:-0.00:b:Drained:-1798.88:311.28:0.01:-0.00:b:Drained:-52.68:773.78:-0.01:-0.00:b:Drained:-386.53:350.64:0.01:0.00:b:Drained:-713.45:304.22:0.01:0.01:b:Drained:-423.61:486.06:0.01:0.01:b:Drained:-346.04:413.87:-0.01:-0.02:b:Drained:-1539.79:25.63:0.01:-0.01:b:Drained:65.65:726.79:0.01:-0.03:b:Drained:-202.27:407.51:-0.01:-0.00:b:Drained:288.06:598.51:-0.00:-0.01:b:Drained:-128.06:686.07:0.01:-0.05:b:Drained:-482.60:600.40:0.01:-0.01:b:Drained:-788.91:361.63:0.02:-0.00:b:Drained:-99.87:558.60:0.01:-0.01:b:Drained:-318.44:212.56:-0.02:-0.01:b:Drained:76.18:620.24:0.01:-0.02:b:Drained:-428.20:506.10:0.02:0.03:b:Drained:-719.93:586.97:0.01:0.00:b:Drained:-624.81:14.49:-0.02:-0.00:b:Drained:-304.39:473.16:-0.01:-0.01:b:Drained:-631.38:85.91:0.01:-0.01:b:Drained:-1314.48:13.46:0.01:0.00:b:Drained:-296.96:485.21:0.01:-0.00:b:Drained:-10.71:662.80:0.01:-0.03:b:Drained:526.92:316.45:0.00:-0.01:b:Drained:444.34:129.47:-0.02:-0.00:b:Drained:-309.72:332.17:0.00:-0.03:b:Drained:-1206.12:579.68:0.02:0.00:b:Drained:-1655.94:32.99:0.01:-0.01:b:Drained:-528.08:310.79:-0.01:0.01:b:Drained:-292.48:374.32:0.00:-0.01:b:Drained:-997.84:90.55:0.00:0.03:b:Drained:-315.48:237.82:0.01:0.00:b:Drained:-221.46:370.74:-0.01:-0.01:b:Drained:-337.36:311.01:-0.01:0.01:b:Drained:159.72:560.52:-0.01:-0.00:b:Drained:-1107.23:657.33:0.01:0.01:b:Drained:-505.17:517.40:0.01:-0.01:b:Drained:-448.39:464.58:0.00:-0.01:b:Drained:-630.00:519.00:-0.01:0.02:b:Drained:-1699.41:409.83:0.01:0.01:b:Drained:-486.79:302.46:0.02:-0.00:b:Drained:276.87:317.01:0.00:-0.02:b:Drained:-1034.08:202.24:0.01:-0.00:b:Drained:-376.55:125.58:0.01:0.00:b:Drained:-769.42:29.81:0.01:0.00:b:Drained:-1930.96:13.58:0.01:-0.00:b:Drained:-1159.36:220.20:0.02:0.01:b:Drained:-583.98:767.95:0.00:0.00:b:Drained:-2348.24:175.91:0.01:0.00:b:Drained:-10.03:756.86:-1.86:0.74:b:Drained:-1456.07:484.39:0.01:0.00:b:Drained:-1520.06:276.15:0.01:0.00:b:Drained:-808.23:122.98:-0.01:0.02:b:Drained:-279.33:165.63:-0.02:-0.04:b:Drained:-213.68:381.94:-0.00:-0.01:b:Drained:-1339.90:132.52:0.02:0.01:b:Drained:-13.65:224.49:-0.00:0.00:b:Drained:-398.11:366.39:0.00:-0.00:b:Drained:231.17:185.07:-0.02:-0.02:b:Drained:-568.04:284.15:0.00:-0.01:b:Drained:-203.83:262.70:0.00:-0.04:b:Drained:-334.41:340.72:-0.00:-0.00:b:Drained:-930.25:528.28:0.00:-0.01:b:Drained:-358.38:509.22:0.02:0.00:b:Drained:-807.33:487.10:0.02:0.00:b:Drained:1.36:515.69:0.01:-0.02:b:Drained:-490.21:18.19:-0.02:0.00:b:Drained:-104.71:453.55:0.00:-0.03:b:Drained:145.59:337.46:0.00:0.01:b:Drained:-596.55:675.33:0.01:-0.01:b:Drained:-597.97:321.14:0.02:-0.01:b:Drained:-460.94:256.19:-0.02:-0.02:b:Drained:-253.17:503.86:0.00:-0.02:b:Drained:-194.10:809.87:0.03:-0.00:b:Drained:-1003.07:99.00:0.01:-0.00:b:Drained:-575.80:426.37:0.01:0.00:b:Drained:244.40:446.02:-0.01:-0.00:b:Drained:8.14:100.48:0.01:-0.02:b:Drained:91.00:15.46:-0.01:-0.00:b:Drained:119.75:108.13:0.02:-0.02:b:Drained:-831.31:611.23:0.02:0.02:b:Drained:-2242.08:128.36:0.01:0.00:s:Drained:180:181:9.0000:110.00:0:s:Drained:181:182:9.0000:114.13:0:s:Drained:182:180:9.0000:114.13:0:s:Drained:77:181:9.0000:120.16:0:s:Drained:77:182:9.0000:128.00:0:s:Drained:161:77:9.0000:127.83:0:s:Drained:161:182:9.0000:140.00:0:s:Drained:88:161:9.0000:130.05:0:s:Drained:88:77:9.0000:140.00:0:s:Drained:23:180:9.0000:104.67:0:s:Drained:23:181:9.0000:119.80:0:s:Drained:18:67:9.0000:135.17:0:s:Drained:100:67:9.0000:135.73:0:s:Drained:100:18:9.0000:140.00:0:s:Drained:156:100:9.0000:125.30:0:s:Drained:156:18:9.0000:133.37:0:s:Drained:67:29:9.0000:145.32:1:s:Drained:60:156:9.0000:140.00:0:s:Drained:100:29:9.0000:155.80:1:s:Drained:169:60:9.0000:137.75:0:s:Drained:56:60:9.0000:140.00:0:s:Drained:56:169:9.0000:140.00:0:s:Drained:120:56:9.0000:124.95:0:s:Drained:120:169:9.0000:140.00:0:s:Drained:74:182:9.0000:134.92:0:s:Drained:74:161:9.0000:154.04:1:s:Drained:163:156:9.0000:131.37:0:s:Drained:163:18:9.0000:140.00:0:s:Drained:33:163:9.0000:106.49:0:s:Drained:33:18:9.0000:140.00:0:s:Drained:54:33:9.0000:100.00:0:s:Drained:54:163:9.0000:125.55:0:s:Drained:110:54:9.0000:100.00:0:s:Drained:110:163:9.0000:139.33:0:s:Drained:170:110:9.0000:103.44:0:s:Drained:170:54:9.0000:113.74:0:s:Drained:31:170:9.0000:103.53:0:s:Drained:31:54:9.0000:140.00:0:s:Drained:48:54:9.0000:100.00:0:s:Drained:48:31:9.0000:106.88:0:s:Drained:33:48:9.0000:100.75:1:s:Drained:159:33:9.0000:100.00:0:s:Drained:159:48:9.0000:130.96:0:s:Drained:74:48:9.0000:197.54:1:s:Drained:59:31:9.0000:119.21:0:s:Drained:59:48:9.0000:121.14:0:s:Drained:59:74:9.0000:195.85:1:s:Drained:171:59:9.0000:111.62:0:s:Drained:171:74:9.0000:140.00:0:s:Drained:171:161:9.0000:169.42:1:s:Drained:128:163:9.0000:124.12:0:s:Drained:128:110:9.0000:131.37:0:s:Drained:128:156:9.0000:177.87:1:s:Drained:121:110:9.0000:129.46:0:s:Drained:121:128:9.0000:140.00:0:s:Drained:121:170:9.0000:198.36:1:s:Drained:157:170:9.0000:140.00:0:s:Drained:157:121:9.0000:140.00:0:s:Drained:26:157:9.0000:126.47:0:s:Drained:26:170:9.0000:136.55:0:s:Drained:26:31:9.0000:167.70:1:s:Drained:115:26:9.0000:100.00:0:s:Drained:115:31:9.0000:140.00:0:s:Drained:168:31:9.0000:100.00:0:s:Drained:168:115:9.0000:100.00:0:s:Drained:168:59:9.0000:111.78:1:s:Drained:116:128:9.0000:112.62:0:s:Drained:116:60:9.0000:158.48:1:s:Drained:135:121:9.0000:113.31:0:s:Drained:164:135:9.0000:111.39:0:s:Drained:164:121:9.0000:140.00:0:s:Drained:164:157:9.0000:137.32:1:s:Drained:37:164:9.0000:140.00:0:s:Drained:37:157:9.0000:140.00:0:s:Drained:37:26:9.0000:180.19:1:s:Drained:17:37:9.0000:140.00:0:s:Drained:17:26:9.0000:140.00:0:s:Drained:17:115:9.0000:194.71:1:s:Drained:175:37:9.0000:140.00:0:s:Drained:175:17:9.0000:140.00:0:s:Drained:118:175:9.0000:130.79:0:s:Drained:118:37:9.0000:131.87:0:s:Drained:164:118:9.0000:187.25:1:s:Drained:50:118:9.0000:133.23:0:s:Drained:50:164:9.0000:140.00:0:s:Drained:135:50:9.0000:192.23:1:s:Drained:107:50:9.0000:100.00:0:s:Drained:107:135:9.0000:140.00:0:s:Drained:13:50:9.0000:140.00:0:s:Drained:13:107:9.0000:140.00:0:s:Drained:95:118:9.0000:134.21:0:s:Drained:95:50:9.0000:140.00:0:s:Drained:95:13:9.0000:159.03:1:s:Drained:83:95:9.0000:126.66:0:s:Drained:83:118:9.0000:130.57:0:s:Drained:83:175:9.0000:162.82:1:s:Drained:21:83:9.0000:100.00:0:s:Drained:21:175:9.0000:132.16:0:s:Drained:136:107:9.0000:115.69:0:s:Drained:136:135:9.0000:123.04:0:s:Drained:174:136:9.0000:140.00:0:s:Drained:116:174:9.0000:165.47:1:s:Drained:135:116:9.0000:193.46:1:s:Drained:12:56:9.0000:107.53:0:s:Drained:12:60:9.0000:140.00:0:s:Drained:174:12:9.0000:128.55:1:s:Drained:116:12:9.0000:178.28:1:s:Drained:93:135:9.0000:108.58:0:s:Drained:93:136:9.0000:110.98:0:s:Drained:93:116:9.0000:107.52:1:s:Drained:166:83:9.0000:104.02:0:s:Drained:166:95:9.0000:133.33:0:s:Drained:64:59:9.0000:111.00:0:s:Drained:64:171:9.0000:128.97:0:s:Drained:64:168:9.0000:146.12:1:s:Drained:144:161:9.0000:140.00:0:s:Drained:144:171:9.0000:140.00:0:s:Drained:64:144:9.0000:196.19:1:s:Drained:88:144:9.0000:196.83:1:s:Drained:65:88:9.0000:140.00:0:s:Drained:65:144:9.0000:140.00:0:s:Drained:127:88:9.0000:100.00:0:s:Drained:127:65:9.0000:137.98:0:s:Drained:75:144:9.0000:100.00:0:s:Drained:75:65:9.0000:140.00:0:s:Drained:19:127:9.0000:100.00:0:s:Drained:19:65:9.0000:140.00:0:s:Drained:179:64:9.0000:109.48:0:s:Drained:179:144:9.0000:132.19:0:s:Drained:179:75:9.0000:113.62:1:s:Drained:137:64:9.0000:100.00:0:s:Drained:137:179:9.0000:137.80:0:s:Drained:168:137:9.0000:167.13:1:s:Drained:117:137:9.0000:102.46:0:s:Drained:117:168:9.0000:140.00:0:s:Drained:117:115:9.0000:177.53:1:s:Drained:125:117:9.0000:100.00:0:s:Drained:125:115:9.0000:140.00:0:s:Drained:109:125:9.0000:100.00:0:s:Drained:109:117:9.0000:110.16:0:s:Drained:103:109:9.0000:110.56:0:s:Drained:103:125:9.0000:121.18:0:s:Drained:112:103:9.0000:116.68:0:s:Drained:112:125:9.0000:120.12:0:s:Drained:112:26:9.0000:197.82:1:s:Drained:17:112:9.0000:165.08:1:s:Drained:176:103:9.0000:130.10:0:s:Drained:176:112:9.0000:136.51:0:s:Drained:68:17:9.0000:132.37:0:s:Drained:68:112:9.0000:140.00:0:s:Drained:68:176:9.0000:136.10:1:s:Drained:175:68:9.0000:179.99:1:s:Drained:76:21:9.0000:130.69:0:s:Drained:76:175:9.0000:140.00:0:s:Drained:68:76:9.0000:146.44:1:s:Drained:25:76:9.0000:140.00:0:s:Drained:25:68:9.0000:140.00:0:s:Drained:25:176:9.0000:146.36:1:s:Drained:4:25:9.0000:119.44:0:s:Drained:4:76:9.0000:123.62:0:s:Drained:113:21:9.0000:129.70:0:s:Drained:113:76:9.0000:140.00:0:s:Drained:113:4:9.0000:128.68:1:s:Drained:139:113:9.0000:105.69:0:s:Drained:139:21:9.0000:122.54:0:s:Drained:139:166:9.0000:156.03:1:s:Drained:79:139:9.0000:128.79:0:s:Drained:79:166:9.0000:140.00:0:s:Drained:96:113:9.0000:132.47:0:s:Drained:96:139:9.0000:133.78:0:s:Drained:172:96:9.0000:132.47:0:s:Drained:172:113:9.0000:140.00:0:s:Drained:172:4:9.0000:135.65:1:s:Drained:96:79:9.0000:181.31:1:s:Drained:119:96:9.0000:140.00:0:s:Drained:119:172:9.0000:140.00:0:s:Drained:73:119:9.0000:140.00:0:s:Drained:73:96:9.0000:140.00:0:s:Drained:178:73:9.0000:100.00:0:s:Drained:178:96:9.0000:140.00:0:s:Drained:178:79:9.0000:100.00:1:s:Drained:167:119:9.0000:135.68:0:s:Drained:167:73:9.0000:140.00:0:s:Drained:91:109:9.0000:100.00:0:s:Drained:91:117:9.0000:107.17:0:s:Drained:91:137:9.0000:110.16:1:s:Drained:111:91:9.0000:122.86:0:s:Drained:111:137:9.0000:140.00:0:s:Drained:111:179:9.0000:158.40:1:s:Drained:80:111:9.0000:101.69:0:s:Drained:80:179:9.0000:140.00:0:s:Drained:80:75:9.0000:120.43:1:s:Drained:69:75:9.0000:122.52:0:s:Drained:69:80:9.0000:126.86:0:s:Drained:69:65:9.0000:160.84:1:s:Drained:126:69:9.0000:117.29:0:s:Drained:126:65:9.0000:140.00:0:s:Drained:126:19:9.0000:120.06:1:s:Drained:78:19:9.0000:140.00:0:s:Drained:78:126:9.0000:140.00:0:s:Drained:9:56:9.0000:121.13:0:s:Drained:9:120:9.0000:121.61:0:s:Drained:9:12:9.0000:179.88:1:s:Drained:16:9:9.0000:114.76:0:s:Drained:16:120:9.0000:140.00:0:s:Drained:51:9:9.0000:138.38:0:s:Drained:51:12:9.0000:140.00:0:s:Drained:174:51:9.0000:179.62:1:s:Drained:162:51:9.0000:123.45:0:s:Drained:162:174:9.0000:127.50:0:s:Drained:42:136:9.0000:100.05:0:s:Drained:42:174:9.0000:140.00:0:s:Drained:42:162:9.0000:143.07:1:s:Drained:155:16:9.0000:116.04:0:s:Drained:155:9:9.0000:135.60:0:s:Drained:87:155:9.0000:124.88:0:s:Drained:87:16:9.0000:140.00:0:s:Drained:51:155:9.0000:180.12:1:s:Drained:71:155:9.0000:137.21:0:s:Drained:71:51:9.0000:140.00:0:s:Drained:52:155:9.0000:117.93:0:s:Drained:52:87:9.0000:133.28:0:s:Drained:133:87:9.0000:100.00:0:s:Drained:133:52:9.0000:139.45:0:s:Drained:71:52:9.0000:143.73:1:s:Drained:71:162:9.0000:188.94:1:s:Drained:58:71:9.0000:116.84:0:s:Drained:58:52:9.0000:140.00:0:s:Drained:6:71:9.0000:122.83:0:s:Drained:6:58:9.0000:136.11:0:s:Drained:6:162:9.0000:156.66:1:s:Drained:159:180:9.0000:128.81:1:s:Drained:18:180:9.0000:122.00:1:s:Drained:23:67:9.0000:105.42:1:s:Drained:29:60:9.0000:145.70:1:s:Drained:159:74:9.0000:100.00:1:s:Drained:34:6:9.0000:102.94:0:s:Drained:34:58:9.0000:140.00:0:s:Drained:97:6:9.0000:136.46:0:s:Drained:97:34:9.0000:140.00:0:s:Drained:97:162:9.0000:183.20:1:s:Drained:42:97:9.0000:193.51:1:s:Drained:141:97:9.0000:107.01:0:s:Drained:141:34:9.0000:140.00:0:s:Drained:14:97:9.0000:136.63:0:s:Drained:14:141:9.0000:140.00:0:s:Drained:131:14:9.0000:113.06:0:s:Drained:131:97:9.0000:140.00:0:s:Drained:131:107:9.0000:198.66:1:s:Drained:13:131:9.0000:198.47:1:s:Drained:42:131:9.0000:100.00:1:s:Drained:160:14:9.0000:108.77:0:s:Drained:160:131:9.0000:140.00:0:s:Drained:160:13:9.0000:120.25:1:s:Drained:124:13:9.0000:129.58:0:s:Drained:124:160:9.0000:140.00:0:s:Drained:106:160:9.0000:140.00:0:s:Drained:106:14:9.0000:140.00:0:s:Drained:36:141:9.0000:103.26:0:s:Drained:36:14:9.0000:109.47:0:s:Drained:106:36:9.0000:117.75:1:s:Drained:106:124:9.0000:183.28:1:s:Drained:124:95:9.0000:191.86:1:s:Drained:140:166:9.0000:100.00:0:s:Drained:140:95:9.0000:139.88:0:s:Drained:124:140:9.0000:144.65:1:s:Drained:132:106:9.0000:123.95:0:s:Drained:132:124:9.0000:140.00:0:s:Drained:104:132:9.0000:135.63:0:s:Drained:104:124:9.0000:140.00:0:s:Drained:104:140:9.0000:137.50:1:s:Drained:134:132:9.0000:127.31:0:s:Drained:134:104:9.0000:131.66:0:s:Drained:38:104:9.0000:140.00:0:s:Drained:38:134:9.0000:140.00:0:s:Drained:143:38:9.0000:121.08:0:s:Drained:143:104:9.0000:140.00:0:s:Drained:143:140:9.0000:143.95:1:s:Drained:143:79:9.0000:154.41:1:s:Drained:173:178:9.0000:115.65:0:s:Drained:173:79:9.0000:139.69:0:s:Drained:173:143:9.0000:137.50:1:s:Drained:30:38:9.0000:111.58:0:s:Drained:30:143:9.0000:137.19:0:s:Drained:30:173:9.0000:118.56:1:s:Drained:55:30:9.0000:132.00:0:s:Drained:55:173:9.0000:140.00:0:s:Drained:105:55:9.0000:105.09:0:s:Drained:105:173:9.0000:124.04:0:s:Drained:40:30:9.0000:108.40:0:s:Drained:40:38:9.0000:110.68:0:s:Drained:146:40:9.0000:104.99:0:s:Drained:146:38:9.0000:122.31:0:s:Drained:146:134:9.0000:113.21:1:s:Drained:122:40:9.0000:121.61:0:s:Drained:122:30:9.0000:139.43:0:s:Drained:55:122:9.0000:128.13:1:s:Drained:62:55:9.0000:113.96:0:s:Drained:62:122:9.0000:140.00:0:s:Drained:73:105:9.0000:136.88:1:s:Drained:114:167:9.0000:128.97:0:s:Drained:114:73:9.0000:140.00:0:s:Drained:114:55:9.0000:186.50:1:s:Drained:114:105:9.0000:103.18:1:s:Drained:32:62:9.0000:100.00:0:s:Drained:32:55:9.0000:131.00:0:s:Drained:32:114:9.0000:116.16:1:s:Drained:39:32:9.0000:140.00:0:s:Drained:39:62:9.0000:140.00:0:s:Drained:86:39:9.0000:125.47:0:s:Drained:86:32:9.0000:140.00:0:s:Drained:86:114:9.0000:165.64:1:s:Drained:0:86:9.0000:123.36:0:s:Drained:0:114:9.0000:140.00:0:s:Drained:0:167:9.0000:138.18:1:s:Drained:165:0:9.0000:117.39:0:s:Drained:165:167:9.0000:133.59:0:s:Drained:183:165:9.0000:124.13:0:s:Drained:183:167:9.0000:125.84:0:s:Drained:183:119:9.0000:120.79:1:s:Drained:150:172:9.0000:100.00:0:s:Drained:150:4:9.0000:140.00:0:s:Drained:15:150:9.0000:109.76:0:s:Drained:15:172:9.0000:133.59:0:s:Drained:15:119:9.0000:145.08:1:s:Drained:183:15:9.0000:154.99:1:s:Drained:98:183:9.0000:110.55:0:s:Drained:98:15:9.0000:140.00:0:s:Drained:165:98:9.0000:191.86:1:s:Drained:8:98:9.0000:123.10:0:s:Drained:8:165:9.0000:140.00:0:s:Drained:2:8:9.0000:121.29:0:s:Drained:2:165:9.0000:138.93:0:s:Drained:101:2:9.0000:120.57:0:s:Drained:101:165:9.0000:139.17:0:s:Drained:101:0:9.0000:125.75:1:s:Drained:145:86:9.0000:140.00:0:s:Drained:145:39:9.0000:140.00:0:s:Drained:94:145:9.0000:113.85:0:s:Drained:94:86:9.0000:135.62:0:s:Drained:101:94:9.0000:124.78:1:s:Drained:0:94:9.0000:169.34:1:s:Drained:149:145:9.0000:125.85:0:s:Drained:149:94:9.0000:140.00:0:s:Drained:89:149:9.0000:109.96:0:s:Drained:89:94:9.0000:117.23:0:s:Drained:89:101:9.0000:168.49:1:s:Drained:92:89:9.0000:114.62:0:s:Drained:92:101:9.0000:140.00:0:s:Drained:92:2:9.0000:157.07:1:s:Drained:129:92:9.0000:132.40:0:s:Drained:129:2:9.0000:140.00:0:s:Drained:138:2:9.0000:113.88:0:s:Drained:138:129:9.0000:120.79:0:s:Drained:138:8:9.0000:115.50:1:s:Drained:22:92:9.0000:134.36:0:s:Drained:22:129:9.0000:140.00:0:s:Drained:82:89:9.0000:132.55:0:s:Drained:82:92:9.0000:140.00:0:s:Drained:22:82:9.0000:122.16:1:s:Drained:43:149:9.0000:103.31:0:s:Drained:43:89:9.0000:140.00:0:s:Drained:43:82:9.0000:151.46:1:s:Drained:41:43:9.0000:109.97:0:s:Drained:41:82:9.0000:140.00:0:s:Drained:57:82:9.0000:107.57:0:s:Drained:57:41:9.0000:119.70:0:s:Drained:57:22:9.0000:158.92:1:s:Drained:153:57:9.0000:135.04:0:s:Drained:153:22:9.0000:140.00:0:s:Drained:28:153:9.0000:140.00:0:s:Drained:28:57:9.0000:140.00:0:s:Drained:154:28:9.0000:100.00:0:s:Drained:154:57:9.0000:135.06:0:s:Drained:41:154:9.0000:153.40:1:s:Drained:66:22:9.0000:100.00:0:s:Drained:66:153:9.0000:124.99:0:s:Drained:66:129:9.0000:147.41:1:s:Drained:25:150:9.0000:198.33:1:s:Drained:24:149:9.0000:100.00:0:s:Drained:24:145:9.0000:121.96:0:s:Drained:177:145:9.0000:106.68:0:s:Drained:177:24:9.0000:117.15:0:s:Drained:177:39:9.0000:123.54:1:s:Drained:158:41:9.0000:122.40:0:s:Drained:158:43:9.0000:133.47:0:s:Drained:44:158:9.0000:102.49:0:s:Drained:44:43:9.0000:129.61:0:s:Drained:44:24:9.0000:122.11:1:s:Drained:81:158:9.0000:137.70:0:s:Drained:81:41:9.0000:140.00:0:s:Drained:154:81:9.0000:134.81:1:s:Drained:99:81:9.0000:140.00:0:s:Drained:99:154:9.0000:140.00:0:s:Drained:45:28:9.0000:140.00:0:s:Drained:45:154:9.0000:140.00:0:s:Drained:99:45:9.0000:132.52:1:s:Drained:3:45:9.0000:140.00:0:s:Drained:3:99:9.0000:140.00:0:s:Drained:147:62:9.0000:121.50:0:s:Drained:147:122:9.0000:140.00:0:s:Drained:147:39:9.0000:181.94:1:s:Drained:53:177:9.0000:108.06:0:s:Drained:53:39:9.0000:140.00:0:s:Drained:53:147:9.0000:145.81:1:s:Drained:7:24:9.0000:119.63:0:s:Drained:7:177:9.0000:123.40:0:s:Drained:7:53:9.0000:161.52:1:s:Drained:44:7:9.0000:173.99:1:s:Drained:46:44:9.0000:114.20:0:s:Drained:46:7:9.0000:140.00:0:s:Drained:123:46:9.0000:100.00:0:s:Drained:123:44:9.0000:131.51:0:s:Drained:123:158:9.0000:131.52:1:s:Drained:49:123:9.0000:105.33:0:s:Drained:49:158:9.0000:140.00:0:s:Drained:49:81:9.0000:151.81:1:s:Drained:108:49:9.0000:128.51:0:s:Drained:108:81:9.0000:140.00:0:s:Drained:108:99:9.0000:157.62:1:s:Drained:130:108:9.0000:118.71:0:s:Drained:130:99:9.0000:140.00:0:s:Drained:130:3:9.0000:172.66:1:s:Drained:5:130:9.0000:113.80:0:s:Drained:5:3:9.0000:140.00:0:s:Drained:1:3:9.0000:124.69:0:s:Drained:1:5:9.0000:130.07:0:s:Drained:102:3:9.0000:140.00:0:s:Drained:102:45:9.0000:140.00:0:s:Drained:102:1:9.0000:192.21:1:s:Drained:142:45:9.0000:132.40:0:s:Drained:142:102:9.0000:135.21:0:s:Drained:28:142:9.0000:155.15:1:s:Drained:70:153:9.0000:130.39:0:s:Drained:70:28:9.0000:135.48:0:s:Drained:142:70:9.0000:137.02:1:s:Drained:35:102:9.0000:140.00:0:s:Drained:35:1:9.0000:140.00:0:s:Drained:90:1:9.0000:138.01:0:s:Drained:90:35:9.0000:140.00:0:s:Drained:148:90:9.0000:135.75:0:s:Drained:148:1:9.0000:140.00:0:s:Drained:5:148:9.0000:169.29:1:s:Drained:61:90:9.0000:128.91:0:s:Drained:61:148:9.0000:140.00:0:s:Drained:11:90:9.0000:140.00:0:s:Drained:11:35:9.0000:140.00:0:s:Drained:47:11:9.0000:117.49:0:s:Drained:47:90:9.0000:140.00:0:s:Drained:61:47:9.0000:147.06:1:s:Drained:27:47:9.0000:117.80:0:s:Drained:27:61:9.0000:140.00:0:s:Drained:85:47:9.0000:129.10:0:s:Drained:85:11:9.0000:140.00:0:s:Drained:184:27:9.0000:100.00:0:s:Drained:184:47:9.0000:104.85:0:s:Drained:184:85:9.0000:110.01:1:s:Drained:151:184:9.0000:118.24:0:s:Drained:151:85:9.0000:139.21:0:s:Drained:72:151:9.0000:122.82:0:s:Drained:72:85:9.0000:133.51:0");

    TowerRenderer tr = new TowerRenderer(t);

    tr.rotateTest();

    tr.render();
    System.err.println("Writing");
    ImageIO.write(tr.getThumbnail(), "PNG", new File("tmp.png"));
    System.err.println("Written");
  }
}
