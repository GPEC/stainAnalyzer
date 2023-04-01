/**
 * HaralickTexture.java
 *
 * Created on August 17, 2005
 * 
 * @author Andy Chan, Genetic Pathology Evaluation Centre, University of British Columbia
 * @author Dmitry Turbin, MD/PhD, Genetic Pathology Evaluation Centre, University of British Columbia
 *
 * Performs texture analysis. It computes 22 parameters:
 * - The first 21 of these are features described by Haralick (1), with the exception of 'Correlation' which is calculated according to the formulae in (2).
 * - The last one (GLCM Sum) is simply a checksum to ensure the gray level matrix has normalized probabilities added up to 1.
 *
 * References:
 * (1) Haralick RM, Shanmugam K, Dinstein I. Textural Features for Image Classification. IEEE Transactions on Systems, Man and Cybernetics, 1973, p.610-621.
 * (2) Pressman NJ. Markovian analysis of cervical cell images. J Histochem Cytochem 24:138-144, 1976.
 * (3) Chan HP, Sahiner B, Petrick N, Helvie MA, Lam, KL, Adler DD and Goodsitt MM. Computerized Classification of Malignant and Benign Microcalcifications on Mammograms: Texture Analysis using an Artificial Neural Network, Phys Med Biol, 42:549-567, 1997.
 */

package ca.ubc.gpec.ia.analyzer.processing;

// Import ImageJ libraries
import ij.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;
import ij.measure.ResultsTable;

// Import Java libraries
import java.util.Hashtable;
import java.util.ArrayList; // not used, but present in the original version

// Import JAMA libraries for matrix calculations
import Jama.Matrix;
import Jama.EigenvalueDecomposition;

public class HaralickTexture {
    // Constants for degrees
    public static final int DEG_0=1, DEG_45=2, DEG_90=4, DEG_135=8;
    public static final int DEG_AVG=DEG_0+DEG_45+DEG_90+DEG_135;
    private final int [] degrees = {DEG_0, DEG_45, DEG_90, DEG_135, DEG_AVG};     
    private final String [] degreeTitles = {"0", "45", "90", "135", "Average"};    
    
    // Constants for texture features
    public static final int DISCRIMINANT_CONSTANT=0;
    public static final int ANGULAR_SECOND_MOMENT=1, CONTRAST=2, CORRELATION=4, DIFFERENCE_MOMENT=8,
            INVERSE_DIFFERENCE_MOMENT=16, SUM_AVERAGE=32, SUM_VARIANCE=64, SUM_ENTROPY=128, ENTROPY=256,
            DIFFERENCE_VARIANCE=512, DIFFERENCE_ENTROPY=1024, INFORMATION_MEASURE_A=2048, INFORMATION_MEASURE_B=4096, 
            MAXIMAL_CORRELATION_COEFFICIENT=8192, COEFFICIENT_OF_VARIATION=16384, PEAK_TRANSITION_PROBABILITY=0x8000,
            DIAGONAL_VARIANCE=0x10000, DIAGONAL_MOMENT=0x20000, SECOND_DIAGONAL_MOMENT=0x40000, PRODUCT_MOMENT=0x80000,
            TRIANGULAR_SYMMETRY=0x100000, GLCM_SUM=0x200000;
    public static final int ALL_FEATURES = ANGULAR_SECOND_MOMENT + CONTRAST + CORRELATION + DIFFERENCE_MOMENT +
            INVERSE_DIFFERENCE_MOMENT + SUM_AVERAGE + SUM_VARIANCE + SUM_ENTROPY + ENTROPY +
            DIFFERENCE_VARIANCE + DIFFERENCE_ENTROPY + INFORMATION_MEASURE_A + INFORMATION_MEASURE_B +
            MAXIMAL_CORRELATION_COEFFICIENT + COEFFICIENT_OF_VARIATION + PEAK_TRANSITION_PROBABILITY +
            DIAGONAL_VARIANCE + DIAGONAL_MOMENT + SECOND_DIAGONAL_MOMENT + PRODUCT_MOMENT +
            TRIANGULAR_SYMMETRY + GLCM_SUM;
    private final int [] features = {ANGULAR_SECOND_MOMENT, CONTRAST, CORRELATION, DIFFERENCE_MOMENT,
            INVERSE_DIFFERENCE_MOMENT, SUM_AVERAGE, SUM_VARIANCE, SUM_ENTROPY, ENTROPY,
            DIFFERENCE_VARIANCE, DIFFERENCE_ENTROPY, INFORMATION_MEASURE_A, INFORMATION_MEASURE_B,
            MAXIMAL_CORRELATION_COEFFICIENT, COEFFICIENT_OF_VARIATION, PEAK_TRANSITION_PROBABILITY, 
            DIAGONAL_VARIANCE, DIAGONAL_MOMENT, SECOND_DIAGONAL_MOMENT, PRODUCT_MOMENT,
            TRIANGULAR_SYMMETRY, GLCM_SUM};   
    private final String [] featureTitles = {"Angular_Second_Moment", "Contrast", "Correlation", "Difference_Moment",
            "Inverse_Difference_Moment", "Sum_Average", "Sum_Variance", "Sum_Entropy", "Entropy",
            "Difference_Variance", "Difference_Entropy", "Information_Measure_A", "Information_Measure_B", 
            "Maximal_Correlation_Coefficient", "Coefficient_Of_Variation", "Peak_Transition_Probability", 
            "Diagonal_Variance", "Diagonal_Moment", "Second_Diagonal_Moment", "Product_Moment",
            "Triangular_Symmetry", "GLCM_Sum"};
    
