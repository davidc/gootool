package com.goofans.gootool.addins;

import javax.imageio.ImageIO;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.io.MacGraphicFormat;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.wog.WorldOfGoo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AddinInstaller
{
  private static final Logger log = Logger.getLogger(AddinInstaller.class.getName());

  private static final String[] ALLOWED_ROOT_DIRS = new String[]{"properties/", "res/"};

  private static final String GOOMOD_DIR_OVERRIDE = "override/";
  private static final String GOOMOD_DIR_MERGE = "merge/";
  private static final String GOOMOD_DIR_COMPILE = "compile/";

  private static final String[] PASSES = new String[]{GOOMOD_DIR_OVERRIDE, GOOMOD_DIR_MERGE, GOOMOD_DIR_COMPILE};
  private static final int PASS_OVERRIDE = 0;
  private static final int PASS_MERGE = 1;
  private static final int PASS_COMPILE = 2;

  private AddinInstaller()
  {
  }

  public static void installAddin(Addin addin) throws IOException, AddinFormatException
  {
    log.log(Level.FINE, "Installing addin " + addin.getId());

    if (addin.getDiskFile().isFile()) {
      ZipFile zipFile = new ZipFile(addin.getDiskFile());

      try {
        for (int pass = 0; pass < PASSES.length; ++pass) {
          String passPrefix = PASSES[pass];
          log.log(Level.FINER, "Pass " + pass + " (looking in " + passPrefix + ")");

          Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

          while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();

            if (!zipEntry.isDirectory() && zipEntry.getName().startsWith(passPrefix)) {
              String fileName = zipEntry.getName().substring(passPrefix.length());
              InputStream is = zipFile.getInputStream(zipEntry);

              try {
                doPass(pass, fileName, is);
              }
              finally {
                is.close();
              }
            }
          }
        }
      }
      finally {
        zipFile.close();
      }
    }
    else {
      File rootDir;
      rootDir = addin.getDiskFile();

      for (int pass = 0; pass < PASSES.length; ++pass) {
        String passPrefix = PASSES[pass];
        log.log(Level.FINER, "Pass " + pass + " (looking in " + passPrefix + ")");

        File passSrcDir = new File(rootDir, passPrefix);
        if (passSrcDir.isDirectory()) {
          doPassInDir(pass, passSrcDir, "");
        }
      }
    }

    if (addin.getType() == Addin.TYPE_LEVEL) {
      installLevel(addin);
    }

    log.log(Level.FINE, "Addin " + addin.getId() + " installed");
  }

  private static void doPassInDir(int pass, File passSrcDir, String pathName) throws IOException, AddinFormatException
  {
    for (File file : passSrcDir.listFiles()) {
      if (file.isDirectory() && !file.getName().equals(".svn")) {
        doPassInDir(pass, file, pathName + file.getName() + "/");
      }
      else if (file.isFile()) {
        InputStream is = new FileInputStream(file);
        try {
          doPass(pass, pathName + file.getName(), is);
        }
        finally {
          is.close();
        }
      }
    }
  }

  private static void doPass(int pass, String fileName, InputStream is) throws IOException, AddinFormatException
  {
//    System.out.println("Doing pass " + pass + " on file " + fileName);
    if (pass == PASS_OVERRIDE) {
      processOverride(fileName, is);
    }
    else if (pass == PASS_MERGE) {
      processMerge(fileName, is);
    }
    else if (pass == PASS_COMPILE) {
      processCompile(fileName, is);
    }
  }

  private static void checkDirOk(String fileName) throws AddinFormatException
  {
    boolean isOk = false;
    for (String allowedRootDir : ALLOWED_ROOT_DIRS) {
      if (fileName.startsWith(allowedRootDir)) {
        isOk = true;
        break;
      }
    }

    if (!isOk) {
      throw new AddinFormatException("Addin tries to install a file in a prohibited directory: " + fileName);
    }
  }

  private static void processOverride(String fileName, InputStream is) throws IOException, AddinFormatException
  {
    log.log(Level.FINER, "Override " + fileName);
    checkDirOk(fileName);


    if (fileName.endsWith(".png") && PlatformSupport.getPlatform() == PlatformSupport.Platform.MACOSX) {
      // Mac PNG files need to be "compiled"
      File destFile = WorldOfGoo.getTheInstance().getCustomGameFile(fileName + ".binltl");
      destFile.getParentFile().mkdirs(); // ensure the directory exists

      Image image = ImageIO.read(is);
      MacGraphicFormat.encodeImage(destFile, image);
    }
    else {
      File destFile = WorldOfGoo.getTheInstance().getCustomGameFile(fileName);

      destFile.getParentFile().mkdirs(); // ensure the directory exists

      try {
        OutputStream os = new FileOutputStream(destFile);
        try {
          Utilities.copyStreams(is, os);
        }
        finally {
          os.close();
        }
      }
      finally {
        is.close();
      }
    }
  }

  private static void processMerge(String fileName, InputStream is) throws IOException, AddinFormatException
  {
    log.log(Level.FINER, "Merge " + fileName);
    checkDirOk(fileName);

    if (!fileName.endsWith(".xsl")) throw new AddinFormatException("Addin has a non-XSLT file in the merge directory: " + fileName);

    File mergeFile = WorldOfGoo.getTheInstance().getCustomGameFile(fileName.substring(0, fileName.length() - 4) + ".bin");

    if (!mergeFile.exists()) throw new AddinFormatException("Addin tries to merge a nonexistent file: " + fileName);

    try {
      Merger merger = new Merger(mergeFile, new InputStreamReader(is, "UTF-8"));
      merger.merge();
      merger.writeEncoded(mergeFile);
    }
    catch (TransformerException e) {
      throw new AddinFormatException("Error transforming " + fileName + ":\n" + e.getMessage(), e);
    }
    finally {
      is.close();
    }
  }

  private static void processCompile(String fileName, InputStream is) throws IOException, AddinFormatException
  {
    log.log(Level.FINER, "Compile " + fileName);
    checkDirOk(fileName);

    if (!fileName.endsWith(".xml")) throw new AddinFormatException("Addin has a non-XML file in the compile directory " + fileName);

    File destFile = WorldOfGoo.getTheInstance().getCustomGameFile(fileName.substring(0, fileName.length() - 4) + ".bin");

    destFile.getParentFile().mkdirs(); // ensure the directory exists

    String xml;
    try {
      xml = Utilities.readStreamIntoString(is);
    }
    finally {
      is.close();
    }
    GameFormat.encodeBinFile(destFile, xml);
  }

  private static void installLevel(Addin addin) throws IOException, AddinFormatException
  {
    String levelNameId = "LEVEL_NAME_" + addin.getLevelDir().toUpperCase();
    String levelTextId = "LEVEL_TEXT_" + addin.getLevelDir().toUpperCase();

    /* First add our two level strings to text.xml */

    File textFile = WorldOfGoo.getTheInstance().getCustomGameFile("properties/text.xml.bin");
    try {
      Merger merger = new Merger(textFile, new InputStreamReader(AddinInstaller.class.getResourceAsStream("/level-text.xsl"), "UTF-8"));
      merger.setTransformParameter("level_name_string", makeString(levelNameId, addin.getLevelNames()));
      merger.setTransformParameter("level_text_string", makeString(levelTextId, addin.getLevelSubtitles()));
      merger.merge();
//      System.out.println("s = " + s);
      merger.writeEncoded(textFile);
    }
    catch (TransformerException e) {
      throw new AddinFormatException("Unable to merge level text", e);
    }

    /* Now add ourselves into the island.xml */

    File islandFile = WorldOfGoo.getTheInstance().getCustomGameFile("res/islands/island1.xml.bin");
    try {
      Merger merger = new Merger(islandFile, new InputStreamReader(AddinInstaller.class.getResourceAsStream("/level-island.xsl"), "UTF-8"));

      merger.setTransformParameter("level_id", addin.getLevelDir());
      merger.setTransformParameter("level_name_id", levelNameId);
      merger.setTransformParameter("level_text_id", levelTextId);
      merger.setTransformParameter("level_ocd", addin.getLevelOcd());
      merger.merge();
//      System.out.println("s = " + s);
      merger.writeEncoded(islandFile);
    }
    catch (TransformerException e) {
      throw new AddinFormatException("Unable to merge level island", e);
    }

    /* Now add our buttons to island1.scene.xml */
    File islandSceneFile = WorldOfGoo.getTheInstance().getCustomGameFile("res/levels/island1/island1.scene.bin");
    try {
      Merger merger = new Merger(islandSceneFile, new InputStreamReader(AddinInstaller.class.getResourceAsStream("/level-island-scene.xsl"), "UTF-8"));

      merger.setTransformParameter("level_id", addin.getLevelDir());
      merger.setTransformParameter("level_name_id", levelNameId);
      merger.merge();
//      System.out.println("s = " + s);
      merger.writeEncoded(islandSceneFile);
//		<button id="lb_GoingUp" depth="8" x="-520" y="278" scalex="1" scaley="1" rotation="0" alpha="1" colorize="255,255,255"   up="IMAGE_SCENE_ISLAND1_LEVELMARKERA_UP" over="IMAGE_SCENE_ISLAND1_LEVELMARKERA_OVER" onclick="pl_GoingUp" onmouseenter="ss_GoingUp" onmouseexit="hs_GoingUp" />

      // TODO OCD flag location
    }
    catch (TransformerException e) {
      throw new AddinFormatException("Unable to merge level island scene", e);
    }
  }

  private static String makeString(String levelSubtitleId, Map<String, String> translations)
  {
    StringBuilder sb = new StringBuilder("<string id=\"");

    sb.append(levelSubtitleId).append("\"");
    for (String nameKey : translations.keySet()) {
      sb.append(" ").append(nameKey).append("=\"").append(translations.get(nameKey)).append("\"");
    }
    sb.append(" />");
    return sb.toString();
  }

  private static void addStringMap(Document d, Node namesNode, Map<String, String> x)
  {
    for (String nameKey : x.keySet()) {
      Element nameNode = (Element) namesNode.appendChild(d.createElement("string"));
      nameNode.setAttribute("lang", nameKey);
      nameNode.setTextContent(x.get(nameKey));
    }
  }

  public static void main(String[] args) throws IOException, AddinFormatException
  {
    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    worldOfGoo.init();
    worldOfGoo.setCustomDir(new File("C:\\BLAH\\"));
    Addin addin = AddinFactory.loadAddinFromDir(new File("addins/src/net.davidc.madscientist.dejavu"));

    AddinInstaller.installAddin(addin);
  }
}
