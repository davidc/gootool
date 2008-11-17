package com.goofans.gootool.l10n;

import javax.swing.*;
import javax.swing.border.Border;
import javax.imageio.ImageIO;
import javax.imageio.IIOException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.wog.WorldOfGoo;
import com.goofans.gootool.GooTool;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ImageTool extends ProgressIndicatingTask
{
  private static final Border TABLE_BORDER = BorderFactory.createLineBorder(Color.BLACK);

  private JPanel contentPanel;
  public JPanel rootPanel;

  private boolean debug;
  private Map<String, Map<String, String>> languages;
  private File sourceDir;

  public ImageTool(File sourceDir, Map<String, Map<String, String>> languages, Color background, boolean debug) throws IOException, FontFormatException, XPathExpressionException
  {
    GridBagLayout layout = new GridBagLayout();
    contentPanel = new JPanel(layout);
    contentPanel.setBackground(background);
    JScrollPane scroller = new JScrollPane(contentPanel);
    scroller.getVerticalScrollBar().setUnitIncrement(20);
    scroller.getHorizontalScrollBar().setUnitIncrement(20);

    rootPanel = new JPanel(new BorderLayout());

    rootPanel.add(scroller, BorderLayout.CENTER);

    this.languages = languages;
    this.debug = debug;
    this.sourceDir = sourceDir;
  }

  public void run() throws Exception
  {
    beginStep("Initialising GUI", false);
    GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridy = 0;
    constraints.gridx = 1;
    contentPanel.add(new JLabel("Original"), constraints);
    for (String language : languages.keySet()) {
      constraints.gridx++;
      contentPanel.add(new JLabel(language), constraints);
    }

    FontManager fm = new FontManager(sourceDir);

    Document d = XMLUtil.loadDocumentFromFile(new File(sourceDir, "l10n_images.xml"));

    NodeList processImageNodes = d.getDocumentElement().getChildNodes();
    for (int i = 0; i < processImageNodes.getLength(); i++) {
      Node node = processImageNodes.item(i);
      if (node instanceof Element) {
        Element el = (Element) node;
        if (el.getTagName().equals("process-image")) {
          String sourceFileName = el.getElementsByTagName("source").item(0).getTextContent().trim();
          String destFileName = el.getElementsByTagName("dest").item(0).getTextContent().trim();

          beginStep("Processing " + sourceFileName, false);

          ImageGenerator generator = new ImageGenerator(new File(sourceDir, sourceFileName), el, fm, debug);

          constraints.gridy++;
          constraints.gridx = 0;
          contentPanel.add(new JLabel("<html>" + sourceFileName + " -&gt;<br>" + destFileName), constraints);

          try {
            constraints.gridx++;
            contentPanel.add(makeLabel(ImageIO.read(new File(WorldOfGoo.getWogDir(), destFileName + ".png"))), constraints);
          }
          catch (IIOException e) {
            // don't care, e.g. test image
          }

          for (String language : languages.keySet()) {
            beginStep("Processing " + sourceFileName + " in " + language, false);
            generator.process(languages.get(language));

            constraints.gridx++;
            contentPanel.add(makeLabel(generator.getFinalImage()), constraints);
          }
        }
      }
    }

    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        showWindow();
      }
    });
//    contentPanel.validate();
  }

  private void showWindow()
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

  static Color randomColor()
  {
    Random r = new Random();
    return new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
  }

  private static JLabel makeLabel(BufferedImage image) throws IOException, FontFormatException
  {
    JLabel label = new JLabel(new ImageIcon(image));
    label.setMinimumSize(new Dimension(image.getWidth(), image.getHeight()));
    label.setBorder(TABLE_BORDER);
    return label;
  }

  public static void main(String[] args) throws IOException, FontFormatException, XPathExpressionException
  {
    WorldOfGoo.init();
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


    Map<String, String> en = new HashMap<String, String>();
    en.put("IMG_RESULTS_CONTINUE", "continue");
    en.put("IMG_WOGC_SIGN1_CONNECT", "connect");
    en.put("IMAGE_GOINGUP_TUTORIALPOST", "Drag n'|drop to|build|to the|pipe");

    Map<String, String> es = new HashMap<String, String>();
    es.put("IMG_RESULTS_CONTINUE", "continuar");
    Map<String, String> fr = new HashMap<String, String>();
    fr.put("IMG_RESULTS_CONTINUE", "continuer");

//    Map<String,String> de = new HashMap<String, String>();
//    de.put("IMG_RESULTS_CONTINUE", "weiter");


    Map<String, String> it = new HashMap<String, String>();
    it.put("IMG_RESULTS_CONTINUE", "continua");
    it.put("IMG_WOGC_SIGN1_CONNECT", "connetti");
    it.put("IMAGE_GOINGUP_TUTORIALPOST1", "Trascina e");
    it.put("IMAGE_GOINGUP_TUTORIALPOST2", "rilascia per ");
    it.put("IMAGE_GOINGUP_TUTORIALPOST3", "costruire");
    it.put("IMAGE_GOINGUP_TUTORIALPOST4", "fino al");
    it.put("IMAGE_GOINGUP_TUTORIALPOST5", "tubo");

    Map<String, String> nl = new HashMap<String, String>();
    nl.put("IMG_RESULTS_CONTINUE", "verda");


    String wikiBase = "http://hell.student.utwente.nl/wog/mediawiki/";
    languages.put("en", TranslationDownloader.getTranslations(wikiBase, "Italian_translation", false));
    languages.put("ru", TranslationDownloader.getTranslations(wikiBase, "Russian_translation", true));
//    languages.put("fr", fr);
    languages.put("de", TranslationDownloader.getTranslations(wikiBase, "German_translation", true));
    languages.put("it", TranslationDownloader.getTranslations(wikiBase, "Italian_translation", true));
//    languages.put("nl", nl);

    new ImageTool(new File("C:\\Users\\david\\Downloads\\wog-translate\\"), languages, Color.WHITE, true).showWindow();

  }
}
