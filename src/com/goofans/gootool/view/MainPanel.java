/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import java.awt.*;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.GooToolResourceBundle;
import com.goofans.gootool.MainController;
import com.goofans.gootool.projects.Project;
import com.goofans.gootool.projects.ProjectManager;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MainPanel
{
  private JComboBox currentProjectCombo;
  private JButton projectPropertiesButton;
  private JButton projectDeleteButton;
  private JButton projectAddButton;
  private JPanel contentPanel;
  JPanel rootPanel;
  public ProjectPanel projectPanel;

  private MainController mainController;
  private JLabel noProjectsText;

  public MainPanel(MainController mainController)
  {
    this.mainController = mainController;

    currentProjectCombo.setActionCommand(MainController.CMD_PROJECT_SELECT);
    currentProjectCombo.addActionListener(mainController);

    projectAddButton.setActionCommand(MainController.CMD_PROJECT_ADD);
    projectAddButton.addActionListener(mainController);

    projectPropertiesButton.setActionCommand(MainController.CMD_PROJECT_PROPERTIES);
    projectPropertiesButton.addActionListener(mainController);

    projectDeleteButton.setActionCommand(MainController.CMD_PROJECT_DELETE);
    projectDeleteButton.addActionListener(mainController);

    GooToolResourceBundle resourceBundle = GooTool.getTextProvider();

    projectPanel = new ProjectPanel();

    noProjectsText = new JLabel(resourceBundle.getString("mainPanel.project.noProjects"));
//    noProjectsText.setHorizontalAlignment(JTextField.CENTER);
//    noProjectsText.setBackground(null);
//    noProjectsText.setFocusable(false);
//    noProjectsText.setEditable(false);
//    noProjectsText.set
    noProjectsText.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
//    noProjectsText.setFont(UIManager.getFont());
//    noProjectsText
    noProjectsText.setHorizontalAlignment(JLabel.CENTER);

//    updateProjectsCombo();
  }

  public final void updateProjectsCombo()
  {
    System.err.println("update projects combo");

    Project currentProject = (Project) currentProjectCombo.getSelectedItem();

    currentProjectCombo.removeAllItems();

    for (Project project : ProjectManager.getProjects()) {
      if (project != null) {
        currentProjectCombo.addItem(project);
      }
    }

    if (currentProject != null) {
      System.out.println("currentProject = " + currentProject);
      currentProjectCombo.setSelectedItem(currentProject);
    }
    else {
      System.out.println("currentProject = " + currentProject);
      currentProjectCombo.setSelectedItem(null);
    }
    updateButtonStates(currentProject);


    // TODO only do this if it's changed from empty to non-empty or vice versa.
    contentPanel.removeAll();
    if (ProjectManager.getProjects().isEmpty()) {
      contentPanel.add(noProjectsText, BorderLayout.CENTER);
    }
    else {
      contentPanel.add(projectPanel.rootPanel, BorderLayout.CENTER);
    }
    contentPanel.invalidate();
    contentPanel.repaint();
  }

  private void createUIComponents()
  {
    System.err.println("creating mainpanel");

  }

/*  public void updateViewFromModel(ProjectModel model)
  {
    projectPanel.updateViewFromModel(model);
  }

  public void updateModelFromView(ProjectModel model)
  {
    projectPanel.updateModelFromView(model);
  }*/

  public Project getSelectedProject()
  {
    return (Project) currentProjectCombo.getSelectedItem();
  }

  public void setSelectedProject(Project project)
  {
    System.out.println("Set Selected Project " + project);
    currentProjectCombo.setSelectedItem(project);
    updateButtonStates(project);
  }

  private void updateButtonStates(Project project)
  {
    if (project == null) {
      projectPropertiesButton.setEnabled(false);
      projectDeleteButton.setEnabled(false);
    }
    else {
      projectPropertiesButton.setEnabled(true);
      projectDeleteButton.setEnabled(true);
    }
  }
}
