package com.goofans.gootool.io;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import sun.awt.image.ByteArrayImageSource;

/**
 * Handles encoding and decoding of the Mac .png.binltl raster format.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MacGraphicFormat
{
  private MacGraphicFormat()
  {
  }

  public static BufferedImage decodeImage(File file) throws IOException
  {
    InputStream is = new FileInputStream(file);

    int width = readUnsignedShort(is);
//    System.out.println("width = " + width);
    int height = readUnsignedShort(is);
//    System.out.println("height = " + height);

    int squareSide = 1;
    while (squareSide < width || squareSide < height) squareSide *= 2;
//    System.out.println("squareSide = " + squareSide);

    int compressedSize = readUnsignedInt(is);
//    System.out.println("compressedSize = " + compressedSize);
    int uncompressedSize = readUnsignedInt(is);
//    System.out.println("uncompressedSize = " + uncompressedSize);

    byte[] compressedData = new byte[compressedSize];
    if (is.read(compressedData) != compressedSize) {
      throw new EOFException("Short read on compressed data, expected " + compressedSize);
    }

    Inflater inflater = new Inflater();
    inflater.setInput(compressedData);

    byte[] uncompressedData = new byte[uncompressedSize];
    int gotBytes;
    try {
      gotBytes = inflater.inflate(uncompressedData);
    }
    catch (DataFormatException e) {
      throw new IOException("zlib compression format error: " + e.getMessage());
    }
    inflater.end();
    if (gotBytes != uncompressedSize) {
      throw new IOException("Uncompressed size is not " + uncompressedSize + ", we got " + gotBytes);
    }

    // TODO: colour seems to be slightly off (at least in screenshot)

    ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    int[] pixInfo = new int[]{8, 8, 8, 8};
    ComponentColorModel colorModel = new ComponentColorModel(colorSpace, pixInfo, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);

    int[] bandOffsets = new int[]{0, 1, 2, 3};
    PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width, height, 4, squareSide * 4, bandOffsets);

    DataBufferByte imageData = new DataBufferByte(uncompressedData, uncompressedSize);
    WritableRaster raster = Raster.createWritableRaster(sampleModel, imageData, new Point(0, 0));

    return new BufferedImage(colorModel, raster, false, null);
  }

  private static void showImageWindow(BufferedImage image)
  {
    JDialog dlg = new JDialog((Frame) null, "Image", true);
    dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    JLabel imgLabel = new JLabel(new ImageIcon(image));
    Dimension d = new Dimension(image.getWidth(null), image.getHeight(null));
    imgLabel.setPreferredSize(d);
    dlg.getContentPane().add(imgLabel);
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
    showImageWindow(decodeImage(new File("bg.png.binltl")));
    RenderedImage image = decodeImage(new File("cliff_left.png.binltl"));

    ImageIO.write(image, "PNG", new File("cliff_left.png"));
  }
}
