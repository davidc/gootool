/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import com.goofans.gootool.Controller;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.l10n.ImageL10nPanel;
import com.goofans.gootool.model.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * GooTool's main frame on the screen.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MainFrame extends JFrame implements ViewComponent
{
  private static final Logger log = Logger.getLogger(MainFrame.class.getName());

  private static final String WINDOW_POSITION_MAXIMISED = "MAX";

  private JPanel rootPanel;
  private JButton saveButton;
  private JButton saveAndLaunchButton;
  private JPanel optionsPanelPanel;
  private JPanel addinsPanelPanel;
  private JPanel profilePanelPanel;
  public JTabbedPane tabbedPane;

  private final Controller controller;

  public AddinsPanel addinsPanel;
  private OptionsPanel optionsPanel;
  public ImageL10nPanel imageLocalisationPanel;
  public MainMenu mainMenu;
  public ProfilePanel profilePanel;

  public MainFrame(final Controller controller)
  {
    super(GooTool.getTextProvider().getString("mainFrame.title"));
    this.controller = controller;

    setMinimumSize(new Dimension(800, 500));
    setIconImage(GooTool.getMainIconImage());

    setContentPane(rootPanel);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent e)
      {
        controller.maybeExit();
      }
    });

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

    saveAndLaunchButton.setActionCommand(Controller.CMD_SAVE_AND_LAUNCH);
    saveAndLaunchButton.addActionListener(controller);

    saveButton.setActionCommand(Controller.CMD_SAVE);
    saveButton.addActionListener(controller);

    mainMenu = new MainMenu(controller);
    setJMenuBar(mainMenu.getJMenuBar());

    pack();

    restoreWindowPosition();
  }

  private void saveWindowPosition()
  {
    if ((getExtendedState() & MAXIMIZED_BOTH) != 0) {
      ToolPreferences.setWindowPosition(WINDOW_POSITION_MAXIMISED);
    }
    else {
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
      log.finer("Opening GooTool window at stored position " + windowPosition);
      StringTokenizer tok = new StringTokenizer(windowPosition, ",");
      setBounds(Integer.valueOf(tok.nextToken()), Integer.valueOf(tok.nextToken()), Integer.valueOf(tok.nextToken()), Integer.valueOf(tok.nextToken()));
    }
  }

  private void createUIComponents()
  {
    optionsPanel = new OptionsPanel(controller);
    addinsPanel = new AddinsPanel(controller);
    profilePanel = new ProfilePanel(controller);
    imageLocalisationPanel = new ImageL10nPanel();

    optionsPanelPanel = optionsPanel.rootPanel;
    addinsPanelPanel = addinsPanel.rootPanel;
    profilePanelPanel = profilePanel.rootPanel;
//    imageLocalisationPanelPanel = imageLocalisationPanel.rootPanel;
  }

  public void updateViewFromModel(Configuration c)
  {
    optionsPanel.updateViewFromModel(c);
    addinsPanel.updateViewFromModel(c);
    profilePanel.updateViewFromModel(c);
  }

  public void updateModelFromView(Configuration c)
  {
    optionsPanel.updateModelFromView(c);
    addinsPanel.updateModelFromView(c);
    profilePanel.updateModelFromView(c);
  }

}