    // Attributes
    private int degree = DEG_AVG;           // Integer representing the direction to traverse when constructing the gray level matrix.
    private String degChoice = "Average";   // String representing direction to traverse when constructing the gray level matrix. 
    private int stepSize = 1;               // The distance between the 1st and 2nd pixel.
    private int grayLevels = 8;             // The number of gray levels used in the gray level matrix based on fixed normalization. For example, with raw gray level intensities of 0 to 255 and number of gray levels set to 8, there will be 32 intensities per gray level, such that intensities of 0 to 31 will be set to gray level 0, intensities 32 to 63 will be set to gray level 1 and so on.
    private int precision = 3;              // Designates the number of decimal places in the analysis results.
    private int computeFeatures = ALL_FEATURES - MAXIMAL_CORRELATION_COEFFICIENT; // The features to be calculated

    private Hashtable lut;                  // A look up table matching gray level intensities to gray levels
    private ResultsTable rt;                // The results of analysis represented as ImageJ's ResultTable object
    private Hashtable allResults = new Hashtable(); // Stores all the analysis results
    
    /**
     * Default constructor.
     */
    public HaralickTexture() {super();}
    
    /**
     * Constructor allowing specification of a number of properties.
     *
     * @param   degree  Integer representing the direction to traverse when constructing the gray level matrix.    
     * @param   stepSize    The distance between the 1st and 2nd pixel.
     * @param   grayLevels  The number of gray levels used in the gray level matrix based on fixed normalization. For example, with raw gray level intensities of 0 to 255 and number of gray levels set to 8, there will be 32 intensities per gray level, such that intensities of 0 to 31 will be set to gray level 0, intensities 32 to 63 will be set to gray level 1 and so on.
     * @param   precision   Designates the number of decimal places in the analysis results.
     * @param   computeFeatures The features to be calculated.
     */    
    public HaralickTexture(int degree, int stepSize, int grayLevels, int precision, int computeFeatures) {
        super();
        this.degree = degree;
        this.stepSize = stepSize;
        this.grayLevels = grayLevels;
        this.precision = precision;
        this.computeFeatures = computeFeatures;
        
        degChoice = getDegreeTitle(degree);
    }

    /**
     * API function for performing analysis on the specific image. A ROI or mask could also be specified.
     *
     * @param   imp The ImagePlus object representing the image to be analyzed.   
     * @param   ip  The ImageProcessor object of the ImagePlus object.
     * @param   roi Optionally specify a region of interest to analyze.
     * @param   mask    Optionally specify a mask for the region to be analyzed.
     */        
    public void analyzeImage (ImagePlus imp, ImageProcessor ip, Roi roi, ImageProcessor mask) {                    
        IJ.showStatus("Analyzing texture of " + imp.getTitle() + "...");       
        //ImageConverter ic = new ImageConverter(imp); // First convert image to 8-bit
        //ic.convertToGray8();          
        Hashtable results = doAnalyses(imp, ip, roi, mask);  
        allResults.put(imp.getTitle(), results); // Store the analysis results
        
        // add results to results table for display purpose 
        addToResultsTable(imp.getTitle() + "[" + degChoice + "]", results);  
    }

