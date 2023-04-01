/*
 * to capture anything that went wrong when working with randomVirtualTmaCores annotation
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception;

/**
 *
 * @author samuelc
 */
public class RandomVirtualTmaCoresRuntimeException extends RuntimeException {
    public RandomVirtualTmaCoresRuntimeException(String msg) {
        super(msg);
    }
}
