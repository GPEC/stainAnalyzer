//package ij.plugin.filter; /// GPEC mod
package ca.ubc.gpec.ia.analyzer.processing;

import java.awt.*;
import java.util.Vector;
import java.util.Properties;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.text.*;
import ij.plugin.MeasurementsWriter;

// GPEC mod
import ij.plugin.filter.PlugInFilter;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Enumeration;
import ca.ubc.gpec.ia.analyzer.measure.Measurements;
import ca.ubc.gpec.ia.analyzer.measure.ResultsTable;
import ca.ubc.gpec.ia.analyzer.processing.HaralickTexture;
import java.util.ArrayList;

/**
 * This plugin implements ImageJ's Analyze/Measure and Analyze/Set Measurements
 * commands.
 */
public class Analyzer implements PlugInFilter, Measurements {

    private String arg;
    private ImagePlus imp;
    private ResultsTable rt;
    private int measurements;
    private StringBuffer min, max, mean, sd;
    /// GPEC mod - added GLCM texture analysis parameters
    // Order must agree with order of checkboxes in Set Measurements dialog box
    private static final int[] list = {AREA, MEAN, STD_DEV, MODE, MIN_MAX,
        CENTROID, CENTER_OF_MASS, PERIMETER, RECT, ELLIPSE, CIRCULARITY, FERET,
        INTEGRATED_DENSITY, MEDIAN, SKEWNESS, KURTOSIS, ROUNDNESS, LIMIT,
        LABELS, INVERT_Y, TEXTURE};
    /*
     * ,GLCM_ASM_H,GLCM_CONTRAST_H,GLCM_CORRELATION_H,
     * GLCM_IDM_H,GLCM_ENTROPY_H,GLCM_SUM_H,GLCM_ASM_V,GLCM_CONTRAST_V,GLCM_CORRELATION_V,
                GLCM_IDM_V,GLCM_ENTROPY_V,GLCM_SUM_V};
     */
    private static final int UNDEFINED = 0, AREAS = 1, LENGTHS = 2, ANGLES = 3, POINTS = 4;
    private static int mode = AREAS;
    private static final String MEASUREMENTS = "measurements";
    private static final String MARK_WIDTH = "mark.width";
    private static final String PRECISION = "precision";
    //private static int counter;
    private static boolean unsavedMeasurements;
    public static Color darkBlue = new Color(0, 0, 160);
    private static int systemMeasurements = Prefs.getInt(MEASUREMENTS, AREA + MEAN + MIN_MAX);
    public static int markWidth = Prefs.getInt(MARK_WIDTH, 0);
    public static int precision = Prefs.getInt(PRECISION, 3);
    private static float[] umeans = new float[MAX_STANDARDS];
    private static ResultsTable systemRT = new ResultsTable();
    private static int redirectTarget;
    private static String redirectTitle = "";
    static int firstParticle, lastParticle;
    private static boolean summarized;
    private boolean included = true; /// GPEC mod - whether the current particle should be included
    /// GPEC mod
    private boolean applyFilters = false;
    private Hashtable filters;
    private Hashtable textureParameters;
    private double meanBackgroundGrayLevel = 0.0;

    public Analyzer() {
        rt = systemRT;
        rt.setPrecision(precision);
        measurements = systemMeasurements;
    }

    /**
     * Constructs a new Analyzer using the specified ImagePlus object and the
     * system-wide measurement options and results table.
     */
    public Analyzer(ImagePlus imp) {
        this();
        this.imp = imp;
    }

    /**
     * Construct a new Analyzer using an ImagePlus object and private
     * measurement options and results table.
     */
    public Analyzer(ImagePlus imp, int measurements, ResultsTable rt) {
        this.imp = imp;
        this.measurements = measurements;
        this.rt = rt;
    }

    public int setup(String arg, ImagePlus imp) {
        this.arg = arg;
        this.imp = imp;
        IJ.register(Analyzer.class);
        if (arg.equals("set")) {
            doSetDialog();
            return DONE;
        } else if (arg.equals("sum")) {
            summarize();
            return DONE;
        } else if (arg.equals("clear")) {
            clearWorksheet();
            return DONE;
        } else {
            return DOES_ALL + NO_CHANGES;
        }
    }

    public void run(ImageProcessor ip) {
        measure();
    }

