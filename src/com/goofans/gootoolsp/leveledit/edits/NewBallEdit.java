package com.goofans.gootoolsp.leveledit.edits;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import com.goofans.gootoolsp.leveledit.model.BallInstance;
import com.goofans.gootoolsp.leveledit.model.Level;
import com.goofans.gootool.GooTool;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class NewBallEdit extends AbstractEdit
{
  private Level level;
  private BallInstance addedBall;

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

  public void redo() throws CannotRedoException
  {
    if (!canRedo()) throw new CannotRedoException();

    level.getLevelContents().addItem(addedBall);
    done = true;
  }

  public String getPresentationName()
  {
    return GooTool.getTextProvider().getText("leveledit.edit.createball", addedBall.type);
  }

}
