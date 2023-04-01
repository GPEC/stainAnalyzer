/*
 * stage template for GuidedManualScorer
 */
package ca.ubc.gpec.ia.analyzer.views.guidedManualScorer;

import ca.ubc.gpec.ia.analyzer.views.MyDialog;
import ca.ubc.gpec.ia.analyzer.views.MyDialogClosedByUserException;
import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import eu.schudt.javafx.controls.calendar.DatePicker;
import java.io.File;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 *
 * @author samuelc
 */
public class GuidedManualScorerStage {

    private Stage stage;
    private File currentSelectDirectory; // keep track of current directory the user last looked at
    private Stage waitDialog;
    private int width;
    private int height;

    /**
     * constructor
     */
    public GuidedManualScorerStage(Stage stage) {
        currentSelectDirectory = null;
        this.stage = stage;
        stage.getIcons().add(new Image(
                this.getClass().getResource(ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + "gpec-icon.png").toExternalForm())
        );
        width = ViewConstants.MAIN_PANE_WIDTH;
        height = ViewConstants.MAIN_PANE_HEIGHT;
        stage.setWidth(width);
        stage.setHeight(height);
    }

    /**
     * return stage NOT waitDialog which is also a stage
     *
     * @return
     */
    public Stage getStage() {
        return stage;
    }

    /**
     * set title
     *
     * @param title
     */
    public void setTitle(String title) {
        stage.setTitle(title);
    }

    /**
     * set width
     *
     * @param width
     */
    public void setWidth(int width) {
        this.width = width;
        stage.setWidth(width);
    }

    /**
     * set height
     *
     * @param height
     */
    public void setHeight(int height) {
        this.height = height;
        stage.setHeight(height);
    }

    /**
     * display scene
     *
     * @param rootPane
     */
    public void show(Pane rootPane) {
        Scene currentScene = new Scene(rootPane, width, height);
        // load css
        currentScene.getStylesheets().add(ViewConstants.getViewFilePath(ViewConstants.DEFAULT_CSS_FILENAME)); // get css
        currentScene.getStylesheets().add(DatePicker.CSS_FILENAME); // css for date picker
        rootPane.getStyleClass().add(ViewConstants.DEFAULT_CSS_THEME_NAME); // apply theme

        stage.setScene(currentScene);
        stage.sizeToScene(); // THIS IS NEEDED TO make sure everything lay out properly!!!!
        stage.show();
    }

    /**
     * show wait dialog
     *
     * @param msg
     */
    public void showWaitDialog(String msg) {
        waitDialog = MyDialog.showWaitDialog(msg, stage);
    }

    /**
     * close wait dialog
     *
     */
    public void closeWaitDialog() {
        waitDialog.close();
    }

    public void showNoticeDialog(String msg) {
        MyDialog.showNoticeDialog(msg, stage);
    }

    public void showWarningDialog(String msg) {
        MyDialog.showWarningDialog(msg, stage);
    }

    /**
     * show error dialog
     *
     * @param msg
     */
    public void showErrorDialog(String msg) {
        MyDialog.showErrorDialog(msg, stage);
    }

    /**
     * show confirm message with warning icon
     *
     * @param title
     * @param msg
     * @return
     * @throws MyDialogClosedByUserException
     */
    public boolean showWarningConfirmDialog(String title, String msg) throws MyDialogClosedByUserException {
        return MyDialog.showWarningConfirmDialog(title, msg, stage);
    }

    /**
     * show confirm message with notice icon
     *
     * @param title
     * @param msg
     * @return
     * @throws MyDialogClosedByUserException
     */
    public boolean showNoticeConfirmDialog(String title, String msg) throws MyDialogClosedByUserException {
        return MyDialog.showNoticeConfirmDialog(title, msg, stage);
    }

    /**
     * show file chooser and ask for file
     *
     * @param title
     * @param extensionFilter
     * @return
     */
    public File askForSingleFile(String title, ExtensionFilter extensionFilter) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(extensionFilter);
        if (currentSelectDirectory != null) {
            fileChooser.setInitialDirectory(currentSelectDirectory);
        }
        File result = fileChooser.showOpenDialog(stage);
        currentSelectDirectory = result == null ? null : result.getParentFile();
        return result;
    }
}