    /**
     * Get the entire set of results
     *
     * @return  A hashtable object that contains all analysis results with the image name as the key
     */        
    public Hashtable getResults () { // 
        return allResults;
    }

    /**
     * Get the results for the particular image
     *
     * @param   title   Specify the title of the image.
     * @return  A hashtable object that contains the analysis results of the image.   
     */        
    public Hashtable getResults (String title) {
        Hashtable results = null;
        if (allResults.containsKey(title)) {
            results = (Hashtable)allResults.get(title);
        }
            
        return results;
    }
    
    /**
     * Display the analysis results in ImageJ.
     */
    public void displayResults () {
        rt.show("Haralick texture analysis results");
    }    
    
    // Public accessors
    /**
     * Returns the value of the degrees attribute.
     *
     * @return  An array of integers of all available degrees. 
     */
    public int [] getDegrees () {return degrees;}
    
    /**
     * Returns the value of the degreeTitles attribute.
     *
     * @return  An array of strings of all available degrees. 
     */    
    public String [] getDegreeTitles () {return degreeTitles;}
    
    /**
     * Returns the degree as a string given the integer representation of the degree.
     *
     * @param   degree  The integer representation of the degree.  
     * @return  The string representation of the degree.
     */       
    public String getDegreeTitle (int degree) {
        String title = null;
        for (int i=0; i<degrees.length; i++) {
            if (degrees[i] == degree) {
                title = degreeTitles[i];
                break;
            }
        }
        
        return title;
    }        
    
    /**
     * Returns the degree as an integer given the string representation of the degree.
     *
     * @param   title  The string representation of the degree.
     * @return  The integer representation of the degree.
     */    
    public int getDegree (String title) {
        int degree1 = 0;
        for (int i=0; i<degreeTitles.length; i++) {
            if (degreeTitles[i].equals(title)) {
                degree1 = degrees[i];
                break;
            }
        }
        
        return degree1;
    }    
    
    /**
     * Returns the value of the features attribute.
     *
     * @return  An array of integers of all available features. 
     */    
    public int [] getFeatures () {return features;}
    
    /**
     * Returns the value of the featureTitles attribute.
     *
     * @return  An array of strings of all available features.
     */     
    public String [] getFeatureTitles () {return featureTitles;}
    
    /**
     * Returns the feature as a string given the integer representation of the feature.
     *
     * @param   feature  The integer representation of the feature.
     * @return  The string representation of the feature.
     */        
    public String getFeatureTitle (int feature) {
        String title = null;
        for (int i=0; i<features.length; i++) {
            if (features[i] == feature) {
                title = featureTitles[i];
                break;
            }
        }
        
        return title;
    }    
    
    /**
     * Returns the feature as an integer given the string representation of the feature.
     *
     * @param   title  The string representation of the feature.
     * @return  The integer representation of the feature.
     */   
    public int getFeature (String title) {
        int feature = 0;
        for (int i=0; i<featureTitles.length; i++) {
            if (featureTitles[i].equals(title)) {
                feature = features[i];
                break;
            }
        }
        
        return feature;        
    }
    
