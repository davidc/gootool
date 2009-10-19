package com.goofans.gootool.addins;

import com.goofans.gootool.util.VersionSpec;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.awt.image.BufferedImage;

/**
 * Immutable after construction. There are setters for the purpose of easier construction in AddinFactory, but they are package-local.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Addin
{
  private static final Logger log = Logger.getLogger(Addin.class.getName());

  public static final int TYPE_UNKNOWN = 0; // no loaded addin should ever have this type
  public static final int TYPE_MOD = 1;
  public static final int TYPE_LEVEL = 2;

  private static final String TYPE_UNKNOWN_STR = "Unknown";
  private static final String TYPE_MOD_STR = "Mod";
  private static final String TYPE_LEVEL_STR = "Level";

  private File diskFile;
  private String id;
  private String name;
  private int type;
  private VersionSpec manifestVersion;
  private VersionSpec version;
  private String description;
  private String author;
  private List<AddinDependency> dependencies;
  private String levelDir;
  private Map<String, String> levelNames;
  private Map<String, String> levelSubtitles;
  private String levelOcd;
  private BufferedImage thumbnail; // 1.1+

  public Addin(File diskFile, String id, String name, int type, VersionSpec manifestVersion, VersionSpec version, String description, String author, List<AddinDependency> dependencies, String levelDir, Map<String, String> levelNames, Map<String, String> levelSubtitles, String levelOcd)
  {
    this.diskFile = diskFile;
    this.id = id;
    this.name = name;
    this.type = type;
    this.manifestVersion = manifestVersion;
    this.version = version;
    this.description = description;
    this.author = author;
    this.dependencies = dependencies;
    this.levelDir = levelDir;
    this.levelNames = levelNames;
    this.levelSubtitles = levelSubtitles;
    this.levelOcd = levelOcd;
  }

  public File getDiskFile()
  {
    return diskFile;
  }

  public String getId()
  {
    return id;
  }

  public String getName()
  {
    return name;
  }

  public int getType()
  {
    return type;
  }

  public String getTypeText()
  {
    if (type == TYPE_LEVEL) return TYPE_LEVEL_STR;
    else if (type == TYPE_MOD) return TYPE_MOD_STR;
    else return TYPE_UNKNOWN_STR;
  }

  public VersionSpec getManifestVersion()
  {
    return manifestVersion;
  }

  public VersionSpec getVersion()
  {
    return version;
  }

  public String getDescription()
  {
    return description;
  }

  public String getAuthor()
  {
    return author;
  }

  public List<AddinDependency> getDependencies()
  {
    return dependencies;
  }

  public boolean areDependenciesSatisfiedBy(List<Addin> addins)
  {
    for (AddinDependency dependency : dependencies) {
      if (!dependency.isSatisfiedBy(addins)) {
        log.fine("Dependency " + dependency + " not satisfied");
        return false;
      }
    }
    return true;
  }

  public String getLevelDir()
  {
    return levelDir;
  }

  public Map<String, String> getLevelNames()
  {
    return levelNames;
  }

  public Map<String, String> getLevelSubtitles()
  {
    return levelSubtitles;
  }

  public String getLevelOcd()
  {
    return levelOcd;
  }

  void setThumbnail(BufferedImage thumbnail)
  {
    this.thumbnail = thumbnail;
  }

  public BufferedImage getThumbnail()
  {
    return thumbnail;
  }

  public String toString()
  {
    return "Addin{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", type=" + type +
            ", version='" + version + '\'' +
            ", description='" + description + '\'' +
            ", author='" + author + '\'' +
            ", dependencies =" + dependencies +
            '}';
  }

  public static int typeFromString(String typeStr)
  {
    if (typeStr.equalsIgnoreCase(TYPE_MOD_STR)) {
      return TYPE_MOD;
    }
    else if (typeStr.equalsIgnoreCase(TYPE_LEVEL_STR)) {
      return TYPE_LEVEL;
    }
    else {
      return TYPE_UNKNOWN;
    }
  }
}
