/*
 * to indicate that additional scoring is required
 */
package ca.ubc.gpec.ia.analyzer.controller;

/**
 *
 * @author samuelc
 */
public class GuidedManualScorerRequireAdditionalScoringException extends Exception {

    public GuidedManualScorerRequireAdditionalScoringException(String msg) {
        super(msg);
    }
}