    /**
     * Perform analysis of all degrees on the specific image. A ROI or mask could also be specified.
     *
     * @param   imp The ImagePlus object representing the image to be analysed.   
     * @param   ip  The ImageProcessor object of the ImagePlus object.
     * @param   roi Optionally specify a region of interest to analyse.
     * @param   mask    Optionally specify a mask for the region to be analysed.
     * @return  A hashtable containing the analysis results.
     */           
    private Hashtable doAnalyses(ImagePlus imp, ImageProcessor ip, Roi roi, ImageProcessor mask) { // Do the texture analysis
        Hashtable lut = setupLut(); // Setup the look up table for gray levels       
        byte [] pixels =(byte []) ip.getPixels(); // Get all the pixels of the current image

        // Now perform degree-specific texture analysis
        Hashtable results = new Hashtable();  
        switch (degree) {
            case DEG_0: results = doAnalysis(imp, ip, roi, mask, pixels, DEG_0); break;
            case DEG_45: results = doAnalysis(imp, ip, roi, mask, pixels, DEG_45); break;
            case DEG_90: results = doAnalysis(imp, ip, roi, mask, pixels, DEG_90); break;
            case DEG_135: results = doAnalysis(imp, ip, roi, mask, pixels, DEG_135); break;
            case DEG_AVG: // Calculate the average from all degrees
                Hashtable [] resultsArray = new Hashtable[degrees.length - 1];
                for (int i=0; i<degrees.length - 1; i++) {        
                    resultsArray[i] = doAnalysis(imp, ip, roi, mask, pixels, degrees[i]);  
                }
                for (int i=0; i<features.length; i++) { // Calculate the mean for each feature
                    int feature = features[i];
                    if ((computeFeatures & feature) != 0) {
                        double sum = 0.0;
                        for (int j=0; j<resultsArray.length; j++) { // Obtain feature value from each degree
                            sum += Double.parseDouble(resultsArray[j].get(new Integer(feature)).toString());
                        }
                        results.put(new Integer(feature), new Double(sum / resultsArray.length)); // Now can store the average
                    }
                }
                break;
        }
         
        return results;
    }
    
    /**
     * Perform analysis of a particular degree on the specific image. A ROI or mask could also be specified.
     *
     * @param   imp The ImagePlus object representing the image to be analysed.   
     * @param   ip  The ImageProcessor object of the ImagePlus object.
     * @param   roi Optionally specify a region of interest to analyse.
     * @param   mask    Optionally specify a mask for the region to be analysed.
     * @param   pixels  An array of bytes representing the pixel values of the image.
     * @param   deg The integer representation of the degree to be used for the analysis.
     * @return  A hashtable containing the analysis results.
     */            
    private Hashtable doAnalysis(ImagePlus imp, ImageProcessor ip, Roi roi, ImageProcessor mask, byte [] pixels, int deg) {
        double [][] matrix = setupMatrix(); // Setup/initialise the co-occurence matrix
        computeMatrix(imp, ip, roi, mask, matrix, pixels, deg); // Compute the normalised probability matrix    
        Hashtable results = new Hashtable();
        for (int i=0; i<features.length; i++) {
            int feature = features[i];
            if ((computeFeatures & feature) != 0) {
                // Compute the individual features
                results.put(new Integer(feature), new Double(computeFeature(matrix, feature)));
            }
        }        
        
//        displayMatrix(matrix);
        return results;        
    }    
    
    /**
     * Construct the lookup table which maps gray level intensities to gray levels based on fixed normalisation. 
     * For example, with raw gray level intensities of 0 to 255 and number of gray levels set to 8,
     * there will be 32 intensities per gray level, such that intensities of 0 to 31 will be set to gray level 0,
     * intensities 32 to 63 will be set to gray level 1 and so on.
     *
     * @return  A hashtable representing the lookup table.
     */    
    private Hashtable setupLut () { 
        lut = new Hashtable(); 
        double grayPerLevel = 256 / grayLevels;
        int currLevel = 0;
        int mappedLevel = 0;
        for (int i=0; i<=255; i++) {
            if (i >= ((currLevel+1) * grayPerLevel)) {currLevel++;}
            mappedLevel = currLevel;

            lut.put(new Integer(i), new Integer(mappedLevel));
            //System.out.println(i + "->" + mappedLevel);
        }
        
        return lut;
    }
    
    /**
     * Initialise a grayLevels x grayLevels co-occurence matrix.
     *
     * @return  An array of doubles of i,j pairs.
     */     
    private double [][] setupMatrix() {
        double [][] matrix = new double [grayLevels][grayLevels];
        // i & j are as PL(i/j) where i is stepSize away from j
        for (int i=0; i<grayLevels; i++)  { // Now initialise all pixel pairs to zero count
            for (int j=0; j<grayLevels; j++) {
                matrix[i][j] = 0;
            }
        }
        
        return matrix;
    }
    
