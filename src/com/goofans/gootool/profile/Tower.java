/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.profile;

import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.DecimalFormat;

/**
 * A user's World of Goo Corporation tower.
 * 
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class Tower
{
  private static final Logger log = Logger.getLogger(Tower.class.getName());

  private static final String TOWERELEMENT_BALL = "b";
  private static final String TOWERELEMENT_STRAND = "s";

  private final List<Ball> balls;
  private final List<Strand> strands;

  private transient double height;
  private transient int usedStrandBalls;
  private transient int usedNodeBalls;
  private transient int totalBalls;
  private static final String TOWER_SEP = ":";

  public Tower(String towerStr) throws IOException
  {
    log.fine("Constructing tower from " + towerStr);

    if (towerStr.charAt(0) == '_') {
      towerStr = towerStr.substring(1);
    }

    balls = new ArrayList<Ball>(300);
    strands = new ArrayList<Strand>(300);

    usedStrandBalls = 0;
    totalBalls = 0;

    StringTokenizer tok = new StringTokenizer(towerStr, TOWER_SEP);
    while (tok.hasMoreTokens()) {
      String type = tok.nextToken();
      if (type.equals(TOWERELEMENT_BALL)) {
        Ball ball = new Ball();
        ball.ballType = tok.nextToken();
        ball.xPos = Double.parseDouble(tok.nextToken());
        ball.yPos = Double.parseDouble(tok.nextToken());
        ball.xMomentum = Double.parseDouble(tok.nextToken());
        ball.yMomentum = Double.parseDouble(tok.nextToken());
        balls.add(ball);
        totalBalls++;
      }
      else if (type.equals(TOWERELEMENT_STRAND)) {
        Strand strand = new Strand();
        strand.strandType = tok.nextToken();
        strand.firstBall = balls.get(Integer.parseInt(tok.nextToken()));
        strand.secondBall = balls.get(Integer.parseInt(tok.nextToken()));
        strand.connectionStrength = Double.parseDouble(tok.nextToken());
        strand.length = Double.parseDouble(tok.nextToken());
        strand.ballUsed = "1".equals(tok.nextToken());
        if (strand.ballUsed) {
          usedStrandBalls++;
          totalBalls++;
        }
        strands.add(strand);

        strand.firstBall.inStructure = true;
        strand.secondBall.inStructure = true;
      }
      else {
        throw new IOException("Invalid tower element type " + type);
      }
    }

    // Only use attached balls to calculate height
    height = 0;
    usedNodeBalls = 0;
    for (Ball ball : balls) {
      if (ball.inStructure) {
        usedNodeBalls++;
        if (ball.yPos > height) height = ball.yPos;
      }
    }

    height /= 100;
  }

  public class Ball
  {
    public String ballType;
    public double xPos;
    public double yPos;
    public double xMomentum;
    public double yMomentum;
    public boolean inStructure;
  }

  public class Strand
  {
    public String strandType;
    public Ball firstBall;
    public Ball secondBall;
    public double connectionStrength;
    public double length;
    public boolean ballUsed;
  }

  public List<Ball> getBalls()
  {
    return balls;
  }

  public List<Strand> getStrands()
  {
    return strands;
  }

  public double getHeight()
  {
    return height;
  }

  public int getUsedStrandBalls()
  {
    return usedStrandBalls;
  }

  public int getUsedNodeBalls()
  {
    return usedNodeBalls;
  }

  // Total balls, including those not connected
  public int getTotalBalls()
  {
    return totalBalls;
  }

  @Override
  @SuppressWarnings({"StringConcatenation"})
  public String toString()
  {
    return "Tower with " + totalBalls + " balls and " + strands.size() + " strands, used " + usedNodeBalls + " node and " + usedStrandBalls + " strand balls to make height " + height;
  }

  public String toData()
  {
    StringBuilder data = new StringBuilder();

    NumberFormat nf2dp = new DecimalFormat("0.00");
    NumberFormat nf4dp = new DecimalFormat("0.0000");

    for (Ball ball : balls) {
      if (data.length() > 0) data.append(TOWER_SEP);
      data.append(TOWERELEMENT_BALL).append(TOWER_SEP);
      data.append(ball.ballType).append(TOWER_SEP);
      data.append(nf2dp.format(ball.xPos)).append(TOWER_SEP);
      data.append(nf2dp.format(ball.yPos)).append(TOWER_SEP);
      data.append(nf2dp.format(ball.xMomentum)).append(TOWER_SEP);
      data.append(nf2dp.format(ball.yMomentum));
    }

    for (Strand strand : strands) {
      if (data.length() > 0) data.append(TOWER_SEP);
      data.append(TOWERELEMENT_STRAND).append(TOWER_SEP);
      data.append(strand.strandType).append(TOWER_SEP);
      data.append(balls.indexOf(strand.firstBall)).append(TOWER_SEP);
      data.append(balls.indexOf(strand.secondBall)).append(TOWER_SEP);
      data.append(nf4dp.format(strand.connectionStrength)).append(TOWER_SEP);
      data.append(nf2dp.format(strand.length)).append(TOWER_SEP);
      data.append(strand.ballUsed ? "1" : "0");
    }

    return data.toString();
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
  public static void main(String[] args) throws IOException
  {
    // 8.1 metres, 295 ish balls
    Tower t = new Tower("_b:Drained:-919.68:408.96:0.03:0.01:b:Drained:-1861.54:131.05:0.01:-0.01:b:Drained:-1065.30:556.08:0.01:0.01:b:Drained:-1748.46:179.49:0.01:-0.00:b:Drained:-457.06:712.98:-0.00:0.00:b:Drained:-1766.93:40.95:0.01:-0.01:b:Drained:-715.67:346.38:0.01:-0.00:b:Drained:-1085.73:14.68:0.01:0.00:b:Drained:-986.20:655.48:0.02:-0.01:b:Drained:-686.37:95.81:0.00:0.01:b:Drained:-1290.29:237.87:-1.98:0.25:b:Drained:-2077.43:241.31:0.01:-0.00:b:Drained:-509.72:169.91:-0.01:-0.01:b:Drained:-446.44:478.36:0.01:-0.01:b:Drained:-511.37:411.27:0.00:0.01:b:Drained:-713.69:729.93:0.03:0.00:b:Drained:-767.40:15.06:-0.02:0.00:b:Drained:-298.73:602.88:-0.01:-0.02:b:Drained:-111.62:151.49:0.01:0.00:b:Drained:521.70:194.08:0.01:0.00:b:Drained:1917.25:15.54:0.74:0.00:b:Drained:-384.28:511.76:0.00:-0.01:b:Drained:-1312.95:487.43:0.02:0.01:b:Drained:-32.27:12.27:-0.01:-0.00:b:Drained:-1119.56:125.69:0.01:-0.00:b:Drained:-360.68:775.26:0.02:-0.00:b:Drained:-194.44:512.87:0.01:-0.01:b:Drained:-2203.18:38.03:0.01:0.00:b:Drained:-1547.11:371.88:0.02:-0.00:b:Drained:-297.63:16.37:-0.00:0.00:b:Drained:-555.72:199.20:0.01:-0.01:b:Drained:-30.98:442.31:-0.02:-0.01:b:Drained:-795.34:245.52:0.02:0.01:b:Drained:-96.95:274.60:-0.01:-0.00:b:Drained:-759.14:440.80:0.01:0.02:b:Drained:-1932.59:251.88:0.01:-0.00:b:Drained:-531.33:519.56:0.01:0.00:b:Drained:-348.06:451.52:0.00:-0.01:b:Drained:-446.42:209.88:0.02:-0.01:b:Drained:-900.61:160.01:0.01:-0.00:b:Drained:-495.66:108.98:0.01:-0.02:b:Drained:-1370.23:247.96:0.02:0.00:b:Drained:-439.06:372.20:0.01:-0.00:b:Drained:-1261.61:234.26:0.01:-0.00:b:Drained:-1236.23:116.85:0.01:-0.00:b:Drained:-1659.55:286.49:0.01:-0.01:b:Drained:-1219.20:17.38:0.01:0.00:b:Drained:-2138.99:139.66:0.02:0.00:b:Drained:-42.57:351.46:-0.00:-0.01:b:Drained:-1415.44:13.18:0.01:0.00:b:Drained:-319.46:512.54:0.02:-0.02:b:Drained:-640.77:207.12:-0.01:-0.01:b:Drained:-900.52:191.56:-0.00:0.03:b:Drained:-921.18:23.24:0.01:0.02:b:Drained:-135.15:353.59:-0.00:-0.04:b:Drained:-676.73:207.91:0.01:-0.00:b:Drained:-569.76:115.37:-0.01:-0.01:b:Drained:-1408.98:358.70:0.01:0.00:b:Drained:-849.11:326.40:-0.00:0.02:b:Drained:63.87:409.92:-0.03:-0.01:b:Drained:-409.27:101.42:-0.03:0.00:b:Drained:-2066.90:16.03:0.01:0.00:b:Drained:-763.54:146.81:0.01:0.00:b:Drained:109.40:74.84:0.59:1.91:b:Drained:144.40:467.24:-0.00:-0.02:b:Drained:400.27:260.61:0.01:-0.02:b:Drained:-1359.16:571.10:0.03:-0.00:b:Drained:-138.26:18.29:-0.01:-0.00:b:Drained:-246.32:699.88:0.01:-0.02:b:Drained:456.54:414.98:0.01:-0.01:b:Drained:-1589.52:497.00:0.01:0.01:b:Drained:-765.67:246.00:-0.00:0.01:b:Drained:-2339.34:297.60:0.01:-0.01:b:Drained:-677.64:441.76:0.01:0.00:b:Drained:84.08:218.15:-0.01:-0.01:b:Drained:341.45:390.70:-0.00:-0.03:b:Drained:-353.50:628.00:0.01:-0.02:b:Drained:221.55:50.36:0.01:-0.01:b:Drained:650.01:247.80:-0.00:-0.00:b:Drained:-476.41:422.37:0.02:0.01:b:Drained:363.36:513.08:-0.01:0.01:b:Drained:-1473.59:150.89:0.01:-0.00:b:Drained:-1301.27:371.76:0.00:0.01:b:Drained:-340.54:612.68:0.02:0.01:b:Drained:66.54:717.77:-0.20:1.99:b:Drained:-2220.69:236.50:0.01:0.00:b:Drained:-928.05:284.09:0.01:-0.00:b:Drained:-905.69:53.40:-0.01:0.03:b:Drained:343.89:128.95:0.02:-0.02:b:Drained:-1171.90:334.05:0.01:0.01:b:Drained:-1999.64:127.43:0.01:0.00:b:Drained:168.90:670.35:0.00:-0.01:b:Drained:-1182.37:448.73:0.02:0.01:b:Drained:-231.13:266.14:-0.00:-0.01:b:Drained:-1058.13:314.27:0.03:0.01:b:Drained:-443.53:619.06:0.01:-0.01:b:Drained:-606.38:554.99:0.01:-0.00:b:Drained:-625.83:432.26:0.01:-0.00:b:Drained:-864.56:710.62:0.02:-0.00:b:Drained:-1610.08:165.65:0.01:-0.01:b:Drained:-207.58:109.15:0.01:0.00:b:Drained:-1040.81:438.86:0.01:-0.00:b:Drained:-1798.88:311.28:0.01:-0.00:b:Drained:-52.68:773.78:-0.01:-0.00:b:Drained:-386.53:350.64:0.01:0.00:b:Drained:-713.45:304.22:0.01:0.01:b:Drained:-423.61:486.06:0.01:0.01:b:Drained:-346.04:413.87:-0.01:-0.02:b:Drained:-1539.79:25.63:0.01:-0.01:b:Drained:65.65:726.79:0.01:-0.03:b:Drained:-202.27:407.51:-0.01:-0.00:b:Drained:288.06:598.51:-0.00:-0.01:b:Drained:-128.06:686.07:0.01:-0.05:b:Drained:-482.60:600.40:0.01:-0.01:b:Drained:-788.91:361.63:0.02:-0.00:b:Drained:-99.87:558.60:0.01:-0.01:b:Drained:-318.44:212.56:-0.02:-0.01:b:Drained:76.18:620.24:0.01:-0.02:b:Drained:-428.20:506.10:0.02:0.03:b:Drained:-719.93:586.97:0.01:0.00:b:Drained:-624.81:14.49:-0.02:-0.00:b:Drained:-304.39:473.16:-0.01:-0.01:b:Drained:-631.38:85.91:0.01:-0.01:b:Drained:-1314.48:13.46:0.01:0.00:b:Drained:-296.96:485.21:0.01:-0.00:b:Drained:-10.71:662.80:0.01:-0.03:b:Drained:526.92:316.45:0.00:-0.01:b:Drained:444.34:129.47:-0.02:-0.00:b:Drained:-309.72:332.17:0.00:-0.03:b:Drained:-1206.12:579.68:0.02:0.00:b:Drained:-1655.94:32.99:0.01:-0.01:b:Drained:-528.08:310.79:-0.01:0.01:b:Drained:-292.48:374.32:0.00:-0.01:b:Drained:-997.84:90.55:0.00:0.03:b:Drained:-315.48:237.82:0.01:0.00:b:Drained:-221.46:370.74:-0.01:-0.01:b:Drained:-337.36:311.01:-0.01:0.01:b:Drained:159.72:560.52:-0.01:-0.00:b:Drained:-1107.23:657.33:0.01:0.01:b:Drained:-505.17:517.40:0.01:-0.01:b:Drained:-448.39:464.58:0.00:-0.01:b:Drained:-630.00:519.00:-0.01:0.02:b:Drained:-1699.41:409.83:0.01:0.01:b:Drained:-486.79:302.46:0.02:-0.00:b:Drained:276.87:317.01:0.00:-0.02:b:Drained:-1034.08:202.24:0.01:-0.00:b:Drained:-376.55:125.58:0.01:0.00:b:Drained:-769.42:29.81:0.01:0.00:b:Drained:-1930.96:13.58:0.01:-0.00:b:Drained:-1159.36:220.20:0.02:0.01:b:Drained:-583.98:767.95:0.00:0.00:b:Drained:-2348.24:175.91:0.01:0.00:b:Drained:-10.03:756.86:-1.86:0.74:b:Drained:-1456.07:484.39:0.01:0.00:b:Drained:-1520.06:276.15:0.01:0.00:b:Drained:-808.23:122.98:-0.01:0.02:b:Drained:-279.33:165.63:-0.02:-0.04:b:Drained:-213.68:381.94:-0.00:-0.01:b:Drained:-1339.90:132.52:0.02:0.01:b:Drained:-13.65:224.49:-0.00:0.00:b:Drained:-398.11:366.39:0.00:-0.00:b:Drained:231.17:185.07:-0.02:-0.02:b:Drained:-568.04:284.15:0.00:-0.01:b:Drained:-203.83:262.70:0.00:-0.04:b:Drained:-334.41:340.72:-0.00:-0.00:b:Drained:-930.25:528.28:0.00:-0.01:b:Drained:-358.38:509.22:0.02:0.00:b:Drained:-807.33:487.10:0.02:0.00:b:Drained:1.36:515.69:0.01:-0.02:b:Drained:-490.21:18.19:-0.02:0.00:b:Drained:-104.71:453.55:0.00:-0.03:b:Drained:145.59:337.46:0.00:0.01:b:Drained:-596.55:675.33:0.01:-0.01:b:Drained:-597.97:321.14:0.02:-0.01:b:Drained:-460.94:256.19:-0.02:-0.02:b:Drained:-253.17:503.86:0.00:-0.02:b:Drained:-194.10:809.87:0.03:-0.00:b:Drained:-1003.07:99.00:0.01:-0.00:b:Drained:-575.80:426.37:0.01:0.00:b:Drained:244.40:446.02:-0.01:-0.00:b:Drained:8.14:100.48:0.01:-0.02:b:Drained:91.00:15.46:-0.01:-0.00:b:Drained:119.75:108.13:0.02:-0.02:b:Drained:-831.31:611.23:0.02:0.02:b:Drained:-2242.08:128.36:0.01:0.00:s:Drained:180:181:9.0000:110.00:0:s:Drained:181:182:9.0000:114.13:0:s:Drained:182:180:9.0000:114.13:0:s:Drained:77:181:9.0000:120.16:0:s:Drained:77:182:9.0000:128.00:0:s:Drained:161:77:9.0000:127.83:0:s:Drained:161:182:9.0000:140.00:0:s:Drained:88:161:9.0000:130.05:0:s:Drained:88:77:9.0000:140.00:0:s:Drained:23:180:9.0000:104.67:0:s:Drained:23:181:9.0000:119.80:0:s:Drained:18:67:9.0000:135.17:0:s:Drained:100:67:9.0000:135.73:0:s:Drained:100:18:9.0000:140.00:0:s:Drained:156:100:9.0000:125.30:0:s:Drained:156:18:9.0000:133.37:0:s:Drained:67:29:9.0000:145.32:1:s:Drained:60:156:9.0000:140.00:0:s:Drained:100:29:9.0000:155.80:1:s:Drained:169:60:9.0000:137.75:0:s:Drained:56:60:9.0000:140.00:0:s:Drained:56:169:9.0000:140.00:0:s:Drained:120:56:9.0000:124.95:0:s:Drained:120:169:9.0000:140.00:0:s:Drained:74:182:9.0000:134.92:0:s:Drained:74:161:9.0000:154.04:1:s:Drained:163:156:9.0000:131.37:0:s:Drained:163:18:9.0000:140.00:0:s:Drained:33:163:9.0000:106.49:0:s:Drained:33:18:9.0000:140.00:0:s:Drained:54:33:9.0000:100.00:0:s:Drained:54:163:9.0000:125.55:0:s:Drained:110:54:9.0000:100.00:0:s:Drained:110:163:9.0000:139.33:0:s:Drained:170:110:9.0000:103.44:0:s:Drained:170:54:9.0000:113.74:0:s:Drained:31:170:9.0000:103.53:0:s:Drained:31:54:9.0000:140.00:0:s:Drained:48:54:9.0000:100.00:0:s:Drained:48:31:9.0000:106.88:0:s:Drained:33:48:9.0000:100.75:1:s:Drained:159:33:9.0000:100.00:0:s:Drained:159:48:9.0000:130.96:0:s:Drained:74:48:9.0000:197.54:1:s:Drained:59:31:9.0000:119.21:0:s:Drained:59:48:9.0000:121.14:0:s:Drained:59:74:9.0000:195.85:1:s:Drained:171:59:9.0000:111.62:0:s:Drained:171:74:9.0000:140.00:0:s:Drained:171:161:9.0000:169.42:1:s:Drained:128:163:9.0000:124.12:0:s:Drained:128:110:9.0000:131.37:0:s:Drained:128:156:9.0000:177.87:1:s:Drained:121:110:9.0000:129.46:0:s:Drained:121:128:9.0000:140.00:0:s:Drained:121:170:9.0000:198.36:1:s:Drained:157:170:9.0000:140.00:0:s:Drained:157:121:9.0000:140.00:0:s:Drained:26:157:9.0000:126.47:0:s:Drained:26:170:9.0000:136.55:0:s:Drained:26:31:9.0000:167.70:1:s:Drained:115:26:9.0000:100.00:0:s:Drained:115:31:9.0000:140.00:0:s:Drained:168:31:9.0000:100.00:0:s:Drained:168:115:9.0000:100.00:0:s:Drained:168:59:9.0000:111.78:1:s:Drained:116:128:9.0000:112.62:0:s:Drained:116:60:9.0000:158.48:1:s:Drained:135:121:9.0000:113.31:0:s:Drained:164:135:9.0000:111.39:0:s:Drained:164:121:9.0000:140.00:0:s:Drained:164:157:9.0000:137.32:1:s:Drained:37:164:9.0000:140.00:0:s:Drained:37:157:9.0000:140.00:0:s:Drained:37:26:9.0000:180.19:1:s:Drained:17:37:9.0000:140.00:0:s:Drained:17:26:9.0000:140.00:0:s:Drained:17:115:9.0000:194.71:1:s:Drained:175:37:9.0000:140.00:0:s:Drained:175:17:9.0000:140.00:0:s:Drained:118:175:9.0000:130.79:0:s:Drained:118:37:9.0000:131.87:0:s:Drained:164:118:9.0000:187.25:1:s:Drained:50:118:9.0000:133.23:0:s:Drained:50:164:9.0000:140.00:0:s:Drained:135:50:9.0000:192.23:1:s:Drained:107:50:9.0000:100.00:0:s:Drained:107:135:9.0000:140.00:0:s:Drained:13:50:9.0000:140.00:0:s:Drained:13:107:9.0000:140.00:0:s:Drained:95:118:9.0000:134.21:0:s:Drained:95:50:9.0000:140.00:0:s:Drained:95:13:9.0000:159.03:1:s:Drained:83:95:9.0000:126.66:0:s:Drained:83:118:9.0000:130.57:0:s:Drained:83:175:9.0000:162.82:1:s:Drained:21:83:9.0000:100.00:0:s:Drained:21:175:9.0000:132.16:0:s:Drained:136:107:9.0000:115.69:0:s:Drained:136:135:9.0000:123.04:0:s:Drained:174:136:9.0000:140.00:0:s:Drained:116:174:9.0000:165.47:1:s:Drained:135:116:9.0000:193.46:1:s:Drained:12:56:9.0000:107.53:0:s:Drained:12:60:9.0000:140.00:0:s:Drained:174:12:9.0000:128.55:1:s:Drained:116:12:9.0000:178.28:1:s:Drained:93:135:9.0000:108.58:0:s:Drained:93:136:9.0000:110.98:0:s:Drained:93:116:9.0000:107.52:1:s:Drained:166:83:9.0000:104.02:0:s:Drained:166:95:9.0000:133.33:0:s:Drained:64:59:9.0000:111.00:0:s:Drained:64:171:9.0000:128.97:0:s:Drained:64:168:9.0000:146.12:1:s:Drained:144:161:9.0000:140.00:0:s:Drained:144:171:9.0000:140.00:0:s:Drained:64:144:9.0000:196.19:1:s:Drained:88:144:9.0000:196.83:1:s:Drained:65:88:9.0000:140.00:0:s:Drained:65:144:9.0000:140.00:0:s:Drained:127:88:9.0000:100.00:0:s:Drained:127:65:9.0000:137.98:0:s:Drained:75:144:9.0000:100.00:0:s:Drained:75:65:9.0000:140.00:0:s:Drained:19:127:9.0000:100.00:0:s:Drained:19:65:9.0000:140.00:0:s:Drained:179:64:9.0000:109.48:0:s:Drained:179:144:9.0000:132.19:0:s:Drained:179:75:9.0000:113.62:1:s:Drained:137:64:9.0000:100.00:0:s:Drained:137:179:9.0000:137.80:0:s:Drained:168:137:9.0000:167.13:1:s:Drained:117:137:9.0000:102.46:0:s:Drained:117:168:9.0000:140.00:0:s:Drained:117:115:9.0000:177.53:1:s:Drained:125:117:9.0000:100.00:0:s:Drained:125:115:9.0000:140.00:0:s:Drained:109:125:9.0000:100.00:0:s:Drained:109:117:9.0000:110.16:0:s:Drained:103:109:9.0000:110.56:0:s:Drained:103:125:9.0000:121.18:0:s:Drained:112:103:9.0000:116.68:0:s:Drained:112:125:9.0000:120.12:0:s:Drained:112:26:9.0000:197.82:1:s:Drained:17:112:9.0000:165.08:1:s:Drained:176:103:9.0000:130.10:0:s:Drained:176:112:9.0000:136.51:0:s:Drained:68:17:9.0000:132.37:0:s:Drained:68:112:9.0000:140.00:0:s:Drained:68:176:9.0000:136.10:1:s:Drained:175:68:9.0000:179.99:1:s:Drained:76:21:9.0000:130.69:0:s:Drained:76:175:9.0000:140.00:0:s:Drained:68:76:9.0000:146.44:1:s:Drained:25:76:9.0000:140.00:0:s:Drained:25:68:9.0000:140.00:0:s:Drained:25:176:9.0000:146.36:1:s:Drained:4:25:9.0000:119.44:0:s:Drained:4:76:9.0000:123.62:0:s:Drained:113:21:9.0000:129.70:0:s:Drained:113:76:9.0000:140.00:0:s:Drained:113:4:9.0000:128.68:1:s:Drained:139:113:9.0000:105.69:0:s:Drained:139:21:9.0000:122.54:0:s:Drained:139:166:9.0000:156.03:1:s:Drained:79:139:9.0000:128.79:0:s:Drained:79:166:9.0000:140.00:0:s:Drained:96:113:9.0000:132.47:0:s:Drained:96:139:9.0000:133.78:0:s:Drained:172:96:9.0000:132.47:0:s:Drained:172:113:9.0000:140.00:0:s:Drained:172:4:9.0000:135.65:1:s:Drained:96:79:9.0000:181.31:1:s:Drained:119:96:9.0000:140.00:0:s:Drained:119:172:9.0000:140.00:0:s:Drained:73:119:9.0000:140.00:0:s:Drained:73:96:9.0000:140.00:0:s:Drained:178:73:9.0000:100.00:0:s:Drained:178:96:9.0000:140.00:0:s:Drained:178:79:9.0000:100.00:1:s:Drained:167:119:9.0000:135.68:0:s:Drained:167:73:9.0000:140.00:0:s:Drained:91:109:9.0000:100.00:0:s:Drained:91:117:9.0000:107.17:0:s:Drained:91:137:9.0000:110.16:1:s:Drained:111:91:9.0000:122.86:0:s:Drained:111:137:9.0000:140.00:0:s:Drained:111:179:9.0000:158.40:1:s:Drained:80:111:9.0000:101.69:0:s:Drained:80:179:9.0000:140.00:0:s:Drained:80:75:9.0000:120.43:1:s:Drained:69:75:9.0000:122.52:0:s:Drained:69:80:9.0000:126.86:0:s:Drained:69:65:9.0000:160.84:1:s:Drained:126:69:9.0000:117.29:0:s:Drained:126:65:9.0000:140.00:0:s:Drained:126:19:9.0000:120.06:1:s:Drained:78:19:9.0000:140.00:0:s:Drained:78:126:9.0000:140.00:0:s:Drained:9:56:9.0000:121.13:0:s:Drained:9:120:9.0000:121.61:0:s:Drained:9:12:9.0000:179.88:1:s:Drained:16:9:9.0000:114.76:0:s:Drained:16:120:9.0000:140.00:0:s:Drained:51:9:9.0000:138.38:0:s:Drained:51:12:9.0000:140.00:0:s:Drained:174:51:9.0000:179.62:1:s:Drained:162:51:9.0000:123.45:0:s:Drained:162:174:9.0000:127.50:0:s:Drained:42:136:9.0000:100.05:0:s:Drained:42:174:9.0000:140.00:0:s:Drained:42:162:9.0000:143.07:1:s:Drained:155:16:9.0000:116.04:0:s:Drained:155:9:9.0000:135.60:0:s:Drained:87:155:9.0000:124.88:0:s:Drained:87:16:9.0000:140.00:0:s:Drained:51:155:9.0000:180.12:1:s:Drained:71:155:9.0000:137.21:0:s:Drained:71:51:9.0000:140.00:0:s:Drained:52:155:9.0000:117.93:0:s:Drained:52:87:9.0000:133.28:0:s:Drained:133:87:9.0000:100.00:0:s:Drained:133:52:9.0000:139.45:0:s:Drained:71:52:9.0000:143.73:1:s:Drained:71:162:9.0000:188.94:1:s:Drained:58:71:9.0000:116.84:0:s:Drained:58:52:9.0000:140.00:0:s:Drained:6:71:9.0000:122.83:0:s:Drained:6:58:9.0000:136.11:0:s:Drained:6:162:9.0000:156.66:1:s:Drained:159:180:9.0000:128.81:1:s:Drained:18:180:9.0000:122.00:1:s:Drained:23:67:9.0000:105.42:1:s:Drained:29:60:9.0000:145.70:1:s:Drained:159:74:9.0000:100.00:1:s:Drained:34:6:9.0000:102.94:0:s:Drained:34:58:9.0000:140.00:0:s:Drained:97:6:9.0000:136.46:0:s:Drained:97:34:9.0000:140.00:0:s:Drained:97:162:9.0000:183.20:1:s:Drained:42:97:9.0000:193.51:1:s:Drained:141:97:9.0000:107.01:0:s:Drained:141:34:9.0000:140.00:0:s:Drained:14:97:9.0000:136.63:0:s:Drained:14:141:9.0000:140.00:0:s:Drained:131:14:9.0000:113.06:0:s:Drained:131:97:9.0000:140.00:0:s:Drained:131:107:9.0000:198.66:1:s:Drained:13:131:9.0000:198.47:1:s:Drained:42:131:9.0000:100.00:1:s:Drained:160:14:9.0000:108.77:0:s:Drained:160:131:9.0000:140.00:0:s:Drained:160:13:9.0000:120.25:1:s:Drained:124:13:9.0000:129.58:0:s:Drained:124:160:9.0000:140.00:0:s:Drained:106:160:9.0000:140.00:0:s:Drained:106:14:9.0000:140.00:0:s:Drained:36:141:9.0000:103.26:0:s:Drained:36:14:9.0000:109.47:0:s:Drained:106:36:9.0000:117.75:1:s:Drained:106:124:9.0000:183.28:1:s:Drained:124:95:9.0000:191.86:1:s:Drained:140:166:9.0000:100.00:0:s:Drained:140:95:9.0000:139.88:0:s:Drained:124:140:9.0000:144.65:1:s:Drained:132:106:9.0000:123.95:0:s:Drained:132:124:9.0000:140.00:0:s:Drained:104:132:9.0000:135.63:0:s:Drained:104:124:9.0000:140.00:0:s:Drained:104:140:9.0000:137.50:1:s:Drained:134:132:9.0000:127.31:0:s:Drained:134:104:9.0000:131.66:0:s:Drained:38:104:9.0000:140.00:0:s:Drained:38:134:9.0000:140.00:0:s:Drained:143:38:9.0000:121.08:0:s:Drained:143:104:9.0000:140.00:0:s:Drained:143:140:9.0000:143.95:1:s:Drained:143:79:9.0000:154.41:1:s:Drained:173:178:9.0000:115.65:0:s:Drained:173:79:9.0000:139.69:0:s:Drained:173:143:9.0000:137.50:1:s:Drained:30:38:9.0000:111.58:0:s:Drained:30:143:9.0000:137.19:0:s:Drained:30:173:9.0000:118.56:1:s:Drained:55:30:9.0000:132.00:0:s:Drained:55:173:9.0000:140.00:0:s:Drained:105:55:9.0000:105.09:0:s:Drained:105:173:9.0000:124.04:0:s:Drained:40:30:9.0000:108.40:0:s:Drained:40:38:9.0000:110.68:0:s:Drained:146:40:9.0000:104.99:0:s:Drained:146:38:9.0000:122.31:0:s:Drained:146:134:9.0000:113.21:1:s:Drained:122:40:9.0000:121.61:0:s:Drained:122:30:9.0000:139.43:0:s:Drained:55:122:9.0000:128.13:1:s:Drained:62:55:9.0000:113.96:0:s:Drained:62:122:9.0000:140.00:0:s:Drained:73:105:9.0000:136.88:1:s:Drained:114:167:9.0000:128.97:0:s:Drained:114:73:9.0000:140.00:0:s:Drained:114:55:9.0000:186.50:1:s:Drained:114:105:9.0000:103.18:1:s:Drained:32:62:9.0000:100.00:0:s:Drained:32:55:9.0000:131.00:0:s:Drained:32:114:9.0000:116.16:1:s:Drained:39:32:9.0000:140.00:0:s:Drained:39:62:9.0000:140.00:0:s:Drained:86:39:9.0000:125.47:0:s:Drained:86:32:9.0000:140.00:0:s:Drained:86:114:9.0000:165.64:1:s:Drained:0:86:9.0000:123.36:0:s:Drained:0:114:9.0000:140.00:0:s:Drained:0:167:9.0000:138.18:1:s:Drained:165:0:9.0000:117.39:0:s:Drained:165:167:9.0000:133.59:0:s:Drained:183:165:9.0000:124.13:0:s:Drained:183:167:9.0000:125.84:0:s:Drained:183:119:9.0000:120.79:1:s:Drained:150:172:9.0000:100.00:0:s:Drained:150:4:9.0000:140.00:0:s:Drained:15:150:9.0000:109.76:0:s:Drained:15:172:9.0000:133.59:0:s:Drained:15:119:9.0000:145.08:1:s:Drained:183:15:9.0000:154.99:1:s:Drained:98:183:9.0000:110.55:0:s:Drained:98:15:9.0000:140.00:0:s:Drained:165:98:9.0000:191.86:1:s:Drained:8:98:9.0000:123.10:0:s:Drained:8:165:9.0000:140.00:0:s:Drained:2:8:9.0000:121.29:0:s:Drained:2:165:9.0000:138.93:0:s:Drained:101:2:9.0000:120.57:0:s:Drained:101:165:9.0000:139.17:0:s:Drained:101:0:9.0000:125.75:1:s:Drained:145:86:9.0000:140.00:0:s:Drained:145:39:9.0000:140.00:0:s:Drained:94:145:9.0000:113.85:0:s:Drained:94:86:9.0000:135.62:0:s:Drained:101:94:9.0000:124.78:1:s:Drained:0:94:9.0000:169.34:1:s:Drained:149:145:9.0000:125.85:0:s:Drained:149:94:9.0000:140.00:0:s:Drained:89:149:9.0000:109.96:0:s:Drained:89:94:9.0000:117.23:0:s:Drained:89:101:9.0000:168.49:1:s:Drained:92:89:9.0000:114.62:0:s:Drained:92:101:9.0000:140.00:0:s:Drained:92:2:9.0000:157.07:1:s:Drained:129:92:9.0000:132.40:0:s:Drained:129:2:9.0000:140.00:0:s:Drained:138:2:9.0000:113.88:0:s:Drained:138:129:9.0000:120.79:0:s:Drained:138:8:9.0000:115.50:1:s:Drained:22:92:9.0000:134.36:0:s:Drained:22:129:9.0000:140.00:0:s:Drained:82:89:9.0000:132.55:0:s:Drained:82:92:9.0000:140.00:0:s:Drained:22:82:9.0000:122.16:1:s:Drained:43:149:9.0000:103.31:0:s:Drained:43:89:9.0000:140.00:0:s:Drained:43:82:9.0000:151.46:1:s:Drained:41:43:9.0000:109.97:0:s:Drained:41:82:9.0000:140.00:0:s:Drained:57:82:9.0000:107.57:0:s:Drained:57:41:9.0000:119.70:0:s:Drained:57:22:9.0000:158.92:1:s:Drained:153:57:9.0000:135.04:0:s:Drained:153:22:9.0000:140.00:0:s:Drained:28:153:9.0000:140.00:0:s:Drained:28:57:9.0000:140.00:0:s:Drained:154:28:9.0000:100.00:0:s:Drained:154:57:9.0000:135.06:0:s:Drained:41:154:9.0000:153.40:1:s:Drained:66:22:9.0000:100.00:0:s:Drained:66:153:9.0000:124.99:0:s:Drained:66:129:9.0000:147.41:1:s:Drained:25:150:9.0000:198.33:1:s:Drained:24:149:9.0000:100.00:0:s:Drained:24:145:9.0000:121.96:0:s:Drained:177:145:9.0000:106.68:0:s:Drained:177:24:9.0000:117.15:0:s:Drained:177:39:9.0000:123.54:1:s:Drained:158:41:9.0000:122.40:0:s:Drained:158:43:9.0000:133.47:0:s:Drained:44:158:9.0000:102.49:0:s:Drained:44:43:9.0000:129.61:0:s:Drained:44:24:9.0000:122.11:1:s:Drained:81:158:9.0000:137.70:0:s:Drained:81:41:9.0000:140.00:0:s:Drained:154:81:9.0000:134.81:1:s:Drained:99:81:9.0000:140.00:0:s:Drained:99:154:9.0000:140.00:0:s:Drained:45:28:9.0000:140.00:0:s:Drained:45:154:9.0000:140.00:0:s:Drained:99:45:9.0000:132.52:1:s:Drained:3:45:9.0000:140.00:0:s:Drained:3:99:9.0000:140.00:0:s:Drained:147:62:9.0000:121.50:0:s:Drained:147:122:9.0000:140.00:0:s:Drained:147:39:9.0000:181.94:1:s:Drained:53:177:9.0000:108.06:0:s:Drained:53:39:9.0000:140.00:0:s:Drained:53:147:9.0000:145.81:1:s:Drained:7:24:9.0000:119.63:0:s:Drained:7:177:9.0000:123.40:0:s:Drained:7:53:9.0000:161.52:1:s:Drained:44:7:9.0000:173.99:1:s:Drained:46:44:9.0000:114.20:0:s:Drained:46:7:9.0000:140.00:0:s:Drained:123:46:9.0000:100.00:0:s:Drained:123:44:9.0000:131.51:0:s:Drained:123:158:9.0000:131.52:1:s:Drained:49:123:9.0000:105.33:0:s:Drained:49:158:9.0000:140.00:0:s:Drained:49:81:9.0000:151.81:1:s:Drained:108:49:9.0000:128.51:0:s:Drained:108:81:9.0000:140.00:0:s:Drained:108:99:9.0000:157.62:1:s:Drained:130:108:9.0000:118.71:0:s:Drained:130:99:9.0000:140.00:0:s:Drained:130:3:9.0000:172.66:1:s:Drained:5:130:9.0000:113.80:0:s:Drained:5:3:9.0000:140.00:0:s:Drained:1:3:9.0000:124.69:0:s:Drained:1:5:9.0000:130.07:0:s:Drained:102:3:9.0000:140.00:0:s:Drained:102:45:9.0000:140.00:0:s:Drained:102:1:9.0000:192.21:1:s:Drained:142:45:9.0000:132.40:0:s:Drained:142:102:9.0000:135.21:0:s:Drained:28:142:9.0000:155.15:1:s:Drained:70:153:9.0000:130.39:0:s:Drained:70:28:9.0000:135.48:0:s:Drained:142:70:9.0000:137.02:1:s:Drained:35:102:9.0000:140.00:0:s:Drained:35:1:9.0000:140.00:0:s:Drained:90:1:9.0000:138.01:0:s:Drained:90:35:9.0000:140.00:0:s:Drained:148:90:9.0000:135.75:0:s:Drained:148:1:9.0000:140.00:0:s:Drained:5:148:9.0000:169.29:1:s:Drained:61:90:9.0000:128.91:0:s:Drained:61:148:9.0000:140.00:0:s:Drained:11:90:9.0000:140.00:0:s:Drained:11:35:9.0000:140.00:0:s:Drained:47:11:9.0000:117.49:0:s:Drained:47:90:9.0000:140.00:0:s:Drained:61:47:9.0000:147.06:1:s:Drained:27:47:9.0000:117.80:0:s:Drained:27:61:9.0000:140.00:0:s:Drained:85:47:9.0000:129.10:0:s:Drained:85:11:9.0000:140.00:0:s:Drained:184:27:9.0000:100.00:0:s:Drained:184:47:9.0000:104.85:0:s:Drained:184:85:9.0000:110.01:1:s:Drained:151:184:9.0000:118.24:0:s:Drained:151:85:9.0000:139.21:0:s:Drained:72:151:9.0000:122.82:0:s:Drained:72:85:9.0000:133.51:0");
    // New, 3 balls only
//    Tower t = new Tower("_b:Drained:-1046.90:68.90:0.03:-0.24:b:Drained:-1527.09:78.73:0.53:-0.19:b:Drained:-1518.01:49.40:-0.39:-0.90:b:Drained:-818.45:43.39:0.66:0.91:b:Drained:-1544.27:36.23:-1.17:0.15:b:Drained:-2232.13:37.04:-0.20:-0.25:b:Drained:-423.70:22.76:0.88:-0.80:b:Drained:-164.23:75.12:0.84:-0.44:b:Drained:-527.35:12.17:-0.59:0.00:b:Drained:-866.42:49.28:0.49:0.62:b:Drained:-1249.70:21.72:0.68:-2.10:b:Drained:-45.64:15.05:0.66:-0.38:b:Drained:62.01:14.08:2.00:0.03:b:Drained:941.24:24.53:0.39:2.01:b:Drained:2054.93:12.20:1.32:-0.00:b:Drained:70.20:32.07:-1.01:1.72:b:Drained:-1884.12:40.15:-0.45:-0.32:b:Drained:139.42:13.85:-1.48:0.00:b:Drained:-1409.32:11.29:0.03:1.15:b:Drained:-3.44:61.61:-0.96:-1.76:b:Drained:-7.49:54.17:-0.96:-1.76:b:Drained:-2467.78:19.48:-0.38:0.76:b:Drained:-1988.11:16.37:0.06:0.81:b:Drained:-610.75:81.35:2.34:-4.88:b:Drained:-698.26:50.81:-0.17:0.51:b:Drained:484.37:17.01:-1.64:0.01:b:Drained:-1129.69:14.15:1.20:0.02:b:Drained:1441.26:13.48:1.26:0.00:b:Drained:-1154.04:24.92:1.24:0.25:b:Drained:-2146.81:50.94:-0.14:0.09:b:Drained:-732.53:42.00:0.15:-1.06:b:Drained:-9.16:13.03:2.00:0.03:b:Drained:-377.82:11.84:-0.07:0.15:b:Drained:-1302.88:32.54:-0.41:0.73:b:Drained:-393.02:34.79:0.99:-0.91:b:Drained:-1936.47:73.26:-1.05:-0.53:b:Drained:-245.44:16.88:-0.04:0.00:b:Drained:-1530.21:12.76:-1.31:-0.00:b:Drained:-1576.47:47.85:1.60:1.04:b:Drained:-1890.48:12.20:-0.90:-0.34:b:Drained:-1500.83:90.92:-0.11:0.64:b:Drained:-2432.58:17.13:0.46:0.42:b:Drained:10.32:86.93:-0.96:-1.76:b:Drained:-1918.24:43.15:-0.49:-0.25:b:Drained:24.40:13.52:2.00:0.03:b:Drained:-680.67:18.31:-0.42:0.34:b:Drained:-1387.35:37.18:0.30:0.01:b:Drained:-1356.50:19.73:-0.51:-0.32:b:Drained:21.03:106.63:0.96:1.76:b:Drained:-843.15:39.45:0.48:-0.08:b:Drained:-804.40:11.65:-0.16:0.06:b:Drained:-1728.39:18.82:1.32:0.02:b:Drained:-1145.27:57.76:0.26:-0.37:b:Drained:-347.76:75.73:0.61:0.10:b:Drained:-268.94:69.60:1.21:-0.18:b:Drained:-2239.60:12.19:-1.32:0.00:b:Drained:-1071.25:15.16:-1.74:0.02:b:Drained:1744.01:15.92:1.72:0.00:b:Drained:-25.82:20.46:-0.96:-1.76:b:Drained:730.29:21.33:0.49:0.12:b:Drained:-1647.54:13.85:1.31:-0.00:b:Drained:72.44:28.25:-1.01:1.72:b:Drained:-19.47:12.88:2.00:0.03:b:Drained:647.62:11.66:1.35:0.01:b:Drained:-1914.66:13.06:1.12:0.01:b:Drained:-1088.58:46.21:0.80:0.19:b:Drained:-2793.18:20.64:0.87:-2.86:b:Drained:-844.00:63.83:0.93:0.21:b:Drained:-598.06:52.26:2.44:-4.86:b:Drained:906.98:18.14:0.47:0.99:b:Drained:-1183.72:86.99:1.12:-1.49:b:Drained:1359.26:18.41:2.04:0.00:b:Drained:1178.10:16.90:2.00:0.00:b:Drained:-498.74:38.73:0.28:-0.02:b:Drained:576.28:15.57:0.42:-0.92:b:Drained:-1808.29:32.19:-0.59:0.51:b:Drained:-1550.49:63.22:-0.10:0.65:b:Drained:63.62:43.26:1.01:-1.72:b:Drained:-9.48:50.51:0.96:1.76:b:Drained:-2306.37:14.50:0.51:0.52:b:Drained:-1324.29:66.01:0.30:-0.88:b:Drained:-1323.18:14.25:-0.09:0.33:b:Drained:532.00:26.33:0.11:-1.81:b:Drained:-1464.49:15.15:-1.43:0.00:b:Drained:-2279.43:13.40:0.56:-0.68:b:Drained:-9.00:51.40:0.96:1.76:b:Drained:-1840.78:41.98:-0.44:-0.21:b:Drained:692.37:19.91:-0.54:-0.13:b:Drained:-1467.05:71.92:-0.83:-1.95:b:Drained:-1219.34:15.06:-0.55:0.21:b:Drained:-712.86:12.92:0.20:-1.79:b:Drained:-899.44:16.14:-0.86:0.23:b:Drained:-1256.70:55.55:1.17:-0.84:b:Drained:-2009.25:47.50:0.20:-2.91:b:Drained:-561.14:36.80:-0.14:0.15:b:Drained:-1503.02:16.59:-1.09:0.48:b:Drained:-2167.63:74.39:-0.89:-0.20:b:Drained:978.86:12.48:1.23:0.01:b:Drained:-1024.81:89.85:0.02:-0.22:b:Drained:-1000.00:68.79:0.30:-0.09:b:Drained:-1095.36:98.83:0.19:0.53:b:Drained:28.61:13.58:-2.00:-0.03:b:Drained:-1860.76:16.53:-0.37:-0.28:b:Drained:24.19:13.52:2.00:0.03:b:Drained:27.56:104.61:-1.01:1.72:b:Drained:386.24:17.40:1.44:0.00:b:Drained:3.06:73.59:0.96:1.76:b:Drained:-471.61:80.94:-0.18:-1.55:b:Drained:-1115.99:72.47:0.18:0.44:b:Drained:-3.84:13.11:2.00:0.03:b:Drained:447.54:11.65:1.30:0.00:b:Drained:-11.65:46.53:0.96:1.76:b:Drained:-968.11:42.15:0.36:0.38:b:Drained:-1024.37:45.30:0.57:0.24:b:Drained:-850.85:91.65:0.31:0.08:b:Drained:-569.03:67.11:0.93:1.03:b:Drained:-832.10:14.66:-1.00:-0.09:b:Drained:-1699.28:12.27:1.31:0.00:b:Drained:68.70:34.63:1.01:-1.72:b:Drained:496.61:49.91:-0.03:-0.48:b:Drained:996.06:34.06:1.33:-0.04:b:Drained:876.29:40.01:1.53:-1.83:b:Drained:25.59:13.54:2.00:0.03:b:Drained:-1497.08:66.00:-1.15:0.36:b:Drained:-2051.90:12.24:-1.10:0.00:b:Drained:-449.32:65.65:0.62:-0.53:b:Drained:-21.97:27.54:-0.96:-1.76:b:Drained:-1477.19:105.24:1.29:-0.99:b:Drained:-153.08:37.30:1.30:0.36:b:Drained:-389.17:61.54:1.05:-0.74:b:Drained:66.39:38.56:-1.01:1.72:b:Drained:-82.21:44.24:1.52:0.50:b:Drained:-1674.76:27.83:1.31:0.01:b:Drained:-558.76:12.05:1.02:0.00:b:Drained:-200.94:57.26:0.87:-0.43:b:Drained:-588.32:18.59:0.61:1.24:b:Drained:-2178.01:29.03:-0.07:-1.64:b:Drained:-363.37:45.24:0.91:-0.63:b:Drained:805.89:13.54:1.36:0.00:b:Drained:-1431.48:73.78:-0.69:-1.81:b:Drained:-106.96:47.62:1.51:0.11:b:Drained:-863.64:17.53:0.32:0.01:b:Drained:-2212.09:13.87:-1.44:0.27:b:Drained:-1385.82:12.21:1.09:0.01:b:Drained:-505.55:11.37:-0.15:-0.01:b:Drained:-2822.55:16.60:0.70:-2.91:b:Drained:52.60:62.01:-1.01:1.72:b:Drained:-1790.42:12.05:-1.10:0.00:b:Drained:-1945.84:17.04:0.77:0.89:b:Drained:-1100.67:15.06:1.25:0.00:b:Drained:1261.71:17.00:1.66:0.00:b:Drained:-878.91:77.19:0.11:-0.18:b:Drained:-1565.46:15.44:-0.05:-0.33:b:Drained:778.55:36.61:-1.13:-2.00:b:Drained:-508.63:65.95:1.05:0.28:b:Drained:840.67:31.46:0.28:-0.68:b:Drained:-814.72:75.04:0.87:1.01:b:Drained:-62.54:58.58:1.46:0.77:b:Drained:71.53:14.21:2.00:0.03:b:Drained:-1172.70:46.46:0.57:-1.27:b:Drained:-28.12:16.24:0.96:1.76:b:Drained:-1120.88:41.13:0.72:0.27:b:Drained:266.31:13.97:-0.15:0.01:b:Drained:-471.35:16.39:1.37:0.00:b:Drained:-981.26:95.79:0.08:0.11:b:Drained:75.76:22.61:1.01:-1.72:b:Drained:-532.00:39.81:-0.08:0.10:b:Drained:-791.15:35.31:0.63:-0.03:b:Drained:-213.10:15.14:0.37:-0.08:b:Drained:-133.52:57.87:1.53:0.16:b:Drained:20.55:105.75:-0.96:-1.76:b:Drained:-1333.08:38.90:-0.35:0.25:b:Drained:-758.37:57.35:1.03:0.74:b:Drained:817.10:37.10:0.49:0.48:b:Drained:-175.70:47.12:0.90:-0.47:b:Drained:2127.53:14.79:-1.66:-0.00:b:Drained:-23.38:24.95:0.96:1.76:b:Drained:-1175.23:12.22:1.31:0.01:b:Drained:-2583.68:16.88:-1.80:0.00:b:Drained:-4.75:59.22:-0.96:-1.76:b:Drained:-2.82:62.76:-0.96:-1.76:b:Drained:-618.30:12.06:0.04:-0.82:b:Drained:202.38:18.15:-1.71:0.00:b:Drained:-981.43:13.38:1.10:0.00:b:Drained:-29.96:55.14:2.52:-1.11:b:Drained:566.13:40.46:0.35:-0.56:b:Drained:1081.54:19.64:-0.22:-1.01:b:Drained:30.90:98.92:-1.01:1.72:b:Drained:36.03:90.19:-1.01:1.72:b:Drained:-2.13:64.03:-0.96:-1.76:b:Drained:-113.96:78.67:1.35:0.33:b:Drained:-625.11:38.81:-0.41:-0.90:b:Drained:-918.33:77.59:-0.49:-1.24:b:Drained:1592.95:14.06:1.40:-0.00:b:Drained:-141.39:13.86:1.36:0.34:b:Drained:-11.98:45.91:0.96:1.76:b:Drained:-277.93:12.63:0.98:0.22:b:Drained:26.44:13.55:-2.00:-0.03:b:Drained:-996.94:33.91:0.45:-0.12:b:Drained:33.56:13.66:-2.00:-0.03:b:Drained:-329.40:47.32:0.12:-0.36:b:Drained:-345.63:21.50:0.65:-0.88:b:Drained:-9.09:51.23:-0.96:-1.76:b:Drained:4.15:75.59:0.96:1.76:b:Drained:27.84:13.57:-2.00:-0.03:b:Drained:341.53:16.31:1.55:0.00:b:Drained:35.09:13.68:-2.00:-0.03:b:Drained:20.93:106.44:0.96:1.76:b:Drained:-630.95:114.24:-3.88:-3.18:b:Drained:52.77:61.72:-1.01:1.72:b:Drained:-6.44:56.11:-0.96:-1.76:b:Drained:7.86:82.41:-0.96:-1.76:b:Drained:615.31:23.89:0.82:-0.13:b:Drained:68.39:35.14:-1.01:1.72:b:Drained:859.96:12.40:-1.09:0.00:b:Drained:-304.13:69.14:1.07:-1.66:b:Drained:-293.21:43.03:0.36:1.49:b:Drained:-535.60:71.60:1.07:0.49:b:Drained:-765.06:33.65:-0.67:1.06:b:Drained:-776.13:12.26:0.91:0.06:b:Drained:15.59:96.63:0.96:1.76:b:Drained:-1059.96:104.50:0.08:0.62:b:Drained:98.77:12.35:0.57:0.04:b:Drained:296.15:21.49:0.48:-0.29:b:Drained:1050.68:35.62:-0.19:0.24:b:Drained:-639.03:66.00:0.20:-0.56:b:Drained:-747.35:14.48:-1.66:0.01:b:Drained:-420.98:56.74:0.56:-0.73:b:Drained:-897.68:49.63:0.45:-0.15:b:Drained:-1226.49:45.05:-0.69:0.35:b:Drained:-953.15:73.98:0.38:0.65:b:Drained:-932.36:42.67:0.53:-0.55:b:Drained:-3.56:13.11:-2.00:-0.03:b:Drained:1017.18:13.97:1.37:0.00:b:Drained:29.08:13.59:2.00:0.03:b:Drained:-939.48:105.49:-0.51:-1.13:b:Drained:71.39:30.04:-1.01:1.72:b:Drained:-660.15:43.91:0.63:-0.35:b:Drained:-471.24:51.71:1.10:-1.46:b:Drained:-227.05:42.20:0.98:-0.68:b:Drained:-450.18:35.86:1.25:0.15:b:Drained:-168.94:13.82:0.71:0.00:b:Drained:-189.70:28.07:0.39:-0.12:b:Drained:-404.11:87.45:0.44:-0.62:b:Drained:-1659.91:59.25:0.84:0.24:b:Drained:-5.43:13.09:2.00:0.03:b:Drained:15.11:95.74:-0.96:-1.76:b:Drained:-87.12:70.18:1.40:0.44:b:Drained:-502.46:93.74:1.23:0.30:b:Drained:-313.08:18.00:1.79:0.20:b:Drained:-670.86:74.21:0.65:-0.33:b:Drained:-784.36:63.58:0.78:0.03:b:Drained:-11.44:46.91:0.96:1.76:b:Drained:-782.87:95.26:0.18:0.28:b:Drained:-1038.56:17.47:0.19:0.24:b:Drained:-943.55:13.42:-1.23:0.00:b:Drained:-1012.92:14.23:-0.67:0.63:b:Drained:-1285.24:13.10:-1.09:0.01:b:Drained:-1360.03:55.26:0.28:-0.19:b:Drained:-1198.42:58.83:0.56:-1.26:b:Drained:-1075.04:76.14:0.19:0.49:b:Drained:-737.32:69.86:1.80:-0.41:b:Drained:-1056.40:41.17:0.68:-1.02:b:Drained:-1195.83:30.45:0.28:-0.88:b:Drained:-1227.88:78.04:0.24:0.54:b:Drained:-1415.27:37.70:-0.40:-1.88:b:Drained:-1482.72:41.04:-0.94:0.37:b:Drained:-1748.61:47.07:0.70:-0.38:b:Drained:-1612.97:53.83:0.90:0.09:b:Drained:-1432.52:15.15:-1.01:-2.08:b:Drained:-1618.03:18.63:-0.10:1.57:b:Drained:-1638.81:38.05:0.80:0.21:b:Drained:-1782.85:52.82:0.42:-1.14:b:Drained:-1818.29:68.50:0.69:-1.02:b:Drained:-1592.40:22.89:-0.13:1.79:b:Drained:-266.23:38.79:0.24:0.28:b:Drained:-1446.44:41.61:0.53:-0.86:b:Drained:-1561.88:86.79:-1.67:0.06:b:Drained:-1967.01:43.65:0.27:0.74:b:Drained:-1831.14:13.81:-0.91:0.02:b:Drained:-1290.66:58.15:1.26:0.41:b:Drained:-1274.72:35.11:0.51:-0.56:b:Drained:-1395.31:66.85:-0.24:0.00:b:Drained:-1699.24:66.63:1.20:0.07:b:Drained:-1703.93:37.00:1.34:0.04:b:Drained:-1759.71:14.31:0.93:-2.26:b:Drained:-2045.20:39.87:0.76:-0.28:b:Drained:-2020.54:14.07:-0.17:-3.05:b:Drained:-2117.02:68.10:-0.28:1.36:b:Drained:-2094.06:11.74:-1.30:0.00:b:Drained:-2205.28:45.47:0.05:-1.43:b:Drained:-2078.28:34.00:0.37:-1.09:b:Drained:-2336.96:19.16:-0.08:0.82:b:Drained:-2512.64:18.40:1.56:0.01:b:Drained:-646.41:16.99:-0.41:-0.97:b:Drained:1948.26:18.49:2.01:0.00:b:Drained:6.46:79.84:0.96:1.76:b:Drained:-30.03:12.72:-0.01:-0.00:b:Drained:80.62:14.35:0.00:0.00:b:Drained:23.43:111.12:-0.16:-0.23:s:Drained:297:298:9.0000:110.00:0:s:Drained:298:299:9.0000:114.13:0:s:Drained:299:297:9.0000:114.13:0");
    System.out.println("t = " + t);
  }
}
