/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.wog.WorldOfGoo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Resources
{
  private final Map<String, Image> images = new HashMap<String, Image>();
  private final Map<String, File> sounds = new HashMap<String, File>();

  public Resources(Document d) throws IOException
  {
    NodeList resourcesNodes = d.getDocumentElement().getElementsByTagName("Resources");

    for (int i = 0; i < resourcesNodes.getLength(); i++) {
      Node resourcesEl = resourcesNodes.item(i);

      File rootDir = WorldOfGoo.getTheInstance().getCustomGameFile("");

      File defaultPath = rootDir;
      String defaultIdPrefix = "";

      for (int j = 0; j < resourcesEl.getChildNodes().getLength(); j++) {
        Node node = resourcesEl.getChildNodes().item(j);
        if (node instanceof Element) {
          Element el = (Element) node;

          if ("SetDefaults".equals(el.getNodeName())) {
            defaultPath = new File(rootDir, el.getAttribute("path"));
            defaultIdPrefix = el.getAttribute("idprefix");
          }
          else if ("Image".equals(el.getNodeName())) {
            String id = defaultIdPrefix + el.getAttribute("id");
            File f = new File(defaultPath, el.getAttribute("path") + ".png");

            // HACK: fix Fish
            if ("IMAGE_BALL_FISH_WINGLEFT".equals(id)) id = "IMAGE_BALL_TIMEBUG_WINGLEFT";
            if ("IMAGE_BALL_FISH_WINGRIGHT".equals(id)) id = "IMAGE_BALL_TIMEBUG_WINGRIGHT";

            try {
              images.put(id, ImageIO.read(f));
            }
            catch (IOException e) {
              throw new IOException("Can't read " + f.getPath() + ": " + e.getMessage());
            }
          }
          else if ("Sound".equals(el.getNodeName())) {
            String id = defaultIdPrefix + el.getAttribute("id");
            File f = new File(defaultPath, el.getAttribute("path") + ".ogg");

//          System.out.println(id + "->" + f.getAbsolutePath());
            sounds.put(id, f);
          }
        }
      }
    }
  }

  public Map<String, Image> getImages()
  {
    return images;
  }

  public Map<String, File> getSounds()
  {
    return sounds;
  }

  public Image getImage(String id) throws IOException
  {
//    return new
//    return Toolkit.getDefaultToolkit().createImage(resources.get(id).getAbsolutePath());
    Image image = images.get(id);
    if (image == null) throw new IOException("Image " + id + " not found");
    return image;
  }


  private static Resources globalResources;

  // TODO don't cache after saving as we may have modified via an addin
  public static synchronized Resources getGlobalResources() throws IOException
  {
    if (globalResources == null) {
      File f = WorldOfGoo.getTheInstance().getCustomGameFile("properties/resources.xml.bin");
      Document doc = GameFormat.decodeXmlBinFile(f);
      globalResources = new Resources(doc);
    }
    return globalResources;
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  public static void main(String[] args) throws IOException
  {
    WorldOfGoo worldOfGoo = WorldOfGoo.getTheInstance();
    worldOfGoo.init();

//    File f = worldOfGoo.getCustomGameFile("res/levels/AB3/AB3.resrc.bin");
//    Document doc = GameFormat.decodeXmlBinFile(f);
//    Resources res = new Resources(doc);

    Resources res = Resources.getGlobalResources();

    System.out.println(">>> IMAGES <<<");

    for (String id : res.getImages().keySet()) {
      System.out.println(id + " -> " + res.getImage(id));
    }

    System.out.println(">>> SOUNDS <<<");

    for (String id : res.getSounds().keySet()) {
      System.out.println(id + " -> " + res.getSounds().get(id));
    }
  }
}
