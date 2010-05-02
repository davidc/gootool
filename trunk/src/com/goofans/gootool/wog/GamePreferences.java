/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.wog;

import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.model.Language;
import com.goofans.gootool.model.Resolution;
import com.goofans.gootool.util.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GamePreferences
{
  private static final Logger log = Logger.getLogger(GamePreferences.class.getName());

  static final XPathExpression USER_CONFIG_XPATH_LANGUAGE;
  static final XPathExpression USER_CONFIG_XPATH_SCREENWIDTH;
  static final XPathExpression USER_CONFIG_XPATH_SCREENHEIGHT;
  static final XPathExpression USER_CONFIG_XPATH_REFRESHRATE;
  static final XPathExpression USER_CONFIG_XPATH_UIINSET;

  static {
    XPath path = XPathFactory.newInstance().newXPath();
    try {
      USER_CONFIG_XPATH_LANGUAGE = path.compile("/config/param[@name='language']/@value");
      USER_CONFIG_XPATH_SCREENWIDTH = path.compile("/config/param[@name='screen_width']/@value");
      USER_CONFIG_XPATH_SCREENHEIGHT = path.compile("/config/param[@name='screen_height']/@value");
      USER_CONFIG_XPATH_REFRESHRATE = path.compile("/config/param[@name='refreshrate']/@value");
      USER_CONFIG_XPATH_UIINSET = path.compile("/config/param[@name='ui_inset']/@value");
    }
    catch (XPathExpressionException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private GamePreferences()
  {
  }

  /*
   * Loads the defaults from main config.txt. These are overwritten by our preferences if we have any.
   */
  static void readGamePreferences(Configuration c, File configFile) throws IOException
  {
    // Load the users's config.txt
    Document document = XMLUtil.loadDocumentFromFile(configFile);

    try {
      String language = USER_CONFIG_XPATH_LANGUAGE.evaluate(document);
      if ("".equals(language)) {
        c.setLanguage(Language.DEFAULT_LANGUAGE);
      }
      else {
        c.setLanguage(Language.getLanguageByCode(language));
        log.fine("Found language " + language);
      }
      int screenWidth = ((Double) USER_CONFIG_XPATH_SCREENWIDTH.evaluate(document, XPathConstants.NUMBER)).intValue();
      int screenHeight = ((Double) USER_CONFIG_XPATH_SCREENHEIGHT.evaluate(document, XPathConstants.NUMBER)).intValue();

      Resolution res = Resolution.getResolutionByDimensions(screenWidth, screenHeight);
      log.fine("Found selected resolution " + res);
      c.setResolution(res);

      Object refreshRateResult = USER_CONFIG_XPATH_REFRESHRATE.evaluate(document, XPathConstants.NUMBER);
      if (refreshRateResult != null) {
        int refreshRate = ((Double) refreshRateResult).intValue();
        log.fine("Found selected refresh rate " + refreshRate);
        c.setRefreshRate(refreshRate);
      }

      int ui_inset = ((Double) USER_CONFIG_XPATH_UIINSET.evaluate(document, XPathConstants.NUMBER)).intValue();
      c.setUiInset(ui_inset);

      log.fine("Found selected ui_inset " + ui_inset);

    }
    catch (XPathExpressionException e) {
      log.log(Level.SEVERE, "Unable to execute XPath", e);
      throw new IOException("Unable to execute XPath: " + e.getLocalizedMessage());
    }
  }

  static void writeGamePreferences(Configuration c, File configFile) throws IOException
  {
    // Load the users's config.txt
    Document document = XMLUtil.loadDocumentFromFile(configFile);

    try {
      Node n;
      if (c.getLanguage() != null) {
        n = (Node) USER_CONFIG_XPATH_LANGUAGE.evaluate(document, XPathConstants.NODE);
        n.setNodeValue(c.getLanguage().getCode());
      }

      Resolution resolution = c.getResolution();
      if (resolution != null) {
        n = (Node) USER_CONFIG_XPATH_SCREENWIDTH.evaluate(document, XPathConstants.NODE);
        n.setNodeValue(String.valueOf(resolution.getWidth()));

        n = (Node) USER_CONFIG_XPATH_SCREENHEIGHT.evaluate(document, XPathConstants.NODE);
        n.setNodeValue(String.valueOf(resolution.getHeight()));
      }

      Integer refreshRate = c.getRefreshRate();
      if (refreshRate != null) {
        n = (Node) USER_CONFIG_XPATH_REFRESHRATE.evaluate(document, XPathConstants.NODE);
        if (n != null) {
          n.setNodeValue(refreshRate.toString());
        }
      }

      n = (Node) USER_CONFIG_XPATH_UIINSET.evaluate(document, XPathConstants.NODE);
      n.setNodeValue(String.valueOf(c.getUiInset()));
    }
    catch (XPathExpressionException e) {
      log.log(Level.SEVERE, "Unable to execute XPath", e);
      throw new IOException("Unable to execute XPath: " + e.getLocalizedMessage());
    }

    try {
      XMLUtil.writeDocumentToFile(document, configFile);
    }
    catch (TransformerException e) {
      log.log(Level.SEVERE, "Unable to write config file", e);
      throw new IOException("Unable to write config file: " + e.getLocalizedMessage());
    }
  }

}
