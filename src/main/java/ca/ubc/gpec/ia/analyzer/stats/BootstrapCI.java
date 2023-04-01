/*
 * responsible for calculating bootstrap confidence interval
 */
package ca.ubc.gpec.ia.analyzer.stats;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import org.apache.commons.math3.stat.descriptive.WeightedEvaluation;

/**
 *
 * @author samuelc
 */
public class BootstrapCI {

    public static final int DEFAULT_NUMBER_OF_BOOTSTRAP_ITERATIONS = 100000;
    public static final double DEFAULT_LOWER_CI_LEVEL = 0.025;
    public static final double DEFAULT_UPPER_CI_LEVEL = 0.975;
    public static final int USE_DEFAULT_RANDOM_SEED = -1; // flag to indicate use BootstrapSampler's default random seed
    // the object to calculate the statistics of interest e.g. mean
    private WeightedEvaluation stats;
    private int numBoot;
    private double lowerCILevel;
    private double upperCILevel;
    private double observedValue;
    private double lowerCI;
    private double upperCI;
    private double[] data; // original data
    private double[] weights; // weight for data points in data 
    private double[][][] bootstrapSamples; // 3-D array to store the bootstrap samples (with weights)
    private double[] bootstrapValues; // statistics on bootstrap samples

    /**
     *
     * @param abstractStorelessUnivariateStatistic
     * @param numBoot
     * @param lowerCILevel
     * @param upperCILevel
     * @param seed
     */
    public BootstrapCI(
            WeightedEvaluation weightedEvaluation,
            double[] data,
            double[] weights,
            int numBoot,
            double lowerCILevel,
            double upperCILevel,
            int seed) {
        stats = weightedEvaluation;
        this.data = Arrays.copyOf(data, data.length);
        this.weights = Arrays.copyOf(weights, weights.length);
        this.numBoot = numBoot;
        this.lowerCILevel = lowerCILevel;
        this.upperCILevel = upperCILevel;

        // calculate observed value ...
        observedValue = stats.evaluate(this.data, this.weights);

        // calculate boostrap samples ...
        BootstrapSampler bootstrapSampler = new BootstrapSampler(this.data, this.weights);
        if (seed != USE_DEFAULT_RANDOM_SEED) {
            bootstrapSampler.setSeed(seed);
        }

        // note: 3D array index = [bootstrap sample index][DATA_INDEX/WEIGHT_INDEX][data point index]
        bootstrapSamples = new double[this.numBoot][][];
        bootstrapValues = new double[this.numBoot];
        for (int i = 0; i < this.numBoot; i++) {
            bootstrapSamples[i] = bootstrapSampler.sample();
            bootstrapValues[i] = stats.evaluate(bootstrapSamples[i][BootstrapSampler.DATA_INDEX], bootstrapSamples[i][BootstrapSampler.WEIGHT_INDEX]);
        }

        Arrays.sort(bootstrapValues);

        lowerCI = bootstrapValues[(int) Math.floor(this.lowerCILevel * ((double) this.numBoot))];
        upperCI = bootstrapValues[(int) Math.ceil(this.upperCILevel * ((double) this.numBoot))];

    }

    /**
     * return observed value
     *
     * @return
     */
    public double getObservedValue() {
        return observedValue;
    }

    /**
     * get lower CI
     *
     * @return
     */
    public double getLowerCI() {
        return lowerCI;
    }

    /**
     * get upper CI
     *
     * @return
     */
    public double getUpperCI() {
        return upperCI;
    }

    /**
     * return the original data
     *
     * @return
     */
    public double[] getOriginalData() {
        return data;
    }
    
    /**
     * return estimate from bootstrap samples
     * 
     * @return 
     */
    public double[] getBootstrapValues() {
        return this.bootstrapValues;
    }

    /**
     * write estimates from bootstrap samples to output stream
     *
     * @param o
     * @param separator - separator character
     */
    public void exportBootstrapSamples(OutputStream o, String separator) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(o);
        // no header
        for (int i = 0; i < bootstrapSamples.length; i++) {
            for (int j = 0; j < bootstrapSamples[0][BootstrapSampler.DATA_INDEX].length; j++) {
                osw.write("" + bootstrapSamples[i][BootstrapSampler.DATA_INDEX][j]);
                if (j < (bootstrapSamples[0][BootstrapSampler.DATA_INDEX].length-1)) {
                    osw.write(separator);
                }
            }
            osw.write(System.getProperty("line.separator"));
        }
        osw.close();
    }
}
