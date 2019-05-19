/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.profile;

/**
 * An achievement stored on the user's profile for a given level. 
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LevelAchievement
{
  private final String levelId;
  private final int mostBalls;
  private final int leastMoves;
  private final int leastTime;

  public LevelAchievement(String levelId, int maxBalls, int leastMoves, int leastTime)
  {
    this.levelId = levelId;
    this.mostBalls = maxBalls;
    this.leastMoves = leastMoves;
    this.leastTime = leastTime;
  }

  public String getLevelId()
  {
    return levelId;
  }

  public int getMostBalls()
  {
    return mostBalls;
  }

  public int getLeastMoves()
  {
    return leastMoves;
  }

  public int getLeastTime()
  {
    return leastTime;
  }

  @Override
  @SuppressWarnings({"StringConcatenation"})
  public String toString()
  {
    return "LevelAchievement{" +
            "levelId='" + levelId + '\'' +
            ", mostBalls=" + mostBalls +
            ", leastMoves=" + leastMoves +
            ", leastTime=" + leastTime +
            '}';
  }
}
