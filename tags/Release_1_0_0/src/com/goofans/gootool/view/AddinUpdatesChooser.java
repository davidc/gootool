package com.goofans.gootool.view;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.TextProvider;
import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinFactory;
import com.goofans.gootool.siteapi.APIException;
import com.goofans.gootool.siteapi.AddinUpdatesCheckRequest;
import com.goofans.gootool.util.DebugUtil;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.wog.WorldOfGoo;

public class AddinUpdatesChooser extends JDialog
{
  private static final Logger log = Logger.getLogger(AddinUpdatesChooser.class.getName());

  private JPanel contentPane;
  private JButton installUpdatesButton;
  private JButton cancelButton;
  private JTable updatesTable;
  private final List<UpdateRow> updateRows;

  private static final String[] COLUMN_NAMES;
  private static final Class[] COLUMN_CLASSES = new Class[]{Boolean.class, String.class, String.class, String.class, String.class};
  private static final TextProvider textProvider = GooTool.getTextProvider();

  static {
    COLUMN_NAMES = new String[]{
            textProvider.getText("addinUpdates.column.install"),
            textProvider.getText("addinUpdates.column.name"),
            textProvider.getText("addinUpdates.column.yourVersion"),
            textProvider.getText("addinUpdates.column.latestVersion"),
            textProvider.getText("addinUpdates.column.releaseDate")
    };
  }

  public AddinUpdatesChooser(JFrame owner, Map<String, AddinUpdatesCheckRequest.AvailableUpdate> updates)
  {
    super(owner, textProvider.getText("addinUpdates.title"), true);

    setContentPane(contentPane);
    getRootPane().setDefaultButton(installUpdatesButton);

    GUIUtil.setCloseOnEscape(this);

    installUpdatesButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        doInstallUpdates();
      }
    });

    cancelButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        setVisible(false);
      }
    });

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    // Prepare data model
    updateRows = new ArrayList<UpdateRow>(updates.size());

    for (Addin addin : WorldOfGoo.getAvailableAddins()) {
      AddinUpdatesCheckRequest.AvailableUpdate update = updates.get(addin.getId());
      if (update != null) {
        if (update.version.compareTo(addin.getVersion()) > 0) {
          UpdateRow row = new UpdateRow();
          row.addin = addin;
          row.update = update;
          row.install = true;
          updateRows.add(row);
        }
      }
    }
    installUpdatesButton.setEnabled(!updateRows.isEmpty());

    // Prepare table model
    AddinUpdatesTableModel updatesModel;

    updatesModel = new AddinUpdatesTableModel();

    updatesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    updatesTable.setModel(updatesModel);
    updatesTable.getTableHeader().setReorderingAllowed(false);

    // Prepare column widths
    TableColumnModel columnModel = updatesTable.getColumnModel();

    columnModel.getColumn(0).setPreferredWidth(25);
    columnModel.getColumn(1).setPreferredWidth(150);
    columnModel.getColumn(2).setPreferredWidth(50);
    columnModel.getColumn(3).setPreferredWidth(50);
    columnModel.getColumn(4).setPreferredWidth(75);

    pack();
    setLocationRelativeTo(owner);
  }

  private static class UpdateRow
  {
    Addin addin;
    AddinUpdatesCheckRequest.AvailableUpdate update;
    boolean install;
  }

  private void doInstallUpdates()
  {
    final int[] numSuccess = new int[]{0};

    try {
      GUIUtil.runTask(this, textProvider.getText("addinUpdating.title"), new ProgressIndicatingTask()
      {
        @Override
        public void run() throws Exception
        {
          WorldOfGoo wog = WorldOfGoo.getTheInstance();
          for (UpdateRow updateRow : updateRows) {
            if (updateRow.install) {
              log.log(Level.INFO, "Downloading update " + updateRow.addin.getId() + " version " + updateRow.update.version);

              beginStep(textProvider.getText("addinUpdating.status.downloading", updateRow.addin.getId()), false);

              File tempFile = Utilities.downloadFileToTemp(new URL(updateRow.update.downloadUrl));

              try {
                beginStep(textProvider.getText("addinUpdating.status.installing", updateRow.addin.getId()), false);

                // Load the addin once to get its ID and ensure it is valid.
                Addin addin = AddinFactory.loadAddin(tempFile);

                wog.uninstallAddin(updateRow.addin, true);
                wog.installAddin(tempFile, addin.getId(), true);
              }
              finally {
                Utilities.deleteFileIfExists(tempFile);
              }

              numSuccess[0]++;
            }

          }
          beginStep(textProvider.getText("addinUpdating.status.reloading"), false);
          wog.updateInstalledAddins();
        }
      });
    }
    catch (Exception e) {
      log.log(Level.SEVERE, "Error downloading updates", e);
      JOptionPane.showMessageDialog(this, textProvider.getText("addinUpdating.failed.message", e.getLocalizedMessage()),
              textProvider.getText("addinUpdating.failed.title"), JOptionPane.ERROR_MESSAGE);
    }

    JOptionPane.showMessageDialog(this, textProvider.getText("addinUpdating.completed.message", numSuccess[0], numSuccess[0] == 1 ? "" : "s"),
            textProvider.getText("addinUpdating.completed.title"), JOptionPane.INFORMATION_MESSAGE);

    //TODO force WOG to rescan addins if we installed updates, once the "skip recheck" option is implemented
    dispose();
  }


  private class AddinUpdatesTableModel extends AbstractTableModel
  {
    public int getRowCount()
    {
      return updateRows.size();
    }

    public int getColumnCount()
    {
      return COLUMN_NAMES.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
      UpdateRow updateRow = updateRows.get(rowIndex);

      if (columnIndex == 0) return updateRow.install;
      if (columnIndex == 1) return updateRow.addin.getName();
      if (columnIndex == 2) return updateRow.addin.getVersion();
      if (columnIndex == 3) return updateRow.update.version;
      if (columnIndex == 4) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return df.format(updateRow.update.releaseDate);
      }

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
      return columnIndex == 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
      if (columnIndex == 0) {
        UpdateRow updateRow = updateRows.get(rowIndex);

        boolean isSomethingEnabled = false;

        if ((Boolean) aValue) {
          updateRow.install = true;
          isSomethingEnabled = true;
        }
        else {
          updateRow.install = false;
        }

        //enable/disable Install button
        if (!isSomethingEnabled) {
          for (UpdateRow row : updateRows) {
            if (row.install) {
              isSomethingEnabled = true;
              break;
            }
          }
        }
        installUpdatesButton.setEnabled(isSomethingEnabled);
      }
    }
  }

  public static void main(String[] args) throws APIException
  {
    DebugUtil.setAllLogging();
    GooTool.initExecutors();

    WorldOfGoo wog = WorldOfGoo.getTheInstance();
    wog.init();

    AddinUpdatesCheckRequest checkRequest = new AddinUpdatesCheckRequest();
    Map<String, AddinUpdatesCheckRequest.AvailableUpdate> updates = checkRequest.checkUpdates();

    AddinUpdatesChooser dialog = new AddinUpdatesChooser(null, updates);
    dialog.setVisible(true);
    System.exit(0);
  }
}
