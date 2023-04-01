/*
 * trying to write an image with unsupported file format
 */
package ca.ubc.gpec.ia.analyzer.writer;

/**
 *
 * @author samuelc
 */
public class ImageExporterUnsupportedFileFormatException extends Exception {
    public ImageExporterUnsupportedFileFormatException(String msg) {
        super(msg);
    }
}
