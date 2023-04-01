/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.gpec.ia.analyzer.controller;

import ca.ubc.gpec.ia.analyzer.settings.NuclearStainAnalyzerSettings;

/**
 *
 * @author samuelc
 */
public class NuclearSettingsEditor extends SettingsEditor {

    /**
     * default constructor
     */
    public NuclearSettingsEditor() {
        // initialise HashMap
        this.settings = new NuclearStainAnalyzerSettings();
        summaryParams = settings.getSummaryParams();
        summaryParamTitles = settings.getSummaryParamTitles();
        logProgress = true; // initial value
        stainSelectionComboBox = null; // defined in createGeneralPanel
        channelsPanel = null; // defined in doSettings(String mode)
        generalPanel = null; // defined in doSettings(String mode)
    }
}
