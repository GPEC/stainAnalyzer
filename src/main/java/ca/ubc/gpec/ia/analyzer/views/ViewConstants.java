/*
 * Constants relating to views
 */
package ca.ubc.gpec.ia.analyzer.views;

import ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.GuidedManualScorer;
import ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport.TrialCentre;
import com.itextpdf.text.FontFactory;
import java.awt.Image;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.Date;
import javafx.geometry.Insets;

/**
 *
 * @author samuelc
 */
public class ViewConstants {

    public static final String RANDOM_VIRTUAL_TMA_CORE = "random intratumoral circle";
    public static final String VIRTUAL_TMA_CORE = "virtual TMA core";
    public static final String RESOURCE_SEPARATOR = "/";
    public static final String VIEWS_ROOT = "ca" + RESOURCE_SEPARATOR
            + "ubc" + RESOURCE_SEPARATOR
            + "gpec" + RESOURCE_SEPARATOR
            + "ia" + RESOURCE_SEPARATOR
            + "analyzer" + RESOURCE_SEPARATOR
            + "views" + RESOURCE_SEPARATOR;
    public static final int START_PANE_WIDTH = 800;
    public static final int START_PANE_HEIGHT = 500;
    public static final int MAIN_PANE_WIDTH = 800;
    public static final int MAIN_PANE_HEIGHT = 850;
    public static final int MAIN_PANE_HEIGHT_RANDOM = 750;
    public static final int MAIN_PANE_HEIGHT_MANUAL = 850;
    public static final int SCORING_PANE_WIDTH = 1020; // low resolution laptop is 1024 x 768
    public static final int SCORING_PANE_HEIGHT = 760;
    public static final double COUNTER_CARTOON_WIDTH = 320;
    public static final int TOOLTIP_SHORT_WIDTH = 200;
    public static final int TOOLTIP_LONG_WIDTH = 400;
    public static final int TEXTFIELD_SHORT_WIDTH = 100;
    public static final int TEXTFIELD_MEDIUM_WIDTH = 200;
    public static final int TEXTFIELD_LONG_WIDTH = 400;
    public static final int INSETS_WIDTH = 10;
    public static final int SPACING = 10; // spacing between components
    public static final int LARGE_SPACING = 20;
    public static final int TITLE_FONT_SIZE = 18;
    public static final String OK = "OK";
    public static final String YES = "Yes";
    public static final String NO = "No";
    public static final String DEFAULT_CSS_THEME_NAME = "theme";
    public static final String DEFAULT_CSS_FILENAME = "main.css";
    public static final String DIALOG_CSS_FILENAME = "dialog.css";
    // specific to guided manual scorer
    public static final String GUIDED_MANUAL_SCORER_SAVE_ANNOTATION_FILE_SUFFIX = "_GPEC";
    public static final String GUIDED_MANUAL_SCORER_KI67_SCORE_REPORT_FILE_SUFFIX = "_ki67_report.pdf";
    public static final String GUIDED_MANUAL_SCORER_KI67_CONSOLIDATED_SCORE_REPORT_SUFFIX = "_ki67_report_consolidated.pdf";
    public static final String GUIDED_MANUAL_SCORER_MAIN_PANE_TITLE = "GPEC - Guided manual Ki67 scorer";
    public static final String GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_INSTRUCTIONS_TITLE = "GPEC - Random virtual core instructions";
    public static final String GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_ASK_FOR_FILE_TITLE = "Please select the virtual slide file to score.";
    public static final String GUIDED_MANUEL_SCORER_RANDOM_VIRTUAL_CORE_VERIFY_TUMOR_SELECTION
            = "Please verify slide label and tumor area selection(s).  "
            + "Please make sure that the tumor area selection(s) looks EXACTLY as they appear in ImageScope.  "
            + "If they look different, please re-do the tumor area selection(s) on ImageScope and restart this application.  "
            + "Please note the following: "
            + "1) If the tumor area selection is composed of multiple free-hand lines, please make sure the lines are joined together (i.e. the ends of the joining lines must be less than 200 pixels apart. "
            + "2) ALL tumor area selections (i.e. annotations) must be in the FIRST layer.  "
            + "3) Selection by ImageScope's negative pen tool NOT supported. ";
    public static final String GUIDED_MANUEL_SCORER_RANDOM_VIRTUAL_CORE_PLEASE_SCORE = "Please score the " + RANDOM_VIRTUAL_TMA_CORE;//;virtual TMA core.";
    public static final int VERIFY_TUMOR_SELECTION_IMAGE_WIDTH = 250;
    public static final String MSG_GUIDED_MANUAL_SCORER_PLEASE_ENTER_LUMINA_ID = "Please enter LUMINA subject identification number.";
    public static final String MSG_GUIDED_MANUAL_SCORER_PLEASE_ENTER_YOUR_NAME = "Please enter your name (the pathologist who is doing the scoring).";
    public static final String MSG_GUIDED_MANUAL_SCORE_SIZE_IN_MICRO_METER = "size in micron";
    public static final String MSG_GUIDED_MANUAL_SCORER_UNRECOVERABLE_ERROR = "Sorry, application will exit now since an unrecoverable error has occurred.";
    public static final String MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_READING_SVS = "Reading Aperio virtual slide file.  Please wait ...";
    public static final String MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_GENERATING_CORES = "Generating " + RANDOM_VIRTUAL_TMA_CORE + "s.  Please wait ...";
    public static final String MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_GENERATING_ADDITIONAL_CORE = "Generating additional " + RANDOM_VIRTUAL_TMA_CORE + ".  Please wait ...";
    public static final String MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_GENERATING_REPORT = "Generating Ki67 score report.  Please wait ...";
    public static final String MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_CHECK_NEED_INTERNAL_RESCORE = "Please wait while the system assesses the heterogeneity of the Ki67 expression ...";
    public static final String MSG_GUIDED_MANUAL_SCORER_PLEASE_WAIT_CHECK_ANNOTATION_FILES_FOR_REPORT_CONSOLIDATION = "Please wait while the system checks the score result annotation files";
    public static final String MSG_GUIDED_MANUAL_SCORER_FAILED_TO_GENERATE_CORES
            = "Failed to generate " + RANDOM_VIRTUAL_TMA_CORE + "s.  Most likely this is caused by the tumor area selection being too small.  "
            + "For example, the tumor area selection must be large enough to enclose a 1 x 1 mm square when trying to generate " + RANDOM_VIRTUAL_TMA_CORE + " with diamater of 1 mm.  "
            + "Please re-select the tumor area.  Please note, this may mean selecting some of the NON-TUMOR or NON-TISSUE area.  "
            + "If tumor cellularity is too low on the whole slide, please consider using a different slide.";
    public static final String MSG_GUIDED_MANUAL_SCORER_PLEASE_SELECT_A_FIELD = "Please select a field";
    public static final String MSG_GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_START_SCORE = "Please start scoring from the top of the " + RANDOM_VIRTUAL_TMA_CORE + ".";
    public static final String MSG_GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_SCORED_HALF_COMPLETED = Math.round(GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT / 2) + " nuclei counted.  Please proceed to the bottom of the " + RANDOM_VIRTUAL_TMA_CORE + ".";
    public static final String MSG_GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_SCORED_COMPLETED = GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT + " nuclei counted.  Please proceed to the next " + RANDOM_VIRTUAL_TMA_CORE;
    public static final String MSG_GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_SCORED_COMPLETED_LAST_ONE
            = GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT
            + " nuclei counted.  Initial set of "
            + GuidedManualScorer.GUIDED_MANUAL_SCORER_NUMBER_OF_RANDOM_TMA_CORES_TO_GENERATE
            + " " + RANDOM_VIRTUAL_TMA_CORE + "s completed.\n\n"
            + "The system will now assess the heterogeneity of the Ki67 expression.\n"
            + "Sufficiently high heterogeneity will trigger (up to 20) additional \n" + RANDOM_VIRTUAL_TMA_CORE + "s to be generated for scoring.\n\n"
            + "Please press the 'next' button to continue.";
    public static final String MSG_GUIDED_MANUAL_SCORER_RANDOM_VIRTUAL_CORE_SCORED_COMPLETED_ADDITIONAL
            = GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT
            + " nuclei counted.\n\n"
            + "The system will now assess the heterogeneity of the Ki67 expression.\n"
            + "Sufficiently high heterogeneity will trigger (up to 20) \n" + RANDOM_VIRTUAL_TMA_CORE + "s to be generated for scoring.\n\n"
            + "Please press the 'next' button to continue.";
    public static final String MSG_GUIDED_MANUAL_SCORER_CONFIRM_GENERATE_REPORT_TITLE = "Generate Ki67 score report?";
    public static final String MSG_GUIDED_MANUAL_SCORER_CONFIRM_GENERATE_REPORT
            = "OK to generate Ki67 score report?\n\n"
            + "After the Ki67 score report has been generated,\n"
            + "you will no longer be able to modify the scores.\n\n"
            + "OK to continue?";
    public static final String MSG_GUIDED_MANUAL_SCORER_EXTERNAL_RESCORE_RECOMMANDED
            = "The estimated 95% confidence interval of the\nfinal Ki67 score crosses the cut point of "
            //"The observed final Ki67 score is within 2% of "
            + GuidedManualScorer.KI67_CUTPOINT_IN_PERCENT
            //+"%\nOR\n"
            //+"95% confidence interval of the final Ki67 score crosses "
            //+ GuidedManualScorer.KI67_CUTPOINT_IN_PERCENT
            //+ "%.\nExternal rescore is strongly recommended."; // this message is used in report.  Be very careful ... may screw up the format of the report!!!
            + "%.\nExternal rescore is required.";
    public static final String MSG_GUIDED_MANUAL_SCORER_EXTERNAL_RESCORE_MAY_BE_GOOD_IDEA
            = "even though the estimated 95% confidence interval of the final Ki67 score does not cross the cut point\n"
            + "of "
            + GuidedManualScorer.KI67_CUTPOINT_IN_PERCENT
            + "%, the observed final Ki67 score is between "
            + GuidedManualScorer.KI67_EXTERNAL_RESCORE_THRESHOLD_LOW_IN_PERCENT
            + " and "
            + GuidedManualScorer.KI67_EXTERNAL_RESCORE_THRESHOLD_HIGH_IN_PERCENT
            + "%.  Please consider external rescore.";
    public static final String MSG_GUIDED_MANUAL_SCORER_READY_TO_GENERATE_REPORT
            = "Based on the current input scores,\n"
            + "the system is able to generate a sufficiently\n"
            + "tight confidence interval of Ki67 score.\n\n"
            + "The system is now ready to generate the Ki67 score report.";
    public static final String MSG_GUIDED_MANUAL_SCORER_MAX_RANDOM_VIRTUAL_TMA_CORES_REACHED
            = "Maximum number ("
            + (GuidedManualScorer.GUIDED_MANUAL_SCORER_MAXIMUM_ADDITIONAL_RANDOM_TMA_CORES_TO_GENERATE + GuidedManualScorer.GUIDED_MANUAL_SCORER_NUMBER_OF_RANDOM_TMA_CORES_TO_GENERATE)
            + ") of " + RANDOM_VIRTUAL_TMA_CORE + "s has been reached.\n"
            + "The system still fails to generate a sufficiently\n"
            + "tight confidence interval of Ki67 score.\n\n"
            + "External rescore is strongly recommended.";
    public static final String MSG_GUIDED_MANUAL_SCORER_AT_LEAST_ONE_VIRTUAL_TMA_CORE_WITH_LESS_THAN_MIN_COUNT
            = "At least one " + RANDOM_VIRTUAL_TMA_CORE + " has less than " + GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT + " tumor nuclei counted.\n"
            + "Please consider double checking your nuclei selection before proceeding to generate report.";
    public static final String MSG_GUIDED_MANUAL_SCORER_HIGH_KI67_HETEROGENEITY_DETECTED
            = "Sufficiently high heterogeneity of Ki67 expression detected.\n"
            + "Additional " + RANDOM_VIRTUAL_TMA_CORE + " will now be generated.";
    public static final String MSG_GUIDED_MANUAL_SCORER_NOT_ENOUGH_TUMOR_CELLS_COUNTED
            = "Insufficient tumor cells counted ... " + GuidedManualScorer.GUIDED_MANUAL_SCORER_MINIMUM_NUMBER_OF_TUMOR_NUCLEI_TO_SCORE + " needed\n"
            + "Additional " + RANDOM_VIRTUAL_TMA_CORE + " will now be generated.";
    public static final String GUIDED_MANUAL_SCORER_COMMENT_INPUT_PROMPT_TEXT = "(Optional) Please enter any comments on scoring this " + RANDOM_VIRTUAL_TMA_CORE + " here.";
    public static final String GUIDED_MANUAL_SCORER_RANDOM_SEED_HINT
            = "Default random seed (integer only) is the x-axis of the 1st vertex in the annotation file.  "
            + "Change the random seed to generate a different set of " + RANDOM_VIRTUAL_TMA_CORE + "s.  "
            + "Please note: the same annotation file with the same random seed will result  "
            + "in the SAME sequence of " + RANDOM_VIRTUAL_TMA_CORE + "s being generated.";
    public static final String REPORT_CONSOLIDATOR_ASK_FOR_FILE_TITLE = "Please select the score result annotation files.";

