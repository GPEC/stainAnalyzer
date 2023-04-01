/*
 * Button with some custom features ...
 * 
 * 1. when focused, preseing enter will be same as mouse click
 * 2. default set focus traversable to true
 * 
 */
package ca.ubc.gpec.ia.analyzer.views;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 *
 * @author samuelc
 */
public class MyButton extends Button {

    private boolean pressed;

    public MyButton(String text) {
        super(text);
        pressed = false;

        super.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.ENTER) {
                    ((MyButton) (ke.getSource())).setPressed(true);
                    ke.consume();
                    pressed = true;
                }
            }
        });

        super.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode() == KeyCode.ENTER) {
                    if (pressed) {
                        ((MyButton) (ke.getSource())).setPressed(false);
                        ((MyButton) (ke.getSource())).getOnMouseClicked().handle(null);
                        pressed = false;
                    }
                    ke.consume();
                }
            }
        });
        
        // default set focus traversable to true
        setFocusTraversable(true);
    }
}
