package com.goofans.gootool.l10n;

import javax.swing.table.AbstractTableModel;

import java.util.List;
import java.util.ArrayList;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class LanguagesTableModel extends AbstractTableModel
{
  private static final String[] COLUMNS = new String[]{"Active", "Code", "Wiki Page"};
  private static final Class[] COLUMN_CLASSES = new Class[]{Boolean.class, String.class, String.class};

  private List<L10nLanguage> languages = new ArrayList<L10nLanguage>();

  public LanguagesTableModel()
  {
    languages.add(new L10nLanguage("de", "German_translation", true));
  }

  public int getRowCount()
  {
    return languages.size();
  }

  public int getColumnCount()
  {
    return COLUMN_CLASSES.length;
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
    if (columnIndex == 0) return languages.get(rowIndex).enabled;
    if (columnIndex == 1) return languages.get(rowIndex).code;
    if (columnIndex == 2) return languages.get(rowIndex).wikiPage;
    return null;
  }

  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return true;//columnIndex == 0;
  }
  
  public void setValueAt(Object aValue, int rowIndex, int columnIndex)
  {
    L10nLanguage language = languages.get(rowIndex);
    if (columnIndex == 0) language.enabled = (Boolean) aValue;
    if (columnIndex == 1) {
      String code = (String) aValue;
      if (code.length() != 2) {
      }
      language.code = code;
    }
    if (columnIndex == 2) language.wikiPage = (String) aValue;
  }

  public void removeRow(int row)
  {
    System.out.println("row = " + row);
    languages.remove(row);
    fireTableDataChanged();
  }
  public void addRow()
  {
    languages.add(new L10nLanguage("xx", "Foo_translation", false));
    fireTableDataChanged();
  }

  public List<L10nLanguage> getLanguages()
  {
    return languages;
  }

  public static class L10nLanguage
  {
    public L10nLanguage(String code, String wikiPage, boolean enabled)
    {
      this.enabled = enabled;
      this.code = code;
      this.wikiPage = wikiPage;
    }

    public boolean enabled;
    public String code;
    public String wikiPage;
  }
}
