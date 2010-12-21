/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.ui;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import com.goofans.gootool.util.GUIUtil;

/**
 * Star bar with hover effect.
 *
 * @author David Croft (<a href="http://www.davidc.net">www.davidc.net</a>)
 * @version $Id$
 */
public class StarBar extends JPanel implements MouseListener
{
  private static ImageIcon imgOff;
  private static ImageIcon imgOn;
  private static ImageIcon imgOver;

  private static final String[] TOOLTIPS = new String[]{"Poor", "OK", "Good", "Great", "Awesome"};
  private static final int NUM_STARS = 5;

  private JLabel label;
  private JLabel info;

  private final JLabel[] stars;
  private int rating = 0;
  private int overStar = 0;
  public static final String RATING_PROPERTY = "rating";

  public StarBar()
  {
    this(0);
  }

  public StarBar(int rating)
  {
    stars = new JLabel[NUM_STARS];
    this.rating = rating;

    setupUI();

    loadIcons();
    updateDisplay();

    for (JLabel star : stars) {
      star.addMouseListener(this);
    }
  }

  private static void loadIcons()
  {
    if (imgOff == null) {
      imgOff = new ImageIcon(StarBar.class.getResource("/star-off.png"));
      imgOn = new ImageIcon(StarBar.class.getResource("/star-on.png"));
      imgOver = new ImageIcon(StarBar.class.getResource("/star-over.png"));
    }
  }

  public void mouseClicked(MouseEvent e)
  {
    if (!isEnabled()) return;

    final int star = getStar(e);
    if (star == 0) return;

    setRating(star);
  }

  public void mousePressed(MouseEvent e)
  {
  }

  public void mouseReleased(MouseEvent e)
  {
  }

  public void mouseEntered(MouseEvent e)
  {
    if (!isEnabled()) return;

    final int star = getStar(e);
    if (star == 0) return;

    overStar = star;
    updateDisplay();
  }

  private int getStar(MouseEvent e)
  {
    if (e.getComponent() instanceof JLabel) {
      JLabel star = (JLabel) e.getComponent();
      for (int i = 0; i < stars.length; i++) {
        JLabel jLabel = stars[i];
        if (star == jLabel) {
          return i + 1;
        }
      }
    }
    return 0;
  }

  public void mouseExited(MouseEvent e)
  {
    if (!isEnabled()) return;

    overStar = 0;
    updateDisplay();
  }

  private void updateDisplay()
  {
    /* Update the stars */

    for (int i = 0; i < stars.length; ++i) {
      if (overStar > i) {
        stars[i].setIcon(imgOver);
      }
      else if (overStar == 0 && rating > i) {
        stars[i].setIcon(imgOn);
      }
      else {
        stars[i].setIcon(imgOff);
      }
    }

    /* Update the info display */

    if (overStar > 0) {
      info.setText(TOOLTIPS[overStar - 1] + " (" + overStar + "/5)");
    }
    else if (rating > 0) {
      info.setText(TOOLTIPS[rating - 1] + " (" + rating + "/5)");
    }
    else if (!isEnabled()) {
      info.setText("");
    }
    else {
      info.setText("Unrated");
    }
  }

  public int getRating()
  {
    return rating;
  }

  public void setRating(int newRating)
  {
    int oldRating = this.rating;
    setRatingQuietly(newRating);
    firePropertyChange(RATING_PROPERTY, oldRating, newRating);
  }

  public void setRatingQuietly(int newRating)
  {
    int oldRating = this.rating;
    this.rating = newRating;

    if (oldRating != newRating) {
      updateDisplay();
    }
  }

  @Override
  public void setEnabled(boolean enabled)
  {
    super.setEnabled(enabled);
    for (JLabel star : stars) {
      star.setEnabled(enabled);
    }

    label.setEnabled(enabled);
    info.setEnabled(enabled);

    if (!enabled) {
      overStar = 0;
    }
    updateDisplay();
  }

  private void setupUI()
  {
    setLayout(new GridBagLayout());
    GridBagConstraints gbc;

    label = new JLabel();
    label.setText("Your rating:");
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
//    gbc.weightx = 1.0;
//    gbc.weighty = 1.0;
    gbc.ipadx = 5;
    add(label, gbc);

    final JPanel starPanel = new JPanel();
    starPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
//    gbc.weightx = 1.0;
//    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.ipadx = 5;
    add(starPanel, gbc);

    for (int i = 0; i < stars.length; i++) {
      stars[i] = new JLabel();
      stars[i].setToolTipText(TOOLTIPS[i]);
      starPanel.add(stars[i]);
    }

    info = new JLabel("nada"); // This will get overwritten, but we need it now for the preferred height
    info.setPreferredSize(new Dimension(100, info.getPreferredSize().height));
    gbc = new GridBagConstraints();
    gbc.gridx = 2;
    gbc.gridy = 0;
//    gbc.weighty = 1.0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.ipadx = 5;
    gbc.weightx = 1;
    add(info, gbc);

//    gbc = new GridBagConstraints();
//    gbc.gridx = 3;
//    gbc.gridy = 0;
//    gbc.fill=GridBagConstraints.HORIZONTAL;
//    add(new Spacer(), gbc);
  }

  @SuppressWarnings({"UseOfSystemOutOrSystemErr", "HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
  public static void main(String[] args)
  {
    GUIUtil.switchToSystemLookAndFeel();
    JFrame frame = new JFrame("Test starbar");
    final StarBar bar = new StarBar(0);
//    bar.setEnabled(false);

    bar.addPropertyChangeListener(RATING_PROPERTY, new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent evt)
      {
        System.out.println("evt.getPropertyName() = " + evt.getPropertyName());
        System.out.println("evt.getNewValue() = " + evt.getNewValue());
        System.out.println("evt = " + evt);
      }
    });

    frame.add(bar);
    frame.pack();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }
}
