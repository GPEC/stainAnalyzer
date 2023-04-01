/*
 * capture exception when user close the dialog without click on any buttons
 */
package ca.ubc.gpec.ia.analyzer.views;

/**
 *
 * @author samuelc
 */
public class MyDialogClosedByUserException extends Exception {
    public MyDialogClosedByUserException(String msg) {
        super(msg);
    }
    
}
