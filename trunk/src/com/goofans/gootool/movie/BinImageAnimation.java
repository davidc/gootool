package com.goofans.gootool.movie;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BinImageAnimation
{
  private static final int KEYFRAME_LENGTH = 32;

  private static final int XFORM_SCALE = 0;
  private static final int XFORM_ROTATE = 1;
  private static final int XFORM_TRANSLATE = 2;

  private boolean hasColor;
  private boolean hasAlpha;
  private boolean hasSound;
  private boolean hasTransform;
  private int numTransforms;
  private int numFrames;

  private TransformType[] transformTypes;
  private float[] frameTimes;
  private KeyFrame[][] transformFrames;
  private KeyFrame[] alphaFrames;

  public BinImageAnimation(File file) throws IOException
  {
    FileInputStream is = new FileInputStream(file);

    int fileLength = (int) file.length();
    byte[] contents = new byte[fileLength];
    if (is.read(contents) != fileLength) {
      throw new IOException("short read on anim " + file.getName());
    }
    is.close();

    init(contents, 0);
  }

  public BinImageAnimation(byte[] contents, int offset)
  {
    init(contents, offset);
  }

  private void init(byte[] contents, int offset)
  {
    hasColor = BinaryFormat.getInt(contents, offset + 0) != 0;
    System.out.println("hasColor = " + hasColor);
    hasAlpha = BinaryFormat.getInt(contents, offset + 4) != 0;
    System.out.println("hasAlpha = " + hasAlpha);
    hasSound = BinaryFormat.getInt(contents, offset + 8) != 0;
    System.out.println("hasSound = " + hasSound);
    hasTransform = BinaryFormat.getInt(contents, offset + 12) != 0;
    System.out.println("hasTransform = " + hasTransform);
    numTransforms = BinaryFormat.getInt(contents, offset + 16);
    System.out.println("numTransforms = " + numTransforms);
    numFrames = BinaryFormat.getInt(contents, offset + 20);
    System.out.println("numFrames = " + numFrames);

    int transformTypesOffset = offset + BinaryFormat.getInt(contents, offset + 24);
    System.out.println("transformTypesOffset = " + transformTypesOffset);
    int frameTimesOffset = offset + BinaryFormat.getInt(contents, offset + 28);
    System.out.println("frameTimesOffset = " + frameTimesOffset);
    int xformFramesOffset = offset + BinaryFormat.getInt(contents, offset + 32);
    System.out.println("xformFramesOffset = " + xformFramesOffset);
    int alphaFramesOffset = offset + BinaryFormat.getInt(contents, offset + 36);
    System.out.println("alphaFramesOffset = " + alphaFramesOffset);
    int colorFramesOffset = offset + BinaryFormat.getInt(contents, offset + 40);
    System.out.println("colorFramesOffset = " + colorFramesOffset);
    int soundFramesOffset = offset + BinaryFormat.getInt(contents, offset + 44);
    System.out.println("soundFramesOffset = " + soundFramesOffset);
    int stringTableOffset = offset + BinaryFormat.getInt(contents, offset + 48);
    System.out.println("stringTableOffset = " + stringTableOffset);

    frameTimes = new float[numFrames];
    for (int i = 0; i < numFrames; ++i) {
      frameTimes[i] = BinaryFormat.getFloat(contents, frameTimesOffset + (i * 4));
      System.out.println("frameTimes[" + i + "] = " + frameTimes[i]);
    }

    if (hasTransform) {
      transformTypes = new TransformType[numTransforms];
      int transformTypeOffset = transformTypesOffset;
      for (int i = 0; i < numTransforms; ++i, transformTypeOffset += 4) {
        transformTypes[i] = TransformType.getByValue(BinaryFormat.getInt(contents, transformTypeOffset));
        System.out.println("transformTypes[" + i + "] = " + transformTypes[i]);
      }

      transformFrames = new KeyFrame[numTransforms][];
      int transformOffset = xformFramesOffset;
      for (int i = 0; i < numTransforms; ++i, transformOffset += 4) {

        int frameOffset = BinaryFormat.getInt(contents, transformOffset);
        System.out.println("frameOffset = " + frameOffset);

        transformFrames[i] = new KeyFrame[numFrames + 1];
        for (int j = 0; j <= numFrames; ++j, frameOffset += KEYFRAME_LENGTH) {
          int framePointer = BinaryFormat.getInt(contents, frameOffset);
          System.out.println("framePointer = " + framePointer);
          if (framePointer != 0) {
            transformFrames[i][j] = new KeyFrame(contents, framePointer, stringTableOffset);
            System.out.println("transformFrames[t=" + i + "," + transformTypes[i] + "][frame=" + j + "] = " + transformFrames[i][j]);
          }
        }
      }
    }

    if (hasAlpha) {
      alphaFrames = new KeyFrame[numFrames + 1];
      int frameOffset = alphaFramesOffset;
      for (int i = 0; i <= numFrames; ++i, frameOffset += 4) {
        int framePointer = BinaryFormat.getInt(contents, frameOffset);
        System.out.println("framePointer = " + framePointer);
        if (framePointer != 0) {
          alphaFrames[i] = new KeyFrame(contents, framePointer, stringTableOffset);
          System.out.println("alphaFrames[" + i + "] = " + alphaFrames[i]);
        }
      }
    }
    if (hasColor) {
      throw new RuntimeException("color frames not yet supported");
    }
    if (hasSound) {
      throw new RuntimeException("sound frames not yet supported");
    }
  }

  public static void main(String[] args) throws IOException
  {
//    BinImageAnimation anim = new BinImageAnimation(new File("C:\\blah\\res\\anim\\ball_counter_ocd.anim.binltl"));
    BinImageAnimation anim = new BinImageAnimation(new File("C:\\blah\\res\\anim\\rot_1rps.anim.binltl"));

    System.out.println("anim = " + anim);
  }
}
