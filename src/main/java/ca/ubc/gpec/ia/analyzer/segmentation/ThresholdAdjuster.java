//package ij.plugin.frame; /// GPEC mod
package ca.ubc.gpec.ia.analyzer.segmentation;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.frame.Recorder;
import ij.plugin.frame.PlugInFrame;
import ij.plugin.frame.PasteController;

/// GPEC mod
//import ij.plugin.Thresholder; /// commented out
import ca.ubc.gpec.ia.analyzer.segmentation.Thresholder;

/**
 * Adjusts the lower and upper threshold levels of the active image. This class
 * is multi-threaded to provide a more responsive user interface.
 */
public class ThresholdAdjuster extends PlugInFrame implements PlugIn, Measurements,
        Runnable, ActionListener, AdjustmentListener, ItemListener {

    static final int RED = 0, BLACK_AND_WHITE = 1, OVER_UNDER = 2;
    public static final int CENTER = 0, TOP_RIGHT = 1; /// GPEC mod - added window positions
    static final String[] modes = {"Red", "Black & White", "Over/Under"};
    static final double defaultMinThreshold = 85;
    static final double defaultMaxThreshold = 170;
    static boolean fill1 = true;
    static boolean fill2 = true;
    static boolean useBW = true;
    static boolean backgroundToNaN = true;
    static Frame instance;
    static int mode = RED;
    ThresholdPlot plot = new ThresholdPlot();
    Thread thread;
    int minValue = -1;
    int maxValue = -1;
    int sliderRange = 256;
    boolean doAutoAdjust, doReset, doApplyLut, doStateChange, doSet;
    Panel panel;
    Button autoB, resetB, applyB, setB;
    int previousImageID;
    int previousImageType;
    double previousMin, previousMax;
    ImageJ ij;
    double minThreshold, maxThreshold;  // 0-255
    Scrollbar minSlider, maxSlider;
    Label label1, label2;
    boolean done;
    boolean invertedLut;
    int lutColor;
    static Choice choice;
    boolean firstActivation;
    double minAutoThreshold, maxAutoThreshold; /// GPEC mod

    public ThresholdAdjuster() {
        super("Threshold");
        if (instance != null) {
            instance.toFront();
            return;
        }

        WindowManager.addWindow(this);
        instance = this;
        setLutColor(mode);
        IJ.register(PasteController.class);

        ij = IJ.getInstance();
        Font font = new Font("SansSerif", Font.PLAIN, 10);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        // plot
        int y = 0;
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(10, 10, 0, 10);
        add(plot, c);

        // minThreshold slider
        minSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange / 3, 1, 0, sliderRange);
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.weightx = IJ.isMacintosh() ? 90 : 100;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 10, 0, 0);
        add(minSlider, c);
        minSlider.addAdjustmentListener(this);
        minSlider.setUnitIncrement(1);

        // minThreshold slider label
        c.gridx = 1;
        c.gridwidth = 1;
        c.weightx = IJ.isMacintosh() ? 10 : 0;
        c.insets = new Insets(5, 0, 0, 10);
        label1 = new Label("       ", Label.RIGHT);
        label1.setFont(font);
        add(label1, c);

        // maxThreshold slider
        maxSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange * 2 / 3, 1, 0, sliderRange);
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 1;
        c.weightx = 100;
        c.insets = new Insets(0, 10, 0, 0);
        add(maxSlider, c);
        maxSlider.addAdjustmentListener(this);
        maxSlider.setUnitIncrement(1);

        // maxThreshold slider label
        c.gridx = 1;
        c.gridwidth = 1;
        c.weightx = 0;
        c.insets = new Insets(0, 0, 0, 10);
        label2 = new Label("       ", Label.RIGHT);
        label2.setFont(font);
        add(label2, c);

        // choice
        choice = new Choice();
        for (int i = 0; i < modes.length; i++) {
            choice.addItem(modes[i]);
        }
        choice.select(mode);
        choice.addItemListener(this);
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        c.insets = new Insets(5, 5, 0, 5);
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        add(choice, c);

        // buttons
        int trim = IJ.isMacOSX() ? 11 : 0;
        panel = new Panel();
        autoB = new TrimmedButton("Auto", trim);
        autoB.addActionListener(this);
        autoB.addKeyListener(ij);
        panel.add(autoB);
        applyB = new TrimmedButton("Apply", trim);
        applyB.addActionListener(this);
        applyB.addKeyListener(ij);
        panel.add(applyB);
        resetB = new TrimmedButton("Reset", trim);
        resetB.addActionListener(this);
        resetB.addKeyListener(ij);
        panel.add(resetB);
        setB = new TrimmedButton("Set", trim);
        setB.addActionListener(this);
        setB.addKeyListener(ij);
        panel.add(setB);
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        c.insets = new Insets(0, 5, 10, 5);
        add(panel, c);

        addKeyListener(ij);  // ImageJ handles keyboard shortcuts
        pack();
        GUI.center(this); // commented out
        firstActivation = true;
        show();

        thread = new Thread(this, "ThresholdAdjuster");
        //thread.setPriority(thread.getPriority()-1);
        thread.start();
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null) {
            setup(imp);
        }
    }

    /// GPEC mod - new constructor
    public ThresholdAdjuster(ImagePlus imp) {
        super("Threshold");
        ImageStatistics stats = plot.setHistogram(imp);
        ImageProcessor ip = imp.getProcessor();
        autoSetLevels(ip, stats);
    }

    /// GPEC mod - added method to position window
    public void positionWindow(int p) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension window = this.getSize();
        if (window.width == 0) {
            return;
        }
        int left = 0;
        int top = 0;

        if (p == CENTER) {
            left = screen.width / 2 - window.width / 2;
            top = (screen.height - window.height) / 4;
        } else if (p == TOP_RIGHT) {
            left = screen.width - window.width;
            top = 0;
        }

        if (top < 0) {
            top = 0;
        }
        this.setLocation(left, top);
    }

    public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource() == minSlider) {
            minValue = minSlider.getValue();
        } else {
            maxValue = maxSlider.getValue();
        }
        notify();
    }

    public synchronized void actionPerformed(ActionEvent e) {
        Button b = (Button) e.getSource();
        if (b == null) {
            return;
        }
        if (b == resetB) {
            doReset = true;
        } else if (b == autoB) {
            doAutoAdjust = true;
        } else if (b == applyB) {
            doApplyLut = true;
        } else if (b == setB) {
            doSet = true;
        }
        notify();
    }

    void setLutColor(int mode) {
        switch (mode) {
            case RED:
                lutColor = ImageProcessor.RED_LUT;
                break;
            case BLACK_AND_WHITE:
                lutColor = ImageProcessor.BLACK_AND_WHITE_LUT;
                break;
            case OVER_UNDER:
                lutColor = ImageProcessor.OVER_UNDER_LUT;
                break;
        }
    }

    public synchronized void itemStateChanged(ItemEvent e) {
        mode = choice.getSelectedIndex();
        setLutColor(mode);
        doStateChange = true;
        notify();
    }

    /// GPEC mod - made public
    public ImageProcessor setup(ImagePlus imp) {
        ImageProcessor ip;
        int type = imp.getType();
        if (type == ImagePlus.COLOR_RGB) {
            return null;
        }
        ip = imp.getProcessor();
        boolean minMaxChange = false;
        boolean not8Bits = type == ImagePlus.GRAY16 || type == ImagePlus.GRAY32;
        if (not8Bits) {
            if (ip.getMin() != previousMin || ip.getMax() != previousMax) {
                minMaxChange = true;
            }
            previousMin = ip.getMin();
            previousMax = ip.getMax();
        }
        int id = imp.getID();
        if (minMaxChange || id != previousImageID || type != previousImageType) {
            //IJ.log(minMaxChange +"  "+ (id!=previousImageID)+"  "+(type!=previousImageType));
            if (not8Bits && minMaxChange) {
                ip.resetMinAndMax();
                imp.updateAndDraw();
            }
            invertedLut = imp.isInvertedLut();
            minThreshold = ip.getMinThreshold();
            maxThreshold = ip.getMaxThreshold();
            ImageStatistics stats = plot.setHistogram(imp);
            if (minThreshold == ip.NO_THRESHOLD) {
                autoSetLevels(ip, stats);
            } else {
                minThreshold = scaleDown(ip, minThreshold);
                maxThreshold = scaleDown(ip, maxThreshold);
            }
            scaleUpAndSet(ip, minThreshold, maxThreshold);
            updateLabels(imp, ip);
            updatePlot();
            updateScrollBars();
            imp.updateAndDraw();
        }
        previousImageID = id;
        previousImageType = type;
        return ip;
    }

    void autoSetLevels(ImageProcessor ip, ImageStatistics stats) {
        if (stats == null || stats.histogram == null) {
            minThreshold = defaultMinThreshold;
            maxThreshold = defaultMaxThreshold;
            return;
        }
        int threshold = ip.getAutoThreshold(stats.histogram);
        //IJ.log(threshold+" "+stats.min+" "+stats.max+" "+stats.dmode);
        if ((stats.max - stats.dmode) > (stats.dmode - stats.min)) {
            minThreshold = threshold;
            maxThreshold = stats.max;
        } else {
            minThreshold = stats.min;
            maxThreshold = threshold;
        }
        if (Recorder.record) {
            Recorder.record("setAutoThreshold");
        }

        /// GPEC mod
        minAutoThreshold = minThreshold;
        maxAutoThreshold = maxThreshold;
    }

    /**
     * Scales threshold levels in the range 0-255 to the actual levels.
     */
    void scaleUpAndSet(ImageProcessor ip, double minThreshold, double maxThreshold) {
        if (!(ip instanceof ByteProcessor) && minThreshold != ImageProcessor.NO_THRESHOLD) {
            double min = ip.getMin();
            double max = ip.getMax();
            if (max > min) {
                minThreshold = min + (minThreshold / 255.0) * (max - min);
                maxThreshold = min + (maxThreshold / 255.0) * (max - min);
            } else {
                minThreshold = ImageProcessor.NO_THRESHOLD;
            }
        }
        ip.setThreshold(minThreshold, maxThreshold, lutColor);
    }

    /**
     * Scales a threshold level to the range 0-255.
     */
    double scaleDown(ImageProcessor ip, double threshold) {
        if (ip instanceof ByteProcessor) {
            return threshold;
        }
        double min = ip.getMin();
        double max = ip.getMax();
        if (max > min) {
            return ((threshold - min) / (max - min)) * 255.0;
        } else {
            return ImageProcessor.NO_THRESHOLD;
        }
    }

    /**
     * Scales a threshold level in the range 0-255 to the actual level.
     */
    double scaleUp(ImageProcessor ip, double threshold) {
        double min = ip.getMin();
        double max = ip.getMax();
        if (max > min) {
            return min + (threshold / 255.0) * (max - min);
        } else {
            return ImageProcessor.NO_THRESHOLD;
        }
    }

    void updatePlot() {
        plot.minThreshold = minThreshold;
        plot.maxThreshold = maxThreshold;
        plot.mode = mode;
        plot.repaint();
    }

    void updateLabels(ImagePlus imp, ImageProcessor ip) {
        double min = ip.getMinThreshold();
        double max = ip.getMaxThreshold();
        if (min == ImageProcessor.NO_THRESHOLD) {
            label1.setText("");
            label2.setText("");
        } else {
            Calibration cal = imp.getCalibration();
            if (cal.calibrated()) {
                min = cal.getCValue((int) min);
                max = cal.getCValue((int) max);
            }
            if (((int) min == min && (int) max == max) || (ip instanceof ShortProcessor)) {
                label1.setText("" + (int) min);
                label2.setText("" + (int) max);
            } else {
                label1.setText("" + IJ.d2s(min, 2));
                label2.setText("" + IJ.d2s(max, 2));
            }
        }
    }

    void updateScrollBars() {
        minSlider.setValue((int) minThreshold);
        maxSlider.setValue((int) maxThreshold);
    }

    /**
     * Restore image outside non-rectangular roi.
     */
    void doMasking(ImagePlus imp, ImageProcessor ip) {
        ImageProcessor mask = imp.getMask();
        if (mask != null) {
            ip.reset(mask);
        }
    }

    /// GPEC mod - made public
    public void adjustMinThreshold(ImagePlus imp, ImageProcessor ip, double value) {
        if (IJ.altKeyDown()) {
            double width = maxThreshold - minThreshold;
            if (width < 1.0) {
                width = 1.0;
            }
            minThreshold = value;
            maxThreshold = minThreshold + width;
            if ((minThreshold + width) > 255) {
                minThreshold = 255 - width;
                maxThreshold = minThreshold + width;
                minSlider.setValue((int) minThreshold);
            }
            maxSlider.setValue((int) maxThreshold);
            scaleUpAndSet(ip, minThreshold, maxThreshold);
            return;
        }
        minThreshold = value;
        if (maxThreshold < minThreshold) {
            maxThreshold = minThreshold;
            maxSlider.setValue((int) maxThreshold);
        }
        scaleUpAndSet(ip, minThreshold, maxThreshold);
    }

    /// GPEC mod - made public
    public void adjustMaxThreshold(ImagePlus imp, ImageProcessor ip, int cvalue) {
        maxThreshold = cvalue;
        if (minThreshold > maxThreshold) {
            minThreshold = maxThreshold;
            minSlider.setValue((int) minThreshold);
        }
        scaleUpAndSet(ip, minThreshold, maxThreshold);
    }

    void reset(ImagePlus imp, ImageProcessor ip) {
        plot.setHistogram(imp);
        ip.resetThreshold();
        updateScrollBars();
        if (Recorder.record) {
            Recorder.record("resetThreshold");
        }
    }

    void doSet(ImagePlus imp, ImageProcessor ip) {
        double level1 = ip.getMinThreshold();
        double level2 = ip.getMaxThreshold();
        if (level1 == ImageProcessor.NO_THRESHOLD) {
            level1 = scaleUp(ip, defaultMinThreshold);
            level2 = scaleUp(ip, defaultMaxThreshold);
        }
        Calibration cal = imp.getCalibration();
        int digits = (ip instanceof FloatProcessor) || cal.calibrated() ? 2 : 0;
        level1 = cal.getCValue(level1);
        level2 = cal.getCValue(level2);
        GenericDialog gd = new GenericDialog("Set Threshold Levels");
        gd.addNumericField("Lower Threshold Level: ", level1, digits);
        gd.addNumericField("Upper Threshold Level: ", level2, digits);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        level1 = gd.getNextNumber();
        level2 = gd.getNextNumber();
        level1 = cal.getRawValue(level1);
        level2 = cal.getRawValue(level2);
        if (level2 < level1) {
            level2 = level1;
        }
        double minDisplay = ip.getMin();
        double maxDisplay = ip.getMax();
        ip.resetMinAndMax();
        double minValue = ip.getMin();
        double maxValue = ip.getMax();
        if (level1 < minValue) {
            level1 = minValue;
        }
        if (level2 > maxValue) {
            level2 = maxValue;
        }
        boolean outOfRange = level1 < minDisplay || level2 > maxDisplay;
        if (outOfRange) {
            plot.setHistogram(imp);
        } else {
            ip.setMinAndMax(minDisplay, maxDisplay);
        }

        minThreshold = scaleDown(ip, level1);
        maxThreshold = scaleDown(ip, level2);
        scaleUpAndSet(ip, minThreshold, maxThreshold);
        updateScrollBars();
        if (Recorder.record) {
            if (imp.getBitDepth() == 32) {
                Recorder.record("setThreshold", ip.getMinThreshold(), ip.getMaxThreshold());
            } else {
                int min = (int) ip.getMinThreshold();
                int max = (int) ip.getMaxThreshold();
                if (cal.isSigned16Bit()) {
                    min = (int) cal.getCValue(level1);
                    max = (int) cal.getCValue(level2);
                }
                Recorder.record("setThreshold", min, max);
            }
        }
        close(); /// GPEC mod - added           
    }

    void changeState(ImagePlus imp, ImageProcessor ip) {
        scaleUpAndSet(ip, minThreshold, maxThreshold);
        updateScrollBars();
    }

    void autoThreshold(ImagePlus imp, ImageProcessor ip) {
        ip.resetThreshold();
        previousImageID = 0;
        setup(imp);
    }

    void apply(ImagePlus imp) {
        try {
            if (imp.getBitDepth() == 32) {
                GenericDialog gd = new GenericDialog("NaN Backround");
                gd.addCheckbox("Set Background Pixels to NaN", backgroundToNaN);
                gd.showDialog();
                if (gd.wasCanceled()) {
                    runThresholdCommand();
                    return;
                }
                backgroundToNaN = gd.getNextBoolean();
                if (backgroundToNaN) {
                    IJ.run("NaN Background");
                } else {
                    runThresholdCommand();
                }
            } else {
                runThresholdCommand();
            }
        } catch (Exception e) {/*
             * do nothing
             */

        }
        close(); /// GPEC mod - uncommented

    }

    void runThresholdCommand() {
        Recorder.recordInMacros = true;
        /// GPEC mod
        //IJ.run("Threshold", "skip"); 
        Thresholder t = new Thresholder();
        t.run("skip");
        Recorder.recordInMacros = false;
    }
    static final int RESET = 0, AUTO = 1, HIST = 2, APPLY = 3, STATE_CHANGE = 4, MIN_THRESHOLD = 5, MAX_THRESHOLD = 6, SET = 7;

    // Separate thread that does the potentially time-consuming processing 
    public void run() {
        while (!done) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
            doUpdate();
        }
    }

    /// GPEC mod - made public
    public void doUpdate() {
        ImagePlus imp;
        ImageProcessor ip;
        int action;
        int min = minValue;
        int max = maxValue;
        if (doReset) {
            action = RESET;
        } else if (doAutoAdjust) {
            action = AUTO;
        } else if (doApplyLut) {
            action = APPLY;
        } else if (doStateChange) {
            action = STATE_CHANGE;
        } else if (doSet) {
            action = SET;
        } else if (minValue >= 0) {
            action = MIN_THRESHOLD;
        } else if (maxValue >= 0) {
            action = MAX_THRESHOLD;
        } else {
            return;
        }
        minValue = -1;
        maxValue = -1;
        doReset = false;
        doAutoAdjust = false;
        doApplyLut = false;
        doStateChange = false;
        doSet = false;
        imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.beep();
            IJ.showStatus("No image");
            return;
        }
        ip = setup(imp);
        if (ip == null) {
            imp.unlock();
            IJ.beep();
            IJ.showStatus("RGB images cannot be thresholded");
            return;
        }
        //IJ.write("setup: "+(imp==null?"null":imp.getTitle()));
        switch (action) {
            case RESET:
                reset(imp, ip);
                break;
            case AUTO:
                autoThreshold(imp, ip);
                break;
            case APPLY:
                apply(imp);
                break;
            case STATE_CHANGE:
                changeState(imp, ip);
                break;
            case SET:
                doSet(imp, ip);
                break;
            case MIN_THRESHOLD:
                adjustMinThreshold(imp, ip, min);
                break;
            case MAX_THRESHOLD:
                adjustMaxThreshold(imp, ip, max);
                break;
        }
        updatePlot();
        updateLabels(imp, ip);
        ip.setLutAnimation(true);
        imp.updateAndDraw();
    }

    public void windowClosing(WindowEvent e) {
        close();
    }

    /**
     * Overrides close() in PlugInFrame.
     */
    public void close() {
        super.close();
        instance = null;
        done = true;
        synchronized (this) {
            notify();
        }
    }

    public void windowActivated(WindowEvent e) {
        super.windowActivated(e);
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp != null) {
            if (!firstActivation) {
                previousImageID = 0;
                setup(imp);
            }
            firstActivation = false;
        }
    }

    /// GPEC mod - added accessor methods
    public double getMinThreshold() {
        return minThreshold;
    }

    public double getMaxThreshold() {
        return maxThreshold;
    }

    public double getMinAutoThreshold() {
        return minAutoThreshold;
    }

    public double getMaxAutoThreshold() {
        return maxAutoThreshold;
    }

    public void doApply() {
        doApplyLut = true;
    }
} // ThresholdAdjuster class

