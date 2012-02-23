/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.profile;

import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.IOException;

import com.goofans.gootool.util.DebugUtil;

/**
 * A single profile in the profile file.
 *
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
  public static final int FLAG_32 = 32;
  public static final int FLAG_64 = 64;
  public static final int FLAG_128 = 128;

  private final String name;
  private final int flags;
  private final int playTime;
  private final int levels;
  private final List<String> skippedLevels = new ArrayList<String>();
  private String onlineId;

  private final List<LevelAchievement> levelAchievements = new ArrayList<LevelAchievement>();

  private final Tower tower;
  private final int newBalls;
  private final String data;

  public Profile(String profileData) throws IOException
  {
    log.finest("Constructing Profile from data: " + profileData);

    data = profileData;

    StringTokenizer tok = new StringTokenizer(profileData, ",");

    name = tok.nextToken();
    flags = Integer.parseInt(tok.nextToken());
    playTime = Integer.parseInt(tok.nextToken());
    levels = Integer.parseInt(tok.nextToken());

    // levels is the number of four-tuples of level data
    for (int i = 0; i < levels; ++i) {
      String levelId = tok.nextToken();

      int mostBalls = Integer.parseInt(tok.nextToken());
      int leastMoves = Integer.parseInt(tok.nextToken());
      int leastTime = Integer.parseInt(tok.nextToken());
      LevelAchievement levelAchievement = new LevelAchievement(levelId, mostBalls, leastMoves, leastTime);
      levelAchievements.add(levelAchievement);
    }

    // End of level data. Number of skipped levels, then their IDs.
    int numSkipped = Integer.parseInt(tok.nextToken());
    while (numSkipped > 0) {
      String skippedLevel = tok.nextToken();
      skippedLevels.add(skippedLevel);
      numSkipped--;
    }

    tower = readTower(tok.nextToken());

    onlineId = readOnlineId(tok.nextToken());

    newBalls = Integer.parseInt(tok.nextToken());

    while (tok.hasMoreTokens()) {
      log.warning("UNUSED TOKEN at end of profileData " + tok.nextToken());
    }
  }

  private Tower readTower(String storedTower) throws IOException
  {
    if (!storedTower.startsWith("_")) {
      throw new IOException("Invalid tower format");
    }

    if (storedTower.length() > 1) {
      return new Tower(storedTower);
    }
    else {
      return null;
    }
  }

  private String readOnlineId(String storedOnlineId) throws IOException
  {
    if (!storedOnlineId.startsWith("_")) {
      throw new IOException("Invalid online ID format");
    }

    if (storedOnlineId.length() > 1) {
      return storedOnlineId.substring(1);
    }
    else {
      return null;
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

  public void setOnlineId(String onlineId)
  {
    if (this.onlineId != null && onlineId != null) {
      throw new RuntimeException("Online ID is already set");
    }
    else if (this.onlineId == null && onlineId == null) {
      throw new RuntimeException("Online ID is already clear");
    }
    this.onlineId = onlineId;
  }

  public List<LevelAchievement> getLevelAchievements()
  {
    return levelAchievements;
  }

  public Tower getTower()
  {
    return tower;
  }

  public List<String> getSkippedLevels()
  {
    return skippedLevels;
  }

  public boolean hasFlag(int flag)
  {
    return (flags & flag) != 0;
  }

  // TODO use toData??
  public String getData()
  {
    return data;
  }

  @Override
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

  public String toData()
  {
    StringBuilder data = new StringBuilder();
    data.append(name).append(",");
    data.append(flags).append(",");
    data.append(playTime).append(",");
    data.append(levels).append(",");

    for (LevelAchievement achievement : levelAchievements) {
      data.append(achievement.getLevelId()).append(",");
      data.append(achievement.getMostBalls()).append(",");
      data.append(achievement.getLeastMoves()).append(",");
      data.append(achievement.getLeastTime()).append(",");
    }
    data.append(skippedLevels.size()).append(",");
    for (String skippedLevel : skippedLevels) {
      data.append(skippedLevel).append(",");
    }
    data.append("_");
    if (tower != null) {
      data.append(tower.toData());
    }
    data.append(",");
    data.append("_");
    if (onlineId != null) {
      data.append(onlineId);
    }
    data.append(",");
    data.append(newBalls);
    return data.toString();
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral"})
  public static void main(String[] args) throws IOException
  {
    DebugUtil.setAllLogging();

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

    // This guy had an unparseable profile "java.lang.NumberFormatException: For input string: "HTInnovationCommittee"
    Profile jonas = new Profile("jonas,31,25383,46,GoingUp,11,3,21,EconomicDivide,16,8,35,HangLow,15,11,95,ImpaleSticky,34,26,179,IvyTower,11,92,303,FlyingMachine,6,9,25,FistyReachesOut,10,36,173,TowerOfGoo,42,66,274,Tumbler,25,40,109,Chain,25,84,288,OdeToBridgeBuilder,35,29,158,RegurgitationPumpingStation,0,121,564,Drool,22,19,99,FlyAwayLittleOnes,8,143,287,ImmigrationNaturalizationUnit,60,59,475,BeautySchool,18,6,28,LeapHole,8,9,16,VolcanicPercolatorDaySpa,18,60,284,BeautyAndTheTentacle,19,39,191,Whistler,26,2,24,RedCarpet,18,75,229,GeneticSortingMachine,17,63,286,BurningMan,6,19,160,SecondHandSmoke,16,13,74,ThirdWheel,19,35,181,SuperFuseChallengeTime,20,22,99,UpperShaft,20,72,405,WaterLock,44,23,186,YouHaveToExplodeTheHead,14,86,435,IncinerationDestination,16,40,531,ProductLauncher,0,24,190,HelloWorld,9,12,50,BulletinBoardSystem,11,42,189,GrapeVineVirus,11,66,190,GraphicProcessingUnit,8,30,207,RoadBlocks,12,90,589,GracefulFailure,7,43,112,AB3,41,17,156,MOM,0,61,547,Deliverance,0,65,379,InfestyTheWorm,7,101,544,BlusteryDay,17,48,198,MistysLongBonyRoad,23,50,245,TheServer,5,31,242,WeatherVane,24,88,320,ObservatoryObservationStation,0,12,96,1,HTInnovationCommittee,_b:Drained:-551.97:811.85:-0.87:-0.44:b:Drained:-875.95:64.04:0.11:-0.14:b:Drained:-1495.10:251.22:0.35:-1.97:b:Drained:-2115.88:461.86:-1.64:1.14:b:Drained:-279.86:339.07:0.31:-0.62:b:Drained:-2047.46:415.85:1.67:-1.11:b:Drained:-1047.97:621.48:-0.63:-0.11:b:Drained:-1287.82:251.34:0.14:-0.53:b:Drained:-2184.47:545.81:1.76:0.95:b:Drained:-2155.64:561.34:1.76:0.95:b:Drained:-1685.95:403.05:1.81:-0.85:b:Drained:-2104.15:548.70:-0.09:2.00:b:Drained:-1935.54:234.87:1.92:-0.55:b:Drained:-1849.72:386.15:1.53:1.29:b:Drained:-222.04:151.81:1.80:0.69:b:Drained:-2044.39:201.50:1.50:1.32:b:Drained:-1594.82:172.97:0.21:-0.35:b:Drained:-1246.23:106.79:0.23:-0.26:b:Drained:-1891.06:458.18:1.62:1.18:b:Drained:-1630.82:508.38:1.90:-0.63:b:Drained:-2072.35:464.33:1.79:0.90:b:Drained:-618.42:539.62:-0.45:-0.08:b:Drained:-149.10:264.24:0.72:-0.48:b:Drained:-1778.42:448.38:-0.40:1.96:b:Drained:-1927.01:431.98:1.62:1.18:b:Drained:-587.16:688.85:-0.67:-0.24:b:Drained:-1949.29:24.18:-1.99:0.20:b:Drained:-1750.18:311.63:0.34:0.11:b:Drained:-1808.72:15.71:0.14:-0.00:b:Drained:-1980.41:84.61:-1.30:-1.52:b:Drained:-637.77:62.24:0.19:-0.02:b:Drained:-1007.76:15.91:0.06:-0.00:b:Drained:-1165.10:700.36:-0.83:-0.75:b:Drained:-833.24:466.72:-0.72:0.44:b:Drained:-1399.35:439.02:-0.03:-0.64:b:Drained:-1184.30:311.28:0.14:-0.70:b:Drained:-522.82:12.15:0.06:0.00:b:Drained:-1956.89:286.08:1.34:1.49:b:Drained:-828.70:577.41:-0.61:0.44:b:Drained:-1932.95:427.65:1.62:1.18:b:Drained:-1992.10:305.79:0.10:-2.00:b:Drained:-936.21:533.38:-0.65:0.26:b:Drained:-1535.10:476.34:-0.26:-0.21:b:Drained:-1670.08:521.80:-0.25:0.49:b:Drained:-1996.71:420.17:-0.03:2.00:b:Drained:-271.61:205.65:0.78:-0.55:b:Drained:-2073.48:433.16:-1.67:1.11:b:Drained:-1420.07:563.80:-0.06:-0.84:b:Drained:-944.36:423.67:-0.58:0.08:b:Drained:-1774.04:426.90:-0.40:1.96:b:Drained:-1553.72:600.84:0.30:-1.98:b:Drained:-2045.81:414.76:1.67:-1.11:b:Drained:-2146.84:483.41:1.64:-1.14:b:Drained:-1838.22:227.96:1.45:1.38:b:Drained:-2206.52:533.94:-1.76:-0.95:b:Drained:-1906.06:467.58:-1.88:0.69:b:Drained:-186.35:17.79:-0.04:0.00:b:Drained:-707.09:636.28:-0.55:0.13:b:Drained:-431.66:862.56:-1.57:-0.15:b:Drained:-1726.92:245.64:0.67:-1.88:b:Drained:-291.88:98.34:0.58:-0.14:b:Drained:-716.23:387.12:-0.43:0.11:b:Drained:-1915.91:344.88:0.48:1.94:b:Drained:-358.53:790.04:-1.36:0.06:b:Drained:-290.41:41.48:0.46:1.25:b:Drained:-1025.35:746.93:-1.29:0.20:b:Drained:-150.80:118.94:1.29:-0.42:b:Drained:-1817.23:247.96:1.45:1.38:b:Drained:-1653.32:422.04:0.33:-1.97:b:Drained:-1715.81:215.70:0.43:-0.09:b:Drained:-401.70:162.97:0.43:-0.19:b:Drained:-1903.84:394.17:0.48:1.94:b:Drained:-1882.45:127.67:-1.99:0.20:b:Drained:-2207.06:525.33:-1.64:1.14:b:Drained:-475.55:738.67:-0.88:-0.34:b:Drained:-1776.27:313.84:1.99:-0.18:b:Drained:-2021.15:490.00:-1.79:-0.90:b:Drained:-1880.82:219.34:1.92:-0.55:b:Drained:-1755.06:333.80:0.40:-1.96:b:Drained:-375.39:655.95:-0.34:-0.01:b:Drained:-1300.89:630.30:-0.09:-1.19:b:Drained:-1893.83:456.17:1.62:1.18:b:Drained:-858.24:203.15:0.15:0.09:b:Drained:-1784.49:115.83:-1.97:0.32:b:Drained:-501.25:617.62:-0.17:-0.34:b:Drained:-1570.68:488.43:1.90:-0.63:b:Drained:-1887.54:460.74:0.00:-0.00:b:Drained:-1988.27:249.40:0.92:-0.69:b:Drained:-2067.51:172.53:-0.57:1.92:b:Drained:-1817.83:451.63:-1.98:0.26:b:Drained:-1376.84:185.99:-0.01:-0.40:b:Drained:-1969.44:204.45:0.79:-1.84:b:Drained:-2061.88:552.75:-1.56:1.25:b:Drained:-1892.32:441.23:0.48:1.94:b:Drained:-1752.04:214.82:2.00:0.05:b:Drained:-2040.39:535.50:1.56:-1.25:b:Drained:-2058.11:189.41:-1.50:-1.32:b:Drained:-1946.54:136.01:-1.89:0.66:b:Drained:-512.35:99.61:0.25:-0.17:b:Drained:-308.14:884.42:-1.93:-0.00:b:Drained:-294.92:12.71:-0.00:0.00:b:Drained:-1995.03:380.78:1.03:-0.89:b:Drained:-1832.82:400.36:-1.53:-1.29:b:Drained:-97.48:887.35:-1.78:1.40:b:Drained:-952.97:292.67:-0.10:-0.01:b:Drained:-2025.27:31.39:0.51:-0.50:b:Drained:-1799.45:428.41:1.53:1.29:b:Drained:-1937.82:132.61:0.78:-0.60:b:Drained:-1778.04:446.83:-0.02:0.41:b:Drained:-813.69:717.66:-0.89:0.52:b:Drained:-1944.15:237.31:1.92:-0.55:b:Drained:-1555.02:611.44:1.57:1.23:b:Drained:-1883.10:323.44:1.99:-0.18:b:Drained:-1904.47:391.59:-0.48:-1.94:b:Drained:-1926.44:79.52:-0.44:1.95:b:Drained:-1704.14:102.64:0.35:-0.16:b:Drained:-1476.79:17.15:-0.06:-0.00:b:Drained:-1513.75:355.08:-0.04:-0.36:b:Drained:-64.06:609.87:-0.70:0.94:b:Drained:-1799.04:213.53:2.00:0.05:b:Drained:-396.16:402.59:-0.03:-0.57:b:Drained:-1721.07:229.29:0.67:-1.88:b:Drained:-614.17:312.85:-0.24:-0.16:b:Drained:-1799.36:138.85:-1.47:-1.36:b:Drained:-1939.04:133.37:1.89:-0.66:b:Drained:-1652.80:418.95:0.33:-1.97:b:Drained:-1891.00:44.00:-1.37:-1.46:b:Drained:-1506.66:315.98:0.35:-1.97:b:Drained:-726.56:266.99:-0.18:-0.06:b:Drained:-2068.56:179.03:0.97:-0.32:b:Drained:-1495.11:250.66:-0.05:-0.35:b:Drained:-1912.73:20.01:0.35:-0.51:b:Drained:-1551.14:463.23:1.55:1.27:b:Drained:-934.43:663.57:-0.87:0.44:b:Drained:-1573.22:489.28:-1.90:0.63:b:Drained:-1998.40:500.64:-0.29:-0.92:b:Drained:-1925.29:74.47:-0.44:1.95:b:Drained:-280.28:588.37:-0.03:-0.05:b:Drained:-1929.96:132.36:1.99:-0.20:b:Drained:-216.29:810.71:-1.31:0.63:b:Drained:-218.71:946.88:-2.30:0.34:b:Drained:-1873.72:365.98:-1.53:-1.29:b:Drained:-1775.52:434.17:-0.40:1.96:b:Drained:-1994.50:351.89:0.10:-2.00:b:Drained:-1124.19:93.25:0.33:-0.38:b:Drained:-75.57:211.01:1.69:0.21:b:Drained:-2030.29:47.09:0.57:-1.92:b:Drained:-1854.23:211.59:0.73:-0.42:b:Drained:-1919.54:326.33:0.80:-0.46:b:Drained:-756.79:16.80:0.18:0.00:b:Drained:-1909.74:308.18:0.99:-1.74:b:Drained:-101.97:1026.42:-3.55:1.74:b:Drained:-1780.99:282.49:-1.45:-1.38:b:Drained:-503.28:344.33:-0.15:-0.37:b:Drained:-2099.16:448.28:0.56:-2.33:b:Drained:-160.98:536.45:-0.24:0.31:b:Drained:-1708.64:142.79:0.21:-1.99:b:Drained:-1188.74:199.58:0.50:-0.49:b:Drained:-85.41:749.78:-0.94:1.29:b:Drained:-1177.79:445.07:-0.13:-0.76:b:Drained:-1358.69:54.67:0.21:-0.31:b:Drained:-512.14:218.14:0.01:-0.36:b:Drained:-1812.15:213.18:2.00:0.05:b:Drained:-394.26:544.48:0.03:-0.34:b:Drained:-1969.52:204.64:-0.79:1.84:b:Drained:-1403.87:322.49:0.01:-0.45:b:Drained:-1817.76:121.01:0.53:-0.33:b:Drained:-1998.02:496.44:-0.03:2.00:b:Drained:-2005.38:497.91:-1.79:-0.90:b:Drained:-2213.91:526.02:-0.12:-4.00:b:Drained:-1810.12:29.75:-0.18:1.99:b:Drained:-1593.40:581.31:1.57:1.23:b:Drained:-512.65:477.72:-0.15:-0.28:b:Drained:-2121.54:579.71:-1.76:-0.95:b:Drained:-1555.25:611.12:0.05:-0.10:b:Drained:-1902.79:398.49:-0.48:-1.94:b:Drained:-914.38:780.32:-1.12:0.57:b:Drained:-1604.34:43.30:0.10:-0.56:b:Drained:-1278.45:374.56:0.11:-0.74:b:Drained:-189.82:677.19:-0.63:0.79:b:Drained:-723.21:486.35:-0.52:0.17:b:Drained:-979.02:143.20:0.42:-0.19:b:Drained:-1171.37:569.04:-0.18:-0.81:b:Drained:-1491.47:126.61:0.12:-0.32:b:Drained:-1885.62:220.71:-1.92:0.55:b:Drained:-411.08:36.45:0.29:-0.08:b:Drained:-1812.23:317.07:1.99:-0.18:b:Drained:-1790.75:146.82:1.47:1.36:b:Drained:-2048.66:542.14:1.56:-1.25:b:Drained:-806.99:861.55:-1.11:0.73:b:Drained:-619.10:178.92:-0.04:-0.21:b:Drained:-838.54:344.31:-0.38:0.31:b:Drained:-1734.57:476.50:1.64:1.14:b:Drained:-1743.43:470.37:1.64:1.14:b:Drained:-2107.10:585.86:-1.16:-2.25:b:Drained:-1054.61:485.93:-0.40:-0.46:b:Drained:-1588.62:371.90:1.95:-0.43:b:Drained:-1438.92:676.95:0.45:-0.99:b:Drained:-2153.17:562.67:1.76:0.95:b:Drained:-614.93:440.84:-0.25:-0.07:b:Drained:-1995.11:28.79:-1.99:0.20:b:Drained:-278.77:460.25:-0.01:-0.32:b:Drained:-1622.90:379.43:1.95:-0.43:b:Drained:-1070.66:233.02:0.38:-0.41:b:Drained:-2070.66:178.37:-1.50:-1.32:b:Drained:-1647.20:384.93:-0.08:0.17:b:Drained:-395.39:274.29:0.25:-0.47:b:Drained:-1848.74:196.63:0.75:-1.85:b:Drained:-705.80:772.85:-0.96:0.18:b:Drained:-749.49:133.33:0.06:-0.28:b:Drained:-1296.45:499.40:-0.02:-0.91:b:Drained:-2114.73:583.38:-1.76:-0.95:b:Drained:-1964.09:404.96:1.62:1.18:b:Drained:-2013.05:392.97:1.67:-1.11:b:Drained:-1896.89:285.62:0.99:-1.74:b:Drained:-1619.95:271.77:0.08:-0.06:b:Drained:-1783.93:279.69:-1.45:-1.38:b:Drained:-1996.81:425.94:-0.03:2.00:b:Drained:-1071.18:366.64:-0.25:-0.50:b:Drained:-1914.47:20.67:1.99:-0.20:b:Drained:-49.26:349.78:0.05:0.51:b:Drained:-52.68:463.13:-0.41:0.64:b:Drained:-150.80:399.01:0.01:-0.17:b:Drained:-1608.84:227.72:0.49:-1.94:b:Drained:-1996.29:395.45:-0.03:2.00:b:Drained:-1616.25:256.94:0.49:-1.94:b:Drained:-293.64:713.30:-1.02:0.80:s:Drained:220:221:9.0000:110.00:0:s:Drained:221:222:9.0000:114.13:0:s:Drained:222:220:9.0000:114.13:0:s:Drained:22:220:9.0000:129.65:0:s:Drained:22:222:9.0000:135.97:0:s:Drained:145:22:9.0000:100.00:0:s:Drained:145:220:9.0000:140.00:0:s:Drained:66:145:9.0000:133.77:0:s:Drained:66:22:9.0000:140.00:0:s:Drained:14:66:9.0000:100.00:0:s:Drained:14:145:9.0000:139.63:0:s:Drained:56:66:9.0000:121.88:0:s:Drained:56:14:9.0000:124.95:0:s:Drained:64:56:9.0000:106.65:0:s:Drained:64:14:9.0000:128.15:0:s:Drained:155:221:9.0000:133.60:0:s:Drained:155:222:9.0000:140.00:0:s:Drained:118:155:9.0000:127.42:0:s:Drained:118:221:9.0000:140.00:0:s:Drained:179:118:9.0000:136.45:0:s:Drained:179:155:9.0000:140.00:0:s:Drained:158:179:9.0000:131.28:0:s:Drained:158:118:9.0000:140.00:0:s:Drained:137:155:9.0000:138.63:0:s:Drained:137:179:9.0000:140.00:0:s:Drained:201:137:9.0000:137.48:0:s:Drained:201:155:9.0000:140.00:0:s:Drained:201:222:9.0000:141.64:1:s:Drained:4:201:9.0000:128.62:0:s:Drained:4:222:9.0000:140.00:0:s:Drained:4:22:9.0000:152.52:1:s:Drained:45:4:9.0000:140.00:0:s:Drained:45:22:9.0000:140.00:0:s:Drained:66:45:9.0000:141.90:1:s:Drained:60:45:9.0000:122.17:0:s:Drained:60:66:9.0000:132.26:0:s:Drained:56:60:9.0000:139.09:1:s:Drained:70:60:9.0000:132.68:0:s:Drained:70:45:9.0000:132.89:0:s:Drained:206:70:9.0000:119.91:0:s:Drained:206:45:9.0000:139.63:0:s:Drained:206:4:9.0000:132.03:1:s:Drained:120:4:9.0000:131.93:0:s:Drained:120:201:9.0000:132.43:0:s:Drained:120:206:9.0000:133.43:1:s:Drained:163:137:9.0000:131.03:0:s:Drained:163:201:9.0000:140.00:0:s:Drained:163:120:9.0000:144.46:1:s:Drained:79:163:9.0000:111.43:0:s:Drained:79:137:9.0000:127.37:0:s:Drained:84:79:9.0000:131.51:0:s:Drained:84:163:9.0000:131.60:0:s:Drained:74:84:9.0000:121.67:0:s:Drained:74:79:9.0000:131.41:0:s:Drained:25:84:9.0000:113.67:0:s:Drained:25:74:9.0000:119.79:0:s:Drained:172:120:9.0000:140.00:0:s:Drained:172:163:9.0000:140.00:0:s:Drained:172:84:9.0000:139.59:1:s:Drained:153:120:9.0000:121.64:0:s:Drained:153:206:9.0000:128.84:0:s:Drained:153:172:9.0000:135.18:1:s:Drained:161:70:9.0000:123.43:0:s:Drained:161:206:9.0000:125.56:0:s:Drained:161:153:9.0000:130.37:1:s:Drained:98:161:9.0000:123.60:0:s:Drained:98:70:9.0000:127.87:0:s:Drained:185:98:9.0000:117.44:0:s:Drained:185:70:9.0000:131.57:0:s:Drained:60:185:9.0000:125.15:1:s:Drained:100:60:9.0000:100.00:0:s:Drained:100:56:9.0000:111.44:0:s:Drained:100:185:9.0000:121.72:1:s:Drained:36:98:9.0000:100.00:0:s:Drained:36:185:9.0000:111.18:0:s:Drained:30:98:9.0000:123.67:0:s:Drained:30:36:9.0000:129.10:0:s:Drained:190:30:9.0000:118.84:0:s:Drained:190:98:9.0000:138.03:0:s:Drained:190:161:9.0000:109.07:1:s:Drained:122:153:9.0000:112.90:0:s:Drained:122:161:9.0000:140.00:0:s:Drained:190:122:9.0000:137.40:1:s:Drained:128:122:9.0000:118.73:0:s:Drained:128:190:9.0000:140.00:0:s:Drained:21:172:9.0000:125.58:0:s:Drained:21:84:9.0000:140.00:0:s:Drained:199:21:9.0000:100.00:0:s:Drained:199:172:9.0000:110.88:0:s:Drained:199:153:9.0000:149.64:1:s:Drained:199:122:9.0000:130.33:1:s:Drained:25:21:9.0000:151.29:1:s:Drained:57:25:9.0000:132.59:0:s:Drained:57:21:9.0000:135.74:0:s:Drained:180:21:9.0000:116.26:0:s:Drained:180:199:9.0000:121.07:0:s:Drained:61:180:9.0000:100.00:0:s:Drained:61:199:9.0000:114.89:0:s:Drained:61:122:9.0000:126.75:1:s:Drained:61:128:9.0000:121.31:1:s:Drained:209:30:9.0000:133.73:0:s:Drained:209:190:9.0000:136.69:0:s:Drained:209:128:9.0000:140.83:1:s:Drained:57:180:9.0000:151.90:1:s:Drained:82:209:9.0000:135.29:0:s:Drained:82:128:9.0000:140.00:0:s:Drained:1:209:9.0000:138.07:0:s:Drained:1:82:9.0000:140.00:0:s:Drained:149:209:9.0000:128.22:0:s:Drained:149:1:9.0000:131.31:0:s:Drained:30:149:9.0000:122.36:1:s:Drained:38:57:9.0000:134.42:0:s:Drained:38:180:9.0000:140.00:0:s:Drained:109:57:9.0000:140.00:0:s:Drained:109:38:9.0000:140.00:0:s:Drained:208:109:9.0000:116.20:0:s:Drained:208:57:9.0000:135.83:0:s:Drained:25:208:9.0000:143.44:1:s:Drained:191:61:9.0000:130.39:0:s:Drained:191:128:9.0000:140.00:0:s:Drained:180:191:9.0000:181.88:1:s:Drained:33:38:9.0000:112.22:0:s:Drained:33:180:9.0000:114.59:0:s:Drained:33:191:9.0000:124.66:1:s:Drained:191:82:9.0000:141.62:1:s:Drained:181:82:9.0000:129.74:0:s:Drained:181:1:9.0000:133.05:0:s:Drained:104:191:9.0000:128.98:0:s:Drained:104:82:9.0000:135.32:0:s:Drained:48:33:9.0000:122.00:0:s:Drained:48:191:9.0000:132.78:0:s:Drained:41:48:9.0000:111.50:0:s:Drained:41:33:9.0000:122.87:0:s:Drained:133:109:9.0000:131.32:0:s:Drained:133:38:9.0000:138.66:0:s:Drained:41:38:9.0000:113.83:1:s:Drained:133:41:9.0000:132.66:1:s:Drained:104:48:9.0000:132.40:1:s:Drained:181:104:9.0000:148.51:1:s:Drained:203:104:9.0000:140.00:0:s:Drained:203:181:9.0000:140.00:0:s:Drained:218:203:9.0000:133.61:0:s:Drained:218:104:9.0000:140.00:0:s:Drained:218:48:9.0000:143.87:1:s:Drained:195:218:9.0000:122.45:0:s:Drained:195:48:9.0000:125.78:0:s:Drained:195:41:9.0000:126.80:1:s:Drained:6:133:9.0000:119.90:0:s:Drained:6:41:9.0000:140.00:0:s:Drained:195:6:9.0000:138.49:1:s:Drained:65:133:9.0000:125.53:0:s:Drained:65:6:9.0000:127.36:0:s:Drained:176:65:9.0000:105.07:0:s:Drained:176:133:9.0000:120.09:0:s:Drained:176:109:9.0000:120.02:1:s:Drained:31:1:9.0000:135.68:0:s:Drained:31:181:9.0000:137.06:0:s:Drained:144:31:9.0000:140.00:0:s:Drained:144:181:9.0000:140.00:0:s:Drained:203:144:9.0000:142.45:1:s:Drained:157:144:9.0000:138.73:0:s:Drained:157:203:9.0000:140.00:0:s:Drained:35:157:9.0000:125.80:0:s:Drained:35:203:9.0000:140.00:0:s:Drained:35:218:9.0000:134.36:1:s:Drained:159:218:9.0000:130.30:0:s:Drained:159:195:9.0000:131.12:0:s:Drained:182:6:9.0000:130.93:0:s:Drained:182:195:9.0000:140.00:0:s:Drained:182:159:9.0000:131.41:1:s:Drained:159:35:9.0000:142.10:1:s:Drained:32:182:9.0000:136.52:0:s:Drained:32:6:9.0000:140.00:0:s:Drained:65:32:9.0000:137.50:1:s:Drained:80:32:9.0000:140.00:0:s:Drained:80:182:9.0000:140.00:0:s:Drained:210:80:9.0000:132.27:0:s:Drained:210:182:9.0000:139.57:0:s:Drained:210:159:9.0000:128.81:1:s:Drained:178:35:9.0000:120.32:0:s:Drained:178:159:9.0000:124.87:0:s:Drained:178:210:9.0000:129.93:1:s:Drained:7:35:9.0000:123.69:0:s:Drained:7:178:9.0000:125.50:0:s:Drained:7:157:9.0000:128.81:1:s:Drained:165:178:9.0000:140.00:0:s:Drained:165:7:9.0000:140.00:0:s:Drained:34:165:9.0000:116.39:0:s:Drained:34:178:9.0000:140.00:0:s:Drained:210:34:9.0000:113.92:1:s:Drained:47:34:9.0000:128.74:0:s:Drained:47:210:9.0000:140.00:0:s:Drained:47:80:9.0000:123.79:1:s:Drained:42:47:9.0000:134.94:0:s:Drained:42:34:9.0000:140.00:0:s:Drained:117:42:9.0000:125.59:0:s:Drained:117:34:9.0000:140.00:0:s:Drained:165:117:9.0000:114.42:1:s:Drained:205:117:9.0000:134.28:0:s:Drained:205:42:9.0000:137.48:0:s:Drained:139:179:9.0000:138.37:0:s:Drained:139:158:9.0000:140.00:0:s:Drained:103:139:9.0000:140.00:0:s:Drained:103:158:9.0000:140.00:0:s:Drained:63:74:9.0000:118.39:0:s:Drained:63:79:9.0000:140.00:0:s:Drained:79:179:9.0000:178.44:1:s:Drained:63:139:9.0000:138.81:1:s:Drained:63:179:9.0000:197.26:1:s:Drained:226:79:9.0000:100.00:0:s:Drained:226:63:9.0000:100.00:0:s:Drained:139:226:9.0000:124.90:1:s:Drained:140:103:9.0000:134.89:0:s:Drained:140:139:9.0000:137.42:0:s:Drained:151:140:9.0000:140.00:0:s:Drained:151:103:9.0000:140.00:0:s:Drained:99:63:9.0000:107.54:0:s:Drained:99:139:9.0000:116.61:0:s:Drained:99:140:9.0000:106.26:1:s:Drained:58:63:9.0000:100.00:0:s:Drained:58:99:9.0000:121.09:0:s:Drained:74:58:9.0000:136.53:1:s:Drained:0:74:9.0000:100.95:0:s:Drained:0:58:9.0000:121.57:0:s:Drained:0:208:9.0000:145.37:1:s:Drained:25:0:9.0000:131.04:1:s:Drained:176:208:9.0000:199.38:1:s:Drained:189:176:9.0000:132.30:0:s:Drained:189:208:9.0000:133.11:0:s:Drained:189:109:9.0000:147.06:1:s:Drained:90:7:9.0000:126.28:0:s:Drained:90:165:9.0000:134.50:0:s:Drained:17:90:9.0000:136.50:0:s:Drained:17:7:9.0000:140.00:0:s:Drained:17:157:9.0000:128.21:1:s:Drained:160:17:9.0000:128.81:0:s:Drained:160:90:9.0000:140.00:0:s:Drained:183:90:9.0000:137.28:0:s:Drained:183:160:9.0000:137.65:0:s:Drained:130:117:9.0000:107.06:0:s:Drained:130:165:9.0000:124.10:0:s:Drained:130:90:9.0000:127.46:1:s:Drained:183:130:9.0000:135.82:1:s:Drained:116:183:9.0000:138.65:0:s:Drained:116:160:9.0000:140.00:0:s:Drained:177:183:9.0000:134.67:0:s:Drained:177:116:9.0000:140.00:0:s:Drained:16:183:9.0000:116.88:0:s:Drained:16:130:9.0000:124.61:0:s:Drained:215:16:9.0000:104.27:0:s:Drained:215:130:9.0000:127.99:0:s:Drained:215:117:9.0000:135.71:1:s:Drained:205:215:9.0000:116.24:1:s:Drained:177:16:9.0000:129.84:1:s:Drained:115:177:9.0000:122.34:0:s:Drained:115:16:9.0000:131.83:0:s:Drained:115:215:9.0000:184.04:1:s:Drained:69:215:9.0000:116.41:0:s:Drained:69:115:9.0000:118.31:0:s:Drained:205:69:9.0000:179.59:1:s:Drained:27:69:9.0000:106.58:0:s:Drained:27:205:9.0000:123.68:0:s:Drained:147:27:9.0000:140.00:0:s:Drained:147:69:9.0000:140.00:0:s:Drained:166:147:9.0000:106.42:0:s:Drained:166:69:9.0000:140.00:0:s:Drained:166:115:9.0000:119.67:1:s:Drained:107:147:9.0000:110.75:0:s:Drained:107:166:9.0000:124.71:0:s:Drained:87:107:9.0000:128.51:0:s:Drained:87:147:9.0000:140.00:0:s:Drained:131:107:9.0000:116.19:0:s:Drained:131:166:9.0000:136.10:0:s:Drained:105:131:9.0000:115.64:0:s:Drained:105:107:9.0000:131.92:0:s:Drained:129:87:9.0000:105.70:0:s:Drained:129:107:9.0000:140.00:0:s:Drained:129:105:9.0000:154.19:1:s:Drained:28:131:9.0000:109.12:0:s:Drained:28:166:9.0000:117.05:0:s:Drained:28:115:9.0000:135.43:1:s:Drained:148:87:9.0000:100.00:0:s:Drained:148:147:9.0000:140.00:0:s:Drained:148:27:9.0000:171.90:1:s:Drained:108:27:9.0000:140.00:0:s:Drained:108:205:9.0000:140.00:0:s:Drained:148:108:9.0000:182.06:1:s:Drained:43:42:9.0000:140.00:0:s:Drained:43:205:9.0000:140.00:0:s:Drained:43:108:9.0000:125.05:1:s:Drained:174:43:9.0000:140.00:0:s:Drained:174:42:9.0000:140.00:0:s:Drained:174:47:9.0000:140.02:1:s:Drained:197:47:9.0000:118.30:0:s:Drained:197:174:9.0000:130.65:0:s:Drained:80:197:9.0000:142.15:1:s:Drained:101:148:9.0000:100.00:0:s:Drained:101:87:9.0000:133.34:0:s:Drained:86:101:9.0000:131.68:0:s:Drained:86:148:9.0000:140.00:0:s:Drained:86:108:9.0000:104.59:1:s:Drained:135:86:9.0000:113.75:0:s:Drained:135:101:9.0000:122.82:0:s:Drained:154:135:9.0000:110.55:0:s:Drained:154:101:9.0000:129.03:0:s:Drained:194:135:9.0000:136.72:0:s:Drained:194:154:9.0000:140.00:0:s:Drained:169:194:9.0000:121.35:0:s:Drained:169:154:9.0000:140.00:0:s:Drained:144:17:9.0000:100.00:1,_SECRET,10");
    System.out.println("jonas = " + jonas);
    System.out.println("jonas.levelAchievements = " + jonas.levelAchievements);
    System.out.println("jonas.tower = " + jonas.tower);
  }
}
