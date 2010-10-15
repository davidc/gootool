/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.model;

import java.io.IOException;

import org.w3c.dom.Element;
import com.goofans.gootool.util.XMLUtil;

/**
 * Node "Strand", attribute "gb1" is mandatory (1012 occurrences found)
 * Node "Strand", attribute "gb2" is mandatory (1012 occurrences found)
 * 2 attributes found
 * 0 child tags found for node "Strand"
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Strand extends LevelContentsItem
{
  public String gb1, gb2;

  public Strand(Element element) throws IOException
  {
    gb1 = XMLUtil.getAttributeStringRequired(element, "gb1");
    gb2 = XMLUtil.getAttributeStringRequired(element, "gb2");
  }
}
