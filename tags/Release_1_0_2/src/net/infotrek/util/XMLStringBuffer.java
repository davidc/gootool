package net.infotrek.util;

import java.util.*;

/**
 * This class allows you to generate an XML text document by pushing and popping tags from a stack maintained
 * internally.
 * <p/>
 * NB Currently more recent than ec svn.
 */
public class XMLStringBuffer
{
  private static final String INDENT_INCREMENT = "  ";

  private StringBuffer buffer;
  private final Stack<Tag> stack = new Stack<Tag>();
  private String currentIndent = "";

  public XMLStringBuffer()
  {
    init(new StringBuffer(), "");
  }

  /**
   * @param start A string of spaces indicating the indentation at which to start the generation.
   */
  public XMLStringBuffer(String start)
  {
    init(new StringBuffer(), start);
  }

  /**
   * Set the doctype for this document.
   *
   * @param docType The DOCTYPE, without the "!DOCTYPE " string
   */
  public void setDocType(String docType)
  {
    buffer.append("<!DOCTYPE ").append(docType).append(">\n");
  }

  /**
   * @param buffer The StringBuffer to use internally to represent the document.
   * @param start  A string of spaces indicating the indentation at which to start the generation.
   */
  public XMLStringBuffer(StringBuffer buffer, String start)
  {
    init(buffer, start);
  }

  private void init(StringBuffer buffer, String start)
  {
    this.buffer = buffer;
    currentIndent = start;
  }

  /**
   * Push a new tag.  Its value is stored and will be compared against the parameter passed to pop().
   *
   * @param tagName    The name of the tag.
   * @param schema     The schema to use (can be null or an empty string).
   * @param attributes A Map representing the attributes (or null)
   */
  public void push(String tagName, String schema, Map<String, String> attributes)
  {
    xmlOpen(buffer, currentIndent, tagName + schema, attributes, false, false);
    stack.push(new Tag(currentIndent, tagName));
    currentIndent += INDENT_INCREMENT;
  }

  /**
   * Push a new tag.  Its value is stored and will be compared against the parameter passed to pop().
   *
   * @param tagName    The name of the tag.
   * @param attributes A Map representing the attributes (or null)
   */
  public void push(String tagName, Map<String, String> attributes)
  {
    xmlOpen(buffer, currentIndent, tagName, attributes, false, false);
    stack.push(new Tag(currentIndent, tagName));
    currentIndent += INDENT_INCREMENT;
  }

  /**
   * Push a new tag.  Its value is stored and will be compared against the parameter passed to pop().
   *
   * @param tagName The name of the tag.
   * @param schema  The schema to use (can be null or an empty string).
   */
  public void push(String tagName, String schema)
  {
    push(tagName, schema, null);
  }

  /**
   * Push a new tag.  Its value is stored and will be compared against the parameter passed to pop().
   *
   * @param tagName The name of the tag.
   */
  public void push(String tagName)
  {
    push(tagName, "");
  }

  /**
   * Pop the last pushed element without verifying it if matches the previously pushed tag.
   */
  public void pop()
  {
    pop(null);
  }

  /**
   * Pop the last pushed element and throws an AssertionError if it doesn't match the corresponding tag that was pushed
   * earlier.
   *
   * @param tagName The name of the tag this pop() is supposed to match.
   * @throws AssertionError if the tag being popped is not at the top of the stack.
   */
  public void pop(String tagName) throws AssertionError
  {
    Tag t = stack.pop();
    if (null != tagName) {
      if (!tagName.equals(t.tagName)) {
        throw new AssertionError("Popping the wrong tag: " + t.tagName + " but expected " + tagName);
      }
    }
    currentIndent = currentIndent.substring(0, currentIndent.length() - INDENT_INCREMENT.length());
    xmlClose(buffer, currentIndent, t.tagName);
  }

  /**
   * Add a required element to the current tag.  An opening and closing tag will be generated even if value is null.
   *
   * @param tagName The name of the tag
   * @param value   The value for this tag
   */
  public void addRequired(String tagName, String value)
  {
    addRequired(tagName, value, null);
  }

  /**
   * Add a required element to the current tag.  An opening and closing tag will be generated even if value is null.
   *
   * @param tagName    The name of the tag
   * @param value      The value for this tag
   * @param attributes A Map containing the attributes (or null)
   */
  public void addRequired(String tagName, String value, Map<String, String> attributes)
  {
    xmlRequired(buffer, currentIndent, tagName, value, attributes);
  }

  /**
   * Add an optional String element to the current tag.  If value is null, nothing is added.
   *
   * @param tagName    The name of the tag
   * @param value      The value for this tag
   * @param attributes A Map containing the attributes (or null)
   */
  public void addOptional(String tagName, String value, Map<String, String> attributes)
  {
    xmlOptional(buffer, currentIndent, tagName, value, attributes);
  }

