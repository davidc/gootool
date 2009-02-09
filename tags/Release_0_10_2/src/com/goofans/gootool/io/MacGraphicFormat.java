package com.goofans.gootool.io;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.goofans.gootool.util.DebugUtil;

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
    int height = readUnsignedShort(is);

    int squareSide = 1;
    while (squareSide < width || squareSide < height) squareSide *= 2;

    int compressedSize = readUnsignedInt(is);
    int uncompressedSize = readUnsignedInt(is);

    byte[] compressedData = new byte[compressedSize];
    if (is.read(compressedData) != compressedSize) {
      throw new EOFException("Short read on compressed data, expected " + compressedSize);
    }

    is.close();

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

    ComponentColorModel colorModel = getColorModel();
    PixelInterleavedSampleModel sampleModel = getSampleModel(width, height, squareSide);

    DataBufferByte imageData = new DataBufferByte(uncompressedData, uncompressedSize);
    WritableRaster raster = Raster.createWritableRaster(sampleModel, imageData, new Point(0, 0));

    return new BufferedImage(colorModel, raster, false, null);
  }

  private static PixelInterleavedSampleModel getSampleModel(int width, int height, int squareSide)
  {
    int[] bandOffsets = new int[]{0, 1, 2, 3};
    PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_BYTE, width, height, 4, squareSide * 4, bandOffsets);
    return sampleModel;
  }

  public static void encodeImage(File file, Image image) throws IOException
  {
    int width = image.getWidth(null);
    int height = image.getHeight(null);

    // Need to make a square first

    int squareSide = 1;
    while (squareSide < width || squareSide < height) squareSide *= 2;

    ComponentColorModel colorModel = getColorModel();
    PixelInterleavedSampleModel sampleModel = getSampleModel(squareSide, squareSide, squareSide);

    WritableRaster raster = Raster.createWritableRaster(sampleModel, new Point(0, 0));
    BufferedImage squareImage = new BufferedImage(colorModel, raster, false, null);
    squareImage.getGraphics().drawImage(image, 0, 0, null);

    // Get the RGBA bytes from this
    DataBufferByte imageData = (DataBufferByte) squareImage.getRaster().getDataBuffer();

    byte[] uncompressedData = imageData.getData();
    int uncompressedSize = uncompressedData.length;

    // Compress data

    ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);

    Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
    deflater.setInput(uncompressedData);
    deflater.finish();
    byte[] tmpbuf = new byte[1024];

    int n;
    while (!deflater.finished()) {
      n = deflater.deflate(tmpbuf);
      bos.write(tmpbuf, 0, n);
    }
    deflater.end();

    byte[] compressedData = bos.toByteArray();
    int compressedSize = compressedData.length;

    // Now write to the file

    OutputStream os = new FileOutputStream(file);

    writeUnsignedShort(os, width);
    writeUnsignedShort(os, height);
    writeUnsignedInt(os, compressedSize);
    writeUnsignedInt(os, uncompressedSize);

    os.write(compressedData);

    os.close();

    deflater.end();
  }

  private static ComponentColorModel getColorModel()
  {
    ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
    int[] pixInfo = new int[]{8, 8, 8, 8};
    ComponentColorModel colorModel = new ComponentColorModel(colorSpace, pixInfo, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_BYTE);
    return colorModel;
  }

  private static int readUnsignedShort(InputStream is) throws IOException
  {
    byte[] tmp = new byte[2];
    if (is.read(tmp, 0, 2) != 2)
      throw new EOFException("End of file reading unsignedShort");
    return ((int) tmp[0] & 0xff) + (((int) tmp[1] & 0xff) << 8);
  }

  private static void writeUnsignedShort(OutputStream os, int value) throws IOException
  {
    os.write(value & 0xff);
    os.write((value >> 8) & 0xff);
  }

  private static int readUnsignedInt(InputStream is) throws IOException
  {
    byte[] tmp = new byte[4];
    if (is.read(tmp, 0, 4) != 4)
      throw new EOFException("End of file reading unsignedInt");
    return ((int) tmp[0] & 0xff) + (((int) tmp[1] & 0xff) << 8) + (((int) tmp[2] & 0xff) << 16) + (((int) tmp[3] & 0xff) << 24);
  }

  private static void writeUnsignedInt(OutputStream os, int value) throws IOException
  {
    os.write(value & 0xff);
    os.write((value >> 8) & 0xff);
    os.write((value >> 16) & 0xff);
    os.write((value >> 24) & 0xff);
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  public static void main(String[] args) throws IOException
  {
//    showImageWindow(decodeImage(new File("bg.png.binltl")));

    BufferedImage image = decodeImage(new File("cliff_left.png.binltl"));

    File tmpfile = new File("cliff_left-OUT.png.binltl");
    encodeImage(tmpfile, image);
    DebugUtil.showImageWindow(decodeImage(tmpfile));

    image = decodeImage(new File("cliff_right.png.binltl"));

    tmpfile = new File("cliff_right-OUT.png.binltl");
    encodeImage(tmpfile, image);
    DebugUtil.showImageWindow(decodeImage(tmpfile));

//    ImageIO.write(image, "PNG", new File("cliff_left.png"));
  }
}
