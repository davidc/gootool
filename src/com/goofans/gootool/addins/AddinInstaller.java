package com.goofans.gootool.addins;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.goofans.gootool.io.BinFormat;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.wog.WorldOfGoo;

/**
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class AddinInstaller
{
  private static final Logger log = Logger.getLogger(AddinInstaller.class.getName());

  private static final String[] ALLOWED_ROOT_DIRS = new String[]{"properties/", "res/"};
  private static final String GOOMOD_DIR_OVERRIDE = "override/";
  private static final String GOOMOD_DIR_MERGE = "merge/";
  private static final String GOOMOD_DIR_COMPILE = "compile/";

  private AddinInstaller()
  {
  }

  public static void installAddin(Addin addin) throws IOException, AddinFormatException
  {
    log.log(Level.FINE, "Installing addin " + addin.getId());

    ZipFile zipFile = new ZipFile(addin.getDiskFile());

    try {
      for (int pass = 0; pass < 3; ++pass) {
        log.log(Level.FINER, "Pass " + pass);

        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

        while (zipEntries.hasMoreElements()) {
          ZipEntry zipEntry = zipEntries.nextElement();

          if (!zipEntry.isDirectory()) {
            if (pass == 0 && zipEntry.getName().startsWith(GOOMOD_DIR_OVERRIDE)) {
              processOverride(zipFile, zipEntry);
            }

            if (pass == 1 && zipEntry.getName().startsWith(GOOMOD_DIR_MERGE)) {
              processMerge(zipFile, zipEntry);
            }

            if (pass == 2 && zipEntry.getName().startsWith(GOOMOD_DIR_COMPILE)) {
              processCompile(zipFile, zipEntry);
            }
          }
        }
      }
      log.log(Level.FINE, "Addin " + addin.getId() + " installed");
    }
    finally {
      zipFile.close();
    }
  }

  private static void checkDirOk(String fileName) throws AddinFormatException
  {
    boolean isOk = false;
    for (String allowedRootDir : ALLOWED_ROOT_DIRS) {
      if (fileName.startsWith(allowedRootDir)) {
        isOk = true;
        break;
      }
    }

    if (!isOk) {
      throw new AddinFormatException("Addin tries to install a file in a prohibited directory: " + fileName);
    }
  }

  private static void processOverride(ZipFile zipFile, ZipEntry zipEntry) throws IOException, AddinFormatException
  {
    String fileName = zipEntry.getName().substring(GOOMOD_DIR_OVERRIDE.length());
    log.log(Level.FINER, "Override " + fileName);
    checkDirOk(fileName);

    File destFile = new File(WorldOfGoo.getCustomDir(), fileName);

    destFile.getParentFile().mkdirs(); // ensure the directory exists

    InputStream is = zipFile.getInputStream(zipEntry);

    try {
      OutputStream os = new FileOutputStream(destFile);
      try {
        Utilities.copyStreams(is, os);
      }
      finally {
        os.close();
      }
    }
    finally {
      is.close();
    }
  }

  private static void processMerge(ZipFile zipFile, ZipEntry zipEntry) throws IOException, AddinFormatException
  {
    String fileName = zipEntry.getName().substring(GOOMOD_DIR_MERGE.length());
    log.log(Level.FINER, "Merge " + fileName);
    checkDirOk(fileName);

    if (!fileName.endsWith(".xsl")) throw new AddinFormatException("Addin has a non-XSLT file in the merge directory: " + fileName);

    File mergeFile = new File(WorldOfGoo.getCustomDir(), fileName.substring(0, fileName.length() - 4) + ".bin");

    if (!mergeFile.exists()) throw new AddinFormatException("Addin tries to merge a nonexistent file: " + fileName);

    InputStream is = zipFile.getInputStream(zipEntry);

    try {
      Merger merger = new Merger(mergeFile, new InputStreamReader(is));
      merger.merge();
      merger.writeEncoded(mergeFile);
    }
    catch (TransformerException e) {
      throw new AddinFormatException("Error transforming " + fileName, e);
    }
    finally {
      is.close();
    }
  }

  private static void processCompile(ZipFile zipFile, ZipEntry zipEntry) throws IOException, AddinFormatException
  {
    String fileName = zipEntry.getName().substring(GOOMOD_DIR_COMPILE.length());
    log.log(Level.FINER, "Compile " + fileName);
    checkDirOk(fileName);

    if (!fileName.endsWith(".xml")) throw new AddinFormatException("Addin has a non-XML file in the compile directory" + fileName);

    File destFile = new File(WorldOfGoo.getCustomDir(), fileName.substring(0, fileName.length() - 4) + ".bin");

    destFile.getParentFile().mkdirs(); // ensure the directory exists

    InputStream is = zipFile.getInputStream(zipEntry);

    String xml;
    try {
      xml = Utilities.readStreamInfoString(is);
    }
    finally {
      is.close();
    }
    BinFormat.encodeFile(destFile, xml);
  }

}
