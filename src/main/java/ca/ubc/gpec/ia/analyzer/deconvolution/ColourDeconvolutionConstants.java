package ca.ubc.gpec.ia.analyzer.deconvolution;

import java.util.Hashtable;

import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettingsException;
import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettingsKeyNotFoundException;
import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettingsValueNotSetException;
import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettings;

/**
 * constants for Colour_Deconvolution
 * @author samuelc
 *
 */

public class ColourDeconvolutionConstants {

	private static ColourDeconvolutionConstants singletonObj;
	
	public final String TITLE = "G. Landini's Colour Deconvolution";
	
	public final String STAIN_OPTION_H_DAB = "H DAB";
	public final String STAIN_OPTION_HE = "H&E";
	public final String STAIN_OPTION_HE_2 = "H&E 2";
	public final String STAIN_OPTION_FEULGEN_LIGHT_GREEN = "Feulgen Light Green";
	public final String STAIN_OPTION_GIEMSA = "Giemsa";
	public final String STAIN_OPTION_FASTRED_FASTBLUE_DAB = "FastRed FastBlue DAB";
	public final String STAIN_OPTION_METHYL_GREEN_DAB = "Methyl Green DAB";
	public final String STAIN_OPTION_HE_DAB = "H&E DAB";
	public final String STAIN_OPTION_H_AEC = "H AEC";
	public final String STAIN_OPTION_AZAN_MALLORY = "Azan-Mallory";
	public final String STAIN_OPTION_MASSON_TRICHROME = "Masson Trichrome";
	public final String STAIN_OPTION_ALCIAN_BLUE_H = "Alcian blue & H";
	public final String STAIN_OPTION_H_PAS = "H PAS";
	public final String STAIN_OPTION_RGB = "RGB";
	public final String STAIN_OPTION_CMY = "CMY";
	public final String STAIN_OPTION_FROM_ROI = "From ROI";
	public final String STAIN_OPTION_USER_VALUES = "User values";
	public final String[] AVAILABLE_STAIN_OPTIONS = {
		STAIN_OPTION_H_DAB,
		STAIN_OPTION_HE,
		STAIN_OPTION_HE_2,
		STAIN_OPTION_FEULGEN_LIGHT_GREEN,
		STAIN_OPTION_GIEMSA,
		STAIN_OPTION_FASTRED_FASTBLUE_DAB,
		STAIN_OPTION_METHYL_GREEN_DAB,
		STAIN_OPTION_HE_DAB,
		STAIN_OPTION_H_AEC,
		STAIN_OPTION_AZAN_MALLORY,
		STAIN_OPTION_MASSON_TRICHROME,
		STAIN_OPTION_ALCIAN_BLUE_H,
		STAIN_OPTION_H_PAS,
		STAIN_OPTION_RGB,
		STAIN_OPTION_CMY,
		STAIN_OPTION_FROM_ROI,
		STAIN_OPTION_USER_VALUES
	};
	public final double[][] INITIAL_CHANNEL_MATRIX = {
			{0,0,0},
			{0,0,0},
			{0,0,0}
	};
	private double[][] FROM_ROI_CHANNEL_MATRIX = null;
	private Hashtable<String, String[]> CHANNEL_NAMES = new Hashtable<String, String[]>(); 
	private Hashtable<String, double[][]> CHANNEL_MATRICES = new Hashtable<String, double[][]>();  
	
