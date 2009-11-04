package com.goofans.gootool.profile;

import java.io.IOException;
import java.util.Random;
import java.security.SecureRandom;

import com.goofans.gootool.io.GameFormat;

/**
 * Updates the user's profile file to add online IDs to each profile that is missing one.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GenerateOnlineIds
{
  private static final Random RANDOM  = new SecureRandom();
  private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7',
          '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

  private GenerateOnlineIds()
  {
  }

  public static void generateOnlineIds() throws IOException
  {
    ProfileData profileData = ProfileFactory.getProfileData();

    for (Profile profile : profileData.getProfiles()) {
      if (profile != null && profile.getOnlineId() == null) {
        profile.setOnlineId(generateId());
      }
    }

    GameFormat.encodeProfileFile(ProfileFactory.getProfileFile(), profileData.toData());
  }

  @SuppressWarnings({"MagicNumber"})
  private static String generateId()
  {
    byte[] bytes = new byte[16];
    RANDOM.nextBytes(bytes);

    char[] chars = new char[32];
    for (int i = 0; i < 16; ++i) {
      int theByte = bytes[i];
      chars[i * 2] = HEX_DIGITS[theByte & 0xf];
      chars[i * 2 + 1] = HEX_DIGITS[theByte >> 4 & 0xf];
    }

    return new String(chars);
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    System.out.println(generateId());
    System.out.println(generateId());
    System.out.println(generateId());
    System.out.println(generateId());
    System.out.println(generateId());
    System.out.println(generateId());
    ProfileFactory.init();
    generateOnlineIds();
  }
}
