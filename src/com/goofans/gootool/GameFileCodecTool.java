/*
 * Copyright (c) 2008, 2009, 2010, 2011, 2012 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.goofans.gootool.io.GameFormat;
import com.goofans.gootool.movie.BinImageAnimation;
import com.goofans.gootool.movie.BinMovie;
import com.goofans.gootool.util.FileNameExtensionFilter;
import com.goofans.gootool.util.GUIUtil;
import com.goofans.gootool.util.ProgressIndicatingTask;
import com.goofans.gootool.util.Utilities;

/**
 * Handles the GUI interaction of encoding/decoding and launches the actual codec in the background.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class GameFileCodecTool
{
  public enum CodecType
  {
    AES_DECODE(false),
    AES_ENCODE(true),
    XOR_DECODE(false),
    XOR_ENCODE(true),
    PNGBINLTL_DECODE(false),
    PNGBINLTL_ENCODE(true),
    ANIM_DECODE(false),
    //   ANIM_ENCODE(true),
    MOVIE_DECODE(false);
    //MOVIE_ENCODE(true);

    private final boolean encode;

    CodecType(boolean encode)
    {
      this.encode = encode;
    }

    public boolean isEncode()
    {
      return encode;
    }
  }

  private final String inputExtension;
  private final String inputDescription;
  private final String outputExtension;

  private final CodecType codecType;

  private File currentInputDir;
  private File currentOutputDir;

  public GameFileCodecTool(String inputExtension, String inputDescription, String outputExtension, CodecType codecType)
  {
    this.inputExtension = inputExtension;
    this.inputDescription = inputDescription;
    this.outputExtension = outputExtension;
    this.codecType = codecType;
  }

  public void runTool(JFrame parent) throws Exception
  {
    JFileChooser inputChooser = new JFileChooser(currentInputDir);
    inputChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    inputChooser.setMultiSelectionEnabled(true);

    if (inputExtension.length() > 0) {
      inputChooser.setFileFilter(new FileNameExtensionFilter(inputDescription, inputExtension));
    }

    if (codecType.isEncode()) {
      inputChooser.setDialogTitle("Select the file(s) or directory to encode");
    }
    else {
      inputChooser.setDialogTitle("Select the file(s) or directory to decode");
    }

    if (inputChooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
      return;
    }

    final File[] inputFiles = inputChooser.getSelectedFiles();

    for (File inputFile : inputFiles) {
      if (!inputFile.exists()) {
        GUIUtil.showErrorDialog(parent, "File not found", "File " + inputFile + " not found");
        return;
      }
    }

    boolean singleFile = inputFiles.length == 1 && inputFiles[0].isFile();

    currentInputDir = inputChooser.getCurrentDirectory();

    JFileChooser outputChooser = new JFileChooser(currentOutputDir);

    if (singleFile) {
      if (outputExtension.length() > 0) {
        outputChooser.setFileFilter(new FileNameExtensionFilter("Output file", outputExtension));
      }
      String inputName = inputFiles[0].getName();
      outputChooser.setDialogTitle("Select output for converted " + inputName);
      outputChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      outputChooser.setMultiSelectionEnabled(false);

      String outputName = generateOutputName(inputName);
      outputChooser.setSelectedFile(new File(currentOutputDir, outputName));
    }
    else {
      outputChooser.setDialogTitle("Select output directory for converted files");
      outputChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      outputChooser.setMultiSelectionEnabled(false);

      if (inputFiles.length == 1) {
        outputChooser.setSelectedFile(new File(currentOutputDir, inputFiles[0].getName()));
      }
    }

    File outputFileSelection = null;

    while (outputFileSelection == null) {
      if (outputChooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
        return;
      }

      outputFileSelection = outputChooser.getSelectedFile();
      if (outputFileSelection.exists()) {
        int retval = JOptionPane.showConfirmDialog(parent, outputFileSelection.getName() + " already exists, would you like to overwrite it?", "Confirm overwrite", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (retval == JOptionPane.CANCEL_OPTION) {
          return;
        }
        else if (retval == JOptionPane.NO_OPTION) {
          outputFileSelection = null;
        }
      }
    }

    currentOutputDir = outputChooser.getCurrentDirectory();
    final File outputFile = outputFileSelection;

    GUIUtil.runTask(parent, "Running conversion", new ProgressIndicatingTask()
    {
      @Override
      public void run() throws Exception
      {
        beginStep("Converting...", false);

        if (inputFiles.length == 1) {
          File inputFile = inputFiles[0];
          if (inputFile.isFile()) {
            doConversion(inputFile, outputFile);
          }
          else if (inputFile.isDirectory()) {
            doDirectory(inputFile, outputFile);
          }
        }
        else {
          Utilities.mkdirsOrException(outputFile);

          for (File inputFile : inputFiles) {
            if (inputFile.isFile()) {
              doConversion(inputFile, new File(outputFile, generateOutputName(inputFile.getName())));
            }
            else if (inputFile.isDirectory()) {
              doDirectory(inputFile, new File(outputFile, inputFile.getName()));
            }
          }
        }
      }
    });
  }

  private void doDirectory(File inputDir, File outputDir) throws IOException
  {
    for (File file : inputDir.listFiles()) {
      if (file.isDirectory()) {
        doDirectory(file, new File(outputDir, file.getName()));
      }
      else if (file.getName().endsWith("." + inputExtension)) {
        Utilities.mkdirsOrException(outputDir);
        doConversion(file, new File(outputDir, generateOutputName(file.getName())));
      }
    }
  }

  private void doConversion(File inputFile, File outputFile) throws IOException
  {
    byte[] bytes;

    switch (codecType) {
      case AES_DECODE:
        bytes = GameFormat.AES_BIN_CODEC.decodeFile(inputFile);
        Utilities.writeFile(outputFile, bytes);
        break;
      case AES_ENCODE:
        bytes = Utilities.readFile(inputFile);
        GameFormat.AES_BIN_CODEC.encodeFile(outputFile, bytes);
        break;
      case XOR_DECODE:
        bytes = GameFormat.MAC_BIN_CODEC.decodeFile(inputFile);
        Utilities.writeFile(outputFile, bytes);
        break;
      case XOR_ENCODE:
        bytes = Utilities.readFile(inputFile);
        GameFormat.MAC_BIN_CODEC.encodeFile(outputFile, bytes);
        break;
      case PNGBINLTL_DECODE:
        RenderedImage decImage;
        FileInputStream is = new FileInputStream(inputFile);
        try {
          decImage = GameFormat.MAC_IMAGE_CODEC.readImage(is);
        }
        finally {
          is.close();
        }
        ImageIO.write(decImage, GameFormat.PNG_FORMAT, outputFile);
        break;
      case PNGBINLTL_ENCODE:
        BufferedImage encImage = ImageIO.read(inputFile);
        FileOutputStream os = new FileOutputStream(outputFile);
        try {
          GameFormat.MAC_IMAGE_CODEC.writeImage(encImage, os);
        }
        finally {
          os.close();
        }
        break;
      case ANIM_DECODE:
        BinImageAnimation anim = new BinImageAnimation(inputFile);
        Utilities.writeFile(outputFile, anim.toXMLDocument().getBytes());
        break;
      case MOVIE_DECODE:
        BinMovie movie = new BinMovie(Utilities.readFile(inputFile));
        Utilities.writeFile(outputFile, movie.toXMLDocument().getBytes());
        break;
    }
  }

  private String generateOutputName(String inputName)
  {
    String outputName;
    if (inputName.endsWith("." + inputExtension)) {
      outputName = inputName.substring(0, inputName.length() - (inputExtension.length() + 1));
    }
    else {
      outputName = inputName;
    }
    if (outputExtension.length() > 0) {
      outputName += "." + outputExtension;
    }
    return outputName;
  }
}
