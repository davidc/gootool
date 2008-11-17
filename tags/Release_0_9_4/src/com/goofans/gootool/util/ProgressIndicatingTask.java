package com.goofans.gootool.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public abstract class ProgressIndicatingTask
{
  private List<ProgressListener> listeners = new ArrayList<ProgressListener>();

  public abstract void run() throws Exception;

  public void addListener(ProgressListener listener)
  {
    listeners.add(listener);
  }

  public void removeListener(ProgressListener listener)
  {
    listeners.remove(listener);
  }


  protected  void beginStep(String taskDescription, boolean progressAvailable)
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
