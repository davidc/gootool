/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

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

  private final List<L10nLanguage> languages = new ArrayList<L10nLanguage>();

  @SuppressWarnings({"HardCodedStringLiteral"})
  public LanguagesTableModel()
  {
    languages.add(new L10nLanguage("nl", "Dutch_translation", true));
    languages.add(new L10nLanguage("fr", "French_translation", true));
    languages.add(new L10nLanguage("de", "German_translation", false));
    languages.add(new L10nLanguage("it", "Italian_translation", false));
    languages.add(new L10nLanguage("pl", "Polish_translation", false));
    languages.add(new L10nLanguage("ru", "Russian_translation", false));
    languages.add(new L10nLanguage("es", "Spanish_translation", false));
    languages.add(new L10nLanguage("sv", "Swedish_translation", false));
    languages.add(new L10nLanguage("cs", "Czech_translation", false));
    languages.add(new L10nLanguage("da", "Danish_translation", false));
    languages.add(new L10nLanguage("eo", "Esperanto_translation", false));
    languages.add(new L10nLanguage("he", "Hebrew_translation", false));
    languages.add(new L10nLanguage("hu", "Hungarian_translation", false));
    languages.add(new L10nLanguage("ja", "Japanese_translation", false));
    languages.add(new L10nLanguage("no", "Norwegian_translation", false));
  }

  public int getRowCount()
  {
    return languages.size();
  }

  public int getColumnCount()
  {
    return COLUMN_CLASSES.length;
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
    if (columnIndex == 0) return languages.get(rowIndex).enabled;
    if (columnIndex == 1) return languages.get(rowIndex).code;
    if (columnIndex == 2) return languages.get(rowIndex).wikiPage;
    return null;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex)
  {
    return true;//columnIndex == 0;
  }
  
  @Override
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
    languages.remove(row);
    fireTableDataChanged();
  }

  public void addRow()
  {
    languages.add(new L10nLanguage("xx", "Foo_translation", false)); //NON-NLS
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
