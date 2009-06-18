package net.infotrek.util.prefs;

import java.io.File;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;
import java.util.prefs.BackingStoreException;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.wog.WorldOfGoo;
import com.goofans.gootool.util.DebugUtil;

/**
 * PreferencesFactory implementation that stores the preferences in a user-defined file. To use it,
 * set the system property <tt>java.util.prefs.PreferencesFactory</tt> to
 * <tt>net.infotrek.util.prefs.FilePreferencesFactory</tt>
 * <p/>
 * The file defaults to [user.home]/.fileprefs, but may be overridden with the system property
 * <tt>net.infotrek.util.prefs.FilePreferencesFactory.file</tt>
 *
 * @author David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @version $Id$
 */
public class FilePreferencesFactory implements PreferencesFactory
{
  private static final Logger log = Logger.getLogger(FilePreferencesFactory.class.getName());

  Preferences rootPreferences;
  public static final String SYSTEM_PROPERTY_FILE = "net.infotrek.util.prefs.FilePreferencesFactory.file";

  public Preferences systemRoot()
  {
    return userRoot();
  }

  public Preferences userRoot()
  {
    if (rootPreferences == null) {
      log.finer("Instantiating root preferences");

      rootPreferences = new FilePreferences(null, "");
    }
    return rootPreferences;
  }

  private static File preferencesFile;

  public static File getPreferencesFile()
  {
    if (preferencesFile == null) {
      String prefsFile = System.getProperty(SYSTEM_PROPERTY_FILE);
      if (prefsFile == null || prefsFile.length() == 0) {
        prefsFile = System.getProperty("user.home") + File.separator + ".fileprefs";
      }
      preferencesFile = new File(prefsFile).getAbsoluteFile();
      log.finer("Preferences file is " + preferencesFile);
    }
    return preferencesFile;
  }

  public static void main(String[] args) throws BackingStoreException
  {
    DebugUtil.setAllLogging();

    System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());
    System.setProperty(SYSTEM_PROPERTY_FILE, "myprefs.txt");

    Preferences p2 = Preferences.userNodeForPackage(WorldOfGoo.class);
    p2.put("hello", "there");
    Preferences p = Preferences.userNodeForPackage(GooTool.class);

    for (String s : p.keys()) {
      System.out.println("p[" + s + "]=" + p.get(s, null));
    }

    System.out.println("p.getBoolean(\"hi\") = " + p.getBoolean("hi", false));
    p.putBoolean("hi", true);
//    p.flush();
    System.out.println("p.get(\"Number\", null) = " + p.get("Number", null));
    p.put("Number", String.valueOf(System.currentTimeMillis()));

//    p.remove("Number");
//    p.removeNode();
  }
}