    void doSetDialog() {
        String NONE = "None";
        String[] titles;
        int[] wList = WindowManager.getIDList();
        if (wList == null) {
            titles = new String[1];
            titles[0] = NONE;
        } else {
            titles = new String[wList.length + 1];
            titles[0] = NONE;
            for (int i = 0; i < wList.length; i++) {
                ImagePlus imp = WindowManager.getImage(wList[i]);
                titles[i + 1] = imp != null ? imp.getTitle() : "";
            }
        }
        ImagePlus tImp = WindowManager.getImage(redirectTarget);
        String target = tImp != null ? tImp.getTitle() : NONE;

        GenericDialog gd = new GenericDialog("Set Measurements", IJ.getInstance());
        /// GPEC mod - added GLCM texture analysis results
        //String[] labels = new String[16];
        //boolean[] states = new boolean[16];
        String[] labels = new String[22];
        boolean[] states = new boolean[22];
        labels[0] = "Area";
        states[0] = (systemMeasurements & AREA) != 0;
        labels[1] = "Mean Gray Value";
        states[1] = (systemMeasurements & MEAN) != 0;
        labels[2] = "Standard Deviation";
        states[2] = (systemMeasurements & STD_DEV) != 0;
        labels[3] = "Modal Gray Value";
        states[3] = (systemMeasurements & MODE) != 0;
        labels[4] = "Min & Max Gray Value";
        states[4] = (systemMeasurements & MIN_MAX) != 0;
        labels[5] = "Centroid";
        states[5] = (systemMeasurements & CENTROID) != 0;
        labels[6] = "Center of Mass";
        states[6] = (systemMeasurements & CENTER_OF_MASS) != 0;
        labels[7] = "Perimeter";
        states[7] = (systemMeasurements & PERIMETER) != 0;
        labels[8] = "Bounding Rectangle";
        states[8] = (systemMeasurements & RECT) != 0;
        labels[9] = "Fit Ellipse";
        states[9] = (systemMeasurements & ELLIPSE) != 0;
        labels[10] = "Circularity";
        states[10] = (systemMeasurements & CIRCULARITY) != 0;
        labels[11] = "Feret's Diameter";
        states[11] = (systemMeasurements & FERET) != 0;
        labels[12] = "Integrated Density";
        states[12] = (systemMeasurements & INTEGRATED_DENSITY) != 0;
        labels[13] = "Median";
        states[13] = (systemMeasurements & MEDIAN) != 0;
        labels[14] = "Skewness";
        states[14] = (systemMeasurements & SKEWNESS) != 0;
        labels[15] = "Kurtosis";
        states[15] = (systemMeasurements & KURTOSIS) != 0;

        gd.addCheckboxGroup(9, 2, labels, states);
        labels = new String[3];
        states = new boolean[3];
        labels[0] = "Limit to Threshold";
        states[0] = (systemMeasurements & LIMIT) != 0;
        labels[1] = "Display Label";
        states[1] = (systemMeasurements & LABELS) != 0;
        labels[2] = "Invert Y Coordinates";
        states[2] = (systemMeasurements & INVERT_Y) != 0;
        gd.addCheckboxGroup(2, 2, labels, states);
        gd.addMessage("");
        gd.addChoice("Redirect To:", titles, target);
        gd.addNumericField("Decimal Places (0-9):", precision, 0, 2, "");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        setOptions(gd);
        int index = gd.getNextChoiceIndex();
        redirectTarget = index == 0 ? 0 : wList[index - 1];
        redirectTitle = titles[index];
        int prec = (int) gd.getNextNumber();
        if (prec < 0) {
            prec = 0;
        }
        if (prec > 9) {
            prec = 9;
        }
        if (prec != precision) {
            precision = prec;
            rt.setPrecision(precision);
            if (mode == AREAS && IJ.isResultsWindow()) {
                IJ.setColumnHeadings("");
                updateHeadings();
            }
        }
    }

    void clearWorksheet() {
        resetCounter();
    }

    void setOptions(GenericDialog gd) {
        int oldMeasurements = systemMeasurements;
        int previous = 0;
        boolean b = false;
        for (int i = 0; i < list.length; i++) {
            //if (list[i]!=previous)
            b = gd.getNextBoolean();
            previous = list[i];
            if (b) {
                systemMeasurements |= list[i];
            } else {
                systemMeasurements &= ~list[i];
            }
        }
        if ((oldMeasurements & (~LIMIT)) != (systemMeasurements & (~LIMIT))) {
            if (IJ.macroRunning()) {
                resetCounter();
                mode = AREAS;
            } else {
                mode = UNDEFINED;
            }
        }
        if ((systemMeasurements & LABELS) == 0) {
            systemRT.disableRowLabels();
        }
    }

    void measure() {
        firstParticle = lastParticle = 0;
        Roi roi = imp.getRoi();
        if (roi != null && roi.getType() == Roi.POINT) {
            measurePoint(roi);
            return;
        }
        if (roi != null && roi.isLine()) {
            measureLength(roi);
            return;
        }
        if (roi != null && roi.getType() == Roi.ANGLE) {
            measureAngle(roi);
            return;
        }
        if (mode != AREAS) {
            if (!resetCounter()) {
                return;
            }
            mode = AREAS;
        }
        ImageStatistics stats;
        if (isRedirectImage()) {
            stats = getRedirectStats(measurements, roi);
            if (stats == null) {
                return;
            }
        } else {
            stats = imp.getStatistics(measurements);
        }
        saveResults(stats, roi, imp, imp.getProcessor(), null); /// GPEC mod - added imp.getProcessor(), null, null as arguments
        displayResults();
    }

    /**
     * Returns
     * <code>true</code> if an image is selected in the "Redirect To:" popup
     * menu of the Analyze/Set Measurements dialog box.
     */
    public static boolean isRedirectImage() {
        return redirectTarget != 0;
    }

    /**
     * Returns the image selected in the "Redirect To:" popup menu of the
     * Analyze/Set Measurements dialog or null if "None" is selected, the image
     * was not found or the image is not the same size as
     * <code>currentImage</code>.
     */
    public static ImagePlus getRedirectImage(ImagePlus currentImage) {
        ImagePlus rImp = WindowManager.getImage(redirectTarget);
        if (rImp == null) {
            IJ.error("Analyzer", "Redirect image (\"" + redirectTitle + "\")\n"
                    + "not found.");
            redirectTarget = 0;
            Macro.abort();
            return null;
        }
        if (rImp.getWidth() != currentImage.getWidth() || rImp.getHeight() != currentImage.getHeight()) {
            IJ.error("Analyzer", "Redirect image (\"" + redirectTitle + "\") \n"
                    + "is not the same size as the current image.");
            Macro.abort();
            return null;
        }
        return rImp;
    }

