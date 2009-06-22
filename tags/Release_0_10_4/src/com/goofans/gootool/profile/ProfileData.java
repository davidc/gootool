package com.goofans.gootool.profile;

import net.infotrek.util.EncodingUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.goofans.gootool.io.GameFormat;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProfileData
{
  private static final Logger log = Logger.getLogger(ProfileData.class.getName());

  private Map<String, String> data;

  private Profile[] profiles = new Profile[3];

  public ProfileData(File f) throws IOException
  {
    byte[] profile = GameFormat.decodeProfileFile(f);

    readProfileData(profile);

    log.finest("ProfileData is " + data);

    for (String key : data.keySet()) {
      if (key.startsWith("profile_")) {
        int profileIndex = Integer.valueOf(key.substring(8));
        profiles[profileIndex] = new Profile(data.get(key));
      }
    }
  }

  private void readProfileData(byte[] profile) throws IOException
  {
    InputStream is = new ByteArrayInputStream(profile);

    data = new TreeMap<String, String>();

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
    String mrpp = data.get("mrpp");
    if (mrpp != null) return profiles[Integer.parseInt(mrpp)];
    return profiles[0];
  }


  public String toString()
  {
    return "ProfileData{" +
            "data=" + data +
            ", profiles=" + (profiles == null ? null : Arrays.asList(profiles)) +
            '}';
  }
}
