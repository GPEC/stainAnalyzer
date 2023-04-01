/*
 * Aperio annotation XML file ... Regions element
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model;

import ca.ubc.gpec.fieldselector.model.FieldOfView;
import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.ConsolidateRegionRuntimeException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionAttributeHeaderDescriptionNotFoundException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionGeneratorMaxTriesExceeded;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotPreprocessedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotSupportedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.util.RegionConsolidator;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.util.RegionGenerator;
import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.TreeSet;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "Regions")
public class Regions {

    private RegionAttributeHeaders headers;
    private TreeSet<Region> regions;

    /**
     * constructor
     */
    public Regions() {
        regions = new TreeSet<>();
    }

    @XmlElement(name = "RegionAttributeHeaders")
    public void setRegionAttributeHeaders(RegionAttributeHeaders headers) {
        this.headers = headers;
    }

    public RegionAttributeHeaders getRegionAttributeHeaders() {
        return headers;
    }

    @XmlElement(name = "Region")
    public void setRegions(TreeSet<Region> regions) {
        this.regions = regions;
    }

    public TreeSet<Region> getRegions() {
        return regions;
    }

    /**
     * generate a Region id that is unique among the Region in this Regions
     *
     * @return
     */
    public int generateUniqueRegionId() {
        if (regions.isEmpty()) {
            return 1; // another int would be good in this case since there is no region yet
        }
        int id = regions.first().getId() + 1;
        boolean ok = false;
        while (!ok) {
            ok = true;
            for (Region region : regions) {
                if (region.getId() == id) {
                    ok = false;
                    id = region.getId() + 1;
                    break;
                }
            }
        }
        return id;
    }

    /**
     * consolidate all possible regions ... this is needed when a large ROI
     * consists of multiple line segments.
     *
     * @return
     * @throws
     * ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotSupportedException
     */
    public TreeSet<Region> consolidateRegions() throws RegionNotSupportedException {
        TreeSet<Region> consolidatedRegions = new TreeSet<>();
        TreeSet<Region> tempRegions = new TreeSet<>();
        for (Region region : regions) {
            // clone ... so that we won't be messing with the original region
            tempRegions.add(region.clone());
        }
        while (!tempRegions.isEmpty()) {
            RegionConsolidator regionConsolidator = new RegionConsolidator();
            boolean continueToIterate = true; // indicate whether or not to continue looping through the regions to add to consolidated region
            while (continueToIterate) { // i.e. region from tempRegions are being consumed
                TreeSet<Region> toBeRemovedRegions = new TreeSet<>(); // keep track of regions to be removed
                for (Region region : tempRegions) {
                    if (!region.isTypeFreeHand()) {
                        consolidatedRegions.add(region.clone()); // non-free hand regions are consolided already
                        toBeRemovedRegions.add(region);
                    } else {
                        if (regionConsolidator.addRegion(region.clone())) {
                            toBeRemovedRegions.add(region);
                        }
                    }
                }
                if (toBeRemovedRegions.isEmpty()) {
                    continueToIterate = false; // no more region is being added to consolidated region ... no need to iterate again
                } else {
                    for (Region region : toBeRemovedRegions) {
                        if (!tempRegions.remove(region)) {
                            throw new ConsolidateRegionRuntimeException("failed to remove region from tempRegions");
                        }
                    }
                }
            }
            Region consolidatedRegion = regionConsolidator.getConsolidatedRegion();
            // consolidatedRegion can be null in the case where there is NO free hand annotation
            // on the entire annotation file.
            if (consolidatedRegion != null) {
                consolidatedRegions.add(consolidatedRegion);
            }
        }

        // consolidate the Region id's of consolidatedRegions
        // i.e. currently the Region id are like ... 1, 5, 9, ... 
        //      however, want Region id to be consecutive i.e. 1, 2, 3, ...
        int maxId = 0;
        for (Region region : consolidatedRegions) {
            int tempId = region.getId();
            maxId = tempId > maxId ? tempId : maxId;
        }
        maxId++;
        for (Region region : consolidatedRegions) {
            region.setId(maxId); // make sure id is unique
            maxId++;
        }
        maxId = 1;
        for (Region region : consolidatedRegions) {
            region.setId(maxId); // want id to be 1,2,3 ...
            // add text & escription to annotation
            region.setText(RegionConsolidator.ANNOTATION_TEXT_LABEL + " #" + maxId);
            Attribute a = new Attribute();
            try {
                a.setId(headers.getDescriptionId());
                a.setName("1"); // need something (just any name?) here
                a.setValue(RegionConsolidator.ANNOTATION_DESCRIPTION);
                region.getAttributes().getAttributes().add(a);
            } catch (RegionAttributeHeaderDescriptionNotFoundException ex) {
                // just ignore if description not available
            }
            maxId++;
        }

        return consolidatedRegions;
    }

    /**
     * generate random virtual TMA cores
     *
     * NOTE: this must be done on a CONSOLIDATED region!!!!
     *
     * NOTE: support only ONE set of random virtual TMA cores
     *
     * @param diameterInPixel
     * @param overlapCircleOk - if indicate overlap circle is OK
     * @param randomNumberGenerator
     * @param numOfCores
     * @param description
     * @return
     * @throws VirtualSlideReaderException
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    public TreeSet<Region> generateRandomVirtualTmaCores(
            int diameterInPixel,
            boolean overlapCircleOk,
            Random randomNumberGenerator,
            int numOfCores,
            String description) throws VirtualSlideReaderException, RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {

        RegionGenerator rg = new RegionGenerator(this, randomNumberGenerator);
        for (int i = 0; i < numOfCores; i++) {
            rg.generateUniqueRandomCircleRoi(diameterInPixel, overlapCircleOk);
        }

        TreeSet<Region> randomRegions = rg.getGeneratedRois();

        // consolidate the Region id's of randomRegions
        // i.e. currently the Region id are like ... 1, 5, 9, ... 
        //      however, want Region id to be consecutive i.e. 1, 2, 3, ...
        int maxId = 0;
        for (Region region : randomRegions) {
            int tempId = region.getId();
            maxId = tempId > maxId ? tempId : maxId;
        }
        maxId++;
        for (Region region : randomRegions) {
            region.setId(maxId); // make sure id is unique
            maxId++;
        }
        maxId = 1;
        for (Region region : randomRegions) {
            region.setId(maxId); // want id to be 1,2,3 ...
            // add text & escription to annotation
            region.setText(RegionGenerator.ANNOTATION_TEXT_LABEL + " #" + maxId);
            Attribute a = new Attribute();
            try {
                a.setId(headers.getDescriptionId());
                a.setName("1"); // need something (just any name?) here
                a.setValue(
                        description + "; "
                        + "random rotation in degree: " + region.getRandomRotationInDegree() + "; "
                        + "time created: " + ViewConstants.formatDatetime(Calendar.getInstance().getTime()));
                region.setAttributes(new Attributes());
                region.getAttributes().getAttributes().add(a);
            } catch (RegionAttributeHeaderDescriptionNotFoundException ex) {
                // just ignore if description not available
            }
            maxId++;
        }

        return randomRegions;
    }

    /**
     * generate a random virtual tma core and add it to
     * randomVirtualTmaCoresRegions
     *
     * @param diameterInPixel
     * @param overlapCircleOk
     * @param randomNumberGenerator
     * @param description
     * @param randomVirtualTmaCoresRegions
     * @return
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    public Region generateAdditionalRandomVirtualTmaCore(
            int diameterInPixel,
            boolean overlapCircleOk,
            Random randomNumberGenerator,
            String description,
            Regions randomVirtualTmaCoresRegions) throws RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        RegionGenerator rg = new RegionGenerator(this, randomNumberGenerator);
        rg.generateUniqueRandomCircleRoi(diameterInPixel, overlapCircleOk);
        Region resultRegion = rg.getGeneratedRois().first(); // there must be at least/most one region

        int id = randomVirtualTmaCoresRegions.generateUniqueRegionId();

        resultRegion.setId(id);

        // add text & escription to annotation
        resultRegion.setText(RegionGenerator.ANNOTATION_TEXT_LABEL_ADDITIONAL_RANDOM_VIRTUAL_TMA_CORE + " #" + id);
        Attribute a = new Attribute();
        try {
            a.setId(headers.getDescriptionId());
            a.setName("1"); // need something (just any name?) here
            a.setValue(
                    description + "; "
                    + "random rotation in degree: " + resultRegion.getRandomRotationInDegree() + "; "
                    + "time created: " + ViewConstants.formatDatetime(Calendar.getInstance().getTime()));
            resultRegion.setAttributes(new Attributes());
            resultRegion.getAttributes().getAttributes().add(a);
        } catch (RegionAttributeHeaderDescriptionNotFoundException ex) {
            // just ignore if description not available
        }

        // append region to randomVirtualTmaCoresRegions
        randomVirtualTmaCoresRegions.regions.add(resultRegion);

        return resultRegion;
    }

    /**
     * generate ROI's based on the input FieldOfView's
     *
     * @param selectedFields
     * @param micronsPerPixel
     * @param description
     * @return
     * @throws VirtualSlideReaderException
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     */
    public TreeSet<Region> generateManuallySelectedTmaCores(
            ArrayList<FieldOfView> selectedFields,
            float micronsPerPixel,
            String description) throws VirtualSlideReaderException, RegionNotPreprocessedException, RegionNotSupportedException {
        RegionGenerator rg = new RegionGenerator(this, null);
        for (FieldOfView fov : selectedFields) {
            rg.generateCircleRoi(fov.getX(), fov.getY(), fov.getDiamter());
        }
        return rg.getGeneratedRois();
    }
}
