/*
 * capture some generic (unspecified) errors during transformation
 */
package ca.ubc.gpec.ia.analyzer.transformation;

/**
 *
 * @author samuelc
 */
public class ImageTransformationException extends Exception {
    public ImageTransformationException(String msg) {
        super(msg);
    }
}
