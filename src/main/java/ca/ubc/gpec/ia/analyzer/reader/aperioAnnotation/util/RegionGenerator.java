/*
 * Generate ROI (Region) inside a parent region
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.util;

import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionGeneratorMaxTriesExceeded;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotPreprocessedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotSupportedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Region;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Regions;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Vertex;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Vertices;
import java.awt.geom.Area;
import java.util.Random;
import java.util.TreeSet;

/**
 *
 * @author samuelc
 */
public class RegionGenerator {

    public static final int MAX_TRIES = 500; // number of times to try generating Roi, if no success, give up and throw exception
    public static final String ANNOTATION_TEXT_LABEL = "GPEC: random virtual TMA core";
    public static final String ANNOTATION_DESCRIPTION = "GPEC: random virtual TMA core.";
    public static final String ANNOTATION_TEXT_LABEL_ADDITIONAL_RANDOM_VIRTUAL_TMA_CORE = "GPEC: (additional) random virtual TMA core";
    public static final String ANNOTATION_DESCRIPTION_ADDITIONAL_RANDOM_VIRTUAL_TMA_CORE = "GPEC: (additional) random virtual TMA core.";
    private Regions contextRegions;
    private TreeSet<Region> generatedRois;
    Random randomNumberGenerator;
    private double totalArea; // sum of area of all region in contextRegions
    private double[] regionWeights; // weighting based on area so that regions with larger area get chosen more often

    /**
     * constructor // assume parent is preprocessed!!!
     *
     * NOTE: random number generator seed (for choosing region) = (first
     * region).getEnclosingRectangleX() + 1;
     *
     * @param contextRegions
     * @param randomNumberGenerator
     * @throws RegionNotPreprocessedException
     */
    public RegionGenerator(Regions contextRegions, Random randomNumberGenerator) throws RegionNotPreprocessedException, RegionNotSupportedException {
        this.contextRegions = contextRegions;
        generatedRois = new TreeSet<>();
        this.randomNumberGenerator = randomNumberGenerator;
        // calculate total area of all region in contextRegions
        totalArea = 0d;
        regionWeights = new double[contextRegions.getRegions().size()];
        int i = 0;
        for (Region region : contextRegions.getRegions()) {
            double a = region.getArea();
            regionWeights[i] = a;
            totalArea = totalArea + a;
            i++;
        }
        for (i = 0; i < regionWeights.length; i++) {
            regionWeights[i] = regionWeights[i] / totalArea;
        }
    }

    /**
     * generate a region id that is guaranteed to be unique
     *
     * @return
     */
    private int generateUniqueRegionId() {
        if (generatedRois.isEmpty()) {
            return contextRegions.generateUniqueRegionId();
        }
        int id = generatedRois.first().getId() + 1;
        boolean ok = false;
        while (!ok) {
            ok = true;
            for (Region region : contextRegions.getRegions()) {
                if (region.getId() == id) {
                    ok = false;
                    id = region.getId() + 1;
                    break;
                }
            }
            for (Region region : generatedRois) {
                if (region.getId() == id) {
                    ok = false;
                    id = region.getId() + 1;
                    break;
                }
            }
        }
        return id;
    }

    /*
     * return the generated ROIs
     */
    public TreeSet<Region> getGeneratedRois() {
        return generatedRois;
    }

