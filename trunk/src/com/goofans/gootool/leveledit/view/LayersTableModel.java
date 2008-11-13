package com.goofans.gootool.leveledit.view;

import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.TableModelListener;
import javax.swing.*;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LayersTableModel extends AbstractTableModel
{
  private static final String[] COLUMNS = new String[]{"Vis", "Name"};
  private static final Class[] COLUMN_CLASSES = new Class[]{Boolean.class, String.class};

  public static final String[] LAYERS = new String[]{"Images", "Geometry", "Boundaries"};

  private boolean visibility[] = new boolean[LAYERS.length];
//  private JLabel labels[] = new JLabel[LAYERS.length];

  public LayersTableModel()
  {
    for (int i = 0; i < LAYERS.length; i++) {
      String layerName = LAYERS[i];
      visibility[i] = true;
//      labels[i] = new JLabel(layerName);
    }
  }

  public int getRowCount()
  {
    return LAYERS.length;
  }

  public int getColumnCount()
  {
    return 2;
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
    if (columnIndex == 0) return visibility[rowIndex];
    if (columnIndex == 1) return LAYERS[rowIndex];
    return null;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return columnIndex == 0;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    if (columnIndex != 0) return;

    visibility[rowIndex] = (Boolean) aValue;
    fireTableCellUpdated(rowIndex, columnIndex);
  }
}