	/**
	 * default constructor - population CHANNEl_NAMES and CHANNEL_MATRICES
	 * - make this class a singleton
	 */
	private ColourDeconvolutionConstants() {
		
		CHANNEL_NAMES.put(STAIN_OPTION_H_DAB, new String[] {"Haem matrix","DAB matrix","Zero matrix"});
		CHANNEL_MATRICES.put(STAIN_OPTION_H_DAB, new double[][] 
			{{0.650, 0.704, 0.286},
			 {0.268, 0.570, 0.776},
			 {0.0,   0.0,   0.0}}
		);

		CHANNEL_NAMES.put(STAIN_OPTION_HE, new String[] {"GL Haem matrix","GL Eos matrix","Zero matrix"});
		CHANNEL_MATRICES.put(STAIN_OPTION_HE, new double [][]
			{{0.644211, 0.716556, 0.266844},
			 {0.092789, 0.954111, 0.283111},
			 {0.0,      0.0,      0.0}}
		);

		CHANNEL_NAMES.put(STAIN_OPTION_HE_2, new String[] {"GL Haem matrix", "GL Eos matrix", "Zero matrix"});
		CHANNEL_MATRICES.put(STAIN_OPTION_HE_2, new double [][]
			{{0.49015734, 0.76897085, 0.41040173},
			 {0.04615336, 0.8420684,  0.5373925},
			 {0.0,        0.0,        0.0}}
		);

		CHANNEL_NAMES.put(STAIN_OPTION_FEULGEN_LIGHT_GREEN, new String[] {"Feulgen", "light green", "Zero matrix"});
		CHANNEL_MATRICES.put(STAIN_OPTION_FEULGEN_LIGHT_GREEN, new double [][]
			{{0.46420921, 0.83008335, 0.30827187},
			 {0.94705542, 0.25373821, 0.19650764},
			 {0.0,        0.0,        0.0}}
		);
		
		CHANNEL_NAMES.put(STAIN_OPTION_GIEMSA, new String[] {"GL Methylene Blue and Eosin", "GL Eos matrix", "Zero matrix"});
		CHANNEL_MATRICES.put(STAIN_OPTION_GIEMSA, new double[][]
			{{0.834750233, 0.513556283, 0.196330403},
			 {0.092789,    0.954111,    0.283111},	
			 {0.0,         0.0,         0.0}}
		);

		CHANNEL_NAMES.put(STAIN_OPTION_FASTRED_FASTBLUE_DAB, new String[] {"fast red", "fast blue", "dab"});
		CHANNEL_MATRICES.put(STAIN_OPTION_FASTRED_FASTBLUE_DAB, new double[][]
			{{0.21393921, 0.85112669, 0.47794022},
			 {0.74890292, 0.60624161, 0.26731082},
			 {0.268,      0.570,      0.776}
			}
		);

		CHANNEL_NAMES.put(STAIN_OPTION_METHYL_GREEN_DAB, new String[] {"MG matrix (GL)","DAB matrix","Zero matrix"});
		CHANNEL_MATRICES.put(STAIN_OPTION_METHYL_GREEN_DAB, new double[][]
			{{0.98003, 0.144316, 0.133146},
			 {0.268,   0.570,    0.776},
			 {0.0,     0.0,      0.0}}
		);
		
		CHANNEL_NAMES.put(STAIN_OPTION_HE_DAB, new String[] {"Haem matrix", "Eos matrix", "DAB matrix"});
		CHANNEL_MATRICES.put(STAIN_OPTION_HE_DAB, new double[][]
			{{0.650, 0.704, 0.286},
			 {0.072, 0.990, 0.105},
			 {0.268, 0.570, 0.776}}
		);

		CHANNEL_NAMES.put(STAIN_OPTION_H_AEC, new String[] {"Haem matrix", "AEC matrix", "Zero matrix"});
		CHANNEL_MATRICES.put(STAIN_OPTION_H_AEC, new double[][]
			{{0.650,  0.704,  0.286},
			 {0.2743, 0.6796, 0.6803},
			 {0.0,    0.0,    0.0}}
		);
		
		CHANNEL_NAMES.put(STAIN_OPTION_AZAN_MALLORY, new String[] {"GL Blue matrix Anilline Blue", "GL Red matrix Azocarmine", "GL Orange matrix Orange-G"});
		CHANNEL_MATRICES.put(STAIN_OPTION_AZAN_MALLORY,	new double[][] 
			{{0.853033,   0.508733,   0.112656},
			 {0.09289875, 0.8662008,  0.49098468},
			 {0.10732849, 0.36765403, 0.9237484}}
		);

		CHANNEL_NAMES.put(STAIN_OPTION_MASSON_TRICHROME, new String[] {"GL Methyl blue", "GL Ponceau Fuchsin (approx.)", "Zero matrix"});
		CHANNEL_MATRICES.put(STAIN_OPTION_MASSON_TRICHROME, new double[][]
			{{0.7995107,  0.5913521,  0.10528667},
			 {0.09997159, 0.73738605, 0.6680326},
			 {0.0,        0.0,        0.0}}
		);

		CHANNEL_NAMES.put(STAIN_OPTION_ALCIAN_BLUE_H, new String[] {"GL Alcian Blue matrix", "GL Haematox after PAS matrix", "Zero matrix"});
		CHANNEL_MATRICES.put(STAIN_OPTION_ALCIAN_BLUE_H, new double[][]
			{{0.874622, 0.457711, 0.158256},
		 	 {0.552556, 0.7544,   0.353744},
	 		 {0.0,      0.0,      0.0}}
		);
		
		CHANNEL_NAMES.put(STAIN_OPTION_H_PAS, new String[] {"GL Haem matrix", "GL PAS matrix", "Zero matrix"});
		CHANNEL_MATRICES.put(STAIN_OPTION_H_PAS, new double[][]
			{{0.644211, 0.716556, 0.266844},
			 {0.175411, 0.972178, 0.154589},
			 {0.0,      0.0,      0.0}}
		);
		
		CHANNEL_NAMES.put(STAIN_OPTION_RGB, new String[]{"R", "G", "B"});
		CHANNEL_MATRICES.put(STAIN_OPTION_RGB, new double[][]
			{{0.0, 1.0, 1.0},
			 {1.0, 0.0, 1.0},
			 {1.0, 1.0, 0.0}}
		);
		
		CHANNEL_NAMES.put(STAIN_OPTION_CMY, new String[] {"C", "M", "Y"});
		CHANNEL_MATRICES.put(STAIN_OPTION_CMY, new double[][]
			{{1.0, 0.0, 0.0},
			 {0.0, 1.0, 0.0},
			 {0.0, 0.0, 1.0}}
		);
		
		// user settings ...
		CHANNEL_NAMES.put(STAIN_OPTION_USER_VALUES, new String[] {"stain 1", "stain 2", "stain 3"});
	}
	
