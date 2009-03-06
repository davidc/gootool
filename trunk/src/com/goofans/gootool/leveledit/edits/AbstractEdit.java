package com.goofans.gootool.leveledit.edits;

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
    return GooTool.getTextProvider().getText("leveledit.edit.undo", getPresentationName());
  }

  public String getRedoPresentationName()
  {
    return GooTool.getTextProvider().getText("leveledit.edit.redo", getPresentationName());
  }
}
