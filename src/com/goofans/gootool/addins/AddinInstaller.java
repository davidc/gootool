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

  private static final String[] PASSES = new String[]{GOOMOD_DIR_OVERRIDE, GOOMOD_DIR_MERGE, GOOMOD_DIR_COMPILE};
  private static final int PASS_OVERRIDE = 0;
  private static final int PASS_MERGE = 1;
  private static final int PASS_COMPILE = 2;

  private AddinInstaller()
  {
  }

  public static void installAddin(Addin addin) throws IOException, AddinFormatException
  {
    log.log(Level.FINE, "Installing addin " + addin.getId());

    if (addin.getDiskFile().isFile()) {
      ZipFile zipFile = new ZipFile(addin.getDiskFile());

      try {
        for (int pass = 0; pass < PASSES.length; ++pass) {
          String passPrefix = PASSES[pass];
          log.log(Level.FINER, "Pass " + pass + " (looking in " + passPrefix + ")");

          Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

          while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();

            if (!zipEntry.isDirectory() && zipEntry.getName().startsWith(passPrefix)) {
              String fileName = zipEntry.getName().substring(passPrefix.length());
              InputStream is = zipFile.getInputStream(zipEntry);

              try {
                doPass(pass, fileName, is);
              }
              finally {
                is.close();
              }
            }
          }
        }
      }
      finally {
        zipFile.close();
      }
    }
    else {
      File rootDir;
      rootDir = addin.getDiskFile();

      for (int pass = 0; pass < PASSES.length; ++pass) {
        String passPrefix = PASSES[pass];
        log.log(Level.FINER, "Pass " + pass + " (looking in " + passPrefix + ")");

        File passSrcDir = new File(rootDir, passPrefix);
        if (passSrcDir.isDirectory()) {
          doPassInDir(pass, passSrcDir, "");
        }
      }
    }
    log.log(Level.FINE, "Addin " + addin.getId() + " installed");
  }

  private static void doPassInDir(int pass, File passSrcDir, String pathName) throws IOException, AddinFormatException
  {
    for (File file : passSrcDir.listFiles()) {
      if (file.isDirectory() && !file.getName().equals(".svn")) {
        doPassInDir(pass, file, pathName + file.getName() + "/");
      }
      else if (file.isFile()) {
        InputStream is = new FileInputStream(file);
        try {
          doPass(pass, pathName + file.getName(), is);
        }
        finally {
          is.close();
        }
      }
    }
  }

  private static void doPass(int pass, String fileName, InputStream is) throws IOException, AddinFormatException
  {
//    System.out.println("Doing pass " + pass + " on file " + fileName);
    if (pass == PASS_OVERRIDE) {
      processOverride(fileName, is);
    }
    else if (pass == PASS_MERGE) {
      processMerge(fileName, is);
    }
    else if (pass == PASS_COMPILE) {
      processCompile(fileName, is);
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

  private static void processOverride(String fileName, InputStream is) throws IOException, AddinFormatException
  {
    log.log(Level.FINER, "Override " + fileName);
    checkDirOk(fileName);

    File destFile = new File(WorldOfGoo.getCustomDir(), fileName);

    destFile.getParentFile().mkdirs(); // ensure the directory exists

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

  private static void processMerge(String fileName, InputStream is) throws IOException, AddinFormatException
  {
    log.log(Level.FINER, "Merge " + fileName);
    checkDirOk(fileName);

    if (!fileName.endsWith(".xsl")) throw new AddinFormatException("Addin has a non-XSLT file in the merge directory: " + fileName);

    File mergeFile = new File(WorldOfGoo.getCustomDir(), fileName.substring(0, fileName.length() - 4) + ".bin");

    if (!mergeFile.exists()) throw new AddinFormatException("Addin tries to merge a nonexistent file: " + fileName);


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

  private static void processCompile(String fileName, InputStream is) throws IOException, AddinFormatException
  {
    log.log(Level.FINER, "Compile " + fileName);
    checkDirOk(fileName);

    if (!fileName.endsWith(".xml")) throw new AddinFormatException("Addin has a non-XML file in the compile directory" + fileName);

    File destFile = new File(WorldOfGoo.getCustomDir(), fileName.substring(0, fileName.length() - 4) + ".bin");

    destFile.getParentFile().mkdirs(); // ensure the directory exists

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