    // specific to report 
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // TRIAL CENTRE HARDCODE FOR DIFFERNT CENTRE!!! BE SURE TO CHANGE ACCORDINGLY BEFORE COMPILE!!!  //
    public static final TrialCentre TRIAL_CENTRE = TrialCentre.BCCA;  ////////////////////////////////
    //public static final TrialCentre TRIAL_CENTRE = TrialCentre.OCOG;  ///////////////////////////////
    //public static final TrialCentre TRIAL_CENTRE = TrialCentre.PMCC;  ///////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String BCCA_LOGO_IMAGE_FILENAME = "bcca_logo.png";
    public static final String OCOG_LOGO_IMAGE_FILENAME = "ocog_logo.png";
    public static final String PMCC_LOGO_IMAGE_FILENAME = "pmcc_logo.png";
    public static final String BCCA_USE_ONLY_FILENAME = "bcca_use_only.png";
    public static final String OCOG_USE_ONLY_FILENAME = "ocog_use_only.png";
    public static final String PMCC_USE_ONLY_FILENAME = "pmcc_use_only.png";
    public static final String RESCORE_BOX_IMAGE_FILENAME = "rescore_box.png";
    public static final String DEFAULT_FONT_NAME = FontFactory.TIMES_ROMAN;
    public static final int DEFAULT_FONT_SIZE = 12; // for report
    public static final int DEFAULT_APP_FONT_SIZE = 16; // for app
    public static final String DEFAULT_APP_TEXT_STYLE = "-fx-font-size:" + DEFAULT_APP_FONT_SIZE + ";"; // text style for app
    public static final int SMALL_FONT_SIZE = 8;
    public static final float DEFAULT_PARAGRAPH_SPACING = DEFAULT_FONT_SIZE;
    public static final float DEFAULT_INDENT = 4 * DEFAULT_FONT_SIZE;
    public static final float SMALL_INDENT = 2 * DEFAULT_FONT_SIZE;
    public static final int REPORT_PAGE_MARGIN_LEFT_RIGHT = 50;
    public static final int REPORT_HEADER_HEIGHT = 200;
    public static final int REPORT_FOOTER_HEIGHT = 100;
    public static final int REPORT_COMPRESSION_LEVEL = 9; // a value between 0 (best speed) and 9 (best compression)
    public static final float REPORT_IMAGE_QUALITY = 0.8f; // 0 - 1; 1=best quality
    public static final String TAB = "\t"; // tab character for data output
    // other constants ...
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");
    public static final SimpleDateFormat SIMPLE_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    public static final String DEFAULT_PERCENT_FORMAT = "###.##%";
    ///////////////////////////////
    // some useful? functions
    ///////////////////////////////

