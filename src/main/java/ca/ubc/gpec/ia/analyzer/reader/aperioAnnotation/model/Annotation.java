/*
 * Aperio annotation XML file ... Annotation element
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model;

import ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.GuidedManualScorer;
import ca.ubc.gpec.ia.analyzer.stats.BootstrapCI;
import java.util.Date;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.apache.commons.math3.stat.descriptive.moment.Mean;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "Annotation")
public class Annotation implements Comparable {

    public static final String ANNOTATION_TYPE_CONSOLIDATED_SELECTION = "consolidated selection";
    public static final String ANNOTATION_TYPE_GENERATED_VIRTUAL_TMA_CORES = "generated virtual TMA cores";
    public static final String ANNOTATION_TYPE_MANUAL_SELECTED_VIRTUAL_TMA_CORES = "manually selected virtual TMA cores";
    public static final int NA_ID = Integer.MIN_VALUE;
    public static final int LINE_COLOR_CODE_GREEN = 65280; // refernce: http://www.endprod.com/colors/
    public static final int LINE_COLOR_CODE_YELLOW = 65535;
    public static final int LINE_COLOR_CODE_RED = 255;
    public static final int LINE_COLOR_CODE_BLUE = 16711680;
    private int id;
    private int lineColor; // color of annotation line
    Regions regions;
    // below are all NON-APERIO fields!!!
    private String type;
    private long utcTimeInMillisCreated; // time created as UTC milliseconds from the epoch.

    // end of fields declaration ////////////////
    /**
     * generate a new Annotation object
     */
    public Annotation() {
        id = NA_ID;
        regions = null;
        type = null;
    }

    @XmlAttribute(name = "Id")
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * check to see if id is set
     *
     * @return
     */
    public boolean idExists() {
        return id != NA_ID;
    }

    @XmlAttribute(name = "LineColor")
    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public int getLineColor() {
        return lineColor;
    }

    @XmlElement(name = "Regions")
    public void setRegions(Regions regions) {
        this.regions = regions;
    }

    public Regions getRegions() {
        return regions;
    }

    /**
     * for Comparable interface WARNING: assume id is unique!!!
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(Object other) {
        Annotation otherAnnotation = (Annotation) other;
        return id - otherAnnotation.id;
    }

    @XmlAttribute(name = "GPEC_AnnotationType")
    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @XmlElement(name = "GPEC_utcTimeInMillisCreated")
    public void setUtcTimeInMillisCreated(long utcTimeInMillisCreated) {
        this.utcTimeInMillisCreated = utcTimeInMillisCreated;
    }

    public long getUtcTimeInMillisCreated() {
        return utcTimeInMillisCreated;
    }

    /**
     * check to see if this annotation is the original Aperio annotation
     *
     * @return
     */
    public boolean isOriginalAnnotation() {
        return type == null;
    }

    /**
     * check to see if this annotation represents consolidated selection
     *
     * @return
     */
    public boolean isConsolidatedAnnotation() {
        if (type == null) {
            return false;
        }
        return type.equals(Annotation.ANNOTATION_TYPE_CONSOLIDATED_SELECTION);
    }

    /**
     * check to see if this annotation represents random virtual TMA cores
     *
     * @return
     */
    public boolean isRandomVirtualTmaCoresdAnnotation() {
        if (type == null) {
            return false;
        }
        return type.equals(Annotation.ANNOTATION_TYPE_GENERATED_VIRTUAL_TMA_CORES);
    }

    public boolean isManuallySelectedVirtualTmaCoresAnnotation() {
        if (type == null) {
            return false;
        }
        return type.equals(Annotation.ANNOTATION_TYPE_MANUAL_SELECTED_VIRTUAL_TMA_CORES);
    }
    
    /**
     * return the date of the last nuclei selection; 
     * return null if no nuclei selection available for whatever reason
     *
     * @return
     */
    public Date getLastNucleiSelectionDate() {
        if (isRandomVirtualTmaCoresdAnnotation()) {
            Date maxDate = null;
            for (Region r : regions.getRegions()) {
                Date d = new Date(r.getNucleiSelections().getLastModified());
                if (maxDate == null) {
                    maxDate = d;
                } else {
                    maxDate = maxDate.before(d) ? d : maxDate;
                }
            }
            return maxDate;
        } else {
            return null; // no nuclei selection for original/consolidated annotation
        }
    }

    /**
     * return the overall nuclei selected (and bootstrap 95% CI). i.e.
     * sum(positive nuclei counted on ALL regions) / sum(total nuclei counted on
     * ALL regions)
     *
     * WARNING!!! assume SAME number of nuclei counted for each core - the
     * GuidedManualScorer only guarantee that at least 100 nuclei were counted
     * for each core but it does NOT prevent user from counting more.
     *
     * return null if no nuclei selection available
     *
     * @return
     */
    public BootstrapCI getOverallPercentPositiveNucleiSelected() {
        if (isRandomVirtualTmaCoresdAnnotation()) {

            // we need to skip the cores with ZERO nuclei selected !!!
            int numCoreWithNonZeroNucleiSelected = 0;
            for (Region r : regions.getRegions()) {
                if (r.getNucleiSelections().getNumTotal() > 0) {
                    numCoreWithNonZeroNucleiSelected++;
                }
            }

            double[] percentPositivePerCore = new double[numCoreWithNonZeroNucleiSelected];
            double[] totalNucleiCountPerCore = new double[numCoreWithNonZeroNucleiSelected];

            int coreCount = 0;
            for (Region r : regions.getRegions()) {
                double numNucleiCount = (double) r.getNucleiSelections().getNumTotal();
                if (numNucleiCount > 0) {
                    totalNucleiCountPerCore[coreCount] = numNucleiCount;
                    percentPositivePerCore[coreCount] = ((double) r.getNucleiSelections().getNumPositive()) / numNucleiCount;
                    coreCount++;
                }
            }
            return new BootstrapCI(
                    new Mean(),
                    percentPositivePerCore,
                    totalNucleiCountPerCore, // this is the weights
                    BootstrapCI.DEFAULT_NUMBER_OF_BOOTSTRAP_ITERATIONS,
                    BootstrapCI.DEFAULT_LOWER_CI_LEVEL,
                    BootstrapCI.DEFAULT_UPPER_CI_LEVEL,
                    12345 // fix random seed so that the bootstrap CI will be reproducible
            );
        } else {
            return null;
        }
    }

    /**
     * get total number of nuclei selected
     *
     * @return
     */
    public int getTotalNumNucleiSelected() {
        int numNucleiSelected = 0;
        for (Region r : regions.getRegions()) {
            numNucleiSelected = numNucleiSelected + r.getNucleiSelections().getNumTotal();
        }
        return numNucleiSelected;
    }

    /**
     * check to see if at least one core has less than
     * GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT
     *
     * @return
     */
    public boolean atLeastOneCoreWithLessThanMinCount() {
        for (Region r : regions.getRegions()) {
            if (r.getNucleiSelections().getNumTotal() < GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_NUMBER_OF_NUCLEI_TO_COUNT) {
                return true; // at least one core with < min required tumor nuclei count
            }
        }
        return false;
    }
}
