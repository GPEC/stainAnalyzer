//package ij.plugin.filter; /// GPEC mod
package ca.ubc.gpec.ia.analyzer.processing;

import ca.ubc.gpec.ia.analyzer.processing.HaralickTexture;
import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.util.Properties;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.text.*;
//import ij.plugin.filter.Analyzer;
import ij.plugin.frame.Recorder;

// GPEC mod
import ij.plugin.filter.PlugInFilter;
import ij.gui.Wand;
import java.util.Hashtable;
import java.util.ArrayList;

import ca.ubc.gpec.ia.analyzer.processing.Analyzer;
import ca.ubc.gpec.ia.analyzer.measure.Measurements;
import ca.ubc.gpec.ia.analyzer.measure.ResultsTable;

/**
 * Implements ImageJ's Analyze Particles command. <p>
 * <pre>
 * for each line do
 * for each pixel in this line do
 * if the pixel value is "inside" the threshold range then
 * trace the edge to mark the object
 * do the measurement
 * fill the object with a color outside the threshold range
 * else
 * continue the scan
 * </pre>
 */
public class ParticleAnalyzer implements PlugInFilter, Measurements {

    /**
     * Display results in the ImageJ console.
     */
    public static final int SHOW_RESULTS = 1;
    /**
     * Obsolete
     */
    public static final int SHOW_SUMMARY = 2;
    /**
     * Display image containing outlines of measured paticles.
     */
    public static final int SHOW_OUTLINES = 4;
    /**
     * Do not measure particles touching edge of image.
     */
    public static final int EXCLUDE_EDGE_PARTICLES = 8;
    /**
     * Display a particle size distribution histogram.
     */
    public static final int SHOW_SIZE_DISTRIBUTION = 16;
    /**
     * Display a progress bar.
     */
    public static final int SHOW_PROGRESS = 32;
    /**
     * Clear ImageJ console before starting.
     */
    public static final int CLEAR_WORKSHEET = 64;
    /**
     * Record starting coordinates so outline can be recreated later using
     * doWand(x,y).
     */
    public static final int RECORD_STARTS = 128;
    /**
     * Display a summary.
     */
    public static final int DISPLAY_SUMMARY = 256;
    /**
     * Do not display particle outline image.
     */
    public static final int SHOW_NONE = 512;
    /**
     * Flood fill to ignore interior holes.
     */
    public static final int FLOOD_FILL = 1024;
    /**
     * Show Masks.
     */
    public static final int SHOW_MASKS = 2048; /// GPEC mod - added   
    /**
     * Overlay outline on another image.
     */
    public static final int SHOW_OVERLAY = 4096; /// GPEC mod - added   
    static final String OPTIONS = "ap.options";
    static final String BINS = "ap.bins";
    static final int BYTE = 0, SHORT = 1, FLOAT = 2;
    private static int staticMinSize = 1;
    private static int staticMaxSize = 999999;
    private static int staticOptions = Prefs.getInt(OPTIONS, CLEAR_WORKSHEET);
    private static int staticBins = Prefs.getInt(BINS, 20);
    private static String[] showStrings = {"Nothing", "Outlines", "Masks", "Ellipses"};
    protected static final int NOTHING = 0, OUTLINES = 1, MASKS = 2, ELLIPSES = 3;
    protected static final int OVERLAY = 4; /// GPEC mod - added
    protected static int showChoice;
    protected ImagePlus imp;
    protected ResultsTable rt;
    protected Analyzer analyzer;
    protected int slice;
    protected boolean processStack;
    protected boolean showResults, excludeEdgeParticles, showSizeDistribution,
            resetCounter, showProgress, recordStarts, displaySummary, floodFill;
    private double level1, level2;
    private int minSize;
    private int maxSize;
    private int sizeBins;
    private int options;
    private int measurements;
    private Calibration calibration;
    private String arg;
    private double fillColor;
    private boolean thresholdingLUT;
    private ImageProcessor drawIP;
    private int width, height;
    private boolean canceled;
    private ImageStack outlines;
    private IndexColorModel customLut;
    private int particleCount;
    private int totalCount;
    private TextWindow tw;
    private Wand wand;
    private int imageType, imageType2;
    private int xStartC, yStartC;
    private boolean roiNeedsImage;
    private int minX, maxX, minY, maxY;
    private ImagePlus redirectImp;
    private ImageProcessor redirectIP;
    private PolygonFiller pf;
    private Roi saveRoi;
    private int beginningCount;
    private Rectangle r;
    private ImageProcessor mask;
    private double totalArea;
    private FloodFiller ff;
    private Polygon polygon;
    /// GPEC mod
    private boolean applyFilters = false;
    private Hashtable filters;
    private ImagePlus overlayImp;
    private Color overlayColour;
    private String imageTitle;
    private Hashtable textureParameters;
    private double meanBackgroundGrayLevel = 0.0;
    private Hashtable images = new Hashtable();

