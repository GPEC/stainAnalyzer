/**
 * The project was started in February 2005 at the Genetic Pathology Evaluation
 * Centre, University of British Columbia
 *
 * @author Andy Chan <akwchan@interchange.ubc.ca>
 * @author Dmitry Turbin <dmitry.turbin@vch.ca>
 * @author Samuel Leung <Samuel.Leung@vch.ca>
 * @author Dustin Thomson <dustint@sfu.ca>
 *
 * Special thanks to Wayne Rasband - for the wonderful ImageJ program Gabriel
 * Landini - for his plugin Colour Deconvolutor Zafir Anjum - for table methods
 * programming
 */
package ca.ubc.gpec.ia.analyzer.controller;

import ca.ubc.gpec.ia.analyzer.util.MeanBackgroundLevels;
import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettings;
import java.net.URL;
import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
//import ij.measure.ResultsTable;
//import ij.plugin.filter.ParticleAnalyzer;
import ij.measure.Calibration;
import ij.plugin.filter.EDM;

// GPEC custom modified ImageJ plugins
import ca.ubc.gpec.ia.analyzer.deconvolution.ColourDeconvolution;
import ca.ubc.gpec.ia.analyzer.deconvolution.EntropyThreshold;
import ca.ubc.gpec.ia.analyzer.segmentation.ThresholdAdjuster;
import ca.ubc.gpec.ia.analyzer.processing.ParticleAnalyzer;
import ca.ubc.gpec.ia.analyzer.measure.ResultsTable;
import ca.ubc.gpec.ia.analyzer.model.IAO;
import ca.ubc.gpec.ia.analyzer.model.ImageDescriptor;
import ca.ubc.gpec.ia.analyzer.segmentation.ImageCalculator;
import ca.ubc.gpec.ia.analyzer.segmentation.Thresholder;
import ca.ubc.gpec.ia.analyzer.processing.HaralickTexture;
import ca.ubc.gpec.ia.analyzer.transformation.ColourDeconvolutionImageTransformation;
import ca.ubc.gpec.ia.analyzer.transformation.ImageTransformationException;
import ca.ubc.gpec.ia.analyzer.transformation.ThresholdImageTransformation;
import ca.ubc.gpec.ia.analyzer.gui.ImageChooserDialog;
import ca.ubc.gpec.ia.stitch.bliss.SettingsException;
import ca.ubc.gpec.ia.stitch.bliss.StitchException;
import ca.ubc.gpec.ia.stitch.bliss.StitchTma;



import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;

import java.util.regex.*;
import java.awt.Color;
import java.util.Hashtable;
import java.awt.Checkbox;
import java.awt.Frame;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 *
 * @author samuelc
 */
public class NuclearStainAnalyzer extends StainAnalyzer {

    //
    //
    public static final boolean DEBUG = true;
    //
    //
    public static final String APPLICATION_NAME = "Nuclear Stain Analyser";
    public static final boolean SHOW_DEBUG_IMAGES = false; // for debug purposes. please set to false for production!!!!
    private StainAnalyzerSettings settings; // analysis settings
    private Hashtable<String, Double> meanBackgroundLevels;
    private SettingsEditor settingsEditor;
//    static int BATCH_MODE = 1;
//    static int SINGLE_MODE = 2;
    // Threshold variables
    static final int AUTO_THRESHOLD = 1;   // Auto Threshold
    static final int MAX_ENTROPY_THRESHOLD = 2;  // Maximum entropy threshold
    static final int K_MEANS_CLUSTERING_THRESHOLD = 3; // K means clustering threshold
    static final int MANUAL_THRESHOLD = 4; // Manually adjust threshold
    static final int MANUAL_RECORD_THRESHOLD = 5; // Manually adjust threshold with results saved    
    static final int AUTO_SET_THRESHOLD = 6; // Auto threshold with "Set"
    static final int SET_ENTIRE_THRESHOLD = 7; // Threshold with "Set" to cover entire spectrum of 1 - 255
    //Flag to indicate if we will be selecting a portion
    //of the image or not (Used for more specific analysis)
    private boolean performSelection = false;
    // Files & paths variables        
    private String resultsDir = "";
    private String resultsParent = "";
    private String selectionDir = "";
    private String resultsSuffix = "_results";
    private String summaryResultsFile = "Summary.txt";
    private String amalgamateSummaryFile = "";
    private boolean amalgamate = false; // Whether we are in the process of amalgamating the results file
    private boolean logProgress = false; // Whether to log progress or not
    private Hashtable images; // A hashtable that keeps all the images
    private String currImgName;
    private String currImgFullName;
    private String currColour;
    private boolean saveImages = true; // Whether to save the result outline images
    private long startTime;
    private long endTime;
    //Handle to the background level database
    MeanBackgroundLevels backgroundLevelDb;

    /**
     * default constructor
     */
    public NuclearStainAnalyzer() {
        // Initialise StainAnalyzerSettings and  hash tables
        settingsEditor = new SettingsEditor();
        settings = settingsEditor.getSettings();
        meanBackgroundLevels = new Hashtable<String, Double>();
        images = new Hashtable();
    }

    /**
     * analyze IAO
     *
     * @param iao
     */
    public IAO analyzeIao(IAO iao) throws ImageTransformationException, MalformedURLException, URISyntaxException {
        System.out.println("analyzing "+iao.getImageDescriptors().first().getUrl());
        // 1. set image
        this.addImage(iao.getImageDescriptors().first().getUrl(), iao.getImageDescriptors().first().getImagePlus());
        this.currImgName = iao.getImageDescriptors().first().getUrl();
        
        // 2. set result directory TODO: currently set to same directory as input file
        this.resultsDir = iao.getImageDescriptors().first().getParentDirectory().getAbsolutePath();
        System.out.println("result dir set to: "+this.resultsDir);
        
        // 3. call private method to analze image
        this.analyzeCurrImg(false);
        
        // TODO: need to change this so to follow the IAO model
        return iao; // return iao UNCHANGED!!!!!
    }

