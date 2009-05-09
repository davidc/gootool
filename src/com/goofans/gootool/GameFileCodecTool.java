package com.goofans.gootool;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.*;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;

import com.goofans.gootool.util.FileNameExtensionFilter;
import com.goofans.gootool.util.Utilities;
import com.goofans.gootool.io.AESBinFormat;
import com.goofans.gootool.io.MacBinFormat;
import com.goofans.gootool.io.MacGraphicFormat;
import com.goofans.gootool.movie.BinImageAnimation;
import com.goofans.gootool.movie.BinMovie;

/**
 * TODO put this in a background thread
 * TODO errors in this must be given back to the user
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

    private boolean encode;

    CodecType(boolean encode)
    {
      this.encode = encode;
    }

    public boolean isEncode()
    {
      return encode;
    }
  }

  private String inputExtension;
  private String inputDescription;
  private String outputExtension;

  private File currentInputDir;
  private File currentOutputDir;

  private CodecType codecType;

  public GameFileCodecTool(String inputExtension, String inputDescription, String outputExtension, CodecType codecType)
  {
    this.inputExtension = inputExtension;
    this.inputDescription = inputDescription;
    this.outputExtension = outputExtension;
    this.codecType = codecType;
  }

  public void runTool(Component parent) throws IOException
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


    File[] inputFiles = inputChooser.getSelectedFiles();

    for (File inputFile : inputFiles) {
      if (!inputFile.exists()) {
        JOptionPane.showMessageDialog(parent, "File " + inputFile + " not found", "File not found", JOptionPane.ERROR_MESSAGE);
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

    File outputFile = null;

    while (outputFile == null) {
      if (outputChooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
        return;
      }

      outputFile = outputChooser.getSelectedFile();
      if (outputFile.exists()) {
        int retval = JOptionPane.showConfirmDialog(parent, outputFile.getName() + " already exist, would you like to overwrite it?", "Confirm overwrite", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (retval == JOptionPane.CANCEL_OPTION) {
          return;
        }
        else if (retval == JOptionPane.NO_OPTION) {
          outputFile = null;
        }
      }
    }

    currentOutputDir = outputChooser.getCurrentDirectory();

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
        bytes = AESBinFormat.decodeFile(inputFile);
        Utilities.writeFile(outputFile, bytes);
        break;
      case AES_ENCODE:
        bytes = Utilities.readFile(inputFile);
        AESBinFormat.encodeFile(outputFile, bytes);
        break;
      case XOR_DECODE:
        bytes = MacBinFormat.decodeFile(inputFile);
        Utilities.writeFile(outputFile, bytes);
        break;
      case XOR_ENCODE:
        bytes = Utilities.readFile(inputFile);
        MacBinFormat.encodeFile(outputFile, bytes);
        break;
      case PNGBINLTL_DECODE:
        RenderedImage decImage = MacGraphicFormat.decodeImage(inputFile);
        ImageIO.write(decImage, "PNG", outputFile);
        break;
      case PNGBINLTL_ENCODE:
        BufferedImage encImage = ImageIO.read(inputFile);
        MacGraphicFormat.encodeImage(outputFile, encImage);
        break;
      case ANIM_DECODE:
        BinImageAnimation anim = new BinImageAnimation(inputFile);
        Utilities.writeFile(outputFile, anim.toXMLDocument().getBytes());
        break;
      case MOVIE_DECODE:
        BinMovie movie = new BinMovie(inputFile);
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
