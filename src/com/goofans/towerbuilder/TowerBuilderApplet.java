package com.goofans.towerbuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class TowerBuilderApplet extends JApplet implements Runnable
{

  private static final int MAX_BALLS = 100;
  private static final float BALL_RADIUS = 4;
  private static final float STRAND_LENGTH = 50;
  private static final float PROXIMITY_LIMIT = 8;

  private List<Ball> balls;
  private List<Strand> strands;
  private TowerRenderer renderPanel;
  private Thread workerThread;
  private List<Candidate> candidates;
  private Candidate bestCandidate;

  public TowerBuilderApplet()
  {
  }

  /**
   * Applet initilisation: create the GUI.
   */
  @Override
  public void init()
  {
    try {
      SwingUtilities.invokeAndWait(new Runnable()
      {
        public void run()
        {
          setLayout(new BorderLayout());
          JButton clear = new JButton("Clear");
          clear.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              reset();
            }
          });
          JButton start = new JButton("Start");
          start.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              if (workerThread != null) {
                workerThread.stop();
                workerThread = null;
              }
              workerThread = new Thread(TowerBuilderApplet.this);
              workerThread.start();
              updateStatus();
            }
          });
          JButton stop = new JButton("Stop");
          stop.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {

              if (workerThread != null) {
                workerThread.stop();
                workerThread = null;
              }
              updateStatus();
            }
          });
          JPanel buttonPanel = new JPanel();
          buttonPanel.add(clear);
          buttonPanel.add(start);
          buttonPanel.add(stop);
          add(buttonPanel, BorderLayout.NORTH);
          renderPanel = new TowerRenderer();
          renderPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE));
          add(renderPanel, BorderLayout.CENTER);
        }
      });
    }
    catch (Exception e) {
      System.err.println("failed to create GUI");
      e.printStackTrace();
    }
  }

  /**
   * Stop/clear: stop the worker thread and reset state to initial.
   */
  private void reset()
  {
    if (workerThread != null) {
      workerThread.stop();
      workerThread = null;
    }

    candidates = null;
    bestCandidate = null;

    balls = new ArrayList<Ball>(MAX_BALLS);
    strands = new ArrayList<Strand>(MAX_BALLS * 2);

    Ball ball1 = new Ball();
    ball1.x = (renderPanel.getWidth() - STRAND_LENGTH) / 2;
    ball1.y = renderPanel.getHeight() / 2;

    Ball ball2 = new Ball();
    ball2.x = (renderPanel.getWidth() + STRAND_LENGTH) / 2;
    ball2.y = renderPanel.getHeight() / 2;

    Strand strand = new Strand();
    strand.ball1 = ball1;
    strand.ball2 = ball2;

    balls.add(ball1);
    balls.add(ball2);
    strands.add(strand);
    repaint();
    updateStatus();
  }

  /**
   * Update the applet status bar.
   */
  private void updateStatus()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(balls.size()).append(" ball");
    if (balls.size() != 1) sb.append("s");
    sb.append(", ");

    sb.append(strands.size()).append(" strand");
    if (strands.size() != 1) sb.append("s");
    sb.append(". ");

    if (workerThread != null)
      sb.append("Running. ");
    else
      sb.append("Not running. ");

    if (candidates != null) {
      sb.append(candidates.size()).append(" candidate balls. ");
    }

    getAppletContext().showStatus(sb.toString());
  }

  @Override
  public void start()
  {
    reset();
  }

  @Override
  public void stop()
  {
    reset();
  }

  private class Ball
  {
    double x, y;

    boolean closeTo(Ball b2)
    {
      return Math.sqrt(Math.pow(b2.x - this.x, 2) + Math.pow(b2.y - this.y, 2)) < PROXIMITY_LIMIT;
    }
  }

  private class Strand
  {
    Ball ball1;
    Ball ball2;
    boolean builtOn;
  }

  private class Candidate
  {
    Ball newBall;
    Strand buildOn;
  }

  private class TowerRenderer extends JPanel
  {
    @Override
    protected void paintComponent(Graphics g)
    {
      super.paintComponent(g);

      // Draw already placed balls and strands.

      for (Ball ball : balls) {
        g.setColor(Color.RED);
        g.drawOval((int) (ball.x - BALL_RADIUS), (int) (ball.y - BALL_RADIUS), (int) (BALL_RADIUS * 2), (int) (BALL_RADIUS * 2));
      }

      for (Strand strand : strands) {
        if (bestCandidate != null && bestCandidate.buildOn == strand)
          g.setColor(Color.GREEN);
        else
          g.setColor(Color.BLUE);

        g.drawLine((int) strand.ball1.x, (int) strand.ball1.y, (int) strand.ball2.x, (int) strand.ball2.y);
      }

      // Phase 2: Draw candidates

      if (candidates != null) {
        for (Candidate candidate : candidates) {
          g.setColor(Color.YELLOW);
          g.drawLine((int) candidate.buildOn.ball1.x, (int) candidate.buildOn.ball1.y, (int) candidate.newBall.x, (int) candidate.newBall.y);
          g.drawLine((int) candidate.buildOn.ball2.x, (int) candidate.buildOn.ball2.y, (int) candidate.newBall.x, (int) candidate.newBall.y);
          g.setColor(Color.ORANGE);
          g.drawOval((int) (candidate.newBall.x - BALL_RADIUS), (int) (candidate.newBall.y - BALL_RADIUS), (int) (BALL_RADIUS * 2), (int) (BALL_RADIUS * 2));
        }
      }

      // Phase 3: Draw selected candidate

      if (bestCandidate != null) {
        g.setColor(Color.GREEN);
        g.drawOval((int) (bestCandidate.newBall.x - BALL_RADIUS), (int) (bestCandidate.newBall.y - BALL_RADIUS), (int) (BALL_RADIUS * 2), (int) (BALL_RADIUS * 2));
      }
    }
  }

  /**
   * Worker thread; calculate candidates and select best ball.
   */
  public void run()
  {
    try {
      while (balls.size() < MAX_BALLS) {
        System.out.println("Place a ball");

        candidates = new ArrayList<Candidate>(strands.size() * 2);

        for (Strand strand : strands) {
//          if (!strand.builtOn) {
          List<Ball> newCandidateBalls = calculateTriangle(strand);
          if (newCandidateBalls != null) {
            for (Ball newCandidateBall : newCandidateBalls) {
              boolean isEligible = true;
              for (Ball ball : balls) {
                if (ball.closeTo(newCandidateBall)) {
                  isEligible = false;
                  break;
                }
              }

              if (isEligible) {
                Candidate c = new Candidate();
                c.newBall = newCandidateBall;
                c.buildOn = strand;
                candidates.add(c);
              }
            }
          }
        }

        // TODO go through candidates and merge ones with similar locations to a single candidates with multiple strands

        // Pause briefly to show all candidates

        repaint();
        updateStatus();
        Thread.sleep(750);

        // Select one
        // TODO this is where the logic for choosing where to build goes. At the moment it just selects the first available candidate.

        bestCandidate = candidates.get(0);
        candidates = null;

        // Pause briefly to show selected candidate

        repaint();
        updateStatus();
        Thread.sleep(750);

        // Place the ball

        if (bestCandidate != null) {
          balls.add(bestCandidate.newBall);
          Strand s1 = new Strand();
          s1.ball1 = bestCandidate.newBall;
          s1.ball2 = bestCandidate.buildOn.ball1;
          strands.add(s1);

          Strand s2 = new Strand();
          s2.ball1 = bestCandidate.newBall;
          s2.ball2 = bestCandidate.buildOn.ball2;
          strands.add(s2);

          bestCandidate.buildOn.builtOn = true;

          bestCandidate = null;
        }

        repaint();
        updateStatus();
        Thread.sleep(750);
      }
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }

    workerThread = null;
    updateStatus();
  }


  /**
   * Calculates the position of the balls on an equilateral triangle above and below the given strand.
   * <p/>
   * <pre>
   *                  m
   *                /   \
   *          ball1 ---- ball2
   *                \   /
   *                  n
   * </pre>
   */
  private List<Ball> calculateTriangle(Strand strand)
  {
//    double theta = Math.atan((strand.ball2.x - strand.ball1.x) / (strand.ball1.y - strand.ball2.y));
//    newBall.x = strand.ball1.x - (STRAND_LENGTH * Math.sin((Math.PI / 3) - theta));
//    newBall.y = strand.ball1.y + (STRAND_LENGTH * Math.cos((Math.PI / 3) - theta));

    System.out.println("strand.ball1.x = " + strand.ball1.x);
    System.out.println("strand.ball1.y = " + strand.ball1.y);
    System.out.println("strand.ball2.x = " + strand.ball2.x);
    System.out.println("strand.ball2.y = " + strand.ball2.y);

    double mpX = (strand.ball1.x + strand.ball2.x) / 2;
    System.out.println("mpX = " + mpX);
    double mpY = (strand.ball1.y + strand.ball2.y) / 2;
    System.out.println("mpY = " + mpY);

    double lAtoMP = Math.sqrt(Math.pow(mpX - strand.ball1.x, 2) + Math.pow(mpY - strand.ball1.y, 2));
    System.out.println("lAtoMP = " + lAtoMP);

    double lMPtoM = Math.sqrt(Math.pow(STRAND_LENGTH, 2) - Math.pow(lAtoMP, 2));
    System.out.println("lMPtoM = " + lMPtoM);
    if (Double.isNaN(lMPtoM)) {
      // Balls too far apart
      System.out.println("NaN!");
      return null;
    }

    Ball m = new Ball();
    Ball n = new Ball();

    double sAtoB;
    if (strand.ball1.x == strand.ball2.x) {
      sAtoB = 1;
      m.y = n.y = mpY;
      System.out.println("Y IS MIDPOINT!");
    }
    else {
      sAtoB = (strand.ball2.y - strand.ball1.y) / (strand.ball2.x - strand.ball1.x);
      System.out.println("sAtoB = " + sAtoB);
      m.y = mpY + (lMPtoM / Math.sqrt(1 + Math.pow(sAtoB, 2)));
      System.out.println("m.y = " + m.y);
      n.y = mpY - (lMPtoM / Math.sqrt(1 + Math.pow(sAtoB, 2)));
      System.out.println("n.y = " + n.y);
    }

    if (strand.ball1.y == strand.ball2.y) {
      m.x = n.x = mpX;
      System.out.println("X IS MIDPOINT!");
    }
    else {
      double sMPtoM = -(strand.ball2.x - strand.ball1.x) / (strand.ball2.y - strand.ball1.y);
      System.out.println("sMPtoM = " + sMPtoM);
      m.x = mpX - ((lMPtoM / Math.sqrt(1 + Math.pow(sMPtoM, 2))) * (Math.abs(sAtoB) / sAtoB));
      n.x = mpX + ((lMPtoM / Math.sqrt(1 + (Math.pow(sMPtoM, 2)))) * Math.abs(sAtoB) / sAtoB);
      System.out.println("m.x = " + m.x);
      System.out.println("n.x = " + n.x);
    }

    System.out.println("---------------------");

    List<Ball> balls = new ArrayList<Ball>(2);

    balls.add(m);
    balls.add(n);

    return balls;
  }
}
