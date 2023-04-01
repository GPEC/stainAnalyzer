/*
 * stitch whole section image in bliss format
 */
package ca.ubc.gpec.ia.stitch.bliss;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Panel;
import java.awt.TextArea;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFileChooser;

import ca.ubc.gpec.ia.stitch.writer.BMPWriter;
import ca.ubc.gpec.ia.stitch.writer.PPMWriter;
import ca.ubc.gpec.ia.stitch.writer.PixelWriter;
import ca.ubc.gpec.ia.stitch.bliss.util.TileLookup;
import ca.ubc.gpec.ia.stitch.bliss.util.DaLookup;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.plugin.*;

/**
 *
 * @author samuelc
 */
public class StitchWhole {

    private String outputFileNameSuffix;
    private String finalScanFileName;
    private int fileCacheSize;
    private int writeCacheSize;
    private String[] directoryNames;
    private String outputFileFormatName;
    private int numRow;
    private int numColumn;
    private final String FILE_CHOOSER_TITLE = "Please select directory(ies) to stitch";
    private final String USER_SETTINGS_TITLE = "Please select stitch options - keep default if in doubt";
    private final String CONFIRM_USER_SETTINGS_TITLE = "Confirm user settings";
    private final String BRIEFING_MESSAGE = "Please note, currently only output in BMP/PPM format.";
    private final String DEFAULT_OUTPUT_FILE_NAME_SUFFIX = "_stitched";
    private final int DEFAULT_FILE_CACHE_SIZE = 40;
    private final int DEFAULT_WRITE_CACHE_SIZE = 512;
    private final String DEFAULT_FINAL_SCAN_FILE_NAME = "FinalScan.ini";
    private final String[] QUADRANT_SUFFIXES = {"1stQuadrant", "2ndQuadrant",
        "3rdQuadrant", "4thQuadrant"};
    private final String[] ROW_COLUMN_CHOICES = {"1", "2", "3", "4", "5", "6",
        "7", "8", "9", "10"};
    private final String DEFAULT_ROW_COLUMN_CHOICE = "4";
    private final String[] OUTPUT_FILE_FORMAT = {"BMP", "PPM"};
    private final int OUTPUT_FILE_FORMAT_INDEX_BMP = 0;
    private final int OUTPUT_FILE_FORMAT_INDEX_PPM = 1;
    private final String DEFAULT_OUTPUT_FILE_FORMAT = OUTPUT_FILE_FORMAT[OUTPUT_FILE_FORMAT_INDEX_BMP];
    private final String FILE_EXTENSION_BMP = ".bmp";
    private final String FILE_EXTENSION_PPM = ".ppm";

    /**
     * constructor - initialize private fields
     */
    public StitchWhole() {
        outputFileNameSuffix = DEFAULT_OUTPUT_FILE_NAME_SUFFIX;
        finalScanFileName = DEFAULT_FINAL_SCAN_FILE_NAME;
        fileCacheSize = DEFAULT_FILE_CACHE_SIZE;
        writeCacheSize = DEFAULT_WRITE_CACHE_SIZE;
        directoryNames = new String[0];
        numRow = 1; // default value
        numColumn = 1; // default value
    }

    /**
     * get index of file format choice
     *
     * @return
     */
    private int getOutputFileFormatChoiceIndex() {
        if (outputFileFormatName.equals(OUTPUT_FILE_FORMAT[OUTPUT_FILE_FORMAT_INDEX_BMP])) {
            return OUTPUT_FILE_FORMAT_INDEX_BMP;
        } else {
            return OUTPUT_FILE_FORMAT_INDEX_PPM;
        }
    }

    private String getOutputFileExtension() {
        if (outputFileFormatName.equals(OUTPUT_FILE_FORMAT[OUTPUT_FILE_FORMAT_INDEX_BMP])) {
            return FILE_EXTENSION_BMP;
        } else {
            return FILE_EXTENSION_PPM;
        }
    }

