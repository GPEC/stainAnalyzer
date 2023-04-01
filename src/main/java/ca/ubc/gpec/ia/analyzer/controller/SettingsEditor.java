/*
 * edit settings
 */
package ca.ubc.gpec.ia.analyzer.controller;

import ca.ubc.gpec.ia.analyzer.deconvolution.ColourDeconvolutionConstants;
import ca.ubc.gpec.ia.analyzer.deconvolution.ColourDeconvolutionFromROI;
import ca.ubc.gpec.ia.analyzer.processing.HaralickTexture;
import ca.ubc.gpec.ia.analyzer.settings.NuclearStainAnalyzerSettings;
import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettings;
import ca.ubc.gpec.ia.analyzer.gui.AboutPanel;
import ca.ubc.gpec.ia.analyzer.gui.ChannelsPanel;
import ca.ubc.gpec.ia.analyzer.gui.GeneralDialog;
import ca.ubc.gpec.ia.analyzer.gui.GeneralPanel;
import ca.ubc.gpec.ia.analyzer.gui.GeneralTable;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.OpenDialog;
import ij.measure.Calibration;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author samuelc
 */
public class SettingsEditor {

    public static final String APPLICATION_NAME = "Settings Editor";
    public static final String EDIT_SETTINGS_MODE_LOAD = "load";
    public static final String EDIT_SETTINGS_MODE_LOAD_AND_SAVE = "load_save";
    public static final String EDIT_SETTINGS_MODE_SAVE = "save";
    public static final String EDIT_SETTINGS_MODE_EDIT_ONLY = "edit_only";
    public static final String EDIT_SETTINGS_MODE_NEW = "new";
    protected StainAnalyzerSettings settings; // Analysis settings
    protected String[] summaryParams;
    protected String[] summaryParamTitles;
    protected boolean logProgress;
    protected GeneralPanel generalPanel;
    protected JComboBox stainSelectionComboBox;
    protected ChannelsPanel channelsPanel;
    protected GeneralTable channelsPanel_table;
    protected JLabel channelsPanel_table_title;

    // Threshold variables
    // the following values will be replaced by String constants in StainAnalyzerSettings
    //static final int AUTO_THRESHOLD = 1; // Auto Threshold
    //static final int MAX_ENTROPY_THRESHOLD = 2; // Maximum entropy threshold
    //static final int K_MEANS_CLUSTERING_THRESHOLD = 3; // K means clustering threshold
    //static final int MANUAL_THRESHOLD = 4; // Manually adjust threshold
    //static final int MANUAL_RECORD_THRESHOLD = 5; // Manually adjust threshold with results saved
    //static final int AUTO_SET_THRESHOLD = 6; // Auto threshold with "Set"
    //static final int SET_ENTIRE_THRESHOLD = 7; // Threshold with "Set" to cover entire spectrum of 1 - 255
    public void run(String arg) {

        String CHOICE_CREATE_NEW_SETTINGS = "Create new settings";
        String CHOICE_EDIT_EXISTING_SETTINGS = "Edit existing settings";
        String CHOICE_GENERATE_CHANNEL_MATRIX_FROM_ROI = "Generate channel matrix from ROI";
        String choices[] = {
            CHOICE_CREATE_NEW_SETTINGS,
            CHOICE_EDIT_EXISTING_SETTINGS,
            CHOICE_GENERATE_CHANNEL_MATRIX_FROM_ROI};

        GenericDialog gd = new GenericDialog(APPLICATION_NAME);
        gd.addChoice("Please choose one of the following options:", choices, CHOICE_CREATE_NEW_SETTINGS);
        gd.showDialog();
        if (gd.wasCanceled()) {	// Exit the plugin
            return;
        }

        String choice = gd.getNextChoice();

        if (choice.equals(CHOICE_CREATE_NEW_SETTINGS)) {
            doSettings(EDIT_SETTINGS_MODE_SAVE);
        } else if (choice.equals(CHOICE_EDIT_EXISTING_SETTINGS)) {
            doSettings(EDIT_SETTINGS_MODE_LOAD_AND_SAVE);
        } else if (choice.equals(CHOICE_GENERATE_CHANNEL_MATRIX_FROM_ROI)) {
            generateChannelMatrixFromROI();
        } else {
            // something must be wrong ... this is not possible
        }

    }

    /**
     * default constructor
     */
    public SettingsEditor() {
        // initialise HashMap
        this.settings = new NuclearStainAnalyzerSettings();
        summaryParams = settings.getSummaryParams();
        summaryParamTitles = settings.getSummaryParamTitles();
        logProgress = true; // initial value
        stainSelectionComboBox = null; // defined in createGeneralPanel
        channelsPanel = null; // defined in doSettings(String mode)
        generalPanel = null; // defined in doSettings(String mode)
    }

