/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import javax.swing.*;
import java.util.logging.Logger;

import com.goofans.gootool.ProjectController;
import com.goofans.gootool.model.ProjectModel;

/**
 * GooTool's main frame on the screen.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProjectPanel extends JPanel implements ViewComponent
{
//  private static final Logger log = Logger.getLogger(ProjectPanel.class.getName());

  JPanel rootPanel;
  private JButton saveButton;
  private JButton saveAndLaunchButton;
  private JPanel optionsPanelPanel;
  private JPanel addinsPanelPanel;
  private JPanel profilePanelPanel;
  public JTabbedPane tabbedPane;

  public AddinsPanel addinsPanel;
  private OptionsPanel optionsPanel;
  public ProfilePanel profilePanel;

  ProjectPanel()
  {
  }

  private void createUIComponents()
  {
//    System.err.println("creating projectpanel");
    optionsPanel = new OptionsPanel();
    addinsPanel = new AddinsPanel();
    profilePanel = new ProfilePanel();

    optionsPanelPanel = optionsPanel.rootPanel;
    addinsPanelPanel = addinsPanel.rootPanel;
    profilePanelPanel = profilePanel.rootPanel;
  }

  public void initController(ProjectController projectController)
  {
    saveAndLaunchButton.setActionCommand(ProjectController.CMD_SAVE_AND_LAUNCH);
    saveAndLaunchButton.addActionListener(projectController);

    saveButton.setActionCommand(ProjectController.CMD_SAVE);
    saveButton.addActionListener(projectController);

    optionsPanel.initController(projectController);
    addinsPanel.initController(projectController);
    profilePanel.initController(projectController);
  }

  public void updateViewFromModel(ProjectModel model)
  {
    if (model == null) {
      // TODO remove the panels!
    }
    else {
      optionsPanel.updateViewFromModel(model);
      addinsPanel.updateViewFromModel(model);
      profilePanel.updateViewFromModel(model);
    }
  }

  public void updateModelFromView(ProjectModel model)
  {
    if (model != null) {
      optionsPanel.updateModelFromView(model);
      addinsPanel.updateModelFromView(model);
      profilePanel.updateModelFromView(model);
    }
  }

}
