package com.goofans.datamining.balls;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.*;
import java.awt.image.BufferedImage;

import com.goofans.gootool.wog.WorldOfGoo;
import com.goofans.gootoolsp.leveledit.resource.Ball;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GenerateBallImages
{
  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "HardcodedFileSeparator", "StringConcatenation", "DuplicateStringLiteralInspection"})
  public static void main(String[] args) throws IOException
  {
    WorldOfGoo.getTheInstance().init();

    File ballsDir = WorldOfGoo.getTheInstance().getCustomGameFile("res/balls");

    File[] ballsDirs = ballsDir.listFiles();
    for (File dir : ballsDirs) {
      if (dir.isDirectory() && !dir.getName().startsWith("_")) {
        Ball ball = new Ball(dir.getName());

        BufferedImage image = ball.getImageInState("walking", new Dimension(200, 150));
        ImageIO.write(image, "PNG", new File("ball_images", dir.getName() + ".png"));
      }
    }
  }
}