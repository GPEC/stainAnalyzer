/*
 * HBox with next, back and save button
 */
package ca.ubc.gpec.ia.analyzer.views;

import javafx.scene.control.Button;

/**
 *
 * @author samuelc
 */
public class NextBackSaveButtonHBox extends NextBackButtonHBox {
    private MyButton saveButton;
    public NextBackSaveButtonHBox(boolean disableNextButton) {
        super(disableNextButton);
        saveButton = new MyButton("save");
        getChildren().add(saveButton);
    }
    
    public Button getSaveButton() {
        return saveButton;
    }
}