    ImageStatistics getRedirectStats(int measurements, Roi roi) {
        ImagePlus redirectImp = getRedirectImage(imp);
        if (redirectImp == null) {
            return null;
        }
        ImageProcessor ip = redirectImp.getProcessor();
        ip.setRoi(roi);
        return ImageStatistics.getStatistics(ip, measurements, redirectImp.getCalibration());
    }

    void measurePoint(Roi roi) {
        if (mode != POINTS) {
            if (!resetCounter()) {
                return;
            }
            //IJ.setColumnHeadings(" \tX\tY\tValue");		
            mode = POINTS;
        }
        Polygon p = roi.getPolygon();
        ImageProcessor ip = imp.getProcessor();
        Calibration cal = imp.getCalibration();
        ip.setCalibrationTable(cal.getCTable());
        for (int i = 0; i < p.npoints; i++) {
            incrementCounter();
            int x = p.xpoints[i];
            int y = p.ypoints[i];
            double value = ip.getPixelValue(x, y);
            if (markWidth > 0) {
                ip.setColor(Toolbar.getForegroundColor());
                ip.setLineWidth(markWidth);
                ip.moveTo(x, y);
                ip.lineTo(x, y);
                imp.updateAndDraw();
                ip.setLineWidth(Line.getWidth());
            }
            if ((measurements & LABELS) != 0) {
                rt.addLabel("Label", getFileName());
            }
            rt.addValue("X", cal.getX(x));
            rt.addValue("Y", cal.getY(updateY(y, imp.getHeight())));
            rt.addValue("Z", cal.getZ(imp.getCurrentSlice() - 1));
            rt.addValue("Value", value);
            displayResults();
        }
        //IJ.write(rt.getCounter()+"\t"+n(cal.getX(x))+n(cal.getY(y))+n(value));
    }

    void measureAngle(Roi roi) {
        if (mode != ANGLES) {
            if (!resetCounter()) {
                return;
            }
            if ((measurements & LABELS) != 0) {
                IJ.setColumnHeadings(" \tName\tangle");
            } else {
                IJ.setColumnHeadings(" \tangle");
            }
            mode = ANGLES;
        }
        incrementCounter();
        if ((measurements & LABELS) != 0) {
            rt.addLabel("Label", getFileName());
        }
        rt.addValue("Angle", ((PolygonRoi) roi).getAngle());
        displayResults();
        //IJ.write(rt.getCounter()+"\t"+n(((PolygonRoi)roi).getAngle()));
    }

    void measureLength(Roi roi) {
        if (mode != LENGTHS) {
            if (!resetCounter()) {
                return;
            }
            if ((measurements & LABELS) != 0) {
                IJ.setColumnHeadings(" \tName\tlength");
            } else {
                IJ.setColumnHeadings(" \tlength");
            }
            mode = LENGTHS;
        }
        incrementCounter();
        if ((measurements & LABELS) != 0) {
            rt.addLabel("Label", getFileName());
        }
        rt.addValue("Length", roi.getLength());
        double angle = 0.0;
        if (roi.getType() == Roi.LINE) {
            Line l = (Line) roi;
            angle = roi.getAngle(l.x1, l.y1, l.x2, l.y2);
        }
        rt.addValue("Angle", angle);
        boolean moreParams = (measurements & MEAN) != 0 || (measurements & STD_DEV) != 0 || (measurements & MODE) != 0 || (measurements & MIN_MAX) != 0;
        if (moreParams) {
            ProfilePlot profile = new ProfilePlot(imp);
            double[] values = profile.getProfile();
            ImageProcessor ip2 = new FloatProcessor(values.length, 1, values);
            ImageStatistics stats = ImageStatistics.getStatistics(ip2, MEAN + STD_DEV + MODE + MIN_MAX, null);
            if ((measurements & MEAN) != 0) {
                rt.addValue(ResultsTable.MEAN, stats.mean);
            }
            if ((measurements & STD_DEV) != 0) {
                rt.addValue(ResultsTable.STD_DEV, stats.stdDev);
            }
            if ((measurements & MODE) != 0) {
                rt.addValue(ResultsTable.MODE, stats.dmode);
            }
            if ((measurements & MIN_MAX) != 0) {
                rt.addValue(ResultsTable.MIN, stats.min);
                rt.addValue(ResultsTable.MAX, stats.max);
            }
        }
        if ((measurements & RECT) != 0) {
            Rectangle r = roi.getBounds();
            Calibration cal = imp.getCalibration();
            rt.addValue(ResultsTable.ROI_X, r.x * cal.pixelWidth);
            rt.addValue(ResultsTable.ROI_Y, updateY2(r.y * cal.pixelHeight));
            rt.addValue(ResultsTable.ROI_WIDTH, r.width * cal.pixelWidth);
            rt.addValue(ResultsTable.ROI_HEIGHT, r.height * cal.pixelHeight);
        }
        displayResults();
    }

