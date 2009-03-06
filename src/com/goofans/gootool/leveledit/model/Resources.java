package com.goofans.gootool.leveledit.model;

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
  private Map<String, Image> images = new HashMap<String, Image>();
  private Map<String, File> sounds = new HashMap<String, File>();

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

          if (el.getNodeName().equals("SetDefaults")) {
            defaultPath = new File(rootDir, el.getAttribute("path"));
            defaultIdPrefix = el.getAttribute("idprefix");
          }
          else if (el.getNodeName().equals("Image")) {
            String id = defaultIdPrefix + el.getAttribute("id");
            File f = new File(defaultPath, el.getAttribute("path") + ".png");

            // HACK: fix Fish
            if (id.equals("IMAGE_BALL_FISH_WINGLEFT")) id = "IMAGE_BALL_TIMEBUG_WINGLEFT";
            if (id.equals("IMAGE_BALL_FISH_WINGRIGHT")) id = "IMAGE_BALL_TIMEBUG_WINGRIGHT";

            images.put(id, ImageIO.read(f));
          }
          else if (el.getNodeName().equals("Sound")) {
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
    return images.get(id);
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

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
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
