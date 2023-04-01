/*
 * capture respone from user in a dialog
 */
package ca.ubc.gpec.ia.analyzer.views;

/**
 *
 * @author samuelc
 */
public class MyDialogUserBooleanResponse {
    Boolean booleanResponse;
    
    /**
     * constructor
     */
    public MyDialogUserBooleanResponse() {
        booleanResponse = null;
    }
    
    /**
     * constructor for a boolean response
     * @param booleanResponse 
     */
    public MyDialogUserBooleanResponse(boolean booleanResponse) {
        this.booleanResponse = booleanResponse;
    }
    
    /**
     * set boolean response
     * @param booleanResponse 
     */
    public void setResponse(boolean booleanResponse) {
        this.booleanResponse = booleanResponse;
    } 
    
    /**
     * get boolean response
     * @return 
     */
    public boolean getResponse() {
        return booleanResponse.booleanValue();
    }
    
    /**
     * check to see if user selected a response
     * @return 
     */
    public boolean isResponseSet() {
        return booleanResponse != null;
    }
}

