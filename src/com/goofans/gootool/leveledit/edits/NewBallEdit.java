package com.goofans.gootool.leveledit.edits;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

import com.goofans.gootool.leveledit.model.BallInstance;
import com.goofans.gootool.leveledit.model.Level;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class NewBallEdit implements UndoableEdit
{
  private Level level;
  private BallInstance addedBall;
  private boolean done = false;

  public NewBallEdit(Level level, BallInstance addedBall)
  {
    this.level = level;
    this.addedBall = addedBall;

    redo();
  }

  public void undo() throws CannotUndoException
  {
    if (!canUndo()) throw new CannotUndoException();

    level.getLevelContents().removeItem(addedBall);
    done = false;
  }

  public boolean canUndo()
  {
    return done;
  }

  public void redo() throws CannotRedoException
  {
    if (!canRedo()) throw new CannotRedoException();

    level.getLevelContents().addItem(addedBall);
    done = true;
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

  public String getPresentationName()
  {
    return "Create ball " + addedBall.type;
  }

  public String getUndoPresentationName()
  {
    return getPresentationName();
  }

  public String getRedoPresentationName()
  {
    return getPresentationName();
  }
}