    /**
     * generate unique, randomly located Roi in shape of circle
     *
     * @param parent
     * @param diameter
     * @param overlapCircleOk - if indicate overlap circle is OK
     * @return
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    private Region generateUniqueRandomCircleRoi(Region parent, int diameter, boolean overlapCircleOk) throws RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        // make sure parent is preprocessed
        if (!parent.isPreprocessed()) {
            throw new RegionNotPreprocessedException(""); // want calling class/method to do the preprocess ... more relavent context can be made
        }

        System.out.println("looking at region: id:" + parent.getId() + "; area:" + parent.getArea() + "; x/y:" + parent.getEnclosingRectangleX() + "/" + parent.getEnclosingRectangleY() + "; region width:" + parent.getEnclosingRectangleWidth() + ", region height:" + parent.getEnclosingRectangleHeight());

        Region r = null;
        int triesCount = 1;

        Area a = new Area(parent.getRoi().getPolygon());

        int length = diameter;

        while (r == null) {
            if (triesCount > MAX_TRIES) {
                throw new RegionGeneratorMaxTriesExceeded("");
            }

            r = new Region();
            r.setId(generateUniqueRegionId());
            // x1,y1 of the top left corner of the square binding the circle
            int x1 = randomNumberGenerator.nextInt(parent.getEnclosingRectangleWidth());
            int y1 = randomNumberGenerator.nextInt(parent.getEnclosingRectangleHeight());
            while (!(a.contains(x1, y1) && a.contains(x1 + length, y1 + length))) {
                x1 = randomNumberGenerator.nextInt(parent.getEnclosingRectangleWidth());
                y1 = randomNumberGenerator.nextInt(parent.getEnclosingRectangleHeight());
                triesCount++; // increment number of tries
                if (triesCount > MAX_TRIES) {
                    throw new RegionGeneratorMaxTriesExceeded("");
                }
            }
            // put x1,y1 back to coordinates system of the whole slide (instead of within region)
            x1 = x1 + parent.getEnclosingRectangleX();
            y1 = y1 + parent.getEnclosingRectangleY();

            int x2 = x1 + length;
            int y2 = y1 + length;
            if (a.contains(x1 - parent.getEnclosingRectangleX(), y1 - parent.getEnclosingRectangleY(), length, length)) {
                Vertex v1 = new Vertex();
                v1.setX(x1);
                v1.setY(y1);

                Vertex v2 = new Vertex();
                v2.setX(x2);
                v2.setY(y2);

                System.out.println("trying x1/y1, x2/y2: " + x1 + "/" + y1 + ", " + x2 + "/" + y2 + " ...");

                r.setType(Region.REGION_TYPE_ELLIPSE);
                Vertices v = new Vertices();
                v.addVertex(v1);
                v.addVertex(v2);

                r.setVertices(v);

                // check newly generated region ...
                r.preprocess(
                        parent.getEnclosingRectangleX() + parent.getEnclosingRectangleWidth(),
                        parent.getEnclosingRectangleY() + parent.getEnclosingRectangleHeight());

                System.out.println("generated region (" + triesCount + ")... x/y: " + r.getEnclosingRectangleX() + "/" + r.getEnclosingRectangleY() + ", width/height: " + r.getEnclosingRectangleWidth() + "/" + r.getEnclosingRectangleHeight());
                if (parent.containsBoundsOf(r)) {
                    System.out.println("found inside");
                    if (!overlapCircleOk) {
                        // check to see if r overlap with any generated rois
                        for (Region region : generatedRois) {
                            if (region.intersectsBoundsOf(r)) {
                                r = null; // try again!!!
                                break; // break for loop
                            }
                        }
                        // no intersection detected ... this means unique circle generated!!!
                        if (r != null) {
                            // generate random rotation 
                            r.setRandomRotationInDegree(Math.round(randomNumberGenerator.nextFloat() * 360));
                            generatedRois.add(r);
                            System.out.println("region generated successfully!!!");
                        }
                    } else {
                        // generate random rotation 
                        r.setRandomRotationInDegree(Math.round(randomNumberGenerator.nextFloat() * 360));
                        generatedRois.add(r);
                        System.out.println("region (overlap OK) generated successfully!!!");
                    }
                } else {
                    r = null; // try again!!!
                }
                triesCount++; // increment number of tries
            } else {
                // (parent.getRoi().contains(x1, y1) && parent.getRoi().contains(x2, y2)) == false
                triesCount++; // increment number of tries
                r = null; // try again!!!
            }
        }

        return r;
    }

    /**
     * generate circle roi with specified x/y coordinates
     *
     * @param x - x-coord of centre; coordinates system of the whole slide (not within region)
     * @param y - y-coord of centre; coordinates system of the whole slide (not within region)
     * @param diameter in pixel
     * @return
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     */
    public void generateCircleRoi(int x, int y, int diameter) throws RegionNotPreprocessedException, RegionNotSupportedException {
        int radius = diameter/2;
        int x1 = x - radius;
        int y1 = y - radius;
        int x2 = x + radius;
        int y2 = y + radius;
        Region r = new Region();
        r.setId(generateUniqueRegionId());
        Vertex v1 = new Vertex();
        v1.setX(x1);
        v1.setY(y1);

        Vertex v2 = new Vertex();
        v2.setX(x2);
        v2.setY(y2);

        System.out.println("trying x1/y1, x2/y2: " + x1 + "/" + y1 + ", " + x2 + "/" + y2 + " ...");

        r.setType(Region.REGION_TYPE_ELLIPSE);
        Vertices v = new Vertices();
        v.addVertex(v1);
        v.addVertex(v2);

        r.setVertices(v);

        // check newly generated region ...
        Region parent = contextRegions.getRegions().first(); // assume must be only ONE context region
        r.preprocess(
                parent.getEnclosingRectangleX() + parent.getEnclosingRectangleWidth(),
                parent.getEnclosingRectangleY() + parent.getEnclosingRectangleHeight());
        System.out.println("parent ... x/y="+parent.getEnclosingRectangleX()+"/"+parent.getEnclosingRectangleY()+", width/height="+parent.getEnclosingRectangleWidth()+"/"+parent.getEnclosingRectangleHeight());
        System.out.println("after preprocess ... x/y="+r.getEnclosingRectangleX()+"/"+r.getEnclosingRectangleY()+", width/height="+r.getEnclosingRectangleWidth()+"/"+r.getEnclosingRectangleHeight());
        
        generatedRois.add(r);
    }

