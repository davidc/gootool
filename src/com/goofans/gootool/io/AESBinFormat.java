/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.io;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.facades.Source;
import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.util.XMLUtil;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.w3c.dom.Document;

/**
 * Encrypt/decrypt .bin files in AES format (Windows/Linux).
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AESBinFormat extends Codec
{
  private static final Logger log = Logger.getLogger(AESBinFormat.class.getName());

  @SuppressWarnings({"MagicNumber"})
  private static final byte[] KEY = {0x0D, 0x06, 0x07, 0x07, 0x0C, 0x01, 0x08, 0x05,
          0x06, 0x09, 0x09, 0x04, 0x06, 0x0D, 0x03, 0x0F,
          0x03, 0x06, 0x0E, 0x01, 0x0E, 0x02, 0x07, 0x0B};

  private static final byte EOF_MARKER = (byte) 0xFD;


  /* If TESTMODE is true, decode() will store the original file, so encode() can verify it later */
  private static boolean TESTMODE;
  //  private static int TESTMODE_DECODING_FILE_SIZE;
  private static int TESTMODE_DECODING_STRING_SIZE;
  private static byte[] TESTMODE_ORIGINAL;

  AESBinFormat()
  {
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

  public byte[] decode(byte[] inputBytes) throws IOException
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

  public byte[] encode(byte[] inputBytes) throws IOException
  {
//    byte[] inputBytes = input.getBytes(CHARSET);

    if (TESTMODE && inputBytes.length != TESTMODE_DECODING_STRING_SIZE) {
      //noinspection UseOfSystemOutOrSystemErr,HardCodedStringLiteral,HardcodedFileSeparator
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
        //noinspection UseOfSystemOutOrSystemErr,HardCodedStringLiteral,HardcodedFileSeparator
        System.err.println("ERROR! DECODING/ENCODING MISMATCH IN ENCRYPTED LENGTH");
        throw new RuntimeException();
      }
      else {
        // Verify the bytes are the same
        for (int i = 0; i < outputBytes.length; ++i) {
          if (outputBytes[i] != TESTMODE_ORIGINAL[i]) {
            if (outputBytes.length - i == 16) {
              // it's ok to have mismatch in the last block because sometimes they pad with other stuff after the 0xFD
              //noinspection UseOfSystemOutOrSystemErr,HardCodedStringLiteral,HardcodedFileSeparator
              System.err.println("warning! mismatch in last block");
            }
            else {
              //noinspection UseOfSystemOutOrSystemErr,HardCodedStringLiteral,HardcodedFileSeparator
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


  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "HardcodedFileSeparator", "DuplicateStringLiteralInspection"})
  public static void main(String[] args) throws IOException, TransformerException
  {
    Source source = ProjectManager.simpleInit().getSource();

    Codec codec = new AESBinFormat();

    SourceFile sourceRoot = source.getRoot();

    byte[] decoded = codec.decodeFile(sourceRoot.getChild("properties/text.xml.bin"));

//    Document doc = XMLUtil.loadDocumentFromInputStream(new ByteArrayInputStream(s.getBytes()));
    Document doc = XMLUtil.loadDocumentFromInputStream(new ByteArrayInputStream(decoded));
    System.out.println(XMLUtil.writeDocumentToString(doc));

    TESTMODE = true;

    testFile(codec, sourceRoot.getChild("properties/resources.xml.bin"));
//    testFile("res\\levels\\GoingUp\\GoingUp.level.bin");
//    testFile("res\\levels\\GoingUp\\GoingUp.resrc.bin");
//    testFile("res\\levels\\GoingUp\\GoingUp.scene.bin");
//    testFile("properties\\materials.xml.bin");

    testDir(codec, sourceRoot);

//    testFile("res\\anim\\ball_counter.anim.binltl");
  }

  @SuppressWarnings({"HardCodedStringLiteral"})
  private static void testDir(Codec codec, SourceFile dir) throws IOException
  {
    for (SourceFile file : dir.list()) {
      if (file.isDirectory()) {
        testDir(codec, file);
      }
      else if (file.isFile()) {
        if (file.getName().endsWith(".bin")) {
          testFile(codec, file);
        }
//        else if (file.getName().endsWith(".binltl")) {
//          testFile(file);
//        }
      }
    }
  }

  @SuppressWarnings({"HardCodedStringLiteral", "StringConcatenation"})
  private static String byteToHex(byte b)
  {
    String s = Integer.toHexString(b).toUpperCase();

    if (s.length() < 2) return "0x0" + s;
    return "0x" + s.substring(s.length() - 2, s.length());
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  private static byte[] testFile(Codec codec, SourceFile f) throws IOException
  {
    System.err.println("Testing " + f);

    byte[] de = codec.decodeFile(f);
//    System.out.println("de = " + de);
//    System.out.println("de.length() = " + de.length());

//    byte[] enc =
    try {
      codec.encode(de);
    }
    catch (RuntimeException re) {
      // We know about the BOM problem in text.xml.bin
      if (!"text.xml.bin".equals(f.getName())) throw re;
    }
//    System.out.println("enc = " + enc);

    return de;
  }
}
