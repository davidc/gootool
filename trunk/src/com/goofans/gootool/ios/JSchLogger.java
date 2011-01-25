/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.ios;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.jcraft.jsch.JSch;

/**
 * Implements JSch's logger interfaces and forwards the log messages on to java.util.logging.
 * Looks at the call stack to infer the real source class/method namme.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
class JSchLogger implements com.jcraft.jsch.Logger
{
  private static final Logger log = Logger.getLogger(JSch.class.getName());

  private static final Level[] LOG_LEVEL_MAPPING = new Level[]{Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE, Level.SEVERE};

  public boolean isEnabled(int level)
  {
    return log.isLoggable(jschToJavaLevel(level));
  }

  public void log(int level, String message)
  {
    LogRecord record = new LogRecord(jschToJavaLevel(level), message);

    // Get the calling method in order to fill in the source class/method name
    StackTraceElement frame = new Throwable().getStackTrace()[1];

    record.setLoggerName(log.getName());
    record.setSourceClassName(frame.getClassName());
    record.setSourceMethodName(frame.getMethodName());

    log.log(record);
  }

  private Level jschToJavaLevel(int level)
  {
    if (level >= 0 && level < LOG_LEVEL_MAPPING.length) {
      return LOG_LEVEL_MAPPING[level];
    }
    else {
      return Level.WARNING;
    }
  }
}
