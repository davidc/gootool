package com.goofans.gootool.view;

import com.goofans.gootool.Controller;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.model.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MainFrame extends JFrame
{
  private static final Logger log = Logger.getLogger(MainFrame.class.getName());

  private JTabbedPane tabbedPane1;
  private JPanel rootPanel;
  private JButton saveButton;
  private JButton saveAndLaunchButton;
  private JPanel optionsPanelPanel;
  private JPanel addinsPanelPanel;
  private JPanel profilePanelPanel;

  private Controller controller;

  public AddinsPanel addinsPanel;
  public OptionsPanel optionsPanel;

  public MainFrame(final Controller controller)
  {
    super("World of Goo Tool");
    this.controller = controller;

    setLocationByPlatform(true);
    setMinimumSize(new Dimension(800, 500));
    setIconImage(GooTool.getTheInstance().getMainIcon());


    setContentPane(rootPanel);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        controller.maybeExit();
      }
    });

    saveAndLaunchButton.setActionCommand(Controller.CMD_SAVE_AND_LAUNCH);
    saveAndLaunchButton.addActionListener(controller);

    saveButton.setActionCommand(Controller.CMD_SAVE);
    saveButton.addActionListener(controller);

    MainMenu mainMenu = new MainMenu(controller);
    setJMenuBar(mainMenu.getJMenuBar());
  }

  private void createUIComponents()
  {
    addinsPanel = new AddinsPanel(controller);
    optionsPanel = new OptionsPanel(this.controller);
    
    optionsPanelPanel = optionsPanel.rootPanel;
    addinsPanelPanel = addinsPanel.rootPanel;
    profilePanelPanel = new ProfilePanel().rootPanel;
  }

  public void updateViewFromModel(Configuration c)
  {
    optionsPanel.updateViewFromModel(c);
    addinsPanel.updateViewFromModel(c);
  }

  public void updateModelFromView(Configuration c)
  {
    optionsPanel.updateModelFromView(c);
    addinsPanel.updateModelFromView(c);
  }

}