    /**
     * Perform the degree specific matrix construction
     *
     * @param   imp The ImagePlus object representing the image to be analysed.   
     * @param   ip  The ImageProcessor object of the ImagePlus object.
     * @param   roi Optionally specify a region of interest to analyse.
     * @param   mask    Optionally specify a mask for the region to be analysed.
     * @param   matrix  A graylevel x graylevel co-occurence matrix that is initialised.
     * @param   pixels  An array of bytes representing the pixel values of the image.
     * @param   deg The integer representation of the degree to be used for the analysis.
     */        
    private void computeMatrix(ImagePlus imp, ImageProcessor ip, Roi roi, ImageProcessor mask, double [][] matrix, byte [] pixels, int deg) { 
        int i=0, j=0;   // i & j are as PL(i/j) where i is stepSize away from j
        int n=0;        // n is the number of ij pairs
        int offset=0;   // offset is the number of pixels from beginning of the row
        int pos=0;      // position of the pixel from 0,0
        
//        ImageProcessor ip = imp.getProcessor();                                                  
        int imgWidth = ip.getWidth();
        if (roi == null) {roi = imp.getRoi();}        
        Rectangle r = null;
        if (roi != null) {r = roi.getBoundingRect();} // Get the bounding rectangle of the ROI
        else {r = ip.getRoi();}                                                  
        
        // Construct the count of each i,j pairs
        for (int y=r.y; y<(r.y+r.height); y++) {
            offset = y*imgWidth;
            for (int x=r.x; x<(r.x+r.width); x++) {
                // First need to see if i is within the roi
                if (isInsideROI(imp, roi, mask, x, y)) {                    
                    int j_x = 0;
                    int j_y = 0;
                    if (deg == DEG_0) {j_x=x+stepSize; j_y=y;}                  // 2nd pixel is east of 1st pixel 
                    else if (deg == DEG_45) {j_x=x+stepSize; j_y=y-stepSize;}   // 2nd pixel is northeast of 1st pixel
                    else if (deg == DEG_90) {j_x=x; j_y=y-stepSize;}            // 2nd pixel is north of 1st pixel
                    else if (deg == DEG_135) {j_x=x-stepSize; j_y=y-stepSize;}  // 2nd pixel is northwest of 1st pixel
                    
                    // Now need to see if j is within the roi
                    if (isInsideROI(imp, roi, mask, j_x, j_y)) {
                        pos = offset + x;
                        i = 0xff & pixels[pos];
                        j = 0xff & (ip.getPixel(j_x, j_y));                        
                        
                        i = Integer.parseInt(lut.get(new Integer(i)).toString());
                        j = Integer.parseInt(lut.get(new Integer(j)).toString());
                        
                        matrix[i][j]++;
                        matrix[j][i]++; // double counting in both directions to make matrix symmetrical
                        n += 2;
                    }
                }
            }
        }

        // Now divide the count by total number of pixels to get a normalized matrix
        for (i=0; i<grayLevels; i++)  {
            for (j=0; j<grayLevels; j++) {
                matrix[i][j] = (matrix[i][j])/(n);
            }
        }
    }
    
