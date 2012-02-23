package net.infotrek.util.prefs;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;

/**
 * Preferences implementation that stores to a user-defined file. See FilePreferencesFactory.
 *
 * @author David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @version $Id$
 */
public class FilePreferences extends AbstractPreferences
{
  private static final Logger log = Logger.getLogger(FilePreferences.class.getName());

  private final Map<String, String> root;
  private final Map<String, FilePreferences> children;
  private boolean isRemoved = false;

  public FilePreferences(AbstractPreferences parent, String name)
  {
    super(parent, name);

    log.finest("Instantiating node " + name);

    root = new TreeMap<String, String>();
    children = new TreeMap<String, FilePreferences>();

    try {
      sync();
    }
    catch (BackingStoreException e) {
      log.log(Level.SEVERE, "Unable to sync on creation of node " + name, e);
    }
  }

  @Override
  protected void putSpi(String key, String value)
  {
    root.put(key, value);
    try {
      flush();
    }
    catch (BackingStoreException e) {
      log.log(Level.SEVERE, "Unable to flush after putting " + key, e);
    }
  }

  @Override
  protected String getSpi(String key)
  {
    return root.get(key);
  }

  @Override
  protected void removeSpi(String key)
  {
    root.remove(key);
    try {
      flush();
    }
    catch (BackingStoreException e) {
      log.log(Level.SEVERE, "Unable to flush after removing " + key, e);
    }
  }

  @Override
  protected void removeNodeSpi() throws BackingStoreException
  {
    isRemoved = true;
    flush();
  }

  @Override
  protected String[] keysSpi() throws BackingStoreException
  {
    return root.keySet().toArray(new String[root.keySet().size()]);
  }

  @Override
  protected String[] childrenNamesSpi() throws BackingStoreException
  {
    return children.keySet().toArray(new String[children.keySet().size()]);
  }

  @Override
  protected FilePreferences childSpi(String name)
  {
    FilePreferences child = children.get(name);
    if (child == null || child.isRemoved()) {
      child = new FilePreferences(this, name);
      children.put(name, child);
    }
    return child;
  }

  @Override
  protected void syncSpi() throws BackingStoreException
  {
    if (isRemoved()) return;

    File file = FilePreferencesFactory.getPreferencesFile();
    if (!file.exists()) return;

    synchronized (file) {
      Properties p = new Properties();
      try {
        FileInputStream is = new FileInputStream(file);
        try {
          p.load(is);
        }
        finally {
          is.close();
        }
      }
      catch (IOException e) {
        throw new BackingStoreException(e);
      }

      StringBuilder sb = new StringBuilder();
      getPath(sb);
      String path = sb.toString();

      Enumeration<?> pnen = p.propertyNames();
      while (pnen.hasMoreElements()) {
        String propKey = (String) pnen.nextElement();
        if (propKey.startsWith(path)) {
          String subKey = propKey.substring(path.length());
          // Only load immediate descendants
          if (subKey.indexOf('.') == -1) {
            root.put(subKey, p.getProperty(propKey));
          }
        }
      }
    }
  }

  private void getPath(StringBuilder sb)
  {
    FilePreferences parent = (FilePreferences) parent();
    if (parent == null) return;

    parent.getPath(sb);
    sb.append(name()).append('.');
  }

  @Override
  protected void flushSpi() throws BackingStoreException
  {
    File file = FilePreferencesFactory.getPreferencesFile();

    synchronized (file) {
      Properties p = new Properties();
      try {

        StringBuilder sb = new StringBuilder();
        getPath(sb);
        String path = sb.toString();

        if (file.exists()) {
          FileInputStream is = new FileInputStream(file);
          try {
            p.load(is);
          }
          finally {
            is.close();
          }

          List<String> toRemove = new ArrayList<String>();

          // Make a list of all direct children of this node to be removed
          final Enumeration<?> pnen = p.propertyNames();
          while (pnen.hasMoreElements()) {
            String propKey = (String) pnen.nextElement();
            if (propKey.startsWith(path)) {
              String subKey = propKey.substring(path.length());
              // Only do immediate descendants
              if (subKey.indexOf('.') == -1) {
                toRemove.add(propKey);
              }
            }
          }

          // Remove them now that the enumeration is done with
          for (String propKey : toRemove) {
            p.remove(propKey);
          }
        }

        // If this node hasn't been removed, add back in any values
        if (!isRemoved) {
          for (String s : root.keySet()) {
            p.setProperty(path + s, root.get(s));
          }
        }

        FileOutputStream os = new FileOutputStream(file);
        try {
          p.store(os, "FilePreferences");
        }
        finally {
          os.close();
        }
      }
      catch (IOException e) {
        throw new BackingStoreException(e);
      }
    }
  }
}
