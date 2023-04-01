package ca.ubc.gpec.ia.analyzer.settings;

import java.util.Iterator;

/**
 * stain analyser setting
 *
 * @author samuelc
 *
 */
public interface StainAnalyzerSettings {

    // available thresholds ...
    public static final String THRESHOLD_CHOICE_AUTO_THRESHOLD = "Auto threshold";
    public static final String THRESHOLD_CHOICE_MAXIMUM_ENTROPY_THRESHOLD = "Maximum entropy threshold";
    public static final String THRESHOLD_CHOICE_K_MEANS_CLUSTERING_THRESHOLD = "K means clustering threshold";
    public static final String THRESHOLD_CHOICE_MANUALLY_ADJUST_THRESHOLD = "Manually adjust threshold";
    public static final String THRESHOLD_CHOICE_MANUALLY_ADJUST_AND_RECORD_THRESHOLD = "Manually adjust & record threshold";
    public static final String THRESHOLD_CHOICE_AUTO_SET_THRESHOLD = "Auto threshold with 'Set'";
    public static final String THRESHOLD_CHOICE_SET_ENTIRE_THRESHOLD = "Threshold with 'Set' to cover entire spectrum of 1-255";
    public static final String[] THRESHOLD_CHOICES = {
        THRESHOLD_CHOICE_AUTO_THRESHOLD,
        THRESHOLD_CHOICE_MAXIMUM_ENTROPY_THRESHOLD,
        THRESHOLD_CHOICE_K_MEANS_CLUSTERING_THRESHOLD,
        THRESHOLD_CHOICE_MANUALLY_ADJUST_THRESHOLD,
        THRESHOLD_CHOICE_MANUALLY_ADJUST_AND_RECORD_THRESHOLD,
        THRESHOLD_CHOICE_AUTO_SET_THRESHOLD,
        THRESHOLD_CHOICE_SET_ENTIRE_THRESHOLD
    };
    // setting files entries (i.e. keys)
    public static final String SETTING_KEY_COEFFICIENT_CONTRAST = "coefficientContrast";
    public static final String SETTING_KEY_COEFFICIENT_DIFFERENCE_VARIANCE = "coefficientDifference_Variance";
    public static final String SETTING_KEY_CALCULATE_ENTROPY = "calculateEntropy";
    public static final String SETTING_KEY_UNIT_OF_LENGTH = "unitOfLength";
    public static final String SETTING_KEY_LOG_PROGRESS = "logProgress";
    public static final String SETTING_KEY_CALCULATE_TRIANGLE_SYMMETRY = "calculateTriangular_Symmetry";
    public static final String SETTING_KEY_OUTPUT_MEDIAN_INTENSITY_BROWN = "outputMedian_Intensity_Brown";
    public static final String SETTING_KEY_CALCULATE_GLCM_SUM = "calculateGLCM_Sum";
    public static final String SETTING_KEY_MAX_CIRCULARITY = "maxCircularity";
    public static final String SETTING_KEY_OUTPUT_TIME_MS = "outputTime_ms";
    public static final String SETTING_KEY_CALCULATE_ANGULAR_SECOND_MOMENT = "calculateAngular_Second_Moment";
    public static final String SETTING_KEY_COEFFICIENT_COEFFICIENT_OF_VARIANCE = "coefficientCoefficient_Of_Variation";
    public static final String SETTING_KEY_MAX_FERET = "maxFeret";
    public static final String SETTING_KEY_COEFFICIENT_INFORMATION_MEASURE_B = "coefficientInformation_Measure_B";
    public static final String SETTING_KEY_COEFFICIENT_INFORMATION_MEASURE_A = "coefficientInformation_Measure_A";
    public static final String SETTING_KEY_OUTPUT_MEAN_INTENSITY_BROWN = "outputMean_Intensity_Brown";
    public static final String SETTING_KEY_CALCULATE_CORRELATION = "calculateCorrelation";
    public static final String SETTING_KEY_MIN_FERET = "minFeret";
    public static final String SETTING_KEY_STEP_SIZE = "stepSize";
    public static final String SETTING_KEY_OUTPUT_MEAN_INTENSITY_ALL = "outputMean_Intensity_All";
    public static final String SETTING_KEY_GRAY_LEVELS = "grayLevels";
    public static final String SETTING_KEY_CALCULATE_SUM_AVERAGE = "calculateSum_Average";
    public static final String SETTING_KEY_BROWN_CORRECTION = "brownCorrection";
    public static final String SETTING_KEY_OUTPUT_TOTAL_NUCLEI_AREA = "outputTotal_Nuclei_Area";
    public static final String SETTING_KEY_CALCULATE_SUM_VARIANCE = "calculateSum_Variance";
    public static final String SETTING_KEY_MAX_AREA = "maxArea";
    public static final String SETTING_KEY_CALCULATE_INVERSE_DIFFERENCE_MOMENT = "calculateInverse_Difference_Moment";
    public static final String SETTING_KEY_OUTPUT_MEDIAN_INTENSITY_ALL = "outputMedian_Intensity_All";
    public static final String SETTING_KEY_OUTPUT_MEAN_INTENSITY_BLUE = "outputMean_Intensity_Blue";
    public static final String SETTING_KEY_OUTPUT_MEAN_OPTICAL_DENSITY_ALL = "outputMean_Optical_Density_All";
    public static final String SETTING_KEY_OUTPUT_PERCENT_BLUE_AREA = "outputPercent_Blue_Area";
    public static final String SETTING_KEY_MIN_ELLIPSE_RATIO = "minEllipseRatio";
    public static final String SETTING_KEY_CALCULATE_INFORMATION_MEASURE_B = "calculateInformation_Measure_B";
    public static final String SETTING_KEY_CALCULATE_DIAGONAL_VARIANCE = "calculateDiagonal_Variance";
    public static final String SETTING_KEY_CALCULATE_INFORMATION_MEASURE_A = "calculateInformation_Measure_A";
    public static final String SETTING_KEY_CALCULATE_DIAGONAL_MOMENT = "calculateDiagonal_Moment";
    public static final String SETTING_KEY_COEFFICIENT_ENTROPY = "coefficientEntropy";
    public static final String SETTING_KEY_MIN_BACKGROUND_THRESHOLD = "minBackgroundThreshold";
    public static final String SETTING_KEY_OUTPUT_MEAN_AREA_BLUE = "outputMean_Area_Blue";
    public static final String SETTING_KEY_MIN_AREA = "minArea";
    public static final String SETTING_KEY_CALCULATE_SECOND_DIAGONAL_MOMENT = "calculateSecond_Diagonal_Moment";
    public static final String SETTING_KEY_CALCULATE_COEFFICIENT_OF_VARIANCE = "calculateCoefficient_Of_Variation";
    public static final String SETTING_KEY_PRECISION = "precision";
    public static final String SETTING_KEY_COEFFICIENT_INVASIVE_DIFFERENCE_MOMENT = "coefficientInverse_Difference_Moment";
    public static final String SETTING_KEY_CALCULATE_MAXIMAL_CORRELATION_COEFFICIENT = "calculateMaximal_Correlation_Coefficient";
    public static final String SETTING_KEY_COEFFICIENT_SUM_VARIANCE = "coefficientSum_Variance";
    public static final String SETTING_KEY_OUTPUT_MEAN_OPTICAL_DENSITY_BLUE = "outputMean_Optical_Density_Blue";
    public static final String SETTING_KEY_OUTPUT_BLUE_NUCLEI_NUMBER = "outputBlue_Nuclei_Number";
    public static final String SETTING_KEY_KNOWN_DISTANCE = "knownDistance";
    public static final String SETTING_KEY_CALCUALTE_SUM_ENTROPY = "calculateSum_Entropy";
    public static final String SETTING_KEY_DISCRIMINANT_CONSTANT = "discriminantConstant";
    public static final String SETTING_KEY_OUTPUT_TOTAL_NUCLEI_NUMBER = "outputTotal_Nuclei_Number";
    public static final String SETTING_KEY_THRESHOLD_OPTION = "thresholdOption";
    public static final String SETTING_KEY_OUTPUT_PERCENT_BROWN_AREA = "outputPercent_Brown_Area";
    public static final String SETTING_KEY_CALCULATE_DIFFERENCE_ENTROPY = "calculateDifference_Entropy";
    public static final String SETTING_KEY_CALCULATE_DIFFERENCE_VARIANCE = "calculateDifference_Variance";
    public static final String SETTING_KEY_OUTPUT_MEDIAN_AREA_BROWN = "outputMedian_Area_Brown";
    public static final String SETTING_KEY_OUTPUT_MEDIAN_AREA_ALL = "outputMedian_Area_All";
    public static final String SETTING_KEY_OUTPUT_MEDIAN_AREA_BLUE = "outputMedian_Area_Blue";
    public static final String SETTING_KEY_OUTPUT_MEAN_AREA_ALL = "outputMean_Area_All";
    public static final String SETTING_KEY_CALCULATE_DIFFERENCE_MOMENT = "calculateDifference_Moment";
    public static final String SETTING_KEY_MIN_THRESHOLD = "minThreshold";
    public static final String SETTING_KEY_COEFFICIENT_DIFFERENCE_ENTROPY = "coefficientDifference_Entropy";
    public static final String SETTING_KEY_OUTPUT_BLUE_NUCLEI_AREA = "outputBlue_Nuclei_Area";
    public static final String SETTING_KEY_CALCULATE_CONTRAST = "calculateContrast";
    public static final String SETTING_KEY_OUTPUT_MEAN_OPTICAL_DENSITY_BROWN = "outputMean_Optical_Density_Brown";
    public static final String SETTING_KEY_CALCULATE_PEAK_TRANSITION_PROBABILITY = "calculatePeak_Transition_Probability";
    public static final String SETTING_KEY_OUTPUT_MEDIAN_OPTICAL_DENSITY_BROWN = "outputMedian_Optical_Density_Brown";
    public static final String SETTING_KEY_MAX_THRESHOLD = "maxThreshold";
    public static final String SETTING_KEY_OUTPUT_MEDIAN_OPTICAL_DENSITY_ALL = "outputMedian_Optical_Density_All";
    public static final String SETTING_KEY_STAIN = "stain";
    public static final String SETTING_KEY_BLUE_CORRECTION = "blueCorrection";
    public static final String SETTING_KEY_OUTPUT_BROWN_NUCLEI_NUMBER = "outputBrown_Nuclei_Number";
    public static final String SETTING_KEY_OUTPUT_MEDIAN_OPTICAL_DENSITY_BLUE = "outputMedian_Optical_Density_Blue";
    public static final String SETTING_KEY_COEFFICIENT_CORRELATION = "coefficientCorrelation";
    public static final String SETTING_KEY_DEGREE = "degree";
    public static final String SETTING_KEY_COEFFICIENT_ANGULAR_SECOND_MOMENT = "coefficientAngular_Second_Moment";
    public static final String SETTING_KEY_MIN_TEXTURE_SCORE = "minTextureScore";
    public static final String SETTING_KEY_MAX_TEXTURE_SCORE = "maxTextureScore";
    public static final String SETTING_KEY_TEXTURE_OBJECTS_MAX_AREA = "textureObjectsMaxArea";
    public static final String SETTING_KEY_TEXTURE_OBJECTS_MIN_AREA = "textureObjectsMinArea";
    public static final String SETTING_KEY_TEXTURE_OBJECTS_MIN_CIRCULARITY = "textureObjectsMinCircularity";
    public static final String SETTING_KEY_TEXTURE_OBJECTS_MAX_CIRCULARITY = "textureObjectsMaxCircularity";
    public static final String SETTING_KEY_TEXTURE_OBJECTS_MIN_ELLIPSE_RATIO = "textureObjectsMinEllipseRatio";
    public static final String SETTING_KEY_TEXTURE_OBJECTS_MAX_ELLIPSE_RATIO = "textureObjectsMaxEllipseRatio";
    public static final String SETTING_KEY_TEXTURE_OBJECTS_MIN_FERET = "textureObjectsMinFeret";
    public static final String SETTING_KEY_TEXTURE_OBJECTS_MAX_FERET = "textureObjectsMaxFeret";
    public static final String SETTING_KEY_TEXTURE_OBJECTS_MIN_ROUNDNESS = "textureObjectsMinRoundness";
    public static final String SETTING_KEY_TEXTURE_OBJECTS_MAX_ROUNDNESS = "textureObjectsMaxRoundness";
    public static final String SETTING_KEY_COEFFICIENT_SUM_AVERAGE = "coefficientSum_Average";
    public static final String SETTING_KEY_MIN_ROUNDNESS = "minRoundness";
    public static final String SETTING_KEY_COEFFICIENT_DIAGONAL_MOMENT = "coefficientDiagonal_Moment";
    public static final String SETTING_KEY_CALCULATE_PRODUCT_MOMENT = "calculateProduct_Moment";
    public static final String SETTING_KEY_OUTPUT_PERCENT_BLUE_NUMBER = "outputPercent_Blue_Number";
    public static final String SETTING_KEY_OUTPUT_MEAN_AREA_BROWN = "outputMean_Area_Brown";
    public static final String SETTING_KEY_MAX_ROUNDNESS = "maxRoundness";
    public static final String SETTING_KEY_DISTANCE_IN_PIXELS = "distanceInPixels";
    public static final String SETTING_KEY_MIN_CIRCULARITY = "minCircularity";
    public static final String SETTING_KEY_OUTPUT_BROWN_NUCLEI_AREA = "outputBrown_Nuclei_Area";
    public static final String SETTING_KEY_MAX_ELLIPSE_RATIO = "maxEllipseRatio";
    public static final String SETTING_KEY_COEFFICIENT_PEAK_TRANSITION_PROBABILITY = "coefficientPeak_Transition_Probability";
    public static final String SETTING_KEY_COEFFICIENT_SUM_ENTROPY = "coefficientSum_Entropy";
    public static final String SETTING_KEY_OUTPUT_MEDIAN_INTENSITY_BLUE = "outputMedian_Intensity_Blue";
    public static final String SETTING_KEY_OUTPUT_PERCENT_BROWN_NUMBER = "outputPercent_Brown_Number";
    public static final String SETTING_KEY_NEGATIVE_SELECT_COLOUR_R = "negativeSelectColourR";
    public static final String SETTING_KEY_NEGATIVE_SELECT_COLOUR_G = "negativeSelectColourG";
    public static final String SETTING_KEY_NEGATIVE_SELECT_COLOUR_B = "negativeSelectColourB";
    public static final String SETTING_KEY_POSITIVE_SELECT_COLOUR_R = "positiveSelectColourR";
    public static final String SETTING_KEY_POSITIVE_SELECT_COLOUR_G = "positiveSelectColourG";
    public static final String SETTING_KEY_POSITIVE_SELECT_COLOUR_B = "positiveSelectColourB";
    public static final String SETTING_KEY_COLOUR_DECONVOLUTION_MOD1_X = "colourDeconvolutionMOD1_X";
    public static final String SETTING_KEY_COLOUR_DECONVOLUTION_MOD1_Y = "colourDeconvolutionMOD1_Y";
    public static final String SETTING_KEY_COLOUR_DECONVOLUTION_MOD1_Z = "colourDeconvolutionMOD1_Z";
    public static final String SETTING_KEY_COLOUR_DECONVOLUTION_MOD2_X = "colourDeconvolutionMOD2_X";
    public static final String SETTING_KEY_COLOUR_DECONVOLUTION_MOD2_Y = "colourDeconvolutionMOD2_Y";
    public static final String SETTING_KEY_COLOUR_DECONVOLUTION_MOD2_Z = "colourDeconvolutionMOD2_Z";
    public static final String SETTING_KEY_COLOUR_DECONVOLUTION_MOD3_X = "colourDeconvolutionMOD3_X";
    public static final String SETTING_KEY_COLOUR_DECONVOLUTION_MOD3_Y = "colourDeconvolutionMOD3_Y";
    public static final String SETTING_KEY_COLOUR_DECONVOLUTION_MOD3_Z = "colourDeconvolutionMOD3_Z";
    // Aperio file specific settings
    public static final String SETTING_KEY_APERIO_ANNOTATION_SELECTION_TAG_NAME = "aperio_annotation_selection_tag_name";
    public static final String SETTING_KEY_APERIO_ANNOTATION_SELECTION_EXPORT_TYPE = "aperio_annotation_selection_export_type"; // export selection to jpeg for analysis
    // settings default values ...
    public static final int SETTING_DEFAULT_VALUE_NEGATIVE_SELECT_COLOUR_R = 0;
    public static final int SETTING_DEFAULT_VALUE_NEGATIVE_SELECT_COLOUR_G = 255;
    public static final int SETTING_DEFAULT_VALUE_NEGATIVE_SELECT_COLOUR_B = 0;
    public static final int SETTING_DEFAULT_VALUE_POSITIVE_SELECT_COLOUR_R = 255;
    public static final int SETTING_DEFAULT_VALUE_POSITIVE_SELECT_COLOUR_G = 255;
    public static final int SETTING_DEFAULT_VALUE_POSITIVE_SELECT_COLOUR_B = 0;
    // Aperio file specific default settings ...
    public static final String SETTING_DEFAULT_VALUE_APERIO_ANNOTATION_SELECTION_TAG_NAME = "GPEC_ANALYZE";
    public static final String SETTING_DEFAULT_VALUE_APERIO_ANNOTATION_SELECTION_EXPORT_TYPE = "jpeg"; // export selection to jpeg for analysis

    /**
     * see if setting has this key
     *
     * @param key
     * @return
     */
    public boolean containsSettingKey(String key);

    /**
     * return setting value, return null if not found
     *
     * @param key
     * @return
     */
    public String getSettingValue(String key);

    /**
     * return setting value in double
     *
     * @param key
     * @return
     * @throws NumberFormatException
     */
    public double getSettingValueDouble(String key) throws NumberFormatException, StainAnalyzerSettingsValueNotSetException, StainAnalyzerSettingsKeyNotFoundException;

    /**
     * put or update a value to the settings object
     *
     * @param key
     * @param value
     */
    public void updateSetting(String key, String value);

    /**
     * return summary params
     *
     * @return
     */
    public String[] getSummaryParams();

    /**
     * return summary param titles
     *
     * @return
     */
    public String[] getSummaryParamTitles();

    /**
     * return threshold choices
     *
     * @return
     */
    public String[] getThresholdChoices();

    /**
     * iterator of keys
     *
     * @return
     */
    public Iterator<String> getSettingKeys();
}