    /**
     * Saves the measurements specified in the "Set Measurements" dialog, or by
     * calling setMeasurments(), in the system results table.
     */
    /// GPEC mod - added ip, mask as argument
    public void saveResults(ImageStatistics stats, Roi roi, ImagePlus imp, ImageProcessor ip, ImageProcessor mask) {
        /// GPEC mod - First figure out whether should include this particle
        double perimeter;
        if (roi != null) {
            perimeter = roi.getLength();
        } else {
            perimeter = 0.0;
        }
        double circularity = perimeter == 0.0 ? 0.0 : 4.0 * Math.PI * (stats.area / (perimeter * perimeter));
        double feret = roi != null ? roi.getFeretsDiameter() : 0.0;
        double ellipseRatio = stats.major / stats.minor;
        double roundness = 4 * stats.area / (Math.PI * feret * feret);

        // Haralick texture analysis
        Hashtable results = new Hashtable();
        Hashtable discriminantParameters = null;
        int[] features = null;
        Hashtable featureValues = null;
        HaralickTexture ht = null;

        double textureScore = 0.0;
        if ((measurements & TEXTURE) != 0) {
            results = doTextureAnalysis(imp, ip, roi, mask);

            /// GPEC mod - added Haralick analysis results to table
            ht = new HaralickTexture();
            features = ht.getFeatures();
            featureValues = new Hashtable();
            // textureScore is the discriminant score from discriminant analysis
            // d = constant + b1x1 + b2x2 + ... + bixi

            discriminantParameters = (Hashtable) textureParameters.get("discriminantParameters");
            if (discriminantParameters.containsKey("discriminantConstant")) {
                textureScore = Double.parseDouble(discriminantParameters.get("discriminantConstant").toString());
            }
            for (int i = 0; i < features.length; i++) {
                int feature = features[i];
                if (results.containsKey(new Integer(feature))) {
                    double x = Double.parseDouble(results.get(new Integer(feature)).toString());
                    featureValues.put(new Integer(feature), new Double(x));
                    if (discriminantParameters.containsKey(new Integer(feature))) {
                        double b = Double.parseDouble(discriminantParameters.get(new Integer(feature)).toString());
                        textureScore += b * x;
                    }
                }
            }
        }

        this.included = true;

        if (this.applyFilters) { // GPEC mod - Apply filters to exclude objects
            double filter = Double.NaN;
            if (this.filters.containsKey("minArea")) {
                filter = Double.parseDouble(this.filters.get("minArea").toString());
                //filter = 15;
                if (!Double.isNaN(filter) && (stats.area < filter)) {
                    this.included = false;
                    return;
                }
            }
            if (this.filters.containsKey("maxArea")) {
                filter = Double.parseDouble(this.filters.get("maxArea").toString());
                //filter = 200;
                if (!Double.isNaN(filter) && (stats.area > filter)) {
                    this.included = false;
                    return;
                }
            }
            if (this.filters.containsKey("minCircularity")) {
                filter = Double.parseDouble(this.filters.get("minCircularity").toString());
                //filter = 0.4;
                if (!Double.isNaN(filter) && (circularity < filter)) {
                    this.included = false;
                    return;
                }
            }
            if (this.filters.containsKey("maxCircularity")) {
                filter = Double.parseDouble(this.filters.get("maxCircularity").toString());
                //filter = 1;
                if (!Double.isNaN(filter) && (circularity > filter)) {
                    this.included = false;
                    return;
                }
            }
            if (this.filters.containsKey("minEllipseRatio")) {
                filter = Double.parseDouble(this.filters.get("minEllipseRatio").toString());
                //filter = 1;
                if (!Double.isNaN(filter) && (ellipseRatio < filter)) {
                    this.included = false;
                    return;
                }
            }
            if (this.filters.containsKey("maxEllipseRatio")) {
                filter = Double.parseDouble(this.filters.get("maxEllipseRatio").toString());
                //filter = 3;
                if (!Double.isNaN(filter) && (ellipseRatio > filter)) {
                    this.included = false;
                    return;
                }
            }
            if (this.filters.containsKey("minFeret")) {
                filter = Double.parseDouble(this.filters.get("minFeret").toString());
                //filter = 7;
                if (!Double.isNaN(filter) && (feret < filter)) {
                    this.included = false;
                    return;
                }
            }
            if (this.filters.containsKey("maxFeret")) {
                filter = Double.parseDouble(this.filters.get("maxFeret").toString());
                //filter = 50;
                if (!Double.isNaN(filter) && (feret > filter)) {
                    this.included = false;
                    return;
                }
            }
            if (this.filters.containsKey("minRoundness")) {
                filter = Double.parseDouble(this.filters.get("minRoundness").toString());
                //filter = 0.5;
                if (!Double.isNaN(filter) && (roundness < filter)) {
                    this.included = false;
                    return;
                }
            }
            if (this.filters.containsKey("maxRoundness")) {
                filter = Double.parseDouble(this.filters.get("maxRoundness").toString());
                //filter = 1;
                if (!Double.isNaN(filter) && (roundness > filter)) {
                    this.included = false;
                    return;
                }
            }

            boolean applyTextureFilter = true;
            double applyTextureValue = Double.NaN;
            if (applyTextureFilter && this.filters.containsKey("textureObjectsMinArea")) {
                applyTextureValue = Double.parseDouble(this.filters.get("textureObjectsMinArea").toString());
                if (!Double.isNaN(applyTextureValue) && (stats.area < applyTextureValue)) {
                    applyTextureFilter = false;
                }
            }
            if (applyTextureFilter && this.filters.containsKey("textureObjectsMaxArea")) {
                applyTextureValue = Double.parseDouble(this.filters.get("textureObjectsMaxArea").toString());
                if (!Double.isNaN(applyTextureValue) && (stats.area > applyTextureValue)) {
                    applyTextureFilter = false;
                }
            }
            if (applyTextureFilter && this.filters.containsKey("textureObjectsMinCircularity")) {
                applyTextureValue = Double.parseDouble(this.filters.get("textureObjectsMinCircularity").toString());
                if (!Double.isNaN(applyTextureValue) && (circularity < applyTextureValue)) {
                    applyTextureFilter = false;
                }
            }
            if (applyTextureFilter && this.filters.containsKey("textureObjectsMaxCircularity")) {
                applyTextureValue = Double.parseDouble(this.filters.get("textureObjectsMaxCircularity").toString());
                if (!Double.isNaN(applyTextureValue) && (circularity > applyTextureValue)) {
                    applyTextureFilter = false;
                }
            }
            if (applyTextureFilter && this.filters.containsKey("textureObjectsMinEllipseRatio")) {
                applyTextureValue = Double.parseDouble(this.filters.get("textureObjectsMinEllipseRatio").toString());
                if (!Double.isNaN(applyTextureValue) && (ellipseRatio < applyTextureValue)) {
                    applyTextureFilter = false;
                }
            }
            if (applyTextureFilter && this.filters.containsKey("textureObjectsMaxEllipseRatio")) {
                applyTextureValue = Double.parseDouble(this.filters.get("textureObjectsMaxEllipseRatio").toString());
                if (!Double.isNaN(applyTextureValue) && (ellipseRatio > applyTextureValue)) {
                    applyTextureFilter = false;
                }
            }
            if (applyTextureFilter && this.filters.containsKey("textureObjectsMinFeret")) {
                applyTextureValue = Double.parseDouble(this.filters.get("textureObjectsMinFeret").toString());
                if (!Double.isNaN(applyTextureValue) && (feret < applyTextureValue)) {
                    applyTextureFilter = false;
                }
            }
            if (applyTextureFilter && this.filters.containsKey("textureObjectsMaxFeret")) {
                applyTextureValue = Double.parseDouble(this.filters.get("textureObjectsMaxFeret").toString());
                if (!Double.isNaN(applyTextureValue) && (feret > applyTextureValue)) {
                    applyTextureFilter = false;
                }
            }
            if (this.filters.containsKey("textureObjectsMinRoundness")) {
                applyTextureValue = Double.parseDouble(this.filters.get("textureObjectsMinRoundness").toString());
                if (!Double.isNaN(applyTextureValue) && (roundness < applyTextureValue)) {
                    applyTextureFilter = false;
                }
            }
            if (this.filters.containsKey("textureObjectsMaxRoundness")) {
                applyTextureValue = Double.parseDouble(this.filters.get("textureObjectsMaxRoundness").toString());
                if (!Double.isNaN(applyTextureValue) && (roundness > applyTextureValue)) {
                    applyTextureFilter = false;
                }
            }

            if (applyTextureFilter) {
                if (this.filters.containsKey("minTextureScore")) {
                    filter = Double.parseDouble(this.filters.get("minTextureScore").toString());
                    if (!Double.isNaN(filter) && (textureScore < filter)) {
                        this.included = false;
                        return;
                    }
                }
                if (this.filters.containsKey("maxTextureScore")) {
                    filter = Double.parseDouble(this.filters.get("maxTextureScore").toString());
                    if (!Double.isNaN(filter) && (textureScore > filter)) {
                        this.included = false;
                        return;
                    }
                }
            }
        }

        incrementCounter();
        int counter = rt.getCounter();
        if (counter <= MAX_STANDARDS) {
            if (umeans == null) {
                umeans = new float[MAX_STANDARDS];
            }
            umeans[counter - 1] = (float) stats.umean;
        }
        if ((measurements & LABELS) != 0) {
            rt.addLabel("Label", getFileName());
        }
        if ((measurements & AREA) != 0) {
            rt.addValue(ResultsTable.AREA, stats.area);
        }
        if ((measurements & MEAN) != 0) {
            rt.addValue(ResultsTable.MEAN, stats.mean);
            /// GPEC mod - added mean OD
            rt.addValue("Mean_Background_Intensity", meanBackgroundGrayLevel);
            rt.addValue("Mean_Optical_Density", Math.log10(meanBackgroundGrayLevel / stats.mean));
        }
        if ((measurements & STD_DEV) != 0) {
            rt.addValue(ResultsTable.STD_DEV, stats.stdDev);
        }
        if ((measurements & MODE) != 0) {
            rt.addValue(ResultsTable.MODE, stats.dmode);
        }
        if ((measurements & MIN_MAX) != 0) {
            rt.addValue(ResultsTable.MIN, stats.min);
            rt.addValue(ResultsTable.MAX, stats.max);
        }
        if ((measurements & CENTROID) != 0) {
            rt.addValue(ResultsTable.X_CENTROID, stats.xCentroid);
            rt.addValue(ResultsTable.Y_CENTROID, updateY(stats.yCentroid));
        }
        if ((measurements & CENTER_OF_MASS) != 0) {
            rt.addValue(ResultsTable.X_CENTER_OF_MASS, stats.xCenterOfMass);
            rt.addValue(ResultsTable.Y_CENTER_OF_MASS, updateY(stats.yCenterOfMass));
        }
        if ((measurements & PERIMETER) != 0 || (measurements & CIRCULARITY) != 0) {
            /// GPEC mod - commented out
			/*
             * double perimeter; if (roi!=null) perimeter = roi.getLength();
             * else perimeter = 0.0;
             */
            if ((measurements & PERIMETER) != 0) {
                rt.addValue(ResultsTable.PERIMETER, perimeter);
            }
            if ((measurements & CIRCULARITY) != 0) {
                /// GPEC mod - commented out
                //double circularity = perimeter==0.0?0.0:4.0*Math.PI*(stats.area/(perimeter*perimeter));
                if (circularity > 1.) {
                    circularity = -1.0;
                }
                rt.addValue(ResultsTable.CIRCULARITY, circularity);
            }
        }
        if ((measurements & RECT) != 0) {
            rt.addValue(ResultsTable.ROI_X, stats.roiX);
            rt.addValue(ResultsTable.ROI_Y, updateY2(stats.roiY));
            rt.addValue(ResultsTable.ROI_WIDTH, stats.roiWidth);
            rt.addValue(ResultsTable.ROI_HEIGHT, stats.roiHeight);
        }
        if ((measurements & ELLIPSE) != 0) {
            rt.addValue(ResultsTable.MAJOR, stats.major);
            rt.addValue(ResultsTable.MINOR, stats.minor);
            rt.addValue(ResultsTable.RATIO, ellipseRatio); /// GPEC mod - added
            rt.addValue(ResultsTable.ANGLE, stats.angle);
        }
        if ((measurements & FERET) != 0) /// GPEC mod
        //rt.addValue(ResultsTable.FERET, roi!=null?roi.getFeretsDiameter():0.0); // commented out
        {
            rt.addValue(ResultsTable.FERET, feret);
        }
        if ((measurements & INTEGRATED_DENSITY) != 0) {
            rt.addValue(ResultsTable.INTEGRATED_DENSITY, stats.area * stats.mean);
        }
        if ((measurements & MEDIAN) != 0) {
            rt.addValue(ResultsTable.MEDIAN, stats.median);
        }
        if ((measurements & SKEWNESS) != 0) {
            rt.addValue(ResultsTable.SKEWNESS, stats.skewness);
        }
        if ((measurements & KURTOSIS) != 0) {
            rt.addValue(ResultsTable.KURTOSIS, stats.kurtosis);
        }
        if ((measurements & ROUNDNESS) != 0) {
            rt.addValue(ResultsTable.ROUNDNESS, roundness); /// GPEC mod - added     
        }
        /// GPEC mod - added Haralick analysis results to table
        if ((measurements & TEXTURE) != 0) {
            for (int i = 0; i < features.length; i++) {
                int feature = features[i];
                if (featureValues.containsKey(new Integer(feature))) {
                    rt.addValue(ht.getFeatureTitle(feature), Double.parseDouble(featureValues.get(new Integer(feature)).toString()));
                }
            }
            if (discriminantParameters.containsKey("discriminantConstant")) {
                rt.addValue("Texture_Score", textureScore);
            }
        }
    }

