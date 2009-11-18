package com.goofans.gootool.l10n;

import java.net.URL;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.nio.charset.Charset;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class TranslationDownloader
{
  protected static final String DEFAULT_WIKI_URL = "http://hell.student.utwente.nl/wog/mediawiki/";

  private TranslationDownloader()
  {
  }

  public static Map<String, String> getTranslations(String wikiBase, String wikiPage, boolean getTranslation) throws IOException
  {
    BufferedReader r = new BufferedReader(new InputStreamReader(new URL(wikiBase + "index.php?title=" + wikiPage + "&action=raw").openStream(), Charset.forName("UTF-8")));

    /**
     * 0 = outside everything
     * 1 = got a |-, looking for a key
     * 2 = got a key, looking for english
     * 3 = got english, looking for translation
     */
    int state = 0;

    String key = null;
    String english = null;
    String translation;

    Map<String, String> translations = new HashMap<String, String>();


    String line;
    while ((line = r.readLine()) != null) {
//      System.out.println(line);
      line = line.trim();
      switch (state) {
        case 0:
          if ("|-".equals(line)) state = 1;
          break;
        case 1:
          if (line.startsWith("| ")) {
            key = line.substring(2);
            state = 2;
          }
          else {
            state = 0;
          }
          break;
        case 2:
          if (line.startsWith("| ")) {
            english = line.substring(2);
            state = 3;
          }
          else {
            state = 0;
          }
          break;
        case 3:
          if (line.startsWith("| ")) {
            translation = line.substring(2);

            if (translation.length() == 0 || !getTranslation) translation = english;
//            System.out.println("key = " + key);
//            System.out.println("english = " + english);
//            System.out.println("translation = " + translation);

            translation = translation.replaceAll("\\<br\\>", "|");
            translation = translation.replaceAll(" \\|", "|");
            translation = translation.replaceAll("\\| ", "|");
            translations.put(key, translation);
          }
          state = 0;
          break;

      }
    }
    return translations;
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
  public static void main(String[] args) throws IOException
  {
    Map<String, String> translations = getTranslations(DEFAULT_WIKI_URL, "Russian_translation", true);
    for (Map.Entry<String, String> entry : translations.entrySet()) {
      System.out.println(entry.getKey() + " = " + entry.getValue());
    }
  }
}
