/*
 * do preprocessing (threshold among other things):
 * 1. calculate background 
 * 2. threshold 
 * 3. close 
 * 4. watershed
 * 
 * TODO: this transformation assume image is grey scale but how do we check
 * to make sure???
 */
package ca.ubc.gpec.ia.analyzer.transformation;

import ca.ubc.gpec.ia.analyzer.deconvolution.EntropyThreshold;
import ca.ubc.gpec.ia.analyzer.model.*;
import ca.ubc.gpec.ia.analyzer.segmentation.ThresholdAdjuster;
import ca.ubc.gpec.ia.analyzer.segmentation.Thresholder;
import ca.ubc.gpec.ia.analyzer.settings.StainAnalyzerSettings;
import ca.ubc.gpec.ia.analyzer.util.MiscUtil;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.EDM;
import ij.process.ImageProcessor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author samuelc
 */
public class ThresholdImageTransformation extends ImageTransformation {

    public static final String RESULT_FILE_PREFIX = "_tr_";
    public static final String RESULT_DIRECTORY_NAME = "_tr";
    public static final String RESULT_FILE_EXTENSION = ".bmp";
    private StainAnalyzerSettings settings;
    private ImageCache imageCache;
    ArrayList<String> settingKeys; // specify which color to threshold

    /**
     * constructor - remember to update Parameter!!! - single settingKey ...
     * this settingKeys will be applied to all images for this transformation in
     * the same order. Therefore, length of settingKeys must equal to the number
     * of images EXCEPT the case where length of settingKeys == 1, in this case,
     * the same settingKey will be applied to all images. - there's no way to
     * check the number of images here as we don't know until apply
     */
    public ThresholdImageTransformation(StainAnalyzerSettings settings, ImageCache imageCache, ArrayList settingKeys) {
        this.setName(this.getClass().getName()); // set name
        this.settings = settings;
        this.imageCache = imageCache;
        this.settingKeys = settingKeys;
        parameters.add(new ImageTransformationParameter(
                StainAnalyzerSettings.SETTING_KEY_MIN_BACKGROUND_THRESHOLD,
                settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_MIN_BACKGROUND_THRESHOLD)));
    }

    /**
     * constructor - remember to update Parameter!!! - single settingKey ...
     * this settingKey will be apply to ALL images for this transformation
     */
    public ThresholdImageTransformation(StainAnalyzerSettings settings, ImageCache imageCache, String settingKey) {
        this.setName(this.getClass().getName()); // set name
        this.settings = settings;
        this.imageCache = imageCache;
        settingKeys = new ArrayList<String>();
        settingKeys.add(settingKey);
        parameters.add(new ImageTransformationParameter(
                StainAnalyzerSettings.SETTING_KEY_MIN_BACKGROUND_THRESHOLD,
                settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_MIN_BACKGROUND_THRESHOLD)));
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
     * get background level
     *
     * @author: Andy Chan / Dmitry Turbin / Dustin Thompson
     *
     * @param imp
     * @param minThreshold
     * @param maxThreshold
     * @return
     */
    private double getMeanGrayLevel(ImagePlus imp, int minThreshold, int maxThreshold) {
        double sum = 0.0;
        int count = 0;

        byte[] pixels = (byte[]) imp.getProcessor().getPixels();
        int width = imp.getWidth();
        int height = imp.getHeight();

        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                int intensity = 0xff & pixels[offset + x];
                if (intensity >= minThreshold && intensity <= maxThreshold) {
                    sum += intensity;
                    count++;
                }
            }
        }
        //System.out.println("Count: " + count);
        return count > 0 ? sum / count : 0;
    }

    /**
     * do the actual background correction: 1. calculate background 2. threshold
     * image
     */
    private ImageDescriptor doAdjustments(ImageDescriptor id, String settingKey) throws ImageCacheItemNotFoundException, UnsupportedImageTransformationException, MalformedURLException, ImageCacheItemAlreadyInCacheException {
        ImagePlus imp = imageCache.getImagePlus(id).duplicate(); // REMEMBER TO GET a copy so the original image won't be overwritten
        data.add(new ImageTransformationData(
                ImageTransformationConstants.TRANSFORMATION_DATA_NAME_BACKGROUND_LEVEL,
                new Double(
                getMeanGrayLevel(
                imp,
                Integer.parseInt(settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_MIN_BACKGROUND_THRESHOLD).toString()), 255))));
        // get threshold correction (input from user)     
        parameters.add(new ImageTransformationParameter(
                settingKey, settings.getSettingValue(settingKey)));
        double correction = Double.parseDouble(settings.getSettingValue(settingKey).toString());
        // do threshold
        threshold(imp, settings.getSettingValue(StainAnalyzerSettings.SETTING_KEY_THRESHOLD_OPTION).toString(), correction);

        // Binary close
        ImageProcessor ip = imp.getProcessor();
        ip.dilate();
        imp.updateAndDraw();
        ip.erode();
        imp.updateAndDraw();

        // Run Watershed
        EDM edm = new EDM();
        edm.setup("watershed", imp);
        edm.run(ip);

        // generate result image & IAO
        URL url = new URL(id.getUrl());
        String outputDirName = FilenameUtils.concat(MiscUtil.revertUrlSpecialCharacterEncoding(FilenameUtils.getFullPath(url.getFile())), RESULT_DIRECTORY_NAME);
        String outputFileUrl = MiscUtil.pathToUrl(FilenameUtils.concat(outputDirName, RESULT_FILE_PREFIX + FilenameUtils.getBaseName(url.getFile()) + RESULT_FILE_EXTENSION));
        ImageDescriptor resultId = new ImageDescriptor(outputFileUrl);
        // put image to cache
        imageCache.put(resultId, imp);
        return resultId;
    }

    /**
     * do threshold
     *
     * @param imp
     * @param threshold
     * @param correction
     */
    private void threshold(ImagePlus imp, String threshold, double correction) {
        ThresholdAdjuster ta = null;
        if (threshold.equals(StainAnalyzerSettings.THRESHOLD_CHOICE_AUTO_THRESHOLD)) {
            ta = new ThresholdAdjuster(imp);
            ImageProcessor ip = imp.getProcessor();
            ip.setThreshold(ta.getMinAutoThreshold(), (int) (ta.getMaxAutoThreshold() * correction), ImageProcessor.RED_LUT);

            System.out.println("auto threshold values ... max=" + (int) (ta.getMaxAutoThreshold() * correction) + " min=" + ta.getMinAutoThreshold());

            Thresholder t = new Thresholder();
            t.setSkipDialog(true);
            t.applyThreshold(imp);
        } else if (threshold.equals(StainAnalyzerSettings.THRESHOLD_CHOICE_MAXIMUM_ENTROPY_THRESHOLD)) {
            ImageProcessor ip = imp.getProcessor();
            EntropyThreshold et = new EntropyThreshold();
            et.run(ip);
            imp.updateAndDraw();
            //lutColor = ImageProcessor.OVER_UNDER_LUT;
        } else if (threshold.equals(StainAnalyzerSettings.THRESHOLD_CHOICE_MANUALLY_ADJUST_THRESHOLD)) {
            //} else if (threshold == MANUAL_THRESHOLD) {
            imp.show(); // Show the image for visual control of thresholding
            IJ.run("View 100%");
            IJ.setTool(12); // Select the hand tool allowing user to move the image
            ta = new ThresholdAdjuster();
            ta.positionWindow(ThresholdAdjuster.TOP_RIGHT);
            ta.run();
        } else if (threshold.equals(StainAnalyzerSettings.THRESHOLD_CHOICE_AUTO_SET_THRESHOLD)) {
            // Using "Set" will retain the intensities / gray values
            ta = new ThresholdAdjuster(imp);
            ImageProcessor ip = imp.getProcessor();
            //IJ.setThreshold(ta.getMinAutoThreshold(), ta.getMaxAutoThreshold());
            ip.setThreshold(ta.getMinAutoThreshold(), ta.getMaxAutoThreshold(), ImageProcessor.RED_LUT);
            imp.updateAndDraw();
        } else if (threshold.equals(StainAnalyzerSettings.THRESHOLD_CHOICE_SET_ENTIRE_THRESHOLD)) {
            // Using "Set" will retain the intensities / gray values
            ta = new ThresholdAdjuster(imp);
            ImageProcessor ip = imp.getProcessor();
            //IJ.setThreshold(1, 254);
            ip.setThreshold(1, 254, ImageProcessor.RED_LUT);
            imp.updateAndDraw();
        } else if (threshold.equals(StainAnalyzerSettings.THRESHOLD_CHOICE_MANUALLY_ADJUST_AND_RECORD_THRESHOLD)) {
            imp.show(); // Show the image for visual control of thresholding
            IJ.run("View 100%");
            IJ.setTool(12); // Select the hand tool allowing user to move the image
            ta = new ThresholdAdjuster();
            ta.positionWindow(ThresholdAdjuster.TOP_RIGHT);
            ta.run();
            // no longer needed as threshold param will be record in IAO
        }
        // write param to IAO
        if (ta != null) {
            parameters.add(new ImageTransformationParameter(
                    "min. auto threshold", "" + ta.getMinAutoThreshold()));
            parameters.add(new ImageTransformationParameter(
                    "min. threshold", "" + ta.getMinThreshold()));
            parameters.add(new ImageTransformationParameter(
                    "max. auto threshold", "" + ta.getMaxAutoThreshold()));
            parameters.add(new ImageTransformationParameter(
                    "max. threshold", "" + ta.getMaxThreshold()));
        }
    }

    /**
     * implementation of ImageTransformation abstract method
     *
     * @param iao
     * @return
     */
    public IAO apply(IAO iao) throws ImageTransformationException, MalformedURLException {
        int settingKeysSize = settingKeys.size();
        int imagesSize = iao.getImageDescriptors().size();
        if (settingKeysSize > 1 && imagesSize != settingKeysSize) {
            throw new ParameterNotFoundImageTransformationException("size of settingKeys (" + settingKeysSize + ") not equal to number of iimages (" + imagesSize + ")");
        }
        if (settingKeysSize == 1) {
            for (int i = 1; i < imagesSize; i++) {
                String settingKey = settingKeys.get(0);
                settingKeys.add(settingKey);
            }
        }
        // IAO to store the result images
        IAO resultIao = new IAO();
        Iterator<String> settingKeysItr = settingKeys.iterator();
        for (ImageDescriptor id : iao.getImageDescriptors()) {
            // do background correction
            resultIao.addImageDescriptor(doAdjustments(id, settingKeysItr.next()));
        }
        iao.addIao(resultIao); // add result IAO to parent IAO
        return resultIao; // send back reference to result IAO
    }
}
