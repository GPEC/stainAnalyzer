/*
 * main controller for stainAnalyzer
 */
package ca.ubc.gpec.ia.analyzer.controller;

import ca.ubc.gpec.fieldselector.FieldSelectorConstants;
import ca.ubc.gpec.fieldselector.FieldSelectorPanel;
import ca.ubc.gpec.fieldselector.exception.FieldSelectionParamStringParseException;
import ca.ubc.gpec.ia.analyzer.controller.helper.guidedManualScorer.*;
import ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.GuidedManualScorer;
import ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.NucleiSelections;
import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.AnnotationProcessingRuntimeException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionGeneratorMaxTriesExceeded;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotPreprocessedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotSupportedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Annotation;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Region;
import ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport;
import ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport.RescoreStatus;
import ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport.ScoreType;
import ca.ubc.gpec.ia.analyzer.util.MiscUtil;
import ca.ubc.gpec.ia.analyzer.views.*;
import ca.ubc.gpec.ia.analyzer.views.guidedManualScorer.GuidedManualScorerStage;
import eu.schudt.javafx.controls.calendar.DatePicker;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import loci.formats.FormatException;

import javax.swing.filechooser.FileNameExtensionFilter;
import jakarta.xml.bind.JAXBException;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;

import static ca.ubc.gpec.ia.analyzer.views.ViewConstants.MAIN_PANE_HEIGHT;
import static ca.ubc.gpec.ia.analyzer.views.ViewConstants.MAIN_PANE_WIDTH;

/**
 *
 * @author samuelc
 */
public class GuidedManualScorerController extends Application {

    private GuidedManualScorerStage guidedManualScorerStage;
    private GuidedManualScorer guidedManualScorer;
    private HostServices hostServices;
    private FileNameExtensionFilter supportedFileNameFilter;
    private String supportedFileNameExtensions;
    private ScoreType scoreType = null;
    private int coreDiameterInMicron;

    /**
     * constructor
     */
    public GuidedManualScorerController() {
        guidedManualScorer = new GuidedManualScorer();
        hostServices = this.getHostServices();
        supportedFileNameFilter = new FileNameExtensionFilter("Aperio/Hamamatsu virtual slide file (*.svs/*.ndpi)", "svs", "ndpi");
        supportedFileNameExtensions = "*.svs/*.ndpi";
    }

