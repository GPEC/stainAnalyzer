/*
 * an HBox containing a next and a back button
 */
package ca.ubc.gpec.ia.analyzer.views;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 *
 * @author samuelc
 */
public class NextBackButtonHBox extends HBox {

    private MyButton nextButton;
    private MyButton backButton;
    
    public NextBackButtonHBox(boolean disableNextButton) {
        super();
        nextButton = new MyButton("next");
        backButton = new MyButton("back");
        nextButton.setDisable(disableNextButton);
        setAlignment(Pos.CENTER_RIGHT);
        setSpacing(ViewConstants.SPACING);
        getChildren().addAll(backButton, nextButton);
    }
    
    /**
     * get next button
     * @return 
     */
    public Button getNextButton() {
        return nextButton;
    }
    
    /**
     * get back button
     * @return 
     */
    public Button getBackButton() {
        return backButton;
    }
}
