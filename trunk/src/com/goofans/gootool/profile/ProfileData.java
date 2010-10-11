/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.profile;

import net.infotrek.util.EncodingUtil;

import java.io.*;
import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.Utilities;

/**
 * Contains all profiles (up to 3) in the user's profile file.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProfileData
{
  private static final Logger log = Logger.getLogger(ProfileData.class.getName());

  private static final String KEY_PROFILE = "profile_";
  private static final String KEY_MRPP = "mrpp";
  public static final int MAX_PROFILES = 3;

  private final Map<String, String> data = new LinkedHashMap<String, String>();
  private final Profile[] profiles = new Profile[MAX_PROFILES];

  public ProfileData(File f) throws IOException
  {
    byte[] profile = GameFormat.decodeProfileFile(f);

    readProfileData(profile);

    log.finest("ProfileData is " + data);

    for (String key : data.keySet()) {
      if (key.startsWith(KEY_PROFILE)) {
        int profileIndex = Integer.valueOf(key.substring(8));
        profiles[profileIndex] = new Profile(data.get(key));
      }
    }
  }

  private ProfileData(Profile p)
  {
    data.put("countrycode", "EU");
    data.put("mrpp", "0");
    profiles[0] = p;
  }

  private void readProfileData(byte[] profile) throws IOException
  {
    InputStream is = new ByteArrayInputStream(profile);

    String key;
    do {
      key = readNextElement(is);

      if (key != null) {
        String value = readNextElement(is);
        data.put(key, value);
      }
    } while (key != null);
  }

  private String readNextElement(InputStream is) throws IOException
  {
    int length = 0;
    int ch;
    int sanity = 0; // prevents eternal loop reading non-profile data

    while ((ch = is.read()) != ',') {
      if (ch == -1) {
        log.warning("EOF reading profile element!");
        return null; // EOF
      }
      length = (length * 10) + (ch - '0');
      if (sanity++ > 5) throw new IOException("Insane profile data");
    }

    if (length == 0) {
      // end of data
      return null;
    }

    byte[] buf = new byte[length];
    int read = is.read(buf, 0, length);
    if (read != length) throw new IOException("Short read, expected " + length + " but got " + read);

    return EncodingUtil.bytesToStringUtf8(buf);
  }

  public Profile[] getProfiles()
  {
    return profiles;
  }

  public Profile getCurrentProfile()
  {
    String mrpp = data.get(KEY_MRPP);
    if (mrpp != null) return profiles[Integer.parseInt(mrpp)];
    return profiles[0];
  }

  @Override
  @SuppressWarnings({"StringConcatenation"})
  public String toString()
  {
    return "ProfileData{" +
            "data=" + data +
            ", profiles=" + (profiles == null ? null : Arrays.asList(profiles)) +
            '}';
  }

  public final byte[] toData()
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try {
      for (String key : data.keySet()) {
        if (!key.startsWith(KEY_PROFILE)) {
          appendString(out, key);
          appendString(out, data.get(key));
        }
      }

      for (int i = 0; i < MAX_PROFILES; i++) {
        if (profiles[i] != null) {
          Profile profile = profiles[i];
          appendString(out, KEY_PROFILE + i);
          appendString(out, profile.toData());
        }
      }

      out.write(EncodingUtil.stringToBytesUtf8(",0."));
    }
    catch (IOException e) {
      //should never happen
      log.log(Level.SEVERE, "Error converting profile to data", e);
    }

    return out.toByteArray();
  }

  private void appendString(OutputStream out, String string) throws IOException
  {
    byte[] bytes = EncodingUtil.stringToBytesUtf8(string);

    out.write(EncodingUtil.stringToBytesUtf8(String.valueOf(bytes.length)));
    out.write(',');
    out.write(bytes);
  }

  @SuppressWarnings({"HardCodedStringLiteral", "HardcodedFileSeparator", "UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    DebugUtil.setAllLogging();
    // Test a profile can be loaded ok
    ProfileData pd = new ProfileData(new File("testcases/qwsx-pers2.dat"));
    System.out.println(pd.getCurrentProfile().getTower());

    // Restore a profile from goofans published profile
//    Profile p = new Profile(EncodingUtil.bytesToStringUtf8(Utilities.readFile(new File("profile.txt"))));
//    ProfileData pd = new ProfileData(p);
//    System.out.println(pd);
//    System.out.println(pd.getCurrentProfile().getTower());

//    GameFormat.encodeBinFile(new File("pers2.dat"), pd.toData());
  }
}
