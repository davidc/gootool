package com.goofans.gootool.util;

/**
 * Implemented by classes (i.e. a gui progress bar) that wish to be notified of the state of a progressing task
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public interface ProgressListener
{
  /**
   * Notification that a step has begun.
   * @param taskDescription User-displayed description of what's happening.
   * @param progressAvailable True if progressStep will be called for this task, false if the progress of this step can't be provided.
   */
  public void beginStep(String taskDescription, boolean progressAvailable);

  /**
   * Indicates the progress of the current step.
   * @param percent The estimated percent completion of this step.
   */
  public void progressStep(float percent);
}
