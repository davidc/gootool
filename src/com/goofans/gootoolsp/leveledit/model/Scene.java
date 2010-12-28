/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.model;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Scene
{
  private final double minX, minY, maxX, maxY;
//  private Color backgroundColor;

  private final List<SceneObject> sceneObjects = new LinkedList<SceneObject>();

  public Scene(Document d) throws IOException
  {
    Element rootElement = d.getDocumentElement();

    if (!"scene".equals(rootElement.getNodeName())) { //NON-NLS
      throw new IOException("Root element isn't a scene");
    }

    minX = XMLUtil.getAttributeDouble(rootElement, "minx", null);
    minY = XMLUtil.getAttributeDouble(rootElement, "miny", null);
    maxX = XMLUtil.getAttributeDouble(rootElement, "maxx", null);
    maxY = XMLUtil.getAttributeDouble(rootElement, "maxy", null);

//    backgroundColor = parseColor(XMLUtil.getAttributeString(rootElement, "backgroundcolor"); // unused! always black

    NodeList childNodes = rootElement.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (node instanceof Element) {
        Element el = (Element) node;
        String elName = el.getNodeName().toLowerCase();
//        System.out.println("elName = " + elName);
        SceneObject sceneObject = new SceneObject(el);
//        System.out.println("sceneObject = " + sceneObject);
        sceneObjects.add(sceneObject);
      }

    }
  }

  public double getMinX()
  {
    return minX;
  }

  public double getMinY()
  {
    return minY;
  }

  public double getMaxX()
  {
    return maxX;
  }

  public double getMaxY()
  {
    return maxY;
  }

  public List<SceneObject> getSceneObjects()
  {
    return sceneObjects;
  }

  //  private Color parseColor(String colorStr)
//  {
//    if (colorStr == null) return null;
//  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    File f = new File("addins\\src\\net.davidc.madscientist.dejavu\\compile\\res\\levels\\MSDejaVu\\MSDejaVu.scene.xml");

    Document d = XMLUtil.loadDocumentFromFile(f);

    Scene s = new Scene(d);

    System.out.println("s = " + s);
  }
}
