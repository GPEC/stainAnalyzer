/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.gpec.ia.analyzer.views;

import javafx.scene.control.Button;

/**
 *
 * @author samuelc
 */
public class ReminderEnableButton extends ReminderAction {
    Button button;
    
    /**
     * constructor
     * 
     * @param threshold
     * @param button 
     */
    public ReminderEnableButton(int threshold, Button button) {
        super(threshold);
        this.button = button;
    }
    
    /**
     * do action if count equals threshold
     * 
     * @param count 
     */
    public void doAction(int count) {
        if (getThreshold() <= count) {
            button.setDisable(false); // enable button!!!
        } else {
            button.setDisable(true); // disable button!!!
        }
    }
}