    /**
     * get default Insets
     *
     * @return
     */
    public static Insets getDefaultInsets() {
        return new Insets(ViewConstants.INSETS_WIDTH);
    }

    /**
     * get file path from under folder name by the class in "grails-like"
     * fashion e.g. "abcController", look under folder named "abc"
     *
     * @param o
     * @param filename
     * @return
     */
    public static String getViewFilePath(Object o, String filename) {
        String simpleClassName = o == null ? "Controller" : o.getClass().getSimpleName();
        simpleClassName = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
        String resourcePath = ViewConstants.RESOURCE_SEPARATOR +
                simpleClassName.substring(0, simpleClassName.length() - "Controller".length()) +
                (o == null ? "" : RESOURCE_SEPARATOR) +
                filename;
        return resourcePath;
    }

    /**
     * add VIEWS_ROOT to file name
     *
     * @param filename
     * @return
     */
    public static String getViewFilePath(String filename) {
        return getViewFilePath(null, filename);
    }

    /**
     * format date, returns null if inputDate is null
     *
     * @param inputDate
     * @return
     */
    public static String formatDate(Date inputDate) {
        if (inputDate == null) {
            return null;
        }
        return SIMPLE_DATE_FORMAT.format(inputDate);
    }

    /**
     * format date, returns null if inputDate is null
     *
     * @param inputDate
     * @return
     */
    public static String formatDatetime(Date inputDate) {
        if (inputDate == null) {
            return null;
        }
        return SIMPLE_DATETIME_FORMAT.format(inputDate);
    }