	/**
	 * load user settings
	 * @param stainAnalyzerSettings
	 */
	public void loadUserSettings(StainAnalyzerSettings stainAnalyzerSettings) throws NumberFormatException {
		try {
			CHANNEL_MATRICES.put(STAIN_OPTION_USER_VALUES, new double[][]
			{
				{stainAnalyzerSettings.getSettingValueDouble(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD1_X), 
				 stainAnalyzerSettings.getSettingValueDouble(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD1_Y), 
				 stainAnalyzerSettings.getSettingValueDouble(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD1_Z)},
					
				{stainAnalyzerSettings.getSettingValueDouble(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD2_X), 
				 stainAnalyzerSettings.getSettingValueDouble(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD2_Y), 
				 stainAnalyzerSettings.getSettingValueDouble(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD2_Z)},
					 
				{stainAnalyzerSettings.getSettingValueDouble(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD3_X), 
				 stainAnalyzerSettings.getSettingValueDouble(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD3_Y), 
				 stainAnalyzerSettings.getSettingValueDouble(StainAnalyzerSettings.SETTING_KEY_COLOUR_DECONVOLUTION_MOD3_Z)}
			});
		} catch (StainAnalyzerSettingsException sase) {
			// assume matrix values are either all set or all not set
			CHANNEL_MATRICES.put(STAIN_OPTION_USER_VALUES, INITIAL_CHANNEL_MATRIX);
		}
		
	}
	
	public String[] getChannelNames(String stainName) {
		return CHANNEL_NAMES.get(stainName);
	}
	
	public double[][] getChannelMatrix(String stainName, StainAnalyzerSettings stainAnalyzerSettings) {
		if (stainName.equals(STAIN_OPTION_USER_VALUES)) {
			// construct the channel matrix
			loadUserSettings(stainAnalyzerSettings);
		} 
		return CHANNEL_MATRICES.get(stainName);
	}
	
	public void setChannelMatrixFromROI(double[][] channelMatrixFromROI) {
		FROM_ROI_CHANNEL_MATRIX = channelMatrixFromROI;
	}
	
	public double[][] getChannelMatrixFromROI() {
		return FROM_ROI_CHANNEL_MATRIX;
	}
	
	public static ColourDeconvolutionConstants getInstance() {
		if (singletonObj == null) {
			singletonObj = new ColourDeconvolutionConstants();
		}
		return singletonObj;
	}
	
}
