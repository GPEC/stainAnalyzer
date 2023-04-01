/*
 * keep track of IAOs;
 * call StainAnalyzer to analyzer images
 */
package ca.ubc.gpec.ia.analyzer.controller;

import ca.ubc.gpec.ia.analyzer.model.IAO;
import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettings;
import ca.ubc.gpec.ia.analyzer.transformation.ImageTransformationException;
import ca.ubc.gpec.ia.analyzer.gui.ConfigJPanel;
import ca.ubc.gpec.ia.analyzer.gui.ImageSelectionJPanel;
import ca.ubc.gpec.ia.analyzer.gui.MainJPanel;
import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.TreeSet;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import loci.formats.FormatException;

/**
 *
 * @author samuelc
 */
public abstract class StainAnalyzerController extends Application {

    private TreeSet<IAO> iaos; // set of ORIGINAL images to be analyzed
    private ImageSelectionJPanel imageSelectionJPanel;
    private ConfigJPanel configJPanel;
    private MainJPanel mainJPanel;
    private StainAnalyzer stainAnalyzer;

    /**
     * constructor
     */
    public StainAnalyzerController(StainAnalyzer stainAnalyzer) {
        iaos = new TreeSet<IAO>();
        imageSelectionJPanel = null;
        configJPanel = null;
        this.stainAnalyzer = stainAnalyzer;
    }

    /**
     * set imageSelectionJPanel
     *
     * @param imageSelectionJPanel
     */
    public void setImageSelectionJPanel(ImageSelectionJPanel imageSelectionJPanel) {
        this.imageSelectionJPanel = imageSelectionJPanel;
    }

    /**
     * set configJPanel
     *
     * @param configJPanel
     */
    public void setConfigJPanel(ConfigJPanel configJPanel) {
        this.configJPanel = configJPanel;
    }

    /**
     * set StainAnalyzerSettings
     *
     * @param settings
     */
    public void setStainAnalyzerSettings(StainAnalyzerSettings settings) {
        stainAnalyzer.setSettings(settings);
    }

    /**
     * set mainJPanel
     *
     * @param mainJPanel
     */
    public void setMainJPanel(MainJPanel mainJPanel) {
        this.mainJPanel = mainJPanel;
    }

    public void setMainTabbedPaneToImageSelection() {
        mainJPanel.getMainJTabbedPane().setSelectedIndex(0);
    }

    /**
     * repaint everything!!!
     */
    public void repaint() {
        mainJPanel.repaint();
        imageSelectionJPanel.repaint();
    }

    /**
     * instantiate an instance of StainAnalyzer, apply settings and analyze
     * input ioa
     *
     * @param iao
     * @throws ImageTransformationException
     * @throws MalformedURLException
     */
    public abstract IAO analyzeIao(IAO iao) throws ImageTransformationException, MalformedURLException, URISyntaxException;

    /**
     * add IAO to iaos
     *
     * @param iao if input iao already in iaos, return false return true if this
     * set did not already contain the specified element
     */
    public boolean addIAO(IAO iao) {
        return iaos.add(iao);
    }

    /**
     * return all iaos.
     *
     * @return
     */
    public TreeSet<IAO> getIAOs() {
        return iaos;
    }

    /**
     * clear all iaos
     *
     * @return
     */
    public void clearIAOs() {
        iaos.clear();
    }

    /**
     * update imageSelectionJPanel with the current list of iaos
     */
    public void updateImageSelectionJPanel() throws IOException, URISyntaxException {
        // get selected IAOs
        imageSelectionJPanel.update(iaos);
    }

}
