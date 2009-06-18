package com.goofans.gootool.movie;

import net.infotrek.util.XMLStringBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import com.goofans.gootool.util.Utilities;

/**
 * TODO Do some basic validation on the file before trying to load it.
 * 
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BinImageAnimation
{
//  private static final int KEYFRAME_LENGTH = 32;

  private boolean hasColor;
  private boolean hasAlpha;
  boolean hasSound;
  private boolean hasTransform;
  private int numTransforms;
  private int numFrames;

  private TransformType[] transformTypes;
  private float[] frameTimes;
  private KeyFrameTransform[][] transformFrames;
  private KeyFrameAlpha[] alphaFrames;
  private KeyFrameColor[] colorFrames;
  private KeyFrameSound[] soundFrames;

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

  private BinImageAnimation()
  {
  }

  private void init(byte[] contents, int offset)
  {
    hasColor = BinaryFormat.getInt(contents, offset + 0) != 0;
    hasAlpha = BinaryFormat.getInt(contents, offset + 4) != 0;
    hasSound = BinaryFormat.getInt(contents, offset + 8) != 0;
    hasTransform = BinaryFormat.getInt(contents, offset + 12) != 0;
    numTransforms = BinaryFormat.getInt(contents, offset + 16);
    numFrames = BinaryFormat.getInt(contents, offset + 20);

    int transformTypesOffset = offset + BinaryFormat.getInt(contents, offset + 24);
    int frameTimesOffset = offset + BinaryFormat.getInt(contents, offset + 28);
    int xformFramesOffset = offset + BinaryFormat.getInt(contents, offset + 32);
    int alphaFramesOffset = offset + BinaryFormat.getInt(contents, offset + 36);
    int colorFramesOffset = offset + BinaryFormat.getInt(contents, offset + 40);
    int soundFramesOffset = offset + BinaryFormat.getInt(contents, offset + 44);
    int stringTableOffset = offset + BinaryFormat.getInt(contents, offset + 48);

    frameTimes = new float[numFrames];
    for (int i = 0; i < numFrames; ++i) {
      frameTimes[i] = BinaryFormat.getFloat(contents, frameTimesOffset + (i * 4));
//      System.out.println("frameTimes[" + i + "] = " + frameTimes[i]);
    }

    if (hasTransform) {
      loadTransformTypes(contents, transformTypesOffset);

      loadTransformFrames(contents, offset, xformFramesOffset, stringTableOffset);
    }

    if (hasAlpha) {
      loadAlphaFrames(contents, offset, alphaFramesOffset, stringTableOffset);
    }

    if (hasColor) {
      loadColorFrames(contents, offset, colorFramesOffset, stringTableOffset);
    }

    if (hasSound) {
      loadSoundFrames(contents, offset, soundFramesOffset, stringTableOffset);
    }

    validateFrames();
  }

  private void loadTransformTypes(byte[] contents, int transformTypesOffset)
  {
    transformTypes = new TransformType[numTransforms];

    int transformTypeOffset = transformTypesOffset;
    for (int i = 0; i < numTransforms; ++i, transformTypeOffset += 4) {
      transformTypes[i] = TransformType.getByValue(BinaryFormat.getInt(contents, transformTypeOffset));
//      System.out.println("transformTypes[" + i + "] = " + transformTypes[i]);
    }
  }

  private void loadTransformFrames(byte[] contents, int offset, int xformFramesOffset, int stringTableOffset)
  {
    transformFrames = new KeyFrameTransform[numTransforms][];
    int transformOffset = xformFramesOffset;
    for (int i = 0; i < numTransforms; ++i, transformOffset += 4) {
//      System.out.println("transformOffset = " + transformOffset);

      // pointer to list of frames in this transform
      int frameOffset = BinaryFormat.getInt(contents, transformOffset);
//      System.out.println("frameOffset = " + frameOffset);

      if (frameOffset > 0) {
        frameOffset += offset;

        transformFrames[i] = new KeyFrameTransform[numFrames];
        for (int j = 0; j < numFrames; ++j, frameOffset += 4) {
          // pointer to frame itself
          int framePointer = BinaryFormat.getInt(contents, frameOffset);
//          System.out.println("framePointer = " + framePointer);
          if (framePointer > 0) {
            framePointer += offset;
            transformFrames[i][j] = new KeyFrameTransform(contents, framePointer, stringTableOffset, transformTypes[i]);
//            System.out.println("transformFrames[t=" + i + "," + transformTypes[i] + "][frame=" + j + "] = " + transformFrames[i][j]);
          }
        }
      }
    }
  }

  private void loadAlphaFrames(byte[] contents, int offset, int alphaFramesOffset, int stringTableOffset)
  {
    alphaFrames = new KeyFrameAlpha[numFrames];
    int frameOffset = alphaFramesOffset;
    for (int i = 0; i < numFrames; ++i, frameOffset += 4) {
      int framePointer = BinaryFormat.getInt(contents, frameOffset);
//      System.out.println("framePointer = " + framePointer);
      if (framePointer > 0) {
        framePointer += offset;
        alphaFrames[i] = new KeyFrameAlpha(contents, framePointer, stringTableOffset);
//        System.out.println("alphaFrames[" + i + "] = " + alphaFrames[i]);
      }
    }
  }

  private void loadColorFrames(byte[] contents, int offset, int colorFramesOffset, int stringTableOffset)
  {
    colorFrames = new KeyFrameColor[numFrames];
    int frameOffset = colorFramesOffset;
    for (int i = 0; i < numFrames; ++i, frameOffset += 4) {
      int framePointer = BinaryFormat.getInt(contents, frameOffset);
//      System.out.println("framePointer = " + framePointer);
      if (framePointer > 0) {
        framePointer += offset;
        colorFrames[i] = new KeyFrameColor(contents, framePointer, stringTableOffset);
//        System.out.println("colorFrames[" + i + "] = " + colorFrames[i]);
      }
    }
  }

  private void loadSoundFrames(byte[] contents, int offset, int soundFramesOffset, int stringTableOffset)
  {
    soundFrames = new KeyFrameSound[numFrames];
    int frameOffset = soundFramesOffset;
    for (int i = 0; i < numFrames; ++i, frameOffset += 4) {
      int framePointer = BinaryFormat.getInt(contents, frameOffset);
//      System.out.println("framePointer = " + framePointer);
      if (framePointer > 0) {
        framePointer += offset;
        soundFrames[i] = new KeyFrameSound(contents, framePointer, stringTableOffset);
//        System.out.println("soundFrames[" + i + "] = " + soundFrames[i]);
      }
    }
  }

  public void validateFrames()
  {
    if (hasTransform) {
//      if (numTransforms != 3) throw new AssertionError(this);
//      if (transformTypes[0] != TransformType.SCALE) throw new AssertionError(this);
//      if (transformTypes[1] != TransformType.ROTATE) throw new AssertionError(this);
//      if (transformTypes[2] != TransformType.TRANSLATE) throw new AssertionError(this);

      // check that the same frames exist in all types
      // check that the interpolation is the same too
      for (int frame = 0; frame < numFrames; frame++) {
        boolean isNull = transformFrames[0][frame] == null;
        if (isNull) {
//          if (transformFrames[1][frame] != null) throw new AssertionError(this);
//          if (transformFrames[2][frame] != null) throw new AssertionError(this);
//          if (hasAlpha && alphaFrames[frame] != null) throw new AssertionError(this);
//          if (hasColor && colorFrames[frame] != null) throw new AssertionError(this);
//          if (hasSound && soundFrames[frame] != null) throw new AssertionError(this);
        }
        else {
//          if (transformFrames[1][frame] == null) throw new AssertionError(this);
//          if (transformFrames[2][frame] == null) throw new AssertionError(this);
//          if (transformFrames[0][frame].nextFrameIndex != transformFrames[1][frame].nextFrameIndex) throw new AssertionError(this);
//          if (transformFrames[0][frame].nextFrameIndex != transformFrames[2][frame].nextFrameIndex) throw new AssertionError(this);
//          if (transformFrames[0][frame].interpolationType != transformFrames[1][frame].interpolationType) throw new AssertionError(this);
//          if (transformFrames[0][frame].interpolationType != transformFrames[2][frame].interpolationType) throw new AssertionError(this);
//          if (hasAlpha && alphaFrames[frame].nextFrameIndex != transformFrames[0][frame].nextFrameIndex) throw new AssertionError(this);
//          if (hasAlpha && alphaFrames[frame].interpolationType != transformFrames[0][frame].interpolationType) throw new AssertionError(this);
//          if (hasColor && colorFrames[frame].nextFrameIndex != transformFrames[0][frame].nextFrameIndex) throw new AssertionError(this);
//          if (hasColor && colorFrames[frame].interpolationType != transformFrames[0][frame].interpolationType) throw new AssertionError(this);
        }
      }

      for (int i = 0; i < transformTypes.length; i++) {
        validateFrameList(transformFrames[i]);
        TransformType transformType = transformTypes[i];
        for (int j = 0; j < transformFrames[i].length; j++) {
          KeyFrame frame = transformFrames[i][j];
          if (frame != null) {
            if (transformType == TransformType.SCALE && frame.x == -1) throw new AssertionError(frame);
            if (transformType == TransformType.ROTATE && frame.x != -1) throw new AssertionError(frame);
            if (transformType == TransformType.TRANSLATE && frame.x == -1) throw new AssertionError(frame);
            if (transformType == TransformType.SCALE && frame.y == -1) throw new AssertionError(frame);
            if (transformType == TransformType.ROTATE && frame.y != -1) throw new AssertionError(frame);
            if (transformType == TransformType.TRANSLATE && frame.y == -1) throw new AssertionError(frame);
            if (transformType == TransformType.ROTATE && frame.angle == -1) throw new AssertionError(frame);
            if (transformType != TransformType.ROTATE && frame.angle != -1) throw new AssertionError(frame);
            if (frame.alpha != -1) throw new AssertionError(frame);
            if (frame.color != -1) throw new AssertionError(frame);
            if (frame.soundStrIndex > 0) throw new AssertionError(frame);
            if (frame.interpolationType != 0 && frame.interpolationType != 1) throw new AssertionError(frame);
          }
        }
      }
    }

    if (hasAlpha) {
      validateFrameList(alphaFrames);
      for (KeyFrame frame : alphaFrames) {
        if (frame != null) {
          if (frame.x != -1) throw new AssertionError(frame);
          if (frame.y != -1) throw new AssertionError(frame);
          if (frame.angle != -1) throw new AssertionError(frame);
          if (frame.alpha == -1) throw new AssertionError(frame);
          if (frame.color != -1) throw new AssertionError(frame);
          if (frame.soundStrIndex > 0) throw new AssertionError(frame);
          if (frame.interpolationType != 0 && frame.interpolationType != 1) throw new AssertionError(frame);
        }
      }
    }

    if (hasColor) {
      validateFrameList(colorFrames);
      for (KeyFrame frame : colorFrames) {
        if (frame != null) {
          if (frame.x != -1) throw new AssertionError(frame);
          if (frame.y != -1) throw new AssertionError(frame);
          if (frame.angle != -1) throw new AssertionError(frame);
          if (frame.alpha != -1) throw new AssertionError(frame);
//          if (frame.color == -1) throw new AssertionError(frame); //TODO
          if (frame.soundStrIndex > 0) throw new AssertionError(frame);
//          if (frame.interpolationType != 0) throw new AssertionError(frame);
          if (frame.interpolationType != 0 && frame.interpolationType != 1) throw new AssertionError(frame);
        }
      }
    }

    if (hasSound) {
      validateFrameList(soundFrames);
      for (KeyFrame frame : soundFrames) {
        if (frame != null) {
          if (frame.x != -1) throw new AssertionError(frame);
          if (frame.y != -1) throw new AssertionError(frame);
          if (frame.angle != -1) throw new AssertionError(frame);
          if (frame.alpha != -1) throw new AssertionError(frame);
          if (frame.color != -1) throw new AssertionError(frame);
          if (frame.soundStrIndex < 1) throw new AssertionError(frame);
          if (frame.interpolationType != 0 && frame.interpolationType != 1) throw new AssertionError(frame);
        }
      }
    }
  }

  private void validateFrameList(KeyFrame[] frameList)
  {
    // assert that no skipped frames exist
    boolean[] used = new boolean[numFrames];
    int pos = 0;
    // locate the first frame
    while (frameList[pos] == null && pos < frameList.length) {
      pos++;
    }
    if (pos >= numFrames) {
      throw new AssertionError("No frames found!");
    }

    while (pos != -1) {
      used[pos] = true;
      pos = frameList[pos].nextFrameIndex;
    }
    for (int i = 0; i < numFrames; i++) {
      if (!used[i] && frameList[i] != null)
        throw new AssertionError("frame " + i + " exists but is unused");
      if (used[i] && frameList[i] == null)
        throw new AssertionError("frame " + i + " is used but doesn't exist");
    }
  }

  /**
   * Produces a well-formed XML document for this BinImageAnimation.
   *
   * @return XML document string
   */
  public String toXMLDocument()
  {
    StringBuffer sb = new StringBuffer();
    // TODO xml prolog
    XMLStringBuffer xml = new XMLStringBuffer(sb, "");
    xml.addComment("This XML format is subject to change. Do not program against this format yet!");
    xml.addComment("See: http://goofans.com/forum/world-of-goo/modding/407");
    toXML(xml);
    return xml.toXML();
  }

  /**
   * Produces an &lt;anim&gt; element, not a well-formed document.
   *
   * @param xml XMLStringBuffer to write into
   */
  public void toXML(XMLStringBuffer xml)
  {
    Map<String, String> attributes = new LinkedHashMap<String, String>();

    if (isSimple()) {

//      attributes.put("frames", String.valueOf(numFrames));

      xml.push("animation", attributes);

      for (int frame = 0; frame < numFrames; ++frame) {
        attributes.clear();
//        attributes.put("frame", String.valueOf(frame));

        attributes.put("time", String.valueOf(frameTimes[frame]));

        KeyFrame anyKeyFrame = null;

        float scaleX = 1, scaleY = 1, angle = 0, translateX = 0, translateY = 0;
        for (int i = 0; i < numTransforms; ++i) {
          KeyFrameTransform transformFrame = transformFrames[i][frame];
          if (transformFrame != null) {
            switch (transformTypes[i]) {
              case ROTATE:
                angle += transformFrame.angle;
                break;
              case SCALE:
                scaleX *= transformFrame.x;
                scaleY *= transformFrame.y;
                break;
              case TRANSLATE:
                translateX += transformFrame.x;
                translateY += transformFrame.y;
                break;
            }
            anyKeyFrame = transformFrame;
          }
        }

        attributes.put("x", String.valueOf(translateX));
        attributes.put("y", String.valueOf(translateY));
        attributes.put("angle", String.valueOf(angle));
        if (scaleX != 1 || scaleY != 1) {
          attributes.put("scale-x", String.valueOf(scaleX));
          attributes.put("scale-y", String.valueOf(scaleY));
        }

        KeyFrameAlpha alphaFrame = alphaFrames[frame];
        attributes.put("alpha", String.valueOf(alphaFrame.alpha));
        anyKeyFrame = alphaFrame;

        if (anyKeyFrame.interpolationType == KeyFrame.INTERPOLATION_LINEAR) {
          attributes.put("interpolation", "linear");
        }

        xml.addEmptyElement("keyframe", attributes);
      }
      xml.pop("animation");
    }
    else {
      attributes.put("frames", String.valueOf(numFrames));
      attributes.put("transform", Boolean.toString(hasTransform));
      attributes.put("alpha", Boolean.toString(hasAlpha));
      attributes.put("color", Boolean.toString(hasColor));
      attributes.put("sound", Boolean.toString(hasSound));

      xml.push("complex-animation", attributes);

      xml.push("frame-timings");
      attributes.clear();
      for (int i = 0; i < numFrames; ++i) {
        attributes.put("frame", String.valueOf(i));
        attributes.put("start", String.valueOf(frameTimes[i]));
        xml.addEmptyElement("timing", attributes);
      }
      xml.pop("frame-timings");

      if (hasTransform) {
        xml.push("transforms");

        attributes.clear();
        for (int i = 0; i < numTransforms; ++i) {
          TransformType transformType = transformTypes[i];
          attributes.put("type", transformType.name().toLowerCase());
          xml.push("transform", attributes);
          toXMLFrameList(xml, "transform-frames", transformFrames[i]);
          xml.pop("transform");
        }

        xml.pop("transforms");
      }

      if (hasAlpha) {
        toXMLFrameList(xml, "alpha-frames", alphaFrames);
      }
      if (hasColor) {
        toXMLFrameList(xml, "color-frames", colorFrames);
      }
      if (hasSound) {
        toXMLFrameList(xml, "sound-frames", soundFrames);
      }
      xml.pop("complex-animation");
    }
  }

  public void toXMLSounds(XMLStringBuffer xml)
  {
    Map<String, String> attributes = new LinkedHashMap<String, String>();

    for (int frame = 0; frame < numFrames; ++frame) {
      KeyFrameSound soundFrame = hasSound ? soundFrames[frame] : null;
      if (soundFrame != null) {
        attributes.clear();

        attributes.put("time", String.valueOf(frameTimes[frame]));
        attributes.put("id", soundFrame.soundStr);

        xml.addEmptyElement("sound", attributes);
      }
    }
  }

  private boolean isSimple()
  {
    // A simple animation has:
    // 1. No colour, and no sound.
    // 2. Has transforms, exactly 3 of them: SCALE, ROTATE and TRANSLATE in that order
    // 3. No skipped keyframes. So all frames are present for all transforms. And they're all sequential, ending with -1.
    // 4. Has alpha, and no skipped frames there either. And an alpha frame exists for each transform frame, and vice versa.

    if (hasColor || hasSound || !hasTransform || !hasAlpha) return false;
    if (numTransforms != 3) return false;
    if (transformTypes[0] != TransformType.SCALE
            || transformTypes[1] != TransformType.ROTATE
            || transformTypes[2] != TransformType.TRANSLATE)
      return false;

    // Check there are no skipped frames and all are sequential

    for (int frame = 0; frame < numFrames; ++frame) {
      if (transformFrames[0][frame] == null
              || transformFrames[1][frame] == null
              || transformFrames[2][frame] == null
              || alphaFrames[frame] == null) {
        return false;
      }
      int expectedNext = (frame == numFrames - 1 ? -1 : frame + 1);
      if (transformFrames[0][frame].nextFrameIndex != expectedNext
              || transformFrames[1][frame].nextFrameIndex != expectedNext
              || transformFrames[2][frame].nextFrameIndex != expectedNext
              || alphaFrames[frame].nextFrameIndex != expectedNext) {
        return false;
      }
    }
    return true;
  }

  private void toXMLFrameList(XMLStringBuffer xml, String tagName, KeyFrame[] frameList)
  {
    xml.push(tagName);

    for (int i = 0; i < numFrames; ++i) {
      KeyFrame frame = frameList[i];

      if (frame != null) {
        frame.toXML(xml, i);
      }
    }

    xml.pop(tagName);
  }

  public void validateContiguousFrames()
  {
    for (int i = 0; i < transformTypes.length; i++) {
      TransformType transformType = transformTypes[i];
      for (int j = 0; j < numFrames; j++) {
        KeyFrameTransform frame = transformFrames[i][j];
        if (frame == null) {
          throw new AssertionError("null frame: " + this);

        }
        else if (j == numFrames - 1 && frame.nextFrameIndex != -1 ||
                j < numFrames - 1 && frame.nextFrameIndex != j + 1) {
          throw new AssertionError("bad next frame: " + this);
        }
      }
    }
  }

  /**
   * Creates a NEW BIA with the sound frames only. REMOVES the sound frames from this anim,
   * possibly shuffling up old frames if needed.
   *
   * @return
   */
  public BinImageAnimation extractSoundAnim()
  {
    if (!hasSound) throw new RuntimeException("extractSoundAnim on an anim with no sound");

    List<KeyFrameSound> newSoundFrames = new ArrayList<KeyFrameSound>();
    List<Float> newFrameTimes = new ArrayList<Float>();

    for (int i = 0; i < soundFrames.length; ++i) {
      KeyFrameSound soundFrame = soundFrames[i];
      if (soundFrame != null) {
        soundFrame.nextFrameIndex = newSoundFrames.size() + 1;
        newSoundFrames.add(soundFrame);
        newFrameTimes.add(frameTimes[i]);
        soundFrames[i] = null;
      }
    }
    newSoundFrames.get(newSoundFrames.size() - 1).nextFrameIndex = -1;

    BinImageAnimation soundAnim = new BinImageAnimation();
    soundAnim.hasSound = true;
    soundAnim.numFrames = newSoundFrames.size();
    soundAnim.soundFrames = new KeyFrameSound[newSoundFrames.size()];
    soundAnim.frameTimes = new float[newFrameTimes.size()];
    for (int i = 0; i < newSoundFrames.size(); ++i) {
      soundAnim.soundFrames[i] = newSoundFrames.get(i);
      soundAnim.frameTimes[i] = newFrameTimes.get(i);
    }

    // Now shuffle down any holes in our frame lists

    consolidateFrames();
    hasSound = false;
    soundFrames = null;

    return soundAnim;
  }

  // For any frame position that has NO keyframes, remove that completely
  // from the index and shuffle down the nextframeindex of all subsequent frames
  private void consolidateFrames()
  {
    eachframe:
    for (int i = 0; i < numFrames; ++i) {
      // something exists at this frame ?
      if ((hasAlpha && alphaFrames[i] != null)
              || (hasColor && colorFrames[i] != null)
              || (hasSound && soundFrames[i] != null)) {
        continue eachframe;
      }
      if (hasTransform) {
        for (int tt = 0; tt < transformTypes.length; tt++) {
          if (transformFrames[tt][i] != null) {
            continue eachframe;
          }
        }
      }

      // Nothing exists at this frame. Shuffle down!

      System.out.println("Shuffling frame " + i + " into oblivion!");

      int k;
      for (k = i; k < numFrames - 1; ++k) {
        frameTimes[k] = frameTimes[k + 1];

        if (hasTransform) {
          for (int tt = 0; tt < transformTypes.length; tt++) {
            transformFrames[tt][k] = transformFrames[tt][k + 1];
            transformFrames[tt][k + 1] = null;
          }
        }

        if (hasAlpha) {
          alphaFrames[k] = alphaFrames[k + 1];
          alphaFrames[k + 1] = null;
        }

        if (hasColor) {
          colorFrames[k] = colorFrames[k + 1];
          colorFrames[k + 1] = null;
        }

        if (hasSound) {
          soundFrames[k] = soundFrames[k + 1];
          soundFrames[k + 1] = null;
        }
      }

      frameTimes[k] = -666;


      // Now rewire ALL frames that have a nextIndex above the frame we just shuffled out.
      for (k = 0; k < numFrames - 1; ++k) {

        if (hasTransform) {
          for (int tt = 0; tt < transformTypes.length; tt++) {
            if (transformFrames[tt][k] != null && transformFrames[tt][k].nextFrameIndex > i)
              transformFrames[tt][k].nextFrameIndex--;
          }
        }

        if (hasAlpha && alphaFrames[k] != null && alphaFrames[k].nextFrameIndex > i) {
          alphaFrames[k].nextFrameIndex--;
        }

        if (hasColor && colorFrames[k] != null && colorFrames[k].nextFrameIndex > i) {
          colorFrames[k].nextFrameIndex--;
        }

        if (hasSound && soundFrames[k] != null && soundFrames[k].nextFrameIndex > i) {
          soundFrames[k].nextFrameIndex--;
        }
      }

      numFrames--;

      // We just killed the frame at i, so we must look at i again
      i--;
    }
  }

  public static void main(String[] args) throws IOException
  {
//    BinImageAnimation anim = new BinImageAnimation(new File("C:\\blah\\res\\anim\\ball_counter_ocd.anim.binltl"));
//    BinImageAnimation anim = new BinImageAnimation(new File("C:\\blah\\res\\anim\\rot_1rps.anim.binltl"));
//    System.out.println("anim = " + anim);
//    System.out.println("anim.toXML() = " + anim.toXML());

    File dir = new File("c:\\blah\\res\\anim");
    for (File file : dir.listFiles()) {
      System.out.println("\n\n>>>>>>>> " + file.getName());
      BinImageAnimation anim = new BinImageAnimation(file);
//      anim.validateContiguousFrames();
//      System.out.println("anim.toXML() = " + anim.toXMLDocument());
      Utilities.writeFile(new File("anim", file.getName().replace(".binltl", ".xml")), anim.toXMLDocument().getBytes());
    }
  }
}
