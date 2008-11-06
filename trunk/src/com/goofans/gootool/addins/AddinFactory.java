package com.goofans.gootool.addins;

import com.goofans.gootool.util.VersionSpec;
import com.goofans.gootool.util.XMLUtil;

import javax.xml.xpath.*;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.List;
import java.util.ArrayList;
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
{  private static final Logger log = Logger.getLogger(AddinFactory.class.getName());

  private static final XPathExpression XPATH_ADDIN_SPECVERSION;
  private static final XPathExpression XPATH_ADDIN_ID;
  private static final XPathExpression XPATH_ADDIN_NAME;
  private static final XPathExpression XPATH_ADDIN_TYPE;
  private static final XPathExpression XPATH_ADDIN_VERSION;
  private static final XPathExpression XPATH_ADDIN_DESCRIPTION;
  private static final XPathExpression XPATH_ADDIN_AUTHOR;
  private static final XPathExpression XPATH_ADDIN_DEPENDENCIES;

  private static final Pattern PATTERN_ID = Pattern.compile("^[-\\p{Alnum}]+(\\.[-\\p{Alnum}]+)+$"); // require at least 1 domain component
  private static final Pattern PATTERN_NAME = Pattern.compile("^\\p{Alnum}[\\p{Graph} ]+$");

  private static final String ADDIN_DEPENDS_REF = "ref";
  private static final String ADDIN_DEPENDS_MIN_VERSION = "min-version";
  private static final String ADDIN_DEPENDS_MAX_VERSION = "max-version";

  private static final VersionSpec SPEC_VERSION_1_0 = new VersionSpec("1.0");

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
    ZipFile zipFile = new ZipFile(file);

    try {
      ZipEntry manifestEntry = zipFile.getEntry(GOOMOD_MANIFEST);

      if (manifestEntry == null) {
        throw new AddinFormatException("No manifest found, is this an addin?");
      }

      InputStream is = zipFile.getInputStream(manifestEntry);
      try {
        return readManifest(is, file);
      }
      finally {
        is.close();
      }
    }
    finally {
      zipFile.close();
    }
  }

  // Synchronized since xpath expressions and patterns aren't thread safe
  private static synchronized Addin readManifest(InputStream is, File addinDiskFile) throws IOException, AddinFormatException
  {
    Document document = XMLUtil.loadDocumentFromReader(new InputStreamReader(is));

    try {
      String specVersionStr = getString(document, XPATH_ADDIN_SPECVERSION);
      if (specVersionStr.length() == 0) throw new AddinFormatException("No spec-version found");
      VersionSpec specVersion = new VersionSpec(specVersionStr);

      if (specVersion.equals(SPEC_VERSION_1_0)) {
        return readManifestVersion1_0(document, addinDiskFile);
      }
      else if (specVersion.compareTo(SPEC_VERSION_1_0) < 0) {
        throw new AddinFormatException("This addin uses outdated spec-version " + specVersion + ". Please upgrade this addin.");
      }
      else {
        throw new AddinFormatException("This addin uses unsupported spec-version " + specVersion + ". Please upgrade GooTool.");
      }
    }
    catch (XPathExpressionException e) {
      throw new AddinFormatException("Unable to parse XPath: " + e.getLocalizedMessage(), e);
    }
  }


  private static Addin readManifestVersion1_0(Document document, File addinDiskFile) throws XPathExpressionException, AddinFormatException
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

      String ref = getAttributeValue(node, ADDIN_DEPENDS_REF);
      if (!PATTERN_ID.matcher(ref).matches()) throw new AddinFormatException("Invalid ref found in addin");

      String minVersionStr = getAttributeValue(node, ADDIN_DEPENDS_MIN_VERSION);
      String maxVersionStr = getAttributeValue(node, ADDIN_DEPENDS_MAX_VERSION);

      VersionSpec minVersion = minVersionStr.length() > 0 ? decodeVersion(minVersionStr, "min-version") : null;
      VersionSpec maxVersion = maxVersionStr.length() > 0 ? decodeVersion(maxVersionStr, "max-version") : null;

      dependencies.add(new AddinDependency(ref, minVersion, maxVersion));
    }

    return new Addin(addinDiskFile, id, name, type, version, description, author, dependencies);
  }

  private static VersionSpec decodeVersion(String minVersionStr, String errStr)
          throws AddinFormatException
  {
    VersionSpec minVersion;
    try {
      minVersion = new VersionSpec(minVersionStr);
    }
    catch (NumberFormatException e) {
      throw new AddinFormatException("Invalid " + errStr + " string " + minVersionStr);
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
  private static Integer getNodeIntegerValue(Document document, XPathExpression expression) throws XPathExpressionException
  {
    Object node = expression.evaluate(document, XPathConstants.NUMBER);

    if (!(node instanceof Double) || ((Double) node).isNaN()) {
//      throw new AddinFormatException("Node " + expression + " is not a numeric value.");
      return null;
    }

    return ((Double) node).intValue();
  }

  /**
   * Gets the node's value as a string. Never returns null. Empty string if node not found.
   */
  private static String getString(Document document, XPathExpression expression) throws XPathExpressionException
  {
    String s = expression.evaluate(document);
    if (s == null) return "";

    return s.trim();
  }

  private static String getAttributeValue(Node node, String attributeName)
  {
    NamedNodeMap attributes = node.getAttributes();
    if (attributes == null) return "";

    Node attribute = attributes.getNamedItem(attributeName);
    if (attribute == null) return "";

    return attribute.getNodeValue().trim();
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException, AddinFormatException
  {
    String file = "addins/src/net.davidc.test.merger/addin.xml";
    Addin addin = readManifest(new FileInputStream(file), null);

    System.out.println("addin = " + addin);
  }
}
