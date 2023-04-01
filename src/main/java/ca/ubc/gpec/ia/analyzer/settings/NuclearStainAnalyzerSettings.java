package ca.ubc.gpec.ia.analyzer.settings;

import java.util.HashMap;
import java.util.Iterator;

/**
 * store settings of nuclear analyser
 *
 * @author samuelc
 *
 */
public class NuclearStainAnalyzerSettings implements StainAnalyzerSettings {

    public static final String[] SUMMARY_PARAMS = {"Blue_Nuclei_Number", "Brown_Nuclei_Number", "Total_Nuclei_Number",
        "Percent_Blue_Number", "Percent_Brown_Number",
        "Blue_Nuclei_Area", "Brown_Nuclei_Area", "Total_Nuclei_Area",
        "Percent_Blue_Area", "Percent_Brown_Area",
        "Mean_Area_Blue", "Median_Area_Blue",
        "Mean_Area_Brown", "Median_Area_Brown",
        "Mean_Area_All", "Median_Area_All",
        "Mean_Intensity_Blue", "Median_Intensity_Blue",
        "Mean_Intensity_Brown", "Median_Intensity_Brown",
        "Mean_Intensity_All", "Median_Intensity_All",
        "Mean_Optical_Density_Blue", "Median_Optical_Density_Blue",
        "Mean_Optical_Density_Brown", "Median_Optical_Density_Brown",
        "Mean_Optical_Density_All", "Median_Optical_Density_All",
        "Time_ms"};
    public static final String[] SUMMARY_PARAM_TITLES = {"Neg Nuclei Number", "Pos Nuclei Number", "Total Nuclei Number",
        "Percent Negative", "Percent Positive",
        "Neg Nuclei Area", "Pos Nuclei Area", "Total Nuclei Area",
        "Percent Neg Area", "Percent Pos Area",
        "Mean Area Neg", "Median Area Neg",
        "Mean Area Pos", "Median Area Pos",
        "Mean Area All", "Median Area All",
        "Mean Intensity Neg", "Median Intensity Neg",
        "Mean Intensity Pos", "Median Intensity Pos",
        "Mean Intensity All", "Median Intensity All",
        "Mean OD Neg", "Median OD Neg",
        "Mean OD Pos", "Median OD Pos",
        "Mean OD All", "Median OD All",
        "Time (ms)"};
    protected HashMap<String, String> settings; // Analysis settings

    /**
     * default constructor
     */
    public NuclearStainAnalyzerSettings() {
        settings = new HashMap<String, String>();
    }

    /**
     * see if setting has this key
     *
     * @param key
     * @return
     */
    public boolean containsSettingKey(String key) {
        return settings.containsKey(key);
    }

    /**
     * return setting value, return "" (empty string) if not found
     *
     * @param key
     * @return
     */
    public String getSettingValue(String key) {
        String value = null;
        if (settings.containsKey(key)) {
            value = settings.get(key);
        }
        if (value == null) {
            value = "";
        }
        return value;
    }

    public double getSettingValueDouble(String key)
            throws NumberFormatException,
            StainAnalyzerSettingsValueNotSetException,
            StainAnalyzerSettingsKeyNotFoundException {
        if (settings.containsKey(key)) {
            String strValue = settings.get(key);
            if (strValue == null) {
                throw new StainAnalyzerSettingsValueNotSetException("value for " + key + " is not set");
            } else {
                return Double.parseDouble(strValue);
            }
        } else {
            throw new StainAnalyzerSettingsKeyNotFoundException(key + " (settings key) not found!!!");
        }
    }

    /**
     * put or update a value to the settings object
     *
     * @param key
     * @param value
     */
    public void updateSetting(String key, String value) {
        // check to see if key exist
        if (settings.containsKey(key)) {
            // remove exist key/value and insert the new key/value
            settings.remove(key);
        }
        settings.put(key, value);
    }

    /**
     * return summary params
     *
     * @return
     */
    public String[] getSummaryParams() {
        return SUMMARY_PARAMS;
    }

    /**
     * return summary param titles
     *
     * @return
     */
    public String[] getSummaryParamTitles() {
        return SUMMARY_PARAM_TITLES;
    }

    /**
     * return threshold choices
     *
     * @return
     */
    public String[] getThresholdChoices() {
        return THRESHOLD_CHOICES;
    }

    /**
     * iterator of keys
     *
     * @return
     */
    public Iterator<String> getSettingKeys() {
        return settings.keySet().iterator();
    }
}
