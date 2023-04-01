/*
 * responsible for overall repport consolidation process
 */
package ca.ubc.gpec.ia.analyzer.controller;

import ca.ubc.gpec.ia.analyzer.controller.helper.guidedManualScorer.GenerateConsolidatedReportHelperService;
import ca.ubc.gpec.ia.analyzer.controller.helper.guidedManualScorer.QcAnnotationsSetForReportConsolidationService;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Annotations;
import ca.ubc.gpec.ia.analyzer.views.MyButton;
import ca.ubc.gpec.ia.analyzer.views.MyTooltip;
import ca.ubc.gpec.ia.analyzer.views.NextBackButtonHBox;
import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import ca.ubc.gpec.ia.analyzer.views.guidedManualScorer.GuidedManualScorerStage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import javafx.application.HostServices;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

/**
 *
 * @author samuelc
 */
public class GuidedManualScorerReportConsolidationController {

    public static final String ANNOTATION_FILENAME_PATTERN = ".xml" + ViewConstants.GUIDED_MANUAL_SCORER_SAVE_ANNOTATION_FILE_SUFFIX;
    public static final String ANNOTATION_FILENAME_REGEX_PATTERN = ".*" + ANNOTATION_FILENAME_PATTERN + "\\d+\\b";
    public static final int MAX_NUM_RESCORE = 2;
    private GuidedManualScorerController parent;
    private GuidedManualScorerStage guidedManualScorerStage; // main stage
    private TreeSet<File> annotationsFileSet;
    private HashMap<File, Annotations> annotationsTable;
    private HostServices hostServices;

    /**
     * constructor
     *
     * @param guidedManualScorerStage
     */
    public GuidedManualScorerReportConsolidationController(GuidedManualScorerController parent, GuidedManualScorerStage guidedManualScorerStage) {
        this.parent = parent;
        this.guidedManualScorerStage = guidedManualScorerStage;
        annotationsFileSet = new TreeSet<File>();
        annotationsTable = new HashMap<File, Annotations>();
        hostServices = parent.getHostServices();
    }

    /**
     * show start page of the report consolidator
     */
    public void showStartPage() {
        VBox root = new VBox();
        root.setPadding(new Insets(ViewConstants.INSETS_WIDTH));
        root.setSpacing(ViewConstants.LARGE_SPACING);
        root.setAlignment(Pos.CENTER);

        askForAnnotationFiles();
    }

