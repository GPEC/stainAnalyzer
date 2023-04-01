/*
 * do something at the appropriate time/occasion indicated by "threshold"
 * 
 * used in CounterZoomPanImaageView
 */
package ca.ubc.gpec.ia.analyzer.views;

/**
 *
 * @author samuelc
 */
public abstract class ReminderAction {
    
    private int threshold;

    public ReminderAction(int threshold) {
        this.threshold = threshold;
    }
    
    /**
     * return internal 
     * @return 
     */
    protected int getThreshold() {
        return threshold;
    }
    
    /**
     * if the internal count == count .. do the action
     * @param count 
     */
    public abstract void doAction(int count);
}
