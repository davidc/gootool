package com.goofans.gootool.profile;

/**
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