    public void run(String arg) {
        // First display info / splash screen
        URL iconURL = this.getClass().getResource("images/Splash_Screen.jpg");

        ImageIcon icon = null;
        if (iconURL != null) {
            icon = new ImageIcon(iconURL);
        }
        //ImageIcon icon = new ImageIcon(iconPath + "Splash_Screen.jpg");
        JOptionPane jp = new JOptionPane();
        jp.setLocation(2, 2);
        if (iconURL != null) {
            JOptionPane.showMessageDialog(null, "", "Subcellular Stain Analyser", JOptionPane.INFORMATION_MESSAGE, icon);
        } else {
            JOptionPane.showMessageDialog(null, "", "Subcellular Stain Analyser", JOptionPane.INFORMATION_MESSAGE);
        }
        // Then ask what the user wants to do
        GenericDialog gd = new GenericDialog(APPLICATION_NAME);
        String choices[] = new String[6];
        // contants for indexes ...
        int ANALYSIS_TYPE_BATCH = 1;
        int ANALYSIS_TYPE_BATCH_BLISS = 2;
        int ANALYSIS_TYPE_BATCH_APERIO = 3;
        int ANALYSIS_TYPE_STITCH = 5;
        int ANALYSIS_TYPE_SETTINGS = 4;
        int ANALYSIS_TYPE_CURRENT = 0;
        choices[ANALYSIS_TYPE_BATCH] = "Batch analyse image(s) from disk using existing settings";
        choices[ANALYSIS_TYPE_BATCH_BLISS] = "Batch analyse BLISS tiled image(s) using existing settings";
        choices[ANALYSIS_TYPE_BATCH_APERIO] = "Batch analyse Aperio selection(s) with existing settings";
        choices[ANALYSIS_TYPE_STITCH] = "Stitch BLISS tiled image(s)";
        choices[ANALYSIS_TYPE_SETTINGS] = "Create new settings";
        choices[ANALYSIS_TYPE_CURRENT] = "Analyse current image using existing settings"; // Added by Dmitry 2011-07-30
        gd.addChoice("Please choose one of the following options:", choices, "Batch analyse image(s) from disk using existing settings");

        gd.addCheckbox("Select Regions For Analysis", false);

        gd.showDialog();
        if (gd.wasCanceled()) {	// Exit the plugin
            return;
        } else {
            int index = gd.getNextChoiceIndex();
            this.performSelection = ((Checkbox) gd.getCheckboxes().get(0)).getState();

            if (DEBUG) {
                System.out.println("choice: " + index);
            }

            // check to see if need to load settings
            if (index == ANALYSIS_TYPE_BATCH || index == ANALYSIS_TYPE_BATCH_BLISS || index == ANALYSIS_TYPE_BATCH_APERIO || index == ANALYSIS_TYPE_CURRENT) {
                settingsEditor.doSettings("load");
            }

            System.out.println("finished loading settings");

            // batch analyse images ... need to check the types of files ...
            if (index == ANALYSIS_TYPE_BATCH || index == ANALYSIS_TYPE_BATCH_APERIO) { // Batch analyse using existing calibration
                File[] fs = null;

                // Get folder that contains images to be analysed
                JFileChooser fc = null;
                //ImageChooserDialog fc = null;
                if (index == ANALYSIS_TYPE_BATCH) {
                    fc = new JFileChooser(OpenDialog.getDefaultDirectory());
                } else if (index == ANALYSIS_TYPE_BATCH_APERIO) {
                    fc = new ImageChooserDialog(OpenDialog.getDefaultDirectory());
                }

                fc.setMultiSelectionEnabled(true);	// enable multiple select
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);	// Allow user to choose files and directories
                fc.setDialogTitle("Please specify location of images to be analysed");

                int returnVal = fc.showOpenDialog(IJ.getInstance());

                if (returnVal != JFileChooser.APPROVE_OPTION) {
                    return; // exit plugin
                }

                // Sets the result location
                setResultsDir(fc.getCurrentDirectory().getAbsolutePath());

                fs = fc.getSelectedFiles();

                if (DEBUG) {
                    System.out.println("number of files chosen = " + fs.length);
                }
                //Do the selections of the images
                if (this.performSelection) {
                    List files = new ArrayList();
                    for (int i = 0; i < fs.length; i++) {
                        File f = fs[i];
                        if (f.isDirectory()) {
                            analysisPrePass(f.getAbsolutePath(), null);
                        } else if (f.isFile()) { // Store the ones that are files to be open later
                            files.add(f.getName());
                        }
                    }
                    if (!files.isEmpty()) {
                        String[] files_array = new String[files.size()];
                        for (int i = 0; i < files.size(); i++) {
                            files_array[i] = files.get(i).toString();
                        }
                        // TODO: need to change fc.getCurrentDirectory()...
                        analysisPrePass(fc.getCurrentDirectory().getAbsolutePath(), files_array);
                    }
                }

                //Analyse the images
                List<String> files = new ArrayList<String>();
                for (int i = 0; i < fs.length; i++) {
                    File f = fs[i];
                    if (f.isDirectory()) {
                        analyzeImages(f.getAbsolutePath(), null);
                    } else if (f.isFile()) { // Store the ones that are files to be open later
                        files.add(f.getName());
                    }
                }
                if (!files.isEmpty()) {
                    String[] files_array = new String[files.size()];
                    for (int i = 0; i < files.size(); i++) {
                        files_array[i] = files.get(i).toString();
                    }
                    analyzeImages(fs[0].getParent(), files_array);
                }

                IJ.showMessage("Batch analysis complete");

            } else if (index == ANALYSIS_TYPE_CURRENT) { // Analyse the opened current image; added by Dmitry 2011-08-06
                // Sets the result location
                SaveDialog od = new SaveDialog("Please specify the output file name", "", "");
                setResultsDir(od.getDirectory());
                this.resultsDir = this.resultsParent + System.getProperty("file.separator") + od.getFileName() + this.resultsSuffix;
                File f = new File(this.resultsDir);
                f.mkdir();
                this.currImgFullName = WindowManager.getCurrentImage().getTitle();
                StringTokenizer st = new StringTokenizer(this.currImgFullName);
                this.currImgName = st.nextToken(".");
                //return;
                analyzeCurrImg(false);
                IJ.showMessage("Analysis complete"); // end of Dmitry's addition

            } else if (index == ANALYSIS_TYPE_BATCH_BLISS || index == ANALYSIS_TYPE_STITCH) { // Batch analyse BLISS tiled images
                StitchTma sb = new StitchTma();
                try {
                    if (index != ANALYSIS_TYPE_STITCH) {
                        // choices[1] = "Batch analyse BLISS tiled images using existing settings";
                        sb.setFinishMsg(false);
                        sb.getSettings("Please select folder(s) to be stitched and analyzed");
                    } else {
                        // choices[2] = "Stitch BLISS tiled images";
                        sb.setFinishMsg(true);
                        sb.getSettings("Please select folder(s) to be stitched");
                    }
                } catch (SettingsException se) {
                    // exit now!!!
                    IJ.error(se.toString());
                    return;
                }

                String[] folders = sb.getStitchedFolders();
                if (folders == null) {
                    return;
                }

                if (index == ANALYSIS_TYPE_BATCH_BLISS) {
                    // Sets the result location
                    setResultsDir(sb.getStitchedParent());
                }
                // Do the stitching
                try {
                    sb.stitchFolders();
                } catch (StitchException se) {
                    // exist now !!!
                    IJ.error(se.toString());
                    return;
                }
                if (index == ANALYSIS_TYPE_BATCH_BLISS) {
                    if (this.performSelection) {
                        for (int i = 0; i < folders.length; i++) {
                            analysisPrePass(folders[i], null);
                        }
                    }
                    for (int i = 0; i < folders.length; i++) {
                        analyzeImages(folders[i], null);
                    }
                    IJ.showMessage("Batch analysis complete");
                }

            } else if (index == ANALYSIS_TYPE_SETTINGS) { // Save settings
                settingsEditor.doSettings("save");
            }
        }
    }

    /**
     * Figures out useful stuff like output directories and the like
     *
     * @param fs
     */
    private void analysisPrePass(String directory, String[] files) {
//    	 Get all the images within this folder
        boolean dirSelected = false; // whether the user was selecting a directory instead of file
        if (files == null) {
            dirSelected = true;
            File fd = new File(directory);
            files = fd.list();
        }

        // Process each image one by one and save analysis results to text file
        Pattern p = Pattern.compile("(.*)\\.(jpg|jpeg|tiff|tif|gif|bmp|png)$", Pattern.CASE_INSENSITIVE);

        for (int i = 0; i < files.length; i++) {
            Matcher m = p.matcher(files[i]);
            if (!m.matches()) {
                if (DEBUG) {
                    System.out.println("analysisPrePass: file skipped: " + files[i]);
                }
                continue;
                // Make sure only image files are chosen
            }

            String imgName = m.group(1);

            currImgName = imgName;
            currImgFullName = imgName + "." + "tif";

            if (DEBUG) {
                System.out.println("analysisPrePass: currImgName = " + imgName);
            }

            //Setup the output directory
            File d = new File(directory);
            if (dirSelected) {
                this.resultsDir = this.resultsParent + System.getProperty("file.separator") + d.getName() + this.resultsSuffix;
            } else {
                this.resultsDir = this.resultsParent + System.getProperty("file.separator") + imgName + this.resultsSuffix;
            }
            File f = new File(this.resultsDir);
            f.mkdir();

            //Set up the cropped output directory
            if (dirSelected) {
                this.selectionDir = this.resultsDir + System.getProperty("file.separator") + "selections";
            } else {
                this.selectionDir = this.resultsDir + System.getProperty("file.separator") + "selections";
            }

            File fo = new File(this.selectionDir);
            fo.mkdir();

            Opener o = new Opener();
            ImagePlus currImage = o.openImage(directory, files[i]);

            // Ask the user to select the part of the image they want
            // analysed, then saved that
            selectImageArea(currImage);
        }
    }

    /**
     * main method to analyze batch images
     *
     * @param directory
     * @param files
     */
    private void analyzeImages(String directory, String[] files) { // Analyse a batch of images
        boolean dirSelected = false; // whether the user was selecting a directory instead of file
        if (files == null) {
            dirSelected = true;
            File fd = new File(directory);
            files = fd.list();
        }

        // Process each image one by one and save analysis results to text file
        Pattern p = Pattern.compile("(.*)\\.(jpg|jpeg|tiff|tif|gif|bmp|png)$", Pattern.CASE_INSENSITIVE);

        //Initialise the background level database
        this.backgroundLevelDb = new MeanBackgroundLevels(directory + System.getProperty("file.separator") + "bglevel.raw");
        this.backgroundLevelDb.load();

        Boolean firstImage = true;
        for (int i = 0; i < files.length; i++) {
            Matcher m = p.matcher(files[i]);
            if (!m.matches()) {
                if (DEBUG) {
                    System.out.println("analyzeImage: file skipped: " + files[i]);
                }
                continue;
                // Make sure only image files are chosen
            }

            String imgName = m.group(1);
            String imgExtension = m.group(2);

            currImgName = imgName;
            currImgFullName = imgName + "." + imgExtension;

            if (DEBUG) {
                System.out.println("analyzeImages: currImgFullName = " + currImgFullName);
                System.out.println("analyzeImages: directory" + directory);
            }

            // Setup the output directory
            File d = new File(directory);

            if (dirSelected) {
                this.resultsDir = this.resultsParent + System.getProperty("file.separator") + d.getName() + this.resultsSuffix;
            } else {
                this.resultsDir = this.resultsParent + System.getProperty("file.separator") + currImgName + this.resultsSuffix;
            }

            File f = new File(this.resultsDir);
            f.mkdir();

            if (dirSelected) {
                this.selectionDir = this.resultsDir + System.getProperty("file.separator") + "selections";
            } else {
                this.selectionDir = this.resultsDir + System.getProperty("file.separator") + "selections";
            }

            /**
             * **************************************************************************
             * NOTICE	* EVEN THOUGH WE ARE GENERATING BACKGROUND INFORMATION
             * HERE, THIS ONLY * OCCURS IF WE ARE SELECTING PARTS OF THE IMAGE.
             * IF WE ARE USING SELECTIONS* THEN WE CAN'T BE SURE THAT ANY
             * BACKGROUND WILL BE INCLUDED IN THE IMAGE * HENCE WE MUST ANALYZE
             * THE BACKGROUND LEVELS AT THIS STEP AND CACHE THEM * * HOWEVER,
             * THIS ADDS A SLIGHT PERFORMANCE HIT AS WE ARE GENERATING THE *
             * COLOUR DECONVOLUTION TWICE FOR EACH IMAGE (ONCE AT THIS STEP AND
             * ONCE AT * THE ANALYSIS STEP * * ANY CHANGES MADE TO THIS CODE,
             * SHOULD BE ALSO MADE TO THE ANALYSIS CODE	* BELOW, IN THE
             * ANALYZEIMAGE FUNCTION	*
             * *************************************************************************
             */
            //Generate the background level information if it's a selection
            if (this.performSelection) {
                //Only generate the background levels if they don't already exist
                if (this.backgroundLevelDb.get(imgName, "Blue") == null || this.backgroundLevelDb.get(imgName, "Brown") == null) {
                    Opener o = new Opener();
                    ImagePlus currImage = o.openImage(directory, imgName + "." + imgExtension);

                    //Before performing the selection, we want to generate the information for the hashtable
                    showFeedback("Generating BG Levels For " + files[i] + "...", 0.1);
                    ColourDeconvolution cd = new ColourDeconvolution();
                    cd.setImage(currImage);
                    cd.run(settings.getSettingValue("stain").toString());

                    String[] colours = {"Blue", "Brown"};
                    for (int x = 0; x < colours.length; x++) {
                        //Generate it
                        backgroundLevelDb.put(imgName, colours[x], new Double(getMeanGrayLevel(cd.getImage("Colour [" + (x + 1) + "]"), Integer.parseInt(settings.getSettingValue("minBackgroundThreshold").toString()), 255)));
                    }
                }
            }


            // Analyse the images
            images = new Hashtable();
            Opener o = new Opener();
            if (this.performSelection) {
                addImage(currImgName, o.openImage(this.selectionDir, currImgName + ".tif"));
            } else {
                addImage(currImgName, o.openImage(directory, currImgFullName));
            }
            if (firstImage) { // If first image then need to clear the results file
                analyzeCurrImg(false);
                firstImage = false;
            } else {
                analyzeCurrImg(true);
            }
        }
        this.backgroundLevelDb.save();
    }

    /**
     * main method analyze image (pointed to by currImgName)
     *
     * @param append
     */
    private void analyzeCurrImg(boolean append) { // Analyse a
        // single image
        System.out.println("start to analyze "+currImgName+" ...");
        startTime = System.currentTimeMillis();
        showFeedback("Analyzing " + this.currImgName + "...", 0.0);

        /**
         * *********************************************************************
         * NOTICE * ALTHOUGH WE ARE GENERATING THE BACKGROUND LEVEL INFORMATION
         * HERE, WE MAY * BE USING PRE-CACHED DATA IF THE USER HAS SELECTED
         * SUBSETS OF THE IMAGE OR* IF THE DATABASE ALLREADY EXISTS ON DISK (TO
         * SAVE TIME) * * THE ONLY DANGER WITH THIS IS IF THE IMAGES HAVE
         * CHANGED ON THE DISK * WITHOUT THE DATABASE BEING UPDATED * * ANY
         * CHANGES TO THE CODE BETWEEN THIS POINT AND THE BACKGROUND LEVEL *
         * ANALYSIS SHOULD BE REVIEWED CAREFULLY, AND THE CHANGES ALSO APPLIED
         * TO * THE BACKGROUND ANALYSIS CODE ABOVE *
         * ********************************************************************
         */
        System.out.println("preparing to do color deconvolution ...");
        // Run Colour Deconvolution
        ColourDeconvolution cd = new ColourDeconvolution();
        cd.setStainAnalyzerSettings(settings);
        cd.setImage(getImage(currImgName));
        cd.run(settings.getSettingValue("stain").toString());
        addImage("Colour [1]", cd.getImage("Colour [1]"));
        addImage("Colour [2]", cd.getImage("Colour [2]"));
        addImage("Colour [3]", cd.getImage("Colour [3]"));
        removeImage("Colour [3]");
        showFeedback("Analysing " + this.currImgName + "...", 0.1);

        // Note: Blue  (= negative = Hematoxylin) = 'Colour [1]' window
        //       Brown (= positive = Antibody)    = 'Colour [2]' window

        String[] colour_windows = {"Colour [1]", "Colour [2]"};
        String[] colours = {"Blue", "Brown"};

        // Generate mask for each stain
        for (int i = 0; i < colour_windows.length; i++) {
            String window = "Colour [" + (i + 1) + "]";
            double correction;
            if (i == 0) {
                //correction = Double.parseDouble(settings.getSettingValue("blueCorrection").toString());
                correction = Double.parseDouble(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_BLUE_CORRECTION).toString());
            } else {
                //correction = Double.parseDouble(settings.getSettingValue("brownCorrection").toString());
                correction = Double.parseDouble(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_BROWN_CORRECTION).toString());
            }

            ImagePlus imp = getImage(window);

            /**
             * // Convert to 8-bit ImageConverter ic = new ImageConverter(imp);
             * // commented out by Dmitry 2011-07-31 ic.convertToGray8(); //
             * because these images are already 8-bit, with LUT applied
             */
            /// Get mean gray level
            if (backgroundLevelDb.loaded()) {
                //Load it from the database if it is there (to save time)
                Double returnValue = backgroundLevelDb.get(currImgName, colours[i]);
                if (returnValue != null) {
                    meanBackgroundLevels.put(colours[i], returnValue);
                } else {
                    //we have tried to look up a key that doesn't exist in the database. This is
                    //an indication that the database is stale and should be re-generated.

                    //Generate it
                    //Double bdLevel = new Double(getMeanGrayLevel(imp, Integer.parseInt(settings.getSettingValue("minBackgroundThreshold").toString()), 255));
                    Double bdLevel = new Double(getMeanGrayLevel(imp, Integer.parseInt(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_MIN_BACKGROUND_THRESHOLD).toString()), 255));
                    meanBackgroundLevels.put(colours[i], bdLevel);
                    //Synchronise with our database
                    backgroundLevelDb.put(currImgName, colours[i], bdLevel);
                }
            } else {
                if (this.performSelection) {
                    //If we are here, the database has not been loaded, but we are doing analysis on selected areas of images.
                    //This is bad news because this means that we will not be doing an accurate background analysis
                    //So alert the user, but proceed
                    IJ.showMessage("System cannot find background level data for current image. Will attempt to analyse, but analysis will use white as background colour.");
                }
                //Generate it
                //Double bdLevel = new Double(getMeanGrayLevel(imp, Integer.parseInt(settings.getSettingValue("minBackgroundThreshold").toString()), 255));
                Double bdLevel = new Double(getMeanGrayLevel(imp, Integer.parseInt(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_MIN_BACKGROUND_THRESHOLD).toString()), 255));
                meanBackgroundLevels.put(colours[i], bdLevel);
                //Synchronise with our database
                backgroundLevelDb.put(currImgName, colours[i], bdLevel);
            }

            // Run threshold with correction
            //threshold(imp, Integer.parseInt(settings.getSettingValue("thresholdOption").toString()), correction);    
            //threshold(imp, settings.getSettingValue("thresholdOption").toString(), correction);
            threshold(imp, settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_THRESHOLD_OPTION).toString(), correction);

            // Binary close
            ImageProcessor ip = imp.getProcessor();
            ip.dilate();
            imp.updateAndDraw();
            ip.erode();
            imp.updateAndDraw();

            // Run Watershed
            EDM edm = new EDM();
            edm.setup("watershed", imp);
            edm.run(ip);

            if (SHOW_DEBUG_IMAGES) {
                Enumeration e = images.keys();
                while (e.hasMoreElements()) {
                    String imageName = e.nextElement().toString();
                    new FileSaver(getImage(imageName)).saveAsTiff(this.resultsDir + System.getProperty("file.separator") + this.currImgName + "_" + imageName + "_BEFORE_filterParticles_" + i + ".tif");
                }
            }

            // Filter particles
            //filterParticles(imp); // 2012-03-16 - moved to after ImageCalculator subtract
        }

        if (SHOW_DEBUG_IMAGES) {
            Enumeration e = images.keys();
            while (e.hasMoreElements()) {
                String imageName = e.nextElement().toString();
                new FileSaver(getImage(imageName)).saveAsTiff(this.resultsDir + System.getProperty("file.separator") + this.currImgName + "_" + imageName + "_AFTER_filterParticles_" + ".tif");
            }
        }

        // Perform image calculation to get the mask for the blue stain minus the brown stuff
        ImageCalculator ical = new ImageCalculator();
        ical.setOperator("Subtract");
        ical.doOperation(getImage("Colour [2]"), getImage("Colour [1]"));

        addImage("Result of Colour", ical.getResultImage());
        ImagePlus imp = getImage("Result of Colour");
        imp.getProcessor().invert();
        imp.updateAndDraw();

        filterParticles(getImage("Colour [2]")); // filter particle for brown channel - moved from above 2012-03-16
        filterParticles(imp); // filter particle for blue channel - moved from above 2012-03-16

        removeImage("Colour [1]");

        // Duplicate original image for overlay
        ImagePlus originalImp = getImage(currImgName);
        ImagePlus overlayImp = NewImage.createRGBImage("Overlay", originalImp.getWidth(), originalImp.getHeight(), 1, NewImage.FILL_WHITE);
        ImageProcessor overlayIP = overlayImp.getProcessor();
        overlayIP.copyBits(originalImp.getProcessor(), 0, 0, Blitter.COPY);;
        addImage("Overlay", overlayImp);

        // For each colour stain...
        for (int i = 0; i < colours.length; i++) {
            String colour = colours[i];
            String mask;
            boolean appending;

            if (i == 0) { //Blue
                mask = "Result of Colour";
                appending = false;
            } else { //Brown
                mask = "Colour [2]";
                appending = true;
            }

            // Perform image calculation and apply the masks to the original image
            ical = new ImageCalculator();
            ical.setOperator("OR");
            ical.doOperation(getImage(currImgName), getImage(mask));

            //closeWindow(mask);
            removeImage(mask);

            String resultsWindow = "Result of " + currImgFullName;

            // Convert to 8-bit
            imp = ical.getResultImage();
            addImage(resultsWindow, imp);
            ImageConverter ic = new ImageConverter(imp);
            ic.convertToGray8();

            // Autothreshold with Set to preserve intensities / gray values
            //threshold(imp, SET_ENTIRE_THRESHOLD, 0); 
            threshold(imp, StainAnalyzerSettings.THRESHOLD_CHOICE_SET_ENTIRE_THRESHOLD, 0);

            // Analyse particles with results saved
            ResultsTable rt = new ResultsTable();
            analyzeParticlesWithResults(imp, false, this.resultsDir + System.getProperty("file.separator") + this.currImgName + "_Outlines.jpg", overlayImp, colour, rt);
            removeImage(resultsWindow);

            saveResultsTable(rt, currImgName + "_" + colour, this.resultsDir + System.getProperty("file.separator") + this.currImgName + "_Results.csv", appending);

        }

        // Save the individual particle results
        for (int i = 0; i < colours.length; i++) {
            String colour = colours[i];
            boolean appending;
            String resultsWindow = "Result of " + colour;

            if (i == 0) { //Blue
                appending = false;
            } else { //Brown
                appending = true;
            }


        }

        // Save the outlines file
        if (this.saveImages) {
            if (imageExist("Overlay")) {
// by Dmitry 2011-00-06                new FileSaver(getImage("Overlay")).saveAsJpeg(this.resultsDir + System.getProperty("file.separator") + this.currImgName + "_Outlines.jpg");
                new FileSaver(getImage("Overlay")).saveAsTiff(this.resultsDir + System.getProperty("file.separator") + this.currImgName + "_Outlines.tif");
            }
        }
        removeImage("Overlay");

        // Generate summary results
        generateSummary(this.currImgName + "_Results.csv", append);

        // All done - Close the original image
        removeImage(this.currImgName);

        images = null;
        showFeedback("Analysing " + this.currImgName + "...", 1.0);
    }

    private void closeWindow(String window) {
        IJ.selectWindow(window);
        IJ.run("Close");
    }

    private void runCmdOnWindow(String window, String cmd) { // Run an ImageJ command on a given window
        IJ.selectWindow(window);
        IJ.run(cmd);
    }

    private void analyzeParticlesWithResults(ImagePlus imp, boolean showResults, String outlinesFile, ImagePlus overlayImp, String colour, ResultsTable rt) {
        // Select the image       
        ImagePlus analyze_imp;
        if (showResults) { // If show results then create a copy of the image
            // Show the original image
            imp.show();

            // Create a copy of the image to be analysed
            analyze_imp = NewImage.createByteImage("Copy of the image", imp.getWidth(), imp.getHeight(), 1, NewImage.FILL_WHITE);
        } else { // Just analyse the original image
            analyze_imp = imp;
        }

        ImageProcessor analyze_ip = analyze_imp.getProcessor();

        if (showResults) {
            analyze_ip.copyBits(imp.getProcessor(), 0, 0, Blitter.COPY);
        }

        // Analyse particles
        int options = 0;
        if (showResults) {
            //options = ParticleAnalyzer.SHOW_OUTLINES + ParticleAnalyzer.SHOW_RESULTS;
            options = ParticleAnalyzer.SHOW_OVERLAY + ParticleAnalyzer.SHOW_RESULTS;
        } else if (outlinesFile.length() > 0) {
            //options = ParticleAnalyzer.SHOW_OUTLINES;
            options = ParticleAnalyzer.SHOW_OVERLAY;
        }

        // Set measurements
        int measurements = 0;
        measurements = measurements + ParticleAnalyzer.AREA;				// Area
        measurements = measurements + ParticleAnalyzer.STD_DEV;				// Standard Deviation
        measurements = measurements + ParticleAnalyzer.MIN_MAX;				// Min & Max Gray Value
        measurements = measurements + ParticleAnalyzer.CENTER_OF_MASS;		// Centre of Mass
        measurements = measurements + ParticleAnalyzer.RECT;				// Bounding Rectangle
        measurements = measurements + ParticleAnalyzer.CIRCULARITY;			// Circularity
        measurements = measurements + ParticleAnalyzer.MEAN;				// Mean Gray Value
        measurements = measurements + ParticleAnalyzer.MODE;				// Modal Gray Value
        measurements = measurements + ParticleAnalyzer.CENTROID;			// Centroid
        measurements = measurements + ParticleAnalyzer.PERIMETER;			// Perimeter
        measurements = measurements + ParticleAnalyzer.ELLIPSE;				// Fit Ellipse
        measurements = measurements + ParticleAnalyzer.FERET;				// Feret's Diameter
        measurements = measurements + ParticleAnalyzer.INTEGRATED_DENSITY;	// Integrated Density
        measurements = measurements + ParticleAnalyzer.MEDIAN;				// Median        
        measurements = measurements + ParticleAnalyzer.SKEWNESS;			// Skewness
        measurements = measurements + ParticleAnalyzer.KURTOSIS;			// Kurtosis
        measurements = measurements + ParticleAnalyzer.ROUNDNESS;			// Roundness    
        measurements = measurements + ParticleAnalyzer.TEXTURE;				// Texture

        ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, 1, 999999);

        // Set overlay image
        pa.setOverlayImp(overlayImp);

        // Set overlay colour
        if (colour.equals("Blue")) {
            Color c = new Color(
                    Integer.parseInt(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_R)),
                    Integer.parseInt(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_G)),
                    Integer.parseInt(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_B)));
            pa.setOverlayColour(c);
        } else if (colour.equals("Brown")) {
            Color c = new Color(
                    Integer.parseInt(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_R)),
                    Integer.parseInt(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_G)),
                    Integer.parseInt(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_B)));
            pa.setOverlayColour(c);
        }

        // Apply filters to remove particles
        Hashtable<String, String> filters = new Hashtable<String, String>();
        String[] f = {"Area", "Circularity", "EllipseRatio", "Feret", "Roundness"};
        for (int i = 0; i < f.length; i++) {
            String filter = f[i];
            if (settings.getSettingValue("min" + filter).toString().length() > 0) {
                filters.put("min" + filter, (String) settings.getSettingValue("min" + filter));
            }
            if (settings.getSettingValue("max" + filter).toString().length() > 0) {
                filters.put("max" + filter, (String) settings.getSettingValue("max" + filter));
            }
            if (settings.getSettingValue("textureObjectsMin" + filter).toString().length() > 0) {
                filters.put("textureObjectsMin" + filter, (String) settings.getSettingValue("textureObjectsMin" + filter));
            }
            if (settings.getSettingValue("textureObjectsMax" + filter).toString().length() > 0) {
                filters.put("textureObjectsMax" + filter, (String) settings.getSettingValue("textureObjectsMax" + filter));
            }
        }
        if (settings.getSettingValue("minTextureScore").toString().length() > 0) {
            filters.put("minTextureScore", (String) settings.getSettingValue("minTextureScore"));
        }
        if (settings.getSettingValue("maxTextureScore").toString().length() > 0) {
            filters.put("maxTextureScore", (String) settings.getSettingValue("maxTextureScore"));
        }

        pa.applyFilters(filters);

        HaralickTexture ht = new HaralickTexture();
        int degree = ht.getDegree(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_DEGREE).toString());
        int stepSize = Integer.parseInt(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_STEP_SIZE).toString());
        int grayLevels = Integer.parseInt(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_GRAY_LEVELS).toString());
        int precision = Integer.parseInt(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_PRECISION).toString());

        int computeFeatures = 0;
        String[] featureTitles = ht.getFeatureTitles();
        Hashtable<Object, Object> discriminantParameters = new Hashtable<Object, Object>();
        discriminantParameters.put("discriminantConstant", settings.getSettingValue("discriminantConstant").toString());
        for (int i = 0; i < featureTitles.length; i++) {
            String key = "calculate" + featureTitles[i];
            if (settings.containsSettingKey(key)) {
                boolean calculate = Boolean.parseBoolean(settings.getSettingValue(key).toString());
                if (calculate) {
                    computeFeatures += ht.getFeature(featureTitles[i]);
                }
            }
            key = "coefficient" + featureTitles[i];
            if (settings.containsSettingKey(key)) {
                double coefficient = settings.getSettingValue(key).toString().length() > 0 ? Double.parseDouble(settings.getSettingValue(key).toString()) : 0.00;
                discriminantParameters.put(new Integer(ht.getFeature(featureTitles[i])), new Double(coefficient));
            }
        }

        // Set mean background level
        pa.setMeanBackgroundGrayLevel(Double.parseDouble(meanBackgroundLevels.get(colour).toString()));
        pa.setTextureParameters(degree, stepSize, grayLevels, precision, computeFeatures, discriminantParameters);
        pa.analyze(analyze_imp, analyze_ip);

        if (showResults) {
            // Show copied image
            analyze_imp.show();
            analyze_imp.updateAndDraw();
        }
    }

    private void filterParticles(ImagePlus imp) {
        ImageProcessor ip = imp.getProcessor();

        // Analyse particles
        ResultsTable rt = new ResultsTable();
        int options = ParticleAnalyzer.SHOW_MASKS;

        // Set measurements
        int measurements = 0;
        measurements = measurements + ParticleAnalyzer.AREA;			// Area
        /**
         * measurements = measurements + ParticleAnalyzer.STD_DEV;	// Standard
         * Deviation measurements = measurements + ParticleAnalyzer.MIN_MAX;	//
         * Min & Max Gray Value measurements = measurements +
         * ParticleAnalyzer.CENTER_OF_MASS; // Centre of Mass measurements =
         * measurements + ParticleAnalyzer.RECT;	// Bounding Rectangle
         */
        measurements = measurements + ParticleAnalyzer.CIRCULARITY;		// Circularity
        /**
         * measurements = measurements + ParticleAnalyzer.MEAN;	// Mean Gray
         * Value measurements = measurements + ParticleAnalyzer.MODE;	// Modal
         * Gray Value measurements = measurements + ParticleAnalyzer.CENTROID;
         * // Centroid measurements = measurements + ParticleAnalyzer.PERIMETER;
         * // Perimeter
         */
        measurements = measurements + ParticleAnalyzer.ELLIPSE;			// Fit Ellipse
        measurements = measurements + ParticleAnalyzer.FERET;			// Feret's Diameter
        /**
         * measurements = measurements + ParticleAnalyzer.INTEGRATED_DENSITY;	//
         * Integrated Density measurements = measurements +
         * ParticleAnalyzer.MEDIAN;	// Median measurements = measurements +
         * ParticleAnalyzer.SKEWNESS;	// Skewness measurements = measurements +
         * ParticleAnalyzer.KURTOSIS;	// Kurtosis
         */
        measurements = measurements + ParticleAnalyzer.ROUNDNESS;		// Roundness    

        ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, 1, 999999);
        Hashtable<String, Double> filters = new Hashtable<String, Double>();
        String[] f = {"Area", "Circularity", "EllipseRatio", "Feret", "Roundness"};
        for (int i = 0; i < f.length; i++) {
            String filter = f[i];
            filters.put("min" + filter, new Double(Double.parseDouble(settings.getSettingValue("min" + filter).toString())));
            filters.put("max" + filter, new Double(Double.parseDouble(settings.getSettingValue("max" + filter).toString())));
        }
        pa.applyFilters(filters);

        pa.analyze(imp, ip);

        String title = imp.getTitle();
        String maskWindow = "Mask of " + title;
        addImage(maskWindow, pa.getImage(maskWindow));

        if (SHOW_DEBUG_IMAGES) {
            new FileSaver(imp).saveAsTiff(this.resultsDir + System.getProperty("file.separator") + this.currImgName + "_" + imp.getTitle() + "_BEFORE_LUT.tif");
            new FileSaver(pa.getImage(maskWindow)).saveAsTiff(this.resultsDir + System.getProperty("file.separator") + this.currImgName + "_" + "Mask of " + imp.getTitle() + "_BEFORE_LUT.tif");
        }

        if (imageExist(maskWindow)) {
            // Close original window             
            removeImage(title);

            // Invert the inverting Lut of mask and rename mask to original image
            imp = getImage(maskWindow);
            ip = imp.getProcessor();
            ip.invertLut();
            ip.invert();

            imp.setTitle(title);
            addImage(title, imp);
        } else { // Create a blank image
            int width = imp.getWidth();
            int height = imp.getHeight();

            // Close original window               
            removeImage(title);

            imp = NewImage.createByteImage(title, width, height, 1, NewImage.FILL_WHITE);
            addImage(title, imp);
        }
    }

    private void saveResultsTable(ResultsTable rt, String img_file, String output_file, boolean append) {
        try {
            FileWriter f = new FileWriter(output_file, append);
            String output = "";
            if (!append) {
                // First build the hashtable of column headings for better user output
                Hashtable ht = new Hashtable();
                ht.put("Mean", "Mean_Intensity");
                ht.put("StdDev", "StdDev_Intensity");
                ht.put("Mode", "Modal_Intensity");
                ht.put("Min", "Min_Intensity");
                ht.put("Max", "Max_Intensity");
                ht.put("X", "Centroid_X");
                ht.put("Y", "Centroid_Y");
                ht.put("XM", "Centre_of_Mass_X");
                ht.put("YM", "Centre_of_Mass_Y");
                ht.put("Perim.", "Perimeter");
                ht.put("BX", "Bound_Rect_X");
                ht.put("BY", "Bound_Rect_Y");
                ht.put("Width", "Bound_Rect_Width");
                ht.put("Height", "Bound_Rect_Height");
                ht.put("Major", "Ellipse_Major_Axis");
                ht.put("Minor", "Ellipse_Minor_Axis");
                ht.put("Angle", "Ellipse_Angle");
                ht.put("Circ.", "Circularity"); // 4pi(area/perimeter^2) - Value of 1 equals perfect circle
                ht.put("Feret", "Ferets_Diameter");
                ht.put("Skew", "Skewness");
                ht.put("Kurt", "Kurtosis");

                output = output + "Image\tParticle";
                int col = 0;
                String heading = rt.getColumnHeading(col);
                while (heading != null) { // keep fetching if still has columns
                    if (rt.columnExists(col)) {
                        String h;
                        if (ht.containsKey(heading)) {
                            h = ht.get(heading).toString();
                        } else {
                            h = heading;
                        }
                        output = output + "\t" + h;
                        heading = rt.getColumnHeading(++col);
                    } else { // done with the columns
                        break;
                    }
                }
                output = output + "\r\n";
            }
            for (int row = 0; row < rt.getCounter(); row++) {
                output = output + img_file + "\t" + rt.getRowAsString(row) + "\r\n";
            }

            f.write(output);
            f.close();
        } catch (IOException e) {
            IJ.showMessage("File not found.");
        }
    }

    private void setResultsDir(String dir) {
        GenericDialog gd = new GenericDialog("Results location:");

        // Obtain and create the stitched path
        gd.addStringField("Please specify the parent folder for the analysed result files: ", dir, 20);
        gd.addStringField("Please specify the suffix of the folder(s) for the analysed result files: ", this.resultsSuffix, 20);
        gd.addStringField("Amalgamate summary results to: ", dir + System.getProperty("file.separator") + summaryResultsFile, 20);

        gd.showDialog();
        if (gd.wasCanceled()) {	// Exit the plugin
            return; // TODO:does not really exit plugin ... need to fix!!!
        }
        // Obtain setting values
        this.resultsParent = gd.getNextString();
        this.resultsSuffix = gd.getNextString();
        this.amalgamateSummaryFile = gd.getNextString();
    }

    private void generateSummary(String detailedResultsFile, boolean append) {
        //Get the detailed results file
        String output = "";
        String amalgamateOutput = "";
        String[] summaryParams = settings.getSummaryParams();

        if (!append) { // Add column titles unless appending to existing summary file
            output += "Image";

            for (int i = 0; i < summaryParams.length; i++) {
                if (Boolean.parseBoolean(settings.getSettingValue("output" + summaryParams[i]).toString())) {
                    output += "\t" + summaryParams[i];
                }
            }
            output += "\r\n";
        }
        if (!amalgamate) {
            amalgamateOutput = output;
        }

        String filename = detailedResultsFile;
        filename = filename.substring(0, filename.indexOf("_Results.csv"));


        try {
            FileReader fr = new FileReader(this.resultsDir + System.getProperty("file.separator") + detailedResultsFile);
            //FileReader fr = new FileReader(file);
            BufferedReader bIn = new BufferedReader(fr);

            String line;
            int line_index = 0;

            int brown_count = 0; // Number of positive nuclei
            int blue_count = 0;  // Number of negative nuclei
            int total_count = 0; // Total number of nuclei
            double brown_area_total = 0; // Total area of positive nuclei
            double blue_area_total = 0; // Total area of negative nuclei
            double brown_intensity_total = 0; // Total intensity of positive nuclei
            double blue_intensity_total = 0; // Total intensity of negative nuclei
            double brown_optical_density_total = 0; // Total optical density of positive nuclei
            double blue_optical_density_total = 0; // Total optical density of negative nuclei            

            List blue_areas = new ArrayList();
            List brown_areas = new ArrayList();
            List all_areas = new ArrayList();
            List blue_intensities = new ArrayList();
            List brown_intensities = new ArrayList();
            List all_intensities = new ArrayList();
            List blue_optical_densities = new ArrayList();
            List brown_optical_densities = new ArrayList();
            List all_optical_densities = new ArrayList();

            while ((line = bIn.readLine()) != null) {
                if (line_index == 0) { // First line - headers
                    //do nothing
                } else {
                    // Get the number of blue/brown nuclei along with their area and intensity
                    StringTokenizer st = new StringTokenizer(line);
                    String img = st.nextToken("\t");

                    int particle = Integer.parseInt(st.nextToken("\t"));
                    double area = Double.parseDouble(st.nextToken("\t"));
                    double intensity = Double.parseDouble(st.nextToken("\t"));
                    double optical_density = Double.parseDouble(st.nextToken("\t"));

                    if (img.endsWith("_Blue")) { // Negative Nuclei
                        blue_count++;
                        total_count++;
                        blue_area_total += area;
                        blue_intensity_total += intensity;
                        blue_optical_density_total += optical_density;

                        // For median calculation
                        blue_areas.add(Double.toString(area));
                        blue_intensities.add(Double.toString(intensity));
                        blue_optical_densities.add(Double.toString(optical_density));
                        all_areas.add(Double.toString(area));
                        all_intensities.add(Double.toString(intensity));
                        all_optical_densities.add(Double.toString(optical_density));
                    } else if (img.endsWith("_Brown")) { // Positive Nuclei
                        brown_count++;
                        total_count++;
                        brown_area_total += area;
                        brown_intensity_total += intensity;
                        brown_optical_density_total += optical_density;

                        // For median calculation
                        brown_areas.add(Double.toString(area));
                        brown_intensities.add(Double.toString(intensity));
                        brown_optical_densities.add(Double.toString(optical_density));
                        all_areas.add(Double.toString(area));
                        all_intensities.add(Double.toString(intensity));
                        all_optical_densities.add(Double.toString(optical_density));
                    }
                }
                line_index++;
            }

            // Output totals
            DecimalFormat df = new DecimalFormat("0.00"); // 2 decimal places
            double blue_percent_count = total_count == 0 ? 0 : (double) blue_count / (double) total_count * 100;
            double brown_percent_count = total_count == 0 ? 0 : (double) brown_count / (double) total_count * 100;
            double total_area = blue_area_total + brown_area_total;
            double blue_percent_area = total_area == 0 ? 0 : blue_area_total / total_area * 100;
            double brown_percent_area = total_area == 0 ? 0 : brown_area_total / total_area * 100;
            double blue_area_mean = blue_count == 0 ? 0 : (double) blue_area_total / (double) blue_count;
            double brown_area_mean = brown_count == 0 ? 0 : (double) brown_area_total / (double) brown_count;
            double all_area_mean = total_count == 0 ? 0 : ((double) blue_area_total + (double) brown_area_total) / (double) total_count;
            double blue_intensity_mean = blue_count == 0 ? 0 : blue_intensity_total / (double) blue_count;
            double brown_intensity_mean = brown_count == 0 ? 0 : brown_intensity_total / (double) brown_count;
            double all_intensity_mean = total_count == 0 ? 0 : (blue_intensity_total + brown_intensity_total) / (double) total_count;
            double blue_optical_density_mean = blue_count == 0 ? 0 : blue_optical_density_total / (double) blue_count;
            double brown_optical_density_mean = brown_count == 0 ? 0 : brown_optical_density_total / (double) brown_count;
            double all_optical_density_mean = total_count == 0 ? 0 : (blue_optical_density_total + brown_optical_density_total) / (double) total_count;

            double blue_area_median = blue_count == 0 ? 0 : getMedian(blue_areas);
            double brown_area_median = brown_count == 0 ? 0 : getMedian(brown_areas);
            double all_area_median = total_count == 0 ? 0 : getMedian(all_areas);
            double blue_intensity_median = blue_count == 0 ? 0 : getMedian(blue_intensities);
            double brown_intensity_median = brown_count == 0 ? 0 : getMedian(brown_intensities);
            double all_intensity_median = total_count == 0 ? 0 : getMedian(all_intensities);
            double blue_optical_density_median = blue_count == 0 ? 0 : getMedian(blue_optical_densities);
            double brown_optical_density_median = brown_count == 0 ? 0 : getMedian(brown_optical_densities);
            double all_optical_density_median = total_count == 0 ? 0 : getMedian(all_optical_densities);

            String paramsOutput = filename;
            for (int i = 0; i < summaryParams.length; i++) {
                String param = summaryParams[i];
                if (Boolean.parseBoolean(settings.getSettingValue("output" + param).toString())) {
                    if (param.equals("Blue_Nuclei_Number")) {
                        paramsOutput += "\t" + blue_count;
                    } else if (param.equals("Brown_Nuclei_Number")) {
                        paramsOutput += "\t" + brown_count;
                    } else if (param.equals("Total_Nuclei_Number")) {
                        paramsOutput += "\t" + total_count;
                    } else if (param.equals("Percent_Blue_Number")) {
                        paramsOutput += "\t" + df.format(blue_percent_count);
                    } else if (param.equals("Percent_Brown_Number")) {
                        paramsOutput += "\t" + df.format(brown_percent_count);
                    } else if (param.equals("Blue_Nuclei_Area")) {
                        paramsOutput += "\t" + df.format(blue_area_total);
                    } else if (param.equals("Brown_Nuclei_Area")) {
                        paramsOutput += "\t" + df.format(brown_area_total);
                    } else if (param.equals("Total_Nuclei_Area")) {
                        paramsOutput += "\t" + df.format(total_area);
                    } else if (param.equals("Percent_Blue_Area")) {
                        paramsOutput += "\t" + df.format(blue_percent_area);
                    } else if (param.equals("Percent_Brown_Area")) {
                        paramsOutput += "\t" + df.format(brown_percent_area);
                    } else if (param.equals("Mean_Area_Blue")) {
                        paramsOutput += "\t" + (blue_count == 0 ? "" : df.format(blue_area_mean));
                    } else if (param.equals("Median_Area_Blue")) {
                        paramsOutput += "\t" + (blue_count == 0 ? "" : df.format(blue_area_median));
                    } else if (param.equals("Mean_Area_Brown")) {
                        paramsOutput += "\t" + (brown_count == 0 ? "" : df.format(brown_area_mean));
                    } else if (param.equals("Median_Area_Brown")) {
                        paramsOutput += "\t" + (brown_count == 0 ? "" : df.format(brown_area_median));
                    } else if (param.equals("Mean_Area_All")) {
                        paramsOutput += "\t" + (total_count == 0 ? "" : df.format(all_area_mean));
                    } else if (param.equals("Median_Area_All")) {
                        paramsOutput += "\t" + (total_count == 0 ? "" : df.format(all_area_median));
                    } else if (param.equals("Mean_Intensity_Blue")) {
                        paramsOutput += "\t" + (blue_count == 0 ? "" : df.format(blue_intensity_mean));
                    } else if (param.equals("Median_Intensity_Blue")) {
                        paramsOutput += "\t" + (blue_count == 0 ? "" : df.format(blue_intensity_median));
                    } else if (param.equals("Mean_Intensity_Brown")) {
                        paramsOutput += "\t" + (brown_count == 0 ? "" : df.format(brown_intensity_mean));
                    } else if (param.equals("Median_Intensity_Brown")) {
                        paramsOutput += "\t" + (brown_count == 0 ? "" : df.format(brown_intensity_median));
                    } else if (param.equals("Mean_Intensity_All")) {
                        paramsOutput += "\t" + (total_count == 0 ? "" : df.format(all_intensity_mean));
                    } else if (param.equals("Median_Intensity_All")) {
                        paramsOutput += "\t" + (total_count == 0 ? "" : df.format(all_intensity_median));
                    } else if (param.equals("Mean_Optical_Density_Blue")) {
                        paramsOutput += "\t" + (blue_count == 0 ? "" : df.format(blue_optical_density_mean));
                    } else if (param.equals("Median_Optical_Density_Blue")) {
                        paramsOutput += "\t" + (blue_count == 0 ? "" : df.format(blue_optical_density_median));
                    } else if (param.equals("Mean_Optical_Density_Brown")) {
                        paramsOutput += "\t" + (brown_count == 0 ? "" : df.format(brown_optical_density_mean));
                    } else if (param.equals("Median_Optical_Density_Brown")) {
                        paramsOutput += "\t" + (brown_count == 0 ? "" : df.format(brown_optical_density_median));
                    } else if (param.equals("Mean_Optical_Density_All")) {
                        paramsOutput += "\t" + (total_count == 0 ? "" : df.format(all_optical_density_mean));
                    } else if (param.equals("Median_Optical_Density_All")) {
                        paramsOutput += "\t" + (total_count == 0 ? "" : df.format(all_optical_density_median));
                    } else if (param.equals("Time_ms")) {
                        endTime = System.currentTimeMillis();
                        long timeDiff = endTime - startTime;
                        paramsOutput += "\t" + timeDiff;
                    }
                }
            }
            paramsOutput += "\r\n";

            output += paramsOutput;
            amalgamateOutput += paramsOutput;

            // Perform texture analysis
            /*
             * runCmdOnWindow(this.currImgFullName, "8-bit"); Hashtable roiMap =
             * autoSelectROI(this.currImgFullName); ImagePlus imp =
             * WindowManager.getCurrentImage(); ImageProcessor ip =
             * imp.getProcessor(); ParticleAnalyzer pa = new ParticleAnalyzer();
             * Hashtable results = pa.doTextureAnalysis(imp, ip, roiMap, 1);
             */
        } catch (IOException e) {
            IJ.showMessage("File '" + this.resultsDir + System.getProperty("file.separator") + detailedResultsFile + "' not found.");
        }
        //}

        // Print results to summary file
        try {
            FileWriter f = new FileWriter(this.resultsDir + System.getProperty("file.separator") + summaryResultsFile, append);
            f.write(output);
            f.close();

            // Amalgamate the summary results
            if (amalgamateSummaryFile.length() > 0) {
                f = new FileWriter(amalgamateSummaryFile, amalgamate);
                f.write(amalgamateOutput);
                f.close();
                amalgamate = true;
            }
        } catch (IOException e) {
            IJ.showMessage("File '" + this.resultsDir + System.getProperty("file.separator") + summaryResultsFile + "' not found.");
        }
    }

    // TODO. to be deleted after Settings_Editor is done
    private void calibrate(double distanceInPixels, double knownDistance, String unitOfLength) {
        Calibration cal = new Calibration();
        if (unitOfLength.equals("um")) {
            unitOfLength = IJ.micronSymbol + "m";
        }
        cal.setUnit(unitOfLength);

        double pixelsPerUnit = distanceInPixels / knownDistance;
        cal.pixelWidth = cal.pixelHeight = 1 / pixelsPerUnit;

        ImagePlus imp = new ImagePlus();
        imp.setGlobalCalibration(cal);
    }

    private double getMedian(List list) {
        double result = Double.NaN;
        Collections.sort(list);
        int count = list.size();
        if (count > 0) {
            if (count % 2 == 0) { // Even number
                Object value1 = list.get(count / 2 - 1);
                Object value2 = list.get(count / 2);
                result = (Double.parseDouble(value1.toString()) + Double.parseDouble(value2.toString())) / 2.0;
            } else { // Odd number
                result = Double.parseDouble(list.get((count - 1) / 2).toString());
            }
        }

        return result;
    }

    private void threshold(ImagePlus imp, String threshold, double correction) {
        if (threshold.equals(StainAnalyzerSettings.THRESHOLD_CHOICE_AUTO_THRESHOLD)) {
            //if (threshold == AUTO_THRESHOLD) {
//            ThresholdAdjuster ta = new ThresholdAdjuster(imp);
//            ImageProcessor ip = imp.getProcessor();            
//            ip.setThreshold(ta.getMinAutoThreshold(), ta.getMaxAutoThreshold(), ImageProcessor.RED_LUT);
//            Thresholder t = new Thresholder();
//            t.setSkipDialog(true);
//            t.applyThreshold(imp);  
//        } else if (threshold == CORRECTED_AUTO_THRESHOLD) { 
            ThresholdAdjuster ta = new ThresholdAdjuster(imp);
            ImageProcessor ip = imp.getProcessor();
            ip.setThreshold(ta.getMinAutoThreshold(), (int) (ta.getMaxAutoThreshold() * correction), ImageProcessor.RED_LUT);

            System.out.println("auto threshold values ... max=" + (int) (ta.getMaxAutoThreshold() * correction) + " min=" + ta.getMinAutoThreshold());

            Thresholder t = new Thresholder();
            t.setSkipDialog(true);
            t.applyThreshold(imp);
        } else if (threshold.equals(StainAnalyzerSettings.THRESHOLD_CHOICE_MAXIMUM_ENTROPY_THRESHOLD)) {
            //} else if (threshold == MAX_ENTROPY_THRESHOLD) {
            ImageProcessor ip = imp.getProcessor();
            EntropyThreshold et = new EntropyThreshold();
            et.run(ip);
            imp.updateAndDraw();
//        } else if (threshold == K_MEANS_CLUSTERING_THRESHOLD) {
            //lutColor = ImageProcessor.OVER_UNDER_LUT;
        } else if (threshold.equals(StainAnalyzerSettings.THRESHOLD_CHOICE_MANUALLY_ADJUST_THRESHOLD)) {
            //} else if (threshold == MANUAL_THRESHOLD) {
            imp.show(); // Show the image for visual control of thresholding
            IJ.run("View 100%");
            IJ.setTool(12); // Select the hand tool allowing user to move the image
            ThresholdAdjuster ta = new ThresholdAdjuster();
            ta.positionWindow(ThresholdAdjuster.TOP_RIGHT);
            ta.run();
        } else if (threshold.equals(StainAnalyzerSettings.THRESHOLD_CHOICE_AUTO_SET_THRESHOLD)) {
            //} else if (threshold == AUTO_SET_THRESHOLD) { // Using "Set" will retain the intensities / gray values
            ThresholdAdjuster ta = new ThresholdAdjuster(imp);
            ImageProcessor ip = imp.getProcessor();
            //IJ.setThreshold(ta.getMinAutoThreshold(), ta.getMaxAutoThreshold());
            ip.setThreshold(ta.getMinAutoThreshold(), ta.getMaxAutoThreshold(), ImageProcessor.RED_LUT);
            imp.updateAndDraw();
        } else if (threshold.equals(StainAnalyzerSettings.THRESHOLD_CHOICE_SET_ENTIRE_THRESHOLD)) {
            //} else if (threshold == SET_ENTIRE_THRESHOLD) { // Using "Set" will retain the intensities / gray values
            ThresholdAdjuster ta = new ThresholdAdjuster(imp);
            ImageProcessor ip = imp.getProcessor();
            //IJ.setThreshold(1, 254);
            ip.setThreshold(1, 254, ImageProcessor.RED_LUT);
            imp.updateAndDraw();
        } else if (threshold.equals(StainAnalyzerSettings.THRESHOLD_CHOICE_MANUALLY_ADJUST_AND_RECORD_THRESHOLD)) {
            //} else if (threshold == MANUAL_RECORD_THRESHOLD) {
            imp.show(); // Show the image for visual control of thresholding
            IJ.run("View 100%");
            IJ.setTool(12); // Select the hand tool allowing user to move the image
            ThresholdAdjuster ta = new ThresholdAdjuster();
            ta.positionWindow(ThresholdAdjuster.TOP_RIGHT);
            ta.run();

            // See if results file exists already
            boolean append;
            String thresholdResultsFile = this.resultsDir + System.getProperty("file.separator") + "Thresholds.csv";
            File f = new File(thresholdResultsFile);
            if (f.exists()) {
                append = true;
            } else {
                append = false;
            }

            // Record results to file
            try {
                FileWriter fw = new FileWriter(thresholdResultsFile, append);
                String output = "";
                if (!append) { // Output headers
                    output = output + "Image\tStain\tMin_Auto_Threshold\tMin_Threshold\tMax_Auto_Threshold\tMax_Threshold\r\n";
                    //output = output + "Image\tStain\tAuto_Threshold\r\n";
                }
                output = output + this.currImgFullName + "\t" + this.currColour + "\t";
                output = output + ta.getMinAutoThreshold() + "\t";
                output = output + ta.getMinThreshold() + "\t";
                output = output + ta.getMaxAutoThreshold() + "\t";
                output = output + ta.getMaxThreshold() + "\r\n";

                fw.write(output);
                fw.close();
            } catch (IOException e) {
                IJ.showMessage("File not found.");
            }
        }
    }

    private ImagePlus getImp(String title) {
        Frame frame = WindowManager.getFrame(title);
        ImagePlus imp;
        if (frame != null && (frame instanceof ImageWindow)) {
            ImageWindow iw = (ImageWindow) frame;
            imp = iw.getImagePlus();
        } else {
            imp = null;
        }

        return imp;
    }

    private void addImage(String title, ImagePlus imp) { // Added the specific ImagePlus object
        if (imp != null) {
            imp.hide();
            images.put(title, imp);
        }
    }

    private void addImage(String title) { // Adds the specified image window to the images hashtable and hide the window
        ImagePlus imp = getImp(title);
        addImage(title, imp);
    }

    private ImagePlus getImage(String title) { // Obtains the specified ImagePlus object from hashtable given the title
        ImagePlus imp = null;
        if (images.containsKey(title)) {
            imp = (ImagePlus) images.get(title);
        }

        return imp;
    }

    private void showImage(String title) { // Shows the image
        if (images.containsKey(title)) {
            ImagePlus imp = (ImagePlus) images.get(title);
            imp.show();
        }
    }

    private void hideImage(String title) { // Hides the image
        if (images.containsKey(title)) {
            ImagePlus imp = (ImagePlus) images.get(title);
            imp.hide();
        }
    }

    private void removeImage(String title) { // Removes the specified image window from the hashtable
        if (images.containsKey(title)) {
            ImagePlus imp = (ImagePlus) images.get(title);
            ImageWindow iw = imp.getWindow();
            if (iw != null) {
                iw.close();
            }
            imp = null;
            images.remove(title);
        }
    }

    private boolean imageExist(String title) {
        if (images.containsKey(title)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean hasWindow(String title) {
        boolean b = false;
        Frame frame = WindowManager.getFrame(title);
        if (frame != null && (frame instanceof ImageWindow)) {
            b = true;
        }

        return b;
    }

    // TODO: TO BE DETELED after Settings_Editor is done
    private Hashtable setupDiscriminantParameters() {
        Hashtable discriminantParameters = new Hashtable();
        discriminantParameters.put("discriminantConstant", new Float(-23.1410538314311));
        discriminantParameters.put(new Integer(HaralickTexture.ANGULAR_SECOND_MOMENT), new Float(-8.02755953028826));
        discriminantParameters.put(new Integer(HaralickTexture.CONTRAST), new Float(0.0350600819933612));
        discriminantParameters.put(new Integer(HaralickTexture.CORRELATION), new Float(-9.59871706945685));
        discriminantParameters.put(new Integer(HaralickTexture.INVERSE_DIFFERENCE_MOMENT), new Float(24.8885241622283));
        discriminantParameters.put(new Integer(HaralickTexture.SUM_AVERAGE), new Float(0.136898504940928));
        discriminantParameters.put(new Integer(HaralickTexture.SUM_VARIANCE), new Float(-0.0746744438954557));
        discriminantParameters.put(new Integer(HaralickTexture.SUM_ENTROPY), new Float(30.2821895638951));
        discriminantParameters.put(new Integer(HaralickTexture.ENTROPY), new Float(-23.7397323909399));
        discriminantParameters.put(new Integer(HaralickTexture.DIFFERENCE_VARIANCE), new Float(-4.35188925570739));
        discriminantParameters.put(new Integer(HaralickTexture.DIFFERENCE_ENTROPY), new Float(21.5273012143129));
        discriminantParameters.put(new Integer(HaralickTexture.INFORMATION_MEASURE_A), new Float(-16.3600558252773));
        discriminantParameters.put(new Integer(HaralickTexture.INFORMATION_MEASURE_B), new Float(-1.1594920275754));
        discriminantParameters.put(new Integer(HaralickTexture.COEFFICIENT_OF_VARIATION), new Float(9.10594391477136));
        discriminantParameters.put(new Integer(HaralickTexture.PEAK_TRANSITION_PROBABILITY), new Float(-2.01854543890479));
        discriminantParameters.put(new Integer(HaralickTexture.DIAGONAL_MOMENT), new Float(0.960551831324328));

        return discriminantParameters;
    }

    ///GPEC mod - added this function to calculate mean gray level of image    
    private double getMeanGrayLevel(ImagePlus imp, int minThreshold, int maxThreshold) {
        double mean = 0.0;
        double sum = 0.0;
        int count = 0;

        byte[] pixels = (byte[]) imp.getProcessor().getPixels();
        int width = imp.getWidth();
        int height = imp.getHeight();

        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                int intensity = 0xff & pixels[offset + x];
                if (intensity >= minThreshold && intensity <= maxThreshold) {
                    sum += intensity;
                    count++;
                }
            }
        }
        //System.out.println("Count: " + count);
        return count > 0 ? sum / count : 0;
    }

    private void logProgress(String msg) {
        if (logProgress) {
            try {
                FileWriter fw = new FileWriter(resultsDir + System.getProperty("file.separator") + "Nuclear_Stain_Analyzer_" + Thread.currentThread().getId() + ".log", true);
                Calendar cal = Calendar.getInstance(TimeZone.getDefault());

                String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(DATE_FORMAT);
                sdf.setTimeZone(TimeZone.getDefault());

                fw.write(sdf.format(cal.getTime()) + " (" + currImgFullName + "): " + msg + "\r\n");
                fw.close();
            } catch (IOException e) {
                //IJ.showMessage("File not found.");
            }
        }
    }

    /**
     * Displays the image for the user to select. Waits in a *TIGHT* loop until
     * the selection is made
     *
     * @TODO: Get rid of this tight loop
     *
     * @param currImage
     * @return path to cropped image
     */
    private String selectImageArea(ImagePlus currImage) {
        Roi selection = null;
        currImage.show();

        //the user MUST make a selection
        while (selection == null) {

            /**
             * Mod by Dmitry 2011-07-28: Class WaitForUserDialog appears
             * starting from ImageJ 1.39r; it was not available when this part
             * of the plugin was programmed. Hurrah, the tight loop is broken! I
             * don't know how to move the dialogue window from the centre of the
             * screen to the corner automatically, but it can be dragged out of
             * the way when working with the first image, and it stays where
             * it's left; at least this is true for Mac OS X 10.7.
             */
            new WaitForUserDialog("Making selection(s)", "Select region(s) and click OK.").show();
            selection = currImage.getRoi();

            /**
             * GeneralDialog okDialog = new GeneralDialog("Continue",false);
             * JPanel panel = new JPanel(); panel.setLayout(new FlowLayout());
             * // JPanel panel = new JPanel(new FlowLayout());
             *
             * //In the constructor for a JDialog subclass:
             * okDialog.resetNumRows(); okDialog.setAlwaysOnTop(true);
             *
             * okDialog.addLabel(panel, "t", "Click OK To Continue",
             * SwingConstants.CENTER, true); okDialog.setLabel("t", "Click OK To
             * Continue"); okDialog.showDialog();
             *
             * while(okDialog.isDisplayable()){
             * try{Thread.sleep(1000);}catch(Exception e){} //Sit in a tight
             * loop :( This is a really really, REALLY BAD way to do this! //Woo
             * for wasting cycles }
             *
             * //Now that we have something selected, lets get it selection =
             * currImage.getRoi();
             */
        }

        //Grab a handle to the current image
        ImageProcessor currProcessor = currImage.getProcessor();
        currProcessor.setRoi(selection);

        //set the fill color
        IJ.setBackgroundColor(255, 255, 255);
        IJ.setForegroundColor(255, 255, 255);

        //Clear The non-selected portion of the image
        Executer clear = new Executer("Clear Outside");
        clear.run();

        //crop the current image to the bounding rectange as defined by the ROI above
        currProcessor.setRoi(selection);
        currProcessor = currProcessor.crop();

        //Display all those wonderful changes
        currImage.setProcessor(currImgName, currProcessor);
        //currImage.updateImage();
        currImage.show();
        currImage.hide();

        String path = this.selectionDir + System.getProperty("file.separator") + this.currImgName + ".tif";
        //String path = this.selectionDir + System.getProperty("file.separator") + this.currImgName + ".bmp";
        //Save the image to the temp directory
        //new FileSaver(currImage).saveAsJpeg(this.selectionDir + System.getProperty("file.separator") + this.currImgName + ".jpg");
        new FileSaver(currImage).saveAsTiff(path);
        //new FileSaver(currImage).saveAsBmp(path);
        return path;
    }
}
