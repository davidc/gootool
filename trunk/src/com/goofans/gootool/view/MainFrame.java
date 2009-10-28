package com.goofans.gootool.view;

import com.goofans.gootool.Controller;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.l10n.ImageL10nPanel;
import com.goofans.gootool.model.Configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class MainFrame extends JFrame implements ViewComponent
{
//  private static final Logger log = Logger.getLogger(MainFrame.class.getName());

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
    super(GooTool.getTextProvider().getText("mainFrame.title"));
    this.controller = controller;

    setLocationByPlatform(true);
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

    saveAndLaunchButton.setActionCommand(Controller.CMD_SAVE_AND_LAUNCH);
    saveAndLaunchButton.addActionListener(controller);

    saveButton.setActionCommand(Controller.CMD_SAVE);
    saveButton.addActionListener(controller);

    mainMenu = new MainMenu(controller);
    setJMenuBar(mainMenu.getJMenuBar());
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
