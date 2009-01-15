package com.goofans.gootool.io;

import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.wog.WorldOfGoo;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AESBinFormat
{
  private static final Logger log = Logger.getLogger(AESBinFormat.class.getName());

  private static final byte[] KEY = {0x0D, 0x06, 0x07, 0x07, 0x0C, 0x01, 0x08, 0x05,
          0x06, 0x09, 0x09, 0x04, 0x06, 0x0D, 0x03, 0x0F,
          0x03, 0x06, 0x0E, 0x01, 0x0E, 0x02, 0x07, 0x0B};

  private static final String CHARSET = "UTF-8";
  private static final byte EOF_MARKER = (byte) 0xFD;


  /* If TESTMODE is true, decode() will store the original file, so encode() can verify it later */
  private static boolean TESTMODE;
  //  private static int TESTMODE_DECODING_FILE_SIZE;
  private static int TESTMODE_DECODING_STRING_SIZE;
  private static byte[] TESTMODE_ORIGINAL;

  private AESBinFormat()
  {
  }

  public static byte[] decodeFile(File file) throws IOException
  {
    byte[] inputBytes = Utilities.readFile(file);
    return decode(inputBytes);
  }

  // Java Crypto API - can't use because user will have to install 192-bit policy file.
//    SecretKey key = new SecretKeySpec(KEY, "AES");
//    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//CBC
//    cipher.init(Cipher.DECRYPT_MODE, key);
//    byte[] decrypted = cipher.doFinal(bytes);

  // wossnames' AESdecrypt class, only handles first 16 bytes of file
//    AESdecrypt aesDecrypt = new AESdecrypt(KEY, 6);
//    aesDecrypt.
//    aesDecrypt.InvCipher(bytes, decrypted);
//


  private static byte[] decode(byte[] inputBytes) throws IOException
  {
    int inputSize = inputBytes.length;

    BufferedBlockCipher cipher = getCipher(false);

    byte[] outputBytes = new byte[cipher.getOutputSize(inputSize)];

    int outputLen = cipher.processBytes(inputBytes, 0, inputSize, outputBytes, 0);

    try {
      outputLen += cipher.doFinal(outputBytes, outputLen);
    }
    catch (InvalidCipherTextException e) {
      log.log(Level.SEVERE, "Can't decrypt file", e);
      throw new IOException("Can't decrypt file: " + e.getLocalizedMessage());
    }

    /* End before any 0xFD cruft at the end of the file */
    /* TODO this could be more efficient by just looping over the last XX bytes of the file */
    for (int i = 0; i < outputLen; ++i) {
      byte b = outputBytes[i];
      if (b == EOF_MARKER) {
        log.finer("Skipped " + (outputLen - i) + " bytes at the end (old size " + outputLen + ", new size " + i + ")");
        StringBuilder sb = new StringBuilder("[");
        for (int j = i; j < outputLen; ++j) {
          sb.append(' ').append(byteToHex(outputBytes[j]));
        }
        sb.append(" ]");
        log.finer("Skipped bytes: " + sb);
        outputLen = i;
        break;
      }
    }

    int start = 0;

    // UTF-8 Byte Order Mark
//    if (outputBytes[0] == (byte) 0xEF && outputBytes[1] == (byte) 0xBB && outputBytes[2] == (byte) 0xBF) {
//      start = 3;
//      log.finer("Skipping first 3 bytes of file, BOM found");
//    }

    if (TESTMODE) {
      TESTMODE_ORIGINAL = inputBytes;
      TESTMODE_DECODING_STRING_SIZE = outputLen - start;
    }

//    return new String(outputBytes, start, outputLen - start, CHARSET);
    byte[] finalBytes = new byte[outputLen - start];
    System.arraycopy(outputBytes, start, finalBytes, 0, outputLen);
    return finalBytes;
  }


  public static void encodeFile(File file, byte[] input) throws IOException
  {
    byte[] bytes = encode(input);
    Utilities.writeFile(file, bytes);
  }

  private static byte[] encode(byte[] inputBytes) throws IOException
  {
//    byte[] inputBytes = input.getBytes(CHARSET);

    if (TESTMODE && inputBytes.length != TESTMODE_DECODING_STRING_SIZE) {
      //noinspection UseOfSystemOutOrSystemErr
      System.err.println("ERROR! DECODING/ENCODING MISMATCH IN STRING SIZE (was " + TESTMODE_DECODING_STRING_SIZE + ", now " + inputBytes.length + ")");
      throw new RuntimeException();
    }

    /* If input was multiple of 16, NO padding. Example: res\levels\BulletinBoardSystem\BulletinBoardSystem.level.bin */
    /* Otherwise pad to next 16 byte boundary */

    int origSize = inputBytes.length;
    if (origSize % 16 != 0) {
      int padding = 16 - origSize % 16;

      int newSize = origSize + padding;

      log.finer("Size " + origSize + " padded with " + padding + " bytes to make " + newSize);

      byte[] newInputBytes = new byte[newSize];
      System.arraycopy(inputBytes, 0, newInputBytes, 0, origSize);
      inputBytes = newInputBytes;
//      inputBytes = Arrays.copyOf(inputBytes, newSize);

      /* Write up to 4 0xFD bytes immediately after the original file. The remainder can stay as the 0x00 provided by Arrays.copyOf. */
      for (int i = origSize; i < origSize + 4 && i < newSize; ++i) {
        inputBytes[i] = EOF_MARKER;
      }
    }
    else {
      log.finer("Size " + origSize + " already multiple of 16, no padding");
    }

    int inputSize = inputBytes.length;

    BufferedBlockCipher cipher = getCipher(true);

    byte[] outputBytes = new byte[cipher.getOutputSize(inputSize)];

    int outputLen = cipher.processBytes(inputBytes, 0, inputSize, outputBytes, 0);

    try {
      outputLen += cipher.doFinal(outputBytes, outputLen);
    }
    catch (InvalidCipherTextException e) {
      log.log(Level.SEVERE, "Can't encrypt file", e);
      throw new IOException("Can't encrypt file: " + e.getLocalizedMessage());
    }


    if (TESTMODE) {
      if (outputBytes.length != TESTMODE_ORIGINAL.length) {
        //noinspection UseOfSystemOutOrSystemErr
        System.err.println("ERROR! DECODING/ENCODING MISMATCH IN ENCRYPTED LENGTH");
        throw new RuntimeException();
      }
      else {
        // Verify the bytes are the same
        for (int i = 0; i < outputBytes.length; ++i) {
          if (outputBytes[i] != TESTMODE_ORIGINAL[i]) {
            if (outputBytes.length - i == 16) {
              // it's ok to have mismatch in the last block because sometimes they pad with other stuff after the 0xFD
              //noinspection UseOfSystemOutOrSystemErr
              System.err.println("warning! mismatch in last block");
            }
            else {
              //noinspection UseOfSystemOutOrSystemErr
              System.err.println("ERROR! DECODING/ENCODING MISMATCH IN ENCRYPTED BYTES AT INDEX " + i + " (" + (outputBytes.length - i) + " from end)");
              throw new RuntimeException();
            }
            break;
          }
        }
      }
    }

//    return Arrays.copyOf(outputBytes, outputLen);
    byte[] outputBytes2 = new byte[outputLen];
    System.arraycopy(outputBytes, 0, outputBytes2, 0, outputLen);
    return outputBytes2;
  }


  private static BufferedBlockCipher getCipher(boolean forEncryption)
  {
    BlockCipher engine = new AESEngine();
    BufferedBlockCipher cipher = new BufferedBlockCipher(new CBCBlockCipher(engine)); //new PaddedBufferedBlockCipher(

    cipher.init(forEncryption, new KeyParameter(KEY));
    return cipher;
  }


  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException, TransformerException
  {
    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    worldOfGoo.init();

    byte[] decoded = decodeFile(worldOfGoo.getGameFile("properties/text.xml.bin"));

//    Document doc = XMLUtil.loadDocumentFromInputStream(new ByteArrayInputStream(s.getBytes()));
    Document doc = XMLUtil.loadDocumentFromInputStream(new ByteArrayInputStream(decoded));
    System.out.println(XMLUtil.writeDocumentToString(doc));

    TESTMODE = true;

    testFile("properties/resources.xml.bin");
//    testFile("res\\levels\\GoingUp\\GoingUp.level.bin");
//    testFile("res\\levels\\GoingUp\\GoingUp.resrc.bin");
//    testFile("res\\levels\\GoingUp\\GoingUp.scene.bin");
//    testFile("properties\\materials.xml.bin");

    testDir(worldOfGoo.getGameFile(""));

//    testFile("res\\anim\\ball_counter.anim.binltl");
  }

  private static void testDir(File dir) throws IOException
  {
    for (File file : dir.listFiles()) {
      if (file.isDirectory()) {
        testDir(file);
      }
      else if (file.isFile()) {
        if (file.getName().endsWith(".bin")) {
          testFile(file);
        }
//        else if (file.getName().endsWith(".binltl")) {
//          testFile(file);
//        }
      }
    }
  }

  private static String byteToHex(byte b)
  {
    String s = Integer.toHexString(b).toUpperCase();

    if (s.length() < 2) return "0x0" + s;
    return "0x" + s.substring(s.length() - 2, s.length());
  }

  private static byte[] testFile(String file) throws IOException
  {
    File f = WorldOfGoo.getTheInstance().getGameFile(file);

    return testFile(f);
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  private static byte[] testFile(File f) throws IOException
  {
    System.err.println("Testing " + f);

    byte[] de = decodeFile(f);
//    System.out.println("de = " + de);
//    System.out.println("de.length() = " + de.length());

//    byte[] enc =
    try {
      encode(de);
    }
    catch (RuntimeException re) {
      // We know about the BOM problem in text.xml.bin
      if (!f.getName().equals("text.xml.bin")) throw re;
    }
//    System.out.println("enc = " + enc);

    return de;
  }

  public static byte[] copyOf(byte[] original, int newLength)
  {
    byte[] copy = new byte[newLength];
    System.arraycopy(original, 0, copy, 0,
            Math.min(original.length, newLength));
    return copy;
  }
}