    /**
     * show start page
     */
    protected void showStartPage() throws IOException {
        VBox root = new VBox();
        root.setPadding(new Insets(ViewConstants.INSETS_WIDTH));
        root.setSpacing(ViewConstants.LARGE_SPACING);
        root.setAlignment(Pos.CENTER);

        Label title = new Label(ViewConstants.GUIDED_MANUAL_SCORER_MAIN_PANE_TITLE);
        title.setAlignment(Pos.CENTER);
        title.setStyle("-fx-font-size:36;");

        VBox startButtons = new VBox();
        startButtons.setAlignment(Pos.CENTER);
        startButtons.setSpacing(ViewConstants.SPACING);

        MyButton startRandomVirtualTMACoresButton = new MyButton("Generate " + ViewConstants.RANDOM_VIRTUAL_TMA_CORE);
        startRandomVirtualTMACoresButton.setTooltip(new MyTooltip("click me to start scoring"));

        MyButton startManualVirtualTMACoresButton = new MyButton("Manually select " + ViewConstants.VIRTUAL_TMA_CORE);
        startManualVirtualTMACoresButton.setTooltip(new MyTooltip("click me to start scoring"));

        MyButton consolidateReportButton = new MyButton("Consolidate Ki67 score report ");
        consolidateReportButton.setTooltip(new MyTooltip("click me to generate a report consolidating the initial and rescore(s) scores"));

        startButtons.getChildren().add(startRandomVirtualTMACoresButton);
        // only add for BCCA
        if (ViewConstants.TRIAL_CENTRE == GuidedManualScorerReport.TrialCentre.BCCA) {
            startButtons.getChildren().add(startManualVirtualTMACoresButton);
        }
        startButtons.getChildren().add(consolidateReportButton);

        root.getChildren().addAll(title, startButtons);

        startRandomVirtualTMACoresButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                try {
                    if (!guidedManualScorerStage.showWarningConfirmDialog("Please confirm trial centre ...", ViewConstants.getTrialCentreConfirmMessage())) {
                        // same behavior as closed by user ...
                        throw new MyDialogClosedByUserException("");
                    }
                } catch (MyDialogClosedByUserException ex) {
                    // exit !!!
                    guidedManualScorerStage.showErrorDialog(ViewConstants.getTrialCentreFailedConfirmMessage());
                    System.exit(0);
                }
                scoreType = GuidedManualScorerReport.ScoreType.RANDOM;
                displayRandomVirtualTMACoreInstructions();
            }
        });

        startManualVirtualTMACoresButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                scoreType = GuidedManualScorerReport.ScoreType.MANUAL;
                // ask user for input file
                askForAperioFile();
            }
        });

        consolidateReportButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                (new GuidedManualScorerReportConsolidationController(GuidedManualScorerController.this, guidedManualScorerStage)).showStartPage();
            }
        });

        guidedManualScorerStage.setTitle(ViewConstants.GUIDED_MANUAL_SCORER_MAIN_PANE_TITLE);
        guidedManualScorerStage.setWidth(ViewConstants.START_PANE_WIDTH);
        guidedManualScorerStage.setHeight(ViewConstants.START_PANE_HEIGHT);
        guidedManualScorerStage.show(root);
    }

    /**
     * display random virtual TMA core instructions
     */
    private void displayRandomVirtualTMACoreInstructions() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setPadding(ViewConstants.getDefaultInsets());

        // display logo
        HBox logo = new HBox();
        ImageView logoImageView = new ImageView(ViewConstants.getTrialCentreLogPath());
        logoImageView.setPreserveRatio(true);
        logoImageView.setFitHeight(50);
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.getChildren().add(logoImageView);
        final WebView browser = new WebView();
        final WebEngine webEngine = browser.getEngine();

        // load webpage ... look at html to see which css it is using
        webEngine.load((getClass().getResource(ViewConstants.getViewFilePath(this, "randomVirtualTMACoreInstructions.html")).toExternalForm()));

        // ok button
        MyButton okButton = new MyButton(ViewConstants.OK);
        okButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                // ask user for input file
                askForAperioFile();
            }
        });

        // add all the nodes to root and add root to scene
        root.getChildren().addAll(logo, browser, okButton);
        guidedManualScorerStage.setTitle(ViewConstants.GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_INSTRUCTIONS_TITLE);
        guidedManualScorerStage.setWidth(ViewConstants.MAIN_PANE_WIDTH);
        guidedManualScorerStage.setHeight(625);
        guidedManualScorerStage.show(root);
    }

    /**
     * ask for Aperio file
     */
    private void askForAperioFile() {
        guidedManualScorer.clear(); // clear everything first 

        final BorderPane root = new BorderPane();
        root.setPadding(new Insets(ViewConstants.INSETS_WIDTH));

        // description of the input file
        Label notice = new Label(
                ViewConstants.GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_ASK_FOR_FILE_TITLE + "  "
                + "Please use the browse button or "
                + "drag and drop the file (" + supportedFileNameExtensions + ") to the area below.  "
                + "Please note, this should be the file with the tumor area selection saved in an annotation file (.xml) using ImageScope.  "
                + "The annotation file must be in the same folder as the virtual slide file (" + supportedFileNameExtensions + ") and must have the same name (e.g. 123.svs, 123.xml)."
                + "\n\n"
                + "To change file selection, simply drag and drop the new file to the area below or use the browse button."
        );
        notice.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        notice.setWrapText(true);
        notice.setTextAlignment(TextAlignment.LEFT);

        // label for input file name
        VBox fileLabelsBox = new VBox();
        fileLabelsBox.setAlignment(Pos.CENTER);
        final Label svsFileLabel = new Label("no Aperio/Hamamatsu virtual slide file (" + supportedFileNameExtensions + ") selected.");
        svsFileLabel.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        final Label annotationFileLabel = new Label("no annoation file (*.xml) selected.");
        annotationFileLabel.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        svsFileLabel.setWrapText(true);
        svsFileLabel.setAlignment(Pos.CENTER_LEFT);
        svsFileLabel.setTextAlignment(TextAlignment.LEFT);
        VBox.setMargin(svsFileLabel, new Insets(ViewConstants.INSETS_WIDTH, 0, ViewConstants.INSETS_WIDTH, 0));
        annotationFileLabel.setWrapText(true);
        annotationFileLabel.setAlignment(Pos.CENTER_LEFT);
        annotationFileLabel.setTextAlignment(TextAlignment.LEFT);
        VBox.setMargin(annotationFileLabel, new Insets(ViewConstants.INSETS_WIDTH, 0, ViewConstants.INSETS_WIDTH, 0));
        fileLabelsBox.getChildren().addAll(svsFileLabel, annotationFileLabel);

        // add next/back button
        final NextBackButtonHBox nextBackButtonHBox = new NextBackButtonHBox(true); // disable first since no svs/annotation file selected

        // browse file button
        MyButton browseButton = new MyButton("browse");
        browseButton.setTooltip(new MyTooltip("select local Aperio/Hamamatsu virtual slide file (" + supportedFileNameExtensions + ")"));
        browseButton.setAlignment(Pos.CENTER);
        BorderPane.setMargin(browseButton, new Insets(ViewConstants.INSETS_WIDTH, 0, 0, 0));

        ////////////////////////////////////////////////////////////////////////
        // action listeners ...
        nextBackButtonHBox.getBackButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    showStartPage();
                } catch (IOException ioe) {
                    guidedManualScorerStage.showErrorDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_UNRECOVERABLE_ERROR + "..." + ioe);
                    System.exit(1);
                }
            }
        });

        /**
         * object to store the selected file info
         */
        class BrowseButtonHelper {

            private File svsFile;
            private File annotationFile;

            BrowseButtonHelper() {
            }

            void setSvsFile(File svsFile) {
                this.svsFile = svsFile;
            }

            File getSvsFile() {
                return svsFile;
            }

            void setAnnotationFile(File annotationFile) {
                this.annotationFile = annotationFile;
            }

            File getAnnotationFile() {
                return annotationFile;
            }
        }

        final BrowseButtonHelper browseButtonHelper = new BrowseButtonHelper();

        /**
         * handles file from browse button AS WELL AS from drag and drop event
         */
        class BrowseButtonEventHandler implements EventHandler {

            @Override
            public void handle(Event event) {
                Dragboard db;
                if (event instanceof DragEvent) { // drag event
                    DragEvent dragEvent = (DragEvent) event;
                    db = dragEvent.getDragboard();
                    boolean success = false;
                    if (db.hasFiles()) {
                        success = true;
                        // just get the first file
                        browseButtonHelper.setSvsFile(db.getFiles().get(0));
                        dragEvent.setDropCompleted(success);
                    } else {
                        // no file dragged ... just return
                        return;
                    }
                } else { // mouse click event on browse button
                    // disable next button ... in case reselecting file failed ...
                    nextBackButtonHBox.getNextButton().setDisable(true);
                    File inputSvsFile = guidedManualScorerStage.askForSingleFile( //Show open file dialog
                            ViewConstants.GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_ASK_FOR_FILE_TITLE,
                            new FileChooser.ExtensionFilter("Aperio/Hamamatsu virtual slide file (*.svs/*.ndpi)", "*.svs", "*.ndpi")); // single file select
                    if (inputSvsFile == null) {
                        // no file chosen ... just return
                        return;
                    }
                    browseButtonHelper.setSvsFile(inputSvsFile);
                }
                event.consume();

                if (browseButtonHelper.getSvsFile().canRead()) {
                    String vendorName = browseButtonHelper.getSvsFile().getName().endsWith(".svs") ? "Aperio" : "Hamamatsu";
                    svsFileLabel.setText(vendorName + " virtual slide file selected: " + browseButtonHelper.getSvsFile().getPath());

                    // check to see if corresponding xml file exists ...
                    // First choice, choose annotationSaveFile, if this does not exist
                    // choose annotationFile, if this does not exist either,
                    // give error message on annotationFileLabel and do not allow
                    // to continue
                    String annotationFilename = GuidedManualScorer.getDefaultAnnotationFilename(browseButtonHelper.getSvsFile());
                    String annotationSaveFilename = GuidedManualScorer.getDefaultSaveAnnotationsFilename(browseButtonHelper.getSvsFile());

                    File annotationFile = new File(annotationSaveFilename);
                    annotationFile = annotationFile.canRead() ? annotationFile : (new File(annotationFilename));
                    if (annotationFile.canRead()) {
                        browseButtonHelper.setAnnotationFile(annotationFile);
                        annotationFileLabel.setText("annotation file selected: " + annotationFile.getPath());
                        nextBackButtonHBox.getNextButton().setDisable(false); // enable next button because file is selected
                    } else {
                        String errorMsg = "ERROR: annotation file is missing or not readable.  "
                                + "Please make sure annotation file exists and is readable and re-select " + vendorName + " virtual slide file.  "
                                + "Please note, annotation file must have the same name as the " + vendorName + " virtual slide file. e.g. " + annotationFilename;
                        guidedManualScorerStage.showErrorDialog(errorMsg);
                        annotationFileLabel.setText(errorMsg);
                        nextBackButtonHBox.getNextButton().setDisable(true); // the next button may have been enabled for a previous valid file input
                    }
                    // set focus to next button
                    nextBackButtonHBox.getNextButton().requestFocus();
                }
            }
        }

        browseButton.setOnMouseClicked(new BrowseButtonEventHandler());

        nextBackButtonHBox.getNextButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    decideWhatToDoWithAperioFile(browseButtonHelper.getSvsFile(), browseButtonHelper.getAnnotationFile());
                } catch (FormatException | IOException | InterruptedException ex) {
                    guidedManualScorerStage.closeWaitDialog();
                    guidedManualScorerStage.showErrorDialog("Failed to read file: " + browseButtonHelper.getSvsFile().getPath() + ";  Reason(s): " + ex);
                    guidedManualScorer.clear();
                }
            }
        });
        /// end of action listeners
        ////////////////////////////////////////////////////////////////////////        

        // add all the nodes to root and add root to scene
        root.setTop(notice);
        root.setCenter(fileLabelsBox);
        root.setRight(browseButton);
        root.setBottom(nextBackButtonHBox);

        guidedManualScorerStage.setTitle(ViewConstants.GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_ASK_FOR_FILE_TITLE);
        guidedManualScorerStage.setWidth(ViewConstants.START_PANE_WIDTH);
        guidedManualScorerStage.setHeight(ViewConstants.START_PANE_HEIGHT);
        guidedManualScorerStage.show(root);

        // drag and drop virtual slide file
        guidedManualScorerStage.getStage().getScene().setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    if (db.getFiles().size() == 1 ? supportedFileNameFilter.accept(db.getFiles().get(0)) : false) {
                        event.acceptTransferModes(TransferMode.COPY);
                    }
                } else {
                    event.consume();
                }
            }
        });
        guidedManualScorerStage.getStage().getScene().setOnDragDropped(new BrowseButtonEventHandler());
    }

    /**
     * decide what to do with aperio file ...
     *
     * 1. show window to verify tumor area selection & sample id 2. generate
     * consolidated tumor area selection 3. generate random virtual TMA cores 4.
     * do scoring
     *
     * @param svsFile
     * @param annotationFile
     * @throws FormatException
     * @throws IOException
     * @throws InterruptedException
     */
    private void decideWhatToDoWithAperioFile(File svsFile, File annotationFile) throws FormatException, IOException, InterruptedException {
        guidedManualScorerStage.showWaitDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_READING_SVS); // show wait message
        PreviewVirtualSlideHelperService helper = new PreviewVirtualSlideHelperService(svsFile, annotationFile);
        helper.start();
        helper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                try {
                    // clear guidedManualScorer object
                    guidedManualScorer.clear();
                    // get the virtual slide reader from WorkerStateEvent object
                    guidedManualScorer.setVirtualSlideReader(((PreviewVirtualSlideInfo) t.getSource().getValue()).getVirtualSlideReader());
                    // set the rescore status on the annotation file
                    boolean rescore = false;
                    if (scoreType == GuidedManualScorerReport.ScoreType.RANDOM) {
                        if (!guidedManualScorer.isRescoreStatusSet()) {
                            // ask if this is a rescore.
                            rescore = guidedManualScorerStage.showNoticeConfirmDialog("Is this a rescore?",
                                    "Please confirm: is this a rescore?\n\n"
                                    + "click \"Yes\" to indicate that this is a rescore. A note will be printed\n"
                                    + "on the final report indicating that this is a rescore."
                                    + "\n\n"
                                    + "click \"No\" to indicate that this is NOT a rescore (i.e. this is the\n"
                                    + "initial scoring.)  If the result from this scoring indicates that an\n"
                                    + "external rescore is required, a box will be printed on the final\n"
                                    + "report to allow you to enter the results from external rescore(s)."
                            );

                        }
                    }
                    guidedManualScorer.getVirtualSlideReader().getAnnotations().setRescore(rescore ? RescoreStatus.RESCORE : RescoreStatus.INITIAL);

                    // support only ONE layer of annotation ... need to check
                    if (((PreviewVirtualSlideInfo) t.getSource().getValue()).getVirtualSlideReader().getNumOriginalAnnotationLayers() > 1) {
                        guidedManualScorerStage.showErrorDialog(
                                "Currently support only ONE layer of Aperio annotation.  "
                                //+ "Please edit the annotation via ImageScope and reselect the Aperio/Hamamatsu/Leica virtual slide file (*.svs/*.ndpi/*.scn).");
                                + "Please edit the annotation via ImageScope and reselect the Aperio/Hamamatsu virtual slide file (" + supportedFileNameExtensions + ").");
                        guidedManualScorerStage.closeWaitDialog(); // want to close wait dialog JUST BEFORE the next action
                        askForAperioFile();
                    } else if ((!guidedManualScorer.getVirtualSlideReader().getAnnotations().randomVirtualTmaCoresExist())
                            & (!guidedManualScorer.getVirtualSlideReader().getAnnotations().manuallySelectedVirtualTmaCoresExist())) {
                        guidedManualScorerStage.closeWaitDialog(); // want to close wait dialog JUST BEFORE the next action
                        // random virtual cores do not exist ... need to verity tumor area selection and re-generate random virtual tma cores
                        displayVerifyTumorAreaSelection(
                                SwingFXUtils.toFXImage(((PreviewVirtualSlideInfo) t.getSource().getValue()).getLabel(), null),
                                SwingFXUtils.toFXImage(((PreviewVirtualSlideInfo) t.getSource().getValue()).getSetSelectionThumbWithRoi(), null),
                                true // disable the next button to make sure the user enters all required info
                        );
                    } else {
                        // random virtual core exist already!!! go to scoring!!!
                        guidedManualScorer.getVirtualSlideReader().preprocessAnnotationSelections(); // preprocess annotation regions
                        // generate all the images first
                        ArrayList<Image> images = new ArrayList<>();
                        ArrayList<Region> regions = new ArrayList<>();
                        if (guidedManualScorer.getVirtualSlideReader().getAnnotations().randomVirtualTmaCoresExist()) {
                            for (Region region : guidedManualScorer.getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getRegions().getRegions()) {
                                images.add(SwingFXUtils.toFXImage(guidedManualScorer.getVirtualSlideReader().getRegionAsBufferedImage(region), null));
                                regions.add(region);
                            }
                        } else if (guidedManualScorer.getVirtualSlideReader().getAnnotations().manuallySelectedVirtualTmaCoresExist()) {
                            for (Region region : guidedManualScorer.getVirtualSlideReader().getAnnotations().getManuallySelectedVirtualTmaCores().getRegions().getRegions()) {
                                images.add(SwingFXUtils.toFXImage(guidedManualScorer.getVirtualSlideReader().getRegionAsBufferedImage(region), null));
                                regions.add(region);
                            }
                        }
                        guidedManualScorerStage.closeWaitDialog(); // want to close wait dialog JUST BEFORE the next action
                        // do scoring on first core                    
                        doScoring(regions, images, 0);
                    }
                } catch (IOException | VirtualSlideReaderException | FormatException | RegionNotPreprocessedException | RegionNotSupportedException e) {
                    dealWithExceptionWhileDealingWithAperioFile(e);
                } catch (MyDialogClosedByUserException ex) {
                    // exit !!!
                    guidedManualScorerStage.showErrorDialog("You did not indicate whether this is a rescore.  Unable to continue.\n\nThis application will now exit.  Please try again.  Bye.");
                    System.exit(0);
                }
            }
        });
    }

    /**
     * display info to verify tumor area selection
     *
     * @param labelImage
     * @param selectionThumb
     * @param disableNextButton - whether to disable the next button: disable
     * the first time, but if its from a "back" action, do not disable it
     */
    private void displayVerifyTumorAreaSelection(Image labelImage, Image selectionThumb, boolean disableNextButton) throws VirtualSlideReaderException {
        Label notice = new Label(ViewConstants.GUIDED_MANUEL_SCORER_RANDOM_VIRTUAL_CORE_VERIFY_TUMOR_SELECTION);
        notice.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        notice.setWrapText(true);
        final boolean rescore = guidedManualScorer.isRescore(); // rescore status; final is needed, since inner class need to access it.

        VBox labelVBox = new VBox();
        //Image labelImage = SwingFXUtils.toFXImage(((PreviewVirtualSlideInfo) t.getSource().getValue()).getLabel(), null);
        ImageView labelView = new ImageView(labelImage);
        labelView.setPreserveRatio(true);
        labelView.setFitWidth(ViewConstants.VERIFY_TUMOR_SELECTION_IMAGE_WIDTH);
        labelVBox.setAlignment(Pos.CENTER_LEFT);
        Label labelSlideLabel = new Label("Slide Label");
        labelSlideLabel.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        labelVBox.getChildren().addAll(labelSlideLabel, labelView);

        VBox selectionVBox = new VBox();
        //Image selectionThumb = SwingFXUtils.toFXImage(((PreviewVirtualSlideInfo) t.getSource().getValue()).getSetSelectionThumbWithRoi(), null);
        ImageView selectionThumbView = new ImageView(selectionThumb);
        selectionThumbView.setPreserveRatio(true);
        selectionThumbView.setFitWidth(ViewConstants.VERIFY_TUMOR_SELECTION_IMAGE_WIDTH);
        selectionVBox.setAlignment(Pos.CENTER_LEFT);
        Label labelTumorAreaSelection = new Label("Tumor area selection");
        labelTumorAreaSelection.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        selectionVBox.getChildren().addAll(labelTumorAreaSelection, selectionThumbView);

        HBox imageHBox = new HBox();
        imageHBox.setSpacing(ViewConstants.LARGE_SPACING);
        imageHBox.getChildren().addAll(labelVBox, selectionVBox);

        final TextFieldHBox idInputHBox = new TextFieldHBox(
                "LUMINA subject identification number: ",
                ViewConstants.MSG_GUIDED_MANUAL_SCORER_PLEASE_ENTER_LUMINA_ID,
                "LUMINA subject ID",
                ViewConstants.TEXTFIELD_MEDIUM_WIDTH);
        String slideId = guidedManualScorer.getVirtualSlideReader().getAnnotations().getSlideId();
        idInputHBox.getInputTextField().setText(slideId == null ? "" : slideId);

        final TextFieldHBox pathologistNameInputHBox = new TextFieldHBox(
                "Pathologist name: ",
                ViewConstants.MSG_GUIDED_MANUAL_SCORER_PLEASE_ENTER_YOUR_NAME,
                "i.e. your name",
                ViewConstants.TEXTFIELD_LONG_WIDTH);
        String pathologistName = guidedManualScorer.getVirtualSlideReader().getAnnotations().getPathologistName();
        pathologistNameInputHBox.getInputTextField().setText(pathologistName == null ? "" : pathologistName);

        final TextFieldHBox coreSizeInputHBox = new TextFieldHBox(
                (scoreType == GuidedManualScorerReport.ScoreType.RANDOM ? ViewConstants.RANDOM_VIRTUAL_TMA_CORE : ViewConstants.VIRTUAL_TMA_CORE) + " diameter (in micron):",
                "", ViewConstants.MSG_GUIDED_MANUAL_SCORE_SIZE_IN_MICRO_METER,
                ViewConstants.TEXTFIELD_SHORT_WIDTH
        );
        coreSizeInputHBox.getInputTextField().setText("" + GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_DIAMETER_IN_MICROMETER);

        final DatePicker datePicker = new DatePicker();
        final TextFieldHBox dateReceivedInputHBox = new TextFieldHBox(
                datePicker,
                "Date specimen received: ",
                "Please enter date specimen received.",
                "Date",
                ViewConstants.TEXTFIELD_SHORT_WIDTH);

        final TextFieldHBox randomSeedInputHBox = new TextFieldHBox(
                "(Optional) Random seed (integer): ",
                ViewConstants.GUIDED_MANUAL_SCORER_RANDOM_SEED_HINT,
                "Random seed (integer)",
                ViewConstants.TEXTFIELD_SHORT_WIDTH);
        randomSeedInputHBox.getInputTextField().setText("" + guidedManualScorer.getVirtualSlideReader().getAnnotations().getRandomSeed());

        VBox mainContentVBox = new VBox();
        mainContentVBox.setPadding(new Insets(ViewConstants.LARGE_SPACING, 0, ViewConstants.LARGE_SPACING, 0));
        mainContentVBox.setSpacing(ViewConstants.LARGE_SPACING);
        if (rescore) {
            // do not show specimen received date for rescore
            mainContentVBox.getChildren().addAll(pathologistNameInputHBox, imageHBox, idInputHBox, randomSeedInputHBox);
        } else {
            if (scoreType == GuidedManualScorerReport.ScoreType.RANDOM) {
                mainContentVBox.getChildren().addAll(pathologistNameInputHBox, imageHBox, idInputHBox, dateReceivedInputHBox, randomSeedInputHBox);
            } else if (scoreType == GuidedManualScorerReport.ScoreType.MANUAL) {
                mainContentVBox.getChildren().addAll(pathologistNameInputHBox, imageHBox, coreSizeInputHBox, idInputHBox, dateReceivedInputHBox, randomSeedInputHBox);
            }
        }
        // add next/back button
        final NextBackButtonHBox nextBackButtonHBox = new NextBackButtonHBox(disableNextButton);

        ////////////////////////////////////////////////////////////////////////
        // action listeners ...
        EventHandler inputCheck = new EventHandler() {
            @Override
            public void handle(Event event) {
                String userPathologistNameInput = pathologistNameInputHBox.getInputText();
                String userCoreSizeInput = coreSizeInputHBox.getInputText();
                String userIdInput = idInputHBox.getInputText();
                String userRandomSeedInput = randomSeedInputHBox.getInputText();
                Date userDateReceivedInput = rescore ? null : datePicker.getSelectedDate();
                boolean isNumeric = true;
                try {
                    Integer.parseInt(userCoreSizeInput);
                    Integer.parseInt(userRandomSeedInput);
                } catch (NumberFormatException nfe) {
                    isNumeric = false;
                }
                if (isNumeric
                        & (userPathologistNameInput != null ? userPathologistNameInput.trim().length() > 0 : false)
                        & (userIdInput != null ? userIdInput.trim().length() > 0 : false)
                        & (rescore ? true : (userDateReceivedInput != null))) {
                    nextBackButtonHBox.getNextButton().setDisable(false);
                } else {
                    nextBackButtonHBox.getNextButton().setDisable(true);
                }
            }
        };
        pathologistNameInputHBox.getInputTextField().setOnKeyTyped(inputCheck);
        idInputHBox.getInputTextField().setOnKeyTyped(inputCheck);
        if (!rescore) {
            datePicker.setWhatToDoWhenPopupCloses(inputCheck);
            if (scoreType == GuidedManualScorerReport.ScoreType.MANUAL) {
                coreSizeInputHBox.setOnKeyTyped(inputCheck);
            }
        }
        randomSeedInputHBox.getInputTextField().setOnKeyTyped(inputCheck);

        nextBackButtonHBox.getBackButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                askForAperioFile();
            }
        });
        nextBackButtonHBox.getNextButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    // get the user inputs here ...
                    // making sure use entered something in idInput is done in idInput.setOnKeyTyped(...)
                    guidedManualScorer.getVirtualSlideReader().getAnnotations().setPathologistName(pathologistNameInputHBox.getInputText().trim());
                    guidedManualScorer.getVirtualSlideReader().getAnnotations().setSlideId(idInputHBox.getInputText().trim());
                    // get md5sum of virtual slide
                    guidedManualScorer.getVirtualSlideReader().getAnnotations().setMd5Sum(MiscUtil.md5Sum(guidedManualScorer.getVirtualSlideReader().getVirtualSlideFile()));
                    // get the random seed
                    guidedManualScorer.getVirtualSlideReader().getAnnotations().setRandomSeed(Integer.parseInt(randomSeedInputHBox.getInputText()));
                    // get the date specimen received
                    guidedManualScorer.getVirtualSlideReader().getAnnotations().setSpecimenReceivedDate(rescore ? null : datePicker.getSelectedDate());
                    // set core diameter
                    coreDiameterInMicron = Integer.parseInt(coreSizeInputHBox.getInputText());
                    // generate random cores!!!
                    generateVirtualTmaCores();
                    // also set trial centre here ...
                    guidedManualScorer.getVirtualSlideReader().getAnnotations().setTrialCentre(ViewConstants.TRIAL_CENTRE);
                } catch (VirtualSlideReaderException | RegionNotPreprocessedException | RegionNotSupportedException | RegionGeneratorMaxTriesExceeded | IOException | NoSuchAlgorithmException ex) {
                    dealWithExceptionWhileDealingWithAperioFile(ex);
                }
            }
        });
        /// end of action listeners
        ////////////////////////////////////////////////////////////////////////      

        // put everything to root
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(ViewConstants.INSETS_WIDTH));
        root.setTop(notice);
        root.setCenter(mainContentVBox);
        root.setBottom(nextBackButtonHBox);
        guidedManualScorerStage.setTitle(ViewConstants.GUIDED_MANUEL_SCORER_RANDOM_VIRTUAL_CORE_VERIFY_TUMOR_SELECTION);
        guidedManualScorerStage.setWidth(ViewConstants.MAIN_PANE_WIDTH);
        int height = ViewConstants.MAIN_PANE_HEIGHT;
        switch (scoreType) {
            case MANUAL:
                height = ViewConstants.MAIN_PANE_HEIGHT_MANUAL;
                break;
            case RANDOM:
                height = ViewConstants.MAIN_PANE_HEIGHT_RANDOM;
                break;
            default:
                height = ViewConstants.MAIN_PANE_HEIGHT;
                break;

        }
        guidedManualScorerStage.setHeight(height);
        guidedManualScorerStage.show(root);
    }

    /**
     * things to do when exception caught during processing aperio file
     */
    private void dealWithExceptionWhileDealingWithAperioFile(Exception ex) {
        guidedManualScorerStage.showErrorDialog("Error occured (" + ex + ").  Please try again.");
        askForAperioFile();
    }

    /**
     * generate random core
     *
     * use default random number generator ... NOTE: the random number generator
     * is RESET when annotations.setRandomSeed() is called.
     *
     * @throws VirtualSlideReaderException
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    private void generateVirtualTmaCores() throws VirtualSlideReaderException, RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {

        if (scoreType == GuidedManualScorerReport.ScoreType.RANDOM) {
            guidedManualScorerStage.showWaitDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_GENERATING_CORES);
            GenerateRandomVirtualTmaCoresHelperService helper = new GenerateRandomVirtualTmaCoresHelperService(
                    guidedManualScorer.getVirtualSlideReader(),
                    coreDiameterInMicron,//GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_DIAMETER_IN_MICROMETER,
                    true // generate initial X cores
            );
            helper.start();
            /**
             * need to catch exception if max tries exceeded
             */
            helper.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    guidedManualScorerStage.closeWaitDialog();
                    if (t.getSource().getException() instanceof RegionGeneratorMaxTriesExceeded) {
                        // show error message
                        guidedManualScorerStage.showErrorDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_FAILED_TO_GENERATE_CORES);
                    } else {
                        // unexpected error ... show error message anyways
                        guidedManualScorerStage.showErrorDialog(t.getSource().getException().toString());
                    }
                    // go back to instruction page
                    displayRandomVirtualTMACoreInstructions();
                }
            });

            /**
             * ok to continue!!!
             */
            helper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    try {
                        ArrayList<Image> images = null;
                        ArrayList<Region> regions = new ArrayList<>();
                        // generate all the images first
                        // generate random virtual tma core
                        images = ((RandomVirtualTmaCoreImages) (t.getSource().getValue())).getImages();
                        for (Region r : guidedManualScorer.getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getRegions().getRegions()) {
                            regions.add(r);
                        }
                        guidedManualScorerStage.closeWaitDialog(); // close wait dialog here as the above are pretty CPU intensive as well
                        // do scoring on first core                    
                        doScoring(regions, images, 0);
                    } catch (VirtualSlideReaderException | FormatException | RegionNotPreprocessedException | IOException | RegionNotSupportedException ex) {
                        guidedManualScorerStage.closeWaitDialog();
                        System.err.println(ex);
                    }
                }
            });
        } else if (scoreType == GuidedManualScorerReport.ScoreType.MANUAL) {
            try {
                // show manual field selection
                final FieldSelectorPanel fsp = new FieldSelectorPanel(
                        guidedManualScorer.getVirtualSlideReader().getLowResView(),
                        MAIN_PANE_WIDTH, // width 
                        MAIN_PANE_HEIGHT, // height 
                        guidedManualScorer.getVirtualSlideReader().getLowResViewScale(),// scaleToOriginal
                        "", //fieldSelectorParamString 
                        coreDiameterInMicron * 2,//2000, // defaultFieldDiameter ... 4000 for 40x, 2000 for 20x corresponding to 1 mm.
                        FieldSelectorConstants.FIELD_SELECTOR_STATE_SELECTING, //FieldSelectorConstants.FIELD_SELECTOR_STATE_SCORING,
                        20, 2, 2, 2, // maxNumFieldHigh/Medium/Low/Negligible
                        true, true // showHotspot / showOtherFields
                );
                fsp.addListners(null);
                fsp.setDebug(true); // true == show nuclei count order
                fsp.setKi67SelectionStateToHigh();
                fsp.setPreferredSize(new Dimension(MAIN_PANE_WIDTH, MAIN_PANE_HEIGHT));

                final SwingNode swingNode = new SwingNode();
                swingNode.setContent(fsp);

                VBox root = new VBox();
                root.getChildren().add(swingNode);

                NextBackButtonHBox controlButtons = new NextBackButtonHBox(false);
                controlButtons.getNextButton().setText("finished field selection");
                controlButtons.getNextButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (fsp.getfieldSelectionParamString().length() == 0) {
                            // do not allow empty selection!
                            guidedManualScorerStage.showErrorDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_PLEASE_SELECT_A_FIELD);
                        } else {
                            try {
                                guidedManualScorer.getVirtualSlideReader().generateManualSelectedVirtualTmaCore(fsp.getSelectedFields());
                                ArrayList<Image> images = new ArrayList();
                                ArrayList<Region> regions = new ArrayList<>();
                                for (Region region : guidedManualScorer.getVirtualSlideReader().getAnnotations().getManuallySelectedVirtualTmaCores().getRegions().getRegions()) {
                                    regions.add(region);
                                    images.add(SwingFXUtils.toFXImage(guidedManualScorer.getVirtualSlideReader().getRegionAsBufferedImage(region), null));
                                }
                                // do scoring on first core                    
                                doScoring(regions, images, 0);
                            } catch (VirtualSlideReaderException | RegionNotSupportedException | RegionNotPreprocessedException | FormatException | IOException ex) {
                                guidedManualScorerStage.showErrorDialog(ex.toString());
                            }

                        }
                    }
                });
                controlButtons.getBackButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        // revious step
                        try {
                            decideWhatToDoWithAperioFile(guidedManualScorer.getVirtualSlideReader().getVirtualSlideFile(), guidedManualScorer.getVirtualSlideReader().getAnnotationFile());
                        } catch (FormatException | IOException | InterruptedException fe) {
                            try {
                                showStartPage();
                            } catch (IOException ioe) {
                                guidedManualScorerStage.showErrorDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_UNRECOVERABLE_ERROR + "..." + ioe);
                                System.exit(1);
                            }
                        }
                    }
                });
                MyButton removeFieldButton = new MyButton("remove currently selected field");
                controlButtons.getChildren().add(0, removeFieldButton);
                removeFieldButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        fsp.removeCurrentViewingField();
                    }
                });

                root.getChildren().add(controlButtons);
                root.setAlignment(Pos.CENTER);

                Scene scene = new Scene(root, MAIN_PANE_WIDTH, MAIN_PANE_HEIGHT);

                guidedManualScorerStage.getStage().setScene(scene);
                guidedManualScorerStage.getStage().sizeToScene();
                guidedManualScorerStage.getStage().setResizable(false);
                guidedManualScorerStage.getStage().show();

            } catch (FieldSelectionParamStringParseException | FormatException | IOException ex) {
                System.err.println(ex);
            }
        }
    }

    /**
     * do the scoring!!!
     *
     * WARNING!!! images must be in the SAME ORDER as regions
     *
     * @param inputRegions
     * @param inputImages
     * @param inputIndex
     * @throws RegionNotPreprocessedException
     * @throws FormatException
     * @throws IOException
     * @throws RegionNotSupportedException
     */
    private void doScoring(ArrayList<Region> inputRegions, ArrayList<Image> inputImages, int inputIndex) throws RegionNotPreprocessedException, FormatException, IOException, RegionNotSupportedException {
        final int index = inputIndex;
        final ArrayList<Region> regions = inputRegions;
        final ArrayList<Image> images = inputImages;
        final boolean isLastCore = (index == (images.size() - 1));

        // do some qc ...
        if (regions.size() != images.size()) {
            throw new AnnotationProcessingRuntimeException("in GuidedManualScorerController doScoring(): regions.size != images.size()");
        }

        // 1. get the region
        final Region region = regions.get(index);
        // 2. get the image
        Image image = images.get(index);
        if (region.getNucleiSelections() == null) {
            // first time scoring ... create a new NucleiSelection object
            region.setNucleiSelections(new NucleiSelections());
        }

        final NextBackSaveButtonHBox nextBackSaveButtonHBox = new NextBackSaveButtonHBox(
                false //   region.getNucleiSelections().getNumTotal() < GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT
        ); // NO LONGER disable next button if existing nuclei count < required count
        // reason for CHANGE of rule: there will be some cores with < 100 cells and we would NOT like to
        // skip the core ... therefore we need to change to make sure at the end there are at least 500 cells counted!!!

        CounterZoomPanImageView counterZoomPanImageView = new CounterZoomPanImageView(image, region.getNucleiSelections(), guidedManualScorerStage.getStage());

        // add reminder if doing random cores
        if (scoreType == GuidedManualScorerReport.ScoreType.RANDOM) {
            counterZoomPanImageView.addReminderAction(new ReminderDialogMessage(
                    0,
                    ViewConstants.MSG_GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_START_SCORE,
                    guidedManualScorerStage.getStage()));
            counterZoomPanImageView.addReminderAction(new ReminderDialogMessage(
                    Math.round(GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT / 2),
                    ViewConstants.MSG_GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_SCORED_HALF_COMPLETED,
                    guidedManualScorerStage.getStage()));
            if (!isLastCore) {
                counterZoomPanImageView.addReminderAction(new ReminderDialogMessage(
                        GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT,
                        ViewConstants.MSG_GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_SCORED_COMPLETED,
                        guidedManualScorerStage.getStage()));
            } else {
                // last core!!!
                String notificationMessage;
                if ((index + 1) == GuidedManualScorer.GUIDED_MANUAL_SCORER_NUMBER_OF_RANDOM_TMA_CORES_TO_GENERATE) {
                    notificationMessage = ViewConstants.MSG_GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_SCORED_COMPLETED_LAST_ONE;
                } else {
                    // scoring additional scores now.
                    notificationMessage = ViewConstants.MSG_GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_SCORED_COMPLETED_ADDITIONAL;
                }
                counterZoomPanImageView.addReminderAction(new ReminderDialogMessage(
                        GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT,
                        notificationMessage,
                        guidedManualScorerStage.getStage()));
            }
        }
        //counterZoomPanImageView.addReminderAction(new ReminderEnableButton(
        //        GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT,
        //        nextBackSaveButtonHBox.getNextButton())); // enable next button when count reaches 100

        Label counterCartoonTitleLabel;
        if ((index + 1) > GuidedManualScorer.GUIDED_MANUAL_SCORER_NUMBER_OF_RANDOM_TMA_CORES_TO_GENERATE) {
            counterCartoonTitleLabel = new Label("Nuclei count on additional\n" + ViewConstants.RANDOM_VIRTUAL_TMA_CORE + ":\n\n# " + (index + 1 - GuidedManualScorer.GUIDED_MANUAL_SCORER_NUMBER_OF_RANDOM_TMA_CORES_TO_GENERATE));
        } else {
            counterCartoonTitleLabel = new Label("Nuclei count on " + ViewConstants.RANDOM_VIRTUAL_TMA_CORE + ":\n\n# " + (index + 1) + " of (initial) " + GuidedManualScorer.GUIDED_MANUAL_SCORER_NUMBER_OF_RANDOM_TMA_CORES_TO_GENERATE);
        }
        counterCartoonTitleLabel.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);
        counterCartoonTitleLabel.setWrapText(true);
        final TextArea commentBox = new TextArea(region.getNucleiSelections().getComment());
        commentBox.setWrapText(true);
        commentBox.setPromptText(ViewConstants.GUIDED_MANUAL_SCORER_COMMENT_INPUT_PROMPT_TEXT);
        commentBox.setStyle(ViewConstants.DEFAULT_APP_TEXT_STYLE);

        CounterCartoon counterCartoon = new CounterCartoon(0, 0);
        counterZoomPanImageView.setCounterCartoon(counterCartoon);

        VBox counterCartoonVBox = new VBox();
        counterCartoonVBox.setAlignment(Pos.CENTER);
        counterCartoonVBox.setPadding(new Insets(0)); //counterCartoonVBox.setPadding(ViewConstants.getDefaultInsets());
        counterCartoonVBox.setSpacing(ViewConstants.SPACING);
        counterCartoonVBox.getChildren().addAll(counterCartoonTitleLabel, counterCartoon, commentBox);

        HBox counterBox = new HBox();
        counterBox.setAlignment(Pos.CENTER);
        counterBox.setPadding(ViewConstants.getDefaultInsets());
        counterBox.getChildren().addAll(counterZoomPanImageView, counterCartoonVBox);

        VBox mainContentVBox = new VBox();
        mainContentVBox.getChildren().add(counterBox);
        mainContentVBox.setAlignment(Pos.CENTER);
        mainContentVBox.setPadding(ViewConstants.getDefaultInsets());

        ////////////////////////////////////////////////////////////////////////
        // action listeners ...
        nextBackSaveButtonHBox.getBackButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    if (index == 0) {
                        try {
                            if (guidedManualScorerStage.showWarningConfirmDialog("Discard ALL scores?", "ALL scores will be discarded.  Are you sure?")) {
                                // remove consolidated annotation AND random virtual TMA cores
                                guidedManualScorer.getVirtualSlideReader().resetAnnotationBackToOriginal();
                                // conslidate tumor area selection
                                guidedManualScorer.getVirtualSlideReader().getAnnotations().consolidateRegions();
                                guidedManualScorer.getVirtualSlideReader().preprocessAnnotationSelections();
                                // verify annotation and regenerate and random virtual TMA cores
                                displayVerifyTumorAreaSelection(
                                        SwingFXUtils.toFXImage(guidedManualScorer.getVirtualSlideReader().getLabel(), null),
                                        SwingFXUtils.toFXImage(guidedManualScorer.getVirtualSlideReader().getThumbImageWithRoi(Annotation.ANNOTATION_TYPE_CONSOLIDATED_SELECTION), null),
                                        false // no need to disable the next button since this is from a back action and all info are filled in already 
                                );
                            }
                        } catch (MyDialogClosedByUserException ex) {
                            // do nothing
                        } catch (JAXBException | FileNotFoundException | VirtualSlideReaderException | FormatException | RegionNotSupportedException | RegionNotPreprocessedException ex) {
                            System.err.println(ex);
                        }
                    } else {
                        try {
                            // save any comments
                            region.getNucleiSelections().setComment(commentBox.getText());
                            // go back to previous core
                            doScoring(regions, images, index - 1);
                        } catch (RegionNotPreprocessedException | FormatException | IOException | RegionNotSupportedException ex) {
                            System.err.println(ex);
                        }
                    }
                } catch (IOException ex) {
                    guidedManualScorerStage.closeWaitDialog();
                    guidedManualScorerStage.showErrorDialog("Failed to read file: " + guidedManualScorer.getVirtualSlideReader().getVirtualSlideFile().getPath() + ";  Reason(s): " + ex);
                    guidedManualScorer.clear();
                }
            }
        });
        nextBackSaveButtonHBox.getNextButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // save any comments
                region.getNucleiSelections().setComment(commentBox.getText());
                if (!isLastCore) {
                    // continue scoring the next core
                    try {
                        doScoring(regions, images, index + 1);
                    } catch (RegionNotPreprocessedException | FormatException | IOException | RegionNotSupportedException ex) {
                        System.err.println(ex);
                    }
                } else {
                    // check to see if internal rescore is required (need to initialize a service
                    guidedManualScorerStage.showWaitDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_CHECK_NEED_INTERNAL_RESCORE); // show wait message
                    CheckNeedInternalRescoreHelperService helper = new CheckNeedInternalRescoreHelperService(guidedManualScorer);
                    helper.start();
                    helper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent t) {
                            guidedManualScorerStage.closeWaitDialog();
                            try {
                                boolean needInternalRescore = ((Boolean) t.getSource().getValue()).booleanValue();
                                boolean needToScoreMoreTumorCells = guidedManualScorer.needCountMoreTumorNuclei();
                                boolean maximumNumberOfRandomVirtualTMACoresReached = guidedManualScorer.maximumNumberOfRandomVirtualTMACoresReached();
                                if ((needInternalRescore | needToScoreMoreTumorCells) & !maximumNumberOfRandomVirtualTMACoresReached) {
                                    guidedManualScorerStage.showNoticeDialog(
                                            (needInternalRescore
                                                    ? ViewConstants.MSG_GUIDED_MANUAL_SCORER_HIGH_KI67_HETEROGENEITY_DETECTED
                                                    : "" + guidedManualScorer.getTotalNumNucleiSelected() + " tumor cells counted.\n"
                                                    + ViewConstants.MSG_GUIDED_MANUAL_SCORER_NOT_ENOUGH_TUMOR_CELLS_COUNTED));
                                    // need internal rescore
                                    // generate one additional random virtual tma cores 
                                    guidedManualScorerStage.showWaitDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_GENERATING_ADDITIONAL_CORE);
                                    GenerateRandomVirtualTmaCoresHelperService helper = new GenerateRandomVirtualTmaCoresHelperService(
                                            guidedManualScorer.getVirtualSlideReader(),
                                            GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_DIAMETER_IN_MICROMETER,
                                            false // generate additional (can overlap) core
                                    );
                                    helper.start();
                                    helper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                        @Override
                                        public void handle(WorkerStateEvent t) {
                                            try {
                                                // add newly generated core to region, images
                                                regions.add(guidedManualScorer.getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getRegions().getRegions().last());
                                                images.add(((RandomVirtualTmaCoreImages) (t.getSource().getValue())).getImages().get(0));
                                                guidedManualScorerStage.closeWaitDialog();
                                                doScoring(regions, images, index + 1);
                                            } catch (VirtualSlideReaderException | FormatException | RegionNotPreprocessedException | IOException | RegionNotSupportedException ex) {
                                                guidedManualScorerStage.closeWaitDialog();
                                                System.err.println(ex);
                                            }
                                        }
                                    });
                                } else {
                                    // ok to generate report ...

                                    // show warning message if random virtual tma cores generate stopped because of max. number resarched
                                    if (needInternalRescore & maximumNumberOfRandomVirtualTMACoresReached) {
                                        guidedManualScorerStage.showWarningDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_MAX_RANDOM_VIRTUAL_TMA_CORES_REACHED);
                                    } else {
                                        guidedManualScorerStage.showNoticeDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_READY_TO_GENERATE_REPORT);
                                    }

                                    // show warning message if at least one core with < min # of tumor nuclei counted 
                                    if (guidedManualScorer.atLeastOneCoreWithLessThanMinCount()) {
                                        guidedManualScorerStage.showWarningDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_AT_LEAST_ONE_VIRTUAL_TMA_CORE_WITH_LESS_THAN_MIN_COUNT);
                                    }

                                    // confirm
                                    try {
                                        if (guidedManualScorerStage.showWarningConfirmDialog(
                                                ViewConstants.MSG_GUIDED_MANUAL_SCORER_CONFIRM_GENERATE_REPORT_TITLE,
                                                ViewConstants.MSG_GUIDED_MANUAL_SCORER_CONFIRM_GENERATE_REPORT)) {
                                            guidedManualScorerStage.showWaitDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_GENERATING_REPORT);
                                            GenerateReportHelperService helper = new GenerateReportHelperService(guidedManualScorer);
                                            helper.start();
                                            helper.setOnFailed(new EventHandler<WorkerStateEvent>() {
                                                @Override
                                                public void handle(WorkerStateEvent t) {
                                                    guidedManualScorerStage.closeWaitDialog();
                                                    guidedManualScorerStage.showErrorDialog("Failed to generate report.\nReason(s): " + t.getSource().getException());
                                                }
                                            });
                                            helper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                                @Override
                                                public void handle(WorkerStateEvent t) {
                                                    guidedManualScorerStage.closeWaitDialog();
                                                    try {
                                                        if (guidedManualScorer.recommandExternalRescore() & !guidedManualScorer.isRescore()) {
                                                            // recommad external rescore
                                                            guidedManualScorerStage.showWarningDialog(ViewConstants.MSG_GUIDED_MANUAL_SCORER_EXTERNAL_RESCORE_RECOMMANDED);
                                                        }
                                                    } catch (VirtualSlideReaderException ex) {
                                                        // warning message regardig external rescore recommandation will be shown in 
                                                        // report anyways ... just ignore error here.
                                                    }
                                                    // show pdf
                                                    String reportFilename = (String) (t.getSource().getValue());
                                                    guidedManualScorerStage.showNoticeDialog("Ki67 score report saved to: " + reportFilename);
                                                    hostServices.showDocument(reportFilename);
                                                    guidedManualScorerStage.showNoticeDialog("This application will now exit.  Bye.");
                                                    guidedManualScorerStage.getStage().close();
                                                }
                                            });
                                        }
                                    } catch (MyDialogClosedByUserException e) {
                                        // do nothing
                                    }
                                } // else 
                            } catch (VirtualSlideReaderException ex) {
                                guidedManualScorerStage.showErrorDialog("Exception encounter while trying to check Ki67 scores: " + ex);
                            }
                        }
                    });
                } // else ... if (!isLastCore)
            }
        });
        nextBackSaveButtonHBox.getSaveButton().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    // save any comments
                    region.getNucleiSelections().setComment(commentBox.getText());
                    // save annotation
                    String outputFilename = guidedManualScorer.saveAnnotations();
                    guidedManualScorerStage.showNoticeDialog("Annotation saved successfully as " + outputFilename);
                } catch (JAXBException ex) {
                    guidedManualScorerStage.showErrorDialog("Failed to save annotation.\nError message: " + ex);
                }

            }
        });
        /// end of action listeners
        ////////////////////////////////////////////////////////////////////////    

        // put everything to root
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #FFFFFF;"); // set background to white because the cartoon is white as well :(
        root.setPadding(new Insets(ViewConstants.INSETS_WIDTH));
        guidedManualScorerStage.setTitle(ViewConstants.GUIDED_MANUEL_SCORER_RANDOM_VIRTUAL_CORE_PLEASE_SCORE);
        guidedManualScorerStage.setWidth(ViewConstants.SCORING_PANE_WIDTH);
        guidedManualScorerStage.setHeight(ViewConstants.MAIN_PANE_HEIGHT);
        guidedManualScorerStage.getStage().setResizable(true);
        root.setCenter(mainContentVBox);
        counterZoomPanImageView.reFitImageByWidth(ViewConstants.SCORING_PANE_WIDTH - ViewConstants.COUNTER_CARTOON_WIDTH);// need to refit image here AFTER all components on windows has been set.
        root.setBottom(nextBackSaveButtonHBox);
        guidedManualScorerStage.show(root);
    }

    /**
     * verify the scores by the following rules ... 1. if all 5 scores above or
     * below 13.5 cut point, no need to check further 2. find all unique pairs
     * and do MAXDEV and RMSE (Lisa & Mei criteria) if MAXDEV>1 or RMSE>0.6,
     * require additional scoring
     *
     * @throws GuidedManualScorerRequireAdditionalScoringException
     */
    public void verifyScores() throws GuidedManualScorerRequireAdditionalScoringException {
    }

    /**
     * display main page
     *
     * @param stage
     * @throws IOException
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        guidedManualScorerStage = new GuidedManualScorerStage(primaryStage);
        showStartPage();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
