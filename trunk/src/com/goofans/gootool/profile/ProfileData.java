package com.goofans.gootool.profile;

import com.goofans.gootool.io.BinFormat;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * @author David Croft (david.croft@infotrek.net)
 * @version $Id$
 */
public class ProfileData
{
  private static final Logger log = Logger.getLogger(ProfileData.class.getName());

  private Map<String, String> data;

  private Profile[] profiles = new Profile[3];// = {null,null,null};

  public ProfileData(File f) throws IOException
  {
    String profile = BinFormat.decodeFile(f);

    readProfileData(profile);

    log.finest("ProfileData is " + data);

    for (String key : data.keySet()) {
      if (key.startsWith("profile_")) {
        int profileIndex = Integer.valueOf(key.substring(8));
        profiles[profileIndex] = new Profile(data.get(key));
      }
    }
  }

  private void readProfileData(String profile) throws IOException
  {
    StringReader r = new StringReader(profile);

    data = new TreeMap<String, String>();

    String key;
    do {
      key = readNextElement(r);

      if (key != null) {
        String value = readNextElement(r);
        data.put(key, value);
      }
    } while (key != null);
  }

  private String readNextElement(StringReader r) throws IOException
  {
//    boolean collectingNumbers;
    int length = 0;
    int ch;

    while ((ch = r.read()) != ',') {
      length = (length * 10) + (ch - '0');
    }

    if (length == 0) {
      // end of data
      return null;
    }

    char[] buf = new char[length];
    int read = r.read(buf, 0, length);
    if (read != length) throw new IOException("Short read, expected " + length + " but got " + read);

    return new String(buf);
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
