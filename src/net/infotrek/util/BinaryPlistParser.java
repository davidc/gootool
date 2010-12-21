/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package net.infotrek.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import com.goofans.gootool.profile.ProfileData;
import com.goofans.gootool.util.Utilities;

/**
 * Reads a binary-format Apple property list and returns its contents as a Map. This is a wholly incomplete implementation; it only handles properties
 * with String values (because that's all that's used in World of Goo plists).
 * <p/>
 * Reference: http://www.opensource.apple.com/source/CF/CF-550.42/CFBinaryPList.c
 * <p/>
 * http://www.opensource.apple.com/source/CF/CF-550/ForFoundationOnly.h for header and trailer structs.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class BinaryPlistParser
{
  @SuppressWarnings({"MagicNumber"})
  private static final byte[] BINARY_PLIST_SIGNATURE_V00 = new byte[]{0x62, 0x70, 0x6c, 0x69, 0x73, 0x74, 0x30, 0x30}; // "bplist00"

  private static final byte kCFBinaryPlistMarkerNull = 0x00;
  private static final byte kCFBinaryPlistMarkerFalse = 0x08;
  private static final byte kCFBinaryPlistMarkerTrue = 0x09;
  private static final byte kCFBinaryPlistMarkerFill = 0x0F;
  private static final byte kCFBinaryPlistMarkerInt = 0x10;
  private static final byte kCFBinaryPlistMarkerReal = 0x20;
  private static final byte kCFBinaryPlistMarkerDate = 0x33;
  private static final byte kCFBinaryPlistMarkerData = 0x40;
  private static final byte kCFBinaryPlistMarkerASCIIString = 0x50;
  private static final byte kCFBinaryPlistMarkerUnicode16String = 0x60;
  private static final byte kCFBinaryPlistMarkerUID = (byte) 0x80;
  private static final byte kCFBinaryPlistMarkerArray = (byte) 0xA0;
  private static final byte kCFBinaryPlistMarkerSet = (byte) 0xC0;
  private static final byte kCFBinaryPlistMarkerDict = (byte) 0xD0;

  private final BinaryBuffer buf;
  private short offsetIntSize;
  private short objectRefSize;
  private int numObjects;
  private int offsetTableOffset;

  public BinaryPlistParser(byte[] data)
  {
    buf = new BigEndianBinaryBuffer(data);
  }

  public Object parsePlist() throws IOException
  {
    // Check the signature in the header

    buf.setPosition(0);

    for (byte sigByte : BINARY_PLIST_SIGNATURE_V00) {
      if (buf.getByte() != sigByte) {
        throw new IOException("Plist signature not found or unsupported version");
      }
    }


    // Read the data from the trailer

    buf.setPosition(buf.getLength() - 26);

    offsetIntSize = buf.getByte();
    System.out.println("offsetIntSize = " + offsetIntSize);
    objectRefSize = buf.getByte();
    System.out.println("objectRefSize = " + objectRefSize);
    numObjects = (int) buf.getLong();
    System.out.println("numObjects = " + numObjects + " (" + Long.toHexString(numObjects) + ")");
    int topObject = (int) buf.getLong();
    System.out.println("topObject = " + topObject);
    offsetTableOffset = (int) buf.getLong();
    System.out.println("offsetTableOffset = " + offsetTableOffset);

    if (numObjects < 1 || numObjects <= topObject || offsetTableOffset < 9 || offsetIntSize < 1 || objectRefSize < 1)
      throw new IOException("Malformed plist trailer");


    return getObject(topObject);
  }

  private Object getObject(int objNum) throws IOException
  {
    if (objNum < 0 || objNum >= numObjects) throw new IOException("Attempt to get invalid object " + objNum);

    int offsetPos = offsetTableOffset + (objNum * offsetIntSize);
    System.out.println("offsetPos = " + offsetPos);
    buf.setPosition(offsetPos);

    int objPos = getIntOfSize(offsetIntSize);
    System.out.println("objPos = " + objPos);
    buf.setPosition(objPos);

    short marker = buf.getByte();
    switch (((byte) (marker & 0xf0))) {
      case kCFBinaryPlistMarkerNull:
        return getPrimitive(marker);
      case kCFBinaryPlistMarkerInt:
        return getIntOfSize((short) (1 << (marker & 0xf)));
      case kCFBinaryPlistMarkerReal:
        return getReal(marker);
      case kCFBinaryPlistMarkerData:
        return getBinaryData(marker);
      case kCFBinaryPlistMarkerASCIIString:
        return getAsciiString(marker);
      case kCFBinaryPlistMarkerDict:
        return getDict(marker);
      case (kCFBinaryPlistMarkerDate & 0xf0):
        if (marker == kCFBinaryPlistMarkerDate) {
          return getDate(marker);
        }
        // fall through to failure
      default:
        throw new IOException("Unhandled plist data marker 0x" + Integer.toHexString(marker));
    }

  }

  private static Object getPrimitive(short marker) throws IOException
  {
    switch (marker) {
      case kCFBinaryPlistMarkerNull:
        return null;
      case kCFBinaryPlistMarkerFalse:
        return false;
      case kCFBinaryPlistMarkerTrue:
        return true;
      default:
        throw new IOException("Unrecognised primitive type " + Integer.toHexString(marker));
    }
  }

  private Object getReal(short marker) throws IOException
  {
    if ((marker & 0xf) == 2) {
      return Float.intBitsToFloat((int) buf.getInt());
    }
    else if ((marker & 0xf) == 3) {
      return Double.longBitsToDouble(buf.getLong());
    }
    else {
      throw new IOException("Unknown real format with marker " + Integer.toHexString(marker));
    }
  }

  private Date getDate(short marker)
  {
    //64-bit double float
    // from http://www.opensource.apple.com/source/CF/CF-550.42/CFDate.c
//    const CFTimeInterval kCFAbsoluteTimeIntervalSince1970 = 978307200.0L;

    double diff = buf.getDouble();
    System.out.println("diff = " + diff);

    Date d = new Date((long) (diff * 1000) + 978307200000L);
    System.out.println("d = " + d);

    return d;
  }

  private int getIntOfSize(short intSize) throws IOException
  {
    switch (intSize) {
      case 1:
        return buf.getByte();
      case 2:
        return buf.getShort();
      case 4:
        return (int) buf.getInt();
      case 8:
        return (int) buf.getLong();
      default:
        throw new IOException("Unknown int size " + intSize);
    }
  }

  private Object getBinaryData(short marker) throws IOException
  {
    int count = getVariableLengthCountFromMarker(marker);
    System.out.println("count = " + count);

    byte[] data = buf.getBytes(count);
    System.out.println("new String(data) = " + new String(data));
    return data;
  }

  private Object getAsciiString(short marker) throws IOException
  {
    int count = getVariableLengthCountFromMarker(marker);
    String s = buf.getString(count, "ASCII");
    System.out.println("s = " + s);
    return s;
  }

  private int getVariableLengthCountFromMarker(short marker) throws IOException
  {
    int count = marker & 0x0f;
    System.out.println("count initial = " + count);
    if (count == 0x0f) {
      count = readVariableLengthInt();
    }
    return count;
  }

  private Map<Object, Object> getDict(short marker) throws IOException
  {
    int count = getVariableLengthCountFromMarker(marker);
    System.out.println("count = " + count);

    int[] keyrefs = new int[count];

    for (int i = 0; i < count; ++i) {
      int keyref = getIntOfSize(objectRefSize);
      System.out.println("keyref = " + keyref);
      keyrefs[i] = keyref;
    }

    int[] objrefs = new int[count];

    for (int i = 0; i < count; ++i) {
      int objref = getIntOfSize(objectRefSize);
      System.out.println("objref = " + objref);
      objrefs[i] = objref;
    }

    Map<Object, Object> m = new TreeMap<Object, Object>();

    for (int i = 0; i < count; ++i) {
      m.put(getObject(keyrefs[i]), getObject(objrefs[i]));
    }

    return m;
  }

  private int readVariableLengthInt() throws IOException
  {
    short marker = buf.getByte();
    if ((marker & 0xf0) != kCFBinaryPlistMarkerInt) {
      throw new IOException("Illegal marker " + marker + " in readVariableLengthInt");
    }

    int count = 1 << (marker & 0x0f);

    System.out.println("count of bytes = " + count);

    long value = 0;
    for (int i = 0; i < count; i++) {
      short b = buf.getByte();
      System.out.println("got byte " + (b & 0xff));
      value = (value << 8) + (b & 0xff);
    }
    return (int) value;
  }


  public static void main(String[] args) throws IOException
  {
    BinaryPlistParser plist = new BinaryPlistParser(Utilities.readFile(new File("plist")));

    Object data = plist.parsePlist();

    System.out.println("data = " + data);

    byte[] pers2dat = (byte[]) ((Map) data).get("pers2.dat");

    ProfileData pd = new ProfileData(pers2dat);
    System.out.println("pd = " + pd);
  }
}
