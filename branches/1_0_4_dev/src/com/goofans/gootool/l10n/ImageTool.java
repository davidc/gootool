/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.l10n;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.wog.WorldOfGoo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ImageTool extends ProgressIndicatingTask
{
  private static final Border TABLE_BORDER = BorderFactory.createLineBorder(Color.BLACK);

  private JPanel contentPanel;
  public JPanel rootPanel;

  private final boolean debug;
  private final Map<String, Map<String, String>> languages;
  private final File sourceDir;
  private final File outputDir;

  public ImageTool(File sourceDir, File outputDir, Map<String, Map<String, String>> languages, Color background, boolean debug)
  {
    if (outputDir == null) {
      GridBagLayout layout = new GridBagLayout();
      contentPanel = new JPanel(layout);
      contentPanel.setBackground(background);
      JScrollPane scroller = new JScrollPane(contentPanel);
      scroller.getVerticalScrollBar().setUnitIncrement(20);
      scroller.getHorizontalScrollBar().setUnitIncrement(20);

      rootPanel = new JPanel(new BorderLayout());

      rootPanel.add(scroller, BorderLayout.CENTER);
    }

    this.languages = languages;
    this.debug = debug;
    this.sourceDir = sourceDir;
    this.outputDir = outputDir;
  }

  @Override
  public void run() throws Exception
  {
    GridBagConstraints constraints = new GridBagConstraints();

    if (outputDir == null) {
      beginStep("Initialising GUI", false);

      constraints.gridy = 0;
      constraints.gridx = 1;
      contentPanel.add(new JLabel("Original"), constraints);
      for (String language : languages.keySet()) {
        constraints.gridx++;
        contentPanel.add(new JLabel(language), constraints);
      }
    }

    FontManager fm = new FontManager(sourceDir);

    Document d = XMLUtil.loadDocumentFromFile(new File(sourceDir, "l10n_images.xml"));

    NodeList processImageNodes = d.getDocumentElement().getChildNodes();
    for (int i = 0; i < processImageNodes.getLength(); i++) {
      Node node = processImageNodes.item(i);
      if (node instanceof Element) {
        Element el = (Element) node;
        if ("process-image".equals(el.getTagName())) {
          String sourceFileName = el.getElementsByTagName("source").item(0).getTextContent().trim();
          String destFileName = el.getElementsByTagName("dest").item(0).getTextContent().trim();

          beginStep("Processing " + sourceFileName, false);

          ImageGenerator generator = new ImageGenerator(new File(sourceDir, sourceFileName), el, fm, debug);

          if (outputDir == null) {
            constraints.gridy++;
            constraints.gridx = 0;
            contentPanel.add(new JLabel("<html>" + sourceFileName + " -&gt;<br>" + destFileName), constraints);

            try {
              constraints.gridx++;
              contentPanel.add(makeLabel(ImageIO.read(WorldOfGoo.getTheInstance().getGameFile(destFileName + ".png"))), constraints);
            }
            catch (IIOException e) {
              // don't care, e.g. test image
            }
          }

          for (String language : languages.keySet()) {
            generator.process(languages.get(language));

            if (outputDir == null) {
              constraints.gridx++;
              contentPanel.add(makeLabel(generator.getFinalImage()), constraints);
            }
            else {
              generator.writeImage(new File(outputDir, destFileName + "." + language + ".png"));
            }
          }
        }
      }
    }
  }

  public void showWindow()
  {
    JFrame frame = new JFrame("Image l10n Test");
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setLocationByPlatform(true);
    frame.add(rootPanel);
    frame.setIconImage(GooTool.getMainIconImage());

    frame.setPreferredSize(new Dimension(1024, 768));

    frame.validate();
    frame.pack();
    frame.setVisible(true);
  }

  private static JLabel makeLabel(BufferedImage image)
  {
    JLabel label = new JLabel(new ImageIcon(image));
    label.setMinimumSize(new Dimension(image.getWidth(), image.getHeight()));
    label.setBorder(TABLE_BORDER);
    return label;
  }

  @SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection", "HardcodedFileSeparator"})
  public static void main(String[] args) throws Exception
  {
    WorldOfGoo.getTheInstance().init();
//    File sourceFile = new File("C:\\Users\\david\\Downloads\\wog-translate\\images_continue.png");

//    ImageGenerator generator = new ImageGenerator(sourceFile);
//    generator.writeImage("en.png");

//    new ImageGenerator(sourceFile, "continue").writeImage("en.png");
//    new ImageGenerator(sourceFile, "continuer").writeImage("es.png");
//    new ImageGenerator(sourceFile, "continuer").writeImage("fr.png");
//    new ImageGenerator(sourceFile, "weiter").writeImage("de.png");
//    new ImageGenerator(sourceFile, "continua").writeImage("it.png");
//    new ImageGenerator(sourceFile, "verder").writeImage("nl.png");

    Map<String, Map<String, String>> languages = new HashMap<String, Map<String, String>>();

//    Map<String, String> en = new HashMap<String, String>();
//    en.put("IMG_RESULTS_CONTINUE", "continue");
//    en.put("IMG_WOGC_SIGN1_CONNECT", "connect");
//    en.put("IMAGE_GOINGUP_TUTORIALPOST", "Drag n'|drop to|build|to the|pipe");
//
//    Map<String, String> es = new HashMap<String, String>();
//    es.put("IMG_RESULTS_CONTINUE", "continuar");
//    Map<String, String> fr = new HashMap<String, String>();
//    fr.put("IMG_RESULTS_CONTINUE", "continuer");
//    Map<String,String> de = new HashMap<String, String>();
//    de.put("IMG_RESULTS_CONTINUE", "weiter");
//
//    Map<String, String> it = new HashMap<String, String>();
//    it.put("IMG_RESULTS_CONTINUE", "continua");
//    it.put("IMG_WOGC_SIGN1_CONNECT", "connetti");
//    it.put("IMAGE_GOINGUP_TUTORIALPOST1", "Trascina e");
//    it.put("IMAGE_GOINGUP_TUTORIALPOST2", "rilascia per ");
//    it.put("IMAGE_GOINGUP_TUTORIALPOST3", "costruire");
//    it.put("IMAGE_GOINGUP_TUTORIALPOST4", "fino al");
//    it.put("IMAGE_GOINGUP_TUTORIALPOST5", "tubo");
//
//    Map<String, String> nl = new HashMap<String, String>();
//    nl.put("IMG_RESULTS_CONTINUE", "verda");


    String wikiBase = TranslationDownloader.DEFAULT_WIKI_URL;
    languages.put("en", TranslationDownloader.getTranslations(wikiBase, "Italian_translation", false));
    languages.put("ru", TranslationDownloader.getTranslations(wikiBase, "Russian_translation", true));
//    languages.put("fr", fr);
    languages.put("de", TranslationDownloader.getTranslations(wikiBase, "German_translation", true));
    languages.put("it", TranslationDownloader.getTranslations(wikiBase, "Italian_translation", true));
//    languages.put("nl", nl);

    ImageTool imageTool = new ImageTool(new File("C:\\Users\\david\\Downloads\\wog-translate\\"), null, languages, Color.WHITE, true);
    imageTool.run();
    imageTool.showWindow();
  }
}
