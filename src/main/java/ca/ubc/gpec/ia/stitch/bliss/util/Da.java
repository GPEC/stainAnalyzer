package ca.ubc.gpec.ia.stitch.bliss.util;

import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * storing info regarding a single Da file
 *
 * @author sleung
 *
 */
public class Da {

    protected StitchedCoordinates stitchedCoordinates;
    protected String name; //abs path + filename of Da file e.g. Da23.jpg
    protected int correctionFactorX; // in pixel coordinates
    protected int correctionFactorY; // in pixel coordinates
    public static int[] DEFAULT_PIXEL_RGB = {255, 255, 255};

    /**
     * set default pixel color for area that are not scanned
     *
     * @param red
     * @param green
     * @param blue
     */
    public static void setDefaultPixelRGB(int red, int green, int blue) {
        DEFAULT_PIXEL_RGB[0] = red;
        DEFAULT_PIXEL_RGB[1] = green;
        DEFAULT_PIXEL_RGB[2] = blue;
    }

    /**
     * constructor
     *
     * @param name
     * @param daCoordinates
     */
    public Da(String name, StitchedCoordinates stitchedCoordinates, int correctionFactorX, int correctionFactorY) {
        this.name = name;
        this.stitchedCoordinates = stitchedCoordinates;
        this.correctionFactorX = correctionFactorX;
        this.correctionFactorY = correctionFactorY;
    }

    public StitchedCoordinates getStitchedCoordinates() {
        return stitchedCoordinates;
    }

    public String getName() {
        return name;
    }

    /**
     * return x,y coordinates of Da file given x,y coordinates from stitched
     * file assume stitched x,y coordinates is within this Da file
     *
     * @param stitchedX
     * @param stitchedY
     * @return
     */
    private int[] getDaXY(int x, int y) {
        return new int[]{x - stitchedCoordinates.getMinX() + correctionFactorX / 2, y - stitchedCoordinates.getMinY() + correctionFactorY / 2};
    }

    /**
     * get RGB info for pixel specified by stitchedX, stitchedY assume stitched
     * x,y coordinates is within this Da file
     *
     * @param stitchedX
     * @param stitchedY
     * @param fileFetcher
     * @return
     */
    public int[] getRGB(int stitchedX, int stitchedY, FileFetcher fileFetcher) {
        int[] localCoordinates = getDaXY(stitchedX, stitchedY);
        return getRGBFromFile(localCoordinates[0], localCoordinates[1], fileFetcher);
    }

    /**
     * get RGB pixel from actual file
     *
     * @param stitchedX
     * @param stitchedY
     * @return
     */
    protected int[] getRGBFromFile(int stitchedX, int stitchedY, FileFetcher fileFetcher) {
        // do on demand fetching for now
        //System.out.println("local xy="+stitchedX+" "+stitchedY);
        ImageProcessor ip = fileFetcher.getImageProcessor(name);
        int[] rgb = ip.getPixel(stitchedX, stitchedY, null);
        if (rgb.length < 3) {
            System.out.println("CRASHING!!! " + name);
        }
        return (rgb);
    }

    /**
     * main function for process test
     *
     * @param args
     */
    public static void main(String[] args) {
        StitchedCoordinates daC1 = new StitchedCoordinates(0, 5, 0, 5, 5, 5);
        StitchedCoordinates daC2 = new StitchedCoordinates(0, 0, 0, 0, 5, 5);

        if (daC1.equals(daC2)) {
            System.out.println("equal");
        } else {
            System.out.println("not equal");
        }
    }
}
