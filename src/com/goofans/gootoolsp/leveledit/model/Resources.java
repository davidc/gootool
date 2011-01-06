/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.model;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.goofans.gootool.facades.Source;
import com.goofans.gootool.facades.SourceFile;
import com.goofans.gootool.facades.Target;
import com.goofans.gootool.facades.TargetFile;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectManager;
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
  private final Map<String, SourceFile> sounds = new HashMap<String, SourceFile>();

  public Resources(Document d) throws IOException
  {
    NodeList resourcesNodes = d.getDocumentElement().getElementsByTagName("Resources");

    Project project = ProjectManager.simpleInit();
    Source source = project.getSource();
    try {

      for (int i = 0; i < resourcesNodes.getLength(); i++) {
        Node resourcesEl = resourcesNodes.item(i);

        SourceFile rootDir = source.getGameRoot();

        SourceFile defaultPath = rootDir;
        String defaultIdPrefix = "";

        for (int j = 0; j < resourcesEl.getChildNodes().getLength(); j++) {
          Node node = resourcesEl.getChildNodes().item(j);
          if (node instanceof Element) {
            Element el = (Element) node;

            if ("SetDefaults".equals(el.getNodeName())) { //NON-NLS
              defaultPath = rootDir.getChild(el.getAttribute("path")); //NON-NLS
              defaultIdPrefix = el.getAttribute("idprefix"); //NON-NLS
            }
            else if ("Image".equals(el.getNodeName())) { //NON-NLS
              String id = defaultIdPrefix + el.getAttribute("id"); //NON-NLS
              SourceFile f = defaultPath.getChild(project.getGamePngFilename(el.getAttribute("path"))); //NON-NLS

              // HACK: fix Fish
              if ("IMAGE_BALL_FISH_WINGLEFT".equals(id)) id = "IMAGE_BALL_TIMEBUG_WINGLEFT"; //NON-NLS
              if ("IMAGE_BALL_FISH_WINGRIGHT".equals(id)) id = "IMAGE_BALL_TIMEBUG_WINGRIGHT"; //NON-NLS

              try {
                images.put(id, ImageIO.read(f.read()));
              }
              catch (IOException e) {
                throw new IOException("Can't read " + f + ": " + e.getMessage());
              }
            }
            else if ("Sound".equals(el.getNodeName())) { //NON-NLS
              String id = defaultIdPrefix + el.getAttribute("id"); //NON-NLS
              SourceFile f = defaultPath.getChild(project.getGameSoundFilename(el.getAttribute("path"))); //NON-NLS
              if (!f.isFile()) {
                f = defaultPath.getChild(project.getGameMusicFilename(el.getAttribute("path"))); //NON-NLS
              }

//          System.out.println(id + "->" + f.getAbsolutePath());
              sounds.put(id, f);
            }
          }
        }
      }
    }
    finally {
      source.close();
    }
  }

  public Map<String, Image> getImages()
  {
    return images;
  }

  public Map<String, SourceFile> getSounds()
  {
    return sounds;
  }

  public Image getImage(String id) throws IOException
  {
//    return new
//    return Toolkit.getDefaultToolkit().createImage(resources.get(id).getAbsolutePath());
    Image image = images.get(id);
    if (image == null) throw new IOException("Image " + id + " not found"); //NON-NLS
    return image;
  }


  private static Resources globalResources = null;

  // TODO don't cache after saving as we may have modified via an addin
  public static synchronized Resources getGlobalResources() throws IOException
  {
    if (globalResources == null) {
      Project project = ProjectManager.simpleInit();
      Target target = project.getTarget();
      try {
        TargetFile f = target.getGameRoot().getChild(project.getGameXmlFilename("properties/resources.xml")); //NON-NLS
        Document doc = project.getCodecForGameXml().decodeFileToXML(f);
        globalResources = new Resources(doc);
      }
      finally {
        target.close();
      }
    }
    return globalResources;
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  public static void main(String[] args) throws IOException
  {
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
