/*
 * responsible for generating bootstrap samples
 */
package ca.ubc.gpec.ia.analyzer.stats;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

/**
 *
 * @author samuelc
 */
public class BootstrapSampler {

    public static final int DATA_INDEX = 0;
    public static final int WEIGHT_INDEX = 1;
    
    private Random randomNumberGenerator;
    private double[] data;
    private double[] weights; // optional weights for each data point
    
    /**
     * constructor - default random seed = current UTC time in milliseconds
     *
     * @param inputData
     * @param inputWeights - weights for items in data
     */
    public BootstrapSampler(double[] inputData, double[] inputWeights) {
        randomNumberGenerator = new Random(Calendar.getInstance().getTimeInMillis());
        data = Arrays.copyOf(inputData, inputData.length);
        weights = Arrays.copyOf(inputWeights, inputWeights.length);
    }

    /**
     * set random seed and (re)initialize randomNumberGenerator
     *
     * @param seed
     */
    public void setSeed(int seed) {
        randomNumberGenerator = new Random(seed);
    }

    /**
     * set and overwrite data
     *
     * @param inputData
     * @param inputWeights - weights for items in data
     */
    public void setData(double[] inputData, double[] inputWeights) {
        data = Arrays.copyOf(inputData, inputData.length);
        weights = Arrays.copyOf(inputWeights, inputWeights.length);
    }

    /**
     * return a bootstrap sample - with replacement - return data with length
     * same as data
     *
     * @return 2-D array ... [DATA_INDEX][i] = data and [WEIGHT_INDEX][i] = weight
     */
    public double[][] sample() {
        int len = data.length;
        double[][] bootstrapData = new double[2][len];
        for (int i = 0; i < len; i++) {
            int nextInt = randomNumberGenerator.nextInt(len);
            bootstrapData[DATA_INDEX][i] = data[nextInt];
            bootstrapData[WEIGHT_INDEX][i] = weights[nextInt];
        }
        return bootstrapData;
    }
}
