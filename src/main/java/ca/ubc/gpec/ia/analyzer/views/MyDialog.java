/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.gpec.ia.analyzer.views;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author samuelc
 */
public class MyDialog {

    private static int ICON_WIDTH = 30;

    public static Stage showWaitDialog(String msg, Stage owner) {
        final Stage dialogStage = new Stage();
        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initStyle(StageStyle.UNDECORATED);

        VBox vbox = new VBox();
        vbox.setPadding(new Insets(20, 5, 20, 5));
        vbox.setSpacing(ViewConstants.SPACING);
        vbox.setAlignment(Pos.CENTER);

        Label notice = new Label(msg);
        notice.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        ProgressBar progressBar = new ProgressBar(-1);

        vbox.getChildren().addAll(notice, progressBar);

        Scene scene = new Scene(vbox);

        // load css for the OK button
        scene.getStylesheets().add(ViewConstants.getViewFilePath(ViewConstants.DIALOG_CSS_FILENAME)); // get css
        vbox.getStyleClass().add(ViewConstants.DEFAULT_CSS_THEME_NAME); // apply theme
        
        dialogStage.setScene(scene);
        dialogStage.sizeToScene();
        dialogStage.show(); // do NOT do showAndWait() as this will stop current thread from doing anything processing 

        return dialogStage;
    }

    /**
     * show a dialog
     *
     * focus is requested for the 'ok' button
     *
     * @param title
     * @param msg
     * @param iconFilename
     * @param owner
     */
    public static void showDialog(String title, String msg, String iconFilename, Stage owner) {
        final Stage dialogStage = new Stage();
        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setTitle(title);

        Image img = new Image(
                MyDialog.class.getResource(ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + iconFilename).toExternalForm());

        ImageView icon = new ImageView(img);
        icon.setPreserveRatio(true);
        icon.setFitWidth(ICON_WIDTH);
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(20, 5, 20, 5));
        vbox.setSpacing(ViewConstants.SPACING);
        vbox.setAlignment(Pos.CENTER);

        HBox hbox = new HBox();
        hbox.setPadding(ViewConstants.getDefaultInsets());
        hbox.setSpacing(ViewConstants.SPACING);
        MyButton okButton = new MyButton(ViewConstants.OK);
        okButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                // ask user for input file
                dialogStage.close();
            }
        });
        okButton.setAlignment(Pos.CENTER);
        okButton.setFocusTraversable(true);
        okButton.requestFocus(); // request focus
        Label msgLabel = new Label(msg);
        msgLabel.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        msgLabel.setMaxWidth(ViewConstants.MAIN_PANE_WIDTH);
        msgLabel.setWrapText(true);
        hbox.getChildren().addAll(icon, msgLabel);

        vbox.getChildren().addAll(hbox, okButton);

        Scene scene = new Scene(vbox);

        // load css for the OK button
        scene.getStylesheets().add(ViewConstants.getViewFilePath(ViewConstants.DEFAULT_CSS_FILENAME)); // get css
        vbox.getStyleClass().add(ViewConstants.DEFAULT_CSS_THEME_NAME); // apply theme

        dialogStage.setScene(scene);
        dialogStage.sizeToScene();
        dialogStage.showAndWait();
    }

    /**
     * show confirm (Yes/No) dialog ... returns true if 'Yes' selected and false
     * if 'No' selected
     *
     * focus is requested for the 'No' button
     *
     * @param title
     * @param msg
     * @param iconFilename
     * @param owner
     */
    public static boolean showConfirmDialog(String title, String msg, String iconFilename, Stage owner) throws MyDialogClosedByUserException {
        final Stage dialogStage = new Stage();
        // initial set to null so that we know if the user actually click any buttons on the dialog
        final MyDialogUserBooleanResponse response = new MyDialogUserBooleanResponse();

        dialogStage.initOwner(owner);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setTitle(title);

        Image img = new Image(
                MyDialog.class.getResource(ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + iconFilename).toExternalForm());
        ImageView icon = new ImageView(img);
        icon.setPreserveRatio(true);
        icon.setFitWidth(ICON_WIDTH);
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(20, 5, 20, 5));
        vbox.setSpacing(ViewConstants.SPACING);
        vbox.setAlignment(Pos.CENTER);

        HBox hbox = new HBox();
        hbox.setPadding(ViewConstants.getDefaultInsets());
        hbox.setSpacing(ViewConstants.SPACING);

        HBox buttons = new HBox();
        buttons.setPadding(ViewConstants.getDefaultInsets());
        buttons.setSpacing(ViewConstants.SPACING);
        buttons.setAlignment(Pos.CENTER);

        MyButton yesButton = new MyButton(ViewConstants.YES);
        yesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                // ask user for input file
                dialogStage.close();
                response.setResponse(true);
            }
        });
        yesButton.setFocusTraversable(true);

        MyButton noButton = new MyButton(ViewConstants.NO);
        noButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                // ask user for input file
                dialogStage.close();
                response.setResponse(false);
            }
        });
        noButton.setFocusTraversable(true);

        buttons.getChildren().addAll(yesButton, noButton);

        Label msgLabel = new Label(msg);
        msgLabel.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        msgLabel.setMaxWidth(ViewConstants.MAIN_PANE_WIDTH);
        msgLabel.setWrapText(true);
        hbox.getChildren().addAll(icon, msgLabel);

        vbox.getChildren().addAll(hbox, buttons);

        Scene scene = new Scene(vbox);

        // load css for the OK button
        scene.getStylesheets().add(ViewConstants.getViewFilePath(ViewConstants.DEFAULT_CSS_FILENAME)); // get css
        vbox.getStyleClass().add(ViewConstants.DEFAULT_CSS_THEME_NAME); // apply theme

        dialogStage.setScene(scene);
        dialogStage.sizeToScene();
        noButton.requestFocus(); // request focus 
        dialogStage.showAndWait();
        if (response.isResponseSet()) {
            return response.getResponse();
        } else {
            throw new MyDialogClosedByUserException("");
        }
    }

    /**
     * confirm message with warning sign
     *
     * @param title
     * @param msg
     * @param owner
     * @return
     * @throws MyDialogClosedByUserException
     */
    public static boolean showWarningConfirmDialog(String title, String msg, Stage owner) throws MyDialogClosedByUserException {
        return showConfirmDialog(title, msg, "dialog_warning.png", owner);
    }

    /**
     * confirm message with notice sign
     *
     * @param title
     * @param msg
     * @param owner
     * @return
     * @throws MyDialogClosedByUserException
     */
    public static boolean showNoticeConfirmDialog(String title, String msg, Stage owner) throws MyDialogClosedByUserException {
        return showConfirmDialog(title, msg, "dialog_notice.png", owner);
    }

    /**
     * show notice dialog
     *
     * @param msg
     * @param owner
     */
    public static void showNoticeDialog(String msg, Stage owner) {
        showDialog("Please note ...", msg, "dialog_notice.png", owner);
    }

    /**
     * show notice dialog
     *
     * @param msg
     * @param owner
     */
    public static void showNoticeDialog(String title, String msg, Stage owner) {
        showDialog(title, msg, "dialog_notice.png", owner);
    }

    /**
     * show warning dialog
     *
     * @param msg
     * @param owner
     */
    public static void showWarningDialog(String msg, Stage owner) {
        showDialog("WARNING", msg, "dialog_warning.png", owner);
    }

    /**
     * show error dialog
     *
     * @param msg
     * @param owner
     */
    public static void showErrorDialog(String msg, Stage owner) {
        showDialog("ERROR", msg, "dialog_error.png", owner);
    }
}
