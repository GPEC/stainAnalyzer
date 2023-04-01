/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.gpec.ia.analyzer.processing;

import ca.ubc.gpec.ia.analyzer.gui.GeneralDialog;
import ca.ubc.gpec.ia.analyzer.gui.GeneralTable;
import ca.ubc.gpec.ia.analyzer.processing.HaralickTexture;
import ij.*;
import ij.gui.*;
import ij.io.DirectoryChooser; // not used, but present in the original version
import ij.io.OpenDialog;
import ij.process.*;
import ij.plugin.PlugIn;

// Import Java libraries
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog; // not used, but present in the original version
import java.util.Hashtable;
import java.util.ArrayList;
import java.io.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.border.Border; // not used, but present in the original version

// Import GPEC modified ImageJ libraries
import ca.ubc.gpec.ia.analyzer.segmentation.Thresholder; // not used, but present in the original version
import ca.ubc.gpec.ia.analyzer.segmentation.ThresholdAdjuster;
import ca.ubc.gpec.ia.analyzer.measure.ResultsTable;

/**
 *
 * @author samuelc
 */
public class HaralickTextureAnalyzer {
    // Constants
    private final static int CURRENT_IMG=1, IMGS_FROM_HARD_DISK=2;
    private final static int DISPLAY_RESULTS=1, SAVE_RESULTS=2;
    private final static int WHOLE_IMG_ROI=1, INDIVIDUAL_PARTICLES=2;
    private final static int MANUAL_THRESHOLD=1, AUTO_THRESHOLD=2;
    
    // Attributes
    private int imageSelection;     // source of image to be analyzed
    private String imagesDir;       // Folder that contains images to be analyzed
    private ArrayList images;       // Images to be analyzed
    private int analysisResults;    // where the analysis results should go
    private String resultsDir;      // Folder that contains the result files
    private String resultsFile;     // The file that contains the analysis results
    private int analyzeObject;      // which object(s) to be analyzed
    private int threshold;          // manual or auto threshold the image
    private double minSize;         // minimum particle size
    private double maxSize;         // maximum particle size
    private int degree;             // degree (0, 45, 90, 135, Average) to be used for texture analysis 
    private int stepSize;           // step size of texture analysis
    private int grayLevels;         // number of gray levels for texture analysis
    private int precision;          // precision of the results
    private int computeFeatures;    // what features to compute
    
    /**
     * The method called upon running the plugin from the ImageJ's Plugin menu
     *
     * @param   arg Argument passed by ImageJ when running the plugin
     */
    public void run(String arg) {
        if (arg.equals("about")) {
            showAbout(); 
            return;
        }   
        
        if (!showAnalysisDialog()) {return;} // Ask user to specify analysis parameters
        
        if (imageSelection == CURRENT_IMG) { // Analyze current image
            ImagePlus imp = WindowManager.getCurrentImage();
            analyzeImage(imp);
        }
        else if (imageSelection == IMGS_FROM_HARD_DISK) { // Ask user to select image(s) from hard disk
            for (int i=0; i < images.size(); i++) {
                IJ.open(imagesDir + System.getProperty("file.separator") + images.get(i).toString());
                ImagePlus imp = WindowManager.getCurrentImage();
                analyzeImage(imp);
            }
        }
        
        IJ.showMessage("Analysis complete");
    }
    
