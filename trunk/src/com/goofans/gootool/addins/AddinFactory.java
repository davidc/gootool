package com.goofans.gootool.addins;

import com.goofans.gootool.util.VersionSpec;
import com.goofans.gootool.util.XMLUtil;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.xpath.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AddinFactory
{
  private static final Logger log = Logger.getLogger(AddinFactory.class.getName());

  private static final XPathExpression XPATH_ADDIN_SPECVERSION;
  private static final XPathExpression XPATH_ADDIN_ID;
  private static final XPathExpression XPATH_ADDIN_NAME;
  private static final XPathExpression XPATH_ADDIN_TYPE;
  private static final XPathExpression XPATH_ADDIN_VERSION;
  private static final XPathExpression XPATH_ADDIN_DESCRIPTION;
  private static final XPathExpression XPATH_ADDIN_AUTHOR;
  private static final XPathExpression XPATH_ADDIN_DEPENDENCIES;
  private static final XPathExpression XPATH_ADDIN_LEVEL;
  private static final XPathExpression XPATH_ADDIN_LEVEL_DIR;
  private static final XPathExpression XPATH_ADDIN_LEVEL_NAME;
  private static final XPathExpression XPATH_ADDIN_LEVEL_SUBTITLE;
  private static final XPathExpression XPATH_ADDIN_LEVEL_OCD;
  private static final XPathExpression XPATH_ADDIN_THUMBNAIL;

  private static final Pattern PATTERN_ID = Pattern.compile("^[-\\p{Alnum}]+(\\.[-\\p{Alnum}]+)+$"); // require at least 1 domain component
  private static final Pattern PATTERN_NAME = Pattern.compile("^\\p{Alnum}[\\p{Graph} ]+$");

  private static final String ADDIN_DEPENDS_REF = "ref";
  private static final String ADDIN_DEPENDS_MIN_VERSION = "min-version";
  private static final String ADDIN_DEPENDS_MAX_VERSION = "max-version";

  private static final VersionSpec SPEC_VERSION_1_0 = new VersionSpec("1.0");
  private static final VersionSpec SPEC_VERSION_1_1 = new VersionSpec("1.1");

  private static final String GOOMOD_MANIFEST = "addin.xml";


  static {
    XPath path = XPathFactory.newInstance().newXPath();
    try {
      XPATH_ADDIN_SPECVERSION = path.compile("/addin/@spec-version");
      XPATH_ADDIN_ID = path.compile("/addin/id");
      XPATH_ADDIN_NAME = path.compile("/addin/name");
      XPATH_ADDIN_TYPE = path.compile("/addin/type");
      XPATH_ADDIN_VERSION = path.compile("/addin/version");
      XPATH_ADDIN_DESCRIPTION = path.compile("/addin/description");
      XPATH_ADDIN_AUTHOR = path.compile("/addin/author");
      XPATH_ADDIN_DEPENDENCIES = path.compile("/addin/dependencies/depends");
      XPATH_ADDIN_LEVEL = path.compile("/addin/level");
      XPATH_ADDIN_LEVEL_DIR = path.compile("/addin/level/dir");
      XPATH_ADDIN_LEVEL_NAME = path.compile("/addin/level/name");
      XPATH_ADDIN_LEVEL_SUBTITLE = path.compile("/addin/level/subtitle");
      XPATH_ADDIN_LEVEL_OCD = path.compile("/addin/level/ocd");
      XPATH_ADDIN_THUMBNAIL = path.compile("/addin/thumbnail");
    }
    catch (XPathExpressionException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private AddinFactory()
  {
  }


  public static Addin loadAddin(File file) throws AddinFormatException, IOException
  {
    log.fine("Loading addin from goomod " + file);
    AddinReader addinReader = new GoomodFileReader(file);

    try {
      return loadAddinFromReader(addinReader, file);
    }
    finally {
      addinReader.close();
    }
  }

  public static Addin loadAddinFromDir(File dir) throws AddinFormatException, IOException
  {
    log.fine("Loading addin from expanded dir " + dir);
    AddinReader addinReader = new ExpandedAddinReader(dir);

    try {
      return loadAddinFromReader(addinReader, dir);
    }
    finally {
      addinReader.close();
    }
  }

  private static Addin loadAddinFromReader(AddinReader addinReader, File addinDiskFile) throws AddinFormatException, IOException
  {
    InputStream is;

    try {
      is = addinReader.getInputStream(GOOMOD_MANIFEST);
    }
    catch (FileNotFoundException e) {
      throw new AddinFormatException("No manifest found, is this an addin?");
    }

    try {
      return readManifest(is, addinDiskFile, addinReader);
    }
    finally {
      is.close();
    }
  }


  // Synchronized since xpath expressions and patterns aren't thread safe
  private static synchronized Addin readManifest(InputStream is, File addinDiskFile, AddinReader addinReader) throws IOException, AddinFormatException
  {
    Document document = XMLUtil.loadDocumentFromInputStream(is);

    try {
      String specVersionStr = getString(document, XPATH_ADDIN_SPECVERSION);
      if (specVersionStr.length() == 0) throw new AddinFormatException("No spec-version found");
      VersionSpec manifestVersion = new VersionSpec(specVersionStr);

      if (manifestVersion.compareTo(SPEC_VERSION_1_0) < 0) {
        throw new AddinFormatException("This addin uses outdated spec-version " + manifestVersion + ". Please upgrade this addin.");
      }
      else if (manifestVersion.equals(SPEC_VERSION_1_0)) {
        return readManifestVersion1_0(document, manifestVersion, addinDiskFile);
      }
      else if (manifestVersion.equals(SPEC_VERSION_1_1)) {
        return readManifestVersion1_1(document, manifestVersion, addinDiskFile, addinReader);
      }
      else {
        throw new AddinFormatException("This addin uses unsupported spec-version " + manifestVersion + ". Please upgrade GooTool.");
      }
    }
    catch (XPathExpressionException e) {
      throw new AddinFormatException("Unable to parse XPath: " + e.getLocalizedMessage(), e);
    }
  }

  /**
   * Reads the manifest for spec-version 1.0, the first version supported by GooTool.
   *
   * @param document        the DOM document of the manifest file.
   * @param manifestVersion The spec-version of the manifest.
   * @param addinDiskFile   The addin file or directory on disk.
   * @return The constructed addin.
   * @throws AddinFormatException if the addin was somehow invalid.
   * @throws javax.xml.xpath.XPathExpressionException
   *                              if the manifest was unparseable (really, should be AddinFormatException).
   */

  private static Addin readManifestVersion1_0(Document document, VersionSpec manifestVersion, File addinDiskFile) throws XPathExpressionException, AddinFormatException
  {
    String id = getStringRequiredValidated(document, XPATH_ADDIN_ID, PATTERN_ID, "id");
    String name = getStringRequiredValidated(document, XPATH_ADDIN_NAME, PATTERN_NAME, "name");

    String typeStr = getStringRequired(document, XPATH_ADDIN_TYPE, "type");
    int type = Addin.typeFromString(typeStr);
    if (type == Addin.TYPE_UNKNOWN) throw new AddinFormatException("Invalid addin type " + typeStr);

    String versionStr = getStringRequired(document, XPATH_ADDIN_VERSION, "version");
    VersionSpec version = decodeVersion(versionStr, "version");

    String description = getStringRequired(document, XPATH_ADDIN_DESCRIPTION, "description");
    String author = getStringRequired(document, XPATH_ADDIN_AUTHOR, "author");

    NodeList depends = (NodeList) XPATH_ADDIN_DEPENDENCIES.evaluate(document, XPathConstants.NODESET);

    List<AddinDependency> dependencies = new ArrayList<AddinDependency>(depends.getLength());

    for (int i = 0; i < depends.getLength(); ++i) {
      Node node = depends.item(i);

      String ref = XMLUtil.getAttributeString(node, ADDIN_DEPENDS_REF, null);
      if (ref == null || !PATTERN_ID.matcher(ref).matches()) throw new AddinFormatException("Invalid ref found in addin");

      String minVersionStr = XMLUtil.getAttributeString(node, ADDIN_DEPENDS_MIN_VERSION, null);
      String maxVersionStr = XMLUtil.getAttributeString(node, ADDIN_DEPENDS_MAX_VERSION, null);

      VersionSpec minVersion = minVersionStr == null ? null : decodeVersion(minVersionStr, "min-version");
      VersionSpec maxVersion = maxVersionStr == null ? null : decodeVersion(maxVersionStr, "max-version");

      dependencies.add(new AddinDependency(ref, minVersion, maxVersion));
    }

    Object levelNode = XPATH_ADDIN_LEVEL.evaluate(document, XPathConstants.NODE);

    if (levelNode == null && type == Addin.TYPE_LEVEL) throw new AddinFormatException("Level addin doesn't have a level description in manifest");
    if (levelNode != null && type != Addin.TYPE_LEVEL) throw new AddinFormatException("Non-level addin can't have a level description in manifest");

    String levelDir = null;
    String levelOcd = null;
    Map<String, String> levelNames = null;
    Map<String, String> levelSubtitles = null;

    if (levelNode != null) {
      levelDir = getStringRequired(document, XPATH_ADDIN_LEVEL_DIR, "level dir"); // TODO validate
      levelOcd = getString(document, XPATH_ADDIN_LEVEL_OCD); // TODO validate

      Node nameNode = (Node) XPATH_ADDIN_LEVEL_NAME.evaluate(document, XPathConstants.NODE);
      if (nameNode == null) throw new AddinFormatException("Missing level name");

      NamedNodeMap map = nameNode.getAttributes();
      levelNames = new TreeMap<String, String>();

      for (int i = 0; i < map.getLength(); i++) {
        Node node = map.item(i);
        levelNames.put(node.getNodeName(), node.getNodeValue());
      }
      if (levelNames.get("text") == null) throw new AddinFormatException("No text attribute on level name");

      Node subtitleNode = (Node) XPATH_ADDIN_LEVEL_SUBTITLE.evaluate(document, XPathConstants.NODE);
      if (subtitleNode == null) throw new AddinFormatException("Missing level subtitle");

      levelSubtitles = new TreeMap<String, String>();
      map = subtitleNode.getAttributes();
      for (int i = 0; i < map.getLength(); i++) {
        Node node = map.item(i);
        levelSubtitles.put(node.getNodeName(), node.getNodeValue());
//        System.out.println("node.getNodeName() = " + node.getNodeName());
//        System.out.println("node.getNodeValue() = " + node.getNodeValue());
      }
      if (levelSubtitles.get("text") == null) throw new AddinFormatException("No text attribute on level subtitle");
    }

    return new Addin(addinDiskFile, id, name, type, manifestVersion, version, description, author, dependencies, levelDir, levelNames, levelSubtitles, levelOcd);
  }

  /*
   * Reads the manifest for spec-version 1.1, first supported in GooTool 1.0.0
    * Version 1.1 is the same as 1.0, with the following additions:
   * - Thumbnails
   * - TODO .movie.xml and .anim.xml files from compile\ are compiled (by AddinInstaller)
   * - TODO maybe choose the chapter?
   * - TODO maybe influence the position?
   * - TODO maybe required previous levels?
   * - TODO triggers on level end for movies
   * - TODO text.xml automation
   *
   * @param document        the DOM document of the manifest file.
   * @param manifestVersion The spec-version of the manifest.
   * @param addinDiskFile   The addin file or directory on disk.
   * @return The constructed addin.
   * @throws AddinFormatException if the addin was somehow invalid.
   * @throws javax.xml.xpath.XPathExpressionException
   *                              if the manifest was unparseable (really, should be AddinFormatException).
   */
  private static Addin readManifestVersion1_1(Document document, VersionSpec manifestVersion, File addinDiskFile, AddinReader addinReader) throws XPathExpressionException, AddinFormatException, IOException
  {
    Addin addin = readManifestVersion1_0(document, manifestVersion, addinDiskFile);

    readThumbnail(document, addinReader, addin);

    return addin;
  }

  private static void readThumbnail(Document document, AddinReader addinReader, Addin addin)
          throws XPathExpressionException, IOException, AddinFormatException
  {
    Node thumbnailNode = getNode(document, XPATH_ADDIN_THUMBNAIL);

    if (thumbnailNode != null) {
      int expectedWidth = XMLUtil.getAttributeIntegerRequired(thumbnailNode, "width");
      int expectedHeight = XMLUtil.getAttributeIntegerRequired(thumbnailNode, "height");
      String expectedType = XMLUtil.getAttributeStringRequired(thumbnailNode, "type");
      String fileName = thumbnailNode.getTextContent().trim();

      InputStream is = addinReader.getInputStream(fileName);
      BufferedImage thumbnailImage;
      try {
        Iterator<ImageReader> readerIterator = ImageIO.getImageReadersByMIMEType(expectedType);
        if (!readerIterator.hasNext()) {
          throw new AddinFormatException("Unable to read image of type " + expectedType);
        }
        ImageReader reader = readerIterator.next();

        ImageInputStream iis = ImageIO.createImageInputStream(is);
        try {
          reader.setInput(iis, true, true);
          thumbnailImage = reader.read(0);
        }
        catch (IOException e) {
          throw new AddinFormatException("Couldn't load the thumbnail file " + fileName, e);
        }
        finally {
          reader.dispose();
        }
      }
      finally {
        is.close();
      }

      if (thumbnailImage.getWidth(null) != expectedWidth)
        throw new AddinFormatException("Thumbnail width should be " + expectedWidth);
      if (thumbnailImage.getHeight(null) != expectedHeight)
        throw new AddinFormatException("Thumbnail height should be " + expectedHeight);

      addin.setThumbnail(thumbnailImage);
    }
  }

  private static VersionSpec decodeVersion(String minVersionStr, String errField) throws AddinFormatException
  {
    VersionSpec minVersion;
    try {
      minVersion = new VersionSpec(minVersionStr);
    }
    catch (NumberFormatException e) {
      throw new AddinFormatException("Invalid " + errField + " string " + minVersionStr);
    }
    return minVersion;
  }

  private static String getStringRequiredValidated(Document document, XPathExpression expression, Pattern pattern, String errField)
          throws XPathExpressionException, AddinFormatException
  {
    String value = getStringRequired(document, expression, errField);
    if (!pattern.matcher(value).matches()) throw new AddinFormatException("Invalid " + errField + " found in addin: " + value);

    return value;
  }

  private static String getStringRequired(Document document, XPathExpression expression, String errField)
          throws XPathExpressionException, AddinFormatException
  {
    String value = getString(document, expression);
    if (value.length() == 0) throw new AddinFormatException("No " + errField + " found in addin");
    return value;
  }

  /**
   * Gets the node's value as an integer. Returns null if node not found or not a number.
   */
//  private static Integer getNodeIntegerValue(Document document, XPathExpression expression) throws XPathExpressionException
//  {
//    Object node = expression.evaluate(document, XPathConstants.NUMBER);
//
//    if (!(node instanceof Double) || ((Double) node).isNaN()) {
//      return null;
//    }
//
//    return ((Double) node).intValue();
//  }

  /**
   * Gets the node's value as a string. Never returns null. Empty string if node not found.
   */
  private static String getString(Document document, XPathExpression expression) throws XPathExpressionException
  {
    String s = expression.evaluate(document);
    if (s == null) return "";

    return s.trim();
  }

  /**
   * Gets the node, or null if node not found.
   */
  private static Node getNode(Document document, XPathExpression expression) throws XPathExpressionException
  {
    return (Node) expression.evaluate(document, XPathConstants.NODE);
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException, AddinFormatException
  {
//    String file = "addins/src/com.goofans.davidc.jingleballs/addin.xml";
//    Addin addin = readManifest(new FileInputStream(file), null);
//    System.out.println("addin = " + addin);

    Addin addin = loadAddin(new File("addins/dist/com.goofans.davidc.jingleballs_1.3.goomod"));
    System.out.println("addin = " + addin);
    System.out.println("addin.getThumbnail() = " + addin.getThumbnail());
  }
}
