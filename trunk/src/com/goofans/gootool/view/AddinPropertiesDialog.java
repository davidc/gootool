package com.goofans.gootool.view;

import com.goofans.gootool.addins.Addin;
import com.goofans.gootool.addins.AddinDependency;
import com.goofans.gootool.GooTool;
import com.goofans.gootool.wog.WorldOfGoo;

import javax.swing.*;
import java.awt.event.*;
import java.util.List;

/**
 * @author David Croft (david.croft@infotrek.net)
 * @version $Id$
 */
public class AddinPropertiesDialog extends JDialog
{
  private JLabel description;
  private JButton okButton;
  private JLabel name;
  private JLabel version;
  private JLabel author;
  private JLabel id;
  private JLabel depends;
  private JPanel rootPanel;

  public AddinPropertiesDialog(JFrame mainFrame, Addin a)
  {
    super(mainFrame, "About " + a.getTypeText() + " " + a.getName(), false);

    setLocationByPlatform(true);

    setIconImage(GooTool.getTheInstance().getMainIcon());

    setContentPane(rootPanel);


    okButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        setVisible(false);
      }
    });

    addWindowListener(new WindowAdapter()
    {
      public void windowOpened(WindowEvent e)
      {
//        pack();
        okButton.requestFocusInWindow();
      }
    });

    //TODO this doesn't work.
    addKeyListener(new KeyAdapter()
    {
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          setVisible(false);
        }
      }
    });

    name.setText(a.getName());
    version.setText(a.getVersion().toString());
    author.setText(a.getAuthor());
    id.setText(a.getId());

    if (a.getDescription().startsWith("<html>")) {
      description.setText(a.getDescription());
    }
    else {
      // add <html> to make sure it wraps, replace newlines with <br/>, remove any HTML that may be in there already.
      description.setText("<html>" + a.getDescription().replaceAll("<", "&lt;").replaceAll("\n", "<br/>") + "</html>");
    }

    if (a.getDependencies().size() == 0) {
      depends.setText("None");
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

        sb.append("<br/>");
      }
      sb.append("</html>");

      depends.setText(sb.toString());
    }

    pack();
    setResizable(false);
  }
}
