/*
 * Guided manual scorer data object
 */
package ca.ubc.gpec.ia.analyzer.model.guidedManualScorer;

import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReader;
import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Annotations;
import ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport.RescoreStatus;
import ca.ubc.gpec.ia.analyzer.stats.BootstrapCI;
import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import java.io.File;
import java.util.Calendar;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

/**
 *
 * @author samuelc
 */
public class GuidedManualScorer {

    // some constants 
    public static final int GUIDED_MANUAL_SCORER_NUMBER_OF_RANDOM_TMA_CORES_TO_GENERATE = 5;
    public static final int GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_DIAMETER_IN_MICROMETER = 600;
    public static final int GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT = 100;
    public static final int GUIDED_MANUAL_SCORER_MAXIMUM_ADDITIONAL_RANDOM_TMA_CORES_TO_GENERATE = 20;
    public static final int GUIDED_MANUAL_SCORER_MINIMUM_NUMBER_OF_TUMOR_NUCLEI_TO_SCORE = 500;
    public static final double KI67_CUTPOINT_IN_PERCENT = 13.25;
    public static final int KI67_EXTERNAL_RESCORE_THRESHOLD_LOW_IN_PERCENT = 11; // scores between 11-16%, need external rescore
    public static final int KI67_EXTERNAL_RESCORE_THRESHOLD_HIGH_IN_PERCENT = 16; // scores between 11-16%, need external rescore
    private VirtualSlideReader virtualSlideReader; // virtual Slide reader

    public GuidedManualScorer() {
        virtualSlideReader = null;
    }

    /**
     * clear ALL DATA on this object
     */
    public void clear() {
        virtualSlideReader = null;
    }

    /**
     * set virtual slide reader
     *
     * @param vsr
     */
    public void setVirtualSlideReader(VirtualSlideReader vsr) {
        virtualSlideReader = vsr;
    }

    /**
     * get the virtual slide reader
     *
     * @return
     */
    public VirtualSlideReader getVirtualSlideReader() {
        return virtualSlideReader;
    }
    
    /**
     * default annotation expected from the user i.e. the user needs to use
     * image scope to generate this file.
     *
     * Example: 1145.svs (svs file) -> 1145.xml (annotation file) OR 1145.ndpi
     * (ndpi file) -> 1145.xml (annotation file)
     *
     * @param virtualSlideFile
     * @return
     */
    public static String getDefaultAnnotationFilename(File virtualSlideFile) {
        String annotationFilename = virtualSlideFile.getPath();
        annotationFilename = annotationFilename.substring(0, annotationFilename.length() - (annotationFilename.endsWith(".svs") ? ".svs" : (annotationFilename.endsWith(".ndpi") ? ".ndpi" : ".scn")).length()) + ".xml";
        return annotationFilename;
    }

    /**
     * default annotation expected from the user i.e. the user needs to use
     * image scope to generate this file.
     *
     * Example: 1145.svs (svs file) -> 1145.xml (annotation file)
     *
     * @return
     */
    public String getDefaultAnnotationFilename() {
        return GuidedManualScorer.getDefaultAnnotationFilename(virtualSlideReader.getVirtualSlideFile());
    }

    /**
     * default save annotation output filename
     *
     * @param virtualSlideFile
     * @return
     */
    public static String getDefaultSaveAnnotationsFilename(File virtualSlideFile) {
        return GuidedManualScorer.getDefaultAnnotationFilename(virtualSlideFile) + ViewConstants.GUIDED_MANUAL_SCORER_SAVE_ANNOTATION_FILE_SUFFIX;
    }

    /**
     * default save annotation output file name
     *
     * @return
     */
    public String getDefaultSaveAnnotationsFilename() {
        return GuidedManualScorer.getDefaultSaveAnnotationsFilename(virtualSlideReader.getVirtualSlideFile());
    }