    /**
     * Generates and shows the dialog which prompts user for settings.
     */
    private boolean showAnalysisDialog() {
        GeneralDialog gd = new GeneralDialog("Haralick Texture");
        
        // Create the individual tab panels
        JPanel generalPanel = createGeneralPanel(gd);
        gd.addTab("General", generalPanel);
        
        JPanel featuresPanel = createFeaturesPanel(gd);  
        gd.addTab("Features", featuresPanel);
        
        // Add help message
        gd.addHelpMsg(createHelpMsg());
        
        gd.showDialog();
        if (gd.wasCanceled()) {return false;}
        
        String comboChoice = "";
        // File settings
        comboChoice = gd.getTableValue("fileSettings", 0, 1).toString();
        if (comboChoice.equals("Current image")) {imageSelection = CURRENT_IMG;} 
        else if (comboChoice.equals("Image(s) from hard disk")) {imageSelection = IMGS_FROM_HARD_DISK;} 
        
        comboChoice = gd.getTableValue("fileSettings", 1, 1).toString();
        if (comboChoice.equals("Display")) {analysisResults = DISPLAY_RESULTS;} 
        else if (comboChoice.equals("Save to hard disk")) {analysisResults = SAVE_RESULTS;}         
        
        // Particle settings
        comboChoice = gd.getTableValue("particleSettings", 0, 1).toString();
        if (comboChoice.equals("Whole image/ROI")) {analyzeObject = WHOLE_IMG_ROI;} 
        else if (comboChoice.equals("Individual particles")) {analyzeObject = INDIVIDUAL_PARTICLES;}  
        
        comboChoice = gd.getTableValue("particleSettings", 1, 1).toString();
        if (comboChoice.equals("Auto")) {threshold = AUTO_THRESHOLD;} 
        else if (comboChoice.equals("Manual")) {threshold = MANUAL_THRESHOLD;}         
        
        minSize = Double.parseDouble(gd.getTableValue("particleSettings", 2, 1).toString());
        maxSize = Double.parseDouble(gd.getTableValue("particleSettings", 3, 1).toString());        
        
        // Texture analysis settings
        comboChoice = gd.getTableValue("textureSettings", 0, 1).toString();
        if (comboChoice.equals("0")) {degree = HaralickTexture.DEG_0;} 
        else if (comboChoice.equals("45")) {degree = HaralickTexture.DEG_45;} 
        else if (comboChoice.equals("90")) {degree = HaralickTexture.DEG_90;} 
        else if (comboChoice.equals("135")) {degree = HaralickTexture.DEG_135;} 
        else if (comboChoice.equals("Average")) {degree = HaralickTexture.DEG_AVG;}
        
        stepSize = Integer.parseInt(gd.getTableValue("textureSettings", 1, 1).toString());
        grayLevels = Integer.parseInt(gd.getTableValue("textureSettings", 2, 1).toString());
        precision = Integer.parseInt(gd.getTableValue("textureSettings", 3, 1).toString());      
        
        // Feature settings
        HaralickTexture mt = new HaralickTexture();
        int [] features = mt.getFeatures();
        String [] featureTitles = mt.getFeatureTitles(); 
        computeFeatures = 0;
        for (int i=0; i<features.length; i++) {
            boolean checked = Boolean.parseBoolean(gd.getTableValue("features", i, 1).toString());
            if (checked) {computeFeatures += features[i];}
        }
        
        if (imageSelection == IMGS_FROM_HARD_DISK) { // Ask user to select images from hard disk
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);	// enable multiple select
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY); // Only allow user to choose files
            fc.setDialogTitle("Please specify image(s) to be analysed");
            int returnVal = fc.showOpenDialog(IJ.getInstance());
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            
            imagesDir = fc.getCurrentDirectory().getAbsolutePath();
            File[] fs = fc.getSelectedFiles();
            images = new ArrayList();
            for (int i = 0; i < fs.length; i++) {
                File f = fs[i];
                if (f.isFile()) { 
                    images.add(f.getName());
                }
            }
        }
        
        if (analysisResults == SAVE_RESULTS) { // Ask user to specify the location of results file
            OpenDialog od = new OpenDialog("Please specify the results file", resultsDir, "Texture_Results.csv"); // .txt is changed to .csv by Dmitry 2011-07-29
            resultsDir = od.getDirectory();
            resultsFile = resultsDir + od.getFileName();
        }
        
        return true;
    }

    /**
     * Shows the 'About' message.
     */  
    private void showAbout() {
        IJ.showMessage("A plugin for performing texture analysis and calculate texture features as described by Haralick.\r\n" + "\r\n" +
                "For more info, please click on the 'Help' button of the plugin");
    }

    /**
     * Creates the panel under the "General" tab
     *
     * @param   gd  The GeneralDialog object where the current panel will be added to
     * @return  The created JPanel object  
     */
    private JPanel createGeneralPanel(GeneralDialog gd) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        int widthRatio = 7;
        
        // Default selections
        Hashtable defaults = new Hashtable();
        defaults.put("imageSelection", "Image(s) from hard disk");
        defaults.put("analysisResults", "Save to hard disk");
        defaults.put("analyzeObject", "Individual particles");
        defaults.put("threshold", "Manual");
        defaults.put("minSize", new Integer(1));
        defaults.put("maxSize", new Integer(999999));
        defaults.put("degree", "Average");
        defaults.put("stepSize", new Integer(1));
        defaults.put("grayLevels", new Integer(8));
        defaults.put("precision", new Integer(3));
        
        // File settings
        String [] fileSettings = {"imageSelection","analysisResults"};
        String [] fileSettingTitles = {"Image(s) selection","Analysis results"};
        int [] fileSettingColumnWidths = new int[2];
        String [] fileSettingColumnNames = {"", ""};
        Object[][] fileSettingsData = new Object[fileSettings.length][fileSettingColumnWidths.length];
        
        for (int i=0; i<fileSettings.length; i++) {
            fileSettingsData[i][0] = new JLabel(fileSettingTitles[i]);
            if (fileSettings[i].equals("imageSelection")) {
                JComboBox combo = new JComboBox();
                combo.addItem("Current image");
                combo.addItem("Image(s) from hard disk");
                combo.setSelectedItem(defaults.get("imageSelection").toString());
                fileSettingsData[i][1] = combo;
            } 
            else if (fileSettings[i].equals("analysisResults")) {
                JComboBox combo = new JComboBox();
                combo.addItem("Display");
                combo.addItem("Save to hard disk");   
                combo.setSelectedItem(defaults.get("analysisResults").toString());
                fileSettingsData[i][1] = combo;                
            }
        }
        
        GeneralTable fileSettingsTable = new GeneralTable(fileSettingsData, fileSettingColumnNames);
        // Set column width
        for (int i=0; i<fileSettings.length; i++) {
            // Title column
            fileSettingColumnWidths[0] = Math.max(fileSettingColumnWidths[0], fileSettingTitles[0].length() * widthRatio);
            
            // Value column
            fileSettingColumnWidths[1] = Math.max(fileSettingColumnWidths[1], 8);
        }
        
        panel.add(new JLabel("File settings:"));
        gd.addTable(panel, "fileSettings", fileSettingsTable);
        
        // Particles settings
        String [] particleSettings = {"analyzeObject","threshold","minSize","maxSize"};
        String [] particleSettingTitles = {"Analyze","Threshold","Particles min size","Particles max size"};
        int [] particleSettingColumnWidths = new int[2];
        String [] particleSettingColumnNames = {"", ""};
        Object[][] particleSettingsData = new Object[particleSettings.length][particleSettingColumnWidths.length];
        
        for (int i=0; i<particleSettings.length; i++) {
            particleSettingsData[i][0] = new JLabel(particleSettingTitles[i]);
            if (particleSettings[i].equals("analyzeObject")) {
                JComboBox combo = new JComboBox();
                combo.addItem("Whole image/ROI");
                combo.addItem("Individual particles");
                combo.setSelectedItem(defaults.get("analyzeObject").toString());
                particleSettingsData[i][1] = combo;                
            }
            else if (particleSettings[i].equals("threshold")) {
                JComboBox combo = new JComboBox();
                combo.addItem("Auto");
                combo.addItem("Manual");
                combo.setSelectedItem(defaults.get("threshold").toString());
                particleSettingsData[i][1] = combo;                
            }
            else {
                particleSettingsData[i][1] = new JTextField(defaults.get(particleSettings[i]).toString());
            }       
        }
        
        GeneralTable particleSettingsTable = new GeneralTable(particleSettingsData, particleSettingColumnNames);
        // Set column width
        for (int i=0; i<particleSettings.length; i++) {
            // Title column
            particleSettingColumnWidths[0] = Math.max(particleSettingColumnWidths[0], particleSettingTitles[0].length() * widthRatio);
            
            // Value column
            particleSettingColumnWidths[1] = Math.max(particleSettingColumnWidths[1], 8);
        }
        
        panel.add(new JLabel(" "));        
        panel.add(new JLabel("Particle settings:"));
        gd.addTable(panel, "particleSettings", particleSettingsTable);        
        
        // Texture analysis settings
        String [] textureSettings = {"degree","stepSize","grayLevels","precision"};
        String [] textureSettingTitles = {"Degree","Step Size","Gray Levels","Precision"};
        int [] textureSettingColumnWidths = new int[2];
        String [] textureSettingColumnNames = {"", ""};
        Object[][] textureSettingsData = new Object[textureSettings.length][textureSettingColumnWidths.length];
        
        for (int i=0; i<textureSettings.length; i++) {
            textureSettingsData[i][0] = new JLabel(textureSettingTitles[i]);
            if (textureSettings[i].equals("degree")) {
                HaralickTexture ht = new HaralickTexture();
                String [] degreeTitles = ht.getDegreeTitles();
                
                JComboBox combo = new JComboBox();
                // Populate the combobox list
                for (int j=0; j<degreeTitles.length; j++) {
                    combo.addItem(degreeTitles[j]);
                }
                combo.setSelectedItem(defaults.get("degree").toString());
                textureSettingsData[i][1] = combo;
            } 
            else {
                textureSettingsData[i][1] = new JTextField(defaults.get(textureSettings[i]).toString());
            }
        }
        
        GeneralTable textureSettingsTable = new GeneralTable(textureSettingsData, textureSettingColumnNames);
        // Set column width
        for (int i=0; i<textureSettings.length; i++) {
            // Title column
            textureSettingColumnWidths[0] = Math.max(textureSettingColumnWidths[0], textureSettingTitles[0].length() * widthRatio);
            
            // Value column
            textureSettingColumnWidths[1] = Math.max(textureSettingColumnWidths[1], 8);
        }
        
        panel.add(new JLabel(" "));
        panel.add(new JLabel("Texture analysis settings:"));
        gd.addTable(panel, "textureSettings", textureSettingsTable);        
        
        return panel;
    }

    /**
     * Creates the panel under the "Features" tab
     *
     * @param   gd  The GeneralDialog object where the current panel will be added to
     * @return  The created JPanel object
     */
    private JPanel createFeaturesPanel(GeneralDialog gd) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        int widthRatio = 7;
        
        HaralickTexture mt = new HaralickTexture();
        int [] features = mt.getFeatures();
        String [] featureTitles = mt.getFeatureTitles();
        
        String[] featureColumnNames = {"Feature", "Calculate"};
        int [] featureColumnWidths = new int[2];
        
        Object[][] featureData = new Object[features.length][featureColumnNames.length];
        
        for (int i=0; i<features.length; i++) {
            int feature = features[i];
            int thisWidth = 0;
            // Feature column
            String title = featureTitles[i].replace("_", " ");
            featureData[i][0] = new JLabel(title);
            thisWidth = Math.max(featureColumnNames[0].length(), title.length()) * widthRatio;
            featureColumnWidths[0] = Math.max(featureColumnWidths[0], thisWidth);
            
            // Calculate column
            boolean calculate = true;
            if (feature == HaralickTexture.MAXIMAL_CORRELATION_COEFFICIENT) {calculate = false;}
            featureData[i][1] = new JCheckBox("", calculate);
        }
        
        GeneralTable featureTable = new GeneralTable(featureData, featureColumnNames);
        
        // Set column width
        for (int i=0; i<featureColumnWidths.length; i++) {
            int width = featureColumnWidths[i];
            if (width == 0) {width = featureColumnNames[i].length() * widthRatio;}
            featureTable.setColumnWidth(i, width);
        }
        
        gd.addTableWithHeader(panel, "features", featureTable);
        
        return panel;
    }   
    
    /**
     * Perform texture analysis on a single image
     *
     * @param   imp The ImagePlus object representing the image to be analyzed
     */
    private void analyzeImage (ImagePlus imp) {        
        if (analyzeObject == WHOLE_IMG_ROI) { // Do the analysis on whole image/ROI
            // First convert image to 8-bit
            ImageConverter ic = new ImageConverter(imp); 
            ic.convertToGray8();   
            
            // Run texture analysis
            HaralickTexture ht = new HaralickTexture(degree, stepSize, grayLevels, precision, computeFeatures);
            ht.analyzeImage(imp, imp.getProcessor(), null, null);
        
            if (analysisResults == DISPLAY_RESULTS) { // Display the results
                ht.displayResults();
            }   
            else if (analysisResults == SAVE_RESULTS) { // Save the results to hard disk
                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(precision); // Set the number of decimal places
                String headers = "Image\tParticle";
                String values = imp.getTitle() + "\tWhole";
                Hashtable results = ht.getResults(imp.getTitle());
                int [] features = ht.getFeatures();
                for (int i=0; i<features.length; i++) {
                    int feature = features[i];
                    if (results.containsKey(new Integer(feature))) {
                        headers += "\t" + ht.getFeatureTitle(feature);
                        values += "\t" + df.format(Double.parseDouble(results.get(new Integer(feature)).toString()));
                    }
                }
                
                headers += "\r\n";
                values += "\r\n";
                saveResults(headers, values);
            }
        }
        else if (analyzeObject == INDIVIDUAL_PARTICLES) { // Do the analysis on individual particles
            // Duplicate original image for overlay image to be drawn
            ImagePlus overlayImp = NewImage.createRGBImage(imp.getTitle() + " Overlay", imp.getWidth(), imp.getHeight(), 1, NewImage.FILL_WHITE);
            ImageProcessor overlayIP = overlayImp.getProcessor();
            overlayIP.copyBits(imp.getProcessor(), 0, 0, Blitter.COPY);
            
            // Convert image to 8-bit
            ImageConverter ic = new ImageConverter(imp); 
            ic.convertToGray8();             
            ImageProcessor ip = imp.getProcessor();    
            
            // Threshold the image according to the threshold choice specified by the user
            threshold(imp, threshold);
            
            // Analyze particles
            int options = ParticleAnalyzer.SHOW_OVERLAY;
            int measurements = ParticleAnalyzer.TEXTURE;
            ResultsTable rt = new ResultsTable();
            ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, minSize, maxSize);
            pa.setOverlayImp(overlayImp); // Set overlay image     
            Color c = new Color(255, 255, 0); // Set overlay colour to yellow             
            pa.setOverlayColour(c);        
            pa.setTextureParameters(degree, stepSize, grayLevels, precision, computeFeatures, new Hashtable());
            pa.analyze(imp, ip);
            
            if (analysisResults == DISPLAY_RESULTS) { // Display the results
                rt.show(imp.getTitle());
                overlayImp.show();
            }     
            else if (analysisResults == SAVE_RESULTS) { // Save the results to hard disk
                String headers = "Image\tParticle";
                for (int i=1; i<=rt.getLastColumn(); i++) {
                    if (rt.columnExists(i)) {
                        headers += "\t" + rt.getColumnHeading(i);
                    }
                }
                headers += "\r\n";
               
                String values = "";
                for (int row = 0; row < rt.getCounter(); row++) {
                     values += imp.getTitle() + "\t" + rt.getRowAsString(row) + "\r\n";
                }    
                
                saveResults(headers, values);
                
                // Save the overlay image as well
                overlayImp.show(); // added by Dmitry 2011-07-30
                IJ.selectWindow(imp.getTitle() + " Overlay");
                IJ.run("Jpeg...", "save=" + resultsDir + System.getProperty("file.separator") + imp.getTitle() + "_Overlay.jpg");
                IJ.run("Close");               
            }
        }
        imp.getWindow().close();
    }

    /**
     * Perform thresholding on the image using the threshold option specified by the user
     *
     * @param   imp The ImagePlus object representing the image to be thresholded
     * @param   threshold   An integer representing either "Manual" or "Auto" thresholding
     */    
    private void threshold(ImagePlus imp, int threshold) {
        if (threshold == MANUAL_THRESHOLD) { // Manual threshold - display Threshold Adjuster to user
            IJ.selectWindow(imp.getTitle());
            ThresholdAdjuster ta = new ThresholdAdjuster();
            ta.positionWindow(ThresholdAdjuster.TOP_RIGHT); // added by Dmitry Turbin 2011-07-31
            ta.run();
        } else if (threshold == AUTO_THRESHOLD) { // Auto threshold - just perform ImageJ's Thresholder
            ThresholdAdjuster ta = new ThresholdAdjuster(imp);
            imp.getProcessor().setThreshold(ta.getMinAutoThreshold(), ta.getMaxAutoThreshold(), ImageProcessor.RED_LUT);
            imp.updateAndDraw();
        }
    }

    /**
     * Save the analysis results to hard disk
     *
     * @param   headers A tab-delimited string containing the header of the columns
     * @param   values  A tab-delimited string containing the values of the columns
     */        
    private void saveResults(String headers, String values) {
        try {
            // Create results directory if not existed yet
            File f = new File(resultsDir);
            if (!f.exists()) {f.mkdir();}
            f = new File(resultsFile);
            boolean append = f.exists();
            FileWriter fw = new FileWriter(resultsFile, append); // append if file already existed
            if (append) {fw.write(values);} else {fw.write(headers + values);}
            fw.close();
        } catch(IOException e) {
            IJ.showMessage("Error saving results to '" + resultsFile + "'");
        }     
    }

    /**
     * Generates the help message to be displayed upon clicking the "Help" button
     *
     * @return  The created JPanel object which contains the help message  
     */         
    private JPanel createHelpMsg () {
        JPanel helpPanel = new JPanel(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        
        //Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        //helpPanel.setBorder(padding);
        
        String msg = "";
        int textAreaRows = 25;
        int textAreaColumns = 50;
        
        // General tab
        JPanel generalPanel = new JPanel();
        msg = "----------------------------------" + "\r\n" +
                "File settings:" + "\r\n" +
                "----------------------------------" + "\r\n" +
                "Image(s) selection:" + "\r\n" +
                "\tCurrent image: Analyze the current active image opened in ImageJ." + "\r\n" +
                "\tImage(s) from hard disk: Analyze one or more image(s) from the hard disk." + "\r\n" +
                "Analysis results:" + "\r\n" +
                "\tDisplay: Both the analysis results and images will be displayed upon completion." + "\r\n" +
                "\tSave to hard disk: User will be prompt a directory to store the analysis results and images." + "\r\n" + "\r\n" +
                "----------------------------------" + "\r\n" +
                "Particle settings:" + "\r\n" +
                "----------------------------------" + "\r\n" +
                "Analyze: " + "\r\n" +
                "\tWhole image/ROI: The texture of entire image (or ROI if specified) will be analyzed. ROI can be of regular or irregular shapes." + "\r\n" +
                "\tIndividual particles: ImageJ's Particle Analyzer will be used to obtain the individual particles " +
                "within the image, after which the texture of each particle will be analyzed." + "\r\n" +
                "Threshold: (Only applicable if analyzing individual particles)" + "\r\n" +
                "\tAuto: Automatic thresholding by ImageJ's Thresholder will be used." + "\r\n" +
                "\tManual: ImageJ's Threshold Adjuster will be shown to user and allows adjustment of thresholds manually. " +
                "Please use the 'Set' button (NOT 'Apply' button) to preserve gray level intensities required by texture analysis." + "\r\n" +
                "Particle min size: (Only applicable if analyzing individual particles)" + "\r\n" + 
                "\tThe minium particle size as usually specified when using ImageJ's Particle Analyzer." + "\r\n" +
                "Particle max size: (Only applicable if analyzing individual particles)" + "\r\n" +
                "\tThe maximum particle size as usually specified when using ImageJ's Particle Analyzer." + "\r\n" + "\r\n" +
                "----------------------------------" + "\r\n" +
                "Texture analysis settings:" + "\r\n" +
                "----------------------------------" + "\r\n" +
                "Degree: The direction to traverse when constructing the gray level matrix." + "\r\n" +
                "\t0: The 2nd pixel is east of the 1st pixel." + "\r\n" +
                "\t45: The 2nd pixel is northeast of the 1st pixel." + "\r\n" +
                "\t90: The 2nd pixel is north of the 1st pixel." + "\r\n" +
                "\t135: The 2nd pixel is northwest of the 1st pixel." + "\r\n" +
                "\tAverage: All the above 4 directions will be used and the results will be averaged." + "\r\n" +
                "Step Size:" + "\r\n" +
                "\tThe distance between the 1st and 2nd pixel." + "\r\n" +
                "Gray Levels:" + "\r\n" + 
                "\tThe number of gray levels used in the gray level matrix based on fixed normalization. For example, " + 
                "with raw gray level intensities of 0 to 255 and number of gray levels set to 8, there will be 32 intensities per gray level, " + 
                "such that intensities of 0 to 31 will be set to gray level 0, intensities 32 to 63 will be set to gray level 1 and so on." + "\r\n" +
                "Precision:" + "\r\n" +
                "\tDesignates the number of decimal places in the analysis results.";
        
        JTextArea generalText = new JTextArea(msg, textAreaRows, textAreaColumns);
        generalText.setTabSize(2);
        generalText.setLineWrap(true);
        generalText.setWrapStyleWord(true);
        JScrollPane generalTextScrollPane = new JScrollPane(generalText);     
        generalPanel.add(generalTextScrollPane);
        tabbedPane.addTab("General", generalPanel);     
        
        // Features tab
        JPanel featuresPanel = new JPanel();
        msg = "----------------------------------" + "\r\n" +
                "Description:" + "\r\n" +
                "----------------------------------" + "\r\n" +
                "This plugin computes 22 parameters:" + "\r\n" +
                "\tThe first 21 of these are features described by Haralick (1), with the exception of 'Correlation' which is calculated according to the formulae in (2)." + "\r\n" +
                "\tThe last one (GLCM Sum) is simply a checksum to ensure the gray level matrix has normalized probabilities added up to 1." + "\r\n" + "\r\n" +
                "By default, 'Maximal Correlation Coefficient' is not calculated since the calculation of eigenvalues of the Q matrix is computational intensive." + "\r\n" + "\r\n" +
                "----------------------------------" + "\r\n" +
                "References:" + "\r\n" +
                "----------------------------------" + "\r\n" +
                "(1) N.J. Pressman, Markovian analysis of cervical cell images, J. Histochem. Cytochem. 24 (1976), 138�144." + "\r\n" +
                "(2) Chan HP, Sahiner B, Petrick N, Helvie MA, Lam, KL, Adler DD and Goodsitt MM, Computerized Classification of Malignant and Benign Microcalcifications on Mammograms: " + 
                "Texture Analysis using an Artificial Neural Network, Phys Med Biol, 42:549-567, 1997.";
    
        JTextArea featuresText = new JTextArea(msg, textAreaRows, textAreaColumns);
        featuresText.setTabSize(2);
        JScrollPane featuresTextScrollPane = new JScrollPane(featuresText);     
        featuresPanel.add(featuresTextScrollPane);        
        tabbedPane.addTab("Features", featuresPanel); 
        
        // About tab
        JPanel aboutPanel = new JPanel();
        msg = "----------------------------------" + "\r\n" +
                "Haralick Texture:" + "\r\n" +
                "----------------------------------" + "\r\n" +
                "Author:" + "\r\n" +
                "\tAndy Chan, Genetic Pathology Evaluation Centre, University of British Columbia" + "\r\n" +
                "\tDmitry Turbin, MD/PhD, Genetic Pathology Evaluation Centre, University of British Columbia" + "\r\n" +
                "Version:" + "\r\n" +
                "\t1.00" + "\r\n" +
                "Release Date:" + "\r\n" +
                "\tAugust 19, 2005" + "\r\n" +
                "Contact/Info:" + "\r\n" +
                "\tPlease refer to http://www.gpec.ubc.ca";
        
        JTextArea aboutText = new JTextArea(msg, textAreaRows, textAreaColumns);
        aboutText.setTabSize(2);
        JScrollPane aboutTextScrollPane = new JScrollPane(aboutText);     
        aboutPanel.add(aboutTextScrollPane);        
        tabbedPane.addTab("About", aboutPanel);         
        
        helpPanel.add(tabbedPane, BorderLayout.CENTER);
        
        return helpPanel;
    }
}

