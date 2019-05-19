/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract superclass for tasks that can indicate their progress to listeners.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public abstract class ProgressIndicatingTask
{
  private final List<ProgressListener> listeners = new ArrayList<ProgressListener>();

  public abstract void run() throws Exception;

  public void addListener(ProgressListener listener)
  {
    listeners.add(listener);
  }

  public void removeListener(ProgressListener listener)
  {
    listeners.remove(listener);
  }


  protected void beginStep(String taskDescription, boolean progressAvailable)
  {
//    log.log(Level.INFO, "Beginning step " + taskDescription);
    for (ProgressListener listener : listeners) {
      listener.beginStep(taskDescription, progressAvailable);
    }
  }

  protected void progressStep(float percent)
  {
    for (ProgressListener listener : listeners) {
      listener.progressStep(percent);
    }
  }
}