    /**
     * Calculate the specific texture feature given the normalised matrix.
     *
     * @param   matrix  A graylevel x graylevel co-occurence matrix that is already normalised.
     * @param   feature The integer representation of the feature to be calculated.
     * @return  A double that represents the value of the calculated feature.
     */           
    private double computeFeature(double [][] matrix, int feature) {
        double value = 0.0;
        
        if (feature == CONTRAST) {     
            for (int n=0; n<grayLevels; n++) {
                double sum = 0.00;                
                for (int i=0; i<grayLevels; i++) {
                    for (int j=0; j<grayLevels; j++) {
                        if (Math.abs(i-j) == n) {sum += matrix[i][j];}
                    }
                }
                value += Math.pow(n, 2) * sum;
            }
        } else if (feature == SUM_AVERAGE || feature == SUM_ENTROPY || feature == SUM_VARIANCE) {
            double [] sum = new double[2*grayLevels];
            for (int k=0; k<=2*grayLevels-2; k++) {sum[k] = 0.0;} // Entitles the elements to zero
            for (int i=0; i<grayLevels; i++) {
                for (int j=0; j<grayLevels; j++) {
                    sum[i+j] += matrix[i][j];
                }
            }
            
            for (int k=0; k<=2*grayLevels-2; k++) {
                switch (feature) {
                    case SUM_VARIANCE: // Need to calculate Sum Average first to get Sum Variance                   
                    case SUM_AVERAGE: value += k * sum[k]; break;
                    case SUM_ENTROPY: if (sum[k] != 0) {value -= sum[k] * Math.log10(sum[k]);} break;                        
                }
            }  
            
            if (feature == SUM_VARIANCE) {
                double sum_average = value;
                value = 0.0;
                for (int k=0; k<=2*grayLevels-2; k++) {
                    value += Math.pow(k-sum_average, 2) * sum[k];
                }
            }
        } else if (feature == DIFFERENCE_ENTROPY || feature == DIFFERENCE_VARIANCE) {
            double [] diff = new double[grayLevels];
            for (int k=0; k<grayLevels; k++) {diff[k] = 0.0;} // Initialize the elements to zero
            for (int i=0; i<grayLevels; i++) {
                for (int j=0; j<grayLevels; j++) {
                    diff[Math.abs(i-j)] += matrix[i][j];
                }
            }
            
            for (int k=0; k<grayLevels; k++) {
                switch (feature) { // Need to calculate Difference Average first to get Difference Variance
                    case DIFFERENCE_VARIANCE: value += k * diff[k]; break;
                    case DIFFERENCE_ENTROPY: if (diff[k] != 0) {value -= diff[k] * Math.log10(diff[k]);} break;                        
                }
            }  
            
            if (feature == DIFFERENCE_VARIANCE) {
                double diff_average = value;
                value = 0.0;
                for (int k=0; k<grayLevels; k++) {
                    value += Math.pow(k-diff_average, 2) * diff[k];
                }
            }
        } else if (feature == CORRELATION || feature == INFORMATION_MEASURE_A || feature == INFORMATION_MEASURE_B ||
                feature == MAXIMAL_CORRELATION_COEFFICIENT) {
            double [] px = new double[grayLevels]; // 
            double [] py = new double[grayLevels];
            for (int index=0; index<grayLevels; index++) {px[index] = 0.0; py[index] = 0.0;} // Inititalize the elements to zero
            
            for (int i=0; i<grayLevels; i++) { // Calculate px(i) and py(j)
                for (int j=0; j<grayLevels; j++) {
                    px[i] += matrix[i][j];
                    py[j] += matrix[i][j];
                }
            }
            
            if (feature == CORRELATION) {
                double meanX = 0.0, meanY = 0.0, stdevX = 0.0, stdevY = 0.0;
                for (int i=0; i<grayLevels; i++) { // Calculate mean of px(i)
                    meanX += i * px[i];
                }
                
                for (int i=0; i<grayLevels; i++) { // Calculate variance of px(i)
                    stdevX += Math.pow(i-meanX, 2) * px[i];
                }
                stdevX = Math.sqrt(stdevX); // Standard deviation is square root of variance
                
                // Note: since our matrix is always square & symmetrical, mean of py(j) will be equal to mean of px(i), and
                // standard deviation of py(j) will be equal to standard deviation of px(i)
                meanY = meanX;
                stdevY = stdevX;
                
                for (int i=0; i<grayLevels; i++) { // Now finally we can calculate the correlation
                    for (int j=0; j<grayLevels; j++) {
                        value += ((i-meanX) * (j-meanY) * matrix[i][j] / (stdevX * stdevY));
                    }
                }             
            } else if (feature == INFORMATION_MEASURE_A || feature == INFORMATION_MEASURE_B) {
                double entropy = computeFeature(matrix, ENTROPY);
                double hx = 0.0, hy = 0.0;
                
                for (int index=0; index<grayLevels; index++) {
                    if (px[index] != 0) {hx -= px[index] * Math.log10(px[index]);} 
                    if (py[index] != 0) {hy -= py[index] * Math.log10(py[index]);} 
                }
                
                double hxy1 = 0.0, hxy2 = 0.0, product = 0.0;
                for (int i=0; i<grayLevels; i++) { // Calculate Hxy1 or Hxy2
                    for (int j=0; j<grayLevels; j++) {
                        product = px[i] * py[j];
                        switch (feature) {
                            case INFORMATION_MEASURE_A: if (product != 0) {hxy1 -= matrix[i][j] * Math.log10(product);} break;
                            case INFORMATION_MEASURE_B: if (product != 0) {hxy2 -= product * Math.log10(product);} break;
                        }
                    }
                }
                
                switch (feature) { // Now can calculate information measure A or B
                    case INFORMATION_MEASURE_A: value = (entropy - hxy1) / (Math.max(hx, hy)); break;
                    case INFORMATION_MEASURE_B: value = Math.sqrt(1 - Math.exp(-2 * (hxy2 - entropy))); break;
                }
            } else if (feature == MAXIMAL_CORRELATION_COEFFICIENT) {
                double [][] Q = new double[grayLevels][grayLevels]; // Q matrix
                for (int i=0; i<grayLevels; i++) { // Populate the Q matrix
                    for (int j=0; j<grayLevels; j++) {
                        Q[i][j] = 0;
                        for (int k=0; k<grayLevels; k++) {
                            Q[i][j] += (matrix[i][k] * matrix[j][k]) / (px[i] * py[k]);
                        }
                    }
                }
                
                // Obtain the eigenvalues of Q using the Jama libraries
                Matrix m = new Matrix(Q, grayLevels, grayLevels);
                EigenvalueDecomposition E = new EigenvalueDecomposition(m);
                double[] eigenValues = E.getRealEigenvalues();  
                
                java.util.Arrays.sort(eigenValues); // Sort the array of eigenvalues
                value = Math.sqrt(eigenValues[eigenValues.length-2]); // Square root of the second largest eigenvalue
            }
        } else if (feature == COEFFICIENT_OF_VARIATION || feature == DIAGONAL_VARIANCE || feature == PRODUCT_MOMENT) {
            double mean = 0.0, variance = 0.0;
            // For symmetrical matrix, mean ,variance & standard deviation calculated from i as reference pixel 
            // is same as using j as reference pixel. Hence using i alone is good enough
            for (int i=0; i<grayLevels; i++) { // Calculate the mean
                for (int j=0; j<grayLevels; j++) {
                    mean += i * matrix[i][j];
                }
            }    
            
            for (int i=0; i<grayLevels; i++) { 
                for (int j=0; j<grayLevels; j++) {
                    switch (feature) {
                        case COEFFICIENT_OF_VARIATION:
                        case DIAGONAL_VARIANCE: variance += Math.pow(i-mean, 2) * matrix[i][j]; break; // Calculate the variance
                        case PRODUCT_MOMENT: value += (i-mean) * (j-mean) * matrix[i][j];
                    }
                }
            }
            
            switch (feature) {
                case COEFFICIENT_OF_VARIATION: value = (Math.sqrt(variance)) / mean; break; // (standard deviation) / (mean)
                case DIAGONAL_VARIANCE: value = variance; break;
            }
        } else {
            for (int i=0; i<grayLevels; i++) {
                for (int j=0; j<grayLevels; j++) {
                    switch (feature) {
                        case ANGULAR_SECOND_MOMENT: value += Math.pow(matrix[i][j], 2); break;
                        case DIFFERENCE_MOMENT: value += Math.pow(i-j, 2) * matrix[i][j]; break;
                        case INVERSE_DIFFERENCE_MOMENT: value += matrix[i][j] / (1 + Math.pow(i-j, 2)); break;
                        case ENTROPY: if (matrix[i][j] != 0) {value -= matrix[i][j] * Math.log10(matrix[i][j]);} break;
                        case PEAK_TRANSITION_PROBABILITY: if (matrix[i][j] > value) {value = matrix[i][j];} break;
                        case DIAGONAL_MOMENT: value += Math.sqrt(0.5 * Math.abs(i-j) * matrix[i][j]); break;
                        case SECOND_DIAGONAL_MOMENT: value += 0.5 * Math.abs(i-j) * matrix[i][j]; break;
                        case TRIANGULAR_SYMMETRY: value += Math.abs(matrix[i][j] - matrix[j][i]); break;
                        case GLCM_SUM: value += matrix[i][j]; break;
                    }
                }
            }
        }
        
        return value;
    }

