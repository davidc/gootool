/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

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
  private final LevelDisplay levelDisplay;

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

  @Override
  public String getColumnName(int columnIndex)
  {
    return COLUMNS[columnIndex];
  }

  @Override
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

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return columnIndex == 0;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    if (columnIndex != 0) return;

    levelDisplay.setLayerVisibile(LevelDisplayLayer.values()[rowIndex], (Boolean) aValue);
//    fireTableCellUpdated(rowIndex, columnIndex);
  }
}
