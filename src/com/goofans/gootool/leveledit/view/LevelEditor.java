package com.goofans.gootool.leveledit.view;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.logging.Logger;
import static java.util.logging.Level.*;
import java.text.MessageFormat;
import java.text.NumberFormat;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.TextProvider;
import com.goofans.gootool.leveledit.model.Level;
import com.goofans.gootool.leveledit.model.LevelContentsItem;
import com.goofans.gootool.leveledit.tools.*;
import com.goofans.gootool.leveledit.ui.Toolbar;
import com.goofans.gootool.util.GUIUtil;
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
  private JButton snapGridButton;
  private JLabel tooltip;
  private JLabel mousePos;
  private JSlider ballZoomSlider;
  private JLabel mouseItem;

//  private Tool currentTool;

  private static final String CMD_ZOOM_IN = "ZoomIn";
  private static final String CMD_ZOOM_OUT = "ZoomOut";
  private static final String CMD_UNDO = "Undo";
  private static final String CMD_REDO = "Redo";
  static final String CMD_SHOW_GRID = "ShowGrid";
  static final String CMD_SNAP_GRID = "SnapGrid";

  private TextProvider textProvider;
  private NumberFormat mousePosNumberFormat;

  private UndoManager undoManager;
  private DefaultMutableTreeNode rootNode;
  private Level level;

  private boolean showGrid, snapGrid;

  private LevelEditorMenuBar menu;

  public LevelEditor(Level level) throws IOException
  {
    super("Level Editor");
    this.level = level;

    menu = new LevelEditorMenuBar(this);
    setJMenuBar(menu);

    textProvider = GooTool.getTextProvider();

    setContentPane(contentPane);
//    setModal(true);
    levelDisplay.setLevel(level);

    undoManager = new UndoManager();

    updateUndoState();

    setLocationByPlatform(true);
    setMinimumSize(new Dimension(800, 500));
    setIconImage(GooTool.getMainIconImage());

    setDefaultCloseOperation(EXIT_ON_CLOSE);

    zoomInButton.addActionListener(this);
    zoomInButton.setActionCommand(CMD_ZOOM_IN);
    zoomOutButton.addActionListener(this);
    zoomOutButton.setActionCommand(CMD_ZOOM_OUT);
    undoButton.addActionListener(this);
    undoButton.setActionCommand(CMD_UNDO);
    redoButton.addActionListener(this);
    redoButton.setActionCommand(CMD_REDO);

    showGridButton.setActionCommand(CMD_SHOW_GRID);
    showGridButton.addActionListener(this);
    snapGridButton.setActionCommand(CMD_SNAP_GRID);
    snapGridButton.addActionListener(this);

    final LayersTableModel layersTableModel = new LayersTableModel(levelDisplay);

    layersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

//    TableColumnModel columnModel = addinTable.getColumnModel();

    layersTable.setModel(layersTableModel);
    layersTable.getColumnModel().getColumn(0).setPreferredWidth(20);
    layersTable.getColumnModel().getColumn(1).setPreferredWidth(100);
//    layersTable.getColumnModel().getColumn(0).setCellEditor(new CheckboxCellEditor());

    layersTable.setGridColor(Color.WHITE);

    layersTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        LevelDisplayLayer layer = null;
        int selectedRow = layersTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < LevelDisplayLayer.values().length) {
          layer = LevelDisplayLayer.values()[selectedRow];
        }
        populateLayer(layer);
      }
    });

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

    contentsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

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
          LevelContentsItem contentsItem = levelDisplay.checkHit(e.getPoint(), currentTool.getHitLayers());
          mouseItem.setText(contentsItem == null ? "" : contentsItem.toString());

          Cursor cursor = currentTool.getCursorAtPoint(e.getPoint(), worldCoords, contentsItem);
          levelDisplay.setCursor(cursor);
        }
        //TODO also set cursor when tool changed

        String strX = mousePosNumberFormat.format(worldCoords.x);
        String strY = mousePosNumberFormat.format(worldCoords.y);
        mousePos.setText(MessageFormat.format(textProvider.getText("leveledit.status.mousePos"), strX, strY));

        //TODO show zoom level in status bar
      }

      public void mouseDragged(MouseEvent e)
      {
      }
    });

    levelDisplay.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseExited(MouseEvent e)
      {
        mousePos.setText("");
      }
    });

    MouseProxy mouseProxy = new MouseProxy();
    levelDisplay.addMouseMotionListener(mouseProxy);
    levelDisplay.addMouseListener(mouseProxy);

    PanTool defaultTool = new PanTool();
    quickAddTool("pan", defaultTool);
    leftToolBar.addSeparator();
    quickAddTool("select", new SelectTool());

