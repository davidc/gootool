/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.datamining.balls;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.util.*;

import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.projects.ProjectManager;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LevelBalls
{
  private LevelBalls()
  {
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "HardcodedFileSeparator", "StringConcatenation", "DuplicateStringLiteralInspection"})
  public static void main(String[] args) throws Exception
  {
    Map<String, Set<String>> ballUsages = new TreeMap<String, Set<String>>();

//    System.out.println("\n==== BY LEVEL ====\n");

    SourceFile levelsDir = ProjectManager.simpleInit().getSource().getRoot().getChild("res/levels/");

    for (SourceFile file : levelsDir.list()) {
      if (file.isDirectory()) {
        String levelName = file.getName();

//        System.out.println("Level " + levelName);

        byte[] xmlBytes = GameFormat.decodeBinFile(file.getChild(levelName + ".level.bin"));
        Document doc = XMLUtil.loadDocumentFromInputStream(new ByteArrayInputStream(xmlBytes));

        XPath xPath = XPathFactory.newInstance().newXPath();


        Set<String> attachedIds = new HashSet<String>();

        NodeList strands = (NodeList) xPath.evaluate("/level/Strand", doc, XPathConstants.NODESET);

        for (int i = 0; i < strands.getLength(); i++) {
          Node node = strands.item(i);
          String gb1 = node.getAttributes().getNamedItem("gb1").getTextContent();
          String gb2 = node.getAttributes().getNamedItem("gb2").getTextContent();

          attachedIds.add(gb1);
          attachedIds.add(gb2);
        }

        NodeList ballInstances = (NodeList) xPath.evaluate("/level/BallInstance", doc, XPathConstants.NODESET);

        Map<String, Integer> ballTypesTotal = new TreeMap<String, Integer>();
        Map<String, Integer> ballTypesSleeping = new TreeMap<String, Integer>();
        Map<String, Integer> ballTypesAttached = new TreeMap<String, Integer>();

        for (int i = 0; i < ballInstances.getLength(); i++) {
          Node node = ballInstances.item(i);
          String type = node.getAttributes().getNamedItem("type").getTextContent();
          Node discoveredAttr = node.getAttributes().getNamedItem("discovered");

          boolean sleeping = discoveredAttr != null && ("false".equalsIgnoreCase(discoveredAttr.getTextContent()));

          String id = node.getAttributes().getNamedItem("id").getTextContent();

          boolean attached = (attachedIds.contains(id));

          if (ballTypesTotal.containsKey(type)) {
            ballTypesTotal.put(type, ballTypesTotal.get(type) + 1);
            if (sleeping) ballTypesSleeping.put(type, ballTypesSleeping.get(type) + 1);
            if (attached) ballTypesAttached.put(type, ballTypesAttached.get(type) + 1);
          }
          else {
            ballTypesTotal.put(type, 1);
            ballTypesSleeping.put(type, 1);
            ballTypesAttached.put(type, 0);
          }
        }

        for (String type : ballTypesTotal.keySet()) {
//          System.out.println(type + ": " + ballTypesTotal.get(type) + " total (" + ballTypesSleeping.get(type) + " sleeping, " + ballTypesAttached.get(type) + " attached)");
          System.out.println("level," + levelName + "," + type + "," + ballTypesTotal.get(type) + "," + ballTypesSleeping.get(type) + "," + ballTypesAttached.get(type));

          if (ballUsages.containsKey(type)) {
            ballUsages.get(type).add(levelName);
          }
          else {
            TreeSet<String> set = new TreeSet<String>();
            set.add(levelName);
            ballUsages.put(type, set);
          }
        }

//        System.out.println("");
      }
    }

//    System.out.println("\n==== BY BALL TYPE ====\n");
    for (String type : ballUsages.keySet()) {
//      System.out.println(type);
      for (String levelName : ballUsages.get(type)) {
//        System.out.println("Used by " + levelName);
        System.out.println("ball," + type + "," + levelName);
      }
//      System.out.println("");
    }
  }
}