    /**
     * Construct a ParticleAnalyzer.
     *
     * @param options	a flag word created by Oring SHOW_RESULTS,
     * EXCLUDE_EDGE_PARTICLES, etc.
     * @param measurements	a flag word created by ORing constants defined in the
     * Measurements interface
     * @param rt	a ResultsTable where the measurements will be stored
     * @param minSize	the smallest particle size in pixels
     * @param maxSize	the largest particle size in pixels
     */
    public ParticleAnalyzer(int options, int measurements, ResultsTable rt, double minSize, double maxSize) {
        this.options = options;
        this.measurements = measurements;
        this.rt = rt;
        if (this.rt == null) {
            this.rt = new ResultsTable();
        }
        this.minSize = (int) minSize;
        this.maxSize = (int) maxSize;
        sizeBins = staticBins;
        slice = 1;
    }

    /**
     * Default constructor
     */
    public ParticleAnalyzer() {
        slice = 1;
    }

    public int setup(String arg, ImagePlus imp) {
        this.arg = arg;
        this.imp = imp;
        IJ.register(ParticleAnalyzer.class);
        if (imp == null) {
            IJ.noImage();
            return DONE;
        }
        if (!showDialog()) {
            return DONE;
        }
        int baseFlags = DOES_8G + DOES_16 + DOES_32 + NO_CHANGES + NO_UNDO;
        int flags = Analyzer.isRedirectImage() ? baseFlags : IJ.setupDialog(imp, baseFlags);
        processStack = (flags & DOES_STACKS) != 0;
        slice = 0;
        saveRoi = imp.getRoi();
        if (saveRoi != null && saveRoi.getType() != Roi.RECTANGLE && saveRoi.isArea()) {
            polygon = saveRoi.getPolygon();
        }
        imp.startTiming();
        return flags;
    }

    public void run(ImageProcessor ip) {
        if (canceled) {
            return;
        }
        slice++;
        if (imp.getStackSize() > 1 && processStack) {
            imp.setSlice(slice);
        }
        if (!analyze(imp, ip)) {
            canceled = true;
        }
        if (slice == imp.getStackSize()) {
            imp.updateAndDraw();
            if (saveRoi != null) {
                imp.setRoi(saveRoi);
            }
        }
    }

    /**
     * Displays a modal options dialog.
     */
    public boolean showDialog() {
        GenericDialog gd = new GenericDialog("Analyze Particles");
        minSize = staticMinSize;
        maxSize = staticMaxSize;
        sizeBins = staticBins;
        options = staticOptions;
        gd.addNumericField("Minimum Size (pixels):", minSize, 0);
        gd.addNumericField("Maximum Size (pixels):", maxSize, 0);
        gd.addNumericField("Bins (2-256):", sizeBins, 0);
        gd.addChoice("Show:", showStrings, showStrings[showChoice]);

        String[] labels = new String[7];
        boolean[] states = new boolean[14];
        labels[0] = "Display Results";
        states[0] = (options & SHOW_RESULTS) != 0;
        labels[1] = "Exclude on Edges";
        states[1] = (options & EXCLUDE_EDGE_PARTICLES) != 0;
        labels[2] = "Clear Results";
        states[2] = (options & CLEAR_WORKSHEET) != 0;
        labels[3] = "Flood Fill";
        states[3] = (options & FLOOD_FILL) != 0;
        labels[4] = "Summarize";
        states[4] = (options & DISPLAY_SUMMARY) != 0;
        labels[5] = "Record Starts";
        states[5] = (options & RECORD_STARTS) != 0;
        labels[6] = "Size Distribution";
        states[6] = (options & SHOW_SIZE_DISTRIBUTION) != 0;
        gd.addCheckboxGroup(4, 2, labels, states);

        gd.showDialog();
        if (gd.wasCanceled()) {
            return false;
        }
        minSize = (int) gd.getNextNumber();
        maxSize = (int) gd.getNextNumber();
        sizeBins = (int) gd.getNextNumber();
        if (gd.invalidNumber()) {
            IJ.error("Minimum Size, Maximum Size or Bins invalid.");
            canceled = true;
            return false;
        }
        staticMinSize = minSize;
        staticMaxSize = maxSize;
        staticBins = sizeBins;
        showChoice = gd.getNextChoiceIndex();
        if (gd.getNextBoolean()) {
            options |= SHOW_RESULTS;
        } else {
            options &= ~SHOW_RESULTS;
        }
        if (gd.getNextBoolean()) {
            options |= EXCLUDE_EDGE_PARTICLES;
        } else {
            options &= ~EXCLUDE_EDGE_PARTICLES;
        }
        if (gd.getNextBoolean()) {
            options |= CLEAR_WORKSHEET;
        } else {
            options &= ~CLEAR_WORKSHEET;
        }
        if (gd.getNextBoolean()) {
            options |= FLOOD_FILL;
        } else {
            options &= ~FLOOD_FILL;
        }
        if (gd.getNextBoolean()) {
            options |= DISPLAY_SUMMARY;
        } else {
            options &= ~DISPLAY_SUMMARY;
        }
        if (gd.getNextBoolean()) {
            options |= RECORD_STARTS;
        } else {
            options &= ~RECORD_STARTS;
        }
        if (gd.getNextBoolean()) {
            options |= SHOW_SIZE_DISTRIBUTION;
        } else {
            options &= ~SHOW_SIZE_DISTRIBUTION;
        }
        staticOptions = options;
        options |= SHOW_PROGRESS;
        if ((options & DISPLAY_SUMMARY) != 0 || (options & SHOW_SIZE_DISTRIBUTION) != 0) {
            Analyzer.setMeasurements(Analyzer.getMeasurements() | AREA);
        }
        return true;
    }

