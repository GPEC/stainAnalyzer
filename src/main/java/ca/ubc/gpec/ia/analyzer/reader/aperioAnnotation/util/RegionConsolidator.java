/*
 * consolidate free-hand Aperio annotation region
 * NOTE: support only ONE consolidated region ... i.e. the regions 
 * added to this consolidator will produce a maximum of ONE consolidated
 * region.  Regions that are not adjacent to the FIRST region added to
 * this consolidator will be IGNORED.
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.util;

import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Region;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotSupportedException;

/**
 * IMPORTANT NOTE!!! assume regions are from the SAME annotation file (this is
 * most likely the case) ... this is needed because each region has id attribute
 * which is guaranteed to be unique ONLY within the context of an annotation
 * file
 *
 * @author samuelc
 */
public class RegionConsolidator {

    // the maximum distance between two vertex (in pixals) to be consolidated
    public static final int CONSOLIDATE_THRESHOLD = 200;
    public static final int NOT_ADJACENT = -1;
    public static final int ADJACENT_FIRST_FIRST = 0;
    public static final int ADJACENT_FIRST_LAST = 1;
    public static final int ADJACENT_LAST_FIRST = 2;
    public static final int ADJACENT_LAST_LAST = 3;
    public static final String ANNOTATION_TEXT_LABEL = "GPEC: consolidated selection";
    public static final String ANNOTATION_DESCRIPTION = "GPEC: consolidated tumor area selection";
    private Region consolidatedRegion;

    /**
     * constructor
     */
    public RegionConsolidator() {
        consolidatedRegion = null;
    }

    /**
     * check to see if r1 is adjacent to r2
     *
     * @param r1
     * @param r2
     * @return NOT_ADJACENT, ADJACENT_FIRST_FIRST, ADJACENT_FIRST_LAST,
     * ADJACENT_LAST_FIRST, or ADJACENT_LAST_LAST
     */
    private int showAdjacent(Region r1, Region r2) {
        if (VertexArithmetic.euclideanDistance(r1.getFirstVertex(), r2.getFirstVertex()) < CONSOLIDATE_THRESHOLD) {
            return ADJACENT_FIRST_FIRST;
        } else if (VertexArithmetic.euclideanDistance(r1.getFirstVertex(), r2.getLastVertex()) < CONSOLIDATE_THRESHOLD) {
            return ADJACENT_FIRST_LAST;
        } else if (VertexArithmetic.euclideanDistance(r1.getLastVertex(), r2.getFirstVertex()) < CONSOLIDATE_THRESHOLD) {
            return ADJACENT_LAST_FIRST;
        } else if (VertexArithmetic.euclideanDistance(r1.getLastVertex(), r2.getLastVertex()) < CONSOLIDATE_THRESHOLD) {
            return ADJACENT_LAST_LAST;
        } else {
            return NOT_ADJACENT;
        }
    }

    /**
     * add (adjacent) region to be consolidated
     *
     * if regions is empty, add the region
     *
     * if regions is not empty, will add region only if the newly added region
     * is adjacent to any of the region in regions
     *
     * @param inputRegion
     * @return true if region added successfully, false otherwise
     * @throws RegionNotSupportedException
     */
    public boolean addRegion(Region inputRegion) throws RegionNotSupportedException {
        if (!inputRegion.isTypeFreeHand()) {
            throw new RegionNotSupportedException("cannot consolidated non-free-hand region");
        }
        if (consolidatedRegion == null) {
            consolidatedRegion = inputRegion.clone();
            return true;
        } else {
            // check to see if region is adjacent to any of the region in regions
            switch (showAdjacent(consolidatedRegion, inputRegion)) {
                // note: because regions is a TreeSet, no duplicate will be added
                // IMPORTANT: duplicate is indicated by 'id' field in Region!!!
                //
                // do consolidation!!!
                case ADJACENT_FIRST_FIRST:
                    consolidatedRegion.getVertices().prependVerticesReverse(inputRegion.getVertices());
                    return true;
                case ADJACENT_FIRST_LAST:
                    consolidatedRegion.getVertices().prependVertices(inputRegion.getVertices());
                    return true;
                case ADJACENT_LAST_FIRST:
                    consolidatedRegion.getVertices().appendVertices(inputRegion.getVertices());
                    return true;
                case ADJACENT_LAST_LAST:
                    consolidatedRegion.getVertices().appendVerticesReverse(inputRegion.getVertices());
                    return true;
                default:
                    return false; // failed to add region
            }
        }
    }

    /**
     * get the consolidated region (single)
     *
     * return null if consolidatedRegion is null
     *
     * return unmodified consolidatedRegion if the region is not a free hand
     * region.
     *
     * @return
     */
    public Region getConsolidatedRegion() {
        if (consolidatedRegion != null) {
            if (consolidatedRegion.isTypeFreeHand()) {
                // close the freehand ROI by adding (repeating) the first vertex to the end
                consolidatedRegion.getVertices().addVertex(consolidatedRegion.getFirstVertex().clone());
            }
        }
        return consolidatedRegion;
    }
}
