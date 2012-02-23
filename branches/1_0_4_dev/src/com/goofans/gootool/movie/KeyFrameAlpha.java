/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.movie;


import java.util.Map;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class KeyFrameAlpha extends KeyFrame
{
  public KeyFrameAlpha(byte[] contents, int offset, int stringTableOffset)
  {
    super(contents, offset, stringTableOffset);
  }

  @Override
  protected void setFrameXMLAttributes(Map<String, String> attributes)
  {
    attributes.put("alpha", String.valueOf(alpha));
  }
}