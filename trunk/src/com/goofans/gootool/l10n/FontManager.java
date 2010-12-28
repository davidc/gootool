/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.l10n;

import java.awt.*;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class FontManager
{
  private final Map<String, Font> fontCache = new HashMap<String, Font>();
  private final File baseDir;

  public FontManager(File baseDir)
  {
    this.baseDir = baseDir;
  }

  public synchronized Font getFont(String filename) throws FontFormatException, IOException
  {
    if (fontCache.containsKey(filename)) {
      return fontCache.get(filename);
    }

    Font font = Font.createFont(Font.TRUETYPE_FONT, new File(baseDir, filename));
    fontCache.put(filename, font);
    return font;
  }
}
