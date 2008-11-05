package net.infotrek.gootool.util;

import javax.swing.filechooser.FileFilter;
import java.util.Locale;
import java.io.File;

/**
 * @author David Croft (david.croft@infotrek.net)
 * @version $Id$
 */
public class WogExeFileFilter extends FileFilter
{

  public WogExeFileFilter()
  {
  }

  /**
   * Tests the specified file, returning true if the file is
   * accepted, false otherwise. True is returned if the extension
   * matches one of the file name extensions of this {@code
   * FileFilter}, or the file is a directory.
   *
   * @param f the {@code File} to test
   * @return true if the file is to be accepted, false otherwise
   */
  public boolean accept(File f)
  {
    if (f != null) {
      if (f.isDirectory()) {
        return true;
      }
      String fileName = f.getName();
      if (fileName.equalsIgnoreCase("WorldOfGoo.exe")) {
        return true;
      }
    }
    return false;
  }

  /**
   * The description of this filter. For example: "JPG and GIF Images."
   *
   * @return the description of this filter
   */
  public String getDescription()
  {
    return "WorldOfGoo.exe";
  }


  /**
   * Returns a string representation of the {@code FileNameExtensionFilter}.
   * This method is intended to be used for debugging purposes,
   * and the content and format of the returned string may vary
   * between implementations.
   *
   * @return a string representation of this {@code FileNameExtensionFilter}
   */
  public String toString()
  {
    return super.toString() + "[description=" + getDescription() + "]";
  }
}
