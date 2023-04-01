package ca.ubc.gpec.ia.analyzer.settings;

import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettingsException;

/**
 * capture exception when setting value is not set 
 * i.e. key is correct but the value is not set
 * @author samuelc
 *
 */
public class StainAnalyzerSettingsValueNotSetException extends StainAnalyzerSettingsException {

	public StainAnalyzerSettingsValueNotSetException(String msg) {
		super(msg);
	}
}
