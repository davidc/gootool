/*
 * Copyright (c) 2008, 2009, 2010 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.view;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinDependency;
import com.goofans.gootool.addins.AddinsStore;
import com.goofans.gootool.util.GUIUtil;

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
      List<Addin> addins = AddinsStore.getAvailableAddins();

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
          sb.append("Max version ").append(dependency.getMaxVersion());
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
