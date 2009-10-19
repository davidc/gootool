package com.goofans.gootool.view;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinDependency;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.wog.WorldOfGoo;

import javax.swing.*;
import java.util.List;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AddinPropertiesDialog extends JDialog
{
  private JTextPane description;
  private JButton okButton;
  private JLabel name;
  private JLabel version;
  private JLabel author;
  private JLabel id;
  private JLabel depends;
  private JPanel rootPanel;
  private JPanel thumbnailPanel;
  private JLabel thumbnail;

  public AddinPropertiesDialog(JFrame mainFrame, Addin a)
  {
    super(mainFrame, "About " + a.getTypeText() + " " + a.getName(), true);

    setDefaultCloseOperation(DISPOSE_ON_CLOSE);

    setResizable(false);

    // TODO 1.6
//    setIconImage(GooTool.getMainIconImage());

    setContentPane(rootPanel);

    GUIUtil.setDefaultClosingOkButton(okButton, this);
    GUIUtil.setCloseOnEscape(this);

    name.setText(a.getName());
    version.setText(a.getVersion().toString());
    author.setText(a.getAuthor());
    id.setText(a.getId());

    if (a.getDescription().startsWith("<html>")) {
      description.setText(a.getDescription());
    }
    else {
      // add <html> to make sure it wraps, replace newlines with <br>, remove any HTML that may be in there already.
      description.setText("<html>" + a.getDescription().replaceAll("<", "&lt;").replaceAll("\n", "<br>") + "</html>");
    }
    description.setCaretPosition(0);

    if (a.getThumbnail() == null) {
      rootPanel.remove(thumbnailPanel);
    }
    else {
      thumbnail.setIcon(new ImageIcon(a.getThumbnail()));
    }

    if (a.getDependencies().isEmpty()) {
      depends.setText("Nothing");
    }
    else {
      List<Addin> addins = WorldOfGoo.getAvailableAddins();

      StringBuilder sb = new StringBuilder("<html>");
      for (AddinDependency dependency : a.getDependencies()) {
        boolean isSatisfied = dependency.isSatisfiedBy(addins);

        if (!isSatisfied) sb.append("<font color=\"red\">");

        sb.append(dependency.getRef());

        boolean opened = false;
        if (dependency.getMinVersion() != null) {
          sb.append(" (");
          sb.append("Min version ").append(dependency.getMinVersion());
          opened = true;
        }

        if (dependency.getMaxVersion() != null) {
          if (opened) sb.append(", ");
          else sb.append("(");
          sb.append("Max version ").append(dependency.getMinVersion());
        }
        if (opened) sb.append(")");

        if (!isSatisfied) sb.append("</font>");

        sb.append("<br>");
      }
      sb.append("</html>");

      depends.setText(sb.toString());
    }

    pack();

    okButton.requestFocusInWindow();

    setLocationRelativeTo(mainFrame);
  }
}
