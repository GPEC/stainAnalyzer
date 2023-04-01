/*
 * edit settings for membranous staining
 * - eventually, want this to refactor the nuclei stain specific params from
 *   SettingsEditor and have NucleiSettingsEditor subclass SettingsEditor 
 */
package ca.ubc.gpec.ia.analyzer.controller;

import ca.ubc.gpec.ia.analyzer.settings.MembranousStainAnalyzerSettings;

/**
 *
 * @author samuelc
 */
public class MembranousSettingsEditor extends SettingsEditor {

    /**
     * default constructor
     */
    public MembranousSettingsEditor() {
        // initialise HashMap
        this.settings = new MembranousStainAnalyzerSettings();
        summaryParams = settings.getSummaryParams();
        summaryParamTitles = settings.getSummaryParamTitles();
        logProgress = true; // initial value
        stainSelectionComboBox = null; // defined in createGeneralPanel
        channelsPanel = null; // defined in doSettings(String mode)
        generalPanel = null; // defined in doSettings(String mode)
    }
}
