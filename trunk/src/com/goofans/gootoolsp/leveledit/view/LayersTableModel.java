package com.goofans.gootoolsp.leveledit.view;

import javax.swing.table.AbstractTableModel;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LayersTableModel extends AbstractTableModel
{
  private static final String[] COLUMNS = new String[]{"Vis", "Name"};
  private static final Class[] COLUMN_CLASSES = new Class[]{Boolean.class, String.class};
  private LevelDisplay levelDisplay;

//  public static final String[] LAYERS = new String[]{"Images", "Geometry", "Boundaries"};

//  private boolean visibility[] = new boolean[LevelDisplayLayer.values().length];

  public LayersTableModel(LevelDisplay levelDisplay)
  {
    this.levelDisplay = levelDisplay;
//    for (int i = 0; i < LAYERS.length; i++) {
//      String layerName = LAYERS[i];
//      visibility[i] = true;
//      labels[i] = new JLabel(layerName);
//    }
  }

  public int getRowCount()
  {
    return LevelDisplayLayer.values().length;
  }

  public int getColumnCount()
  {
    return COLUMNS.length;
  }

  public String getColumnName(int columnIndex)
  {
    return COLUMNS[columnIndex];
  }

  public Class<?> getColumnClass(int columnIndex)
  {
    return COLUMN_CLASSES[columnIndex];
  }


  public Object getValueAt(int rowIndex, int columnIndex)
  {
    if (columnIndex == 0) return levelDisplay.isLayerVisibile(LevelDisplayLayer.values()[rowIndex]);
    if (columnIndex == 1) return LevelDisplayLayer.values()[rowIndex].toString();
    return null;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return columnIndex == 0;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    if (columnIndex != 0) return;

    levelDisplay.setLayerVisibile(LevelDisplayLayer.values()[rowIndex], (Boolean) aValue);
//    fireTableCellUpdated(rowIndex, columnIndex);
  }
}
