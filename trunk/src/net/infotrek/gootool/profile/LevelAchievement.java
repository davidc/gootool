package net.infotrek.gootool.profile;

/**
 * @author David Croft (david.croft@infotrek.net)
 * @version $Id$
 */
public class LevelAchievement
{
  private String levelId;
  private int mostBalls;
  private int leastMoves;
  private int leastTime;

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
