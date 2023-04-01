/*
 * to capture anything that may go wrong when reading virtual files
 */
package ca.ubc.gpec.ia.analyzer.reader;

/**
 *
 * @author samuelc
 */
public class VirtualSlideReaderException extends Exception {
    public VirtualSlideReaderException(String msg) {
        super(msg);
    }
    
}