    /**
     * ask for annotation files
     */
    private void askForAnnotationFiles() {

        final BorderPane root = new BorderPane();
        root.setPadding(new Insets(ViewConstants.INSETS_WIDTH));

        // description of the input file
        Label notice = new Label(
                ViewConstants.REPORT_CONSOLIDATOR_ASK_FOR_FILE_TITLE + "  "
                + "Please drag and drop the score result annotation files to the area below.  "
                + "The name of these files are in the format of *.xml_GPEXxxx (ending with 13 digits) e.g. '1001-001-luminal.xml_GPEC1376330571243'.  "
                + "When you did the initial scoring of this case, you will have one score result annotation file.  "
                + "The pathologist who did the rescore will have another score result annotation file.  "
                + "Please select all (up to three since there will be a maximum of two rescores) score result annotation files.  "
                + "\n\n"
                + "To change file selection, simply drag and drop the new file to the area below."
        );
        notice.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        notice.setWrapText(true);
        notice.setTextAlignment(TextAlignment.LEFT);

        // label for input file name
        final VBox fileLabelsBox = new VBox();
        fileLabelsBox.setAlignment(Pos.CENTER);
        fileLabelsBox.setSpacing(ViewConstants.SPACING);
        final Label noFileSelectedLabel = new Label("no score result annotation file selected.");
        noFileSelectedLabel.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        noFileSelectedLabel.setAlignment(Pos.CENTER_LEFT);
        noFileSelectedLabel.setTextAlignment(TextAlignment.LEFT);
        VBox.setMargin(noFileSelectedLabel, new Insets(ViewConstants.INSETS_WIDTH, 0, ViewConstants.INSETS_WIDTH, 0));
        fileLabelsBox.getChildren().add(noFileSelectedLabel);

        // add next/back button
        final NextBackButtonHBox nextBackButtonHBox = new NextBackButtonHBox(true); // disable first since no svs/annotation file selected
        nextBackButtonHBox.getNextButton().setText("generate consolidated score report"); // change 'next' button text
        ////////////////////////////////////////////////////////////////////////
        // action listeners ...
        nextBackButtonHBox.getBackButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    parent.showStartPage();
                } catch (IOException ioe) {
                    guidedManualScorerStage.showErrorDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_UNRECOVERABLE_ERROR + "..." + ioe);
                    System.exit(1);
                }
            }
        });

        /**
         * object to store select files info
         */
        class BrowseButtonHelper {

            private TreeSet<File> annotationFiles;

            BrowseButtonHelper() {
                annotationFiles = new TreeSet<>();
            }

            void addAnnotationFile(File annotationFile) {
                annotationFiles.add(annotationFile);
            }

            SortedSet<File> getAnnotationFiles() {
                return annotationFiles;
            }

            /**
             * clear the annotationFiles tree set
             */
            void clear() {
                annotationFiles.clear();
            }

            /**
             * all annotation file should begin with the same case number -
             * check if this is true
             */
            boolean areFileNamesConsistent() {
                String prefix = annotationFiles.first().getName();
                prefix = prefix.substring(0, prefix.indexOf(ANNOTATION_FILENAME_PATTERN));
                for (File f : annotationFiles) {
                    if (!f.getName().substring(0, prefix.length()).equals(prefix)) {
                        return false;
                    }
                }
                return true;
            }
        } // end of BrowseButtonHelper

        final BrowseButtonHelper browseButtonHelper = new BrowseButtonHelper();

        class BrowseButtonHelperFileRemover implements EventHandler<MouseEvent> {

            private File f; // pointer to file to remove
            private HBox hbox; // hbox containing the filename label and remove button

            BrowseButtonHelperFileRemover(File f, HBox hbox) {
                this.f = f;
                this.hbox = hbox;
            }

            @Override
            public void handle(MouseEvent event) {
                browseButtonHelper.getAnnotationFiles().remove(f);
                fileLabelsBox.getChildren().remove(hbox);
                if (browseButtonHelper.getAnnotationFiles().size() > 1) {
                    // at least two annotation file need ... because no need to consolidate a single score result!
                    nextBackButtonHBox.getNextButton().setDisable(false); // enable next button to allow user to continue
                } else {
                    nextBackButtonHBox.getNextButton().setDisable(true);
                }
            }
        }

        nextBackButtonHBox.getNextButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // read the annotation
                for (File f : browseButtonHelper.getAnnotationFiles()) {
                    // parse annotation
                    try {
                        annotationsFileSet.add(f);
                        JAXBContext context = JAXBContext.newInstance(Annotations.class);
                        Unmarshaller um = context.createUnmarshaller();
                        annotationsTable.put(f, (Annotations) um.unmarshal(new FileReader(f)));

                    } catch (JAXBException | FileNotFoundException ex) {
                        String errorMsg = "ERROR: something went wrong while reading the score result annotation files.  Please double check and re-select.  Exception encountered: " + ex.toString();
                        guidedManualScorerStage.showErrorDialog(errorMsg);
                    }
                }
                // need to do some checking to make sure the annotationsSet are consistent
                guidedManualScorerStage.showWaitDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_CHECK_ANNOTATION_FILES_FOR_REPORT_CONSOLIDATION); // show wait message
                QcAnnotationsSetForReportConsolidationService helper = new QcAnnotationsSetForReportConsolidationService(guidedManualScorerStage, annotationsTable);
                helper.start();
                helper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent t) {
                        guidedManualScorerStage.closeWaitDialog(); // close wait dialog
                        // check to see if there are any error messages
                        String errMsg = (String) (t.getSource().getValue());
                        if (errMsg != null) {
                            guidedManualScorerStage.showErrorDialog(errMsg);
                            annotationsFileSet.clear(); // clear files
                            annotationsTable.clear(); // clear files
                        } else {
                            // generate report!
                            guidedManualScorerStage.showWaitDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_GENERATING_REPORT);
                            GenerateConsolidatedReportHelperService helper = new GenerateConsolidatedReportHelperService(annotationsTable);
                            helper.start();
                            helper.setOnFailed(new EventHandler<WorkerStateEvent>() {
                                @Override
                                public void handle(WorkerStateEvent t) {
                                    guidedManualScorerStage.closeWaitDialog();
                                    guidedManualScorerStage.showErrorDialog("Failed to generate consolidated report.\nReason(s): " + t.getSource().getException());
                                }
                            });
                            helper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                @Override
                                public void handle(WorkerStateEvent t) {
                                    guidedManualScorerStage.closeWaitDialog();

                                    // show pdf
                                    String reportFilename = (String) (t.getSource().getValue());
                                    guidedManualScorerStage.showNoticeDialog("Consolidated Ki67 score report saved to: " + reportFilename);
                                    hostServices.showDocument(reportFilename);
                                    guidedManualScorerStage.showNoticeDialog("This application will now exit.  Bye.");
                                    guidedManualScorerStage.getStage().close();
                                }
                            });
                        }
                    }
                });
            } // end of nextBackButtonHBox.getNextButton().setOnMouseClicked(new EventHandler<MouseEvent>()
        });
        /// end of action listeners
        ////////////////////////////////////////////////////////////////////////        
        // add all the nodes to root and add root to scene
        root.setTop(notice);
        root.setCenter(fileLabelsBox);
        root.setBottom(nextBackButtonHBox);
        guidedManualScorerStage.setTitle(ViewConstants.REPORT_CONSOLIDATOR_ASK_FOR_FILE_TITLE);
        guidedManualScorerStage.setWidth(ViewConstants.MAIN_PANE_WIDTH);
        guidedManualScorerStage.setHeight(ViewConstants.MAIN_PANE_HEIGHT);
        guidedManualScorerStage.show(root);

        // drag and drop virtual slide file
        guidedManualScorerStage.getStage().getScene().setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    // need to check to see if at least one file matches a score result annotation file  
                    for (File f : db.getFiles()) {
                        if (f.getName().matches(ANNOTATION_FILENAME_REGEX_PATTERN) & f.canRead()) {
                            event.acceptTransferModes(TransferMode.COPY);
                            break;
                        }
                    }
                } else {
                    event.consume();
                }
            }
        }
        );
        guidedManualScorerStage.getStage().getScene().setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    for (File f : db.getFiles()) { // we know there will be at least one annotation file due to check in setOnDragOver()
                        if (f.getName().matches(ANNOTATION_FILENAME_REGEX_PATTERN)) {
                            browseButtonHelper.addAnnotationFile(f);
                        }
                    }

                    // do some checking here!!!
                    // all annotation file's beginning part should be the same
                    if (!browseButtonHelper.areFileNamesConsistent()) {
                        String errorMsg = "ERROR: it seems that not all selected score result annotation files refer to same virtual slide image.  "
                                + "The filename of all score result annotation files referring to the same virtual slide image should begin "
                                + "with the virtual slide image filename (minus the file extension).  "
                                + "Please double check and re-select.  ";
                        guidedManualScorerStage.showErrorDialog(errorMsg);
                        success = false; // failed to get file!
                    } else if (browseButtonHelper.getAnnotationFiles().size() > (MAX_NUM_RESCORE + 1)) {
                        String errorMsg = "ERROR: cannot consolidate more than " + MAX_NUM_RESCORE + " rescores.  Please double check and re-select";
                        guidedManualScorerStage.showErrorDialog(errorMsg);
                        success = false; // failed to get file!
                    } else {
                        // list the selected files
                        fileLabelsBox.getChildren().clear();
                        for (File f : browseButtonHelper.getAnnotationFiles()) {
                            HBox hbox = new HBox();

                            Label l = new Label(f.getAbsolutePath());
                            l.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
                            l.setTooltip(new MyTooltip("selected file: " + f.getAbsolutePath()));

                            MyButton removeButton = new MyButton("x");
                            removeButton.setTooltip(new MyTooltip("click me to remove " + f.getName()));
                            removeButton.setOnMouseClicked(new BrowseButtonHelperFileRemover(f, hbox));

                            hbox.getChildren().addAll(l, removeButton);
                            hbox.setAlignment(Pos.CENTER);
                            hbox.setSpacing(ViewConstants.SPACING);

                            fileLabelsBox.getChildren().add(hbox);
                        }
                        if (browseButtonHelper.getAnnotationFiles().size() > 1) {
                            // at least two annotation file need ... because no need to consolidate a single score result!
                            nextBackButtonHBox.getNextButton().setDisable(false); // enable next button to allow user to continue
                        }
                    }
                }
                if (!success) {
                    // need to clear all the file records in browseButtonHelper
                    browseButtonHelper.clear();
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
    }

}
