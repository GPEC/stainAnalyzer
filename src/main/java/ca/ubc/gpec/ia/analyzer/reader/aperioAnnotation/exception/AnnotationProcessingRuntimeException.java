/*
 * something went wrong while trying to process annotation
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception;

/**
 *
 * @author samuelc
 */
public class AnnotationProcessingRuntimeException extends RuntimeException {
    public AnnotationProcessingRuntimeException(String msg) {
        super(msg);
    }
}