    /**
     * Performs particle analysis on the specified image. Returns false if there
     * is an error.
     */
    public boolean analyze(ImagePlus imp) {
        return analyze(imp, imp.getProcessor());
    }

    /**
     * Performs particle analysis on the specified ImagePlus and ImageProcessor.
     * Returns false if there is an error.
     */
    public boolean analyze(ImagePlus imp, ImageProcessor ip) {
        imageTitle = imp.getTitle(); /// GPEC mod - added
        showResults = (options & SHOW_RESULTS) != 0;
        excludeEdgeParticles = (options & EXCLUDE_EDGE_PARTICLES) != 0;
        showSizeDistribution = (options & SHOW_SIZE_DISTRIBUTION) != 0;
        resetCounter = (options & CLEAR_WORKSHEET) != 0;
        showProgress = (options & SHOW_PROGRESS) != 0;
        floodFill = (options & FLOOD_FILL) != 0;
        recordStarts = (options & RECORD_STARTS) != 0;
        displaySummary = (options & DISPLAY_SUMMARY) != 0;
        if ((options & SHOW_OUTLINES) != 0) {
            showChoice = OUTLINES;
        }
        if ((options & SHOW_NONE) != 0) {
            showChoice = NOTHING;
        }
        if ((options & SHOW_MASKS) != 0) /// GPEC mod - added
        {
            showChoice = MASKS;
        }
        if ((options & SHOW_OVERLAY) != 0) /// GPEC mod - added
        {
            showChoice = OVERLAY;
        }
        ip.snapshot();
        ip.setProgressBar(null);
        if (Analyzer.isRedirectImage()) {
            redirectImp = Analyzer.getRedirectImage(imp);
            if (redirectImp == null) {
                return false;
            }
            redirectIP = redirectImp.getProcessor();
        }
        if (!setThresholdLevels(imp, ip)) {
            return false;
        }
        width = ip.getWidth();
        height = ip.getHeight();
        //ImagePlus drawImp = new ImagePlus(); /// GPEC mod - added
        if (showChoice != NOTHING) {
            if (showChoice == OVERLAY) { /// GPEC mod - added
                //drawImp = NewImage.createRGBImage("Overlay", this.overlayImp.getWidth(), this.overlayImp.getHeight(), 1, NewImage.FILL_WHITE);
                //drawImp = this.overlayImp;
                drawIP = this.overlayImp.getProcessor();
                //drawIP.copyBits(this.overlayImp.getProcessor(), 0, 0, Blitter.COPY);
            } else {
                if (slice == 1) {
                    outlines = new ImageStack(width, height);
                }
                drawIP = new ByteProcessor(width, height);
            }
            if (showChoice == MASKS) {
                drawIP.invertLut();
            } else if (showChoice == OUTLINES) {
                if (customLut == null) {
                    makeCustomLut();
                }
                drawIP.setColorModel(customLut);
                drawIP.setFont(new Font("SansSerif", Font.PLAIN, 9));
            } else if (showChoice == OVERLAY) { /// GPEC mod - added
                drawIP.setFont(new Font("SansSerif", Font.PLAIN, 9));
            }
            if (showChoice != OVERLAY) {
                outlines.addSlice(null, drawIP);
                drawIP.setColor(Color.white);
                drawIP.fill();
                drawIP.setColor(Color.black);
            }
        }
        calibration = redirectImp != null ? redirectImp.getCalibration() : imp.getCalibration();

        if (rt == null) {
            rt = Analyzer.getResultsTable();
            analyzer = new Analyzer(imp);
        } else {
            analyzer = new Analyzer(imp, measurements, rt);
        }
        if (resetCounter && slice == 1) {
            if (!Analyzer.resetCounter()) {
                return false;
            }
        }
        beginningCount = Analyzer.getCounter();

        byte[] pixels = null;
        if (ip instanceof ByteProcessor) {
            pixels = (byte[]) ip.getPixels();
        }
        if (r == null) {
            r = ip.getRoi();
            mask = ip.getMask();
            if (displaySummary) {
                if (mask != null) {
                    totalArea = ImageStatistics.getStatistics(ip, AREA, calibration).area;
                } else {
                    totalArea = r.width * calibration.pixelWidth * r.height * calibration.pixelHeight;
                }
            }
        }
        minX = r.x;
        maxX = r.x + r.width;
        minY = r.y;
        maxY = r.y + r.height;
        if (r.width < width || r.height < height || mask != null) {
            if (!eraseOutsideRoi(ip, r, mask)) {
                return false;
            }
        }
        int offset;
        double value;
        int inc = Math.max(r.height / 25, 1);
        int mi = 0;
        if (recordStarts) {
            xStartC = getColumnID("XStart");
            yStartC = getColumnID("YStart");
        }
        ImageWindow win = imp.getWindow();
        if (win != null) {
            win.running = true;
        }
        if (measurements == 0) {
            measurements = Analyzer.getMeasurements();
        }
        if (showChoice == ELLIPSES) {
            measurements |= ELLIPSE;
        }
        /// GPEC mod - added GLCM
        roiNeedsImage = (measurements & PERIMETER) != 0 || (measurements & CIRCULARITY) != 0 || (measurements & FERET) != 0;
        //roiNeedsImage = (measurements&PERIMETER)!=0 || (measurements&CIRCULARITY)!=0 || (measurements&FERET)!=0 ||
        //        (measurements&GLCM_ASM)!=0 || (measurements&GLCM_CONTRAST)!=0 || (measurements&GLCM_CORRELATION)!=0 ||
        //        (measurements&GLCM_ENTROPY)!=0 || (measurements&GLCM_IDM)!=0 || (measurements&GLCM_SUM)!=0;                
        particleCount = 0;
        wand = new Wand(ip);
        pf = new PolygonFiller();
        if (floodFill) {
            ImageProcessor ipf = ip.duplicate();
            ipf.setValue(fillColor);
            ff = new FloodFiller(ipf);
        }

        /// GPEC mod
        if (this.applyFilters) {
            analyzer.applyFilters(this.filters);
        }
        if ((measurements & TEXTURE) != 0) {
            int degree = Integer.parseInt(textureParameters.get("degree").toString());
            int stepSize = Integer.parseInt(textureParameters.get("stepSize").toString());
            int grayLevels = Integer.parseInt(textureParameters.get("grayLevels").toString());
            int precision = Integer.parseInt(textureParameters.get("precision").toString());
            int computeFeatures = Integer.parseInt(textureParameters.get("computeFeatures").toString());
            Hashtable discriminantParameters = (Hashtable) textureParameters.get("discriminantParameters");
            analyzer.setTextureParameters(degree, stepSize, grayLevels, precision, computeFeatures, discriminantParameters);
        }
        analyzer.setMeanBackgroundGrayLevel(meanBackgroundGrayLevel);

        for (int y = r.y; y < (r.y + r.height); y++) {
            offset = y * width;
            for (int x = r.x; x < (r.x + r.width); x++) {
                if (pixels != null) {
                    value = pixels[offset + x] & 255;
                } else if (imageType == SHORT) {
                    value = ip.getPixel(x, y);
                } else {
                    value = ip.getPixelValue(x, y);
                }
                if (value >= level1 && value <= level2) {
                    analyzeParticle(x, y, imp, ip);
                }
            }
            if (showProgress && ((y % inc) == 0)) {
                IJ.showProgress((double) (y - r.y) / r.height);
            }
            if (win != null) {
                canceled = !win.running;
            }
            if (canceled) {
                Macro.abort();
                break;
            }
        }

        if (showProgress) {
            IJ.showProgress(1.0);
        }
        imp.killRoi();
        ip.resetRoi();
        ip.reset();
        if (displaySummary && processStack && IJ.getInstance() != null) {
            updateSliceSummary();
        }
        totalCount += particleCount;
        if (!canceled) {
            showResults();
        }
        if (showChoice == OVERLAY) {
            this.overlayImp.updateAndDraw();
            //this.overlayImp.show();
        }
        return true;
    }