    /**
     * get trial centre logo
     *
     * @return
     */
    public static Image getTrialCentreLogo() {
        Image logo = null;
        if (TRIAL_CENTRE == TrialCentre.BCCA) {
            logo = Toolkit.getDefaultToolkit().getImage(
                    ViewConstants.class.getResource(ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + ViewConstants.BCCA_LOGO_IMAGE_FILENAME).getPath());
        } else if (TRIAL_CENTRE == TrialCentre.OCOG) {
            logo = Toolkit.getDefaultToolkit().getImage(
                    ViewConstants.class.getResource(ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + ViewConstants.OCOG_LOGO_IMAGE_FILENAME).getPath());
        } else if (TRIAL_CENTRE == TrialCentre.PMCC) {
            logo = Toolkit.getDefaultToolkit().getImage(
                    ViewConstants.class.getResource(ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + ViewConstants.PMCC_LOGO_IMAGE_FILENAME).getPath());
        }
        return logo;
    }

    public static String getTrialCentreLogPath() {
        String logoUrl = null;
        if (TRIAL_CENTRE == TrialCentre.BCCA) {
            logoUrl = ViewConstants.class.getResource(ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + ViewConstants.BCCA_LOGO_IMAGE_FILENAME).toExternalForm();
        } else if (TRIAL_CENTRE == TrialCentre.OCOG) {
            logoUrl = ViewConstants.class.getResource(ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + ViewConstants.OCOG_LOGO_IMAGE_FILENAME).toExternalForm();
        } else if (TRIAL_CENTRE == TrialCentre.PMCC) {
            logoUrl = ViewConstants.class.getResource(ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + ViewConstants.PMCC_LOGO_IMAGE_FILENAME).toExternalForm();
        }
        return logoUrl;
    }