    /**
     * get the StainAnalyzerSettings object
     *
     * @return
     */
    public StainAnalyzerSettings getSettings() {
        return settings;
    }

    /**
     * set StainAnalyzerSettings object
     * @param settings 
     */
    public void setSettings(StainAnalyzerSettings settings) {
        this.settings = settings;
    }
    /**
     * return logProgress
     *
     * @return
     */
    public boolean isLogProgress() {
        return logProgress;
    }

    /**
     * generate channel matrix from ROI the results will be store in
     * ColourDeconvolutionConstants singlaton instance
     */
    public void generateChannelMatrixFromROI() {
        ColourDeconvolutionFromROI cdfroi = new ColourDeconvolutionFromROI(IJ.getInstance());
        ColourDeconvolutionConstants.getInstance().setChannelMatrixFromROI(cdfroi.getChannelMatrix());
    }

    public void doSettings(String mode) {

        HaralickTexture ht = new HaralickTexture();
        int[] features = ht.getFeatures();
        String[] featureTitles = ht.getFeatureTitles();

        boolean loadSuccess = false;

        // aperio file specific settings ... use default for now
        // TODO: need to put into settings editor
        settings.updateSetting(
                StainAnalyzerSettings.SETTING_KEY_APERIO_ANNOTATION_SELECTION_TAG_NAME, 
                StainAnalyzerSettings.SETTING_DEFAULT_VALUE_APERIO_ANNOTATION_SELECTION_TAG_NAME);
        settings.updateSetting(
                StainAnalyzerSettings.SETTING_KEY_APERIO_ANNOTATION_SELECTION_EXPORT_TYPE,
                StainAnalyzerSettings.SETTING_DEFAULT_VALUE_APERIO_ANNOTATION_SELECTION_EXPORT_TYPE);
        
        if (mode.equals(EDIT_SETTINGS_MODE_LOAD) | mode.equals(EDIT_SETTINGS_MODE_LOAD_AND_SAVE)) {
            // Prompt for settings file
            OpenDialog od = new OpenDialog(
                    "Please specify the settings file", "", "Settings.txt");
            String settingsDir = od.getDirectory();
            String settingsFile = od.getFileName();
            OpenDialog.setDefaultDirectory(od.getDirectory()); // remember
            // this
            // directory
            // Load settings
            try {
                FileReader fr = new FileReader(settingsDir + settingsFile);
                BufferedReader bIn = new BufferedReader(fr);

                String line;
                while ((line = bIn.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line);
                    String key = st.nextToken("\t");
                    String value = st.nextToken("\t");
                    settings.updateSetting(key, value);
                }

                // load channels settings to ColourDeconvolutionConstants object
                ColourDeconvolutionConstants.getInstance().loadUserSettings(settings);

                // check to see if SETTING_KEY_POSITIVE_SELECT_COLOUR_R/G/B are set
                if (settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_R).trim().length() == 0) {
                    // SETTING_KEY_POSITIVE_SELECT_COLOUR_R/G/B - for old settings file ...
                    // assume SETTING_KEY_NEGATIVE_SELECT_COLOUR_R/G/B not set as well ...
                    // set default
                    settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_R, "" + StainAnalyzerSettings.SETTING_DEFAULT_VALUE_POSITIVE_SELECT_COLOUR_R); // settings.updateSetting("positiveR", 255); 
                    settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_G, "" + StainAnalyzerSettings.SETTING_DEFAULT_VALUE_POSITIVE_SELECT_COLOUR_G); // settings.updateSetting("positiveG", 255); 
                    settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_B, "" + StainAnalyzerSettings.SETTING_DEFAULT_VALUE_POSITIVE_SELECT_COLOUR_B); // settings.updateSetting("positiveB", 0);
                    settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_R, "" + StainAnalyzerSettings.SETTING_DEFAULT_VALUE_NEGATIVE_SELECT_COLOUR_R); // settings.updateSetting("negativeR", 0);
                    settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_G, "" + StainAnalyzerSettings.SETTING_DEFAULT_VALUE_NEGATIVE_SELECT_COLOUR_G); // settings.updateSetting("negativeG", 255); 
                    settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_B, "" + StainAnalyzerSettings.SETTING_DEFAULT_VALUE_NEGATIVE_SELECT_COLOUR_B); // settings.updateSetting("negativeB", 0);
                }

                loadSuccess = true;
            } catch (IOException e) {
                IJ.showMessage("File '" + settingsDir + settingsFile
                        + "' not found.");
                loadSuccess = false;
            } catch (Exception e) {
                IJ.showMessage("File '" + settingsDir + settingsFile
                        + "' Is Not Valid.");
                loadSuccess = false;
            }
        } else if (mode.equals(EDIT_SETTINGS_MODE_SAVE) | mode.equals(EDIT_SETTINGS_MODE_NEW)) { // Initialise settings with default
            // values
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_STAIN, "H DAB"); // "stain"
            // since default stain is H_DAB, there is no need to set user-define values
            // for colour deconvolution

            // Minimum particle area in unit calibrated by user
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_AREA, "30"); // "minArea"
            // Maximum particle area in unit calibrated by user
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_AREA, "300"); // "maxArea"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_CIRCULARITY, "0.30"); //"minCircularity"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_CIRCULARITY, "1"); //"maxCircularity"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_ELLIPSE_RATIO, "1"); //"minEllipseRatio"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_ELLIPSE_RATIO, "4"); //"maxEllipseRatio"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_FERET, "8"); //"minFeret"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_FERET, "50"); //"maxFeret"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_ROUNDNESS, "0.30"); //"minRoundness"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_ROUNDNESS, "1"); //"maxRoundness"

            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_TEXTURE_SCORE, "-0.05"); //"minTextureScore"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_TEXTURE_SCORE, "20"); //"maxTextureScore"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MIN_AREA, ""); //"textureObjectsMinArea"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MAX_AREA, ""); //"textureObjectsMaxArea"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MIN_CIRCULARITY, ""); //"textureObjectsMinCircularity"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MAX_CIRCULARITY, ""); //"textureObjectsMaxCircularity"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MIN_ELLIPSE_RATIO, ""); //"textureObjectsMinEllipseRatio"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MAX_ELLIPSE_RATIO, ""); //"textureObjectsMaxEllipseRatio"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MIN_FERET, ""); //"textureObjectsMinFeret"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MAX_FERET, ""); //"textureObjectsMaxFeret"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MIN_ROUNDNESS, ""); //"textureObjectsMinRoundness"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MAX_ROUNDNESS, ""); //"textureObjectsMaxRoundness"

            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_THRESHOLD, "0.8"); //"minThreshold"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_THRESHOLD, "1.0"); //"maxThreshold"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_THRESHOLD_OPTION, StainAnalyzerSettings.THRESHOLD_CHOICE_AUTO_THRESHOLD); //"thresholdOption"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_BLUE_CORRECTION, "0.80"); //"blueCorrection"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_BROWN_CORRECTION, "1"); //_"brownCorrection"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_BACKGROUND_THRESHOLD, "240"); //"minBackgroundThreshold"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_DISTANCE_IN_PIXELS, "2142"); //"distanceInPixels"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_KNOWN_DISTANCE, "900"); //"knownDistance"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_UNIT_OF_LENGTH, "um"); //"unitOfLength"
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_LOG_PROGRESS, "false"); //"logProgress"

            // Colour selection settings
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_R, "" + StainAnalyzerSettings.SETTING_DEFAULT_VALUE_POSITIVE_SELECT_COLOUR_R); // settings.updateSetting("positiveR", 255); 
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_G, "" + StainAnalyzerSettings.SETTING_DEFAULT_VALUE_POSITIVE_SELECT_COLOUR_G); // settings.updateSetting("positiveG", 255); 
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_B, "" + StainAnalyzerSettings.SETTING_DEFAULT_VALUE_POSITIVE_SELECT_COLOUR_B); // settings.updateSetting("positiveB", 0);
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_R, "" + StainAnalyzerSettings.SETTING_DEFAULT_VALUE_NEGATIVE_SELECT_COLOUR_R); // settings.updateSetting("negativeR", 0);
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_G, "" + StainAnalyzerSettings.SETTING_DEFAULT_VALUE_NEGATIVE_SELECT_COLOUR_G); // settings.updateSetting("negativeG", 255); 
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_B, "" + StainAnalyzerSettings.SETTING_DEFAULT_VALUE_NEGATIVE_SELECT_COLOUR_B); // settings.updateSetting("negativeB", 0);

            // Texture settings
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_DEGREE, "Average"); // settings.updateSetting("degree", "Average");
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_STEP_SIZE, "1"); // settings.updateSetting("stepSize", new Integer(1));
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_GRAY_LEVELS, "16"); // settings.updateSetting("grayLevels", new Integer(16));
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_PRECISION, "3"); // settings.updateSetting("precision", new Integer(3));
            HashMap<Integer, Float> discriminantParameters = setupDiscriminantParameters();
            settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_DISCRIMINANT_CONSTANT,
                    discriminantParameters.get(HaralickTexture.DISCRIMINANT_CONSTANT).toString());
            for (int i = 0; i < features.length; i++) {
                int feature = features[i];
                String featureTitle = featureTitles[i];

                boolean checked = true;
                if (feature == HaralickTexture.MAXIMAL_CORRELATION_COEFFICIENT) {
                    checked = false;
                }
                settings.updateSetting("calculate" + featureTitle, (new Boolean(checked)).toString());
                if (discriminantParameters.containsKey(new Integer(feature))) { // coefficient
                    settings.updateSetting("coefficient" + featureTitle,
                            discriminantParameters.get(new Integer(feature)).toString());
                }
            }

            // Output settings
            for (int i = 0; i < summaryParams.length; i++) {
                settings.updateSetting("output" + summaryParams[i], (new Boolean(true)).toString());
            }
        }

        System.out.println("trying to display analyser settings ...");
        // Display analyser settings to user
        GeneralDialog gd = new GeneralDialog("Analyser settings");

        // Create the individual tab panels
        generalPanel = new GeneralPanel(gd, settings);
        gd.addTab("General", generalPanel);

        channelsPanel = new ChannelsPanel(settings);
        gd.addTab("Channels", channelsPanel);

        JPanel filtersPanel = createFiltersPanel(gd);
        gd.addTab("Filters", filtersPanel);

        JPanel texturePanel = createTexturePanel(gd);
        gd.addTab("Texture", texturePanel);

        JPanel outputPanel = createOutputPanel(gd);
        gd.addTab("Output", outputPanel);

        AboutPanel aboutPanel = new AboutPanel();
        gd.addTab("About", aboutPanel);
        gd.showDialog();

        if (gd.wasCanceled()) {
            return;
        }

        // Obtain setting values
        final int MIN_COL = 1, MAX_COL = 2;
        final int AREA = 0, CIRCULARITY = 1, ELLIPSE_RATIO = 2, FERET = 3, ROUNDNESS = 4;

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_STAIN, channelsPanel.getStainSelectionComboBox().getSelectedItem().toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD1_X, channelsPanel.getChannel1_modx().getText());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD1_Y, channelsPanel.getChannel1_mody().getText());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD1_Z, channelsPanel.getChannel1_modz().getText());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD2_X, channelsPanel.getChannel2_modx().getText());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD2_Y, channelsPanel.getChannel2_mody().getText());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD2_Z, channelsPanel.getChannel2_modz().getText());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD3_X, channelsPanel.getChannel3_modx().getText());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD3_Y, channelsPanel.getChannel3_mody().getText());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD3_Z, channelsPanel.getChannel3_modz().getText());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_AREA, gd.getTableValue("generalFilters", AREA, MIN_COL).toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_AREA, gd.getTableValue("generalFilters", AREA, MAX_COL).toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_CIRCULARITY, gd.getTableValue("generalFilters", CIRCULARITY, MIN_COL).toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_CIRCULARITY, gd.getTableValue("generalFilters", CIRCULARITY, MAX_COL).toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_ELLIPSE_RATIO, gd.getTableValue("generalFilters", ELLIPSE_RATIO, MIN_COL).toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_ELLIPSE_RATIO, gd.getTableValue("generalFilters", ELLIPSE_RATIO, MAX_COL).toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_FERET, gd.getTableValue("generalFilters", FERET, MIN_COL).toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_FERET, gd.getTableValue("generalFilters", FERET, MAX_COL).toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_ROUNDNESS, gd.getTableValue("generalFilters", ROUNDNESS, MIN_COL).toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_ROUNDNESS, gd.getTableValue("generalFilters", ROUNDNESS, MAX_COL).toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_TEXTURE_SCORE, gd.getTableValue("textureFilters", 0, MIN_COL).toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MAX_TEXTURE_SCORE, gd.getTableValue("textureFilters", 0, MAX_COL).toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MIN_AREA, gd.getTableValue("textureObjectsFilters", AREA, MIN_COL).toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MAX_AREA, gd.getTableValue("textureObjectsFilters", AREA, MAX_COL).toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MIN_CIRCULARITY, gd.getTableValue("textureObjectsFilters", CIRCULARITY, MIN_COL).toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MAX_CIRCULARITY, gd.getTableValue("textureObjectsFilters", CIRCULARITY, MAX_COL).toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MIN_ELLIPSE_RATIO, gd.getTableValue("textureObjectsFilters", ELLIPSE_RATIO, MIN_COL).toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MAX_ELLIPSE_RATIO, gd.getTableValue("textureObjectsFilters", ELLIPSE_RATIO, MAX_COL).toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MIN_FERET, gd.getTableValue("textureObjectsFilters", FERET, MIN_COL).toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MAX_FERET, gd.getTableValue("textureObjectsFilters", FERET, MAX_COL).toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MIN_ROUNDNESS, gd.getTableValue("textureObjectsFilters", ROUNDNESS, MIN_COL).toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_TEXTURE_OBJECTS_MAX_ROUNDNESS, gd.getTableValue("textureObjectsFilters", ROUNDNESS, MAX_COL).toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_THRESHOLD_OPTION, gd.getComboBoxValue(StainAnalyzerSettings.SETTING_KEY_THRESHOLD_OPTION).toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_BLUE_CORRECTION, gd.getTextFieldValue("blueCorrection").toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_BROWN_CORRECTION, gd.getTextFieldValue("brownCorrection").toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_MIN_BACKGROUND_THRESHOLD, gd.getTextFieldValue("minBackgroundThreshold").toString());

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_DISTANCE_IN_PIXELS, gd.getTextFieldValue("distanceInPixels").toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_KNOWN_DISTANCE, gd.getTextFieldValue("knownDistance").toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_UNIT_OF_LENGTH, gd.getTextFieldValue("unitOfLength").toString());
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_LOG_PROGRESS, (new Boolean(gd.getCheckBoxValue("logProgress")).toString()));

        // Colour selection settings
        String[] positive = gd.getTextFieldValue("positiveRGB").toString().split(",");
        String[] negative = gd.getTextFieldValue("negativeRGB").toString().split(",");

        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_R, positive[0]);
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_G, positive[1]);
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_B, positive[2]);
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_R, negative[0]);
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_G, negative[1]);
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_B, negative[2]);

        // Texture features
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_DEGREE, "" + gd.getTableValue("settings", 0, 1));
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_STEP_SIZE, "" + gd.getTableValue("settings", 1, 1));
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_GRAY_LEVELS, "" + gd.getTableValue("settings", 2, 1));
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_PRECISION, "" + gd.getTableValue("settings", 3, 1));
        settings.updateSetting(StainAnalyzerSettings.SETTING_KEY_DISCRIMINANT_CONSTANT, "" + gd.getTableValue("settings", 4, 1));

        for (int i = 0; i < features.length; i++) {
            int feature = features[i];
            String featureTitle = featureTitles[i];
            settings.updateSetting("calculate" + featureTitle,
                    "" + gd.getTableValue("texture", i, 1));
            settings.updateSetting("coefficient" + featureTitle,
                    "" + gd.getTableValue("texture", i, 2));
        }

        // Output settings
        for (int i = 0; i < summaryParams.length; i++) {
            settings.updateSetting("output" + summaryParams[i],
                    "" + gd.getTableValue("output", i, 1));
        }

        if (mode.equals(EDIT_SETTINGS_MODE_LOAD)) {
            logProgress = Boolean.parseBoolean((String) settings.getSettingValue("logProgress"));

            // Set calibration
            calibrate(Double.parseDouble((String) settings.getSettingValue("distanceInPixels")),
                    Double.parseDouble((String) settings.getSettingValue("knownDistance")),
                    (String) settings.getSettingValue("unitOfLength"));
        } else if (mode.equals(EDIT_SETTINGS_MODE_SAVE) | mode.equals(EDIT_SETTINGS_MODE_LOAD_AND_SAVE)) {
            String s = "";
            for (Iterator<String> itr = settings.getSettingKeys(); itr.hasNext();) {
                String setting = itr.next();
                String value = settings.getSettingValue(setting).toString();

                // thresholdChoices has been changed from int to String - no longer need to do the following
                //if (setting.equals(StainAnalyzerSettings.SETTING_KEY_THRESHOLD_OPTION)) {
                //	if (value.equals(thresholdChoices[AUTO_THRESHOLD - 1])) {
                //		value = AUTO_THRESHOLD + "";
                //	} else if (value
                //			.equals(thresholdChoices[MAX_ENTROPY_THRESHOLD - 1])) {
                //		value = MAX_ENTROPY_THRESHOLD + "";
                // } else if
                // (value.equals(thresholdChoices[K_MEANS_CLUSTERING_THRESHOLD-1]))
                // {
                // value = K_MEANS_CLUSTERING_THRESHOLD + "";
                //	} else if (value
                //			.equals(thresholdChoices[MANUAL_THRESHOLD - 1])) {
                //		value = MANUAL_THRESHOLD + "";
                //	} else if (value
                //			.equals(thresholdChoices[MANUAL_RECORD_THRESHOLD - 1])) {
                //		value = MANUAL_RECORD_THRESHOLD + "";
                //	}
                //}

                if (value.length() > 0) {
                    s += setting + "\t" + value + "\r\n";
                }
            }

            // Ask user where to save settings
            OpenDialog od = new OpenDialog(
                    "Please specify name/location for settings file.", "",
                    "Settings.txt");
            String settingsDir = od.getDirectory();
            String settingsFile = od.getFileName();

            // Save settings
            try {
                FileWriter f = new FileWriter(settingsDir + settingsFile, false);
                f.write(s);
                f.close();
                IJ.showMessage("Settings saved.");
            } catch (IOException e) {
                IJ.showMessage("File '" + settingsDir + settingsFile
                        + "' not found.");
            }
        }
    }

    private HashMap<Integer, Float> setupDiscriminantParameters() {
        HashMap<Integer, Float> discriminantParameters = new HashMap<Integer, Float>();
        discriminantParameters.put(new Integer(HaralickTexture.DISCRIMINANT_CONSTANT), new Float(-23.1410538314311));
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

    private JPanel createFiltersPanel(GeneralDialog gd) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[] generalFilters = {"Area", "Circularity", "EllipseRatio", "Feret", "Roundness"};
        String[] generalFilterTitles = {"Area (" + settings.getSettingValue("unitOfLength") + ")", "Circularity", "Ellipse Ratio", "Feret's Diameter", "Roundness"};
        String[] columnNames = {"Filter", "Minimum", "Maximum"};
        int[] columnWidths = new int[3];
        int widthRatio = 7;

        Object[][] generalData = new Object[generalFilters.length][columnNames.length];

        for (int i = 0; i < generalFilters.length; i++) {
            int thisWidth = 0;
            // Filter column
            String generalFilter = generalFilters[i];
            String title = generalFilterTitles[i];
            generalData[i][0] = new JLabel(title);
            thisWidth = Math.max(columnNames[0].length(), title.length()) * widthRatio;
            columnWidths[0] = Math.max(columnWidths[0], thisWidth);

            String minimum = "0.0";
            String maximum = "0.0";
            if (generalFilter.equals("Area")) {
                minimum = settings.getSettingValue("minArea");
                maximum = settings.getSettingValue("maxArea");
            } else if (generalFilter.equals("Circularity")) {
                minimum = settings.getSettingValue("minCircularity");
                maximum = settings.getSettingValue("maxCircularity");
            } else if (generalFilter.equals("EllipseRatio")) {
                minimum = settings.getSettingValue("minEllipseRatio");
                maximum = settings.getSettingValue("maxEllipseRatio");
            } else if (generalFilter.equals("Feret")) {
                minimum = settings.getSettingValue("minFeret");
                maximum = settings.getSettingValue("maxFeret");
            } else if (generalFilter.equals("Roundness")) {
                minimum = settings.getSettingValue("minRoundness");
                maximum = settings.getSettingValue("maxRoundness");
            }

            // Minimum column
            generalData[i][1] = new JTextField(minimum);
            thisWidth = Math.max(columnNames[1].length(), minimum.length()) * widthRatio;
            columnWidths[1] = Math.max(columnWidths[1], thisWidth);

            // Maximum column
            generalData[i][2] = new JTextField(maximum);
            thisWidth = Math.max(columnNames[2].length(), maximum.length()) * widthRatio;
            columnWidths[2] = Math.max(columnWidths[1], thisWidth);
        }

        GeneralTable generalTable = new GeneralTable(generalData, columnNames);

        // Set column width
        for (int i = 0; i < columnWidths.length; i++) {
            int width = columnWidths[i];
            if (width == 0) {
                width = columnNames[i].length() * widthRatio;
            }
            generalTable.setColumnWidth(i, width);
        }

        // Add table to panel
        panel.add(new JLabel("General filters"));
        gd.addTableWithHeader(panel, "generalFilters", generalTable);

        String[] textureFilters = {"TextureScore"};
        String[] textureFilterTitles = {"Texture Score"};

        Object[][] textureData = new Object[textureFilters.length][columnNames.length];

        for (int i = 0; i < textureFilters.length; i++) {
            int thisWidth = 0;
            // Filter column
            String textureFilter = textureFilters[i];
            String title = textureFilterTitles[i];
            textureData[i][0] = new JLabel(title);
            thisWidth = Math.max(columnNames[0].length(), title.length()) * widthRatio;
            columnWidths[0] = Math.max(columnWidths[0], thisWidth);

            String minimum = "0.0";
            String maximum = "0.0";
            if (textureFilter.equals("TextureScore")) {
                minimum = settings.getSettingValue("minTextureScore");
                maximum = settings.getSettingValue("maxTextureScore");
            }

            // Minimum column
            textureData[i][1] = new JTextField(minimum);
            thisWidth = Math.max(columnNames[1].length(), minimum.length()) * widthRatio;
            columnWidths[1] = Math.max(columnWidths[1], thisWidth);

            // Maximum column
            textureData[i][2] = new JTextField(maximum);
            thisWidth = Math.max(columnNames[2].length(), maximum.length()) * widthRatio;
            columnWidths[2] = Math.max(columnWidths[1], thisWidth);
        }

        GeneralTable textureTable = new GeneralTable(textureData, columnNames);

        // Set column width
        for (int i = 0; i < columnWidths.length; i++) {
            int width = columnWidths[i];
            if (width == 0) {
                width = columnNames[i].length() * widthRatio;
            }
            textureTable.setColumnWidth(i, width);
        }

        // Add table to panel
        panel.add(new JLabel(" "));
        panel.add(new JLabel("Apply texture score filter"));
        gd.addTableWithHeader(panel, "textureFilters", textureTable);

        String[] textureObjectsFilters = {"Area", "Circularity", "EllipseRatio", "Feret", "Roundness"};
        String[] textureObjectsFilterTitles = {"Area (" + settings.getSettingValue("unitOfLength") + ")", "Circularity", "Ellipse Ratio", "Feret's Diameter", "Roundness"};

        Object[][] textureObjectsData = new Object[textureObjectsFilters.length][columnNames.length];

        for (int i = 0; i < textureObjectsFilters.length; i++) {
            int thisWidth = 0;
            // Filter column
            String textureObjectsFilter = textureObjectsFilters[i];
            String title = textureObjectsFilterTitles[i];
            textureObjectsData[i][0] = new JLabel(title);
            thisWidth = Math.max(columnNames[0].length(), title.length()) * widthRatio;
            columnWidths[0] = Math.max(columnWidths[0], thisWidth);

            String minimum = "0.0";
            String maximum = "0.0";
            if (textureObjectsFilter.equals("Area")) {
                minimum = settings.getSettingValue("textureObjectsMinArea");
                maximum = settings.getSettingValue("textureObjectsMaxArea");
            } else if (textureObjectsFilter.equals("Circularity")) {
                minimum = settings.getSettingValue("textureObjectsMinCircularity");
                maximum = settings.getSettingValue("textureObjectsMaxCircularity");
            } else if (textureObjectsFilter.equals("EllipseRatio")) {
                minimum = settings.getSettingValue("textureObjectsMinEllipseRatio");
                maximum = settings.getSettingValue("textureObjectsMaxEllipseRatio");
            } else if (textureObjectsFilter.equals("Feret")) {
                minimum = settings.getSettingValue("textureObjectsMinFeret");
                maximum = settings.getSettingValue("textureObjectsMaxFeret");
            } else if (textureObjectsFilter.equals("Roundness")) {
                minimum = settings.getSettingValue("textureObjectsMinRoundness");
                maximum = settings.getSettingValue("textureObjectsMaxRoundness");
            }

            // Minimum column
            textureObjectsData[i][1] = new JTextField(minimum);
            thisWidth = Math.max(columnNames[1].length(), minimum.length()) * widthRatio;
            columnWidths[1] = Math.max(columnWidths[1], thisWidth);

            // Maximum column
            textureObjectsData[i][2] = new JTextField(maximum);
            thisWidth = Math.max(columnNames[2].length(), maximum.length()) * widthRatio;
            columnWidths[2] = Math.max(columnWidths[1], thisWidth);
        }

        GeneralTable textureObjectsTable = new GeneralTable(textureObjectsData, columnNames);

        // Set column width
        for (int i = 0; i < columnWidths.length; i++) {
            int width = columnWidths[i];
            if (width == 0) {
                width = columnNames[i].length() * widthRatio;
            }
            textureObjectsTable.setColumnWidth(i, width);
        }

        // Add table to panel
        panel.add(new JLabel("on objects that have"));
        gd.addTableWithHeader(panel, "textureObjectsFilters", textureObjectsTable);

        return panel;
    }

    private JPanel createTexturePanel(GeneralDialog gd) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        int widthRatio = 7;

        String[] s = {"degree", "stepSize", "grayLevels", "precision", "discriminantConstant"};
        String[] sTitles = {"Degree", "Step Size", "Gray Levels", "Precision", "Discriminant Constant"};
        int[] sColumnWidths = new int[2];
        String[] sColumnNames = {"", ""};
        Object[][] sData = new Object[s.length][sColumnWidths.length];

        for (int i = 0; i < s.length; i++) {
            sData[i][0] = new JLabel(sTitles[i]);
            if (s[i].equals("degree")) {
                HaralickTexture ht = new HaralickTexture();
                String[] degreeTitles = ht.getDegreeTitles();

                JComboBox combo = new JComboBox();
                // Populate the combobox list
                for (int j = 0; j < degreeTitles.length; j++) {
                    combo.addItem(degreeTitles[j]);
                }
                combo.setSelectedItem(settings.getSettingValue("degree"));
                sData[i][1] = combo;
            } else {
                sData[i][1] = new JTextField(settings.getSettingValue(s[i]));
            }
        }

        GeneralTable sTable = new GeneralTable(sData, sColumnNames);
        // Set column width
        for (int i = 0; i < s.length; i++) {
            // Title column
            sColumnWidths[0] = Math.max(sColumnWidths[0], sTitles[0].length() * widthRatio);

            // Value column
            sColumnWidths[1] = Math.max(sColumnWidths[1], 8);
        }

        gd.addTable(panel, "settings", sTable);
        panel.add(new JLabel(" "));

        HaralickTexture mt = new HaralickTexture();
        int[] features = mt.getFeatures();
        String[] featureTitles = mt.getFeatureTitles();

        String[] featureColumnNames = {"Feature", "Calculate", "Coefficient"};
        int[] featureColumnWidths = new int[3];

        Object[][] featureData = new Object[features.length][featureColumnNames.length];
        HashMap<Integer, Float> discriminantParameters = setupDiscriminantParameters();

        for (int i = 0; i < features.length; i++) {
            int feature = features[i];
            int thisWidth = 0;
            // Feature column
            String title = featureTitles[i].replace("_", " ");
            featureData[i][0] = new JLabel(title);
            thisWidth = Math.max(featureColumnNames[0].length(), title.length()) * widthRatio;
            featureColumnWidths[0] = Math.max(featureColumnWidths[0], thisWidth);

            // Calculate column
            boolean calculate = false;
            if (settings.containsSettingKey("calculate" + featureTitles[i])) {
                calculate = Boolean.parseBoolean(settings.getSettingValue("calculate" + featureTitles[i]).toString());
            }
            featureData[i][1] = new JCheckBox("", calculate);

            // Coefficient column
            String coefficient = "";
            if (settings.containsSettingKey("coefficient" + featureTitles[i])) {
                coefficient = settings.getSettingValue("coefficient" + featureTitles[i]).toString();
            }
            featureData[i][2] = new JTextField(coefficient);
            thisWidth = Math.max(featureColumnNames[2].length(), coefficient.length()) * widthRatio;
            featureColumnWidths[2] = Math.max(featureColumnWidths[2], thisWidth);
        }

        GeneralTable featureTable = new GeneralTable(featureData, featureColumnNames);

        // Set column width
        for (int i = 0; i < featureColumnWidths.length; i++) {
            int width = featureColumnWidths[i];
            if (width == 0) {
                width = featureColumnNames[i].length() * widthRatio;
            }
            featureTable.setColumnWidth(i, width);
        }

        gd.addTableWithHeader(panel, "texture", featureTable);

        return panel;
    }

    // TODO. to be deleted after Settings_Editor is done
    private JPanel createOutputPanel(GeneralDialog gd) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        int widthRatio = 7;

        int[] columnWidths = new int[2];
        String[] columnNames = {"Parameter", "Output"};
        Object[][] data = new Object[summaryParams.length][columnWidths.length];

        for (int i = 0; i < summaryParams.length; i++) {
            data[i][0] = new JLabel(summaryParamTitles[i]);
            data[i][1] = new JCheckBox("", Boolean.parseBoolean(settings.getSettingValue("output" + summaryParams[i])));
        }

        GeneralTable table = new GeneralTable(data, columnNames);
        // Set column width
        for (int i = 0; i < summaryParams.length; i++) {
            // Parameters column
            columnWidths[0] = Math.max(columnWidths[0], summaryParamTitles[0].length() * widthRatio);
        }

        // Output column
        columnWidths[1] = columnNames[1].length() * widthRatio;

        gd.addTableWithHeader(panel, "output", table);

        return panel;
    }
}