    void updateSliceSummary() {
        float[] areas = rt.getColumn(ResultsTable.AREA);
        String label = imp.getStack().getShortSliceLabel(slice);
        label = label != null && !label.equals("") ? label : "" + slice;
        String aLine;
        if (areas != null) {
            double sum = 0.0;
            int start = areas.length - particleCount;
            if (start < 0) {
                return;
            }
            for (int i = start; i < areas.length; i++) {
                sum += areas[i];
            }
            int places = Analyzer.getPrecision();
            Calibration cal = imp.getCalibration();
            String total = "\t" + IJ.d2s(sum, places);
            String average = "\t" + IJ.d2s(sum / particleCount, places);
            String fraction = "\t" + IJ.d2s(sum * 100.0 / totalArea, 1);
            aLine = label + "\t" + particleCount + total + average + fraction;
        } else {
            aLine = label + "\t" + particleCount;
        }
        if (tw == null) {
            String title = "Summary of " + imp.getTitle();
            String headings = "Slice\tCount\tTotal Area\tAverage Size\tArea Fraction";
            tw = new TextWindow(title, headings, aLine, 180, 360);
        } else {
            tw.append(aLine);
        }
    }

    boolean eraseOutsideRoi(ImageProcessor ip, Rectangle r, ImageProcessor mask) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        ip.setRoi(r);
        if (excludeEdgeParticles && polygon != null) {
            ImageStatistics stats = ImageStatistics.getStatistics(ip, MIN_MAX, null);
            if (fillColor >= stats.min && fillColor <= stats.max) {
                double replaceColor = level1 - 1.0;
                if (replaceColor < 0.0 || replaceColor == fillColor) {
                    replaceColor = level2 + 1.0;
                    int maxColor = imageType == BYTE ? 255 : 65535;
                    if (replaceColor > maxColor || replaceColor == fillColor) {
                        IJ.error("Particle Analyzer", "Unable to remove edge particles");
                        return false;
                    }
                }
                for (int y = minY; y < maxY; y++) {
                    for (int x = minX; x < maxX; x++) {
                        int v = ip.getPixel(x, y);
                        if (v == fillColor) {
                            ip.putPixel(x, y, (int) replaceColor);
                        }
                    }
                }
            }
        }
        ip.setValue(fillColor);
        if (mask != null) {
            mask = mask.duplicate();
            mask.invert();
            ip.fill(mask);
        }
        ip.setRoi(0, 0, r.x, height);
        ip.fill();
        ip.setRoi(r.x, 0, r.width, r.y);
        ip.fill();
        ip.setRoi(r.x, r.y + r.height, r.width, height - (r.y + r.height));
        ip.fill();
        ip.setRoi(r.x + r.width, 0, width - (r.x + r.width), height);
        ip.fill();
        ip.resetRoi();
        //IJ.log("erase: "+fillColor+"  "+level1+"  "+level2+"  "+excludeEdgeParticles);
        //(new ImagePlus("ip2", ip.duplicate())).show();
        return true;
    }

    boolean setThresholdLevels(ImagePlus imp, ImageProcessor ip) {
        double t1 = ip.getMinThreshold();
        double t2 = ip.getMaxThreshold();
        boolean invertedLut = imp.isInvertedLut();
        boolean byteImage = ip instanceof ByteProcessor;
        if (ip instanceof ShortProcessor) {
            imageType = SHORT;
        } else if (ip instanceof FloatProcessor) {
            imageType = FLOAT;
        } else {
            imageType = BYTE;
        }
        if (t1 == ip.NO_THRESHOLD) {
            ImageStatistics stats = imp.getStatistics();
            if (imageType != BYTE || (stats.histogram[0] + stats.histogram[255] != stats.pixelCount)) {
                IJ.error("Particle Analyzer",
                        "A thresholded image or an 8-bit binary image is\n"
                        + "required. Refer to Image->Adjust->Threshold\n"
                        + "or to Process->Binary->Threshold.");
                canceled = true;
                return false;
            }
            if (invertedLut) {
                level1 = 255;
                level2 = 255;
                fillColor = 64;
            } else {
                level1 = 0;
                level2 = 0;
                fillColor = 192;
            }
        } else {
            level1 = t1;
            level2 = t2;
            if (imageType == BYTE) {
                if (level1 > 0) {
                    fillColor = 0;
                } else if (level2 < 255) {
                    fillColor = 255;
                }
            } else if (imageType == SHORT) {
                if (level1 > 0) {
                    fillColor = 0;
                } else if (level2 < 65535) {
                    fillColor = 65535;
                }
            } else if (imageType == FLOAT) {
                fillColor = -Float.MAX_VALUE;
            } else {
                return false;
            }
        }
        imageType2 = imageType;
        if (redirectIP != null) {
            if (redirectIP instanceof ShortProcessor) {
                imageType2 = SHORT;
            } else if (redirectIP instanceof FloatProcessor) {
                imageType2 = FLOAT;
            } else {
                imageType2 = BYTE;
            }
        }
        return true;
    }
    int counter = 0;

    void analyzeParticle(int x, int y, ImagePlus imp, ImageProcessor ip) {
        //Wand wand = new Wand(ip);
        ImageProcessor ip2 = redirectIP != null ? redirectIP : ip;
        wand.autoOutline(x, y, level1, level2);
        if (wand.npoints == 0) {
            IJ.log("wand error: " + x + " " + y);
            return;
        }
        Roi roi = new PolygonRoi(wand.xpoints, wand.ypoints, wand.npoints, Roi.TRACED_ROI);
        Rectangle r = roi.getBounds();
        if (r.width > 1 && r.height > 1) {
            PolygonRoi proi = (PolygonRoi) roi;
            pf.setPolygon(proi.getXCoordinates(), proi.getYCoordinates(), proi.getNCoordinates());
            ip2.setMask(pf.getMask(r.width, r.height));
            if (floodFill) {
                ff.particleAnalyzerFill(x, y, level1, level2, ip2.getMask(), r);
            }
        }
        ip2.setRoi(r);
        ip.setValue(fillColor);
        ImageStatistics stats = getStatistics(ip2, measurements, calibration);
        boolean include = true;
        if (excludeEdgeParticles) {
            if (r.x == minX || r.y == minY || r.x + r.width == maxX || r.y + r.height == maxY) {
                include = false;
            }
            if (polygon != null) {
                Rectangle bounds = roi.getBounds();
                int x1 = bounds.x + wand.xpoints[wand.npoints - 1];
                int y1 = bounds.y + wand.ypoints[wand.npoints - 1];
                int x2, y2;
                for (int i = 0; i < wand.npoints; i++) {
                    x2 = bounds.x + wand.xpoints[i];
                    y2 = bounds.y + wand.ypoints[i];
                    if (!polygon.contains(x2, y2)) {
                        include = false;
                        break;
                    }
                    if ((x1 == x2 && ip.getPixel(x1, y1 - 1) == fillColor) || (y1 == y2 && ip.getPixel(x1 - 1, y1) == fillColor)) {
                        include = false;
                        break;
                    }
                    x1 = x2;
                    y1 = y2;
                }
            }
        }
        ImageProcessor mask = ip2.getMask();
        if (stats.pixelCount >= minSize && stats.pixelCount <= maxSize && include) {
            if (roiNeedsImage) {
                roi.setImage(imp);
            }
            /// GPEC mod - added ip, mask as arguments     
            saveResults(stats, roi, imp, ip, mask);
            if (analyzer.isIncluded()) {
                particleCount++;
                if (showChoice != NOTHING) {
                    drawParticle(drawIP, roi, stats, mask);
                }
            }
        }
        if (redirectIP != null) {
            ip.setRoi(r);
        }
        ip.fill(mask);
    }

    /// GPEC mod - added accessor methods
    public void applyFilters(Hashtable filters) {
        this.filters = filters;
        this.applyFilters = true;
    }

    public void setOverlayImp(ImagePlus imp) {
        this.overlayImp = imp;
    }

    public void setOverlayColour(Color c) {
        this.overlayColour = c;
    }

    public Hashtable doTextureAnalysis(ImagePlus imp, ImageProcessor ip, Hashtable roiMap, int stepSize, int grayLevels) {
        Analyzer a = new Analyzer();
        int degree = Integer.parseInt(textureParameters.get("degree").toString());
        stepSize = Integer.parseInt(textureParameters.get("stepSize").toString());
        grayLevels = Integer.parseInt(textureParameters.get("grayLevels").toString());
        int precision = Integer.parseInt(textureParameters.get("precision").toString());
        int computeFeatures = Integer.parseInt(textureParameters.get("computeFeatures").toString());
        a.setTextureParameters(degree, stepSize, grayLevels, precision, computeFeatures, new Hashtable());
        return a.doTextureAnalysis(imp, ip, null, null);
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

    public ImagePlus getImage(String title) { // Obtains the specified ImagePlus object from hashtable given the title
        ImagePlus imp = null;
        if (images.containsKey(title)) {
            imp = (ImagePlus) images.get(title);
        }

        return imp;
    }

    ImageStatistics getStatistics(ImageProcessor ip, int mOptions, Calibration cal) {
        switch (imageType2) {
            case BYTE:
                return new ByteStatistics(ip, mOptions, cal);
            case SHORT:
                return new ShortStatistics(ip, mOptions, cal);
            case FLOAT:
                return new FloatStatistics(ip, mOptions, cal);
            default:
                return null;
        }
    }

    /**
     * Saves statistics for one particle in a results table. This is a method
     * subclasses may want to override.
     */
    /// GPEC mod - added ip, mask as argument
    protected void saveResults(ImageStatistics stats, Roi roi, ImagePlus imp, ImageProcessor ip, ImageProcessor mask) {
        analyzer.saveResults(stats, roi, imp, ip, mask); /// GPEC mod - added ip, mask as argument
        if (recordStarts) {
            int coordinates = ((PolygonRoi) roi).getNCoordinates();
            Rectangle r = roi.getBounds();
            int x = r.x + ((PolygonRoi) roi).getXCoordinates()[coordinates - 1];
            int y = r.y + ((PolygonRoi) roi).getYCoordinates()[coordinates - 1];
            rt.addValue(xStartC, x);
            rt.addValue(yStartC, y);
        }
        if (showResults) {
            analyzer.displayResults();
        }
    }

    /**
     * Draws a selected particle in a separate image. This is another method
     * subclasses may want to override.
     */
    protected void drawParticle(ImageProcessor drawIP, Roi roi,
            ImageStatistics stats, ImageProcessor mask) {
        switch (showChoice) {
            case MASKS:
                drawFilledParticle(drawIP, roi, mask);
                break;
            case OUTLINES:
                drawOutline(drawIP, roi, rt.getCounter());
                break;
            case ELLIPSES:
                drawEllipse(drawIP, stats, rt.getCounter());
                break;
            case OVERLAY:
                drawOverlay(drawIP, roi, rt.getCounter());
                break; /// GPEC mod - added
            default:
        }
    }

    void drawFilledParticle(ImageProcessor ip, Roi roi, ImageProcessor mask) {
        //IJ.write(roi.getBounds()+" "+mask.length);
        ip.setRoi(roi.getBounds());
        ip.fill(mask);
    }

    void drawOutline(ImageProcessor ip, Roi roi, int count) {
        Rectangle r = roi.getBounds();
        int nPoints = ((PolygonRoi) roi).getNCoordinates();
        int[] xp = ((PolygonRoi) roi).getXCoordinates();
        int[] yp = ((PolygonRoi) roi).getYCoordinates();
        int x = r.x, y = r.y;
        ip.setValue(0.0);
        ip.moveTo(x + xp[0], y + yp[0]);
        for (int i = 1; i < nPoints; i++) {
            ip.lineTo(x + xp[i], y + yp[i]);
        }
        ip.lineTo(x + xp[0], y + yp[0]);
        String s = IJ.d2s(count, 0);
        ip.moveTo(r.x + r.width / 2 - ip.getStringWidth(s) / 2, r.y + r.height / 2 + 4);
        ip.setValue(1.0);
        ip.drawString(s);
    }

    void drawEllipse(ImageProcessor ip, ImageStatistics stats, int count) {
        stats.drawEllipse(ip);
    }

    /// GPEC mod - added        
    void drawOverlay(ImageProcessor ip, Roi roi, int count) {
        Rectangle r = roi.getBounds();
        int nPoints = ((PolygonRoi) roi).getNCoordinates();
        int[] xp = ((PolygonRoi) roi).getXCoordinates();
        int[] yp = ((PolygonRoi) roi).getYCoordinates();
        int x = r.x, y = r.y;
        ip.setColor(this.overlayColour);
        ip.setLineWidth(1);
        ip.moveTo(x + xp[0], y + yp[0]);
        for (int i = 1; i < nPoints; i++) {
            ip.lineTo(x + xp[i], y + yp[i]);
        }
        ip.lineTo(x + xp[0], y + yp[0]);
        String s = IJ.d2s(count, 0);
        ip.moveTo(r.x + r.width / 2 - ip.getStringWidth(s) / 2, r.y + r.height / 2 + 4);
        ip.drawString(s);
    }

    void showResults() {
        int count = rt.getCounter();
        if (count == 0) {
            return;
        }
        boolean lastSlice = !processStack || slice == imp.getStackSize();
        if (displaySummary && lastSlice && rt == Analyzer.getResultsTable() && imp != null) {
            showSummary();
        }
        if (showSizeDistribution && lastSlice) {
            float[] areas = rt.getColumn(ResultsTable.AREA);
            if (areas != null) {
                ImageProcessor ip = new FloatProcessor(count, 1, areas, null);
                new HistogramWindow("Particle Size Distribution", new ImagePlus("", ip), sizeBins);
            }
        }
        if (outlines != null && lastSlice) {
            String title = imp != null ? imp.getTitle() : "Outlines";
            if (imageTitle != null && title.equals("Outlines")) {
                title = imageTitle;
            } /// GPEC mod - added
            String prefix = showChoice == MASKS ? "Mask of " : "Drawing of ";
            //new ImagePlus(prefix+title, outlines).show(); /// GPEC mod - commented out
            images.put(prefix + title, new ImagePlus(prefix + title, outlines));
        }
        if (showResults && !processStack) {
            Analyzer.firstParticle = beginningCount;
            Analyzer.lastParticle = Analyzer.getCounter() - 1;
        } else {
            Analyzer.firstParticle = Analyzer.lastParticle = 0;
        }
    }

    void showSummary() {
        String s = "";
        s += "Threshold: ";
        if ((int) level1 == level1 && (int) level2 == level2) {
            s += (int) level1 + "-" + (int) level2 + "\n";
        } else {
            s += IJ.d2s(level1, 2) + "-" + IJ.d2s(level2, 2) + "\n";
        }
        s += "Count: " + totalCount + "\n";
        float[] areas = rt.getColumn(ResultsTable.AREA);
        String aLine;
        if (areas != null) {
            double sum = 0.0;
            int start = areas.length - totalCount;
            if (start < 0) {
                return;
            }
            for (int i = start; i < areas.length; i++) {
                sum += areas[i];
            }
            int places = Analyzer.getPrecision();
            Calibration cal = imp.getCalibration();
            String unit = cal.getUnit();
            String total = IJ.d2s(sum, places);
            s += "Total Area: " + total + " " + unit + "^2\n";
            String average = IJ.d2s(sum / totalCount, places);
            s += "Average Size: " + IJ.d2s(sum / totalCount, places) + " " + unit + "^2\n";
            if (processStack) {
                totalArea *= imp.getStackSize();
            }
            String fraction = IJ.d2s(sum * 100.0 / totalArea, 1);
            s += "Area Fraction: " + fraction + "%";
            aLine = " " + "\t" + totalCount + "\t" + total + "\t" + average + "\t" + fraction;
        } else {
            aLine = " " + "\t" + totalCount;
        }
        if (tw != null) {
            tw.append("");
            tw.append(aLine);
        } else {
            new TextWindow("Summary of " + imp.getTitle(), s, 300, 200);
        }
    }

    int getColumnID(String name) {
        int id = rt.getFreeColumn(name);
        if (id == ResultsTable.COLUMN_IN_USE) {
            id = rt.getColumnIndex(name);
        }
        return id;
    }

    void makeCustomLut() {
        IndexColorModel cm = (IndexColorModel) LookUpTable.createGrayscaleColorModel(false);
        byte[] reds = new byte[256];
        byte[] greens = new byte[256];
        byte[] blues = new byte[256];
        cm.getReds(reds);
        cm.getGreens(greens);
        cm.getBlues(blues);
        reds[1] = (byte) 255;
        greens[1] = (byte) 0;
        blues[1] = (byte) 0;
        customLut = new IndexColorModel(8, 256, reds, greens, blues);
    }

    /**
     * Called once when ImageJ quits.
     */
    public static void savePreferences(Properties prefs) {
        prefs.put(OPTIONS, Integer.toString(staticOptions));
        prefs.put(BINS, Integer.toString(staticBins));
    }
}