    /// GPEC mod - added public accessors
    public boolean isIncluded() {
        return this.included;
    }

    public void applyFilters(Hashtable filters) {
        this.filters = filters;
        this.applyFilters = true;
    }

    public void setTextureParameters(int degree, int stepSize, int grayLevels, int precision, int computeFeatures, Hashtable discriminantParameters) {
        if (textureParameters == null) {
            textureParameters = new Hashtable();
        }
        textureParameters.put("degree", new Integer(degree));
        textureParameters.put("stepSize", new Integer(stepSize));
        textureParameters.put("grayLevels", new Integer(grayLevels));
        textureParameters.put("precision", new Integer(precision));
        textureParameters.put("computeFeatures", new Integer(computeFeatures));
        textureParameters.put("discriminantParameters", discriminantParameters);
    }

    public void setMeanBackgroundGrayLevel(double mean) {
        meanBackgroundGrayLevel = mean;
    }

    /// GPEC mod  - added
    public Hashtable doTextureAnalysis(ImagePlus imp, ImageProcessor ip, Roi roi, ImageProcessor mask) {
        int degree = Integer.parseInt(textureParameters.get("degree").toString());
        int stepSize = Integer.parseInt(textureParameters.get("stepSize").toString());
        int grayLevels = Integer.parseInt(textureParameters.get("grayLevels").toString());
        int precision = Integer.parseInt(textureParameters.get("precision").toString());
        int computeFeatures = Integer.parseInt(textureParameters.get("computeFeatures").toString());

        HaralickTexture mt = new HaralickTexture(degree, stepSize, grayLevels, precision, computeFeatures);
        mt.analyzeImage(imp, ip, roi, mask);
        Hashtable results = mt.getResults(imp.getTitle());

        return results;
    }

