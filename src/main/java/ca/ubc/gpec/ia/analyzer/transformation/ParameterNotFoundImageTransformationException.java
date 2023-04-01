/*
 * image transformation parameter not found
 */
package ca.ubc.gpec.ia.analyzer.transformation;

/**
 *
 * @author samuelc
 */
public class ParameterNotFoundImageTransformationException extends ImageTransformationException {
    public ParameterNotFoundImageTransformationException(String msg) {
        super(msg);
    }
}
