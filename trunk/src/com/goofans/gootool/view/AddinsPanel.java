package com.goofans.gootool.view;

import com.goofans.gootool.Controller;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.ToolPreferences;
import com.goofans.gootool.siteapi.RatingSubmitRequest;
import com.goofans.gootool.siteapi.APIException;
import com.goofans.gootool.util.HyperlinkLaunchingListener;
import com.goofans.gootool.ui.HyperlinkLabel;
import com.goofans.gootool.ui.StarBar;
import com.goofans.gootool.model.Configuration;
import com.goofans.gootool.addins.Addin;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Map;
import java.net.URL;
import java.net.MalformedURLException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AddinsPanel implements ViewComponent, PropertyChangeListener
{
  private static final Logger log = Logger.getLogger(AddinsPanel.class.getName());

  public JTable addinTable;
  public JPanel rootPanel;

  private JButton installButton;
  private JButton uninstallButton;
  private JButton enableButton;
  private JButton disableButton;
  private JButton propertiesButton;
  private JLabel description;
  private JButton moveUpButton;
  private JButton moveDownButton;
  private HyperlinkLabel findMoreHyperlink;
  private StarBar ratingBar;
  private MyTableModel addinsModel;

  private Controller controller;

  private static final String[] COLUMN_NAMES;
  private static final Class[] COLUMN_CLASSES = new Class[]{String.class, String.class, String.class, String.class, Boolean.class};
  private static final int RATING_FACTOR = 20;

  static {
    // TODO load from resources
    COLUMN_NAMES = new String[]{"Mod Name", "Type", "Version", "Author", "Enabled"};
  }


  public AddinsPanel(Controller controller)
  {
    this.controller = controller;
    addinsModel = new MyTableModel();

    addinTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    addinTable.setModel(addinsModel);

    TableColumnModel columnModel = addinTable.getColumnModel();

    log.finer("Columns in table = " + columnModel.getColumnCount());
    columnModel.getColumn(0).setPreferredWidth(150);
    columnModel.getColumn(1).setPreferredWidth(50);
    columnModel.getColumn(2).setPreferredWidth(75);
    columnModel.getColumn(3).setPreferredWidth(150);
    columnModel.getColumn(4).setPreferredWidth(25);

    addinTable.getTableHeader().setReorderingAllowed(false);

    addinTable.setDragEnabled(true);
    addinTable.setTransferHandler(new MyTransferHandler(controller));

    // TODO 1.6
//    addinTable.setDropMode(DropMode.INSERT_ROWS);

    addinTable.doLayout();
    addinTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        updateButtonStates();
      }
    });

    propertiesButton.setActionCommand(Controller.CMD_ADDIN_PROPERTIES);
    propertiesButton.addActionListener(controller);
    installButton.setActionCommand(Controller.CMD_ADDIN_INSTALL);
    installButton.addActionListener(controller);
    uninstallButton.setActionCommand(Controller.CMD_ADDIN_UNINSTALL);
    uninstallButton.addActionListener(controller);
    enableButton.setActionCommand(Controller.CMD_ADDIN_ENABLE);
    enableButton.addActionListener(controller);
    disableButton.setActionCommand(Controller.CMD_ADDIN_DISABLE);
    disableButton.addActionListener(controller);

    ratingBar.addPropertyChangeListener("rating", this);

    try {
      findMoreHyperlink.setURL(new URL("http://goofans.com/"));
      findMoreHyperlink.addHyperlinkListener(new HyperlinkLaunchingListener(rootPanel));
    }
    catch (MalformedURLException e) {
      log.log(Level.WARNING, "Unable to make GooFans URL", e);
    }
  }

  private void updateButtonStates()
  {
    int row = addinTable.getSelectedRow();

    if (row < 0 || controller.getDisplayAddins().isEmpty()) {
      description.setText(null);
      propertiesButton.setEnabled(false);
      enableButton.setEnabled(false);
      disableButton.setEnabled(false);
      uninstallButton.setEnabled(false);
      ratingBar.setEnabled(false);
      ratingBar.setRatingQuietly(0);
    }
    else {
      Addin addin = controller.getDisplayAddins().get(row);
      boolean isEnabled = controller.getEditorConfig().isEnabledAdddin(addin.getId());

      if (addin.getDescription().startsWith("<html>")) {
        description.setText(addin.getDescription());
      }
      else {
        // add <html> to make sure it wraps, replace newlines with <br>, remove any HTML that may be in there already.
        description.setText("<html>" + addin.getDescription().replaceAll("<", "&lt;").replaceAll("\n", "<br>") + "</html>");
      }
      propertiesButton.setEnabled(true);
      enableButton.setEnabled(!isEnabled);
      disableButton.setEnabled(isEnabled);
      uninstallButton.setEnabled(true);

      // Maybe enable the rating bar

      if (ToolPreferences.isGooFansLoginOk()) {
        ratingBar.setEnabled(true);

        // See if we've rated this addin
        Integer rating = ToolPreferences.getRatings().get(addin.getId());
        if (rating == null) {
          ratingBar.setRatingQuietly(0);
        }
        else {
          ratingBar.setRatingQuietly(rating / RATING_FACTOR);
        }
      }
      else {
        ratingBar.setEnabled(false);
        ratingBar.setRatingQuietly(0);
      }
    }
  }


  public void updateViewFromModel(Configuration c)
  {
    int selectedRow = addinTable.getSelectedRow();
    addinsModel.fireTableDataChanged();
    addinTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
    updateButtonStates();
  }

  public void updateModelFromView(Configuration c)
  {
  }

  private void createUIComponents()
  {
    findMoreHyperlink = new HyperlinkLabel(GooTool.getTextProvider().getText("addins.getmore"));
  }

  public void propertyChange(PropertyChangeEvent evt)
  {
    if (evt.getSource() instanceof StarBar && evt.getPropertyName().equals("rating")) {
      int row = addinTable.getSelectedRow();

      if (row >= 0 && !controller.getDisplayAddins().isEmpty()) {
        final Addin addin = controller.getDisplayAddins().get(row);
        final int rating = (Integer) evt.getNewValue() * RATING_FACTOR;

        /* Update our ToolPreferences for this addin */
        Map<String, Integer> ratings = ToolPreferences.getRatings();
        ratings.put(addin.getId(), rating);
        ToolPreferences.setRatings(ratings);

        /* Send the rating update to the server */
        GooTool.executeTaskInThreadPool(new Runnable()
        {
          public void run()
          {
            try {
              new RatingSubmitRequest().submitRating(addin.getId(), rating);
            }
            catch (APIException e) {
              log.log(Level.SEVERE, "Unable to submit rating for " + addin.getId(), e);
            }
          }
        });
      }
    }
  }

  private class MyTableModel extends AbstractTableModel
  {
    public int getRowCount()
    {
      return controller.getDisplayAddins().size();
    }

    public int getColumnCount()
    {
      return 5;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
      Addin addin = controller.getDisplayAddins().get(rowIndex);

      if (columnIndex == 0) return addin.getName();
      if (columnIndex == 1) return addin.getTypeText();
      if (columnIndex == 2) return addin.getVersion();
      if (columnIndex == 3) return addin.getAuthor();
      if (columnIndex == 4) return controller.getEditorConfig().isEnabledAdddin(addin.getId());

      return null;
    }

    @Override
    public String getColumnName(int column)
    {
      return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
      return COLUMN_CLASSES[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
      return columnIndex == 4;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
      if (columnIndex == 4) {
        //TODO this should really call the controller to do this.
        Addin addin = controller.getDisplayAddins().get(rowIndex);
        if ((Boolean) aValue) {
          controller.getEditorConfig().enableAddin(addin.getId());
        }
        else {
          controller.getEditorConfig().disableAddin(addin.getId());
        }
        updateViewFromModel(controller.getEditorConfig());
      }
    }
  }

  // Following code handles drag and drop

  private class MyTransferHandler extends TransferHandler
  {
    Controller controller;

    public MyTransferHandler(Controller controller)
    {
      this.controller = controller;
    }

    public int getSourceActions(JComponent c)
    {
      return MOVE;
    }

    protected Transferable createTransferable(JComponent c)
    {
      int row = ((JTable) c).getSelectedRow();
      if (row < 0) return null;

      String addinId = controller.getDisplayAddins().get(row).getId();

      if (!controller.getEditorConfig().isEnabledAdddin(addinId)) {
        return null;
      }

      return new MyTransferable(row);
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors)
    {
      for (DataFlavor transferFlavor : transferFlavors) {
        if (transferFlavor == FLAVOR) {
          return true;
        }
      }
      return false;
    }
//    public boolean canImport(TransferSupport support)
//    {
//      if (!support.isDataFlavorSupported(FLAVOR))
//      return false;


//      DropLocation dropLocation = support.getDropLocation();
//
//      if (!(support.getDropLocation() instanceof JTable.DropLocation)) {
//        return false;
//      }
//       TODO check they're not dropping it below the end of hte table
//      return true;
//    }

    public boolean importData(JComponent comp, Transferable t)
    {
      return super.importData(comp, t);    //To change body of overridden methods use File | Settings | File Templates.
    }

//    public boolean importData(TransferSupport support)
//    {
//      if (!support.isDrop()) return false;
//
//      DropLocation dropLocation = support.getDropLocation();
//
//      if (!(support.getDropLocation() instanceof JTable.DropLocation)) {
//        return false;
//      }
//
//      int destRow = ((JTable.DropLocation) dropLocation).getRow();
//      try {
//        Object transferData = support.getTransferable().getTransferData(FLAVOR);
//        if (!(transferData instanceof Integer)) return false;
//
//        int srcRow = (Integer) transferData;
//
//        if (srcRow != destRow && srcRow != destRow - 1) {
//          controller.reorderAddins(srcRow, destRow);
//          addinsModel.fireTableDataChanged();
//        }
//      }
//      catch (UnsupportedFlavorException e) {
//        log.log(Level.FINER, "Unsupported flavour for import", e);
//        return false;
//      }
//      catch (IOException e) {
//        log.log(Level.WARNING, "IOException on import", e);
//        return false;
//      }
//
//      return true;
//    }
  }

  private static final DataFlavor FLAVOR = new DataFlavor(MyTransferable.class, null);

  private class MyTransferable implements Transferable
  {
    private int row;

    MyTransferable(int row)
    {
      this.row = row;
    }

    public DataFlavor[] getTransferDataFlavors()
    {
      return new DataFlavor[]{FLAVOR};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor)
    {
      return flavor.equals(FLAVOR);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
    {
      if (flavor == FLAVOR) {
        return row;
      }
      else {
        throw new UnsupportedFlavorException(flavor);
      }
    }
  }
}
