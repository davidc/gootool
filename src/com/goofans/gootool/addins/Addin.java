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

  private final File diskFile;
  private final String id;
  private final String name;
  private final int type;
  private final VersionSpec manifestVersion;
  private final VersionSpec version;
  private final String description;
  private final String author;
  private final List<AddinDependency> dependencies;
  private final String levelDir;
  private final Map<String, String> levelNames;
  private final Map<String, String> levelSubtitles;
  private final String levelOcd;
  private String levelCutscene; // 1.1+
  private boolean levelSkipEolSequence = false; // 1.1+
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

  void setLevelCutscene(String levelCutscene)
  {
    this.levelCutscene = levelCutscene;
  }

  public String getLevelCutscene()
  {
    return levelCutscene;
  }

  void setLevelSkipEolSequence(boolean levelSkipEolSequence)
  {
    this.levelSkipEolSequence = levelSkipEolSequence;
  }

  public boolean isLevelSkipEolSequence()
  {
    return levelSkipEolSequence;
  }

  void setThumbnail(BufferedImage thumbnail)
  {
    this.thumbnail = thumbnail;
  }

  public BufferedImage getThumbnail()
  {
    return thumbnail;
  }

  @Override
  @SuppressWarnings({"StringConcatenation"})
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
