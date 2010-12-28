/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.addins;

import com.goofans.gootool.projects.LocalProject;
import net.infotrek.util.EncodingUtil;

import javax.imageio.ImageIO;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.facades.TargetFile;
import com.goofans.gootool.io.Codec;
import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.io.MacGraphicFormat;
import com.goofans.gootool.io.UnicodeReader;
import com.goofans.gootool.platform.PlatformSupport;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.*;

/**
 * Installs an addin into the current custom WoG.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AddinInstaller
{
  private static final Logger log = Logger.getLogger(AddinInstaller.class.getName());

  private static final String[] ALLOWED_ROOT_DIRS = new String[]{"properties/", "res/"};

  private static final String STRINGS_FILE = "text.xml";
  private static final String GOOMOD_DIR_OVERRIDE = "override/";
  private static final String GOOMOD_DIR_MERGE = "merge/";
  private static final String GOOMOD_DIR_COMPILE = "compile/";

  private static final String[] PASSES = new String[]{GOOMOD_DIR_OVERRIDE, GOOMOD_DIR_MERGE, GOOMOD_DIR_COMPILE};
  private static final int PASS_OVERRIDE = 0;
  private static final int PASS_MERGE = 1;
  private static final int PASS_COMPILE = 2;

  private static final List<String> SKIP_FILES = Arrays.asList(".svn", "Thumbs.db", "thumbs.db", ".DS_Store"); //NON-NLS
  private static final String EXTENSION_PNG = ".png";
  private static final String EXTENSION_XSL = ".xsl";
  private static final String EXTENSION_BIN = ".bin";
  private static final String EXTENSION_XML = ".xml";

  private Project project;
  private final SourceFile sourceGameRoot;
  private final TargetFile targetGameRoot;
  private Codec gameXmlCodec;

  public AddinInstaller(Project project)
  {
    this.project = project;
    this.sourceGameRoot = project.getSource().getGameRoot();
    this.targetGameRoot = project.getTarget().getGameRoot();
    gameXmlCodec = project.getCodecForGameXml();
  }

  public void installAddin(Addin addin) throws IOException, AddinFormatException
  {
    log.log(Level.FINE, "Installing addin " + addin.getId());

    AddinReader addinReader = AddinFactory.getAddinReader(addin.getDiskFile());

    try {
      doPasses(addin, addinReader);

      if (addin.getManifestVersion().compareTo(AddinFactory.SPEC_VERSION_1_1) >= 0) {
        if (addinReader.fileExists(STRINGS_FILE)) {
          doStringsFile(addin, addinReader.getInputStream(STRINGS_FILE));
        }
      }
    }
    finally {
      addinReader.close();
    }

    if (addin.getType() == Addin.TYPE_LEVEL) {
      for (AddinLevel level : addin.getLevels()) {
        installLevel(level);
      }
    }

    log.log(Level.FINE, "Addin " + addin.getId() + " installed");
  }

  private void doPasses(Addin addin, AddinReader addinReader) throws IOException, AddinFormatException
  {
    for (int pass = 0; pass < PASSES.length; ++pass) {
      String passPrefix = PASSES[pass];
      log.log(Level.FINER, "Pass " + pass + " (looking in " + passPrefix + ")");

      Iterator<String> passEntries = addinReader.getEntriesInDirectory(passPrefix, SKIP_FILES);

      while (passEntries.hasNext()) {
        String fileName = passEntries.next();

        InputStream is = addinReader.getInputStream(passPrefix + fileName);

        try {
          doPassOnFile(addin, pass, fileName, is);
        }
        finally {
          is.close();
        }
      }
    }
  }

  private void doPassOnFile(Addin addin, int pass, String fileName, InputStream is) throws IOException, AddinFormatException
  {
//    System.out.println("Doing pass " + pass + " on file " + fileName);

    // Validate that the file extension(s) are lower case (#0000275).

    int lastSlash = fileName.lastIndexOf('/');
    int baseStarts = (lastSlash == -1 ? 0 : lastSlash + 1); // Offset of first character of base filename
//    System.out.println("baseName = " + fileName.substring(baseStarts));

    int firstDot = fileName.indexOf('.', baseStarts); // Offset of first period of base filename
    if (firstDot != -1) {
      String extension = fileName.substring(firstDot + 1);
      if (!extension.toLowerCase().equals(extension)) {
        throw new AddinFormatException("Upper case file extension found in '" + fileName + "'");
      }
    }

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

  private void processOverride(String fileName, InputStream is) throws IOException, AddinFormatException
  {
    log.log(Level.FINER, "Override " + fileName);
    checkDirOk(fileName);

    if (fileName.endsWith(EXTENSION_BIN)) {
      throw new AddinFormatException("Bin files are not allowed in the override directory");
    }
    else if (fileName.endsWith(EXTENSION_PNG) && project instanceof LocalProject && PlatformSupport.getPlatform() == PlatformSupport.Platform.MACOSX) {
      // Mac PNG files need to be "compiled"

      TargetFile destFile = targetGameRoot.getChild(project.getGamePngFilename(fileName));
      destFile.getParentDirectory().mkdirs();

      Image image = ImageIO.read(is);
      MacGraphicFormat.encodeImage(destFile.write(), image);
    }
    else {
      TargetFile destFile = targetGameRoot.getChild(fileName);
      destFile.getParentDirectory().mkdirs();

      OutputStream os = destFile.write();
      try {
        Utilities.copyStreams(is, os);
      }
      finally {
        os.close();
      }

      if (fileName.endsWith(EXTENSION_PNG)) {
        // Force the image to be read, so Windows users can detect images that Java can't read and prevent
        // problems on Mac
        ImageIO.read(destFile.read());
      }
    }
  }

  private void processMerge(String fileName, InputStream xslInputStream) throws IOException, AddinFormatException
  {
    log.log(Level.FINER, "Merge " + fileName);
    checkDirOk(fileName);

    if (!fileName.endsWith(EXTENSION_XSL))
      throw new AddinFormatException("Addin has a non-XSLT file in the merge directory: " + fileName);

    TargetFile mergeFile = targetGameRoot.getChild(project.getGameXmlFilename(fileName.substring(0, fileName.length() - 4)));

    if (!mergeFile.isFile()) throw new AddinFormatException("Addin tries to merge a nonexistent file: " + fileName);

    try {
      byte[] sourceBytes = gameXmlCodec.decodeFile(mergeFile);
      Merger merger = new Merger(sourceBytes, new UnicodeReader(xslInputStream, GameFormat.DEFAULT_CHARSET));
      merger.merge();

      gameXmlCodec.encodeFile(mergeFile, merger.getResult());
    }
    catch (TransformerException e) {
      throw new AddinFormatException("Error transforming " + fileName + ":\n" + e.getMessage(), e);
    }
  }

  private void processCompile(String fileName, InputStream is) throws IOException, AddinFormatException
  {
    log.log(Level.FINER, "Compile " + fileName);
    checkDirOk(fileName);

    if (fileName.endsWith(".anim.xml")) {
      //if addin.getManifestVersion().compareTo(AddinFactory.SPEC_VERSION_1_2) >= 0) {
      //else
      throw new AddinFormatException("Animations are not supported in this spec-version");
    }
    else if (fileName.endsWith(".movie.xml")) {
      //if addin.getManifestVersion().compareTo(AddinFactory.SPEC_VERSION_1_2) >= 0) {
      //else
      throw new AddinFormatException("Movies are not supported in this spec-version");
    }
    else if (fileName.endsWith(EXTENSION_XML)) {
      TargetFile destFile = targetGameRoot.getChild(project.getGameXmlFilename(fileName.substring(0, fileName.length() - 4)));
      destFile.getParentDirectory().mkdirs();

      String xml = Utilities.readStreamIntoString(is);

      gameXmlCodec.encodeFile(destFile, xml.getBytes(GameFormat.DEFAULT_CHARSET));
    }
    else {
      throw new AddinFormatException("Addin has an uncompilable file in the compile directory: " + fileName);
    }
  }

  private void installLevel(AddinLevel level) throws IOException, AddinFormatException
  {
    String levelNameId = "LEVEL_NAME_" + level.getDir().toUpperCase(); //NON-NLS
    String levelTextId = "LEVEL_TEXT_" + level.getDir().toUpperCase(); //NON-NLS

    /* First add our two level strings to text.xml */

    try {
      TargetFile textFile = targetGameRoot.getChild(project.getGameXmlFilename("properties/text.xml"));
      byte[] sourceBytes = gameXmlCodec.decodeFile(textFile);

      Merger merger = new Merger(sourceBytes, new InputStreamReader(AddinInstaller.class.getResourceAsStream("/level-text.xsl"), GameFormat.DEFAULT_CHARSET));
      merger.setTransformParameter("level_name_string", makeString(levelNameId, level.getNames()));
      merger.setTransformParameter("level_text_string", makeString(levelTextId, level.getSubtitles()));
      merger.merge();
      gameXmlCodec.encodeFile(textFile, merger.getResult());
    }
    catch (TransformerException e) {
      throw new AddinFormatException("Unable to merge level text", e);
    }

    /* Now add ourselves into the island.xml */
    // TODO we should buffer these up and do them for all levels at the end

    try {
      TargetFile islandFile = targetGameRoot.getChild(project.getGameXmlFilename("res/islands/island1.xml"));
      byte[] sourceBytes = gameXmlCodec.decodeFile(islandFile);

      Merger merger = new Merger(sourceBytes, new InputStreamReader(AddinInstaller.class.getResourceAsStream("/level-island.xsl"), GameFormat.DEFAULT_CHARSET));

      merger.setTransformParameter("level_id", level.getDir());
      merger.setTransformParameter("level_name_id", levelNameId);
      merger.setTransformParameter("level_text_id", levelTextId);
      if (level.getOcd() != null) {
        merger.setTransformParameter("level_ocd", level.getOcd());
      }
      if (level.getCutscene() != null) {
        merger.setTransformParameter("level_cutscene", level.getCutscene());
      }
      if (level.isSkipEolSequence()) {
        merger.setTransformParameter("level_skipeolsequence", true);
      }
      merger.merge();
      gameXmlCodec.encodeFile(islandFile, merger.getResult());
    }
    catch (TransformerException e) {
      throw new AddinFormatException("Unable to merge level island", e);
    }

    /* Now add our buttons to island1.scene.xml */
    // TODO we should buffer these up and do them for all levels at the end

    try {
      TargetFile islandSceneFile = targetGameRoot.getChild(project.getGameXmlFilename("res/levels/island1/island1.scene"));
      byte[] sourceBytes = gameXmlCodec.decodeFile(islandSceneFile);

      Merger merger = new Merger(sourceBytes, new InputStreamReader(AddinInstaller.class.getResourceAsStream("/level-island-scene.xsl"), GameFormat.DEFAULT_CHARSET));

      merger.setTransformParameter("level_id", level.getDir());
      merger.setTransformParameter("level_name_id", levelNameId);
      merger.merge();
//      System.out.println("s = " + s);
//      merger.writeEncoded(islandSceneFile);
      gameXmlCodec.encodeFile(islandSceneFile, merger.getResult());
//		<button id="lb_GoingUp" depth="8" x="-520" y="278" scalex="1" scaley="1" rotation="0" alpha="1" colorize="255,255,255"   up="IMAGE_SCENE_ISLAND1_LEVELMARKERA_UP" over="IMAGE_SCENE_ISLAND1_LEVELMARKERA_OVER" onclick="pl_GoingUp" onmouseenter="ss_GoingUp" onmouseexit="hs_GoingUp" />
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
      sb.append(" ").append(nameKey).append("=\"").append(XMLUtil.escapeEntities(translations.get(nameKey))).append("\"");
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

  // TODO this can be done at the same time as adding level strings (if this is a level), to avoid multiple writes.
  private void doStringsFile(Addin addin, InputStream inputStream) throws IOException, AddinFormatException
  {
    // Load game text.xml
    TargetFile gameTextFile = targetGameRoot.getChild(project.getGameXmlFilename("properties/text.xml"));
    byte[] sourceBytes = gameXmlCodec.decodeFile(gameTextFile);

    Document gameStringsDoc = XMLUtil.loadDocumentFromInputStream(new ByteArrayInputStream(sourceBytes));

    gameStringsDoc.getDocumentElement().appendChild(gameStringsDoc.createTextNode("\n"));
    gameStringsDoc.getDocumentElement().appendChild(gameStringsDoc.createComment("Strings added by GooTool from " + addin.getId())); //NON-NLS
    gameStringsDoc.getDocumentElement().appendChild(gameStringsDoc.createTextNode("\n"));

    // Load addin text.xml
    Document addinStringsDoc = XMLUtil.loadDocumentFromInputStream(inputStream);

    if (!"strings".equals(addinStringsDoc.getDocumentElement().getTagName())) { //NON-NLS
      throw new AddinFormatException("Strings file doesn't have strings as root element");
    }

    // Process addin's strings
    NodeList strings = addinStringsDoc.getDocumentElement().getElementsByTagName("string"); //NON-NLS

    for (int i = 0; i < strings.getLength(); ++i) {
      Element addinString = (Element) strings.item(i);
      String stringId = XMLUtil.getAttributeStringRequired(addinString, "id"); //NON-NLS


      Element gameString = XMLUtil.findElementByAttributeValue(gameStringsDoc.getDocumentElement(), "string", "id", stringId, false); //NON-NLS
      if (gameString == null) {
        // New string, just clone it across
        gameStringsDoc.getDocumentElement().appendChild(gameStringsDoc.importNode(addinString, false));
        gameStringsDoc.getDocumentElement().appendChild(gameStringsDoc.createTextNode("\n"));
      }
      else {
        // Existing string, copy all attributes except ID
        gameStringsDoc.getDocumentElement().appendChild(gameStringsDoc.createComment("Modified " + stringId)); //NON-NLS
        gameStringsDoc.getDocumentElement().appendChild(gameStringsDoc.createTextNode("\n"));

        NamedNodeMap attributeMap = addinString.getAttributes();
        for (int j = 0; j < attributeMap.getLength(); ++j) {
          Attr attribute = (Attr) attributeMap.item(j);
          if (!"id".equals(attribute.getName())) { //NON-NLS
            gameString.setAttribute(attribute.getName(), attribute.getValue());
          }
        }
      }
    }

    try {
      gameXmlCodec.encodeFile(gameTextFile, EncodingUtil.stringToBytesUtf8(XMLUtil.writeDocumentToString(gameStringsDoc)));
    }
    catch (TransformerException e) {
      throw new IOException("Unable to write text.xml: " + e.getLocalizedMessage());
    }
  }

  public static void main(String[] args) throws IOException, AddinFormatException
  {
    Addin addin = AddinFactory.loadAddinFromDir(new File("addins/src/com.goofans.davidc.jingleballs"));
    AddinInstaller installer = new AddinInstaller(ProjectManager.simpleInit());
    installer.installAddin(addin);
  }
}
