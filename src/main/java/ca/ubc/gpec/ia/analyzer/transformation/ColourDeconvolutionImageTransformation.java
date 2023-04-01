/*
 * so colour deconvolution image transformation
 */
package ca.ubc.gpec.ia.analyzer.transformation;

import ca.ubc.gpec.ia.analyzer.deconvolution.ColourDeconvolutionEngine;
import ca.ubc.gpec.ia.analyzer.model.*;
import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettings;
import ca.ubc.gpec.ia.analyzer.util.MiscUtil;
import ij.ImagePlus;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author samuelc
 */
public class ColourDeconvolutionImageTransformation extends ImageTransformation {

    public static boolean DEBUG = true;
    public static final String RESULT_FILE_PREFIX = "_cdr_";
    public static final String RESULT_DIRECTORY_NAME = "_cdr";
    public static final String RESULT_FILE_EXTENSION = ".bmp";
    public static final String[] COLOUR_CHANNEL_NAMES = {"colour channel 1", "colour channel 2", "colour channel 3"};
    private StainAnalyzerSettings settings;
    private ImageCache imageCache;

    /**
     * constructor - remember to update Parameter!!!
     */
    public ColourDeconvolutionImageTransformation(StainAnalyzerSettings settings, ImageCache imageCache) {
        this.setName(this.getClass().getName()); // set name
        this.settings = settings;
        this.imageCache = imageCache;
        if (parameters.isEmpty()) {
            ImageTransformationParameter param = new ImageTransformationParameter();
            param.setName(StainAnalyzerSettings.SETTING_KEY_STAIN);
            param.setValue(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_STAIN).toString());
            parameters.add(param);
        }
    }

    /**
     * setter
     *
     * @param settings
     */
    public void setSettings(StainAnalyzerSettings settings) {
        this.settings = settings;
    }

    /**
     * getter
     *
     * @return
     */
    public StainAnalyzerSettings getSettings() {
        return settings;
    }

    /**
     * implementation of ImageTransformation abstract method
     *
     * @param iao
     * @return
     */
    public IAO apply(IAO iao) throws ImageTransformationException, MalformedURLException {
        // do color deconvolution
        if (iao.getImageDescriptors().size() != 1) {
            throw new UnsupportedImageTransformationException("0 or >1 image found in IAO.  Colour deconvolution can only be applied to exactly 1 image.");
        }
        ImageDescriptor id = iao.getImageDescriptors().first();
        if (DEBUG) {
            System.out.println("color deconvolution ... " + id.getUrl());
        }
        // 1. get and prepare an instance of ColourDeconvolutionEngine
        ColourDeconvolutionEngine cde = new ColourDeconvolutionEngine(new ImagePlus(id.getUrl()), settings);
        // 2. do deconvolution !!!
        cde.deconvolute(getParameterValue(StainAnalyzerSettings.SETTING_KEY_STAIN));
        // add a new IAO to contain the result
        // generate file names for deconvoluted results
        URL url = new URL(id.getUrl());
        String outputDirName = FilenameUtils.concat(MiscUtil.revertUrlSpecialCharacterEncoding(FilenameUtils.getFullPath(url.getFile())), RESULT_DIRECTORY_NAME);
        IAO resultIao = new IAO();
        int counter = 1;
        for (ImagePlus imp : cde.getImages()) {
            String outputFileUrl = MiscUtil.pathToUrl(FilenameUtils.concat(outputDirName, RESULT_FILE_PREFIX + FilenameUtils.getBaseName(url.getFile()) + "-c" + counter + RESULT_FILE_EXTENSION));
            ImageDescriptor resultId = new ImageDescriptor(outputFileUrl);
            resultIao.addImageDescriptor(resultId);
            // put image to cache
            imageCache.put(resultId, imp);
            counter++;
        }
        iao.addIao(resultIao);
        return resultIao;
    }
}