    /**
     * default archive annotation filename
     *
     * i.e. the annotation file containing the score data AFTER the Ki67 score
     * report has been generated.
     *
     * @param virtualSlideFile
     * @return
     */
    public String getDefaultArchiveAnnotationFilename() {
        return getDefaultSaveAnnotationsFilename() + Calendar.getInstance().getTimeInMillis();
    }

    /**
     * default report name
     *
     * @param virtualSlideFile
     * @return
     */
    public String getDefaultReportFilename(File virtualSlideFile) {
        String annotationFilename = virtualSlideFile.getPath();
        annotationFilename = annotationFilename.substring(0, annotationFilename.length() - (annotationFilename.endsWith(".ndpi") ? 5 : 4 /* assume .svs */)) + ViewConstants.GUIDED_MANUAL_SCORER_KI67_SCORE_REPORT_FILE_SUFFIX;
        return annotationFilename;
    }

    /**
     * save annotations i.e. the generate virtual cores, the scores, the input
     * id, the random seed ... etc
     *
     * @return saved file path
     * @throws JAXBException
     */
    public String saveAnnotations() throws JAXBException {
        // get the original annotation
        // export annotation to file
        String outputFilename = getDefaultSaveAnnotationsFilename();
        virtualSlideReader.exportAnnotationsToFile(outputFilename);
        return outputFilename;
    }

    /**
     * return true if rescore status is set
     *
     * @return
     * @throws ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException
     */
    public boolean isRescoreStatusSet() throws VirtualSlideReaderException {
        Annotations a = virtualSlideReader.getAnnotations();
        return a == null ? false : !(a.getRescore() == RescoreStatus.NOT_SET);
    }

    /**
     * return true only if rescore is set to RescoreStatus.RESCORE otherwise,
     * return false (i.e. if rescore not set, return false)
     *
     * @return
     * @throws ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException
     */
    public boolean isRescore() throws VirtualSlideReaderException {
        Annotations a = virtualSlideReader.getAnnotations();
        return a == null ? false : (a.getRescore() == RescoreStatus.RESCORE);
    }