class ThresholdPlot extends Canvas implements Measurements, MouseListener {

    static final int WIDTH = 256, HEIGHT = 48;
    double minThreshold = 85;
    double maxThreshold = 170;
    int[] histogram;
    Color[] hColors;
    int hmax;
    Image os;
    Graphics osg;
    int mode;

    public ThresholdPlot() {
        addMouseListener(this);
        setSize(WIDTH + 1, HEIGHT + 1);
    }

    /**
     * Overrides Component getPreferredSize(). Added to work around a bug in
     * Java 1.4.1 on Mac OS X.
     */
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH + 1, HEIGHT + 1);
    }

    ImageStatistics setHistogram(ImagePlus imp) {
        ImageProcessor ip = imp.getProcessor();
        if (!(ip instanceof ByteProcessor)) {
            double min = ip.getMin();
            double max = ip.getMax();
            ip.setMinAndMax(min, max);
            ip = new ByteProcessor(ip.createImage());
        }
        ip.setRoi(imp.getRoi());
        ImageStatistics stats = ImageStatistics.getStatistics(ip, AREA + MIN_MAX + MODE, null);
        int maxCount2 = 0;
        histogram = stats.histogram;
        for (int i = 0; i < stats.nBins; i++) {
            if ((histogram[i] > maxCount2) && (i != stats.mode)) {
                maxCount2 = histogram[i];
            }
        }
        hmax = stats.maxCount;
        if ((hmax > (maxCount2 * 2)) && (maxCount2 != 0)) {
            hmax = (int) (maxCount2 * 1.5);
            histogram[stats.mode] = hmax;
        }
        os = null;

        ColorModel cm = ip.getColorModel();
        if (!(cm instanceof IndexColorModel)) {
            return null;
        }
        IndexColorModel icm = (IndexColorModel) cm;
        int mapSize = icm.getMapSize();
        if (mapSize != 256) {
            return null;
        }
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];
        icm.getReds(r);
        icm.getGreens(g);
        icm.getBlues(b);
        hColors = new Color[256];
        for (int i = 0; i < 256; i++) {
            hColors[i] = new Color(r[i] & 255, g[i] & 255, b[i] & 255);
        }
        return stats;
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        if (histogram != null) {
            if (os == null && hmax > 0) {
                os = createImage(WIDTH, HEIGHT);
                osg = os.getGraphics();
                osg.setColor(Color.white);
                osg.fillRect(0, 0, WIDTH, HEIGHT);
                osg.setColor(Color.gray);
                for (int i = 0; i < WIDTH; i++) {
                    if (hColors != null) {
                        osg.setColor(hColors[i]);
                    }
                    osg.drawLine(i, HEIGHT, i, HEIGHT - ((int) (HEIGHT * histogram[i]) / hmax));
                }
                osg.dispose();
            }
            g.drawImage(os, 0, 0, this);
        } else {
            g.setColor(Color.white);
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }
        g.setColor(Color.black);
        g.drawRect(0, 0, WIDTH, HEIGHT);
        if (mode == ThresholdAdjuster.RED) {
            g.setColor(Color.red);
        } else if (mode == ThresholdAdjuster.OVER_UNDER) {
            g.setColor(Color.blue);
            g.drawRect(1, 1, (int) minThreshold - 2, HEIGHT);
            g.drawRect(1, 0, (int) minThreshold - 2, 0);
            g.setColor(Color.green);
            g.drawRect((int) maxThreshold + 1, 1, WIDTH - (int) maxThreshold, HEIGHT);
            g.drawRect((int) maxThreshold + 1, 0, WIDTH - (int) maxThreshold, 0);
            return;
        }
        g.drawRect((int) minThreshold, 1, (int) (maxThreshold - minThreshold), HEIGHT);
        g.drawLine((int) minThreshold, 0, (int) maxThreshold, 0);
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }
} // ThresholdPlot class
