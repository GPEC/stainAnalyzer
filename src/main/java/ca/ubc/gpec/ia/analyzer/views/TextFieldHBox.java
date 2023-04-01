/*
 * extending the HBox ... consist of a label and a text field on the right of 
 * the label
 */
package ca.ubc.gpec.ia.analyzer.views;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

/**
 *
 * @author samuelc
 */
public class TextFieldHBox extends HBox {

    private TextField inputTextField;
    private Label label;

    /**
     * constructor
     *
     * @param labelText
     * @param toolTipText
     * @param promptText
     * @param width
     */
    public TextFieldHBox(String labelText, String toolTipText, String promptText, int width) {
        super();
        label = new Label(labelText);
        label.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        inputTextField = new TextField();
        setup(toolTipText, promptText, width);
    }

    /**
     * constructor with reference to textField object
     *
     * @param textField
     * @param labelText
     * @param toolTipText
     * @param promptText
     * @param width
     */
    public TextFieldHBox(TextField textField, String labelText, String toolTipText, String promptText, int width) {
        super();
        label = new Label(labelText);
        label.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        inputTextField = textField;
        setup(toolTipText, promptText, width);
    }

    /**
     * setup the textfield
     *
     * @param toolTipText
     * @param promptText
     * @param width
     */
    private void setup(String toolTipText, String promptText, int width) {
        setSpacing(ViewConstants.SPACING);
        inputTextField.setMaxWidth(width);
        if (toolTipText != null) {
            Tooltip tp = new MyTooltip(toolTipText);
            tp.setMinWidth(width);
            inputTextField.setTooltip(tp);
        }
        if (promptText != null) {
            inputTextField.setPromptText(promptText);
        }
        this.getChildren().addAll(label, inputTextField);
    }

    /**
     * return TextField object so that we could add eventhandler to it
     *
     * @return
     */
    public TextField getInputTextField() {
        return inputTextField;
    }

    /**
     * return text entered into the textfield
     *
     * @return
     */
    public String getInputText() {
        return inputTextField.getText();
    }
}
