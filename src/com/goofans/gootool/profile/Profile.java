package com.goofans.gootool.profile;

import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Profile
{
  private static final Logger log = Logger.getLogger(Profile.class.getName());

  public static final int FLAG_ONLINE = 1;
  public static final int FLAG_GOOCORP_UNLOCKED = 2;
  public static final int FLAG_GOOCORP_DESTROYED = 4;
  public static final int FLAG_WHISTLE = 8;
  public static final int FLAG_TERMS = 16;

  private String name;
  private int flags;
  private int playTime;
  private int levels;
  private String onlineId;

  private List<LevelAchievement> levelAchievements = new ArrayList<LevelAchievement>();

  private Tower tower;
  private int newBalls;

  public Profile(String profileData) throws IOException
  {
    Logger.getLogger("").setLevel(Level.ALL);
    Logger.getLogger("").getHandlers()[0].setLevel(Level.ALL);

    log.finest("Constructing Profile from data: " + profileData);

    StringTokenizer tok = new StringTokenizer(profileData, ",");
//    String[] bits = profileData.split(",");

    name = tok.nextToken();
    flags = Integer.parseInt(tok.nextToken());
    playTime = Integer.parseInt(tok.nextToken());
    levels = Integer.parseInt(tok.nextToken());

    while (tok.hasMoreTokens()) {
      String levelId = tok.nextToken();
      if (levelId.equals("0")) {
        break;
      }

      int mostBalls = Integer.parseInt(tok.nextToken());
      int leastMoves = Integer.parseInt(tok.nextToken());
      int leastTime = Integer.parseInt(tok.nextToken());
      LevelAchievement levelAchievement = new LevelAchievement(levelId, mostBalls, leastMoves, leastTime);
      levelAchievements.add(levelAchievement);
    }

    tower = new Tower(tok.nextToken());
    onlineId = tok.nextToken();
    newBalls = Integer.parseInt(tok.nextToken());

    while (tok.hasMoreTokens()) {
      log.warning("UNUSED TOKEN at end of profileData " + tok.nextToken());
    }
  }

  public int getNewBalls()
  {
    return newBalls;
  }

  public String getName()
  {
    return name;
  }

  public int getFlags()
  {
    return flags;
  }

  public int getPlayTime()
  {
    return playTime;
  }

  public int getLevels()
  {
    return levels;
  }

  public String getOnlineId()
  {
    return onlineId;
  }

  public List<LevelAchievement> getLevelAchievements()
  {
    return levelAchievements;
  }

  public Tower getTower()
  {
    return tower;
  }

  public boolean hasFlag(int flag)
  {
    return (flags & flag) != 0;
  }

  public String toString()
  {
//    return "Profile{" +
//            "name='" + name + '\'' +
//            ", flags=" + flags +
//            ", playTime=" + playTime +
//            ", levels=" + levels +
//            ", onlineId='" + onlineId + '\'' +
//            ", levelAchievements=" + levelAchievements +
//            ", tower='" + tower + '\'' +
//            ", newBalls=" + newBalls +
//            '}';
    return name;
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr"})
  public static void main(String[] args) throws IOException
  {
    Profile newPlayer = new Profile("davidc,0,55,0,0,_,_,0");
    System.out.println("newPlayer = " + newPlayer);
    System.out.println("newPlayer.levelAchievements = " + newPlayer.levelAchievements);
    System.out.println("newPlayer.tower = " + newPlayer.tower);

    Profile doneOneLevel = new Profile("davidc,0,103,1,GoingUp,11,3,17,0,_b:Drained:-45:685:0:0:b:Drained:-38:646:0:0:b:Drained:-6:897:0:0:b:Drained:-95:612:0:0:b:Drained:38:713:0:0:b:Drained:-23:684:0:0:b:Drained:-74:792:0:0,_,7");
    System.out.println("doneOneLevel = " + doneOneLevel);
    System.out.println("doneOneLevel.levelAchievements = " + doneOneLevel.levelAchievements);
    System.out.println("doneOneLevel.tower = " + doneOneLevel.tower);

    // enabled online score submission (flags=1 now):
    Profile doneTwoLevels = new Profile("davidc,1,254,2,GoingUp,11,3,17,EconomicDivide,15,9,34,0,_b:Drained:-45:685:0:0:b:Drained:-38:646:0:0:b:Drained:-6:897:0:0:b:Drained:-95:612:0:0:b:Drained:38:713:0:0:b:Drained:-23:684:0:0:b:Drained:-74:792:0:0:b:Drained:70:896:0:0:b:Drained:20:666:0:0:b:Drained:33:712:0:0:b:Drained:76:709:0:0:b:Drained:39:897:0:0:b:Drained:3:869:0:0:b:Drained:-81:600:0:0,_,14");
    System.out.println("doneTwoLevels = " + doneTwoLevels);
    System.out.println("doneTwoLevels.levelAchievements = " + doneTwoLevels.levelAchievements);
    System.out.println("doneTwoLevels.tower = " + doneTwoLevels.tower);

    Profile corpUnlockedAndBuilt = new Profile("davidc,3,541,3,GoingUp,11,3,17,EconomicDivide,15,9,34,HangLow,22,6,170,0,_b:Drained:-488.46:11.32:-1.08:0.00:b:Drained:47.44:170.26:-0.96:-1.76:b:Drained:81.99:40.52:1.25:1.56:b:Drained:-624.22:16.52:-1.96:0.00:b:Drained:30.96:139.96:-0.96:-1.76:b:Drained:64.31:118.50:2.00:0.08:b:Drained:-14.71:69.73:-1.15:-1.64:b:Drained:130.92:101.79:-1.25:-1.56:b:Drained:16.60:17.35:-1.99:0.16:b:Drained:88.33:48.45:1.25:1.56:b:Drained:47.27:117.85:-2.00:-0.08:b:Drained:20.30:120.34:0.96:1.76:b:Drained:77.32:225.21:0.96:1.76:b:Drained:88.66:230.47:0.94:-1.76:b:Drained:-22.69:20.41:-1.99:0.16:b:Drained:63.63:200.03:-0.96:-1.76:b:Drained:-974.91:15.79:-1.57:0.00:b:Drained:84.61:238.55:0.15:0.22:b:Drained:146.75:121.72:-0.06:0.03:b:Drained:-25.42:54.51:1.15:1.64:b:Drained:-31.54:45.80:-1.15:-1.64:b:Drained:49.11:14.82:-1.99:0.16:b:Drained:-48.60:21.54:-1.15:-1.64:b:Drained:42.72:57.62:0.76:-1.85:b:Drained:19.84:113.07:-0.76:1.85:b:Drained:-47.94:22.56:0.06:0.18:b:Drained:60.76:13.91:0.01:0.00:b:Drained:18.44:116.80:0.10:0.07:s:Drained:25:26:9.0000:110.00:0:s:Drained:26:27:9.0000:114.13:0:s:Drained:27:25:9.0000:114.13:0:s:Drained:18:27:9.0000:126.74:0:s:Drained:18:26:9.0000:140.00:0:s:Drained:17:18:9.0000:132.84:0:s:Drained:17:27:9.0000:138.94:0,_6d917cb0e56f4945dc9e9d1eaa34056f,0");
    System.out.println("corpUnlockedAndBuilt = " + corpUnlockedAndBuilt);
    System.out.println("corpUnlockedAndBuilt.levelAchievements = " + corpUnlockedAndBuilt.levelAchievements);
    System.out.println("corpUnlockedAndBuilt.tower = " + corpUnlockedAndBuilt.tower);
  }
}

