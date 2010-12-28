/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.MainController;
import com.goofans.gootool.ToolPreferences;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MainWindow extends JFrame
{
  private static final Logger log = Logger.getLogger(MainWindow.class.getName());

  private static final String WINDOW_POSITION_MAXIMISED = "MAX";

  public MainMenu mainMenu;
  public MainPanel mainPanel;

  private MainController mainController;

  public MainWindow(MainController mainController)
  {
    super(GooTool.getTextProvider().getString("mainFrame.title"));

    this.mainController = mainController;

    setIconImage(GooTool.getMainIconImage());
    mainPanel = new MainPanel(mainController);
    setContentPane(mainPanel.rootPanel);

    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    setMinimumSize(new Dimension(800, 500)); // TODO does this work? why is it necessary - shouldn't components have their own min size that is enforced?

    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        MainWindow.this.mainController.maybeExit();
      }
    });

    mainMenu = new MainMenu(mainController);
    setJMenuBar(mainMenu.getJMenuBar());

    pack();
    restoreWindowPosition();

    // This needs to be after the restore!
    addComponentListener(new ComponentAdapter()
    {
      @Override
      public void componentResized(ComponentEvent e)
      {
        saveWindowPosition();
      }

      @Override
      public void componentMoved(ComponentEvent e)
      {
        saveWindowPosition();
      }
    });
  }

  private void saveWindowPosition()
  {
    if ((getExtendedState() & MAXIMIZED_BOTH) != 0) {
      ToolPreferences.setWindowPosition(WINDOW_POSITION_MAXIMISED);
    }
    else {
      //noinspection StringConcatenation
      ToolPreferences.setWindowPosition(getX() + "," + getY() + "," + getWidth() + "," + getHeight());
    }
  }

  private void restoreWindowPosition()
  {
    String windowPosition = ToolPreferences.getWindowPosition();
    if (windowPosition == null) {
      log.finer("Opening GooTool window at platform default position");
      setLocationByPlatform(true);
    }
    else if (windowPosition.equals(WINDOW_POSITION_MAXIMISED)) {
      log.finer("Opening GooTool window in maximised state");
      setLocationByPlatform(true);
      setExtendedState(MAXIMIZED_BOTH);
    }
    else {
      //noinspection StringConcatenation
      log.finer("Opening GooTool window at stored position " + windowPosition);
      StringTokenizer tok = new StringTokenizer(windowPosition, ",");
      setBounds(Integer.valueOf(tok.nextToken()), Integer.valueOf(tok.nextToken()), Integer.valueOf(tok.nextToken()), Integer.valueOf(tok.nextToken()));
    }
  }

  /*public void updateViewFromModel(ProjectModel model)
  {
    mainPanel.updateViewFromModel(model);
  }

  public void updateModelFromView(ProjectModel model)
  {
    mainPanel.updateModelFromView(model);
  }*/
}
