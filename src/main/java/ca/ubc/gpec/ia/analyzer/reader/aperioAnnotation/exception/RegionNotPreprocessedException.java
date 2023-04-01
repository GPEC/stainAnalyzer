/*
 * calling method from Region but have not preprocess data in Region
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception;

/**
 *
 * @author samuelc
 */
public class RegionNotPreprocessedException extends Exception {
    public RegionNotPreprocessedException(String msg) {
        super(msg);
    }
}
