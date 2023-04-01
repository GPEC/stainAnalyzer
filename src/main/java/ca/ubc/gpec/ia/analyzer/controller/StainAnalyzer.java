/*
 * Generic stain analyzer working on a SINGLE IAO
 * (an IAO can contain > 1 original image)
 * - subclass would be e.g. MembranousStainAnalyzer, NuclearStainAnalyzer (after refactor)
 */
package ca.ubc.gpec.ia.analyzer.controller;

import ca.ubc.gpec.ia.analyzer.model.IAO;
import ca.ubc.gpec.ia.analyzer.model.ImageCache;
import ca.ubc.gpec.ia.analyzer.model.ImageTransformation;
import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettings;
import ca.ubc.gpec.ia.analyzer.transformation.ImageTransformationException;
import ij.IJ;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 *
 * @author samuelc
 */
public abstract class StainAnalyzer {

    // need protected instead of private because subclass is the one
    // doing the real work, therefore need access to them
    protected StainAnalyzerSettings settings; // analyzer settings
    protected ArrayList<ImageTransformation> transformations; // list of transformation
    // image analysis object
    protected IAO iao;
    // cache for images (e.g. temp images)
    protected ImageCache imageCache;

    /**
     * constructor - subclass NEEDS to initialize settings!!!
     */
    public StainAnalyzer() {
        transformations = new ArrayList<ImageTransformation>();
        imageCache = new ImageCache();
    }

    /**
     * set IAO
     *
     * @param iao
     */
    public void setIao(IAO iao) {
        this.iao = iao;
    }

    /**
     * get root IAO
     *
     * @return
     */
    public IAO getIao() {
        return iao;
    }

    /**
     * set stain analyzer settings
     * @param settings 
     */
    public void setSettings(StainAnalyzerSettings settings) {
        this.settings = settings;
    }
        
    /**
     * show some progress messages
     *
     * @param msg
     * @param progress
     */
    protected void showFeedback(String msg, double progress) {
        IJ.showStatus(msg);
        IJ.showProgress(progress);
    }
    
    /**
     * analyze iao
     * @param iao
     * @throws ImageTransformationException
     * @throws MalformedURLException 
     */
    public abstract IAO analyzeIao(IAO iao) throws ImageTransformationException, MalformedURLException, URISyntaxException;
}
