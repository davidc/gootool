/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package net.infotrek.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;

/**
 * Base64 coding from: http://iharder.sourceforge.net/current/java/base64/
 *
 * @author David Croft (david.croft@infotrek.net)
 * @version $Id$
 */
public class TextUtil
{
  private static final byte[] BASE64_ALPHABET =
          {
                  (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F', (byte) 'G',
                  (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L', (byte) 'M', (byte) 'N',
                  (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R', (byte) 'S', (byte) 'T', (byte) 'U',
                  (byte) 'V', (byte) 'W', (byte) 'X', (byte) 'Y', (byte) 'Z',
                  (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f', (byte) 'g',
                  (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l', (byte) 'm', (byte) 'n',
                  (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r', (byte) 's', (byte) 't', (byte) 'u',
                  (byte) 'v', (byte) 'w', (byte) 'x', (byte) 'y', (byte) 'z',
                  (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5',
                  (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+', (byte) '/'
          };
  private static final byte EQUALS_SIGN = (byte) '=';

  private static final byte[] BASE64_DECODABET =
          {
                  -9, -9, -9, -9, -9, -9, -9, -9, -9,                 // Decimal  0 -  8
                  -5, -5,                                      // Whitespace: Tab and Linefeed
                  -9, -9,                                      // Decimal 11 - 12
                  -5,                                         // Whitespace: Carriage Return
                  -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9,     // Decimal 14 - 26
                  -9, -9, -9, -9, -9,                             // Decimal 27 - 31
                  -5,                                         // Whitespace: Space
                  -9, -9, -9, -9, -9, -9, -9, -9, -9, -9,              // Decimal 33 - 42
                  62,                                         // Plus sign at decimal 43
                  -9, -9, -9,                                   // Decimal 44 - 46
                  63,                                         // Slash at decimal 47
                  52, 53, 54, 55, 56, 57, 58, 59, 60, 61,              // Numbers zero through nine
                  -9, -9, -9,                                   // Decimal 58 - 60
                  -1,                                         // Equals sign at decimal 61
                  -9, -9, -9,                                      // Decimal 62 - 64
                  0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,            // Letters 'A' through 'N'
                  14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,        // Letters 'O' through 'Z'
                  -9, -9, -9, -9, -9, -9,                          // Decimal 91 - 96
                  26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38,     // Letters 'a' through 'm'
                  39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,     // Letters 'n' through 'z'
                  -9, -9, -9, -9                                 // Decimal 123 - 126
          };
  private static final byte WHITE_SPACE_ENC = -5; // Indicates white space in encoding
  private static final byte EQUALS_SIGN_ENC = -1; // Indicates equals sign in encoding
  private static final String UTF_8 = "UTF-8";

  private TextUtil()
  {
  }

  /**
   * Encode binary data into String using base64.
   */
  public static String base64Encode(byte[] data)
  {
    int len = data.length;
    int len43 = len * 4 / 3;
    byte[] outBuff = new byte[(len43)                      // Main 4:3
            + ((len % 3) > 0 ? 4 : 0)];      // Account for padding

    int d = 0;
    int e = 0;
    int len2 = len - 2;

    for (; d < len2; d += 3, e += 4) {
      encode3to4(data, d, 3, outBuff, e);
    }

    if (d < len) { // pad
      encode3to4(data, d, len - d, outBuff, e);
      e += 4;
    }

    try {
      return new String(outBuff, 0, e, UTF_8);
    }
    catch (UnsupportedEncodingException e1) {
      throw new RuntimeException(e1);
    }
  }

  private static byte[] encode3to4(byte[] source, int srcOffset, int numSigBytes, byte[] destination, int destOffset)
  {
    //           1         2         3
    // 01234567890123456789012345678901 Bit position
    // --------000000001111111122222222 Array position from threeBytes
    // --------|    ||    ||    ||    | Six bit groups to index ALPHABET
    //          >>18  >>12  >> 6  >> 0  Right shift necessary
    //                0x3f  0x3f  0x3f  Additional AND

    // Create buffer with zero-padding if there are only one or two
    // significant bytes passed in the array.
    // We have to shift left 24 in order to flush out the 1's that appear
    // when Java treats a value as negative that is cast from a byte to an int.
    int inBuff = (numSigBytes > 0 ? ((source[srcOffset] << 24) >>> 8) : 0)
            | (numSigBytes > 1 ? ((source[srcOffset + 1] << 24) >>> 16) : 0)
            | (numSigBytes > 2 ? ((source[srcOffset + 2] << 24) >>> 24) : 0);

    switch (numSigBytes) {
      case 3:
        destination[destOffset] = BASE64_ALPHABET[(inBuff >>> 18)];
        destination[destOffset + 1] = BASE64_ALPHABET[(inBuff >>> 12) & 0x3f];
        destination[destOffset + 2] = BASE64_ALPHABET[(inBuff >>> 6) & 0x3f];
        destination[destOffset + 3] = BASE64_ALPHABET[(inBuff) & 0x3f];
        return destination;

      case 2:
        destination[destOffset] = BASE64_ALPHABET[(inBuff >>> 18)];
        destination[destOffset + 1] = BASE64_ALPHABET[(inBuff >>> 12) & 0x3f];
        destination[destOffset + 2] = BASE64_ALPHABET[(inBuff >>> 6) & 0x3f];
        destination[destOffset + 3] = EQUALS_SIGN;
        return destination;

      case 1:
        destination[destOffset] = BASE64_ALPHABET[(inBuff >>> 18)];
        destination[destOffset + 1] = BASE64_ALPHABET[(inBuff >>> 12) & 0x3f];
        destination[destOffset + 2] = EQUALS_SIGN;
        destination[destOffset + 3] = EQUALS_SIGN;
        return destination;

      default:
        return destination;
    }   // end switch
  }   // end encode3to4


  /**
   * Decode binary data from String using base64.
   */
  public static byte[] base64Decode(String str) throws IOException
  {
    byte[] source = str.getBytes(UTF_8);
    int len = source.length;

    int len34 = len * 3 / 4;
    byte[] outBuff = new byte[len34]; // Upper limit on size of output
    int outBuffPosn = 0;

    byte[] b4 = new byte[4];
    int b4Posn = 0;
    int i;
    byte sbiCrop;
    byte sbiDecode;
    for (i = 0; i < len; i++) {
      sbiCrop = (byte) (source[i] & 0x7f); // Only the low seven bits
      sbiDecode = BASE64_DECODABET[sbiCrop];

      if (sbiDecode >= WHITE_SPACE_ENC) // White space, Equals sign or better
      {
        if (sbiDecode >= EQUALS_SIGN_ENC) {
          b4[b4Posn++] = sbiCrop;
          if (b4Posn > 3) {
            outBuffPosn += decode4to3(b4, 0, outBuff, outBuffPosn);
            b4Posn = 0;

            // If that was the equals sign, break out of 'for' loop
            if (sbiCrop == EQUALS_SIGN)
              break;
          }   // end if: quartet built

        }   // end if: equals sign or better

      }   // end if: white space, equals sign or better
      else {
        throw new IOException("Bad Base64 input character at " + i + ": " + source[i] + "(decimal)");
      }   // end else:
    }   // each input character

    byte[] out = new byte[outBuffPosn];
    System.arraycopy(outBuff, 0, out, 0, outBuffPosn);
    return out;
  }


  private static int decode4to3(byte[] source, int srcOffset, byte[] destination, int destOffset)
  {
    // Example: Dk==
    if (source[srcOffset + 2] == EQUALS_SIGN) {
      // Two ways to do the same thing. Don't know which way I like best.
      //int outBuff =   ( ( DECODABET[ source[ srcOffset    ] ] << 24 ) >>>  6 )
      //              | ( ( DECODABET[ source[ srcOffset + 1] ] << 24 ) >>> 12 );
      int outBuff = ((BASE64_DECODABET[source[srcOffset]] & 0xFF) << 18)
              | ((BASE64_DECODABET[source[srcOffset + 1]] & 0xFF) << 12);

      destination[destOffset] = (byte) (outBuff >>> 16);
      return 1;
    }

    // Example: DkL=
    else if (source[srcOffset + 3] == EQUALS_SIGN) {
      // Two ways to do the same thing. Don't know which way I like best.
      //int outBuff =   ( ( DECODABET[ source[ srcOffset     ] ] << 24 ) >>>  6 )
      //              | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
      //              | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 );
      int outBuff = ((BASE64_DECODABET[source[srcOffset]] & 0xFF) << 18)
              | ((BASE64_DECODABET[source[srcOffset + 1]] & 0xFF) << 12)
              | ((BASE64_DECODABET[source[srcOffset + 2]] & 0xFF) << 6);

      destination[destOffset] = (byte) (outBuff >>> 16);
      destination[destOffset + 1] = (byte) (outBuff >>> 8);
      return 2;
    }

    // Example: DkLE
    else {
      // Two ways to do the same thing. Don't know which way I like best.
      //int outBuff =   ( ( DECODABET[ source[ srcOffset     ] ] << 24 ) >>>  6 )
      //              | ( ( DECODABET[ source[ srcOffset + 1 ] ] << 24 ) >>> 12 )
      //              | ( ( DECODABET[ source[ srcOffset + 2 ] ] << 24 ) >>> 18 )
      //              | ( ( DECODABET[ source[ srcOffset + 3 ] ] << 24 ) >>> 24 );
      int outBuff = ((BASE64_DECODABET[source[srcOffset]] & 0xFF) << 18)
              | ((BASE64_DECODABET[source[srcOffset + 1]] & 0xFF) << 12)
              | ((BASE64_DECODABET[source[srcOffset + 2]] & 0xFF) << 6)
              | ((BASE64_DECODABET[source[srcOffset + 3]] & 0xFF));


      destination[destOffset] = (byte) (outBuff >> 16);
      destination[destOffset + 1] = (byte) (outBuff >> 8);
      destination[destOffset + 2] = (byte) (outBuff);

      return 3;
    }
  }   // end decodeToBytes


  private static final long BP_K = 1024;
  private static final long BP_M = 1024*1024;
  private static final long BP_G = 1024*1024*1024;

  public static String binaryNumToString(long num)
  {
    NumberFormat nf = NumberFormat.getNumberInstance();
    if (num > BP_G) {
      return nf.format(((double)num) / BP_G) + "G";
    }
    if (num > BP_M) {
      return nf.format(((double)num) / BP_M) + "M";
    }
    if (num > BP_K) {
      return nf.format(((double)num) / BP_K) + "K";
    }
    return nf.format(num);
  }

  public static String stripHtmlTags(String s)
  {
    return s.replaceAll("\\<.*?\\>", "");
  }


  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  public static void main(String[] args) throws IOException
  {
    String original = "Hello, world!!!";
    System.out.println("original = " + original);
    String encoded = base64Encode(original.getBytes());
    System.out.println("encoded = " + encoded);
    byte[] decoded = base64Decode(encoded);
    String decodedStr = new String(decoded, UTF_8);
    System.out.println("decodedStr = " + decodedStr);
    System.out.println("(original.equals(decodedStr)) = " + (original.equals(decodedStr)));
  }

  public static String formatTime(int secs)
  {
    StringBuilder sb = new StringBuilder();

    int days = secs / 86400;
    if (days > 0) {
      sb.append(days).append(" day");
      if (days != 1) sb.append("s");
      sb.append(", ");
      secs %= 86400;
    }

    int hours = secs / 3600;
    if (days > 0 || hours > 0) {
      sb.append(hours).append(" hour");
      if (hours != 1) sb.append("s");
      sb.append(", ");
      secs %= 3600;
    }

    int minutes = secs / 60;
    if (days > 0 || hours > 0 || minutes > 0) {
      sb.append(minutes).append(" minute");
      if (minutes != 1) sb.append("s");
      sb.append(", ");
      secs %= 60;
    }
    sb.append(secs).append(" second");
    if (secs != 1) sb.append("s");
    sb.append(".");

    return sb.toString();
  }
}