package com.goofans.gootool.l10n;

import javax.swing.*;
import javax.swing.border.Border;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Random;
import java.util.HashMap;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import com.goofans.gootool.util.XMLUtil;
import com.goofans.gootool.wog.WorldOfGoo;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ImageTool implements ActionListener
{
  private static final String CHANGE_COLOR = "Color";
  private static final Border TABLE_BORDER = BorderFactory.createLineBorder(Color.BLACK);

  private JPanel contentPanel;
  public JPanel rootPanel;

  public ImageTool(File sourceDir, Map<String, Map<String, String>> languages) throws IOException, FontFormatException
  {
    GridBagLayout layout = new GridBagLayout();
    contentPanel = new JPanel(layout);
//    JViewport viewport = new JViewport();
    JScrollPane scroller = new JScrollPane(contentPanel);
//    viewport.a
//    scroller.setViewport(viewport);
//    scroller.add(contentPanel);

    rootPanel = new JPanel(new GridBagLayout());
    JButton button = new JButton("Color");
    button.setActionCommand(CHANGE_COLOR);
    button.addActionListener(this);

    GridBagConstraints rootContraints = new GridBagConstraints();
    rootContraints.gridx = 0;
    rootContraints.gridy = 0;
    rootPanel.add(button, rootContraints);

    rootContraints.gridx = 0;
    rootContraints.gridy = 1;
    rootPanel.add(scroller, rootContraints);

    GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridy = 0;
    constraints.gridx = 1;
    contentPanel.add(new JLabel("Original"), constraints);
    for (String language : languages.keySet()) {
      constraints.gridx++;
      contentPanel.add(new JLabel(language), constraints);
    }

//    contentPanel.add(new JLabel(new ImageIcon(ImageIO.read(sourceFile))));


    Document d = XMLUtil.loadDocumentFromReader(new InputStreamReader(ImageGenerator.class.getResourceAsStream("/i18n_images.xml")));

    NodeList processImageNodes = d.getDocumentElement().getChildNodes();
    for (int i = 0; i < processImageNodes.getLength(); i++) {
      Node node = processImageNodes.item(i);
      if (node instanceof Element) {
        Element el = (Element) node;
        if (el.getTagName().equals("process-image")) {
          String sourceFileName = el.getElementsByTagName("source").item(0).getTextContent().trim();
          String destFileName = el.getElementsByTagName("dest").item(0).getTextContent().trim();

          ImageGenerator generator = new ImageGenerator(new File(sourceDir, sourceFileName));

          constraints.gridy++;
          constraints.gridx = 0;
          contentPanel.add(new JLabel("<html>" + sourceFileName + " -&gt;<br>" + destFileName), constraints);

          constraints.gridx++;
          System.out.println("constraints.gridx = " + constraints.gridx);
          System.out.println("constraints.gridy = " + constraints.gridy);
          contentPanel.add(makeLabel(ImageIO.read(new File(WorldOfGoo.getWogDir(), destFileName + ".png"))), constraints);

          for (String language : languages.keySet()) {

            generator.reset();

            NodeList addTextNodes = el.getElementsByTagName("add-text");

            for (int j = 0; j < addTextNodes.getLength(); j++) {
              Element addTextEl = (Element) addTextNodes.item(j);

              String string = addTextEl.getElementsByTagName("string").item(0).getTextContent().trim();
              String fontName = addTextEl.getElementsByTagName("font-name").item(0).getTextContent().trim();
              Font font = getFont(fontName);
              float fontSize = getOptionalFloat(addTextEl, "font-size");
              float stretch = getOptionalFloat(addTextEl, "stretch");
              float outline = getOptionalFloat(addTextEl, "outline");
              Color color = parseColor(addTextEl.getElementsByTagName("color").item(0).getTextContent().trim());
              int xPos = Integer.parseInt(addTextEl.getElementsByTagName("x-position").item(0).getTextContent().trim());
              int yPos = Integer.parseInt(addTextEl.getElementsByTagName("y-position").item(0).getTextContent().trim());
              float rotation = getOptionalFloat(addTextEl, "rotation");

              String text = null;
              int openBracketsPos = string.indexOf('[');
              if (openBracketsPos > 0) {
                String realString = string.substring(0, openBracketsPos);
                String wholeText = languages.get(language).get(realString);
                if (wholeText != null) {
                  String[] bits = wholeText.split("\\|");

                  int offset = Integer.valueOf(string.substring(openBracketsPos + 1, string.indexOf(']')));
                  System.out.println("offset = " + offset);

                  if (offset > bits.length) {
                    text = "!!offset " + offset + "!!";
                  }
                  else {
                    text = bits[offset - 1];
                  }
                }
              }
              else {
                text = languages.get(language).get(string);
              }
              if (text == null) text = "!!" + language + "!!";

              generator.drawText(text, font, fontSize, stretch, outline, color, new Point(xPos, yPos), rotation);
            }

            generator.finish();

            constraints.gridx++;
            contentPanel.add(makeLabel(generator.getFinalImage()), constraints);

          }
        }
      }
    }

  }

  private void showWindow() throws IOException, FontFormatException
  {
    JFrame frame = new JFrame("i18n Test");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationByPlatform(true);
    frame.add(rootPanel);

    frame.validate();
    frame.pack();
    frame.setVisible(true);
  }

  static Color randomColor()
  {
    Random r = new Random();
    return new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256));
  }

  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals(CHANGE_COLOR)) {
      contentPanel.setBackground(ImageTool.randomColor());
    }
  }

  private static JLabel makeLabel(BufferedImage image) throws IOException, FontFormatException
  {
    JLabel label = new JLabel(new ImageIcon(image));
    label.setMinimumSize(new Dimension(image.getWidth(), image.getHeight()));
    label.setBorder(TABLE_BORDER);
    return label;
  }

  private static Font getFont(String filename) throws FontFormatException, IOException
  {
    // TODO cache
    return Font.createFont(Font.TRUETYPE_FONT, new File("C:\\Users\\david\\Downloads\\wog-translate\\" + filename));
  }

  // TODO make getElementRequired etc in XMLUtil
  private static float getOptionalFloat(Element addTextEl, String tagName)
  {
    NodeList list = addTextEl.getElementsByTagName(tagName);
    if (list.getLength() == 0) return 0;
    return Float.parseFloat(list.item(0).getTextContent().trim());
  }

  private static Color parseColor(String s)
  {
    if (s.startsWith("#")) s = s.substring(1);
    return new Color(Integer.valueOf(s.substring(0, 2), 16),
            Integer.valueOf(s.substring(2, 4), 16),
            Integer.valueOf(s.substring(4, 6), 16));
  }

  public static void main(String[] args) throws IOException, FontFormatException
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
    languages.put("es", TranslationDownloader.getTranslations(wikiBase, "Spanish_translation", false));
//    languages.put("fr", fr);
    languages.put("de", TranslationDownloader.getTranslations(wikiBase, "German_translation", true));
    languages.put("it", TranslationDownloader.getTranslations(wikiBase, "Italian_translation", true));
//    languages.put("nl", nl);

    new ImageTool(new File("C:\\Users\\david\\Downloads\\wog-translate\\"), languages).showWindow();

  }
}
