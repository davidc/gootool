/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

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
  private final Level level;
  private final BallInstance addedBall;

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
    return GooTool.getTextProvider().formatString("leveledit.edit.createball", addedBall.type);
  }

}
