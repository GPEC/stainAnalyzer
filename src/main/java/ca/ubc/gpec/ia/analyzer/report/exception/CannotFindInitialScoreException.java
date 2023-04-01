/*
 * exception to capture the scenario when the inital score annotation
 * is not found
 */

package ca.ubc.gpec.ia.analyzer.report.exception;

/**
 *
 * @author samuelc
 */
public class CannotFindInitialScoreException extends RuntimeException {
    public CannotFindInitialScoreException(String msg) {
        super(msg);
    }
}