//    quickAddTool(leftToolBar, "selectmulti");
    quickAddTool("move", new MoveTool());
//    quickAddTool(leftToolBar, "rotate");
    leftToolBar.addSeparator();
    final BallTool ballTool = new BallTool();
    quickAddTool("ball", ballTool);
    quickAddTool("strand", new StrandTool());
//    quickAddTool(leftToolBar, "geomcircle");
//    quickAddTool(leftToolBar, "geomrectangle");
//    quickAddTool(leftToolBar, "pipe");
//    leftToolBar.addSeparator();
//    quickAddTool(leftToolBar, "camera");

    leftToolBar.setCurrentTool(defaultTool);

    ballZoomSlider.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent e)
      {
        ballPalette.setThumbnailSize(ballZoomSlider.getValue());
      }
    });

    ballPalette.addSelectionListener(new BallPalette.SelectionListener()
    {
      public void ballSelected(BallPaletteBall ball)
      {
        leftToolBar.setCurrentTool(ballTool);
      }
    });
  }

  public void doUndoableEdit(UndoableEdit edit)
  {
    undoManager.addEdit(edit);
    levelDisplay.repaint();
    updateUndoState();
  }

  private void updateUndoState()
  {
    boolean canUndo = undoManager.canUndo();
    undoButton.setEnabled(canUndo);
    undoButton.setToolTipText(canUndo ? undoManager.getUndoPresentationName() : textProvider.getText("leveledit.edit.cantundo"));

    boolean canRedo = undoManager.canRedo();
    redoButton.setEnabled(canRedo);
    redoButton.setToolTipText(canRedo ? undoManager.getRedoPresentationName() : textProvider.getText("leveledit.edit.cantredo"));
  }

  private void undo()
  {
    try {
      undoManager.undo();
    }
    catch (CannotUndoException e) {
      log.log(WARNING, "Can't undo", e);
      String msg = textProvider.getText("leveledit.edit.cantundo");
      JOptionPane.showMessageDialog(this, msg, msg, JOptionPane.ERROR);
    }
    levelDisplay.repaint();
    updateUndoState();
  }

  private void redo()
  {
    try {
      undoManager.redo();
    }
    catch (CannotRedoException e) {
      log.log(WARNING, "Can't redo", e);
      String msg = textProvider.getText("leveledit.edit.cantredo");
      JOptionPane.showMessageDialog(this, msg, msg, JOptionPane.ERROR);
    }
    levelDisplay.repaint();
    updateUndoState();
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
    else if (cmd.equals(CMD_UNDO)) {
      undo();
    }
    else if (cmd.equals(CMD_REDO)) {
      redo();
    }
    else if (cmd.equals(CMD_SHOW_GRID)) {
      showGrid = !showGrid;
      showGridButton.setSelected(showGrid);
      menu.showGridItem.setSelected(showGrid);
      levelDisplay.repaint();
    }
    else if (cmd.equals(CMD_SNAP_GRID)) {
      snapGrid = !snapGrid;
      snapGridButton.setSelected(snapGrid);
      menu.snapGridItem.setSelected(snapGrid);
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

  private void populateLayer(LevelDisplayLayer layer)
  {
    // TODO do nothing if layer wasn't changed
//    rootNode.add(new DefaultMutableTreeNode("def"));
//    rootNode.add(new DefaultMutableTreeNode("ghi"));
//    rootNode.add(new DefaultMutableTreeNode("jkl"));
//    rootNode.add(new DefaultMutableTreeNode("mno"));
    rootNode.removeAllChildren();
//    LevelDisplayLayer layer = LevelDisplayLayer.BALLS;

    System.out.println("layer = " + layer);

    DefaultTreeModel treeModel = (DefaultTreeModel) contentsTree.getModel();
    while (rootNode.getChildCount() > 0) {
      DefaultMutableTreeNode node = rootNode.getFirstLeaf();
      System.out.println("removing = " + node);
      treeModel.removeNodeFromParent(node);
    }

    Class[] layerClasses = LevelDisplay.layerContents.get(layer);
    if (layerClasses != null) {
      for (Class clazz : layerClasses) {
        System.out.println("clazz = " + clazz);
        java.util.List<LevelContentsItem> contents = level.getLevelContents().getLevelContents(clazz);
        for (LevelContentsItem content : contents) {
          System.out.println("content = " + content);
          rootNode.add(new DefaultMutableTreeNode(content.toString()));
        }
      }
    }

    /* TODO when selecting something from the LevelDisplay, tree.scrollPathToVisible(new TreePath(childNode.getPath())); */
  }

  private void createUIComponents()
  {
    levelDisplay = new LevelDisplay(this);

    rootNode = new DefaultMutableTreeNode("Root");

    contentsTree = new JTree(new DefaultTreeModel(rootNode));
//    contentsTree.setRootVisible(false);
    try {
      ballPalette = new BallPalette();
      ballPalette.addBalls();

      leftToolBar = new Toolbar(SwingConstants.VERTICAL);
    }
    catch (Exception e) {
      // TODO more robust
      log.log(SEVERE, "Can't create ball palette", e);
    }
  }

  private void quickAddTool(String toolName, final Tool tool) throws IOException
  {
    ImageIcon icon = new ImageIcon(ImageIO.read(Toolbar.class.getResourceAsStream("/leveledit/toolbar/" + toolName + ".png")));
    String tooltip = textProvider.getText("leveledit.tool." + toolName + ".tooltip");
    leftToolBar.addTool(tool, tooltip, icon);

    final String shortcut = textProvider.getText("leveledit.tool." + toolName + ".shortcut");
    if (shortcut.length() > 0) {
      KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcut);
      log.log(FINER, "Adding keystroke " + keyStroke + " for tool " + toolName);
      getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, toolName);
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

  public boolean isShowGrid()
  {
    return showGrid;
  }

  public boolean isSnapGrid()
  {
    return snapGrid;
  }

  /**
   * Proxies the mouse events to the selected tool.
   */
  private class MouseProxy implements MouseListener, MouseMotionListener
  {

    public void mouseClicked(MouseEvent e)
    {
      Tool currentTool = leftToolBar.getCurrentTool();
      if (currentTool instanceof MouseListener) {
        ((MouseListener) currentTool).mouseClicked(e);
      }
    }

    public void mousePressed(MouseEvent e)
    {
      Tool currentTool = leftToolBar.getCurrentTool();
      if (currentTool instanceof MouseListener) {
        ((MouseListener) currentTool).mousePressed(e);
      }
    }

    public void mouseReleased(MouseEvent e)
    {
      Tool currentTool = leftToolBar.getCurrentTool();
      if (currentTool instanceof MouseListener) {
        ((MouseListener) currentTool).mouseReleased(e);
      }
    }

    public void mouseEntered(MouseEvent e)
    {
      Tool currentTool = leftToolBar.getCurrentTool();
      if (currentTool instanceof MouseListener) {
        ((MouseListener) currentTool).mouseEntered(e);
      }
    }

    public void mouseExited(MouseEvent e)
    {
      Tool currentTool = leftToolBar.getCurrentTool();
      if (currentTool instanceof MouseListener) {
        ((MouseListener) currentTool).mouseExited(e);
      }
    }

    public void mouseDragged(MouseEvent e)
    {
      Tool currentTool = leftToolBar.getCurrentTool();
      if (currentTool instanceof MouseMotionListener) {
        ((MouseMotionListener) currentTool).mouseDragged(e);
      }
    }

    public void mouseMoved(MouseEvent e)
    {

      Tool currentTool = leftToolBar.getCurrentTool();
      if (currentTool instanceof MouseMotionListener) {
        ((MouseMotionListener) currentTool).mouseMoved(e);
      }
    }

  }


  public static void main(String[] args) throws IOException
  {
    GUIUtil.switchToSystemLookAndFeel();
//    DebugUtil.setAllLogging();

    WorldOfGoo.getTheInstance().init();

    Level level = new Level("GoingUp");

    LevelEditor dialog = new LevelEditor(level);
    dialog.pack();
    dialog.setLocationByPlatform(true);
    dialog.setVisible(true);
  }
}