    /**
     * public interface for TESTIBNG this method ... DO NOT USE IN PRODUCTION
     * CODE
     *
     * @param parent
     * @param diameter
     * @param overlapCircleOk
     * @return
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    public Region forTestOnly_generateUniqueRandomCircleRoi(Region parent, int diameter, boolean overlapCircleOk) throws RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        return generateUniqueRandomCircleRoi(parent, diameter, overlapCircleOk);
    }

    /**
     * helper function to generateUniqueRandomCircleRoi(int diameter)
     *
     * @param diameter
     * @param overlapCircleOk - if indicate overlap circle is OK
     * @param triesCount - this is NOT the same as triesCount in
     * generateUniqueRandomCircleRoi(Region parent, int diameter)
     * @return
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    private Region generateUniqueRandomCircleRoi(int diameter, boolean overlapCircleOk, int triesCount) throws RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        if (triesCount > contextRegions.getRegions().size() * 5) {
            throw new RegionGeneratorMaxTriesExceeded("");
        }
        int index = 0;
        int numberOfRegions = contextRegions.getRegions().size();
        if (contextRegions.getRegions().size() > 1) {
            // choose which region to generate roi
            // since not all regions are same size, need to scale by size so that 
            // we look more often on region with large size
            index = randomNumberGenerator.nextInt(Math.max(0, numberOfRegions));
            while (randomNumberGenerator.nextDouble() > regionWeights[index]) {
                index = randomNumberGenerator.nextInt(Math.max(0, numberOfRegions));
            }
        }
        int count = 0;
        for (Region region : contextRegions.getRegions()) {
            if (count == index) {
                try {
                    return generateUniqueRandomCircleRoi(region, diameter, overlapCircleOk);
                } catch (RegionGeneratorMaxTriesExceeded rgmte) {
                    // may be this region cannot accomodate the ROI ... try another region
                    triesCount++;
                    return generateUniqueRandomCircleRoi(diameter, overlapCircleOk, triesCount);
                }
            }
            count++;
        }
        return null;
    }

    /**
     * generate unique, randomly located Roi in shape of circle ... randomly
     * choose the region. NOTE: if there is only one region in this annotation,
     * then this method will have the same result as
     * generateUniqueRandomCircleRoi(Region parent, int diameter)
     *
     * @param diameter
     * @param overlapCircleOk - if indicate overlap circle is OK
     * @return
     * @throws RegionNotPreprocessedException
     * @throws RegionNotSupportedException
     * @throws RegionGeneratorMaxTriesExceeded
     */
    public Region generateUniqueRandomCircleRoi(int diameter, boolean overlapCircleOk) throws RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded {
        if (contextRegions.getRegions().isEmpty()) {
            return null; // CANNOT generate any ROI since no region inside regions
        }
        return generateUniqueRandomCircleRoi(diameter, overlapCircleOk, 0);
    }
}
