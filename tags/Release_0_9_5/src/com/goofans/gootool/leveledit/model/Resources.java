package com.goofans.gootool.leveledit.model;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.HashMap;
import java.awt.*;

import com.goofans.gootool.io.BinFormat;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.wog.WorldOfGoo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Resources
{
  private Map<String, Image> images = new HashMap<String, Image>();
  private Map<String, File> sounds = new HashMap<String, File>();

  public Resources(Document d, File rootDir) throws IOException
  {
    Element resourcesEl = (Element) d.getDocumentElement().getElementsByTagName("Resources").item(0);

    File defaultPath = rootDir;
    String defaultIdPrefix = "";

    for (int i = 0; i < resourcesEl.getChildNodes().getLength(); i++) {
      Node node = resourcesEl.getChildNodes().item(i);
      if (node instanceof Element) {
        Element el = (Element) node;

        if (el.getNodeName().equals("SetDefaults")) {
          defaultPath = new File(rootDir, el.getAttribute("path"));
          defaultIdPrefix = el.getAttribute("idprefix");
        }
        else if (el.getNodeName().equals("Image")) {
          String id = defaultIdPrefix + el.getAttribute("id");
          File f = new File(defaultPath, el.getAttribute("path") + ".png");

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

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    WorldOfGoo.init();
    File f = new File(WorldOfGoo.getWogDir(), "res/levels/AB3/AB3.resrc.bin");

    String xml = BinFormat.decodeFile(f);
    Document doc = XMLUtil.loadDocumentFromReader(new StringReader(xml));

    Resources res = new Resources(doc, WorldOfGoo.getWogDir());

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
