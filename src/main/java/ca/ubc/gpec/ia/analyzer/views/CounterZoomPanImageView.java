/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.gpec.ia.analyzer.views;

import ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.NucleiSelected;
import ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.NucleiSelectedState;
import ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.NucleiSelections;
import java.util.ArrayList;
import java.util.Calendar;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 *
 * @author samuelc
 */
public class CounterZoomPanImageView extends ZoomPanImageView {

    private NucleiSelections nucleiSelection;
    private ArrayList<ReminderAction> reminderActions;
    private CounterCartoon counterCartoon;
    private boolean showStartMessage;

    public CounterZoomPanImageView(Image image, NucleiSelections inputNucleiSelection, Stage inputStage) {
        super(image, inputStage);
        reminderActions = new ArrayList<>();
        setupHelpMenu();
        nucleiSelection = inputNucleiSelection;
        counterCartoon = null;
        showStartMessage = true;

        final EventHandler<? super KeyEvent> superOnKeyPressedHandler = super.getOnKeyPressed();

        // add key typed listener for capturing nuclei selection
        this.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                // call super class handler first
                superOnKeyPressedHandler.handle(ke);
                switch (ke.getCode()) {
                    case D: // negative
                        nucleiSelection.addNuclei(NucleiSelected.X_NOT_SET, NucleiSelected.Y_NOT_SET, Calendar.getInstance().getTimeInMillis(), NucleiSelectedState.NEGATIVE);
                        // refresh counter cartoon and play sound if available
                        playCartoonNegativeSound();
                        updateCartoon();
                        // check to see if there are any messages that needs to be shown (via dialog)
                        checkReminderActions();
                        break;
                    case F: // positive
                        nucleiSelection.addNuclei(NucleiSelected.X_NOT_SET, NucleiSelected.Y_NOT_SET, Calendar.getInstance().getTimeInMillis(), NucleiSelectedState.POSITIVE);
                        // refresh counter cartoon and play sound if available
                        playCartoonPositiveSound();
                        updateCartoon();
                        // check to see if there are any messages that needs to be shown (via dialog)
                        checkReminderActions();
                        break;
                    case Z: // undo count
                        nucleiSelection.removeNuclei();
                        // refresh counter cartoon and play sound if available
                        playCartoonUndoSound();
                        updateCartoon();
                        checkReminderActions();
                        break;
                    case R: // reset all counts!!!
                        if (ke.isControlDown()) {
                            String title = "Reset ALL nuclei selection?\n";
                            try {
                                if (MyDialog.showWarningConfirmDialog(title, title + "You will NOT be able to undo this operation.", stage)) {
                                    nucleiSelection.reset();
                                    // refresh counter cartoon if available
                                    updateCartoon();
                                }
                            } catch (MyDialogClosedByUserException e) {
                                // do nothing;
                            }
                        }
                        break;
                }
                ke.consume(); // consume this event so that nobody will do anything with it anymore!!
                //System.out.println("+/-/total ... " + nucleiSelection.getNumPositive() + "/" + nucleiSelection.getNumNegative() + "/" + nucleiSelection.getNumTotal());
            }
        });

        this.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (showStartMessage) {
                    checkReminderActions(); // check to see if there are any start messages i.e. message at count 0
                    showStartMessage = false; // show start message only once
                }
                ((CounterZoomPanImageView)(e.getSource())).requestFocus(); // request the focus when mouse enter
            }
        });
    }

    /**
     * set the counter cartoon
     *
     * @param counterCartoon
     */
    public void setCounterCartoon(CounterCartoon counterCartoon) {
        this.counterCartoon = counterCartoon;
        updateCartoon(); // update immediately in case the initial count is non-zero
    }

    /**
     * refresh cartoon
     */
    private void updateCartoon() {
        // refresh counter cartoon if available
        if (counterCartoon != null) {
            counterCartoon.refresh(nucleiSelection.getNumPositive(), nucleiSelection.getNumNegative());
        }
    }

    /**
     * play positive sound if cartoon available, otherwise, do nothing
     */
    private void playCartoonPositiveSound() {
        if (counterCartoon != null) {
            counterCartoon.playPositiveSound();
        }
    }

    /**
     * play negative sound if cartoon available, otherwise, do nothing
     */
    private void playCartoonNegativeSound() {
        if (counterCartoon != null) {
            counterCartoon.playNegativeSound();
        }
    }

    /**
     * play undo sound if cartoon available, otherwise, do nothing
     */
    private void playCartoonUndoSound() {
        if (counterCartoon != null) {
            counterCartoon.playUndoSound();
        }
    }

    /**
     * add reminder action
     *
     * @param reminderAction
     */
    public void addReminderAction(ReminderAction reminderAction) {
        reminderActions.add(reminderAction);
    }

    /**
     * check to see if needed to display reminder messages
     */
    private void checkReminderActions() {
        for (ReminderAction reminderAction : reminderActions) {
            reminderAction.doAction(nucleiSelection.getNumTotal());
        }
    }

    /**
     * set up help menu
     */
    private void setupHelpMenu() {
        final String title = "How to count nuclei ...";
        MenuItem navHelp = new MenuItem(title);
        navHelp.getStyleClass().add(ViewConstants.DEFAULT_CSS_THEME_NAME);
        navHelp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                MyDialog.showNoticeDialog(
                        title,
                        "count positive nuclei: press the \"F\" key\n"
                        + "count negative nuclei: press the \"D\" key\n"
                        + "undo nuclei count: press the \"Z\" key\n"
                        + "reset nuclei count: press control-R",
                        stage);
            }
        });
        helpMenu.getItems()
                .add(navHelp);
    }
}
