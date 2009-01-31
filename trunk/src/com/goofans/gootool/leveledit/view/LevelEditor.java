package com.goofans.gootool.leveledit.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.logging.Logger;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.TextProvider;
import com.goofans.gootool.leveledit.model.Level;
import com.goofans.gootool.leveledit.tools.*;
import com.goofans.gootool.leveledit.ui.Toolbar;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.wog.WorldOfGoo;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LevelEditor extends JFrame implements ActionListener
{
  private static final Logger log = Logger.getLogger(LevelEditor.class.getName());

  private JPanel contentPane;
  private JTree contentsTree;
  private JTable layersTable;
  private LevelDisplay levelDisplay;
  private JButton zoomInButton;
  private JButton zoomOutButton;
  private BallPalette ballPalette;
  private JButton loadButton;
  private JButton saveButton;
  private JButton groupButton;
  private JButton ungroupButton;
  private JButton undoButton;
  private JButton redoButton;
  private Toolbar leftToolBar;
  private JTable table1;
  private JButton showGridButton;
  private JButton snapToGridButton;
  private JLabel tooltip;
  private JLabel mousePos;
  private JSlider slider1;
  private JSlider ballZoomSlider;

//  private Tool currentTool;

  private static final String CMD_ZOOM_IN = "ZoomIn";
  private static final String CMD_ZOOM_OUT = "ZoomOut";
  private TextProvider textProvider;
  private NumberFormat mousePosNumberFormat;


  public LevelEditor(Level level) throws IOException
  {
    super("Level Editor");

    textProvider = GooTool.getTextProvider();

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
    
    mousePosNumberFormat = NumberFormat.getNumberInstance();
    mousePosNumberFormat.setMaximumFractionDigits(2);

//    levelDisplay.get
    levelDisplay.addMouseMotionListener(new MouseMotionListener()
    {
      public void mouseMoved(MouseEvent e)
      {
        Point.Double worldCoords = new Point.Double(levelDisplay.canvasToWorldX(e.getX()),
                levelDisplay.canvasToWorldY(e.getY()));

        Tool currentTool = leftToolBar.getCurrentTool();
        if (currentTool != null) {
          Cursor cursor = currentTool.getCursorAtPoint(worldCoords);
          levelDisplay.setCursor(cursor);
        }

        String strX = mousePosNumberFormat.format(worldCoords.x);
        String strY = mousePosNumberFormat.format(worldCoords.y);
        mousePos.setText(MessageFormat.format(textProvider.getText("leveledit.status.mousePos"), strX, strY)); // TODO remove mousepos on mouseout
      }

      public void mouseDragged(MouseEvent e)
      {
      }
    });


    PanTool defaultTool = new PanTool();
    quickAddTool("pan", defaultTool);
    leftToolBar.addSeparator();
    quickAddTool("select", new SelectTool());

//    quickAddTool(leftToolBar, "selectmulti");
    quickAddTool("move", new MoveTool());
//    quickAddTool(leftToolBar, "rotate");
    leftToolBar.addSeparator();
    quickAddTool("ball", new BallTool());
//    quickAddTool(leftToolBar, "strand");
//    quickAddTool(leftToolBar, "geomcircle");
//    quickAddTool(leftToolBar, "geomrectangle");
//    quickAddTool(leftToolBar, "pipe");
//    leftToolBar.addSeparator();
//    quickAddTool(leftToolBar, "camera");

    leftToolBar.setCurrentTool(defaultTool);
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
//      for (String toolCmd : tools.keySet()) {
//        if (cmd.equals(toolCmd)) {
//          System.out.println("select tool " + toolCmd);

//          for (String toolCmd2 : tools.keySet()) {
//            toolButtons.get(toolCmd2).setSelected(toolCmd.equals(toolCmd2));
//          }
//
//          currentTool = tools.get(toolCmd);
//          break;
//        }
//      }
    }
  }

  private void createUIComponents() throws IOException
  {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("abc");
    rootNode.add(new DefaultMutableTreeNode("def"));
    rootNode.add(new DefaultMutableTreeNode("ghi"));
    rootNode.add(new DefaultMutableTreeNode("jkl"));
    rootNode.add(new DefaultMutableTreeNode("mno"));

    contentsTree = new JTree(rootNode);

    ballPalette = new BallPalette();

    leftToolBar = new Toolbar(SwingConstants.VERTICAL);
  }

  private void quickAddTool(String toolName, final Tool tool) throws IOException
  {
    ImageIcon icon = new ImageIcon(ImageIO.read(Toolbar.class.getResourceAsStream("/leveledit/toolbar/" + toolName + ".png")));
    String tooltip = textProvider.getText("leveledit.tool." + toolName + ".tooltip");
    leftToolBar.addTool(tool, tooltip, icon);

    final String shortcut = textProvider.getText("leveledit.tool." + toolName + ".shortcut");
    if (shortcut.length() > 0) {
      KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcut);
      log.log(java.util.logging.Level.FINER, "Adding keystroke " + keyStroke + " for tool " + toolName);
      getRootPane().getInputMap().put(keyStroke, toolName);
      getRootPane().getActionMap().put(toolName, new AbstractAction()
      {
        public void actionPerformed(ActionEvent e)
        {
          System.out.println("Keypress " + shortcut);
          leftToolBar.setCurrentTool(tool);
        }
      });
    }
  }

  public static void main(String[] args) throws IOException
  {
    GUIUtil.switchToSystemLookAndFeel();
    DebugUtil.setAllLogging();

    WorldOfGoo.getTheInstance().init();

    Level level = new Level("GoingUp");

    LevelEditor dialog = new LevelEditor(level);
    dialog.pack();
    dialog.setLocationByPlatform(true);
    dialog.setVisible(true);
  }
}
