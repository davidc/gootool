package com.goofans.datamining;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.ByteArrayInputStream;

import com.goofans.gootool.wog.WorldOfGoo;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.io.GameFormat;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Levels
{
  private static XPath xpath = XPathFactory.newInstance().newXPath();

  public static void main(String[] args) throws IOException, XPathExpressionException
  {
    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    worldOfGoo.init();

    Document textDoc = XMLUtil.loadDocumentFromInputStream(new ByteArrayInputStream(GameFormat.decodeBinFile(worldOfGoo.getGameFile("properties/text.xml.bin"))));

    for (int island = 1; island <= 5; ++island) {
      Document islandDoc = XMLUtil.loadDocumentFromInputStream(new ByteArrayInputStream(GameFormat.decodeBinFile(worldOfGoo.getGameFile("res/islands/island" + island + ".xml.bin"))));
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