    /**
     * message to confirm that the user is from the "correct" trial centre
     *
     * @return
     */
    public static String getTrialCentreConfirmMessage() {
        String message = "Please confirm: ";
        if (TRIAL_CENTRE == TrialCentre.BCCA) {
            message = message + "are you from BC Cancer Agency (BCCA)?";
        } else if (TRIAL_CENTRE == TrialCentre.OCOG) {
            message = message + "are you from Ontario Clinical Oncology Group (OCOG)?";
        } else if (TRIAL_CENTRE == TrialCentre.PMCC) {
            message = message + "are you from Princess Margaret Cancer Centre (UHN/PMCC)?";
        }
        return message;
    }

    /**
     * message to show if failed to confirm trial centre
     *
     * @return
     */
    public static String getTrialCentreFailedConfirmMessage() {
        String message = "Please note: this application is for ";
        if (TRIAL_CENTRE == TrialCentre.BCCA) {
            message = message + "BC Cancer Agency (BCCA) ";
        } else if (TRIAL_CENTRE == TrialCentre.OCOG) {
            message = message + "Ontario Clinical Oncology Group (OCOG) ";
        } else if (TRIAL_CENTRE == TrialCentre.PMCC) {
            message = message + "Princess Margaret Cancer Centre (UHN/PMCC) ";
        }
        return message + "use only.\nPlease install the appropriate trial centre version.\nThis application will now exit.  Bye.";
    }
}
