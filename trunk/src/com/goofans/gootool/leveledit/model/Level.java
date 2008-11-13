package com.goofans.gootool.leveledit.model;

import java.io.File;
import java.io.StringReader;
import java.io.IOException;

import com.goofans.gootool.io.BinFormat;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.wog.WorldOfGoo;
import org.w3c.dom.Document;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Level
{
  private Scene scene;
  private Resources resources;

  public Level(File wogDir, String prefix) throws IOException
  {
    File dir = new File(wogDir, "res/levels/" + prefix);

    File sceneFile = new File(dir, prefix + ".scene.bin");
    String xml = BinFormat.decodeFile(sceneFile);
    Document doc = XMLUtil.loadDocumentFromReader(new StringReader(xml));
    scene = new Scene(doc);

    File resourcesFile = new File(dir, prefix + ".resrc.bin");
    xml = BinFormat.decodeFile(resourcesFile);
    doc = XMLUtil.loadDocumentFromReader(new StringReader(xml));

    resources = new Resources(doc, wogDir);
  }

  public Scene getScene()
  {
    return scene;
  }

  public Resources getResources()
  {
    return resources;
  }

  public static void main(String[] args) throws IOException
  {
    WorldOfGoo.init();
    Level l = new Level(WorldOfGoo.getWogDir(), "EconomicDivide");

    System.out.println("l = " + l);
  }
}