    /**
     * get an instance of a suitable writer
     *
     * @param filenameNoExtension
     * @param width
     * @param height
     * @return
     */
    private PixelWriter getWriter(String filenameNoExtension, int width,
            int height) throws IOException {
        PixelWriter pw = null;
        switch (getOutputFileFormatChoiceIndex()) {
            case OUTPUT_FILE_FORMAT_INDEX_BMP:
                pw = new BMPWriter(filenameNoExtension + FILE_EXTENSION_BMP, width,
                        height, PixelWriter.BMP_NUM_BIT_PER_PIXEL_COLOR,
                        writeCacheSize);
                break;
            case OUTPUT_FILE_FORMAT_INDEX_PPM:
                pw = new PPMWriter(filenameNoExtension + FILE_EXTENSION_PPM, width,
                        height, PixelWriter.PPM_NUM_BIT_PER_PIXEL_COLOR,
                        writeCacheSize);
                break;
            default:
                pw = null;
                break;
        }
        return pw;
    }

    /**
     * get directory names from user
     */
    private void askDirectoryNames() {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true); // enable multiple select
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Only allow
        // user to
        // choose
        // directories
        fc.setDialogTitle(FILE_CHOOSER_TITLE);
        int returnVal = fc.showOpenDialog(IJ.getInstance());

        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File[] files = fc.getSelectedFiles();
        int numOfDirectories = files.length;
        directoryNames = new String[numOfDirectories];
        for (int i = 0; i < numOfDirectories; i++) {
            directoryNames[i] = files[i].getAbsolutePath();
            // System.out.println(directoryNames[i]);
        }
    }

    /**
     * get settings and directories names from user
     */
    private void askSettings() {
        // show user options
        GenericDialog gd = new GenericDialog(USER_SETTINGS_TITLE);
        gd.addStringField("Output file name suffix",
                DEFAULT_OUTPUT_FILE_NAME_SUFFIX);
        gd.addStringField("Final Scan file name", DEFAULT_FINAL_SCAN_FILE_NAME);
        gd.addChoice("Output file format", OUTPUT_FILE_FORMAT,
                DEFAULT_OUTPUT_FILE_FORMAT);
        gd.addNumericField(
                "File cache (larger the faster, but requires more memory)",
                DEFAULT_FILE_CACHE_SIZE, 0);
        gd.addNumericField(
                "Write cache (need to experiment to find optimal size)",
                DEFAULT_WRITE_CACHE_SIZE, 0);
        gd.addChoice("Split files into number of rows", ROW_COLUMN_CHOICES,
                DEFAULT_ROW_COLUMN_CHOICE);
        gd.addChoice("Split files into number of columns", ROW_COLUMN_CHOICES,
                DEFAULT_ROW_COLUMN_CHOICE);

        gd.showDialog();

        if (gd.wasCanceled()) {
            return;
        } // user pressed cancel button. just use default values

        outputFileNameSuffix = gd.getNextString();
        finalScanFileName = gd.getNextString();
        fileCacheSize = (int) gd.getNextNumber();
        writeCacheSize = (int) gd.getNextNumber();
        Vector choices = gd.getChoices();
        outputFileFormatName = ((Choice) choices.get(0)).getSelectedItem();
        numRow = Integer.parseInt(((Choice) choices.get(1)).getSelectedItem());
        numColumn = Integer.parseInt(((Choice) choices.get(2)).getSelectedItem());
    }

    private int changeY(int y) {
        switch (getOutputFileFormatChoiceIndex()) {
            case OUTPUT_FILE_FORMAT_INDEX_BMP:
                return y - 1;
            default:
                return y + 1;
        }
    }

    private int startY(int lowerY, int upperY) {
        switch (getOutputFileFormatChoiceIndex()) {
            case OUTPUT_FILE_FORMAT_INDEX_BMP:
                return upperY;
            default:
                return lowerY;
        }
    }

    private int endY(int lowerY, int upperY) {
        switch (getOutputFileFormatChoiceIndex()) {
            case OUTPUT_FILE_FORMAT_INDEX_BMP:
                return lowerY;
            default:
                return upperY;
        }
    }

    private double getProgress(int y, int startY, int endY) {
        int range = Math.abs(endY - startY);
        switch (getOutputFileFormatChoiceIndex()) {
            case OUTPUT_FILE_FORMAT_INDEX_BMP:
                return 1 - ((double) y) / ((double) range);
            default:
                return ((double) y) / ((double) range);
        }
    }

    /**
     * Stitch one directory
     *
     * @param directoryName
     * @param message - message to show in status bar
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void stitchDirectory(String directoryName, String message)
            throws FileNotFoundException, IOException {
        // figure out tile files location in stitched file
        TileLookup tileLookup = new DaLookup(directoryName + File.separator
                + finalScanFileName, false, fileCacheSize);

        // construct writer to write file
        PixelWriter wPixel = getWriter(
                directoryName + outputFileNameSuffix,
                tileLookup.getFileWidth(),
                tileLookup.getFileHeight());

        // write the file
        int totalNumOfRows = tileLookup.getFileHeight();
        int startY = startY(0, totalNumOfRows);
        int endY = endY(0, totalNumOfRows);
        int y = startY;
        while (y != endY) {
            //System.out.print(y+" ");
            double progress = getProgress(y, startY, endY);
            IJ.showStatus(message + "   (" + Math.round(progress * 100) + "%)");
            IJ.showProgress(progress);
            for (int x = 0; x < tileLookup.getFileWidth(); x++) {
                int[] rgb = tileLookup.getRGB(x, y);
                // System.out.println(rgb[0]+" "+rgb[1]+" "+rgb[2]);
                wPixel.writePixel(rgb[0], rgb[1], rgb[2]);
            }
            y = changeY(y);
        }
        wPixel.close();
    }

    /**
     * stitch directory in various number of tiles The tile number will be r1c1
     * r1c2 r1c3 ... r2c1 r2c2 r2c3 ... r3c1 r3c2 r3c3 ...
     *
     * All tile will be same size.
     *
     * assume rowNum and colNum > 0
     *
     * @param directoryName
     * @param rowNum
     * @param colNum
     * @param message
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void stitchDirectoryTiles(String directoryName, int rowNum,
            int columnNum, String message) throws FileNotFoundException,
            IOException {
        // figure out tile files location in stitched file
        TileLookup tileLookup = new DaLookup(directoryName + File.separator
                + finalScanFileName, false, fileCacheSize);

        int wholeWidth = tileLookup.getFileWidth();
        int wholeHeight = tileLookup.getFileHeight();

        // tile coordinates is row,column
        int[][] startRow = new int[rowNum][columnNum];
        int[][] endRow = new int[rowNum][columnNum];
        int[][] startColumn = new int[rowNum][columnNum];
        int[][] endColumn = new int[rowNum][columnNum];

        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < columnNum; j++) {
                startRow[i][j] = Math.round(((float) wholeHeight)
                        * ((1 + i) / (float) rowNum)) - 1;
                endRow[i][j] = Math.round(((float) wholeHeight)
                        * (i / (float) rowNum));
                startColumn[i][j] = Math.round(((float) wholeWidth)
                        * (j / (float) columnNum));
                endColumn[i][j] = Math.round(((float) wholeWidth)
                        * ((j + 1) / (float) columnNum)) - 1;

                // make sure we have even number of columns ... don't know why
                // ... but it works.
                if (((endColumn[i][j] - startColumn[i][j]) % 2) == 0) {
                    startColumn[i][j] = startColumn[i][j] - 1;
                }
            }
        }

        // create directory first ...
        String outputDirName = directoryName + outputFileNameSuffix;
        File outputDir = new File(outputDirName);
        if (!outputDir.mkdir()) {
            throw new IOException("failed to create directory: "
                    + outputDirName);
        }
        for (int i = 0; i < rowNum; i++) {
            for (int j = 0; j < columnNum; j++) {

                System.out.println("start/end row=" + startRow[i][j] + "/"
                        + endRow[i][j] + " start/end column="
                        + startColumn[i][j] + "/" + endColumn[i][j]);

                PixelWriter wPixel = getWriter(
                        outputDirName + File.separator + "r" + (i + 1) + "c" + (j + 1),
                        endColumn[i][j] - startColumn[i][j] + 1,
                        startRow[i][j] - endRow[i][j] + 1);

                // write to file
                int startY = startY(endRow[i][j], startRow[i][j]);
                int endY = endY(endRow[i][j], startRow[i][j]);
                int y = startY;
                while (y != endY) {
                    // System.out.print(y+" ");
                    double progress = getProgress(y, startY, endY);
                    IJ.showStatus(message + "   (r" + (i + 1) + "c" + (j + 1)
                            + " - " + Math.round(progress * 100) + "%)");
                    IJ.showProgress(progress);
                    for (int x = startColumn[i][j]; x <= endColumn[i][j]; x++) {
                        int[] rgb = tileLookup.getRGB(x, y);
                        // System.out.println(rgb[0]+" "+rgb[1]+" "+rgb[2]);
                        wPixel.writePixel(rgb[0], rgb[1], rgb[2]);
                    }
                    y = changeY(y);
                }
                wPixel.close();
            }
        }

    }

    /**
     * the thread ImageJ uses
     */
    public void run(String arg) {
        // ass user to specify directories
        askDirectoryNames();
        if (directoryNames.length == 0) {
            // user have not selected any files
            IJ.showMessage("Stitch canceled",
                    "No directory selected - stitch canceled.");
            return;
        }

        // ask user to specify some settings
        askSettings();

        // show message before being stitching ...
        GenericDialog gd = new GenericDialog(CONFIRM_USER_SETTINGS_TITLE);
        gd.addMessage(BRIEFING_MESSAGE);
        gd.addMessage("Output files:");
        TextArea ta = new TextArea();
        ta.setEditable(false);
        for (int i = 0; i < directoryNames.length; i++) {
            if (numRow > 1 || numColumn > 1) {
                String filenames = "";
                for (int j = 0; j < numRow; j++) {
                    for (int k = 0; k < numColumn; k++) {
                        filenames = filenames + directoryNames[i]
                                + outputFileNameSuffix + File.separator + "r"
                                + (j + 1) + "c" + (k + 1) + getOutputFileExtension() + "\n";
                    }
                }
                ta.append(filenames + "\n");
            } else {
                ta.append(directoryNames[i] + outputFileNameSuffix + getOutputFileExtension()
                        + "\n");
            }
        }
        Panel p = new Panel();
        p.add(ta);
        gd.addPanel(p);
        gd.addMessage("Final scan file = " + finalScanFileName);
        gd.addMessage("File cache size = " + fileCacheSize);
        gd.addMessage("Write cache size = " + writeCacheSize);
        if (numRow > 1 || numColumn > 1) {
            gd.addMessage("Split stitched file into tiles.");
        } else {
            gd.addMessage("Generate single stitched file.");
        }

        gd.setOKLabel("Continue to stitch ...");
        gd.showDialog();

        if (gd.wasCanceled()) {
            IJ.showMessage("Stitch canceled", "Stitch canceled.");
            return;
        } // user pressed cancel button. don't do anything.

        // iterate through the selected directories and stitch them
        int errorsEncountered = 0;
        for (int i = 0; i < directoryNames.length; i++) {
            try {
                String message = "processing "
                        + new File(directoryNames[i]).getName() + "...";
                IJ.showStatus(message);
                if (numRow > 1 || numColumn > 1) {
                    stitchDirectoryTiles(directoryNames[i], numRow, numColumn,
                            message);
                } else {
                    stitchDirectory(directoryNames[i], message);
                }
            } catch (FileNotFoundException fnfe) {
                IJ.showMessage("Exception encounted while processing "
                        + directoryNames[i] + ": " + fnfe);
                errorsEncountered++;
            } catch (IOException ioe) {
                IJ.showMessage("Exception encounted while processing "
                        + directoryNames[i] + ": " + ioe);
                errorsEncountered++;
            }
        }
        if (errorsEncountered == 0) {
            String message = "Stitch completed.";
            IJ.showStatus(message);
            IJ.showMessage(message);
        } else {
            if (errorsEncountered > 1) {
                String message = "Stitch completed with " + errorsEncountered
                        + " errors.";
                IJ.showStatus(message);
                IJ.showMessage(message);
            } else {
                String message = "Stitch completed with 1 error.";
                IJ.showStatus(message);
                IJ.showMessage(message);
            }
        }

    }

    /**
     * main method for process testing
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            // TileLookup tileLookup = new
            // DaLookup("C:\\Documents and Settings\\sleung\\workspace-java\\NOT_VERSION_CONTROLLED\\temp\\VS09-4099A1_CD99-VGH\\FinalScan.ini",120);
            // TileLookup tileLookup = new
            // DaLookup("C:\\Documents and Settings\\sleung\\workspace-java\\NOT_VERSION_CONTROLLED\\temp\\S08-10325-F_Ki67-LGH\\FinalScan.ini",120);
            // cache size of 40 works with default memory setting (256M)
            // TileLookup tileLookup = new
            // DaLookup("C:\\Documents and Settings\\sleung\\workspace-java\\NOT_VERSION_CONTROLLED\\temp\\AB269129-2R_F480-Caltag\\FinalScan.ini",40);
            // TileLookup tileLookup = new
            // DaLookup("C:\\Documents and Settings\\sleung\\workspace-java\\NOT_VERSION_CONTROLLED\\temp\\10-001_PR-Site114_-1_v-_b-\\FinalScan.ini",true,40);
            int testWidth = 500;
            int testHeight = 500;
            // TileLookup tileLookup = new
            // DaLookup("C:\\Documents and Settings\\sleung\\Desktop\\cheng-han 2011-01-14\\HW023_HE_2org\\FinalScan.ini",true,40);

            TileLookup tileLookup = new DaLookup(
                    "/media/image_data/bliss_stitch/Xenografts/HW023_Ki67-Dako_1org/FinalScan.ini",
                    true, 40);

            // PPMWriter needs 8
            // BMPWriter needs 24

            PPMWriter wBMP = new PPMWriter(
                    "/media/image_data/bliss_stitch/Xenografts/HW023_Ki67-Dako_1orgTEST.ppm",
                    // tileLookup.getFileWidth(), tileLookup.getFileHeight(),
                    // 24,
                    testWidth, testHeight, 8, 512);

            // NOTE: (0,0) is top left corner

            // for (int y = tileLookup.getFileHeight() - 1; y >= 0; y--) {
            for (int y = 10100; y < testHeight + 10100; y++) {
                System.out.print(y + " ");
                if (y % 100 == 0) {
                    System.out.println(y + " ");
                }
                // for (int x = 0; x < tileLookup.getFileWidth(); x++) {
                for (int x = 13900; x < testWidth + 13900; x++) {
                    int[] rgb = tileLookup.getRGB(x, y);
                    // System.out.print("length rgp = "+rgb.length+" "+rgb[0]+" ");
                    //Color c = new Color(rgb[0],rgb[1],rgb[2]);
                    //System.out.println("(x=" + x + ",y=" + y + ") " + rgb[0]
                    //		+ " " + rgb[1] + " " + rgb[2]+" "+c.getRGB()+" "+Integer.reverseBytes(c.getRGB()));
                    if (rgb.length < 3) {
                        System.out.println("CRASHING!!! x=" + x + ", y=" + y);
                    }
                    wBMP.writePixel(rgb[0], rgb[1], rgb[2]);
                }
            }
            System.out.println("finished");
            wBMP.close();

        } catch (FileNotFoundException fnfe) {
            System.err.println(fnfe);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
}
