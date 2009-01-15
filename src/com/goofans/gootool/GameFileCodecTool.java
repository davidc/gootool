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

/**
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
    PNGBINLTL_ENCODE(true);

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
//    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); TODO
    if (inputExtension.length() > 0) {
      inputChooser.setFileFilter(new FileNameExtensionFilter(inputDescription, inputExtension));
    }

    if (codecType.isEncode()) {
      inputChooser.setDialogTitle("Select the file to encode");
    }
    else {
      inputChooser.setDialogTitle("Select the file to decode");
    }

    if (inputChooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
      return;
    }

    File inputFile = inputChooser.getSelectedFile();
    if (!inputFile.exists()) {
      JOptionPane.showMessageDialog(parent, "File " + inputFile + " not found", "File not found", JOptionPane.ERROR_MESSAGE);
      return;
    }

    currentInputDir = inputChooser.getCurrentDirectory();

    // TODO if >1 file, choose a directory not a file

    JFileChooser outputChooser = new JFileChooser(currentOutputDir);
    if (outputExtension.length() > 0) {
      outputChooser.setFileFilter(new FileNameExtensionFilter("Output file", outputExtension));
    }
    String inputName = inputFile.getName();
    outputChooser.setDialogTitle("Select output for converted " + inputName);

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
    outputChooser.setSelectedFile(new File(currentOutputDir, outputName));

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

    doConversion(inputFile, outputFile);
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
    }
  }
}
