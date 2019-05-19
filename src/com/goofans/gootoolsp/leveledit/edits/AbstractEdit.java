/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootoolsp.leveledit.edits;

import javax.swing.undo.UndoableEdit;

import com.goofans.gootool.GooTool;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public abstract class AbstractEdit implements UndoableEdit
{
  protected boolean done = false;

  public boolean canUndo()
  {
    return done;
  }

  public boolean canRedo()
  {
    return !done;
  }

  public void die()
  {
  }

  public boolean addEdit(UndoableEdit anEdit)
  {
    return false;
  }

  public boolean replaceEdit(UndoableEdit anEdit)
  {
    return false;
  }

  public boolean isSignificant()
  {
    return true;
  }

  public String getUndoPresentationName()
  {
    return GooTool.getTextProvider().formatString("leveledit.edit.undo", getPresentationName());
  }

  public String getRedoPresentationName()
  {
    return GooTool.getTextProvider().formatString("leveledit.edit.redo", getPresentationName());
  }
}
