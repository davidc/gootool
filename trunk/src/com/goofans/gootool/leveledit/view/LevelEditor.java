package com.goofans.gootool.leveledit.view;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.leveledit.model.Level;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.wog.WorldOfGoo;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LevelEditor extends JFrame implements ActionListener
{
  private JPanel contentPane;
  private JButton loadButton;
  private JButton saveButton;
  private JButton panButton;
  private JButton selectButton;
  private JTable table1;
  private JTree contentsTree;
  private JTable layersTable;
  private LevelDisplay levelDisplay;
  private JButton zoomInButton;
  private JButton zoomOutButton;

  Tool currentTool = null;

  private static final String CMD_ZOOM_IN = "ZoomIn";
  private static final String CMD_ZOOM_OUT = "ZoomOut";
  private static final String CMD_TOOL_PAN = "Tool>Pan";
  private static final String CMD_TOOL_SELECT = "Tool>Select";

  private Map<String, Tool> tools;
  private Map<String, JButton> toolButtons;

 
  public LevelEditor(Level level)
  {
    super("Level Editor");
    setContentPane(contentPane);
//    setModal(true);
    levelDisplay.setLevel(level);


    setLocationByPlatform(true);
    setMinimumSize(new Dimension(800, 500));
    setIconImage(GooTool.getMainIconImage());

    setDefaultCloseOperation(EXIT_ON_CLOSE);

    zoomInButton.addActionListener(this);
    zoomInButton.setActionCommand(CMD_ZOOM_IN);
    zoomOutButton.addActionListener(this);
    zoomOutButton.setActionCommand(CMD_ZOOM_OUT);

    tools = new HashMap<String, Tool>();
    toolButtons = new HashMap<String, JButton>();
    addTool(panButton, CMD_TOOL_PAN, new PanTool());
    addTool(selectButton, CMD_TOOL_SELECT, new SelectTool());

    final LayersTableModel layersTableModel = new LayersTableModel(levelDisplay);

    layersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

//    TableColumnModel columnModel = addinTable.getColumnModel();

    layersTable.setModel(layersTableModel);
    layersTable.getColumnModel().getColumn(0).setPreferredWidth(20);
    layersTable.getColumnModel().getColumn(1).setPreferredWidth(100);
//    layersTable.getColumnModel().getColumn(0).setCellEditor(new CheckboxCellEditor());

    layersTable.setGridColor(Color.WHITE);

  /*  layersTableModel.addTableModelListener(new TableModelListener()
    {
      public void tableChanged(TableModelEvent e)
      {
        if (e.getType() == TableModelEvent.UPDATE) {
          levelDisplay.setLayerVisibility((Boolean) layersTableModel.getValueAt(0, 0),
                  (Boolean) layersTableModel.getValueAt(1, 0),
                  (Boolean) layersTableModel.getValueAt(2, 0));
        }
      }
    });*/

//    levelDisplay.get
    levelDisplay.addMouseMotionListener(new MouseMotionListener()
    {
      public void mouseMoved(MouseEvent e)
      {
        if (currentTool != null) {
          // TODO ask display to convert x,y to world coordinates
          Cursor cursor = currentTool.getCursorAtPoint(new Point2D.Double(e.getX(), e.getY()));
          levelDisplay.setCursor(cursor);
        }
      }

      public void mouseDragged(MouseEvent e)
      {
      }
    });
  }

  private void addTool(JButton button, String cmd, Tool tool)
  {
    tools.put(cmd, tool);
    toolButtons.put(cmd, button);
    button.addActionListener(this);
    button.setActionCommand(cmd);
  }

  public void actionPerformed(ActionEvent e)
  {
    String cmd = e.getActionCommand();
    if (cmd.equals(CMD_ZOOM_IN)) {
      levelDisplay.setScale(levelDisplay.getScale() * 2);
    }
    else if (cmd.equals(CMD_ZOOM_OUT)) {
      levelDisplay.setScale(levelDisplay.getScale() / 2);
    }
    else {
      for (String toolCmd : tools.keySet()) {
        if (cmd.equals(toolCmd)) {
//          System.out.println("select tool " + toolCmd);

          for (String toolCmd2 : tools.keySet()) {
            toolButtons.get(toolCmd2).setSelected(toolCmd.equals(toolCmd2));
          }

          currentTool = tools.get(toolCmd);
          break;
        }
      }
    }
  }

  private void createUIComponents()
  {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("abc");
    rootNode.add(new DefaultMutableTreeNode("def"));
    rootNode.add(new DefaultMutableTreeNode("ghi"));
    rootNode.add(new DefaultMutableTreeNode("jkl"));
    rootNode.add(new DefaultMutableTreeNode("mno"));

    contentsTree = new JTree(rootNode);
  }

  public static void main(String[] args) throws IOException
  {
    GUIUtil.switchToSystemLookAndFeel();

    WorldOfGoo.init();

    Level level = new Level(WorldOfGoo.getWogDir(), "MapWorldView");

    LevelEditor dialog = new LevelEditor(level);
    dialog.pack();
    dialog.setVisible(true);
  }
}