    /**
     * Determine whether a given pixel is within the region of interest.
     *
     * @param   imp The ImagePlus object representing the image.   
     * @param   roi Specify the region of interest.
     * @param   mask    Optionally specify a mask for the region.
     * @param   x   The x-coordinate of the pixel of interest.
     * @param   y   The y-coordinate of the pixel of interest.
     * @return  A boolean indicating whether the pixel is within the region of interest.
     */         
    private boolean isInsideROI (ImagePlus imp, Roi roi, ImageProcessor mask, int x, int y) { // Determine whether a given pixel is within the ROI
        boolean isInside = false;

        if (mask != null) { // Use mask generated by Analyze Particles
            int roiWidth = (int)roi.getBoundingRect().getWidth();
            int roiHeight = (int)roi.getBoundingRect().getHeight();
            int roiX = roi.getBoundingRect().x;
            int roiY = roi.getBoundingRect().y;
            if (mask.getWidth() == roiWidth && mask.getHeight() == roiHeight && y >= roiY && x >= roiX && y < (roiY + roiHeight) && x < (roiX + roiWidth)) {
                byte[] mpixels = (byte[])mask.getPixels();
                int my = y - roiY;                
                int mi = (my * roiWidth) + (x - roiX);
                //try {
                    if (mpixels[mi]!=0) {isInside = true;}
                //}
                //catch (ArrayIndexOutOfBoundsException e) {
                    //System.out.println(x + "," + y + "(" + roiX + "," + roiY + "):" + mi);
                //}
            }
        } else if (roi != null) {
            try {isInside = roi.contains(x, y);} 
            catch (NullPointerException e) {
                // Somehow for polygon calling roi.contains() throws NullPointerException
                // So call the contains() method of the Polygon object directly              
                switch (roi.getType()) {
                    case Roi.POLYGON:
                    case Roi.FREEROI:
                    case Roi.TRACED_ROI:
                    case Roi.POLYLINE:
                    case Roi.FREELINE:
                    case Roi.ANGLE:
                    case Roi.POINT: isInside = roi.getPolygon().contains(x, y); break;
                    default: isInside = false; break;
                }
            }
        } else { // No real region of interest - just return true
            isInside = true;
        }
        
        return isInside;
    }   
    