    // Update centroid and center of mass y-coordinate
    // based on value "Invert Y Coordinates" flag
    double updateY(double y) {
        if (imp == null) {
            return y;
        } else {
            if ((systemMeasurements & INVERT_Y) != 0) {
                Calibration cal = imp.getCalibration();
                y = imp.getHeight() * cal.pixelHeight - y;
            }
            return y;
        }
    }

    // Update bounding rectangle y-coordinate based
    // on value "Invert Y Coordinates" flag
    double updateY2(double y) {
        if (imp == null) {
            return y;
        } else {
            if ((systemMeasurements & INVERT_Y) != 0) {
                Calibration cal = imp.getCalibration();
                y = imp.getHeight() * cal.pixelHeight - y - cal.pixelHeight;
            }
            return y;
        }
    }

    String getFileName() {
        String s = "";
        if (imp != null) {
            if (mode == AREAS && redirectTarget != 0) {
                ImagePlus rImp = WindowManager.getImage(redirectTarget);
                if (rImp != null) {
                    s = rImp.getTitle();
                }
            } else {
                s = imp.getTitle();
            }
            int len = s.length();
            if (len > 4 && s.charAt(len - 4) == '.' && !Character.isDigit(s.charAt(len - 1))) {
                s = s.substring(0, len - 4);
            }
            Roi roi = imp.getRoi();
            String roiName = roi != null ? roi.getName() : null;
            if (roiName != null) {
                s += ":" + roiName;
            }
            if (imp.getStackSize() > 1) {
                ImageStack stack = imp.getStack();
                int currentSlice = imp.getCurrentSlice();
                String label = stack.getShortSliceLabel(currentSlice);
                String colon = s.equals("") ? "" : ":";
                if (label != null && !label.equals("")) {
                    s += colon + label;
                } else {
                    s += colon + currentSlice;
                }
            }
        }
        return s;
    }

