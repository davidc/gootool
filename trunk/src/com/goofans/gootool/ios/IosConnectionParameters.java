/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.ios;

/**
 * Encapsulates the construction parameters of an IosConnection, to enable connection pooling/reuse.
 * Immutable after construction.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class IosConnectionParameters
{
  final String host;
  final String password;

  public IosConnectionParameters(String host, String password)
  {
    this.host = host;
    if (password != null && password.length() == 0) {
      this.password = null;
    }
    else {
      this.password = password;
    }
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IosConnectionParameters that = (IosConnectionParameters) o;

    if (host != null ? !host.equals(that.host) : that.host != null) return false;
    return !(password != null ? !password.equals(that.password) : that.password != null);
  }

  @Override
  public int hashCode()
  {
    int result = host != null ? host.hashCode() : 0;
    result = 31 * result + (password != null ? password.hashCode() : 0);
    return result;
  }
}
