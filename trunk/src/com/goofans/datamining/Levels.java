/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.datamining;

import com.goofans.gootool.facades.Source;
import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.io.Codec;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "HardcodedFileSeparator", "DuplicateStringLiteralInspection", "StringConcatenation"})
public class Levels
{
  private static final XPath xpath = XPathFactory.newInstance().newXPath();

  private Levels()
  {
  }

  public static void main(String[] args) throws IOException, XPathExpressionException
  {
    Project project = ProjectManager.simpleInit();
    SourceFile sourceRoot = project.getSource().getGameRoot();

    Codec codec = project.getCodecForGameXml();

    Document textDoc = XMLUtil.loadDocumentFromInputStream(new ByteArrayInputStream(codec.decodeFile(sourceRoot.getChild(project.getGameXmlFilename("properties/text.xml")))));

    for (int island = 1; island <= 5; ++island) {
      Document islandDoc = XMLUtil.loadDocumentFromInputStream(new ByteArrayInputStream(codec.decodeFile(sourceRoot.getChild(project.getGameXmlFilename("res/islands/island" + island + ".xml")))));
      NodeList levelList = islandDoc.getElementsByTagName("level");
      for (int i = 0; i < levelList.getLength(); i++) {
        Element levelNode = (Element) levelList.item(i);
        String levelId = levelNode.getAttribute("id");
        String levelName = getText(textDoc, levelNode.getAttribute("name"));
        String levelSubtitle = getText(textDoc, levelNode.getAttribute("text"));
        String levelOcd = levelNode.getAttribute("ocd");
        String levelDepends = levelNode.getAttribute("depends");

        System.out.print("INSERT INTO goofans_wog_levels (dirname, official, ocd, depends, name_en, subtitle_en, island) VALUES ");
        System.out.println("('" + levelId + "', 1, '" + levelOcd + "', '" + levelDepends + "', '" + levelName + "', '" + levelSubtitle + "', " + island + ");");
      }
    }
  }

  private static String getText(Document textDoc, String key) throws XPathExpressionException
  {
    XPathExpression exptr = xpath.compile("/strings/string[@id='" + key + "']/@text");
    return exptr.evaluate(textDoc).replaceAll("\\|", " ").replaceAll("'", "\\\\'");
  }
}