    /**
     * Writes the last row in the results table to the ImageJ window.
     */
    public void displayResults() {
        int counter = rt.getCounter();
        if (counter == 1) {
            IJ.setColumnHeadings(rt.getColumnHeadings());
        }
        IJ.write(rt.getRowAsString(counter - 1));
    }

    /**
     * Updates the displayed column headings. Does nothing if the results table
     * headings and the displayed headings are the same. Redisplays the results
     * if the headings are different and the results table is not empty.
     */
    public void updateHeadings() {
        TextPanel tp = IJ.getTextPanel();
        if (tp == null) {
            return;
        }
        String worksheetHeadings = tp.getColumnHeadings();
        String tableHeadings = rt.getColumnHeadings();
        if (worksheetHeadings.equals(tableHeadings)) {
            return;
        }
        IJ.setColumnHeadings(tableHeadings);
        int n = rt.getCounter();
        if (n > 0) {
            StringBuffer sb = new StringBuffer(n * tableHeadings.length());
            for (int i = 0; i < n; i++) {
                sb.append(rt.getRowAsString(i) + "\n");
            }
            tp.append(new String(sb));
        }
    }

    /**
     * Converts a number to a formatted string with a tab at the end.
     */
    public String n(double n) {
        String s;
        if (Math.round(n) == n) {
            s = IJ.d2s(n, 0);
        } else {
            s = IJ.d2s(n, precision);
        }
        return s + "\t";
    }

    void incrementCounter() {
        //counter++;
        if (rt == null) {
            rt = systemRT;
        }
        rt.incrementCounter();
        unsavedMeasurements = true;
    }

    public void summarize() {
        rt = systemRT;
        if (rt.getCounter() == 0) {
            return;
        }
        if (summarized) {
            rt.show("Results");
        }
        measurements = systemMeasurements;
        min = new StringBuffer(100);
        max = new StringBuffer(100);
        mean = new StringBuffer(100);
        sd = new StringBuffer(100);
        min.append("Min\t");
        max.append("Max\t");
        mean.append("Mean\t");
        sd.append("SD\t");
        if ((measurements & LABELS) != 0) {
            min.append("\t");
            max.append("\t");
            mean.append("\t");
            sd.append("\t");
        }
        if (mode == POINTS) {
            summarizePoints(rt);
        } else if (mode == LENGTHS) {
            summarizeLengths(rt);
        } else if (mode == ANGLES) {
            add2(rt.getColumnIndex("Angle"));
        } else {
            summarizeAreas();
        }
        TextPanel tp = IJ.getTextPanel();
        if (tp != null) {
            String worksheetHeadings = tp.getColumnHeadings();
            if (worksheetHeadings.equals("")) {
                IJ.setColumnHeadings(rt.getColumnHeadings());
            }
        }
        IJ.write("");
        IJ.write(new String(mean));
        IJ.write(new String(sd));
        IJ.write(new String(min));
        IJ.write(new String(max));
        IJ.write("");
        mean = null;
        sd = null;
        min = null;
        max = null;
        summarized = true;
    }

    void summarizePoints(ResultsTable rt) {
        add2(rt.getColumnIndex("X"));
        add2(rt.getColumnIndex("Y"));
        add2(rt.getColumnIndex("Z"));
        add2(rt.getColumnIndex("Value"));
    }

    void summarizeLengths(ResultsTable rt) {
        int index = rt.getColumnIndex("Mean");
        if (rt.columnExists(index)) {
            add2(index);
        }
        index = rt.getColumnIndex("StdDev");
        if (rt.columnExists(index)) {
            add2(index);
        }
        index = rt.getColumnIndex("Mode");
        if (rt.columnExists(index)) {
            add2(index);
        }
        index = rt.getColumnIndex("Min");
        if (rt.columnExists(index)) {
            add2(index);
        }
        index = rt.getColumnIndex("Max");
        if (rt.columnExists(index)) {
            add2(index);
        }
        index = rt.getColumnIndex("Angle");
        if (rt.columnExists(index)) {
            add2(index);
        }
        index = rt.getColumnIndex("Length");
        if (rt.columnExists(index)) {
            add2(index);
        }
    }

