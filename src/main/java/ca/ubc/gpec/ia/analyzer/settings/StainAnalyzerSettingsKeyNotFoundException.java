package ca.ubc.gpec.ia.analyzer.settings;

import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettingsException;

/**
 * capture exception when key not found
 * @author samuelc
 *
 */
public class StainAnalyzerSettingsKeyNotFoundException extends StainAnalyzerSettingsException {

	public StainAnalyzerSettingsKeyNotFoundException(String msg) {
		super(msg);
	}
}