  /**
   * Add an optional String element to the current tag.  If value is null, nothing is added.
   *
   * @param tagName The name of the tag
   * @param value   The value for this tag
   */
  public void addOptional(String tagName, String value)
  {
    addOptional(tagName, value, null);
  }

  /**
   * Add an optional Boolean element to the current tag.  If value is null, nothing is added.
   *
   * @param tagName    The name of the tag
   * @param value      The value for this tag
   * @param attributes A Map containing the attributes (or null)
   */
  public void addOptional(String tagName, Boolean value, Map<String, String> attributes)
  {
    if (null != value) {
      xmlOptional(buffer, currentIndent, tagName, value.toString(), attributes);
    }
  }

  /**
   * Add an optional Boolean element to the current tag.  If value is null, nothing is added.
   *
   * @param tagName The name of the tag
   * @param value   The value for this tag
   */
  public void addOptional(String tagName, Boolean value)
  {
    addOptional(tagName, value, null);
  }

  /**
   * Add an empty element tag (e.g. <foo/>)
   *
   * @param tagName the tag name
   */
  public void addEmptyElement(String tagName)
  {
    buffer.append(currentIndent).append("<").append(tagName).append("/>\n");
  }

  /**
   * Add an empty element tag with properties (e.g. <foo x=y/>)
   *
   * @param tagName    the tag name
   * @param attributes the attributes to add to this tag
   */
  public void addEmptyElement(String tagName, Map<String, String> attributes)
  {
    addRequired(tagName, null, attributes);
  }

  /**
   * Add a CDATA tag.
   *
   * @param content the cdata content to add.
   */
  public void addCDATA(String content)
  {
    buffer.append(currentIndent).append("<![CDATA[").append(content).append("]]>\n");
  }

  /**
   * Add a comment.
   *
   * @param comment the comment to add
   */
  public void addComment(String comment)
  {
    buffer.append(currentIndent).append("<!-- ");
    buffer.append(comment.replaceAll("--", "- -"));
    buffer.append(" -->\n");
  }

  /**
   * @return The StringBuffer used to create the document.
   */
  public StringBuffer getStringBuffer()
  {
    return buffer;
  }

  /**
   * @return The String representation of the XML for this XMLStringBuffer.
   */
  public String toXML()
  {
    if (!stack.isEmpty()) {
      throw new AssertionError("Requested XML output when tag stack was not empty.");
    }
    return buffer.toString();
  }

  private static String xml(String indent, String elementName, String content, Map<String, String> attributes)
  {
    StringBuffer result = new StringBuffer();

    if (content == null) {
      xmlOpen(result, indent, elementName, attributes, false, true);
    }
    else {
      xmlOpen(result, indent, elementName, attributes, true /* no newline */, false);
      result.append(encodeXml(content));
      xmlClose(result, "", elementName);
    }

    return result.toString();
  }

  private static void xmlOptional(StringBuffer result, String sp,
                                  String elementName, String value, Map<String, String> attributes)
  {
    if (null != value) {
      xmlRequired(result, sp, elementName, value, attributes);
    }
  }

  private static void xmlRequired(StringBuffer result, String sp,
                                  String elementName, String value, Map<String, String> attributes)
  {
    result.append(xml(sp, elementName, value, attributes));
  }

  private static void xmlOpen(StringBuffer result, String indent,
                              String tag, Map<String, String> attributes,
                              boolean noNewLine, boolean alsoClose)
  {
    result.append(indent).append("<").append(tag);

    if (null != attributes) {
      for (Map.Entry entry : attributes.entrySet()) {
        if (entry.getKey() != null && entry.getValue() != null)
          result.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
      }
    }

    if (alsoClose) result.append("/");
    result.append(">");
    if (!noNewLine) result.append("\n");
  }

  private static void xmlClose(StringBuffer result, String indent,
                               String tag)
  {
    result.append(indent).append("</").append(tag).append(">\n");
  }

  private class Tag
  {
    public String tagName;
    public String indent;

    public Tag(String ind, String n)
    {
      tagName = n;
      indent = ind;
    }
  }

  private static String encodeXml(String in)
  {
    StringBuilder out = new StringBuilder();

    for (int i = 0; i < in.length(); ++i) {
      char ch = in.charAt(i);
      switch (ch) {
        case '<':
          out.append("&lt;");
          break;
        case '>':
          out.append("&gt;");
          break;
        case '&':
          out.append("&amp;");
          break;
        default:
          out.append(ch);
          break;
      }
    }

    return out.toString();
  }
}