    void summarizeAreas() {
        if ((measurements & AREA) != 0) {
            add2(ResultsTable.AREA);
        }
        if ((measurements & MEAN) != 0) {
            add2(ResultsTable.MEAN);
        }
        if ((measurements & STD_DEV) != 0) {
            add2(ResultsTable.STD_DEV);
        }
        if ((measurements & MODE) != 0) {
            add2(ResultsTable.MODE);
        }
        if ((measurements & MIN_MAX) != 0) {
            add2(ResultsTable.MIN);
            add2(ResultsTable.MAX);
        }
        if ((measurements & CENTROID) != 0) {
            add2(ResultsTable.X_CENTROID);
            add2(ResultsTable.Y_CENTROID);
        }
        if ((measurements & CENTER_OF_MASS) != 0) {
            add2(ResultsTable.X_CENTER_OF_MASS);
            add2(ResultsTable.Y_CENTER_OF_MASS);
        }
        if ((measurements & PERIMETER) != 0) {
            add2(ResultsTable.PERIMETER);
        }
        if ((measurements & RECT) != 0) {
            add2(ResultsTable.ROI_X);
            add2(ResultsTable.ROI_Y);
            add2(ResultsTable.ROI_WIDTH);
            add2(ResultsTable.ROI_HEIGHT);
        }
        if ((measurements & ELLIPSE) != 0) {
            add2(ResultsTable.MAJOR);
            add2(ResultsTable.MINOR);
            add2(ResultsTable.ANGLE);
        }
        if ((measurements & CIRCULARITY) != 0) {
            add2(ResultsTable.CIRCULARITY);
        }
        if ((measurements & FERET) != 0) {
            add2(ResultsTable.FERET);
        }
        if ((measurements & INTEGRATED_DENSITY) != 0) {
            add2(ResultsTable.FERET);
        }
        if ((measurements & MEDIAN) != 0) {
            add2(ResultsTable.MEDIAN);
        }
        if ((measurements & SKEWNESS) != 0) {
            add2(ResultsTable.SKEWNESS);
        }
        if ((measurements & KURTOSIS) != 0) {
            add2(ResultsTable.KURTOSIS);
        }
    }

    private void add2(int column) {
        float[] c = column >= 0 ? rt.getColumn(column) : null;
        if (c != null) {
            ImageProcessor ip = new FloatProcessor(c.length, 1, c, null);
            if (ip == null) {
                return;
            }
            ImageStatistics stats = new FloatStatistics(ip);
            if (stats == null) {
                return;
            }
            mean.append(n(stats.mean));
            min.append(n(stats.min));
            max.append(n(stats.max));
            sd.append(n(stats.stdDev));
        } else {
            mean.append("-\t");
            min.append("-\t");
            max.append("-\t");
            sd.append("-\t");
        }
    }

    /**
     * Returns the current measurement count.
     */
    public static int getCounter() {
        return systemRT.getCounter();
    }

    /**
     * Sets the measurement counter to zero. Displays a dialog that allows the
     * user to save any existing measurements. Returns false if the user cancels
     * the dialog.
     */
    public synchronized static boolean resetCounter() {
        TextPanel tp = IJ.isResultsWindow() ? IJ.getTextPanel() : null;
        int counter = systemRT.getCounter();
        int lineCount = tp != null ? IJ.getTextPanel().getLineCount() : 0;
        ImageJ ij = IJ.getInstance();
        if (counter > 0 && lineCount > 0 && unsavedMeasurements && !IJ.macroRunning() && ij != null && !ij.quitting()) {
            SaveChangesDialog d = new SaveChangesDialog(ij, "Save " + counter + " measurements?");
            if (d.cancelPressed()) {
                return false;
            } else if (d.savePressed()) {
                new MeasurementsWriter().run("");
            }
        }
        umeans = null;
        systemRT.reset();
        unsavedMeasurements = false;
        if (tp != null) {
            tp.selectAll();
            tp.clearSelection();
        }
        summarized = false;
        return true;
    }

    public static void setSaved() {
        unsavedMeasurements = false;
    }

    // Returns the measurements defined in the Set Measurements dialog. */
    public static int getMeasurements() {
        return systemMeasurements;
    }

    // Sets the system-wide measurements. */
    public static void setMeasurements(int measurements) {
        systemMeasurements = measurements;
    }

    /**
     * Called once when ImageJ quits.
     */
    public static void savePreferences(Properties prefs) {
        prefs.put(MEASUREMENTS, Integer.toString(systemMeasurements));
        prefs.put(MARK_WIDTH, Integer.toString(markWidth));
        prefs.put(PRECISION, Integer.toString(precision));
    }

    /**
     * Returns an array containing the first 20 uncalibrated means.
     */
    public static float[] getUMeans() {
        return umeans;
    }

    /**
     * Returns the ImageJ results table.
     */
    public static ResultsTable getResultsTable() {
        return systemRT;
    }

    /**
     * Returns the number of digits displayed on the right of decimal point.
     */
    public static int getPrecision() {
        return precision;
    }

    /**
     * Returns an updated Y coordinate based on the current "Invert Y
     * Coordinates" flag.
     */
    public static int updateY(int y, int imageHeight) {
        if ((systemMeasurements & INVERT_Y) != 0) {
            y = imageHeight - y - 1;
        }
        return y;
    }
}
