/*
 * Aperio annotation XML file ... Annotations element
 * 
 * this is the TOP LEVEL node of the Aperio annotation XML file!!!
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model;

import ca.ubc.gpec.fieldselector.model.FieldOfView;
import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.AnnotationProcessingRuntimeException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.ConsolidateRegionRuntimeException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RandomVirtualTmaCoresRuntimeException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionGeneratorMaxTriesExceeded;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotPreprocessedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotSupportedException;
import ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport.TrialCentre;
import ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport.RescoreStatus;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.TreeSet;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "Annotations")
public class Annotations {

    public static final int RANDOM_SEED_NOT_SET = -1;
    private float micronsPerPixel;
    private TreeSet<Annotation> annotations;
    private int randomSeed;
    private Random randomNumberGenerator;
    private TrialCentre trialCentre;
    private String slideId;
    private String md5Sum; // md5sum of the virtual slide
    private Date specimenReceivedDate;
    private RescoreStatus rescore; // indicate whether this is a rescore
    private String pathologistName;

    /**
     * constructor
     */
    public Annotations() {
        annotations = new TreeSet<>();
        randomSeed = RANDOM_SEED_NOT_SET;
        randomNumberGenerator = null;
        slideId = null;
        specimenReceivedDate = null;
        rescore = RescoreStatus.NOT_SET;
        pathologistName = null;
    }

    @XmlAttribute(name = "MicronsPerPixel")
    public void setMicronsPerPixel(float micronsPerPixel) {
        this.micronsPerPixel = micronsPerPixel;
    }

    public float getMicronsPerPixel() {
        return micronsPerPixel;
    }

    @XmlElement(name = "Annotation")
    public void setAnnotations(TreeSet<Annotation> annotations) {
        this.annotations = annotations;
    }

    public TreeSet<Annotation> getAnnotations() {
        return annotations;
    }

    /**
     * set random seed NOTE: also RESET randomNumberGenerator!!!
     *
     * @param randomSeed
     */
    @XmlElement(name = "GPEC_randomSeed")
    public void setRandomSeed(int randomSeed) {
        this.randomSeed = randomSeed;
        randomNumberGenerator = new Random(randomSeed);
    }

    /**
     * get random seed
     *
     * if never set before, set to default - see generateDefaultRandomSeed() for
     * definition of default random seed
     *
     * @return
     */
    public int getRandomSeed() {
        if (randomSeed == RANDOM_SEED_NOT_SET) {
            randomSeed = generateDefaultRandomSeed();
            randomNumberGenerator = new Random(randomSeed);
        }
        return randomSeed;
    }

    /**
     * return randomNumberGenerator
     *
     * DO NOT EXPOSE IT TO PUBLIC!!! this is because its very difficult to keep
     * track of when randomNumberGenerator get used e.g. via nextInt() ...
     *
     * @return
     */
    private Random getRandomNumberGenerator() {
        if (randomNumberGenerator == null) {
            this.setRandomSeed(this.getRandomSeed()); // force reset of random seed and hence generate new instanc eof randomNumberGenerator
        }
        return randomNumberGenerator;
    }

    @XmlElement(name = "GPEC_rescore")
    public void setRescore(RescoreStatus rescore) {
        this.rescore = rescore;
    }

    public RescoreStatus getRescore() {
        return rescore;
    }

    @XmlElement(name = "GPEC_pathologistName")
    public void setPathologistName(String pathologistName) {
        this.pathologistName = pathologistName;
    }

    public String getPathologistName() {
        return pathologistName;
    }

    @XmlElement(name = "GPEC_trialCentre")
    public void setTrialCentre(TrialCentre trialCentre) {
        this.trialCentre = trialCentre;
    }

    public TrialCentre getTrialCentre() {
        return trialCentre;
    }

    @XmlElement(name = "GPEC_slideId")
    public void setSlideId(String slideId) {
        this.slideId = slideId;
    }

    public String getSlideId() {
        return slideId;
    }

    @XmlElement(name = "GPEC_md5Sum")
    public void setMd5Sum(String md5Sum) {
        this.md5Sum = md5Sum;
    }

    public String getMd5Sum() {
        return md5Sum;
    }

    @XmlElement(name = "GPEC_specimenReceivedDate")
    public void setSpecimenReceivedDate(Date specimenReceivedDate) {
        this.specimenReceivedDate = specimenReceivedDate;
    }

    public Date getSpecimenReceivedDate() {
        return specimenReceivedDate;
    }

    /**
     * find out the last score date ... returns null if nothing is scored
     *
     * @return
     */
    public Date getMaxScoredDate() {
        TreeSet<Date> lastDates = new TreeSet<Date>();
        for (Annotation a : annotations) {
            if (a.isRandomVirtualTmaCoresdAnnotation()) {
                // only concerned about scored annotations ... i.e. need to ignore original and consolidated annotations
                lastDates.add(a.getLastNucleiSelectionDate());
            }
        }
        return lastDates.last();
    }

    /**
     * generate random seed NOTE: default random seed = x-axis of first vertex
     * of the first region of the first annotation of the ORIGINAL annotation
     *
     * ASSUME original annotation exist
     */
    private int generateDefaultRandomSeed() {
        Annotation a = getOriginalAnnotation();
        if (a == null) {
            throw new AnnotationProcessingRuntimeException("generateDefaultRandomSeed(): original annotation not found.");
        }
        // DO NOT set random seed
        return Math.round(a.getRegions().getRegions().first().getVertices().getVertices().first().getX());
    }

    /**
     * original annotation ALWAYS the first!!!!
     *
     * @return
     */
    public Annotation getOriginalAnnotation() {
        if (annotations.isEmpty()) {
            return null; // no original annotation
        }
        Annotation originalAnnotation = annotations.first();

        if (originalAnnotation.isOriginalAnnotation()) {
            return originalAnnotation;
        } else {
            throw new AnnotationProcessingRuntimeException("original annotation is NOT originalAnnotation !!!! something is wrong");
        }
    }

    /**
     * get the number of original annotation layers
     *
     * @return
     * @throws VirtualSlideReaderException
     */
    public int getNumOriginalAnnotationLayers() {
        // getAnnotations() checks for annotation file existance and virtual slide file type
        int count = 0;
        for (Annotation a : annotations) {
            if (a.isOriginalAnnotation()) {
                count++;
            }
        }
        return count;
    }

    /**
     * check to see if consolidatedAnnotation exists
     *
     * @return
     */
    public boolean consolidatedAnnotationExists() {
        if (annotations.isEmpty()) {
            return false; // no annotation ... consolidatedAnnotation must not exist
        }
        for (Annotation a : annotations) {
            if (a.isConsolidatedAnnotation()) {
                return true;
            }
        }
        return false;
    }

    /**
     * check to see if random virtual tma cores exists
     *
     * @return
     */
    public boolean randomVirtualTmaCoresExist() {
        if (annotations.isEmpty()) {
            return false; // no annotation ... random virtual tma cores must not exist
        }
        for (Annotation a : annotations) {
            if (a.isRandomVirtualTmaCoresdAnnotation()) {
                return true;
            }
        }
        return false;
    }

    /**
     * check to see if manually selected tma core exists
     *
     * @return
     */
    public boolean manuallySelectedVirtualTmaCoresExist() {
        if (annotations.isEmpty()) {
            return false; // no annotation ... random virtual tma cores must not exist
        }
        for (Annotation a : annotations) {
            if (a.isManuallySelectedVirtualTmaCoresAnnotation()) {
                return true;
            }
        }
        return false;
    }

    /**
     * get the consolidated annotation
     *
     * return null if not found
     *
     * @return
     */
    public Annotation getConsolidatedAnnotation() {
        if (annotations.isEmpty()) {
            return null; // consolidated annotation not found
        }
        for (Annotation a : annotations) {
            if (a.isConsolidatedAnnotation()) {
                return a;
            }
        }
        // consolidated annotation not found
        throw new ConsolidateRegionRuntimeException("getConsolidatedAnnotation(): try to get consolidated annotation but it is unavailable");
    }

    /**
     * get the random virtual TMA core annotation
     *
     * return null if not found
     *
     * @return
     */
    public Annotation getRandomVirtualTmaCoresAnnotation() {
        if (annotations.isEmpty()) {
            return null;
        }
        for (Annotation a : annotations) {
            if (a.isRandomVirtualTmaCoresdAnnotation()) {
                return a;
            }
        }
        // consolidated annotation not found
        throw new RandomVirtualTmaCoresRuntimeException("getRandomVirtualTmaCoresAnnotation(): try to get randomVirtualTmaCores annotation but it is unavailable");
    }

    /**
     * get the manually selected TMA core annotation
     *
     * return null if not found
     *
     * @return
     */
    public Annotation getManuallySelectedVirtualTmaCores() {
        if (annotations.isEmpty()) {
            return null;
        }
        for (Annotation a : annotations) {
            if (a.isManuallySelectedVirtualTmaCoresAnnotation()) {
                return a;
            }
        }
        // consolidated annotation not found
        throw new RandomVirtualTmaCoresRuntimeException("getManuallySelectedVirtualTmaCores(): try to get manuallySelectedVirtualTmaCores annotation but it is unavailable");
    }

    /**
     * add annotation
     *
     * @param annotation
     */
    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }

    /**
     * generate an id that is guaranteed to be unique among Annotation objects
     * in this Annotations object
     *
     * @return
     */
    public int generateUniqueAnnotationId() {
        if (annotations.isEmpty()) {
            return 1;
        }
        int id = annotations.first().getId() + 1;
        boolean ok = false;
        while (!ok) {
            ok = true;
            for (Annotation annotation : annotations) {
                if (annotation.getId() == id) {
                    ok = false;
                    id = annotation.getId() + 1;
                    break;
                }
            }
        }
        return id;
    }

    /**
     * consolidate regions of the original annotation (i.e. the first layer of
     * the Aperio annotation) - add the consolidated region to the second layer
     * - if consolidated annotation exist already, DO NOT do consolidation again
     */
    public void consolidateRegions() throws RegionNotSupportedException {
        if (consolidatedAnnotationExists()) {
            return; // consolidated annotation exists already
        }
        Annotation consolidatedAnnotation = new Annotation();
        consolidatedAnnotation.setId(generateUniqueAnnotationId());
        consolidatedAnnotation.setType(Annotation.ANNOTATION_TYPE_CONSOLIDATED_SELECTION);
        consolidatedAnnotation.setLineColor(Annotation.LINE_COLOR_CODE_YELLOW);
        consolidatedAnnotation.setUtcTimeInMillisCreated(Calendar.getInstance().getTimeInMillis());

        Regions consolidatedRegions = new Regions();
        consolidatedRegions.setRegions(getOriginalAnnotation().getRegions().consolidateRegions()); // consolidate region
        consolidatedRegions.setRegionAttributeHeaders(getOriginalAnnotation().getRegions().getRegionAttributeHeaders().clone()); // clone attribute header

        consolidatedAnnotation.setRegions(consolidatedRegions);
        annotations.add(consolidatedAnnotation);
    }

    /**
     * generate random virtual TMA cores
     *
     * NOTE: NON-overlapping virtual TMA cores
     *
     * NOTE: support only ONE set of random virtual TMA cores
     *
     * @param diameterInMicrometer
     * @param randomNumberGenerator
     * @param numOfCores
     * @throws VirtualSlideReaderException
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    public void generateRandomVirtualTmaCores(
            int diameterInMicrometer,
            Random randomNumberGenerator,
            int numOfCores) throws VirtualSlideReaderException, RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        if (randomVirtualTmaCoresExist()) {
            // trying to generate random virtual tma core annotation when it exists already!!! 
            // this should not happen!!!
            throw new RandomVirtualTmaCoresRuntimeException("trying to generate random virtual tma core annotation when it exists already!!!");
        }

        Annotation randomVirtualTmaCoreAnnotation = new Annotation();
        randomVirtualTmaCoreAnnotation.setId(generateUniqueAnnotationId());
        randomVirtualTmaCoreAnnotation.setType(Annotation.ANNOTATION_TYPE_GENERATED_VIRTUAL_TMA_CORES);
        randomVirtualTmaCoreAnnotation.setLineColor(Annotation.LINE_COLOR_CODE_BLUE);

        Regions randomVirtualTmaCoreRegions = new Regions();
        int diameterInPixel = Math.round(diameterInMicrometer / getMicronsPerPixel());

        randomVirtualTmaCoreRegions.setRegions(
                getConsolidatedAnnotation().getRegions().generateRandomVirtualTmaCores(
                        diameterInPixel,
                        false, // overlapCircleOk set to false to ensure non-overlapping virtual TMA cores
                        randomNumberGenerator,
                        numOfCores,
                        "diameter in micrometer: " + diameterInMicrometer));
        randomVirtualTmaCoreRegions.setRegionAttributeHeaders(getConsolidatedAnnotation().getRegions().getRegionAttributeHeaders().clone()); // clone attribute header

        randomVirtualTmaCoreAnnotation.setRegions(randomVirtualTmaCoreRegions);
        randomVirtualTmaCoreAnnotation.setUtcTimeInMillisCreated(Calendar.getInstance().getTimeInMillis()); // record time created
        annotations.add(randomVirtualTmaCoreAnnotation);
    }

    /**
     * generate random virtual TMA cores
     *
     * NOTE: NON-overlapping virtual TMA cores
     *
     * NOTE: support only ONE set of random virtual TMA cores
     *
     * NOTE: using default random number generator
     *
     * @param diameterInMicrometer
     * @param numOfCores
     * @throws VirtualSlideReaderException
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    public void generateRandomVirtualTmaCores(
            int diameterInMicrometer,
            int numOfCores) throws VirtualSlideReaderException, RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        generateRandomVirtualTmaCores(
                diameterInMicrometer,
                getRandomNumberGenerator(),
                numOfCores);
    }

    /**
     * add additional random virtual TMA cores
     *
     * @param diameterInMicrometer
     * @param randomSeed
     * @param overlapCircleOk
     */
    public void generateAdditionalRandomVirtualTmaCore(
            int diameterInMicrometer,
            Random randomNumberGenerator,
            boolean overlapCircleOk) throws RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        if (!randomVirtualTmaCoresExist()) {
            // trying to generate additional random virtual tma core annotation when initial virtual tma cores does not exist!!! 
            // this should not happen!!!
            throw new RandomVirtualTmaCoresRuntimeException("trying to generate ADDITIONAL random virtual tma core annotation when inital virtual tma cores does NOT exist!!!");
        }

        int diameterInPixel = Math.round(diameterInMicrometer / getMicronsPerPixel());

        getConsolidatedAnnotation().getRegions().generateAdditionalRandomVirtualTmaCore(
                diameterInPixel,
                overlapCircleOk,
                randomNumberGenerator, // random seed set to UTC time
                "diameter in micrometer: " + diameterInMicrometer,
                getRandomVirtualTmaCoresAnnotation().getRegions());
    }

    /**
     * add additional random virtual TMA cores
     *
     * use default random number generator
     *
     * @param diameterInMicrometer
     * @param overlapCircleOk
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    public void generateAdditionalRandomVirtualTmaCore(
            int diameterInMicrometer,
            boolean overlapCircleOk) throws RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        generateAdditionalRandomVirtualTmaCore(
                diameterInMicrometer,
                getRandomNumberGenerator(),
                overlapCircleOk);
    }

    /**
     * generate ROI's based on the input FieldOfView's
     *
     * @param selectedFields
     * @throws VirtualSlideReaderException
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     */
    public void generateManuallySelectedVirtualTmaCores(
            ArrayList<FieldOfView> selectedFields) throws VirtualSlideReaderException, RegionNotPreprocessedException, RegionNotSupportedException {

        Annotation manuallySelectedVirtualTmaCoreAnnotation = new Annotation();
        manuallySelectedVirtualTmaCoreAnnotation.setId(generateUniqueAnnotationId());
        manuallySelectedVirtualTmaCoreAnnotation.setType(Annotation.ANNOTATION_TYPE_MANUAL_SELECTED_VIRTUAL_TMA_CORES);
        manuallySelectedVirtualTmaCoreAnnotation.setLineColor(Annotation.LINE_COLOR_CODE_BLUE);

        Regions manuallySelectedVirtualTmaCoreRegions = new Regions();

        manuallySelectedVirtualTmaCoreRegions.setRegions(getConsolidatedAnnotation().getRegions().generateManuallySelectedTmaCores(selectedFields, micronsPerPixel, ""));
        manuallySelectedVirtualTmaCoreRegions.setRegionAttributeHeaders(getConsolidatedAnnotation().getRegions().getRegionAttributeHeaders().clone()); // clone attribute header

        manuallySelectedVirtualTmaCoreAnnotation.setRegions(manuallySelectedVirtualTmaCoreRegions);
        manuallySelectedVirtualTmaCoreAnnotation.setUtcTimeInMillisCreated(Calendar.getInstance().getTimeInMillis()); // record time created
        annotations.add(manuallySelectedVirtualTmaCoreAnnotation);
    }

    /**
     * export to file
     *
     * @param filename
     * @throws JAXBException
     */
    public void exportToFile(String filename) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Annotations.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.marshal(this, new File(filename));
    }
}
