/*
 * responsible for QC the annotations set
 */
package ca.ubc.gpec.ia.analyzer.controller.helper.guidedManualScorer;

import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Annotations;
import ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport;
import ca.ubc.gpec.ia.analyzer.views.MyDialogClosedByUserException;
import ca.ubc.gpec.ia.analyzer.views.guidedManualScorer.GuidedManualScorerStage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author samuelc
 */
public class QcAnnotationsSetForReportConsolidationService extends Service<String> {

    // error messages
    public static final String ERR_MSG_SLIDE_IDS_NOT_SIMILAR = "ERROR: the slide ID's entered by the pathologists not similar.";
    public static final String ERR_MSG_EARLIEST_SCORE_NOT_INITIAL = "ERROR: the first (earliest date) score result annotation file is not initial score. Please double check to make sure selected score result annotation files are correct.";
    public static final String ERR_MSG_MORE_THAN_ONE_INITIAL = "ERROR: all score result annotation file (except the first one) must be rescore.";
    public static final String ERR_MSG_MD5_NOT_MATCH = "ERROR: virtual slides checkdum (MD5) recorded on the annotation files are NOT CONSISTENT.  They may be referring to DIFFERENT virtual slide files.  Please check the annotation files carefully.";
    public static final String WARNING_MSG_SLIDE_IDS_NOT_SIMILAR = "WARNING: the slide ID's entered by the pathologists not similar.  Please verify that they all refer to the same slide:";
    
    private final GuidedManualScorerStage guidedManualScorerStage;
    private final TreeSet<File> annotationsFileSet;
    private final HashMap<File, Annotations> annotationsTable;

    /**
     * constructor
     *
     * @param annotationsTable
     */
    public QcAnnotationsSetForReportConsolidationService(GuidedManualScorerStage guidedManualScorerStage, HashMap<File, Annotations> annotationsTable) {
        this.guidedManualScorerStage = guidedManualScorerStage;
        this.annotationsTable = annotationsTable;
        annotationsFileSet = new TreeSet<File>();
        for (File f : annotationsTable.keySet()) {
            annotationsFileSet.add(f);
        }
    }

    /**
     * do the work!!! - the generated random virtual TMA cores regions are
     * stored in virtualSlideReader object
     *
     * @return
     */
    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() {
                String errorMsg;
                if ((errorMsg = checkIfOnlyFirstAnnotationIsInitial()) != null) {
                    return errorMsg;
                }
                if ((errorMsg = checkIfAllSlideIdsAreTheSameCase()) != null) {
                    String returnErrorMsg = ERR_MSG_SLIDE_IDS_NOT_SIMILAR;
                    // ask to very ok
                    if (guidedManualScorerStage != null) {
                        try {
                            if (!guidedManualScorerStage.showWarningConfirmDialog("Please verify slide ID's ...", errorMsg)) {
                                return (returnErrorMsg);
                            }
                        } catch (MyDialogClosedByUserException ex) {
                            return returnErrorMsg;
                        }
                    } else {
                        // guidedManualScorerStage is null ...  must be doing testing
                        System.err.println("... asking user to verify slide ids refer to same slides");
                    }
                }

                // all test cases passed!!!
                return null;
            }
        };
    }

    /**
     * Test case 1: expect the first annotation to be NOT rescore and the result
     * are rescore
     *
     * @return error message. when return null means no error
     */
    public String checkIfOnlyFirstAnnotationIsInitial() {
        // find first annotation
        Annotations firstAnnotations = annotationsTable.get(annotationsFileSet.first());
        for (Annotations a : annotationsTable.values()) {
            if (a.getMaxScoredDate().before(firstAnnotations.getMaxScoredDate())) {
                firstAnnotations = a;
            }
        }

        if (firstAnnotations.getRescore() != GuidedManualScorerReport.RescoreStatus.INITIAL) {
            return ERR_MSG_EARLIEST_SCORE_NOT_INITIAL;
        }

        for (Annotations a : annotationsTable.values()) {
            if (a != firstAnnotations) {
                if (a.getRescore() != GuidedManualScorerReport.RescoreStatus.RESCORE) {
                    return ERR_MSG_MORE_THAN_ONE_INITIAL;
                }
            }
        }
        return null; // test case passed!
    }

    /**
     * helper function for test case 2.
     *
     * make sure the first item in the list is a substring of ALL items on in
     * the list.
     *
     * @param inputList
     * @param items
     * @return
     */
    public boolean allSubstringOfFirst(ArrayList<String> inputList) {
        ArrayList<String> items = new ArrayList<String>();
        // do a deep copy so that the sort function will not sort the input list
        for (String i : inputList) {
            items.add(i.trim());
        }

        Collections.sort(items, new Comparator<String>() {
            public int compare(String a, String b) {
                int compareByLength = a.length() - b.length();
                return compareByLength == 0 ? a.compareTo(b) : compareByLength;
            }
        });

        // the first element must be the smallest.
        String firstItem = items.get(0);
        for (String item : items) {
            if (!item.contains(firstItem)) {
                return false;
            }
        }
        return true; // no possible match found
    }

    /**
     * Test case 2: expect all annotations to be referring to the same case.
     *
     * There's no way to verify based on slideId since this field is entered by
     * the user and there's no way to make sure that the user put the same slide
     * id. So, we can only do a sub-optimal check ... expect the id with the
     * short length be a substring of all the other id
     *
     * @return an error message if the slideIds are not similar
     */
    public String checkIfAllSlideIdsAreTheSameCase() {
        ArrayList<String> slideIds = new ArrayList();
        for (File f : annotationsFileSet) {
            slideIds.add(annotationsTable.get(f).getSlideId());
        }

        // the slide ids entered by initial and subsequent scorers are not similar
        if (!allSubstringOfFirst(slideIds)) {
            String errMsg = WARNING_MSG_SLIDE_IDS_NOT_SIMILAR;
            for (String slideId : slideIds) {
                errMsg = errMsg + " " + slideId + ",";
            }
            errMsg = errMsg.substring(0, errMsg.length() - 1);
            return errMsg;
        }

        return null; // test case passed!
    }

    /**
     * Test case 3: expects all md5sum to be the same
     *
     * @return
     */
    public String checkIfAllMd5SumAreTheSameCase() {
        ArrayList<String> md5Sums = new ArrayList();
        for (File f : annotationsFileSet) {
            md5Sums.add(annotationsTable.get(f).getMd5Sum());
        }

        String firstSum = md5Sums.get(0);

        for (String md5Sum : md5Sums) {
            if (!firstSum.equals(md5Sum)) {
                return ERR_MSG_MD5_NOT_MATCH;
            }
        }

        return null; // if we are here, must be all md5sum are the same
    }
}
