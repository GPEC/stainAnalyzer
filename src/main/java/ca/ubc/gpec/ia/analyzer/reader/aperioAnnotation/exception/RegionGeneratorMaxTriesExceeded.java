/*
 * trying to generate random ROI, but max try exceeded!!!
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception;

/**
 *
 * @author samuelc
 */
public class RegionGeneratorMaxTriesExceeded extends Exception {
    public RegionGeneratorMaxTriesExceeded(String msg) {
        super(msg);
    }
}
