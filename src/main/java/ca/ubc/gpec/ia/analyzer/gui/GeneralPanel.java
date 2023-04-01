package ca.ubc.gpec.ia.analyzer.gui;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettings;

public class GeneralPanel extends JPanel {

	/**
	 * Create the panel.
	 */
	 public GeneralPanel (GeneralDialog gd, StainAnalyzerSettings settings) {
	        super(new SpringLayout()); 
   
	        gd.resetNumRows();      
	        gd.addComboBox(this, "thresholdOption", "Threshold: ", settings.getThresholdChoices(), settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_THRESHOLD_OPTION));           
	        gd.addTextField(this, "blueCorrection", "Threshold correction for negative: ", settings.getSettingValue("blueCorrection"), 3);
	        gd.addTextField(this, "brownCorrection", "Threshold correction for positive: ", settings.getSettingValue("brownCorrection"), 3);   
	        gd.addTextField(this, "minBackgroundThreshold", "Minimum background threshold: ", settings.getSettingValue("minBackgroundThreshold"), 3);                   
	        gd.addTextField(this, "distanceInPixels", "Distance in pixels: ", settings.getSettingValue("distanceInPixels"), 6);     
	        gd.addTextField(this, "knownDistance", "Known distance: ", settings.getSettingValue("knownDistance"), 6);          
	        gd.addTextField(this, "unitOfLength", "Unit of length: ", settings.getSettingValue("unitOfLength"), 2); 
	        gd.addTextField(this, "positiveRGB","Positive outlines",
	        		settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_R)+","+
	        		settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_G)+","+
	        		settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_POSITIVE_SELECT_COLOUR_B),1);
	        gd.addTextField(this, "negativeRGB","Negative outlines",
	        		settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_R)+","+
	        		settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_G)+","+
	        		settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_NEGATIVE_SELECT_COLOUR_B),1);
	        // Lay out the panel
	        GeneralDialog.makeCompactGrid(this, gd.getNumRows(), 2, 0, 0, 5, 5);  	          
	 }
		
		
}