    /**
     * check whether to recommend rescore ... rule:
     *
     * as of 2015-03-19, the "new" rescore rule is approved and is in effect: rescore if 95% CI crosses KI67_CUTPOINT_IN_PERCENT
     *
     * @return
     * @throws VirtualSlideReaderException
     */
    public boolean recommandExternalRescore() throws VirtualSlideReaderException {
        BootstrapCI scoreWithCI = getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getOverallPercentPositiveNucleiSelected();
        //double scoreToCheck = scoreWithCI.getObservedValue() * 100d;
        if (
            (scoreWithCI.getLowerCI() * 100d > KI67_CUTPOINT_IN_PERCENT || scoreWithCI.getUpperCI() * 100d < KI67_CUTPOINT_IN_PERCENT) //&&
            //(scoreToCheck < (KI67_CUTPOINT_IN_PERCENT - 2) || scoreToCheck > (KI67_CUTPOINT_IN_PERCENT + 2))
        ) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * check whether to show a notice to ask the user to consider external
     * rescore ... rule:
     *
     * if observed Ki67 score is between
     * KI67_EXTERNAL_RESCORE_THRESHOLD_LOW_IN_PERCENT and
     * KI67_EXTERNAL_RESCORE_THRESHOLD_HIGH_IN_PERCENT
     *
     * @return
     * @throws VirtualSlideReaderException
     */
    public boolean shouldConsiderExternalRescore() throws VirtualSlideReaderException {
        BootstrapCI scoreWithCI = getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getOverallPercentPositiveNucleiSelected();
        if (scoreWithCI.getObservedValue() * 100d < KI67_EXTERNAL_RESCORE_THRESHOLD_LOW_IN_PERCENT || scoreWithCI.getObservedValue() * 100d > KI67_EXTERNAL_RESCORE_THRESHOLD_HIGH_IN_PERCENT) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * decide if internal rescoring (i.e. additional tma cores) is required
     *
     * @return
     * @throws VirtualSlideReaderException
     */
    public boolean needInternvalRescore() throws VirtualSlideReaderException {
        // get scores
        BootstrapCI scoreWithCI = getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getOverallPercentPositiveNucleiSelected();

        // first check the number of random cores generated ...
        int currNumCores = getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getRegions().getRegions().size();
        if (currNumCores > GUIDED_MANUAL_SCORER_NUMBER_OF_RANDOM_TMA_CORES_TO_GENERATE) {
            // additional random cores must have been generated ...
            // stop rule:
            //   if the upper 95% CI < KI67_EXTERNAL_RESCORE_THRESHOLD_HIGH_IN_PERCENT (16%)
            //   OR
            //   if the lower 95% CI > KI67_EXTERNAL_RESCORE_THRESHOLD_LOW_IN_PERCENT (11%)
            if (scoreWithCI.getUpperCI() * 100d < KI67_EXTERNAL_RESCORE_THRESHOLD_HIGH_IN_PERCENT || scoreWithCI.getLowerCI() * 100d > KI67_EXTERNAL_RESCORE_THRESHOLD_LOW_IN_PERCENT) {
                return false; // no more internal resocre needed
            } else {
                return true; // need to do some more internal rescore
            }
        } else {
            // additional random cores have not been generated ...
            // stop rule:
            //   if the scores of the 5 cores span the Ki67 cutpoint (13.25%)
            double[] originalData = scoreWithCI.getOriginalData();
            Double max = (new Max()).evaluate(originalData) * 100d;
            Double min = (new Min()).evaluate(originalData) * 100d;
            if (min > KI67_CUTPOINT_IN_PERCENT || max < KI67_CUTPOINT_IN_PERCENT) {
                return false; // scores of 5 cores do NOT span cut point ... no internal rescore is required 
            } else {
                return true; // scores of 5 cores SPAN cut point ... internal rescore is required 
            }
        }
    }

    /**
     * check to see if the maximum number of random virtual tma cores has been
     * reached.
     *
     * @return
     * @throws VirtualSlideReaderException
     */
    public boolean maximumNumberOfRandomVirtualTMACoresReached() throws VirtualSlideReaderException {
        if (virtualSlideReader.getAnnotations().getRandomVirtualTmaCoresAnnotation().getRegions().getRegions().size()
                >= (GUIDED_MANUAL_SCORER_NUMBER_OF_RANDOM_TMA_CORES_TO_GENERATE + GUIDED_MANUAL_SCORER_MAXIMUM_ADDITIONAL_RANDOM_TMA_CORES_TO_GENERATE)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * return the total number of nuclei selected
     *
     * @return
     */
    public int getTotalNumNucleiSelected() throws VirtualSlideReaderException {
        return getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getTotalNumNucleiSelected();
    }

    /**
     * check to see if need to count more tumor nuclei because total number of
     * tumor nuclei counted is <
     * GUIDED_MANUAL_SCORER_MINIMUM_NUMBER_OF_TUMOR_NUCLEI_TO_SCORE @return
     */
    public boolean needCountMoreTumorNuclei() throws VirtualSlideReaderException {
        return getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getTotalNumNucleiSelected() < GUIDED_MANUAL_SCORER_MINIMUM_NUMBER_OF_TUMOR_NUCLEI_TO_SCORE;
    }

    /**
     * check to see if at least one core has less than
     * GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT
     *
     * @return
     */
    public boolean atLeastOneCoreWithLessThanMinCount() throws VirtualSlideReaderException {
        return getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().atLeastOneCoreWithLessThanMinCount();
    }

    /**
     * get the number of virtual tma cores generated
     *
     * @return
     * @throws VirtualSlideReaderException
     */
    public int getTotalNumVirtualTmaCores() throws VirtualSlideReaderException {
        return getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getRegions().getRegions().size();
    }
}
