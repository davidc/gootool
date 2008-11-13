package com.goofans.gootool.view;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.profile.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class ProfilePanel implements ActionListener
{
  private static final Logger log = Logger.getLogger(ProfilePanel.class.getName());

  private JComboBox profilesCombo;
  private JLabel playTime;
  private JLabel levelsPlayed;
  public JPanel rootPanel;
  private JButton refreshButton;
  private JLabel profileName;
  private JLabel flags;
  private JTable levelsTable;
  private JPanel towerPanel;
  private JLabel towerTotalBalls;
  private JLabel towerNodeBalls;
  private JLabel towerStrandBalls;
  private JLabel towerHeight;

  private static final String CMD_REFRESH = "REFRESH";
  private static final String CMD_PROFILE_CHANGED = "PROFILE_CHANGED";
  private Profile currentProfile;


  private static final String[] COLUMN_NAMES;
  private ProfilePanel.LevelsTableModel levelsModel;

  static {
    // TODO load from resources
    COLUMN_NAMES = new String[]{"Level", "Most Balls", "Least Moves", "Least Time"};
  }


  public ProfilePanel()
  {
    refreshButton.setActionCommand(CMD_REFRESH);
    refreshButton.addActionListener(this);

    profilesCombo.setActionCommand(CMD_PROFILE_CHANGED);
    profilesCombo.addActionListener(this);

    levelsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    levelsModel = new LevelsTableModel();
    levelsTable.setModel(levelsModel);

    TableColumnModel columnModel = levelsTable.getColumnModel();

    columnModel.getColumn(0).setPreferredWidth(150);
    columnModel.getColumn(1).setPreferredWidth(50);
    columnModel.getColumn(2).setPreferredWidth(75);
    columnModel.getColumn(3).setPreferredWidth(150);

    levelsTable.getTableHeader().setReorderingAllowed(false);
//    levelsTable.so
// TODO sorting

    if (ProfileFactory.isProfileFound()) {
      loadProfiles();
    }
  }

  public void actionPerformed(ActionEvent e)
  {
    String cmd = e.getActionCommand();

    log.fine("cmd " + cmd);

    if (cmd.equals(CMD_REFRESH)) {
      if (!ProfileFactory.isProfileFound()) {
        JOptionPane.showMessageDialog(rootPanel, "Sorry, GooTool couldn't find your profile. Please specify the location of your profile on the Options tab.", "Profile not found", JOptionPane.ERROR_MESSAGE);
        return;
      }

      loadProfiles();
    }
    else if (cmd.equals(CMD_PROFILE_CHANGED) && profilesCombo.getSelectedItem() != currentProfile) {
      currentProfile = (Profile) profilesCombo.getSelectedItem();
      log.fine("currentProfile = " + currentProfile);

      if (currentProfile != null) {
        profileName.setText(currentProfile.getName());
        playTime.setText(formatTime(currentProfile.getPlayTime()));
        levelsPlayed.setText(String.valueOf(currentProfile.getLevels()));

        Tower t = currentProfile.getTower();

        towerHeight.setText(formatHeight(t.getHeight()));
        towerTotalBalls.setText(String.valueOf(t.getUsedStrandBalls() + t.getUsedNodeBalls()) + " of " + t.getTotalBalls());
        towerNodeBalls.setText(String.valueOf(t.getUsedNodeBalls()));
        towerStrandBalls.setText(String.valueOf(t.getUsedStrandBalls()));

        StringBuilder flagInfo = new StringBuilder();
        if (currentProfile.hasFlag(Profile.FLAG_ONLINE)) {
          flagInfo.append("Online Enabled. ");
        }
        if (currentProfile.hasFlag(Profile.FLAG_GOOCORP_UNLOCKED)) {
          flagInfo.append("GooCorp Unlocked. ");
        }
        if (currentProfile.hasFlag(Profile.FLAG_GOOCORP_DESTROYED)) {
          flagInfo.append("GooCorp Destroyed. ");
        }
        if (currentProfile.hasFlag(Profile.FLAG_WHISTLE)) {
          flagInfo.append("Whistle Found. ");
        }
        if (currentProfile.hasFlag(Profile.FLAG_TERMS)) {
          flagInfo.append("Terms Accepted. ");
        }
        if (currentProfile.hasFlag(32)) {
          flagInfo.append("Flag32. ");
        }
        if (currentProfile.hasFlag(64)) {
          flagInfo.append("Flag64. ");
        }
        if (currentProfile.hasFlag(128)) {
          flagInfo.append("Flag128. ");
        }

        if (flagInfo.length() == 0) {
          flagInfo.append("None.");
        }
        flags.setText(flagInfo.toString());

        levelsModel.fireTableDataChanged();

        towerPanel.removeAll();

        try {
          final TowerRenderer tr = new TowerRenderer(currentProfile.getTower());
          tr.render();

          BufferedImage thumbImg = tr.getThumbnail();
          JLabel thumb = new JLabel(new ImageIcon(thumbImg));
          thumb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//          thumb.setVerticalAlignment(SwingConstants.CENTER);
//          thumb.setHorizontalAlignment(SwingConstants.CENTER);

          Dimension thumbDim = new Dimension(thumbImg.getWidth(), thumbImg.getHeight());
          thumb.setSize(thumbDim);
          thumb.setMinimumSize(thumbDim);
          thumb.addMouseListener(new MouseAdapter()
          {
            public void mouseClicked(MouseEvent e)
            {
              // TODO make this a proper gui frame, with scrollbars etc.
              // TODO parent frame
              final JDialog d = new JDialog();
              d.setTitle("Tower");
              BufferedImage prettyImg = tr.getPretty();
              d.setPreferredSize(new Dimension(prettyImg.getWidth(), prettyImg.getHeight()));
              d.setIconImage(GooTool.getMainIconImage());
              JLabel jLabel = new JLabel(new ImageIcon(prettyImg));
              jLabel.setBorder(new LineBorder(Color.BLACK));
              d.add(jLabel);
              d.setModal(true);
              d.pack();
              d.setLocationByPlatform(true);

              d.addKeyListener(new KeyAdapter()
              {
                public void keyPressed(KeyEvent e)
                {
                  if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    d.setVisible(false);
                  }
                }
              });

              d.setVisible(true);
            }
          });
          towerPanel.add(thumb);
        }
        catch (IOException e1) {
          log.log(Level.SEVERE, "Unable to render tower", e);
          towerPanel.add(new JLabel("Sorry, couldn't render your tower."));
        }

      }
    }
  }

  private void loadProfiles()
  {
    profilesCombo.removeAllItems();

    ProfileData profileData;
    try {
      profileData = ProfileFactory.getProfileData();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to read profile", e);
      JOptionPane.showMessageDialog(rootPanel, "Sorry, GooTool can't read your profile.", "Profile corrupt", JOptionPane.ERROR_MESSAGE);
      return;
    }

    for (Profile profile : profileData.getProfiles()) {
      if (profile != null) {
        profilesCombo.addItem(profile);
      }
    }
    profilesCombo.setSelectedItem(profileData.getCurrentProfile());
  }

  private String formatTime(int secs)
  {
    StringBuilder sb = new StringBuilder();

    int days = secs / 86400;
    if (days > 0) {
      sb.append(days).append(" day");
      if (days != 1) sb.append("s");
      sb.append(", ");
      secs %= 86400;
    }

    int hours = secs / 3600;
    if (days > 0 || hours > 0) {
      sb.append(hours).append(" hour");
      if (hours != 1) sb.append("s");
      sb.append(", ");
      secs %= 3600;
    }

    int minutes = secs / 60;
    if (days > 0 || hours > 0 || minutes > 0) {
      sb.append(minutes).append(" minute");
      if (minutes != 1) sb.append("s");
      sb.append(", ");
      secs %= 60;
    }
    sb.append(secs).append(" second");
    if (secs != 1) sb.append("s");
    sb.append(".");

    return sb.toString();
  }

  private String formatHeight(double height)
  {
    NumberFormat nf = NumberFormat.getNumberInstance();
    return nf.format(height) + " m";
  }


  private class LevelsTableModel extends AbstractTableModel
  {
    public int getRowCount()
    {
      return currentProfile != null ? currentProfile.getLevelAchievements().size() : 0;
    }

    public int getColumnCount()
    {
      return 4;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
      LevelAchievement levelAchievement = currentProfile.getLevelAchievements().get(rowIndex);

      if (columnIndex == 0) return levelAchievement.getLevelId();
      if (columnIndex == 1) return levelAchievement.getMostBalls();
      if (columnIndex == 2) return levelAchievement.getLeastMoves();
      if (columnIndex == 3) return levelAchievement.getLeastTime();

      return null;
    }

    public String getColumnName(int column)
    {
      return COLUMN_NAMES[column];
    }
  }
}
