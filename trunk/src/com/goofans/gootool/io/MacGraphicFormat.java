package com.goofans.gootool.io;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.RenderedImage;
import java.io.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MacGraphicFormat
{
  private MacGraphicFormat()
  {
  }

  public static RenderedImage decodeImage(File file) throws IOException
  {
    System.out.println("file.length() = " + file.length());
    InputStream is = new FileInputStream(file);

    int width = readUnsignedShort(is);
    System.out.println("width = " + width);
    int height = readUnsignedShort(is);
    System.out.println("height = " + height);

    int squareSide = 1;
    while (squareSide < width || squareSide < height) squareSide *= 2;
    System.out.println("squareSide = " + squareSide);

    int compressedSize = readUnsignedInt(is);
    System.out.println("compressedSize = " + compressedSize);
    int uncompressedSize = readUnsignedInt(is);
    System.out.println("uncompressedSize = " + uncompressedSize);

    byte[] compressedData = new byte[compressedSize];
    if (is.read(compressedData) != compressedSize) {
      throw new EOFException("Short read on compressed data, expected " + compressedSize);
    }

    Inflater compresser = new Inflater();
    compresser.setInput(compressedData);

    byte[] uncompressedData = new byte[uncompressedSize];
    int gotBytes;
    try {
      gotBytes = compresser.inflate(uncompressedData);
    }
    catch (DataFormatException e) {
      throw new IOException("zlib compression format error: " + e.getMessage());
    }
    compresser.end();
    if (gotBytes != uncompressedSize) {
      throw new IOException("Uncompressed size is not " + uncompressedSize + ", we got " + gotBytes);
    }

    System.out.println("uncompressedData.length = " + uncompressedData.length);

//    DataBuffer buf = new DataBufferByte(uncompressedData, uncompressedSize);
//    SampleModel sm = new BandedSampleModel(DataBuffer.TYPE_BYTE, squareSide, squareSide, squareSide);
//WritableRaster raster = Raster.createWritableRaster(sm, buf, new Point(0,0));

    File f = new File("output.raw");
    FileOutputStream fos = new FileOutputStream(f);
    fos.write(uncompressedData);
    fos.close();

    MemoryImageSource producer = new MemoryImageSource(squareSide, squareSide, new DirectColorModel(32, 0xff, 0xff00, 0xff0000, 0xff000000), uncompressedData, 0, squareSide * 4);
    Image srcImage = Toolkit.getDefaultToolkit().createImage(producer);
//        BufferedImageFilter fil = new BufferedImageFilter(new RescaleOp(1, 0, null));
//    fil.


    showImageWindow(srcImage);

    BufferedImage destImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//    ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.TYPE_RGB), true, false, 0, DataBuffer.TYPE_BYTE);
//    BufferedImage destImg = new BufferedImage(ColorModel.getRGBdefault(), raster, false, null);
//    boolean b = destImg.getGraphics().drawImage(srcImage, 0, 0, null);
//    ((Graphics2D)destImg.getGraphics()).drawRenderedImage();
    destImg.getGraphics().drawImage(srcImage, 0, 0, null);

    return destImg;
  }

  private static void showImageWindow(Image image)
  {
    JDialog dlg = new JDialog((Frame) null, "Image", true);
    JPanel rootPanel = new JPanel();
    dlg.add(rootPanel);
    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    JLabel imgLabel = new JLabel(new ImageIcon(image));
    Dimension d = new Dimension(image.getWidth(null), image.getHeight(null));
    imgLabel.setPreferredSize(d);
    rootPanel.add(imgLabel);
    dlg.pack();
    dlg.setVisible(true);
  }


  private static int readUnsignedShort(InputStream is) throws IOException
  {
    byte[] tmp = new byte[2];
    if (is.read(tmp, 0, 2) != 2)
      throw new EOFException("End of file reading unsignedShort");
    return ((int) tmp[0] & 0xff) + (((int) tmp[1] & 0xff) << 8);
  }

  private static int readUnsignedInt(InputStream is) throws IOException
  {
    byte[] tmp = new byte[4];
    if (is.read(tmp, 0, 4) != 4)
      throw new EOFException("End of file reading unsignedInt");
    return ((int) tmp[0] & 0xff) + (((int) tmp[1] & 0xff) << 8) + (((int) tmp[2] & 0xff) << 16) + (((int) tmp[3] & 0xff) << 24);
  }

  public static void main(String[] args) throws IOException
  {
    RenderedImage image = decodeImage(new File("cliff_left.png.binltl"));

    ImageIO.write(image, "PNG", new File("cliff_left.png"));
  }
}
