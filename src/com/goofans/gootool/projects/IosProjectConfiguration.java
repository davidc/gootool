/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.projects;

/**
 * Represents the current World of Goo configuration on an iOS device.
 * <p/>
 * There's an "active" instance (what is currently configured on disk) and a "pending" instance (what will be applied).
 * <p/>
 * Support equals() in order to determine whether we must prompt before exit.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class IosProjectConfiguration extends ProjectConfiguration
{
  IosProjectConfiguration()
  {
  }

  @Override
  public boolean equals(Object o)
  {
    return super.equals(o);
  }

  @Override
  public int hashCode()
  {
    return super.hashCode();
  }

  @Override
  public Object clone()
  {
    IosProjectConfiguration clone;
    clone = (IosProjectConfiguration) super.clone();
    // Do any deep cloning of fields here
    return clone;
  }

  @Override
  public String toString()
  {
    return "IosProjectConfiguration{" +
            super.toString() +
            '}';
  }
}