    /**
     * Add the results in Hashtable format to the ResultsTable object.
     *
     * @param   rowLabel    The label of the row.
     * @param   results A hashtable consists of the texture feature results.
     */          
    private void addToResultsTable (String rowLabel, Hashtable results) {
        if (rt == null) {
            rt = new ResultsTable();
            rt.setPrecision(precision);      
        }
        
        rt.incrementCounter();   
        if (rowLabel != null) {rt.addLabel("Object", rowLabel);}
        
        for (int i=0; i<features.length; i++) {
            int feature = features[i];
            if (results.containsKey(new Integer(feature))) {  
                rt.addValue(featureTitles[i], Double.parseDouble(results.get(new Integer(feature)).toString()));
            }
        }        
    }

    // The following functions are for testing/debugging purposes...
    /**
     * Display i/j pairs of the matrix with non-zero values using System.out.println().
     *
     * @param   matrix  An array of doubles corresponds to the gray level matrix.
     */       
    private void displayMatrix(double [][] matrix) { // Display i/j pairs with non-zero values
        System.out.println("Display non-zero values in matrix...");
        for (int i=0; i<grayLevels; i++) {
            for (int j=0; j<grayLevels; j++) {
                double value = matrix[i][j];
                if (value > 0) {
                    System.out.println("(" + i + "," + j + "): " + value);
                }
            }
        }
    }    
    
    /**
     * Display the results of texture feature calculations using System.out.println().
     *
     * @param   matrix  An array of doubles corresponds to the gray level matrix.
     */       
    private void displayResults (double [][] matrix) {             
        String output = "";
        for (int i=0; i<featureTitles.length; i++) {
            int feature = features[i];
            if ((computeFeatures & feature) != 0) {
                output += featureTitles[i] + ": " + computeFeature(matrix, feature) + "\r\n";
            }
        }
 
        System.out.println(output);      
    }
}

